// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache 2.0 license.
// Please see LICENSE file in the project root for terms.
package com.yahoo.labs.yamall.hadoop.core;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;

public class InstanceOrHashMapWritable implements Writable {
    public enum TypeWritable {
        EMPTY, INSTANCE, HASHMAP
    }

    private Writable o;
    private TypeWritable type;

    public InstanceOrHashMapWritable() {
        o = null;
        type = TypeWritable.EMPTY;
    }

    public InstanceOrHashMapWritable(
            InstanceNoTagWritable instance) {
        o = (Writable) instance;
        type = TypeWritable.INSTANCE;
    }

    public InstanceOrHashMapWritable(
            HashMapInt2StringWritable hm) {
        o = (Writable) hm;
        type = TypeWritable.HASHMAP;
    }

    public void readFields(DataInput in) throws IOException {
        type = TypeWritable.values()[in.readInt()];
        if (type == TypeWritable.INSTANCE) {
            InstanceNoTagWritable instance = new InstanceNoTagWritable();
            instance.readFields(in);
            o = (Writable) instance;
        }
        else {
            HashMapInt2StringWritable hm = new HashMapInt2StringWritable();
            hm.readFields(in);
            o = (Writable) hm;
        }
    }

    public void write(DataOutput out) throws IOException {
        out.writeInt(type.ordinal());
        if (type == TypeWritable.INSTANCE) {
            ((InstanceNoTagWritable) o).write(out);
        }
        else {
            ((HashMapInt2StringWritable) o).write(out);
        }
    }

    public Writable getObject() {
        return o;
    }

    public void setObject(Writable o) {
        this.o = o;
    }

    public TypeWritable getType() {
        return type;
    }

    public void setType(TypeWritable type) {
        this.type = type;
    }

}