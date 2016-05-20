// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache 2.0 license.
// Please see LICENSE file in the project root for terms.
package com.yahoo.labs.yamall.core;

import org.testng.Assert;
import org.testng.annotations.Test;

public class InstanceTest {

    @Test
    public void constructorTest() {
        Instance a = new Instance();

        Assert.assertTrue(a.getLabel() == 0.0);
        Assert.assertTrue(a.getTag().equals(""));
        Assert.assertTrue(a.getVector().size() == 0);
        Assert.assertTrue(a.getWeight() == 1.0);
    }

}
