// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache 2.0 license.
// Please see LICENSE file in the project root for terms.
package com.yahoo.labs.yamall.ml;

/**
 * Absolute loss.
 * 
 * @author Francesco Orabona, (francesco@yahoo-inc.com)
 * @version 1.0
 */
@SuppressWarnings("serial")
public class AbsLoss implements Loss {
    public double lossValue(double pred, double label) {
        return Math.abs(pred - label);
    }

    public double negativeGradient(double pred, double label, double importance) {
        return Math.signum(label - pred);
    }

    public double negativeGradientInvariant(double pred, double label, double eta_importance, double normx) {
        return Math.signum(label - pred);
    }

    public String toString() {
        return "Absolute loss";
    }

    public double lossConstantBinaryLabels(double sPlus, double sMinus) {
        // TODO Auto-generated method stub
        return 0;
    }
}
