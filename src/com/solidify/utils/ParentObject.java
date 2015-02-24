package com.solidify.utils;

import org.json.JSONObject;

/**
 * Created by jrobins on 2/24/15.
 */
public class ParentObject extends JSONObject {
    private String field;

    public ParentObject() {
        super();
        this.field = null;
    }

    public ParentObject(String field) {
        super();
        this.field = field;
    }

    public String getField() {
        return field;
    }

}
