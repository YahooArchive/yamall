// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache 2.0 license.
// Please see LICENSE file in the project root for terms.
package com.yahoo.labs.yamall.parser;

import java.util.HashMap;

import com.yahoo.labs.yamall.core.Instance;
import com.yahoo.labs.yamall.util.MurmurHash3;

import it.unimi.dsi.fastutil.chars.Char2BooleanOpenHashMap;

/**
 * Parser of VW Format strings
 * <p>
 * The format is: <tt>
 * [Label] [Importance] [Tag]|Namespace Features |Namespace Features ... |Namespace Features
 * </tt>
 * </p>
 *
 * <p>
 * where:
 * </p>
 *
 * <ol>
 * <li>Label and Importance are floating point numbers. If Importance is missing, it is treated as 1.0.</li>
 * <li>Namespace = String[:Value]</li>
 * <li>Features = (String[:Value] )*</li>
 * <li>Tag = optional String</li>
 * </ol>
 *
 * <p>
 * Value is a floating point number. If Value is omitted, it is treated as 1.0.
 * </p>
 *
 * @author Francesco Orabona, (francesco@yahoo-inc.com)
 * @version 1.0
 */
public class VWParser implements InstanceParser {

    private int constIndex = 0;
    private int bits;
    private int mask_hash;
    private HashMap<Integer, String> invertHashMap = null;
    private Char2BooleanOpenHashMap ignoreNamespaceHashMap = null;

    /**
     * VW Parser.
     * 
     * @param bits
     *            number of bits to use in the hashing, between 1 and 31.
     * @param ignoreNamespaces
     *            namespaces to ignore. If null, nothing will be ignored.
     * @param invertHash
     *            if set to True, it saves a map to invert the hashing function.
     */
    public VWParser(
            int bits, String ignoreNamespaces, boolean invertHash) {
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
        if (ignoreNamespaces != null) {
            ignoreNamespaceHashMap = new Char2BooleanOpenHashMap();
            for (char ch : ignoreNamespaces.toCharArray()) {
                ignoreNamespaceHashMap.put(ch, true);
            }
        }
    }

    public Instance parse(String line) {
        Instance instance = new Instance();

        final FastStringTokenizer stringTokenizer = new FastStringTokenizer(line, '|');
        parsePrefix(instance, stringTokenizer.nextToken());
        String token;
        while ((token = stringTokenizer.nextToken()) != null) {
            parseSuffix(instance, token);
        }

        // append a constant feature to every example.
        instance.getVector().put(constIndex, 1.0);
        return instance;
    }

    public HashMap<Integer, String> getInvertHashMap() {
        return invertHashMap;
    }

    private void parsePrefix(Instance instance, String subLine) {
        final FastStringTokenizer tokenizer = new FastStringTokenizer(subLine, ' ');

        if (subLine.charAt(0) == ' ') {
            // the label is not present
            instance.setLabel(0);
            tokenizer.nextToken(); // skip the first space
        }
        else {
            // instance.label = Double.parseDouble(tokenizer.nextToken());
            instance.setLabel(NumberParser.getDoubleNoSpecial(tokenizer.nextToken()));
        }

        String token = tokenizer.nextToken();
        // this token could be either an importance or a tag.
        if (token == null) {
            // end of suffix: stop
            return;
        }
        else {
            String nextToken = tokenizer.nextToken();
            if (nextToken == null && subLine.charAt(subLine.length() - 1) != ' ') {
                instance.setTag(token);
            }
            else {
                // instance.weight = Double.parseDouble(token);
                instance.setWeight(NumberParser.getDoubleNoSpecial(token));
                // parse tag
                if (nextToken != null) {
                    instance.setTag(nextToken);
                }
            }
        }
    }

    private void parseSuffix(Instance instance, String subLine) {
        double namespaceValue = 1.0;
        String namespaceName = "";

        final FastStringTokenizer tokenizer = new FastStringTokenizer(subLine, ' ');
        String token = null;

        if (subLine.charAt(0) != ' ') {
            // there is a namespace!
            token = tokenizer.nextToken();

            int pos = token.indexOf(":");
            if (pos > 0) {
                // a namespace value is present
                // namespaceValue = Double.parseDouble(token.substring(pos + 1));
                // namespaceValue = NumberParser.getDoubleNoSpecial(token.substring(pos + 1));
                namespaceValue = NumberParser.getDoubleNoSpecial(token, pos + 1, token.length());
                // take the namespace name
                namespaceName = token.substring(0, pos);
            }
            else {
                namespaceName = token;
            }
            if (ignoreNamespaceHashMap != null) {
                if (ignoreNamespaceHashMap.get(namespaceName.charAt(0)))
                    return;
            }
        }

        // parse features
        while ((token = tokenizer.nextToken()) != null) {
            int pos = token.indexOf(":");
            if (pos > 0) {
                // a feature value is present
                // final double featureValue = Double.parseDouble(token.substring(pos + 1));
                // final double featureValue = NumberParser.getDoubleNoSpecial(token.substring(pos + 1));
                final double featureValue = NumberParser.getDoubleNoSpecial(token, pos + 1, token.length());
                // take the feature name
                if (Math.abs(featureValue) > 1e-10) {
                    String s = new String(namespaceName + " " + token.substring(0, pos));
                    int hashed = MurmurHash3.maskedHash(s, mask_hash);
                    instance.getVector().put(hashed, namespaceValue * featureValue);
                    if (invertHashMap != null)
                        invertHashMap.put(hashed, s);
                }
            }
            else {
                // no feature value present, hence it is 1
                String s = new String(namespaceName + " " + token);
                int hashed = MurmurHash3.maskedHash(s, mask_hash);
                instance.getVector().put(hashed, namespaceValue);
                if (invertHashMap != null)
                    invertHashMap.put(hashed, s);
            }
        }
    }
}
