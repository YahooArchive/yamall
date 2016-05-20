// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache 2.0 license.
// Please see LICENSE file in the project root for terms.
package com.yahoo.labs.yamall.hadoop.core;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;

import com.yahoo.labs.yamall.core.SparseVector;

import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

public class SparseVectorWritable implements Writable {

    private SparseVector entries = null;

    SparseVectorWritable() {
    }

    public SparseVector getEntries() {
        return entries;
    }

    public void setEntries(SparseVector entries) {
        this.entries = entries;
    }

    public void write(DataOutput out) throws IOException {
        out.writeInt(entries.size());
        ObjectIterator<Int2DoubleMap.Entry> iter = entries.int2DoubleEntrySet().fastIterator();
        while (iter.hasNext()) {
            Int2DoubleMap.Entry entry = iter.next();
            out.writeInt(entry.getIntKey());
            out.writeDouble(entry.getDoubleValue());
        }
    }

    public void readFields(DataInput in) throws IOException {
        int size = in.readInt();

        double[] values = new double[size];
        int[] keys = new int[size];
        for (int i = 0; i < size; i++) {
            keys[i] = in.readInt();
            values[i] = in.readDouble();
        }
        entries = new SparseVector(keys, values);
    }

}
