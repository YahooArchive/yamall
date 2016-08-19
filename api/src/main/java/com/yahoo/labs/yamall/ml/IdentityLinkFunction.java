// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache 2.0 license.
// Please see LICENSE file in the project root for terms.
package com.yahoo.labs.yamall.ml;

/**
 * Identity link function.
 * 
 * @author Francesco Orabona
 * @version 1.0
 */
@SuppressWarnings("serial")
public class IdentityLinkFunction implements LinkFunction {

    public double apply(double input) {
        return input;
    }

    public String toString() {
        return "Identity link function";
    }

}
