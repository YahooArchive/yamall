// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache 2.0 license.
// Please see LICENSE file in the project root for terms.
package com.yahoo.labs.yamall.ml;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.yahoo.labs.yamall.core.Instance;
import com.yahoo.labs.yamall.core.SparseVector;

@SuppressWarnings("serial")
public class KT implements Learner {
    private double reward = 0.0;
    private transient double[] theta;
    private double s = 0.0;
    private Loss lossFnc;
    private double initialWealth = 1.0;
    private double maxNormGrad = 0.0;
    private int size_hash = 0;

    public KT(
            int bits) {
        size_hash = 1 << bits;
        theta = new double[size_hash];
    }

    public double update(Instance sample) {
        if (maxNormGrad < Math.sqrt(sample.getVector().squaredL2Norm()))
            maxNormGrad = Math.sqrt(sample.getVector().squaredL2Norm());

        double pred = (reward + initialWealth) * sample.getVector().dot(theta) / (s + 1.0)
                / (maxNormGrad * maxNormGrad);

        final double negativeGrad = lossFnc.negativeGradient(pred, sample.getLabel(), sample.getWeight());

        reward = reward + pred * negativeGrad;

        s = s + 1;

        sample.getVector().addScaledSparseVectorToDenseVector(theta, negativeGrad);

        return pred;
    }

    public double predict(Instance sample) {
        return (reward + initialWealth) * sample.getVector().dot(theta) / (s + 1.0);
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

    public SparseVector getWeights() {
        return SparseVector.rescaledDense2Sparse(theta,
                (reward + initialWealth) / (s + 1.0) / (maxNormGrad * maxNormGrad));
    }

    public String toString() {
        String tmp = "Using KT-based optimizer\n";
        tmp = tmp + "Initial learning rate = " + initialWealth + "\n";
        tmp = tmp + "Loss function = " + getLoss().toString();
        return tmp;
    }

    private void writeObject(ObjectOutputStream o) throws IOException {
        o.defaultWriteObject();
        o.writeObject(SparseVector.dense2Sparse(theta));
    }

    private void readObject(ObjectInputStream o) throws IOException, ClassNotFoundException {
        o.defaultReadObject();
        theta = ((SparseVector) o.readObject()).toDenseVector(size_hash);
    }
}
