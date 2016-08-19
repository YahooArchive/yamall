// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache 2.0 license.
// Please see LICENSE file in the project root for terms.
package com.yahoo.labs.yamall.ml;

/**
 * Square loss.
 * 
 * @author Francesco Orabona
 * @version 1.0
 */
@SuppressWarnings("serial")
public class SquareLoss implements Loss {

    public double lossValue(double pred, double label) {
        return (pred - label) * (pred - label);
    }

    public double negativeGradient(double pred, double label, double importance) {
        return 2.0 * (label - pred) * importance;
    }

    public double negativeGradientInvariant(double pred, double label, double eta_importance, double normx) {
        return 2.0 * (label - pred) * (1.0 - Math.exp(-eta_importance * normx)) / normx;
    }

    public String toString() {
        return "Square loss";
    }

    public double lossConstantBinaryLabels(double sPlus, double sMinus) {
        double ratio = sPlus / sMinus;
        double c = (ratio - 1.0) / (ratio + 1.0);
        return (sPlus * (1 - c) * (1 - c) + sMinus * (1 + c) * (1 + c)) / (sPlus + sMinus);
    }

}
