// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache 2.0 license.
// Please see LICENSE file in the project root for terms.
package com.yahoo.labs.yamall.ml;

import java.io.Serializable;

/**
 * Link function interface.
 * 
 * @author Francesco Orabona
 * @version 1.0
 */
public interface LinkFunction extends Serializable {
    /**
     * Applies the link function to the input.
     * 
     * @param input
     *            input to the link function.
     * @return output of the link function.
     */
    public double apply(double input);

    /**
     * Returns the textual description of the link function.
     * 
     * @return a string describing the link function.
     */
    public String toString();
}
