// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache 2.0 license.
// Please see LICENSE file in the project root for terms.
package com.yahoo.labs.yamall.ml;

/**
 * Hinge loss.
 * 
 * @author Francesco Orabona
 * @version 1.0
 */
@SuppressWarnings("serial")
public class HingeLoss implements Loss {

    public double lossValue(double pred, double label) {
        return Math.max(1 - pred * label, 0);
    }

    public double negativeGradient(double pred, double label, double importance) {
        if (1 - pred * label > 0)
            return label;
        else
            return 0;
    }

    public double negativeGradientInvariant(double pred, double label, double importance, double h_normx) {
        if (1 - pred * label > 0)
            return label;
        else
            return 0;
    }

    public String toString() {
        return "Hinge loss";
    }

    public double lossConstantBinaryLabels(double sPlus, double sMinus) {
        double c = Math.signum(sPlus - sMinus);
        return (sPlus * (1 - c) + sMinus * (1 + c)) / (sPlus + sMinus);
    }
}
