// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache 2.0 license.
// Please see LICENSE file in the project root for terms.
package com.yahoo.labs.yamall.ml;

import java.io.Serializable;

/**
 * Loss function interface.
 * 
 * @author Francesco Orabona
 * @version 1.0
 */
public interface Loss extends Serializable {
    /**
     * Returns the value of the loss function.
     * 
     * @param pred
     *            prediction of the algorithm.
     * @param label
     *            true label.
     * @return the value of the loss function.
     */
    public double lossValue(double pred, double label);

    /**
     * Returns the negative value of the gradient.
     * 
     * @param pred
     *            prediction of the algorithm.
     * @param label
     *            true label.
     * @param importance
     *            importance weight of the sample.
     * @return the negative value of the gradient of the loss function.
     */
    public double negativeGradient(double pred, double label, double importance);

    /**
     * Returns the negative value of the gradient, using the invariant rule.
     * <p>
     * Details in N. Karampatziakis, J. Langford, "Online Importance Weight Aware Updates", UAI 2011
     * 
     * @param pred
     *            prediction of the algorithm.
     * @param label
     *            true label.
     * @param importance
     *            importance weight of the sample.
     * @param h_normx
     *            h_normx.
     * @return the negative value of the gradient of the loss function.
     */
    public double negativeGradientInvariant(double pred, double label, double importance, double h_normx);

    /**
     * Returns the value of the loss for a constant predictor in the case of binary labels.
     * 
     * @param sPlus
     *            sum of weights of the positive labels.
     * @param sMinus
     *            sum of weights of the negative labels.
     * @return the value of the loss for a constant predictor.
     */
    public double lossConstantBinaryLabels(double sPlus, double sMinus);

    /**
     * Returns the textual description of the loss function.
     * 
     * @return a string describing the loss function.
     */
    public String toString();
}
