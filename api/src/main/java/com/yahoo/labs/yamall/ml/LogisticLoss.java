// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache 2.0 license.
// Please see LICENSE file in the project root for terms.
package com.yahoo.labs.yamall.ml;

/**
 * Logistic loss.
 * 
 * @author Francesco Orabona
 * @version 1.0
 */
@SuppressWarnings("serial")
public class LogisticLoss implements Loss {

    public double lossValue(double pred, double label) {
        return Math.log(Math.exp(-pred * label) + 1.0);
    }

    public double negativeGradient(double pred, double label, double importance) {
        return label / (1.0 + Math.exp(label * pred)) * importance;
    }

    public double negativeGradientInvariant(double pred, double label, double eta_importance, double normx) {
        double d = Math.exp(label * pred);
        double x = eta_importance * normx + label * pred + d;
        double w = wexpmx(x);
        return -(label * w + pred) / normx;
    }

    /*
     * This piece of code is approximating W(exp(x))-x. W is the Lambert W function: W(z)*exp(W(z))=z. The absolute error of this approximation is less than
     * 9e-5. Faster/better approximations can be substituted here.
     */
    private double wexpmx(double x) {
        double w = x >= 1. ? 0.86 * x + 0.01 : Math.exp(0.8 * x - 0.65); // initial guess
        double r = x >= 1. ? x - Math.log(w) - w : 0.2 * x + 0.65 - w; // residual
        double t = 1. + w;
        double u = 2. * t * (t + 2. * r / 3.); // magic
        return w * (1. + r / t * (u - r) / (u - 2. * r)) - x; // more magic
    }

    public double lossConstantBinaryLabels(double sPlus, double sMinus) {
        double c = Math.log(sPlus / sMinus);
        return (sPlus * Math.log(1 + Math.exp(-c)) + sMinus * Math.log(1 + Math.exp(c))) / (sPlus + sMinus);
    }

    public String toString() {
        return "Logistic loss";
    }
}
