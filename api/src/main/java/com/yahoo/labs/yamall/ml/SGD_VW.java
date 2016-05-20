// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache 2.0 license.
// Please see LICENSE file in the project root for terms.
package com.yahoo.labs.yamall.ml;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.yahoo.labs.yamall.core.Instance;
import com.yahoo.labs.yamall.core.SparseVector;

import it.unimi.dsi.fastutil.ints.Int2DoubleMap;

/**
 * Stochastic Gradient Descent algorithm from VW.
 * <p>
 * The details of the algorithm are from S. Ross, P. Mineiro, J. Langford, "Normalized online learning", UAI 2013.
 * <p>
 * The memory required is 2^bits*3*8 bytes.
 * 
 * @author Francesco Orabona <francesco@yahoo-inc.com>
 * @version 1.1
 */
@SuppressWarnings("serial")
public class SGD_VW implements Learner {
    private double eta = .5;
    private transient double[] w;
    private transient double[] s;
    private transient double[] G;
    private double N = 0;
    private Loss lossFnc;
    private double iter = 0;
    private int size_hash = 0;

    public SGD_VW(
            int bits) {
        size_hash = 1 << bits;
        w = new double[size_hash];
        s = new double[size_hash];
        G = new double[size_hash];
    }

    public void setLoss(Loss lossFnc) {
        this.lossFnc = lossFnc;
    }

    public void setLearningRate(double eta) {
        this.eta = eta;
    }

    /*
     * Algorithm from S. Ross,P. Mineiro, J. Langford. "Normalized online learning", UAI, 2013
     */
    public double update(Instance sample) {
        iter++;

        double pred = 0;
        for (Int2DoubleMap.Entry entry : sample.getVector().int2DoubleEntrySet()) {
            double x_i;
            if ((x_i = entry.getDoubleValue()) != 0.0) {
                int key = entry.getIntKey();
                double s_i = s[key];
                double w_i = w[key];
                if (Math.abs(x_i) > s_i) {
                    w_i = w_i * s_i / Math.abs(x_i);
                    w[key] = w_i;
                    s_i = Math.abs(x_i);
                    s[key] = s_i;
                    N += 1.0;
                }
                else
                    N += x_i * x_i / (s_i * s_i);
                pred += w_i * x_i;
            }
        }

        final double negativeGrad = lossFnc.negativeGradient(pred, sample.getLabel(), sample.getWeight());

        if (Math.abs(negativeGrad) > 1e-8) {
            final double negativeGradSquared = negativeGrad * negativeGrad;

            final double a = eta * Math.sqrt(iter / N) * negativeGrad;

            for (Int2DoubleMap.Entry entry : sample.getVector().int2DoubleEntrySet()) {
                double x_i;
                if ((x_i = entry.getDoubleValue()) != 0.0) {
                    int key = entry.getIntKey();
                    double G_i = G[key];
                    double s_i = s[key];

                    G_i += negativeGradSquared * x_i * x_i;
                    G[key] = G_i;

                    w[key] += a * Math.sqrt(1 / G_i) / s_i * x_i;
                }
            }
        }
        return pred;
    }

    public double predict(Instance sample) {
        return sample.getVector().dot(w);
    }

    public Loss getLoss() {
        return lossFnc;
    }

    public SparseVector getWeights() {
        return SparseVector.dense2Sparse(w);
    }

    public String toString() {
        String tmp = "Using VW optimizer (adaptive and normalized)\n";
        tmp = tmp + "Initial learning rate = " + eta + "\n";
        tmp = tmp + "Loss function = " + getLoss().toString();
        return tmp;
    }

    private void writeObject(ObjectOutputStream o) throws IOException {
        o.defaultWriteObject();
        o.writeObject(SparseVector.dense2Sparse(w));
        o.writeObject(SparseVector.dense2Sparse(s));
        o.writeObject(SparseVector.dense2Sparse(G));
    }

    private void readObject(ObjectInputStream o) throws IOException, ClassNotFoundException {
        o.defaultReadObject();
        w = ((SparseVector) o.readObject()).toDenseVector(size_hash);
        s = ((SparseVector) o.readObject()).toDenseVector(size_hash);
        G = ((SparseVector) o.readObject()).toDenseVector(size_hash);
    }

}
