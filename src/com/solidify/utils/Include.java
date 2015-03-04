package com.solidify.utils;

import java.util.HashSet;

/**
 * Created by jrobins on 3/4/15.  Determines if the current parser key should be included in the results.
 * Provides the ability to skip all keys accept the ones in the keys HashSet.  currentKey is the position of the parser
 * maintained by the ParsedObject class.
 */
public class Include implements SkipLogic {
    @Override
    public boolean skip(String key, String currentKey, HashSet<String> keys) {
       boolean out = true;
        if (currentKey.length() > 0) {
            if (keys.contains(currentKey)) {
                out = false;
            } else {
                for (String val : keys) {
                    if (val.startsWith(currentKey+"."+key)) {
                        out = false;
                        break;
                    }
                }
            }
        } else {
            if (keys.contains(key)) {
                out = false;
            } else {
                for (String val : keys) {
                    if (val.startsWith(key)) {
                        out = false;
                        break;
                    }
                }
            }
        }
        return out;
    }
}
