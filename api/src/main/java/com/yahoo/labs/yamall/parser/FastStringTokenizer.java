// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache 2.0 license.
// Please see LICENSE file in the project root for terms.
package com.yahoo.labs.yamall.parser;

/**
 * Fast tokenizer for parsing.
 * 
 * @author Francesco Orabona
 * @version 1.0
 */
public class FastStringTokenizer {

    private final char delim;
    private final String line;
    private final int len;
    private int idx;

    /**
     * FastStringTokenizer constructor.
     * 
     * @param line
     *            string to be tokenized.
     * @param delim
     *            delimeter character.
     */
    public FastStringTokenizer(
            String line, char delim) {
        this.delim = delim;
        this.line = line;
        len = line.length();

        // Skip a single delimiter at the beginning
        idx = 0;
        if (idx < len && line.charAt(idx) == delim)
            idx++;
    }

    /**
     * Returns the next token.
     * <p>
     * Two consecutive delimiters will cause nextToken to return an empty string. If there are not tokens, it returns null.
     * 
     * @return the next token.
     */
    public String nextToken() {
        if (idx < len) {
            int j = idx;
            while (j < len && line.charAt(j) != delim) {
                j++;
            }
            int start = idx;
            idx = j + 1; // skip the delimiter
            return line.substring(start, j);
        }
        else
            return null;
    }
}
