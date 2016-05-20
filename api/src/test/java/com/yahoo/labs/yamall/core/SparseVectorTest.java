// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache 2.0 license.
// Please see LICENSE file in the project root for terms.
package com.yahoo.labs.yamall.core;

import org.testng.Assert;
import org.testng.annotations.Test;

public class SparseVectorTest {

    @Test
    public void putGetTest() {
        SparseVector a = new SparseVector();

        a.put(1, 1);
        a.put(2, 2);
        a.put(3, 3);

        Assert.assertTrue(a.get(1) == 1);
        Assert.assertTrue(a.get(2) == 2);
        Assert.assertTrue(a.get(3) == 3);
        Assert.assertTrue(a.get(4) == 0);
    }

    @Test
    public void dotTest() {
        SparseVector a = new SparseVector();
        SparseVector b = new SparseVector();

        a.put(1, 1.0);
        a.put(2, 2.0);
        a.put(3, 3.0);

        b.put(1, 4.0);
        b.put(2, 5.0);
        b.put(3, 6.0);
        b.put(4, 7.0);

        double[] c = new double[5];
        c[1] = 4.0;
        c[2] = 5.0;
        c[3] = 6.0;
        c[4] = 7.0;

        Assert.assertTrue(a.dot(b) == 32);
        Assert.assertTrue(b.dot(a) == 32);
        Assert.assertTrue(a.dot(c) == 32);
    }

    @Test
    public void addScaledTest() {
        SparseVector a = new SparseVector();
        SparseVector b = new SparseVector();

        a.put(1, 1.0);
        a.put(2, 2.0);
        a.put(3, 3.0);

        b.put(1, 4.0);
        b.put(2, 5.0);
        b.put(3, 6.0);

        a.addScaledSparseVector(b, 1.0);
        Assert.assertTrue(a.get(1) == 5.0);
        Assert.assertTrue(a.get(2) == 7.0);
        Assert.assertTrue(a.get(3) == 9.0);
        Assert.assertTrue(a.get(4) == 0.0);

        a.addScaledSparseVector(b, 2.0);
        Assert.assertTrue(a.get(1) == 13.0);
        Assert.assertTrue(a.get(2) == 17.0);
        Assert.assertTrue(a.get(3) == 21.0);
        Assert.assertTrue(a.get(4) == 0.0);
    }

    @Test
    public void addScaledDenseTest() {
        SparseVector a = new SparseVector();

        a.put(1, 1.0);
        a.put(2, 2.0);
        a.put(3, 3.0);

        double[] c = new double[5];
        c[1] = 4.0;
        c[2] = 5.0;
        c[3] = 6.0;
        c[4] = 7.0;

        a.addScaledSparseVectorToDenseVector(c, 1.0);
        Assert.assertTrue(a.get(1) == 1.0);
        Assert.assertTrue(a.get(2) == 2.0);
        Assert.assertTrue(a.get(3) == 3.0);
        Assert.assertTrue(a.get(4) == 0.0);
        Assert.assertTrue(c[1] == 5.0);
        Assert.assertTrue(c[2] == 7.0);
        Assert.assertTrue(c[3] == 9.0);
        Assert.assertTrue(c[4] == 7.0);

        c[1] = 4.0;
        c[2] = 5.0;
        c[3] = 6.0;
        c[4] = 7.0;
        a.addScaledSparseVectorToDenseVector(c, -2.0);
        Assert.assertTrue(a.get(1) == 1.0);
        Assert.assertTrue(a.get(2) == 2.0);
        Assert.assertTrue(a.get(3) == 3.0);
        Assert.assertTrue(a.get(4) == 0.0);
        Assert.assertTrue(c[1] == 2.0);
        Assert.assertTrue(c[2] == 1.0);
        Assert.assertTrue(c[3] == 0.0);
        Assert.assertTrue(c[4] == 7.0);
    }

    @Test
    public void squaredL2NormTest() {
        SparseVector a = new SparseVector();

        a.put(1, 4.5);
        a.put(2, 3.4);
        a.put(3, -7.0);

        Assert.assertTrue(a.squaredL2Norm() == a.dot(a));
    }

    @Test
    public void maxIndexTest() {
        SparseVector a = new SparseVector();

        a.put(1, 4.5);
        a.put(2, 3.4);
        a.put(3, -7.0);

        Assert.assertTrue(a.maxIndex() == 3);
    }

    @Test
    public void toDenseVectorTest() {
        SparseVector a = new SparseVector();

        a.put(1, 4.5);
        a.put(20, 3.4);
        a.put(30, -7.0);

        double[] b = a.toDenseVector(4);

        Assert.assertTrue(b[1] == 4.5);
        Assert.assertTrue(b[20] == 3.4);
        Assert.assertTrue(b[30] == -7.0);
    }

    @Test
    public void dense2SparseTest() {
        double[] a = new double[5];
        a[1] = 4.0;
        a[2] = 5.0;
        a[3] = 6.0;
        a[4] = 7.0;

        SparseVector b = SparseVector.dense2Sparse(a);

        Assert.assertTrue(b.get(1) == 4.0);
        Assert.assertTrue(b.get(2) == 5.0);
        Assert.assertTrue(b.get(3) == 6.0);
        Assert.assertTrue(b.get(4) == 7.0);
    }

}
