// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache 2.0 license.
// Please see LICENSE file in the project root for terms.
package com.yahoo.labs.yamall.hadoop.core;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;

/**
 * 
 * @author francesco
 * @version 1.0
 */
public class InstanceNoTagWritable implements Writable {

    private double label;
    private double weight;
    private SparseVectorWritable sv;

    public InstanceNoTagWritable() {
        sv = new SparseVectorWritable();
    }

    public void readFields(DataInput arg0) throws IOException {
        label = arg0.readDouble();
        weight = arg0.readDouble();
        sv.readFields(arg0);
    }

    public void write(DataOutput arg0) throws IOException {
        arg0.writeDouble(label);
        arg0.writeDouble(weight);
        sv.write(arg0);
    }

    public double getLabel() {
        return label;
    }

    public void setLabel(double label) {
        this.label = label;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public SparseVectorWritable getSparseVector() {
        return sv;
    }

    public void setSparseVector(SparseVectorWritable sv) {
        this.sv = sv;
    }

}
