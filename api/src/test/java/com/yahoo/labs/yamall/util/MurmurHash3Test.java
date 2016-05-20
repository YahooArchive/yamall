// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache 2.0 license.
// Please see LICENSE file in the project root for terms.
package com.yahoo.labs.yamall.util;

import java.util.UUID;

import org.testng.Assert;
import org.testng.annotations.Test;

public class MurmurHash3Test {

    @Test
    public void maskedHashTest() {
        String uuid;

        int mask = (1 << 18) - 1;
        int neg = 0;
        int max = 0;
        for (int i = 0; i < 100000; i++) {
            uuid = UUID.randomUUID().toString();
            int idx = MurmurHash3.maskedHash(uuid, mask);
            if (idx < 0)
                neg++;
            if (max < idx)
                max = idx;
        }
        Assert.assertTrue(neg == 0);
        Assert.assertTrue(max <= mask);
    }

}
