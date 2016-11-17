// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache 2.0 license.
// Please see LICENSE file in the project root for terms.
package com.yahoo.labs.yamall.parser;

import java.util.ArrayList;
import java.util.HashMap;

import com.yahoo.labs.yamall.core.Instance;
import com.yahoo.labs.yamall.util.MurmurHash3;

import it.unimi.dsi.fastutil.chars.Char2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleIterator;
import it.unimi.dsi.fastutil.ints.IntArrayList;

/**
 * Parser of Tab Separated Values Format strings.
 * <p>
 * Two consecutive tabs are interpreted as a missing value.
 *
 * @author Francesco Orabona
 * @version 1.1
 */
public class TSVParser implements InstanceParser {

    private int constIndex = 0;
    private int bits;
    private int mask_hash;
    private HashMap<Integer, String> invertHashMap = null;
    private ArrayList<String> feature = null;
    private IntArrayList type = null;
    private ArrayList<String> namespace = null;
    private ArrayList<DoubleArrayList> bins = null;
    private Char2BooleanOpenHashMap ignoreNamespaceHashMap = null;

    /**
     * TSV Parser.
     * <p>
     * The specifications are divided by '\n' characters. For each line we have
     * <p>
     * field_name,field_type,name_space[,bins]
     * <p>
     * where field_name is the name of the feature field_type an indicator of whether this field is numerical, possible values for field_type are: 0
     * (categorical), 1 (numerical), 2(numerical, no binning), 3 (string tokens separated by white spaces). name_space is the name of the namespace. bins are
     * the bins border for the binning of numerical variables.
     * <p>
     * Specify the bins by adding a comma separated list of increasing real numbers. e.g., for the height variable, suppose we specify bins "177,180". This
     * makes height below 177 in bin -1, height in [177,180) in bin 0, height 180 and above in bin 1.
     * <p>
     * Reserved field_names: "label" for the label; "ignore" to ignore the feature, "weight" for the weight of the example, "tag" for the tag of the example.
     * The field_name and field_type are ignored for labels, weights, and tags.
     * <p>
     * Note that reserved field names cannot be ignored through the ignoreNamespaces parameter.
     * <p>
     * The labels are expected to be -1 and 1.
     * 
     * @param bits
     *            number of bits to use in the hashing, between 1 and 31.
     * @param ignoreNamespaces
     *            namespaces to ignore. If null, nothing will be ignored.
     * @param invertHash
     *            if set to True, it saves a map to invert the hashing function.
     * @param spec
     *            specifications for the features.
     */
    public TSVParser(
            int bits, String ignoreNamespaces, boolean invertHash, String spec) {
        if (bits > 31)
            bits = 31;
        else if (bits < 1)
            bits = 1;
        mask_hash = (1 << bits) - 1;
        this.bits = bits;
        if (invertHash) {
            invertHashMap = new HashMap<Integer, String>();
            invertHashMap.put(0, new String("bias_term"));
        }
        if (ignoreNamespaces != null) {
            ignoreNamespaceHashMap = new Char2BooleanOpenHashMap();
            for (char ch : ignoreNamespaces.toCharArray()) {
                ignoreNamespaceHashMap.put(ch, true);
            }
        }
        parseSpecifications(spec);
    }

    private void parseSpecifications(String spec) {
        feature = new ArrayList<String>();
        type = new IntArrayList();
        namespace = new ArrayList<String>();
        bins = new ArrayList<DoubleArrayList>();

        final FastStringTokenizer stringTokenizer = new FastStringTokenizer(spec, '\n');

        String token;
        while ((token = stringTokenizer.nextToken()) != null) {
            final FastStringTokenizer stringTokenizer2 = new FastStringTokenizer(token, ',');
            feature.add(stringTokenizer2.nextToken());
            type.add(NumberParser.getInteger(stringTokenizer2.nextToken()));
            String nt = stringTokenizer2.nextToken();
            if (nt == null)
                nt = new String("");
            else if (!nt.equals("label") && !nt.equals("weight") && !nt.equals("tag") && ignoreNamespaceHashMap != null)
                if (ignoreNamespaceHashMap.get(nt.charAt(0)))
                    nt = "ignore";
            namespace.add(nt);
            DoubleArrayList tmp = null;
            nt = stringTokenizer2.nextToken();
            if (nt != null) {
                tmp = new DoubleArrayList();
                do {
                    tmp.add(NumberParser.getDoubleNoSpecial(nt));
                }
                while ((nt = stringTokenizer2.nextToken()) != null);
            }
            bins.add(tmp);
        }
    }

    public Instance parse(String line) {
        Instance instance = new Instance();

        final FastStringTokenizer stringTokenizer = new FastStringTokenizer(line, '\t');

        int pos = 0;
        String token;
        while ((token = stringTokenizer.nextToken()) != null) {
            if (!token.equals("") && !namespace.get(pos).equals("ignore")) {
                if (namespace.get(pos).equals("label")) {
                    final double val = NumberParser.getDoubleNoSpecial(token);
                    instance.setLabel(val);
                }
                else if (namespace.get(pos).equals("weight")) {
                    final double val = NumberParser.getDoubleNoSpecial(token);
                    instance.setWeight(val);
                }
                else if (namespace.get(pos).equals("tag")) {
                    instance.setTag(token);
                }
                else {
                    switch (type.getInt(pos)) {
                        // categorical
                        case 0: {
                            String s = namespace.get(pos) + " " + feature.get(pos) + "_" + token;
                            int hashed = MurmurHash3.maskedHash(s, mask_hash);
                            instance.getVector().put(hashed, 1.0);
                            if (invertHashMap != null)
                                invertHashMap.put(hashed, s);
                            break;
                        }

                        // numerical
                        case 1: {
                            if (bins.get(pos) == null) {
                                String s = namespace.get(pos) + " " + feature.get(pos);
                                int hashed = MurmurHash3.maskedHash(s, mask_hash);
                                double val = NumberParser.getDoubleNoSpecial(token);
                                if (Math.abs(val) > 1e-10) {
                                    instance.getVector().put(hashed, val);
                                    if (invertHashMap != null)
                                        invertHashMap.put(hashed, s);
                                }
                            }
                            else {
                                double val = NumberParser.getDoubleNoSpecial(token);
                                DoubleArrayList bin_borders = bins.get(pos);
                                DoubleIterator iter = bin_borders.iterator();
                                int i = -1;
                                while (iter.hasNext() && iter.nextDouble() <= val)
                                    i++;
                                String s = namespace.get(pos) + " " + feature.get(pos) + "_" + Integer.toString(i);
                                int hashed = MurmurHash3.maskedHash(s, mask_hash);
                                instance.getVector().put(hashed, 1.0);
                                if (invertHashMap != null)
                                    invertHashMap.put(hashed, s);
                            }
                            break;
                        }

                        // numerical, never binning
                        case 2: {
                            String s = namespace.get(pos) + " " + feature.get(pos);
                            int hashed = MurmurHash3.maskedHash(s, mask_hash);
                            double val = NumberParser.getDoubleNoSpecial(token);
                            if (Math.abs(val) > 1e-10) {
                                instance.getVector().put(hashed, val);
                                if (invertHashMap != null)
                                    invertHashMap.put(hashed, s);
                            }
                            break;
                        }

                        // string tokens
                        case 3: {
                            final FastStringTokenizer stringTokenizer2 = new FastStringTokenizer(token, ' ');
                            String token2;
                            while ((token2 = stringTokenizer2.nextToken()) != null) {
                                String s = namespace.get(pos) + " " + feature.get(pos) + "_" + token2;
                                int hashed = MurmurHash3.maskedHash(s, mask_hash);
                                instance.getVector().put(hashed, 1);
                                if (invertHashMap != null)
                                    invertHashMap.put(hashed, s);
                            }
                            break;
                        }
                    }
                }
            }
            pos++;
        }

        // append a constant feature to every example.
        instance.getVector().put(constIndex, 1.0);
        return instance;

    }

    public HashMap<Integer, String> getInvertHashMap() {
        return invertHashMap;
    }

    public String toString() {
        return "TSV parser";
    }
}
