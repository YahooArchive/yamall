// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache 2.0 license.
// Please see LICENSE file in the project root for terms.
package com.yahoo.labs.yamall.parser;

import java.util.HashMap;

import com.yahoo.labs.yamall.core.Instance;

/**
 * Parser of Instances.
 *
 * @author Francesco Orabona, (francesco@yahoo-inc.com)
 * @version 1.0
 */
public interface InstanceParser {
    /**
     * Parse a sample string and returns an instance.
     * 
     * @param line
     *            sample string.
     * @return sample in Instance format.
     */
    public Instance parse(String line);

    /**
     * Returns the map from hashed keys to namespaces and feature names.
     * 
     * @return hash map from hashed keys to strings composed by namespaces and feature names.
     */
    public HashMap<Integer, String> getInvertHashMap();
}
