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
public class PerCoordinateSOLO implements Learner {
    private transient double[] theta;
    private transient double[] sumSqGrads;
    private transient double[] w;
    private double eta = 1.0;
    private Loss lossFnc;
    public double iter = 0;
    private int size_hash = 0;

    public PerCoordinateSOLO(
            int bits) {
        size_hash = 1 << bits;
        theta = new double[size_hash];
        sumSqGrads = new double[size_hash];
        w = new double[size_hash];
    }

    public void setLoss(Loss lossFnc) {
        this.lossFnc = lossFnc;
    }

    public void setLearningRate(double eta) {
        this.eta = eta;
    }

    public double update(Instance sample) {
        iter++;

        double pred = predict(sample);

        final double negativeGrad = lossFnc.negativeGradient(pred, sample.getLabel(), sample.getWeight());
        final double negativeGradSquared = negativeGrad * negativeGrad;

        for (Int2DoubleMap.Entry entry : sample.getVector().int2DoubleEntrySet()) {
            int key = entry.getIntKey();
            double theta_i = theta[key];
            double sumSqGrads_i = sumSqGrads[key];
            double x_i = entry.getDoubleValue();

            theta_i = theta_i + x_i * negativeGrad;
            theta[key] = theta_i;

            sumSqGrads_i = sumSqGrads_i + negativeGradSquared * x_i * x_i;
            sumSqGrads[key] = sumSqGrads_i;

            w[key] = eta * theta_i / Math.sqrt(sumSqGrads_i);
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
        String tmp = "Using SOLO optimizer (adaptive)\n";
        tmp = tmp + "Initial learning rate = " + eta + "\n";
        tmp = tmp + "Loss function = " + getLoss().toString();
        return tmp;
    }

    private void writeObject(ObjectOutputStream o) throws IOException {
        o.defaultWriteObject();
        o.writeObject(SparseVector.dense2Sparse(theta));
        o.writeObject(SparseVector.dense2Sparse(sumSqGrads));
        o.writeObject(SparseVector.dense2Sparse(w));
    }

    private void readObject(ObjectInputStream o) throws IOException, ClassNotFoundException {
        o.defaultReadObject();
        theta = ((SparseVector) o.readObject()).toDenseVector(size_hash);
        sumSqGrads = ((SparseVector) o.readObject()).toDenseVector(size_hash);
        w = ((SparseVector) o.readObject()).toDenseVector(size_hash);
    }

}
