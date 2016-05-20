// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache 2.0 license.
// Please see LICENSE file in the project root for terms.
package com.yahoo.labs.yamall.ml;

/**
 * Logistic link function.
 * 
 * @author Francesco Orabona, (francesco@yahoo-inc.com)
 * @version 1.0
 */
@SuppressWarnings("serial")
public class LogisticLinkFunction implements LinkFunction {

    public double apply(double input) {
        return 1 / (Math.exp(-input) + 1);
    }

    public String toString() {
        return "Logistic link function";
    }

}
