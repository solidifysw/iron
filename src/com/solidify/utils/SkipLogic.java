package com.solidify.utils;

import java.util.HashSet;

/**
 * Created by jrobins on 3/4/15.
 */
public interface SkipLogic {
    public boolean skip(String key, String currentKey, HashSet<String> keys);
}
