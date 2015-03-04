package com.solidify.utils;

import java.util.HashSet;

/**
 * Created by jrobins on 3/4/15.  Determines if the current parsed key should be included or skipped when creating
 * the final parsed object.  This version is used to skip any of the keys in the keys HashSet.  currentKey is indicates
 * the current position in the parser and is maintained by the ParsedObject class.
 */
public class Skip implements SkipLogic {
    @Override
    public boolean skip(String key, String currentKey, HashSet<String> keys) {
        boolean out = false;
        if (keys.isEmpty()) {
            return out;
        }

        if (currentKey.length() > 0) { // currentKey may have something like abc.def so put .key on the end and check keys
            if (keys.contains(currentKey + "." + key)) {
                out = true;
            }
        } else if (keys.contains(key)) {
            out = true;
        }

        return out;
    }
}
