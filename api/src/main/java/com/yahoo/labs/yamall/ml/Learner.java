// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache 2.0 license.
// Please see LICENSE file in the project root for terms.
package com.yahoo.labs.yamall.ml;

import java.io.Serializable;

import com.yahoo.labs.yamall.core.Instance;
import com.yahoo.labs.yamall.core.SparseVector;

/**
 * Learner interface.
 * 
 * @author Francesco Orabona
 * @version 1.0
 */
public interface Learner extends Serializable {
    /**
     * Updates the learner using a single sample.
     * 
     * @param sample
     *            Sample to use in the update.
     * @return Prediction over the sample, before update.
     * @see Learner#predict(Instance)
     */
    public double update(Instance sample);

    /**
     * Predicts on a single sample.
     * 
     * @param sample
     *            Sample to use in the update.
     * @return Prediction over the sample.
     * @see Learner#update(Instance)
     */
    public double predict(Instance sample);

    /**
     * Sets the loss function to be used in the training.
     * 
     * @param lossFnc
     *            loss function to be used.
     */
    public void setLoss(Loss lossFnc);

    /**
     * Gets the loss function used during training.
     * 
     * @return the loss function used by the learning algorithm.
     */
    public Loss getLoss();

    /**
     * Sets the learning rate of the algorithm.
     * <p>
     * The exact meaning depends on the algorithm.
     * 
     * @param eta
     *            the learning rate to be used.
     */
    public void setLearningRate(double eta);

    /**
     * Gets the weights of the algorithm.
     * <p>
     * The exact meaning depends on the algorithm.
     * 
     * @return the weight vector of the algorithm.
     */
    public SparseVector getWeights();

    /**
     * Returns the textual description of the algorithm.
     * 
     * @return a string describing the algorithm.
     */
    public String toString();
}
