// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache 2.0 license.
// Please see LICENSE file in the project root for terms.
package com.yahoo.labs.yamall.ml;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.yahoo.labs.yamall.core.Instance;
import com.yahoo.labs.yamall.core.SparseVector;

/**
 * Stochastic gradient descent algorithm based on Scale Invariant Online Linear Optimization.
 * <p>
 * The details of the algorithm are from F. Orabona, D. Pal, "Scale-Free Algorithms for Online Linear Optimization", ALT 2015
 * 
 * @author Francesco Orabona <francesco@yahoo-inc.com>
 * @version 1.0
 */
@SuppressWarnings("serial")
public class SOLO implements Learner {
    private transient double[] theta;
    private double eta = 1.0;
    private double scaling = 1;
    private double sumSqGrads = 1;
    private Loss lossFnc;
    private int size_hash = 0;

    public SOLO(
            int bits) {
        size_hash = 1 << bits;
        theta = new double[size_hash];
    }

    public void setLoss(Loss lossFnc) {
        this.lossFnc = lossFnc;
    }

    public void setLearningRate(double eta) {
        this.eta = eta;
    }

    public double update(Instance sample) {
        double pred = predict(sample);

        double negativeGrad = lossFnc.negativeGradient(pred, sample.getLabel(), sample.getWeight());
        sample.getVector().addScaledSparseVectorToDenseVector(theta, negativeGrad);

        sumSqGrads += sample.getVector().squaredL2Norm() * negativeGrad * negativeGrad;
        scaling = eta / Math.sqrt(sumSqGrads);

        return pred;
    }

    public double predict(Instance sample) {
        return sample.getVector().dot(theta) * scaling;
    }

    public Loss getLoss() {
        return lossFnc;
    }

    public SparseVector getWeights() {
        return SparseVector.rescaledDense2Sparse(theta, scaling);
    }

    public String toString() {
        String tmp = "Using SOLO optimizer\n";
        tmp = tmp + "Initial learning rate = " + eta + "\n";
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
