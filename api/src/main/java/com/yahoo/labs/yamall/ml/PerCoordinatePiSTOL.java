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
 * Parameter-free STOchastic Learning algorithm.
 * <p>
 * The details of the algorithm are from
 * <p>
 * F. Orabona, "Simultaneous Model Selection and Optimization through Parameter-free Stochastic Learning", NIPS 2014
 * <p>
 * and
 * <p>
 * F. Orabona, "Are You Still Tuning Hyperparameters? Parameter-free Model Selection and Learning", NIPS Workshop 2014
 * <p>
 * The memory required is 2^bits*4*8 bytes.
 * 
 * @author Francesco Orabona
 * @version 1.0
 */
@SuppressWarnings("serial")
public class PerCoordinatePiSTOL implements Learner {
    private transient double[] w = null;
    private transient double[] theta;
    private transient double[] sumAbsGradient;
    private transient double[] scale;
    private Loss lossFnc;
    private double L = 1;
    private double eta = 1;
    private int size_hash = 0;
    private int iter = 0;
    private int wCreationStamp = -1;

    public PerCoordinatePiSTOL(
            int bits) {
        size_hash = 1 << bits;
        theta = new double[size_hash];
        scale = new double[size_hash];
        sumAbsGradient = new double[size_hash];
    }

    public double update(Instance sample) {
        iter++;

        double pred = 0;
        for (Int2DoubleMap.Entry entry : sample.getVector().int2DoubleEntrySet()) {
            final int key = entry.getIntKey();
            double scale_i = scale[key];
            final double x_i = entry.getDoubleValue();
            if (Math.abs(x_i) > scale_i) {
                scale_i = Math.abs(x_i);
                scale[key] = scale_i;
            }
            double theta_i = theta[key];
            if (theta_i != 0) {
                final double sumAbsGradient_i = sumAbsGradient[key];
                // double w_i = (reward_i+initialWealth)*theta_i/(sumGradientScale_i+scale_i*scale_i);
                final double q_i = 0.5 / (scale_i * L * (sumAbsGradient_i + scale_i * L));
                final double w_i = eta * theta_i * q_i * Math.sqrt(sumAbsGradient_i)
                        * Math.exp(0.5 * q_i * theta_i * theta_i);

                pred += w_i * x_i;
            }
        }

        final double negativeGrad = lossFnc.negativeGradient(pred, sample.getLabel(), sample.getWeight());

        for (Int2DoubleMap.Entry entry : sample.getVector().int2DoubleEntrySet()) {
            final int key = entry.getIntKey();
            final double x_i = entry.getDoubleValue();

            theta[key] += x_i * negativeGrad;
            sumAbsGradient[key] += Math.abs(x_i * negativeGrad);
        }

        return pred;
    }

    public double predict(Instance sample) {
        createW();
        return sample.getVector().dot(w);
    }

    public void setLoss(Loss lossFnc) {
        this.lossFnc = lossFnc;
    }

    public void setLearningRate(double eta) {
        this.eta = eta;
    }

    public Loss getLoss() {
        return lossFnc;
    }

    public SparseVector getWeights() {
        createW();
        return SparseVector.dense2Sparse(w);
    }

    private void createW() {
        if (wCreationStamp != iter) {
            if (w == null)
                w = new double[size_hash];
            for (int i = 0; i < theta.length; i++) {
                final double theta_i = theta[i];
                if (theta_i != 0) {
                    final double scale_i = scale[i];
                    final double sumAbsGradient_i = sumAbsGradient[i];

                    final double q_i = 0.5 / (scale_i * L * (sumAbsGradient_i + scale_i * L));
                    w[i] = eta * theta_i * q_i * Math.sqrt(sumAbsGradient_i) * Math.exp(0.5 * q_i * theta_i * theta_i);
                }
            }
            wCreationStamp = iter;
        }
    }

    public String toString() {
        String tmp = "Using PiSTOL optimizer (Adaptive)\n";
        tmp = tmp + "Initial learning rate = " + eta + "\n";
        tmp = tmp + "Loss function = " + getLoss().toString();
        return tmp;
    }

    private void writeObject(ObjectOutputStream o) throws IOException {
        o.defaultWriteObject();
        o.writeObject(SparseVector.dense2Sparse(theta));
        o.writeObject(SparseVector.dense2Sparse(scale));
        o.writeObject(SparseVector.dense2Sparse(sumAbsGradient));
    }

    private void readObject(ObjectInputStream o) throws IOException, ClassNotFoundException {
        o.defaultReadObject();
        theta = ((SparseVector) o.readObject()).toDenseVector(size_hash);
        scale = ((SparseVector) o.readObject()).toDenseVector(size_hash);
        sumAbsGradient = ((SparseVector) o.readObject()).toDenseVector(size_hash);
    }
}
