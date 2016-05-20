// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache 2.0 license.
// Please see LICENSE file in the project root for terms.
package com.yahoo.labs.yamall.ml;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

import com.yahoo.labs.yamall.core.SparseVector;
import com.yahoo.labs.yamall.parser.VWParser;

/**
 * I/O functions for the Learner class
 * 
 * @author Francesco Orabona, (francesco@yahoo-inc.com)
 * @version 1.0
 */
public class IOLearner {

    /**
     * Loads a model from disk.
     * <p>
     * The type of model is taken from the file.
     * 
     * @param filename
     *            name of the file to load.
     * @return model.
     */
    public static Learner loadLearner(String filename) {
        Learner obj = null;
        try {
            FileInputStream fileIn = new FileInputStream(filename);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            obj = (Learner) in.readObject();
            in.close();
            fileIn.close();
        }
        catch (IOException i) {
            i.printStackTrace();
        }
        catch (ClassNotFoundException c) {
            System.out.println("Learner class not found");
            c.printStackTrace();
        }
        return obj;
    }

    /**
     * Saves a model to disk.
     * 
     * @param obj
     *            model to save.
     * @param filename
     *            name of the file to save.
     */
    public static void saveLearner(Learner obj, String filename) {
        try {
            FileOutputStream fileOut = new FileOutputStream(filename);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(obj);
            out.close();
            fileOut.close();
        }
        catch (IOException i) {
            i.printStackTrace();
        }
    }

    /**
     * Saves a model to disk in a human readable form.
     * 
     * @param w
     *            weight vector to save.
     * @param map
     *            HashMap from keys to namespaces and features names.
     * @param filename
     *            name of the file to save.
     * @see VWParser#getInvertHashMap
     */
    public static void saveInvertHash(SparseVector w, HashMap<Integer, String> map, String filename) {
        try {
            FileOutputStream fileOut = new FileOutputStream(filename);

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fileOut));

            for (Map.Entry<Integer, Double> entry : w.entrySet()) {
                bw.write(map.get(entry.getKey()) + ":" + entry.getValue().toString());
                bw.newLine();
            }
            bw.close();
            fileOut.close();
        }
        catch (IOException i) {
            i.printStackTrace();
        }
    }
}
