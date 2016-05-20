// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache 2.0 license.
// Please see LICENSE file in the project root for terms.
package com.yahoo.labs.yamall.hadoop.core;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.io.Writable;

public class HashMapInt2StringWritable implements Writable {

    private HashMap<Integer, String> entries = null;

    public HashMapInt2StringWritable() {
        entries = new HashMap<Integer, String>();
    }

    public HashMapInt2StringWritable(
            HashMap<Integer, String> in) {
        entries = in;
    }

    public HashMap<Integer, String> getEntries() {
        return entries;
    }

    public void setEntries(HashMap<Integer, String> entries) {
        this.entries = entries;
    }

    public void merge(HashMapInt2StringWritable other) {
        for (Map.Entry<Integer, String> entry : other.entries.entrySet()) {
            entries.put(entry.getKey(), entry.getValue());
        }
    }

    public void write(DataOutput out) throws IOException {
        out.writeInt(entries.size());
        for (Map.Entry<Integer, String> entry : entries.entrySet()) {
            out.writeInt(entry.getKey());
            out.writeUTF(entry.getValue());
        }
    }

    public void readFields(DataInput in) throws IOException {
        int size = in.readInt();

        entries = new HashMap<Integer, String>(size);

        for (int i = 0; i < size; i++) {
            int key = in.readInt();
            String value = in.readUTF();
            entries.put(key, value);
        }
    }

}
