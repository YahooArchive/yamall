// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache 2.0 license.
// Please see LICENSE file in the project root for terms.
package com.yahoo.labs.yamall.core;

import java.io.Serializable;

/**
 * Representation of an instance.
 * <p>
 * An instance is defined by a label, a weight, a tag, and a vector.
 * 
 * @author Francesco Orabona
 * @version 1.0
 */
@SuppressWarnings("serial")
public class Instance implements Serializable {
    private double label;
    private double weight;
    private String tag;
    private SparseVector sv;

    /**
     * @return the label
     */
    public double getLabel() {
        return label;
    }

    /**
     * @param label
     *            the label to set
     */
    public void setLabel(double label) {
        this.label = label;
    }

    /**
     * @return the weight
     */
    public double getWeight() {
        return weight;
    }

    /**
     * @param weight
     *            the weight to set
     */
    public void setWeight(double weight) {
        this.weight = weight;
    }

    /**
     * @return the tag
     */
    public String getTag() {
        return tag;
    }

    /**
     * @param tag
     *            the tag to set
     */
    public void setTag(String tag) {
        this.tag = tag;
    }

    /**
     * @return the vector
     */
    public SparseVector getVector() {
        return sv;
    }

    /**
     * @param vector
     *            the vector to set
     */
    public void setVector(SparseVector vector) {
        this.sv = vector;
    }

    /**
     * Builds and empty vector with label=0, weight=1.0 and empty tag.
     */
    public Instance() {
        label = 0.0;
        weight = 1.0;
        tag = "";
        sv = new SparseVector();
    }

    /**
     * Builds and empty vector with specified label, weight=1.0 and empty tag.
     * 
     * @param label
     *            label to be used for the instance.
     */
    public Instance(
            double label) {
        this.label = label;
        weight = 1.0;
        tag = "";
        sv = new SparseVector();
    }

    /**
     * String representation of the instance.
     */
    public String toString() {
        String s = "";
        s += "Label: " + label + " weight: " + weight + " tag: " + tag + "\nFeatures:" + sv.toString();
        return s;
    }

}
