// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache 2.0 license.
// Please see LICENSE file in the project root for terms.
package com.yahoo.labs.yamall.hadoop;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.TreeMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import com.yahoo.labs.yamall.core.Instance;
import com.yahoo.labs.yamall.hadoop.core.CompositeDoubleTextWritable;
import com.yahoo.labs.yamall.ml.IOLearner;
import com.yahoo.labs.yamall.ml.Learner;
import com.yahoo.labs.yamall.parser.InstanceParser;
import com.yahoo.labs.yamall.parser.TSVParser;
import com.yahoo.labs.yamall.parser.VWParser;

/**
 * Yamall - Hadoop version
 * 
 * Test a yamall model.
 * 
 * @author Francesco Orabona
 * @version 1.0
 */
public class Test extends Configured implements Tool {

    /**
     * Mapper - Read examples and write them
     * 
     * @author Francesco Orabona, (francesco@yahoo-inc.com)
     *
     */
    public static class TestMapper extends Mapper<Object, Text, DoubleWritable, CompositeDoubleTextWritable> {

        private static final String MODEL_BIN = "model.bin";
        private InstanceParser parser;
        private Learner learner;
        private static final String SPEC_FILE = "spec.txt";

        @Override
        protected void setup(Mapper<Object, Text, DoubleWritable, CompositeDoubleTextWritable>.Context context)
                throws IOException, InterruptedException {
            super.setup(context);

            Configuration config = context.getConfiguration();

            // move model to the node
            FileSystem fileSystem = FileSystem.get(config);
            fileSystem.copyToLocalFile(new Path(config.get("yamall.vw_model")), new Path(MODEL_BIN));

            learner = IOLearner.loadLearner(MODEL_BIN);

            if (config.get("yamall.parser").equals("vw"))
                parser = new VWParser(Integer.parseInt(config.get("yamall.bit_precision")), config.get("yamall.ignore"),
                        false);
            else {
                fileSystem.copyToLocalFile(new Path(config.get("yamall.parser_spec")), new Path(SPEC_FILE));

                String spec = new String(Files.readAllBytes(Paths.get(SPEC_FILE)));

                parser = new TSVParser(Integer.parseInt(config.get("yamall.bit_precision")),
                        config.get("yamall.ignore"), false, spec);
            }
        }

        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            Instance sample = parser.parse(value.toString());

            double pred = learner.predict(sample);

            CompositeDoubleTextWritable dt = new CompositeDoubleTextWritable(sample.getLabel(), sample.getTag());

            context.write(new DoubleWritable(pred), dt);
        }
    }

    /**
     * Reducer - For each example, sum losses and weights.
     * 
     * @author Francesco Orabona, (francesco@yahoo-inc.com)
     *
     */
    public static class TestReducer extends Reducer<DoubleWritable, CompositeDoubleTextWritable, NullWritable, Text> {

        long position = 1;
        double sumPosRanks = 0;
        long positiveLabels = 0;
        long negativeLabels = 0;
        long correct = 0;
        double sumLosses = 0;
        private MultipleOutputs<NullWritable, Text> writer;

        protected void setup(Reducer<DoubleWritable, CompositeDoubleTextWritable, NullWritable, Text>.Context context)
                throws IOException, InterruptedException {
            super.setup(context);
            writer = new MultipleOutputs<>(context);
        }

        /**
         * predictions and labels-tags arrive
         */
        public void reduce(DoubleWritable key, Iterable<CompositeDoubleTextWritable> values, Context context)
                throws IOException, InterruptedException {

            int count = 0;
            double sumPositions = 0;
            int numPositive = 0;
            for (CompositeDoubleTextWritable val : values) {
                double label = val.val1;
                sumPositions = sumPositions + position;
                position++;
                count++;
                if (label > 0) {
                    numPositive++;
                    positiveLabels++;
                }
                else
                    negativeLabels++;
                if (label * key.get() > 0)
                    correct++;
                sumLosses += Math.log(Math.exp(-label * key.get()) + 1);
                writer.write("out", NullWritable.get(),
                        val.val2 + "\t" + Double.toString(label) + "\t" + 1.0 / (1.0 + Math.exp(-key.get())), "scores");
            }

            double tiedRank = (double) sumPositions / (double) count;

            sumPosRanks = sumPosRanks + numPositive * tiedRank;
        }

        protected void cleanup(Reducer<DoubleWritable, CompositeDoubleTextWritable, NullWritable, Text>.Context context)
                throws IOException, InterruptedException {

            double auc = (sumPosRanks - positiveLabels * (positiveLabels + 1) / 2) / (positiveLabels * negativeLabels);
            double err = 1.0 - (double) correct / ((double) positiveLabels + (double) negativeLabels);
            double logloss = sumLosses / ((double) positiveLabels + (double) negativeLabels);

            context.write(NullWritable.get(), new Text("AUC: " + Double.toString(auc) + " Err: " + Double.toString(err)
                    + " LogLoss: " + Double.toString(logloss)));
            writer.close();

            super.cleanup(context);
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
        conf.set("yamall.vw_model", args[2]);
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

        Job job = Job.getInstance(conf, "Yamall Test on MapReduce");
        job.setNumReduceTasks(1);
        job.setJarByClass(Test.class);
        job.setMapperClass(TestMapper.class);
        job.setMapOutputKeyClass(DoubleWritable.class);
        job.setReducerClass(TestReducer.class);
        job.setOutputKeyClass(NullWritable.class);
        job.setOutputValueClass(CompositeDoubleTextWritable.class);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        MultipleOutputs.addNamedOutput(job, "out", TextOutputFormat.class, NullWritable.class, Text.class);

        return job.waitForCompletion(true) ? 0 : 1;
    }

    public static void main(final String[] args) throws Exception {
        Configuration conf = new Configuration();
        int res = ToolRunner.run(conf, new Test(), args);
        System.exit(res);
    }
}
