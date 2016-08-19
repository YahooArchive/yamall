// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache 2.0 license.
// Please see LICENSE file in the project root for terms.
package com.yahoo.labs.yamall.hadoop;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import com.yahoo.labs.yamall.core.Instance;
import com.yahoo.labs.yamall.hadoop.core.HashMapInt2StringWritable;
import com.yahoo.labs.yamall.hadoop.core.InstanceNoTagWritable;
import com.yahoo.labs.yamall.hadoop.core.InstanceOrHashMapWritable;
import com.yahoo.labs.yamall.hadoop.core.InstanceOrHashMapWritable.TypeWritable;
import com.yahoo.labs.yamall.ml.IOLearner;
import com.yahoo.labs.yamall.ml.Learner;
import com.yahoo.labs.yamall.ml.LogisticLoss;
import com.yahoo.labs.yamall.ml.SGD_VW;
import com.yahoo.labs.yamall.parser.InstanceParser;
import com.yahoo.labs.yamall.parser.TSVParser;
import com.yahoo.labs.yamall.parser.VWParser;

/**
 * Yamall - Hadoop version
 * 
 * Train a yamall model.
 * 
 * @author Guy Halawi, Francesco Orabona
 * @version 1.0
 */
public class Train extends Configured implements Tool {

    /**
     * Mapper - Read examples and write them shuffled
     * 
     * @author ghalawi
     *
     */
    public static class TrainMapper extends Mapper<Object, Text, DoubleWritable, InstanceOrHashMapWritable> {

        private Random rand;
        private InstanceParser parser;
        private static final String SPEC_FILE = "spec.txt";

        @Override
        protected void setup(Mapper<Object, Text, DoubleWritable, InstanceOrHashMapWritable>.Context context)
                throws IOException, InterruptedException {
            super.setup(context);
            rand = new Random();

            Configuration config = context.getConfiguration();

            if (config.get("yamall.parser").equals("vw"))
                parser = new VWParser(Integer.parseInt(config.get("yamall.bit_precision")), config.get("yamall.ignore"),
                        true);
            else {
                FileSystem fileSystem = FileSystem.get(config);
                fileSystem.copyToLocalFile(new Path(config.get("yamall.parser_spec")), new Path(SPEC_FILE));

                String spec = new String(Files.readAllBytes(Paths.get(SPEC_FILE)));

                parser = new TSVParser(Integer.parseInt(config.get("yamall.bit_precision")),
                        config.get("yamall.ignore"), true, spec);
            }
        }

        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {

            Instance sample = parser.parse(value.toString());

            InstanceNoTagWritable tmp = new InstanceNoTagWritable();
            tmp.setLabel(sample.getLabel());
            tmp.setWeight(sample.getWeight());
            tmp.getSparseVector().setEntries(sample.getVector());

            // key is random because we want to shuffle the examples
            context.write(new DoubleWritable(rand.nextDouble()), new InstanceOrHashMapWritable(tmp));
        }

        protected void cleanup(Mapper<Object, Text, DoubleWritable, InstanceOrHashMapWritable>.Context context)
                throws IOException, InterruptedException {

            InstanceOrHashMapWritable tmp = new InstanceOrHashMapWritable(
                    new HashMapInt2StringWritable(parser.getInvertHashMap()));
            context.write(new DoubleWritable(rand.nextDouble()), tmp);

            super.cleanup(context);
        }
    }

    /**
     * Reducer - For each example arrived, push it to learner class. When finished, save the model and move to HDFS
     * 
     * @author ghalawi
     *
     */
    public static class TrainReducer extends Reducer<DoubleWritable, InstanceOrHashMapWritable, Text, Text> {

        private static final String MODEL_BIN = "model.bin";
        private static final String MODEL_TXT = "model.txt";
        private Learner learner;
        private HashMapInt2StringWritable hm;

        /**
         * Reducer starts
         */
        @Override
        protected void setup(Reducer<DoubleWritable, InstanceOrHashMapWritable, Text, Text>.Context context)
                throws IOException, InterruptedException {
            super.setup(context);

            Configuration config = context.getConfiguration();

            // initialize learner and loss
            learner = new SGD_VW(Integer.parseInt(config.get("yamall.bit_precision")));
            // learner = new PerCoordinatePiSTOL(18);
            learner.setLoss(new LogisticLoss());
            hm = new HashMapInt2StringWritable();
        }

        /**
         * Reducer ends
         */
        @Override
        protected void cleanup(Reducer<DoubleWritable, InstanceOrHashMapWritable, Text, Text>.Context context)
                throws IOException, InterruptedException {

            Configuration config = context.getConfiguration();

            // save the model to local file
            IOLearner.saveLearner(learner, MODEL_BIN);
            // move it to HDFS
            FileSystem fileSystem = FileSystem.get(config);
            fileSystem.moveFromLocalFile(new Path(MODEL_BIN), new Path(config.get("yamall.output")));

            // save the readable model to local file
            IOLearner.saveInvertHash(learner.getWeights(), hm.getEntries(), MODEL_TXT);
            // move it to HDFS
            fileSystem.moveFromLocalFile(new Path(MODEL_TXT), new Path(config.get("yamall.output")));

            super.cleanup(context);
        }

        /**
         * Examples arrive
         */
        public void reduce(DoubleWritable key, Iterable<InstanceOrHashMapWritable> values, Context context)
                throws IOException, InterruptedException {

            Instance sample = new Instance();

            // feed with examples
            for (InstanceOrHashMapWritable val : values) {
                if (val.getType() == TypeWritable.INSTANCE) {
                    InstanceNoTagWritable tmp = (InstanceNoTagWritable) (val.getObject());
                    sample.setLabel(tmp.getLabel());
                    sample.setWeight(tmp.getWeight());
                    sample.setVector(tmp.getSparseVector().getEntries());
                    learner.update(sample);
                }
                else
                    hm.merge((HashMapInt2StringWritable) val.getObject());
            }
        }
    }

    public static void startLogger(Level level) {
        Logger rootLogger = Logger.getRootLogger();
        rootLogger.setLevel(level);
        ConsoleAppender ca = new ConsoleAppender();
        ca.setWriter(new OutputStreamWriter(System.out));
        ca.setLayout(new PatternLayout("%-5p [%t]: %m%n"));
        rootLogger.addAppender(ca);
    }

    /**
     * Run the map/reduce job
     */
    public final int run(final String[] args) throws Exception {

        startLogger(Level.INFO);

        Configuration conf = getConf();
        conf.set("yamall.output", args[1]);
        conf.setIfUnset("yamall.bit_precision", "18");
        conf.setIfUnset("yamall.parser", "vw");

        // Print to screen all the options
        TreeMap<String, String> map = new TreeMap<String, String>();
        for (Map.Entry<String, String> entry : conf) {
            map.put(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<String, String> entry : map.entrySet()) {
            System.out.printf("%s=%s\n", entry.getKey(), entry.getValue());
        }

        Job job = Job.getInstance(conf, "Yamall Train on MapReduce");
        job.setNumReduceTasks(1); // important
        job.setJarByClass(Train.class);
        job.setMapperClass(TrainMapper.class);
        job.setMapOutputKeyClass(DoubleWritable.class);
        job.setMapOutputValueClass(InstanceOrHashMapWritable.class);
        job.setReducerClass(TrainReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        return job.waitForCompletion(true) ? 0 : 1;
    }

    public static void main(final String[] args) throws Exception {
        Configuration conf = new Configuration();
        int res = ToolRunner.run(conf, new Train(), args);
        System.exit(res);
    }
}
