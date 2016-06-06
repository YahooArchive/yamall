// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache 2.0 license.
// Please see LICENSE file in the project root for terms.
package com.yahoo.labs.yamall.parser;

import java.util.ArrayList;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.yahoo.labs.yamall.core.Instance;

import it.unimi.dsi.fastutil.ints.Int2DoubleMap;

public class TSVParserTest {

    @Test
    public void tsvParserTest() {
        String spec = "name,0,ignore\nmale,1,label\nweight,1,feature\nstrong,0,feature\nlas,0,\npersonality,3,description\nheight,1,feature,177,180";
        TSVParser tsvParser = new TSVParser(18, null, true, spec);

        String str = "john\t1\t76.0\ttrue\ttrue\toccasional jogger\t150";
        Instance sample = tsvParser.parse(str);
        System.out.println(str);
        ArrayList<String> f = new ArrayList<String>();
        for (Int2DoubleMap.Entry entry : sample.getVector().int2DoubleEntrySet()) {
            System.out.println(entry.getIntKey() + "=" + tsvParser.getInvertHashMap().get(entry.getIntKey()) + ","
                    + entry.getDoubleValue());
            f.add(tsvParser.getInvertHashMap().get(entry.getIntKey()));
        }
        System.out.println();
        Assert.assertTrue(sample.getLabel() == 1.0);
        Assert.assertTrue(sample.getVector().containsValue(76.0));
        Assert.assertTrue(f.contains("description personality_jogger"));
        Assert.assertTrue(f.contains("description personality_occasional"));
        Assert.assertTrue(f.contains("feature height_-1"));

        str = "janes\t-1\t80.0\tfalse\ttrue\tavid jogger\t178";
        sample = tsvParser.parse(str);
        System.out.println(str);
        f = new ArrayList<String>();
        for (Int2DoubleMap.Entry entry : sample.getVector().int2DoubleEntrySet()) {
            System.out.println(entry.getIntKey() + "=" + tsvParser.getInvertHashMap().get(entry.getIntKey()) + ","
                    + entry.getDoubleValue());
            f.add(tsvParser.getInvertHashMap().get(entry.getIntKey()));
        }
        System.out.println();
        Assert.assertTrue(sample.getLabel() == -1.0);
        Assert.assertTrue(sample.getVector().containsValue(80.0));
        Assert.assertTrue(f.contains("feature height_0"));

        str = "janes\t-1\t80.0\tfalse\ttrue\tavid jogger\t177";
        sample = tsvParser.parse(str);
        System.out.println(str);
        f = new ArrayList<String>();
        for (Int2DoubleMap.Entry entry : sample.getVector().int2DoubleEntrySet()) {
            System.out.println(entry.getIntKey() + "=" + tsvParser.getInvertHashMap().get(entry.getIntKey()) + ","
                    + entry.getDoubleValue());
            f.add(tsvParser.getInvertHashMap().get(entry.getIntKey()));
        }
        System.out.println();
        Assert.assertTrue(sample.getLabel() == -1.0);
        Assert.assertTrue(sample.getVector().containsValue(80.0));
        Assert.assertTrue(f.contains("feature height_0"));

        str = "janes\t-1\t80.0\tfalse\ttrue\tavid jogger\t190";
        sample = tsvParser.parse(str);
        System.out.println(str);
        f = new ArrayList<String>();
        for (Int2DoubleMap.Entry entry : sample.getVector().int2DoubleEntrySet()) {
            System.out.println(entry.getIntKey() + "=" + tsvParser.getInvertHashMap().get(entry.getIntKey()) + ","
                    + entry.getDoubleValue());
            f.add(tsvParser.getInvertHashMap().get(entry.getIntKey()));
        }
        System.out.println();
        Assert.assertTrue(sample.getLabel() == -1.0);
        Assert.assertTrue(sample.getVector().containsValue(80.0));
        Assert.assertTrue(f.contains("feature height_2"));

        str = "janes\t-1\t\t\t\t\t190";
        sample = tsvParser.parse(str);
        System.out.println(str);
        f = new ArrayList<String>();
        for (Int2DoubleMap.Entry entry : sample.getVector().int2DoubleEntrySet()) {
            System.out.println(entry.getIntKey() + "=" + tsvParser.getInvertHashMap().get(entry.getIntKey()) + ","
                    + entry.getDoubleValue());
            f.add(tsvParser.getInvertHashMap().get(entry.getIntKey()));
        }
        System.out.println();
        Assert.assertTrue(sample.getVector().size() == 2);

        spec = "name,0,ignore\nmale,1,label\nweight_pounds,1,a\nstrong,0,b\nnum,3,c,177,180\npersonality,1,d";
        tsvParser = new TSVParser(18, "abc", true, spec);
        str = "janes\t-1\t100\ttrue\t150\t2";
        sample = tsvParser.parse(str);
        System.out.println(str);
        f = new ArrayList<String>();
        for (Int2DoubleMap.Entry entry : sample.getVector().int2DoubleEntrySet()) {
            System.out.println(entry.getIntKey() + "=" + tsvParser.getInvertHashMap().get(entry.getIntKey()) + ","
                    + entry.getDoubleValue());
            f.add(tsvParser.getInvertHashMap().get(entry.getIntKey()));
        }
        System.out.println();
        Assert.assertTrue(sample.getLabel() == -1.0);
        Assert.assertTrue(sample.getVector().containsValue(2.0));
        Assert.assertTrue(sample.getVector().size() == 2);

        spec = "name,0,ignore\nmale,1,label\nimportance,1,weight";
        tsvParser = new TSVParser(18, "abc", true, spec);
        str = "janes\t-1\t3";
        sample = tsvParser.parse(str);
        System.out.println(str);
        f = new ArrayList<String>();
        for (Int2DoubleMap.Entry entry : sample.getVector().int2DoubleEntrySet()) {
            System.out.println(entry.getIntKey() + "=" + tsvParser.getInvertHashMap().get(entry.getIntKey()) + ","
                    + entry.getDoubleValue());
            f.add(tsvParser.getInvertHashMap().get(entry.getIntKey()));
        }
        System.out.println();
        Assert.assertTrue(sample.getLabel() == -1.0);
        Assert.assertTrue(sample.getWeight() == 3);

        spec = "name,0,ignore\nmale,1,label\nimportance,1,weight\ntag,1,tag";
        tsvParser = new TSVParser(18, "abclw", true, spec);
        str = "janes\t-1\t3\tiamthetag";
        sample = tsvParser.parse(str);
        System.out.println(str);
        f = new ArrayList<String>();
        for (Int2DoubleMap.Entry entry : sample.getVector().int2DoubleEntrySet()) {
            System.out.println(entry.getIntKey() + "=" + tsvParser.getInvertHashMap().get(entry.getIntKey()) + ","
                    + entry.getDoubleValue());
            f.add(tsvParser.getInvertHashMap().get(entry.getIntKey()));
        }
        System.out.println();
        // label, tag, and weight cannot be ignored
        Assert.assertTrue(sample.getLabel() == -1.0);
        Assert.assertTrue(sample.getWeight() == 3);
        Assert.assertTrue(sample.getTag().equals("iamthetag"));
    }
}
