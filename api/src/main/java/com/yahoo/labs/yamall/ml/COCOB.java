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
public class COCOB implements Learner {
    private double reward = 0.0;
    private transient double[] theta;
    private double sumAbsGradientScale = 0.0;
    private Loss lossFnc;
    private double initialWealth = 1.0;
    private double maxNormGrad = 0.0;
    private int size_hash = 0;
    double squaredNormTheta = 0;

    public COCOB(
            int bits) {
        size_hash = 1 << bits;
        theta = new double[size_hash];
    }

    public double update(Instance sample) {
        double squaredNormSample = sample.getVector().squaredL2Norm();
        double normSample = Math.sqrt(squaredNormSample);
        double normTheta = Math.sqrt(squaredNormTheta);

        if (maxNormGrad < normSample)
            maxNormGrad = normSample;

        double dotproduct = sample.getVector().dot(theta);
        double beta = shrink(2 * maxNormGrad * normTheta / (sumAbsGradientScale + maxNormGrad * maxNormGrad + 1.0));
        double pred = dotproduct * beta * (reward + initialWealth) / (maxNormGrad * (normTheta + 1e-10));

        final double negativeGrad = lossFnc.negativeGradient(pred, sample.getLabel(), sample.getWeight());

        reward = reward + pred * negativeGrad;

        sumAbsGradientScale = sumAbsGradientScale + Math.abs(negativeGrad) * normSample * maxNormGrad;

        sample.getVector().addScaledSparseVectorToDenseVector(theta, negativeGrad);
        squaredNormTheta += 2 * negativeGrad * dotproduct + negativeGrad * negativeGrad * squaredNormSample;

        return pred;
    }

    private double shrink(double x) {
        if (x > 20)
            return 1;
        return (Math.exp(x) - 1.0) / (Math.exp(x) + 1.0);
    }

    public double predict(Instance sample) {
        double dotproduct = sample.getVector().dot(theta);
        double normTheta = Math.sqrt(squaredNormTheta);
        double beta = shrink(2 * maxNormGrad * normTheta / (sumAbsGradientScale + maxNormGrad * maxNormGrad + 1.0));
        return dotproduct * beta * (reward + initialWealth) / (maxNormGrad * (normTheta + 1e-10));
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
        double normTheta = Math.sqrt(squaredNormTheta);
        double beta = shrink(2 * maxNormGrad * normTheta / (sumAbsGradientScale + maxNormGrad * maxNormGrad + 1.0));
        return SparseVector.rescaledDense2Sparse(theta,
                beta * (reward + initialWealth) / (maxNormGrad * (normTheta + 1e-10)));
    }

    public String toString() {
        String tmp = "Using Continuos Coin Betting optimizer\n";
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
