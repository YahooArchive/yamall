// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache 2.0 license.
// Please see LICENSE file in the project root for terms.
package com.yahoo.labs.yamall.parser;

import org.testng.Assert;
import org.testng.annotations.Test;

public class FastStringTokenizerTest {

    @Test
    public void tokenizerTest() {
        FastStringTokenizer fst = new FastStringTokenizer(" this is a test ", ' ');
        Assert.assertTrue(fst.nextToken().equals("this"));
        Assert.assertTrue(fst.nextToken().equals("is"));
        Assert.assertTrue(fst.nextToken().equals("a"));
        Assert.assertTrue(fst.nextToken().equals("test"));
        Assert.assertTrue(fst.nextToken() == null);

        fst = new FastStringTokenizer("  this   ", ' ');
        Assert.assertTrue(fst.nextToken().equals(""));
        Assert.assertTrue(fst.nextToken().equals("this"));
        Assert.assertTrue(fst.nextToken().equals(""));
        Assert.assertTrue(fst.nextToken().equals(""));
        Assert.assertTrue(fst.nextToken() == null);

    }

}
