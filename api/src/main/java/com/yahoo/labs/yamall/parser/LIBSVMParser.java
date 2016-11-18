// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache 2.0 license.
// Please see LICENSE file in the project root for terms.
package com.yahoo.labs.yamall.parser;

import java.util.HashMap;

import com.yahoo.labs.yamall.core.Instance;
import com.yahoo.labs.yamall.util.MurmurHash3;

/**
 * Parser of LIBSVM Format strings
 * <p>
 * The format is: <tt>
 * Label Features
 * </tt>
 * </p>
 *
 * <p>
 * where:
 * </p>
 *
 * <ol>
 * <li>Label and is a floating point number.</li>
 * <li>Features = (String[:Value] )*</li>
 * </ol>
 *
 * <p>
 * Value is a floating point number. If Value is omitted, it is treated as 1.0.
 * </p>
 *
 * @author Francesco Orabona
 * @version 1.0
 */
public class LIBSVMParser implements InstanceParser {

    private int constIndex = 0;
    private int bits;
    private int mask_hash;
    private HashMap<Integer, String> invertHashMap = null;

    /**
     * LIBSVM Parser.
     * 
     * @param bits
     *            number of bits to use in the hashing, between 1 and 31.
     * @param invertHash
     *            if set to True, it saves a map to invert the hashing function.
     */
    public LIBSVMParser(
            int bits, boolean invertHash) {
        if (bits > 31)
            bits = 31;
        else if (bits < 1)
            bits = 1;
        this.bits = bits;
        mask_hash = (1 << bits) - 1;
        if (invertHash) {
            invertHashMap = new HashMap<Integer, String>();
            invertHashMap.put(0, new String("bias_term"));
        }
    }

    public Instance parse(String line) {
        Instance instance = new Instance();

        final FastStringTokenizer stringTokenizer = new FastStringTokenizer(line, ' ');
        instance.setLabel(NumberParser.getDoubleNoSpecial(stringTokenizer.nextToken()));
        //parsePrefix(instance, stringTokenizer.nextToken());
        String token;
        while ((token = stringTokenizer.nextToken()) != null) {
            int pos = token.indexOf(":");
            if (pos > 0) {
                // a feature value is present
                // final double featureValue = Double.parseDouble(token.substring(pos + 1));
                // final double featureValue = NumberParser.getDoubleNoSpecial(token.substring(pos + 1));
                final double featureValue = NumberParser.getDoubleNoSpecial(token, pos + 1, token.length());
                // take the feature name
                if (Math.abs(featureValue) > 1e-10) {
                    String s = new String(token.substring(0, pos));
                    int hashed = MurmurHash3.maskedHash(s, mask_hash);
                    instance.getVector().put(hashed, featureValue);
                    if (invertHashMap != null)
                        invertHashMap.put(hashed, s);
                }
            }
            else {
                // no feature value present, hence it is 1
                int hashed = MurmurHash3.maskedHash(token, mask_hash);
                instance.getVector().put(hashed, 1.0);
                if (invertHashMap != null)
                    invertHashMap.put(hashed, token);
            }
        }
        
        // append a constant feature to every example.
        instance.getVector().put(constIndex, 1.0);
        return instance;
    }

    public HashMap<Integer, String> getInvertHashMap() {
        return invertHashMap;
    }

    public String toString() {
        return "LIBSVM parser";
    }
}
