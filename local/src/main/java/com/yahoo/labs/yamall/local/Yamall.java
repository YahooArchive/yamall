// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache 2.0 license.
// Please see LICENSE file in the project root for terms.
package com.yahoo.labs.yamall.local;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.yahoo.labs.yamall.core.Instance;
import com.yahoo.labs.yamall.ml.AbsLoss;
import com.yahoo.labs.yamall.ml.COCOB;
import com.yahoo.labs.yamall.ml.HingeLoss;
import com.yahoo.labs.yamall.ml.IOLearner;
import com.yahoo.labs.yamall.ml.IdentityLinkFunction;
import com.yahoo.labs.yamall.ml.KT;
import com.yahoo.labs.yamall.ml.Learner;
import com.yahoo.labs.yamall.ml.LinkFunction;
import com.yahoo.labs.yamall.ml.LogisticLinkFunction;
import com.yahoo.labs.yamall.ml.LogisticLoss;
import com.yahoo.labs.yamall.ml.Loss;
import com.yahoo.labs.yamall.ml.PerCoordinateCOCOB;
import com.yahoo.labs.yamall.ml.PerCoordinateKT;
import com.yahoo.labs.yamall.ml.PerCoordinatePiSTOL;
import com.yahoo.labs.yamall.ml.PerCoordinateSOLO;
import com.yahoo.labs.yamall.ml.SGD_FM;
import com.yahoo.labs.yamall.ml.SGD_VW;
import com.yahoo.labs.yamall.ml.SOLO;
import com.yahoo.labs.yamall.ml.SquareLoss;
import com.yahoo.labs.yamall.parser.VWParser;

public class Yamall {

    private static Options options = new Options();

    private static Learner learner = null;
    private static double minPrediction = 0;
    private static double maxPrediction = 0;
    private static int fmNumberFactors = 0;
    private static boolean binary = false;

    public static void main(String[] args) {
        String[] remainingArgs = null;
        String inputFile = null;
        String predsFile = null;
        String saveModelFile = null;
        String initialModelFile = null;
        String lossName = null;
        String linkName = null;
        String invertHashName = null;
        double learningRate = 1;
        String minPredictionString = null;
        String maxPredictionString = null;
        String fmNumberFactorsString = null;
        int bitsHash;
        int numberPasses;
        int holdoutPeriod = 10;

        boolean testOnly = false;
        boolean exponentialProgress;
        double progressInterval;

        options.addOption("h", "help", false, "displays this help");
        options.addOption("t", false, "ignore label information and just test");
        options.addOption(Option.builder().hasArg(false).required(false).longOpt("binary")
                .desc("reports loss as binary classification with -1,1 labels").build());
        options.addOption(
                Option.builder().hasArg(false).required(false).longOpt("solo").desc("uses SOLO optimizer").build());
        options.addOption(Option.builder().hasArg(false).required(false).longOpt("pcsolo")
                .desc("uses Per Coordinate SOLO optimizer").build());
        options.addOption(
                Option.builder().hasArg(false).required(false).longOpt("pistol").desc("uses PiSTOL optimizer").build());
        options.addOption(Option.builder().hasArg(false).required(false).longOpt("kt")
                .desc("(EXPERIMENTAL) uses KT optimizer").build());
        options.addOption(Option.builder().hasArg(false).required(false).longOpt("pckt")
                .desc("(EXPERIMENTAL) uses Per Coordinate KT optimizer").build());
        options.addOption(Option.builder().hasArg(false).required(false).longOpt("pccocob")
                .desc("(EXPERIMENTAL) uses Per Coordinate COCOB optimizer").build());
        options.addOption(Option.builder().hasArg(false).required(false).longOpt("cocob")
                .desc("(EXPERIMENTAL) uses COCOB optimizer").build());
        options.addOption(Option.builder().hasArg(false).required(false).longOpt("fm")
                .desc("Factorization Machine").build());
        options.addOption(Option.builder("f").hasArg(true).required(false).desc("final regressor to save")
                .type(String.class).longOpt("final_regressor").build());
        options.addOption(Option.builder("p").hasArg(true).required(false).desc("file to output predictions to")
                .longOpt("predictions").type(String.class).build());
        options.addOption(
                Option.builder("i").hasArg(true).required(false).desc("initial regressor(s) to load into memory")
                        .longOpt("initial_regressor").type(String.class).build());
        options.addOption(Option.builder().hasArg(true).required(false)
                .desc("specify the loss function to be used. Currently available ones are: absolute, squared (default), hinge, logistic")
                .longOpt("loss_function").type(String.class).build());
        options.addOption(Option.builder().hasArg(true).required(false)
                .desc("specify the link function used in the output of the predictions. Currently available ones are: identity (default), logistic")
                .longOpt("link").type(String.class).build());
        options.addOption(Option.builder().hasArg(true).required(false)
                .desc("output human-readable final regressor with feature names").longOpt("invert_hash")
                .type(String.class).build());
        options.addOption(
                Option.builder("l").hasArg(true).required(false).desc("set (initial) learning Rate, default = 1.0")
                        .longOpt("learning_rate").type(String.class).build());
        options.addOption(Option.builder("b").hasArg(true).required(false)
                .desc("number of bits in the feature table, default = 18").longOpt("bit_precision").type(String.class)
                .build());
        options.addOption(Option.builder("P").hasArg(true).required(false)
                .desc("progress update frequency, integer: additive; float: multiplicative, default = 2.0")
                .longOpt("progress").type(String.class).build());
        options.addOption(Option.builder().hasArg(true).required(false)
                .desc("smallest prediction to output, before the link function, default = -50")
                .longOpt("min_prediction").type(String.class).build());
        options.addOption(Option.builder().hasArg(true).required(false)
                .desc("smallest prediction to output, before the link function, default = 50").longOpt("max_prediction")
                .type(String.class).build());
        options.addOption(Option.builder().hasArg(true).required(false)
                .desc("ignore namespaces beginning with the characters in <arg>").longOpt("ignore").type(String.class)
                .build());
        options.addOption(Option.builder().hasArg(true).required(false).desc("number of training passes")
                .longOpt("passes").type(String.class).build());
        options.addOption(
                Option.builder().hasArg(true).required(false).desc("holdout period for test only, default = 10")
                        .longOpt("holdout_period").type(String.class).build());
        options.addOption(Option.builder().hasArg(true).required(false)
                .desc("number of factors for Factorization Machines default = 8")
                .longOpt("fmNumberFactors").type(String.class).build());

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        }
        catch (ParseException e) {
            System.out.println("Unrecognized option");
            help();
        }
        if (cmd.hasOption("h"))
            help();
        if (cmd.hasOption("t"))
            testOnly = true;
        if (cmd.hasOption("binary")) {
            binary = true;
            System.out.println("Reporting binary loss");
        }
        initialModelFile = cmd.getOptionValue("i");
        predsFile = cmd.getOptionValue("p");
        lossName = cmd.getOptionValue("loss_function", "squared");
        linkName = cmd.getOptionValue("link", "identity");
        saveModelFile = cmd.getOptionValue("f");
        learningRate = Double.parseDouble(cmd.getOptionValue("l", "1.0"));
        bitsHash = Integer.parseInt(cmd.getOptionValue("b", "18"));
        invertHashName = cmd.getOptionValue("invert_hash");
        minPredictionString = cmd.getOptionValue("min_prediction", "-50");
        maxPredictionString = cmd.getOptionValue("max_prediction", "50");
        fmNumberFactorsString = cmd.getOptionValue("fmNumberFactors", "8");

        numberPasses = Integer.parseInt(cmd.getOptionValue("passes", "1"));
        System.out.println("Number of passes = " + numberPasses);
        if (numberPasses > 1) {
            holdoutPeriod = Integer.parseInt(cmd.getOptionValue("holdout_period", "10"));
            System.out.println("Holdout period = " + holdoutPeriod);
        }

        remainingArgs = cmd.getArgs();
        if (remainingArgs.length == 1)
            inputFile = remainingArgs[0];
        VWParser vwparser = new VWParser(bitsHash, cmd.getOptionValue("ignore"), (invertHashName != null));
        System.out.println("Num weight bits = " + bitsHash);

        // setup progress
        String progress = cmd.getOptionValue("P", "2.0");
        if (progress.indexOf('.') >= 0) {
            exponentialProgress = true;
            progressInterval = (double) Double.parseDouble(progress);
        }
        else {
            exponentialProgress = false;
            progressInterval = (double) Integer.parseInt(progress);
        }

        // min and max predictions
        minPrediction = (double) Double.parseDouble(minPredictionString);
        maxPrediction = (double) Double.parseDouble(maxPredictionString);
        
        // number of factors for Factorization Machines
        fmNumberFactors = (int) Integer.parseInt(fmNumberFactorsString);

        // configure the learner
        Loss lossFnc = null;
        LinkFunction link = null;
        if (initialModelFile == null) {
            if (cmd.hasOption("kt")) {
                learner = new KT(bitsHash);
            }
            else if (cmd.hasOption("pckt")) {
                learner = new PerCoordinateKT(bitsHash);
            }
            else if (cmd.hasOption("pcsolo")) {
                learner = new PerCoordinateSOLO(bitsHash);
            }
            else if (cmd.hasOption("solo")) {
                learner = new SOLO(bitsHash);
            }
            else if (cmd.hasOption("pccocob")) {
                learner = new PerCoordinateCOCOB(bitsHash);
            }
            else if (cmd.hasOption("cocob")) {
                learner = new COCOB(bitsHash);
            }
            else if (cmd.hasOption("pistol")) {
                learner = new PerCoordinatePiSTOL(bitsHash);
            }
            else if (cmd.hasOption("fm")) {
            	learner = new SGD_FM(bitsHash, fmNumberFactors);
            }
            else
                learner = new SGD_VW(bitsHash);
        }
        else {
            learner = IOLearner.loadLearner(initialModelFile);
        }

        // setup link function
        if (linkName.equals("identity")) {
            link = new IdentityLinkFunction();
        }
        else if (linkName.equals("logistic")) {
            link = new LogisticLinkFunction();
        }
        else {
            System.out.println("Unknown link function.");
            System.exit(0);
        }

        // setup loss function
        if (lossName.equals("squared")) {
            lossFnc = new SquareLoss();
        }
        else if (lossName.equals("hinge")) {
            lossFnc = new HingeLoss();
        }
        else if (lossName.equals("logistic")) {
            lossFnc = new LogisticLoss();
        }
        else if (lossName.equals("absolute")) {
            lossFnc = new AbsLoss();
        }
        else {
            System.out.println("Unknown loss function.");
            System.exit(0);
        }

        learner.setLoss(lossFnc);
        
        
        learner.setLearningRate(learningRate);

        // maximum range predictions
        System.out.println("Max prediction = " + maxPrediction + ", Min Prediction = " + minPrediction);
        // print information about the learner
        System.out.println(learner.toString());
        // print information about the link function
        System.out.println(link.toString());
        // print information about ignored namespaces
        System.out.println("Ignored namespaces = " + cmd.getOptionValue("ignore", ""));

        long start = System.nanoTime();
        FileInputStream fstream;
        try {
            BufferedReader br = null;
            if (inputFile != null) {
                fstream = new FileInputStream(inputFile);
                System.out.println("Reading datafile = " + inputFile);
                br = new BufferedReader(new InputStreamReader(fstream));
            }
            else {
                System.out.println("Reading from console");
                br = new BufferedReader(new InputStreamReader(System.in));
            }

            File fout = null;
            FileOutputStream fos = null;
            BufferedWriter bw = null;
            if (predsFile != null) {
                fout = new File(predsFile);
                fos = new FileOutputStream(fout);
                bw = new BufferedWriter(new OutputStreamWriter(fos));
            }

            try {
                System.out.println("average       example  current  current  current");
                System.out.println("loss          counter    label  predict  features");
                int iter = 0;
                double cumLoss = 0;
                double weightedSampleSum = 0;
                double sPlus = 0;
                double sMinus = 0;
                Instance sample = null;
                boolean justPrinted = false;
                int pass = 0;
                ObjectOutputStream ooutTr = null;
                ObjectOutputStream ooutHO = null;
                ObjectInputStream oinTr = null;
                double pred = 0;
                int limit = 1;
                double hError = Double.MAX_VALUE;
                double lastHError = Double.MAX_VALUE;
                int numTestSample = 0;
                int numTrainingSample = 0;
                int idx = 0;

                if (numberPasses > 1) {
                    ooutTr = new ObjectOutputStream(new FileOutputStream("cache_training.bin"));
                    ooutHO = new ObjectOutputStream(new FileOutputStream("cache_holdout.bin"));
                    oinTr = new ObjectInputStream(new FileInputStream("cache_training.bin"));
                }

                do {
                    while (true) {
                        double score;

                        if (pass > 0 && numberPasses > 1) {
                            Instance tmp = (Instance) oinTr.readObject();
                            if (tmp != null)
                                sample = tmp;
                            else
                                break;
                        }
                        else {
                            String strLine = br.readLine();
                            if (strLine != null)
                                sample = vwparser.parse(strLine);
                            else
                                break;
                        }

                        justPrinted = false;
                        idx++;

                        if (numberPasses > 1 && pass == 0 && idx % holdoutPeriod == 0) {
                            // store the current sample for the holdout set
                            ooutHO.writeObject(sample);
                            ooutHO.reset();
                            numTestSample++;
                        }
                        else {
                            if (numberPasses > 1 && pass == 0) {
                                ooutTr.writeObject(sample);
                                ooutTr.reset();
                                numTrainingSample++;
                            }

                            iter++;
                            if (testOnly) {
                                // predict the sample
                                score = learner.predict(sample);
                            }
                            else {
                                // predict the sample and update the classifier using the sample
                                score = learner.update(sample);
                            }
                            score = Math.min(Math.max(score, minPrediction), maxPrediction);
                            pred = link.apply(score);
                            if (!binary)
                                cumLoss += learner.getLoss().lossValue(score, sample.getLabel()) * sample.getWeight();
                            else if (Math.signum(score) != sample.getLabel())
                                cumLoss += sample.getWeight();

                            weightedSampleSum += sample.getWeight();
                            if (sample.getLabel() > 0)
                                sPlus = sPlus + sample.getWeight();
                            else
                                sMinus = sMinus + sample.getWeight();

                            // output predictions to file
                            if (predsFile != null) {
                                bw.write(String.format("%.6f %s", pred, sample.getTag()));
                                bw.newLine();
                            }

                            // print statistics to screen
                            if (iter == limit) {
                                justPrinted = true;
                                System.out.printf("%.6f %12d  % .4f  % .4f  %d\n", cumLoss / weightedSampleSum, iter,
                                        sample.getLabel(), pred, sample.getVector().size());
                                if (exponentialProgress)
                                    limit *= progressInterval;
                                else
                                    limit += progressInterval;
                            }
                        }
                    }
                    if (numberPasses > 1) {
                        if (pass == 0) { // finished first pass of many
                            // write a null at the end of the files
                            ooutTr.writeObject(null);
                            ooutHO.writeObject(null);
                            ooutTr.flush();
                            ooutHO.flush();
                            ooutTr.close();
                            ooutHO.close();

                            System.out.println("finished first epoch");
                            System.out.println(numTrainingSample + " training samples");
                            System.out.println(numTestSample + " holdout samples saved");
                        }
                        lastHError = hError;
                        hError = evalHoldoutError();
                    }
                    if (numberPasses > 1) {
                        System.out.printf("Weighted loss on holdout on epoch %d = %.6f\n", pass + 1, hError);

                        oinTr.close();
                        oinTr = new ObjectInputStream(new FileInputStream("cache_training.bin"));

                        if (hError > lastHError) {
                            System.out.println("Early stopping");
                            break;
                        }
                    }
                    pass++;
                }
                while (pass < numberPasses);

                if (justPrinted == false) {
                    System.out.printf("%.6f %12d  % .4f  % .4f  %d\n", cumLoss / weightedSampleSum, iter,
                            sample.getLabel(), pred, sample.getVector().size());
                }
                System.out.println("finished run");

                System.out.println(String.format("average loss best constant predictor: %.6f",
                        lossFnc.lossConstantBinaryLabels(sPlus, sMinus)));

                if (saveModelFile != null)
                    IOLearner.saveLearner(learner, saveModelFile);
                if (invertHashName != null)
                    IOLearner.saveInvertHash(learner.getWeights(), vwparser.getInvertHashMap(), invertHashName);
            }
            catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            // close the input stream
            try {
                br.close();
            }
            catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            // close the output stream
            if (predsFile != null) {
                try {
                    bw.close();
                }
                catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            long millis = System.nanoTime() - start;
            System.out.printf("Elapsed time: %d min, %d sec\n", TimeUnit.NANOSECONDS.toMinutes(millis),
                    TimeUnit.NANOSECONDS.toSeconds(millis) - 60 * TimeUnit.NANOSECONDS.toMinutes(millis));
        }
        catch (

        FileNotFoundException e) {
            System.out.println("Error opening the input file");
            e.printStackTrace();
        }

    }

    private static void help() {
        // this prints out some help
        HelpFormatter formater = new HelpFormatter();
        formater.printHelp("yamall", options);
        System.exit(0);
    }

    private static double evalHoldoutError() throws FileNotFoundException, IOException, ClassNotFoundException {
        double cumLoss = 0;
        double weightedSampleSum = 0;
        ObjectInputStream oin = new ObjectInputStream(new FileInputStream("cache_holdout.bin"));

        Instance testSample;
        while ((testSample = (Instance) oin.readObject()) != null) {
            weightedSampleSum += testSample.getWeight();
            double score = learner.predict(testSample);
            score = Math.min(Math.max(score, minPrediction), maxPrediction);
            if (!binary)
                cumLoss += learner.getLoss().lossValue(score, testSample.getLabel()) * testSample.getWeight();
            else if (Math.signum(score) != testSample.getLabel())
                cumLoss += testSample.getWeight();
        }
        oin.close();

        return cumLoss / weightedSampleSum;
    }

}