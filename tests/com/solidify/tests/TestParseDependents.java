package com.solidify.tests;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.solidify.admin.reports.Utils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by jrobins on 2/23/15.
 */
public class TestParseDependents {

    @Test
    public void testParseDependents() {
        JsonFactory factory = new JsonFactory();

        JsonToken current = null;
        JSONObject tmp = new JSONObject();
        JSONArray tmpA = new JSONArray();
        JSONObject tmpDep = new JSONObject();
        tmpDep.put("firstName","Jane");
        tmpDep.put("lastName","Doe");
        tmpDep.put("relationship","SPOUSE");
        tmpA.put(tmpDep);
        tmp.put("dependents",tmpA);

        try {
            JsonParser jp = factory.createParser(tmp.toString());
            current = jp.nextToken(); // {
            current = jp.nextToken(); // dependents

            JSONObject jo = new JSONObject();
            Utils.buildDependents(jo, jp);
            assertTrue(jo.has("dependents"));
            JSONArray ja = jo.getJSONArray("dependents");
            assertEquals(1, ja.length());
            JSONObject dep = ja.getJSONObject(0);
            assertEquals("Doe", dep.getString("lastName"));
            assertEquals("Jane",dep.getString("firstName"));
            assertEquals("SPOUSE",dep.getString("relationship"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
