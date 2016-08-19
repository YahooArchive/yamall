// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache 2.0 license.
// Please see LICENSE file in the project root for terms.
package com.yahoo.labs.yamall.examples;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import com.yahoo.labs.yamall.core.Instance;
import com.yahoo.labs.yamall.parser.VWParser;

/**
 * Example file on how to use the VW parser from yamall
 * 
 * @author Francesco Orabona
 *
 */
public class StatisticsVWFile {

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println(
                    "Usage: java -classpath yamall-examples-jar-with-dependencies.jar com.yahoo.labs.yamall.examples.StatisticsVWFile vw_filename_to_parse");
            System.exit(0);
        }

        FileInputStream fstream = new FileInputStream(args[0]);
        BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

        VWParser vwparser = new VWParser(18, null, false);

        int numSamples = 0;
        double maxSquaredNorm = 0;

        do {
            Instance sample;
            String strLine = br.readLine();
            if (strLine != null)
                sample = vwparser.parse(strLine);
            else
                break;

            numSamples++;
            double sampleSquaredNorm = sample.getVector().squaredL2Norm();
            if (sampleSquaredNorm > maxSquaredNorm)
                maxSquaredNorm = sampleSquaredNorm;
        }
        while (true);

        br.close();

        System.out.println("Number of samples in VW file = " + numSamples);
        System.out.println("Maximum squared L2 norm of samples = " + maxSquaredNorm);
    }

}