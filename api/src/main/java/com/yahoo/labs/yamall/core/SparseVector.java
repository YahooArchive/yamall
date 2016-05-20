// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache 2.0 license.
// Please see LICENSE file in the project root for terms.
package com.yahoo.labs.yamall.core;

import java.io.Serializable;

import it.unimi.dsi.fastutil.ints.Int2DoubleArrayMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;

/**
 * Sparse vectors.
 * 
 * @author Francesco Orabona, (francesco@yahoo-inc.com)
 * @version 1.1
 */
@SuppressWarnings("serial")
public class SparseVector extends Int2DoubleArrayMap implements Serializable {

    /**
     * Creates a new empty SparseVector of given capacity.
     * 
     * @param size
     *            the initial capacity.
     */
    public SparseVector(
            int size) {
        super(size);
    }

    /**
     * Creates a new empty array map.
     */
    public SparseVector() {
        super();
    }

    /**
     * Creates a new empty array map with given key and value backing arrays.
     * <p>
     * The resulting map will have as many entries as the given arrays. It is responsibility of the caller that the elements of key are distinct.
     * 
     * @param key
     *            the key array.
     * @param value
     *            the value array (it must have the same length as key).
     */
    public SparseVector(
            final int[] key, final double[] value) {
        super(key, value);
    }

    /**
     * Dot product between two SparseVectors.
     * <p>
     * The implementation takes advantage of the sparsity of the vectors, iterating over the one with less non-zero elements.
     * 
     * @param other
     *            vector to take the product with.
     * @return the result of the dot product.
     */
    public double dot(SparseVector other) {
        double result = 0.0;

        // We minimize the running time, by iterating over the vector with smaller number of coordinates.
        if (this.size() < other.size()) {
            for (Int2DoubleMap.Entry entry : this.int2DoubleEntrySet()) {
                result += (entry.getDoubleValue() * other.get(entry.getIntKey()));
            }
        }
        else {
            for (Int2DoubleMap.Entry entry : other.int2DoubleEntrySet()) {
                result += (entry.getDoubleValue() * this.get(entry.getIntKey()));
            }
        }
        return result;
    }

    /**
     * Dot product with a dense vector.
     * 
     * @param other
     *            dense vector.
     * @return the result of the dot product.
     */
    public double dot(double[] other) {
        double result = 0.0;

        for (Int2DoubleMap.Entry entry : this.int2DoubleEntrySet()) {
            double val = other[entry.getIntKey()];
            if (val != 0)
                result += (entry.getDoubleValue() * val);
        }
        return result;
    }

    /**
     * Add a SparseVector multiplied by a constant to a dense vector and put the result in the dense vector.
     * 
     * @param other
     *            dense vector.
     * @param scaling
     *            scaling to the sparse vector.
     */
    public void addScaledSparseVectorToDenseVector(double[] other, double scaling) {
        for (Int2DoubleMap.Entry entry : this.int2DoubleEntrySet()) {
            int key = entry.getIntKey();
            other[key] += scaling * entry.getDoubleValue();
        }
    }

    /**
     * Add a SparseVector multiplied by a constant.
     * 
     * @param other
     *            vector to add.
     * @param scaling
     *            constant to use in the multiplication.
     */
    public void addScaledSparseVector(SparseVector other, double scaling) {
        for (Int2DoubleMap.Entry entry : other.int2DoubleEntrySet()) {
            int key = entry.getIntKey();
            this.put(key, this.get(key) + scaling * entry.getDoubleValue());
        }
    }

    public static SparseVector rescaledDense2Sparse(double[] in, double scaling) {
        int count = 0;
        for (int i = 0; i < in.length; i++) {
            if (in[i] != 0) {
                count++;
            }
        }
        double[] values = new double[count];
        int[] keys = new int[count];
        int j = 0;
        for (int i = 0; j < count; i++) {
            if (in[i] != 0) {
                values[j] = in[i] * scaling;
                keys[j++] = i;
            }
        }
        return new SparseVector(keys, values);
    }

    /**
     * Squared L2 norm.
     * 
     * @return squared L2 norm.
     */
    public double squaredL2Norm() {
        double result = 0.0;
        for (Int2DoubleMap.Entry entry : this.int2DoubleEntrySet()) {
            double vl = entry.getDoubleValue();
            result += (vl * vl);
        }
        return result;
    }

    /**
     * Return the value of the maximum index.
     * 
     * @return the value of the maximum index.
     */
    public int maxIndex() {
        int max = Integer.MIN_VALUE;
        for (Int2DoubleMap.Entry entry : this.int2DoubleEntrySet()) {
            int cur = entry.getIntKey();
            if (cur > max)
                max = cur;
        }
        return max;
    }

    /**
     * String representation of the SparseVector.
     * <p>
     * It will return (key1,value1) (key2,value2) ...
     */
    public String toString() {
        String s = "";
        for (Int2DoubleMap.Entry entry : this.int2DoubleEntrySet())
            s += "(" + entry.getIntKey() + ", " + entry.getDoubleValue() + ") ";

        return s;
    }

    /**
     * Returns a dense vector.
     * <p>
     * If newsize is smaller than the size of the vector, the actual size of the vector is used instead.
     * 
     * @param newsize
     *            size of the new vector.
     * @return dense vector.
     */
    public double[] toDenseVector(int newsize) {
        newsize = Math.max(newsize, this.maxIndex() + 1);
        double[] vector = new double[newsize];
        for (Int2DoubleMap.Entry entry : this.int2DoubleEntrySet())
            vector[entry.getIntKey()] = entry.getDoubleValue();
        return vector;
    }

    /**
     * Builds a sparse vector from a dense one.
     * 
     * @param in
     *            dense vector.
     * @return sparse vector.
     */
    public static SparseVector dense2Sparse(double[] in) {
        int count = 0;
        for (int i = 0; i < in.length; i++) {
            if (in[i] != 0) {
                count++;
            }
        }
        double[] values = new double[count];
        int[] keys = new int[count];
        int j = 0;
        for (int i = 0; j < count; i++) {
            if (in[i] != 0) {
                values[j] = in[i];
                keys[j++] = i;
            }
        }
        return new SparseVector(keys, values);
    }

}
