package com.solidify.tests;

import com.solidify.admin.reports.Utils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashSet;

import static org.junit.Assert.*;
/**
 * Created by jrobins on 2/13/15.
 */
public class TestParseEnrollment {
    private String json;

    @Before
    public void setUp() {
        json = "{\"member\":{\"firstName\":\"John\"},\"classId\":\"123\",\"enrollment\":{\"classes\":[\"abc\"],\"alias\":\"test\",\"productConfigs\":[\"config1\",\"config2\"],\"memberId\":\"mem1\"}}";
    }

    @Test
    public void testParse() {
        try {
            JSONObject jo = Utils.parseEnrollment(json);
            JSONArray productConfigs = jo.getJSONArray("productConfigs");
            HashSet<String> prodConfigs = new HashSet<String>();
            for (int i=0; i<productConfigs.length(); i++) {
                prodConfigs.add(productConfigs.getString(i));
            }
            assertTrue(prodConfigs.contains("config1"));
            assertTrue(prodConfigs.contains("config2"));

            JSONArray classes = jo.getJSONArray("classes");
            HashSet<String> cls = new HashSet<String>();
            for (int i=0; i<classes.length(); i++) {
                cls.add(classes.getString(i));
            }
            assertTrue(cls.contains("abc"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
