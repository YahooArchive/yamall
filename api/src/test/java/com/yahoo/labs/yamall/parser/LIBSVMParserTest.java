// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache 2.0 license.
// Please see LICENSE file in the project root for terms.
package com.yahoo.labs.yamall.parser;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.yahoo.labs.yamall.core.Instance;

public class LIBSVMParserTest {

    @Test
    public void vwParserTest() {
        LIBSVMParser libsvmParser = new LIBSVMParser(18, false);

        Instance sample = libsvmParser.parse("-1.0 a:1");
        System.out.println(sample.toString());
        Assert.assertTrue(sample.getLabel() == -1.0);
        Assert.assertTrue(sample.getWeight() == 1.0);
        Assert.assertTrue(sample.getVector().size() == 2);

        // label -1, tag -1
        sample = libsvmParser.parse("-1 1:27 2_Private");
        System.out.println(sample.toString());
        Assert.assertTrue(sample.getLabel() == -1.0);
        Assert.assertTrue(sample.getWeight() == 1.0);
        Assert.assertTrue(sample.getVector().size() == 3);
    }
}
