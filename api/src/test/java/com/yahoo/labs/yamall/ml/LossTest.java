// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache 2.0 license.
// Please see LICENSE file in the project root for terms.
package com.yahoo.labs.yamall.ml;

import org.testng.Assert;
import org.testng.annotations.Test;

public class LossTest {

    class NumericalDerivative {

        Loss LossFunction;
        double epsilon;

        NumericalDerivative(
                Loss LossFunction, double epsilon) {
            this.LossFunction = LossFunction;
            this.epsilon = epsilon;
        }

        private double derivative(double prediction, double label) {
            return (LossFunction.lossValue(prediction + epsilon, label)
                    - LossFunction.lossValue(prediction - epsilon, label)) / (2.0 * epsilon);
        }

        double absDifferenceDerivatives(double prediction, double label) {
            return Math.abs(derivative(prediction, label) + LossFunction.negativeGradient(prediction, label, 1.0));
        }
    }

    @Test
    public void squareLossTest() {
        Loss fnc = new SquareLoss();
        NumericalDerivative ndSquareLoss = new NumericalDerivative(fnc, 1e-5);

        // value of the loss
        Assert.assertTrue(fnc.lossValue(0.0, 0.0) == 0.0);
        Assert.assertTrue(fnc.lossValue(0.0, 1.0) == 1.0);
        Assert.assertTrue(fnc.lossValue(0.0, 2.0) == 4.0);
        Assert.assertTrue(fnc.lossValue(1.0, 2.0) == 1.0);
        Assert.assertTrue(fnc.lossValue(-1.0, 2.0) == 9.0);

        // derivatives
        Assert.assertTrue(ndSquareLoss.absDifferenceDerivatives(0.0, 0.0) < 1e-8);
        Assert.assertTrue(ndSquareLoss.absDifferenceDerivatives(0.0, 1.0) < 1e-8);
        Assert.assertTrue(ndSquareLoss.absDifferenceDerivatives(0.0, 2.0) < 1e-8);
        Assert.assertTrue(ndSquareLoss.absDifferenceDerivatives(1.0, 2.0) < 1e-8);
        Assert.assertTrue(ndSquareLoss.absDifferenceDerivatives(-1.0, 2.0) < 1e-8);
    }

    @Test
    public void logisticLossTest() {
        Loss fnc = new LogisticLoss();
        NumericalDerivative ndSquareLoss = new NumericalDerivative(fnc, 1e-5);

        // value of the loss
        Assert.assertTrue(fnc.lossValue(0.0, 0.0) == Math.log(2));
        Assert.assertTrue(fnc.lossValue(0.0, 1.0) == Math.log(2));
        Assert.assertTrue(fnc.lossValue(0.0, -1.0) == Math.log(2));
        Assert.assertTrue(fnc.lossValue(Double.MAX_VALUE, 1.0) == 0.0);
        Assert.assertTrue(fnc.lossValue(Double.POSITIVE_INFINITY, 1.0) == 0.0);
        Assert.assertTrue(fnc.lossValue(-Double.MAX_VALUE, 1.0) == Double.POSITIVE_INFINITY);
        Assert.assertTrue(fnc.lossValue(Double.NEGATIVE_INFINITY, 1.0) == Double.POSITIVE_INFINITY);
        Assert.assertTrue(fnc.lossValue(Double.MAX_VALUE, -1.0) == Double.POSITIVE_INFINITY);
        Assert.assertTrue(fnc.lossValue(Double.POSITIVE_INFINITY, -1.0) == Double.POSITIVE_INFINITY);
        Assert.assertTrue(fnc.lossValue(-Double.MAX_VALUE, -1.0) == 0.0);
        Assert.assertTrue(fnc.lossValue(Double.NEGATIVE_INFINITY, -1.0) == 0.0);

        // derivatives
        Assert.assertTrue(ndSquareLoss.absDifferenceDerivatives(0.0, 0.0) < 1e-8);
        Assert.assertTrue(ndSquareLoss.absDifferenceDerivatives(0.0, 1.0) < 1e-8);
        Assert.assertTrue(ndSquareLoss.absDifferenceDerivatives(0.0, -1.0) < 1e-8);
        Assert.assertTrue(ndSquareLoss.absDifferenceDerivatives(1.0, 1.0) < 1e-8);
        Assert.assertTrue(ndSquareLoss.absDifferenceDerivatives(-1.0, 1.0) < 1e-8);
    }

    @Test
    public void hingeLossTest() {
        Loss fnc = new HingeLoss();
        NumericalDerivative ndSquareLoss = new NumericalDerivative(fnc, 1e-5);

        // value of the loss
        Assert.assertTrue(fnc.lossValue(0.0, 0.0) == 1.0);
        Assert.assertTrue(fnc.lossValue(0.0, 1.0) == 1.0);
        Assert.assertTrue(fnc.lossValue(0.0, -1.0) == 1.0);
        Assert.assertTrue(fnc.lossValue(2.0, 1.0) == 0.0);
        Assert.assertTrue(fnc.lossValue(-2.0, 1.0) == 3.0);
        Assert.assertTrue(fnc.lossValue(2.0, -1.0) == 3.0);
        Assert.assertTrue(fnc.lossValue(-2.0, -1.0) == 0.0);

        // derivatives
        Assert.assertTrue(ndSquareLoss.absDifferenceDerivatives(0.0, 0.0) < 1e-8);
        Assert.assertTrue(ndSquareLoss.absDifferenceDerivatives(0.0, 1.0) < 1e-8);
        Assert.assertTrue(ndSquareLoss.absDifferenceDerivatives(0.0, -1.0) < 1e-8);
        Assert.assertTrue(ndSquareLoss.absDifferenceDerivatives(0.99, 1.0) < 1e-8);
        Assert.assertTrue(ndSquareLoss.absDifferenceDerivatives(-1.0, 1.0) < 1e-8);
    }

    @Test
    public void absLossTest() {
        Loss fnc = new AbsLoss();
        NumericalDerivative ndSquareLoss = new NumericalDerivative(fnc, 1e-5);

        // value of the loss
        Assert.assertTrue(fnc.lossValue(0.0, 0.0) == 0.0);
        Assert.assertTrue(fnc.lossValue(0.0, 1.0) == 1.0);
        Assert.assertTrue(fnc.lossValue(0.0, -1.0) == 1.0);
        Assert.assertTrue(fnc.lossValue(2.0, 1.0) == 1.0);
        Assert.assertTrue(fnc.lossValue(-2.0, 1.0) == 3.0);
        Assert.assertTrue(fnc.lossValue(2.0, -1.0) == 3.0);
        Assert.assertTrue(fnc.lossValue(-2.0, -1.0) == 1.0);

        // derivatives
        Assert.assertTrue(ndSquareLoss.absDifferenceDerivatives(0.0, 0.0) < 1e-8);
        Assert.assertTrue(ndSquareLoss.absDifferenceDerivatives(0.0, 1.0) < 1e-8);
        Assert.assertTrue(ndSquareLoss.absDifferenceDerivatives(0.0, -1.0) < 1e-8);
        Assert.assertTrue(ndSquareLoss.absDifferenceDerivatives(1.0, 1.0) < 1e-8);
        Assert.assertTrue(ndSquareLoss.absDifferenceDerivatives(-1.0, 1.0) < 1e-8);
    }
}
