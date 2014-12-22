package com.solidify.admin.reports;

import java.util.HashMap;

/**
 * Created by jennifermac on 12/22/14.
 */
public class ReportRow {

    private HashMap<String,String> vals = new HashMap<String,String>();

    public void set(String name, String val) {
        vals.put(name,val);
    }

    public String get(String name) {
        if (vals.containsKey(name)) {
            return vals.get(name);
        } else {
            return "";
        }
    }
}
