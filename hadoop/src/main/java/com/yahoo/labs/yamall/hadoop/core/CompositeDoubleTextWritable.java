// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache 2.0 license.
// Please see LICENSE file in the project root for terms.
package com.yahoo.labs.yamall.hadoop.core;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;

public class CompositeDoubleTextWritable implements Writable {
    public double val1 = 0;
    public String val2 = "";

    public CompositeDoubleTextWritable() {
    }

    public CompositeDoubleTextWritable(
            double val1, String val2) {
        this.val1 = val1;
        this.val2 = val2;
    }

    public void readFields(DataInput in) throws IOException {
        val1 = in.readDouble();
        val2 = in.readUTF();
    }

    public void write(DataOutput out) throws IOException {
        out.writeDouble(val1);
        out.writeUTF(val2);
    }

    @Override
    public String toString() {
        return this.val1 + "\t" + this.val2;
    }

}