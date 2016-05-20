// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache 2.0 license.
// Please see LICENSE file in the project root for terms.
package com.yahoo.labs.yamall.parser;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.yahoo.labs.yamall.core.Instance;

public class VWParserTest {

    @Test
    public void vwParserTest() {
        VWParser vwParser = new VWParser(18, "cd", false);

        Instance sample = vwParser.parse("-1.0 1.0 tag| a:1");
        System.out.println(sample.toString());
        Assert.assertTrue(sample.getLabel() == -1.0);
        Assert.assertTrue(sample.getWeight() == 1.0);
        Assert.assertTrue(sample.getTag().equals("tag"));
        Assert.assertTrue(sample.getVector().size() == 2);

        // label -1, tag -1
        sample = vwParser.parse("-1 -1|f 1:27 2_Private");
        System.out.println(sample.toString());
        Assert.assertTrue(sample.getLabel() == -1.0);
        Assert.assertTrue(sample.getWeight() == 1.0);
        Assert.assertTrue(sample.getTag().equals("-1"));
        Assert.assertTrue(sample.getVector().size() == 3);

        // label -1, tag -1, 'c' namespace should be skipped
        sample = vwParser.parse("-1 -1|c 1:27 2_Private |a 3:1");
        System.out.println(sample.toString());
        Assert.assertTrue(sample.getLabel() == -1.0);
        Assert.assertTrue(sample.getWeight() == 1.0);
        Assert.assertTrue(sample.getTag().equals("-1"));
        Assert.assertTrue(sample.getVector().size() == 2);
    }
}
