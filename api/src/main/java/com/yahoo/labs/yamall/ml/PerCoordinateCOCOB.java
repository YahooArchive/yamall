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

@SuppressWarnings("serial")
public class PerCoordinateCOCOB implements Learner {
    private double initialWealth = 1;
    private transient double[] w = null;
    private transient double[] reward;
    private transient double[] theta;
    private transient double[] sumAbsGradient;
    private transient double[] scale;
    private Loss lossFnc;
    private int size_hash = 0;
    private int wCreationStamp = -1;
    private int iter = 0;

    public PerCoordinateCOCOB(
            int bits) {
        size_hash = 1 << bits;
        theta = new double[size_hash];
        reward = new double[size_hash];
        scale = new double[size_hash];
        sumAbsGradient = new double[size_hash];
    }

    public double update(Instance sample) {
        iter++;

        double pred = 0;
        for (Int2DoubleMap.Entry entry : sample.getVector().int2DoubleEntrySet()) {
            int key = entry.getIntKey();
            double reward_i = reward[key];
            double scale_i = scale[key];
            double x_i = entry.getDoubleValue();
            double sumAbsGradient_i = sumAbsGradient[key];
            double theta_i = theta[key];
            if (Math.abs(x_i) > scale_i) {
                scale_i = Math.abs(x_i);
                scale[key] = scale_i;
            }

            double beta_i = shrink(2 * theta_i * scale_i / (sumAbsGradient_i + 1.0 + scale_i));
            double w_i = beta_i * (reward_i + initialWealth) / scale_i;
            // w[key] = w_i;

            pred += w_i * x_i;
        }

        final double negativeGrad = lossFnc.negativeGradient(pred, sample.getLabel(), sample.getWeight());

        for (Int2DoubleMap.Entry entry : sample.getVector().int2DoubleEntrySet()) {
            int key = entry.getIntKey();
            double x_i = entry.getDoubleValue();
            double reward_i = reward[key];
            double sumAbsGradient_i = sumAbsGradient[key];
            double scale_i = scale[key];
            double theta_i = theta[key];

            double beta_i = shrink(2 * theta_i * scale_i / (sumAbsGradient_i + 1.0 + scale_i));
            double w_i = beta_i * (reward_i + initialWealth) / scale_i;

            reward_i = reward_i + w_i * x_i * negativeGrad;
            reward[key] = reward_i;
            theta_i = theta_i + x_i * negativeGrad;
            theta[key] = theta_i;
            sumAbsGradient_i = sumAbsGradient_i + Math.abs(x_i * negativeGrad) * scale_i;
            sumAbsGradient[key] = sumAbsGradient_i;
        }

        return pred;
    }

    private double shrink(double x) {
        if (x > 20)
            return 1;
        return (Math.exp(x) - 1.0) / (Math.exp(x) + 1.0);
    }

    public double predict(Instance sample) {
        return sample.getVector().dot(w);
    }

    public void setLoss(Loss lossFnc) {
        this.lossFnc = lossFnc;
    }

    public void setLearningRate(double eta) {
        this.initialWealth = eta;
    }

    public Loss getLoss() {
        return lossFnc;
    }

    private void createW() {
        if (wCreationStamp != iter) {
            if (w == null)
                w = new double[size_hash];
            for (int i = 0; i < theta.length; i++) {
                final double theta_i = theta[i];
                if (theta_i != 0) {
                    double reward_i = reward[i];
                    final double scale_i = scale[i];
                    final double sumAbsGradient_i = sumAbsGradient[i];

                    double beta_i = shrink(2 * theta_i * scale_i / (sumAbsGradient_i + 1.0 + scale_i));
                    w[i] = beta_i * (reward_i + initialWealth) / scale_i;
                }
            }
            wCreationStamp = iter;
        }
    }

    public SparseVector getWeights() {
        createW();
        return SparseVector.dense2Sparse(w);
    }

    public String toString() {
        String tmp = "Using Continuos Coin Betting optimizer (Adaptive)\n";
        tmp = tmp + "Initial learning rate = " + initialWealth + "\n";
        tmp = tmp + "Loss function = " + getLoss().toString();
        return tmp;
    }

    private void writeObject(ObjectOutputStream o) throws IOException {
        o.defaultWriteObject();
        o.writeObject(SparseVector.dense2Sparse(w));
        o.writeObject(SparseVector.dense2Sparse(reward));
        o.writeObject(SparseVector.dense2Sparse(theta));
        o.writeObject(SparseVector.dense2Sparse(sumAbsGradient));
        o.writeObject(SparseVector.dense2Sparse(scale));
    }

    private void readObject(ObjectInputStream o) throws IOException, ClassNotFoundException {
        o.defaultReadObject();
        w = ((SparseVector) o.readObject()).toDenseVector(size_hash);
        reward = ((SparseVector) o.readObject()).toDenseVector(size_hash);
        theta = ((SparseVector) o.readObject()).toDenseVector(size_hash);
        sumAbsGradient = ((SparseVector) o.readObject()).toDenseVector(size_hash);
        scale = ((SparseVector) o.readObject()).toDenseVector(size_hash);
    }

}
