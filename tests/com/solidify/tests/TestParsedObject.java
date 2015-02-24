package com.solidify.tests;

import com.solidify.utils.ParsedObject;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created by jrobins on 2/24/15.
 */
public class TestParsedObject {
    @Test
    public void testParsedObject() {
        JSONObject jo = new JSONObject();
        jo.put("a","1");
        jo.put("b","2");
        JSONObject jo2 = new JSONObject();
        jo2.put("c","3");
        JSONObject jo3 = new JSONObject();
        jo3.put("d","4");
        jo2.put("y",jo3);
        jo.put("x",jo2);
        ParsedObject po = new ParsedObject(jo.toString());
        JSONObject obj = po.get();
        System.out.println(obj.toString());
        assertTrue(obj.has("a"));
    }
}
