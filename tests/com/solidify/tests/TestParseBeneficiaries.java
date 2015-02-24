package com.solidify.tests;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.solidify.admin.reports.Utils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by jrobins on 2/23/15.
 */
public class TestParseBeneficiaries {

    @Test
    public void testParseBeneficiaries() {
        JsonFactory factory = new JsonFactory();

        JsonToken current = null;
        JSONObject obj = new JSONObject();
        JSONArray bens = new JSONArray();
        JSONObject ben = new JSONObject();
        ben.put("firstName","Jane");
        ben.put("lastName","Doe");
        ben.put("relationship","SPOUSE");
        ben.put("id","123");
        ben.put("zip","");
        ben.put("address1","123 Elm St.");
        ben.put("city","Fairfax");
        ben.put("state", "VA");

        bens.put(ben);
        obj.put("beneficiaries",bens);

        try {
            JsonParser jp = factory.createParser(obj.toString());
            current = jp.nextToken(); // {
            current = jp.nextToken(); // dependents

            JSONObject jo = new JSONObject();
            Utils.buildBeneficiaries(jo, jp);
            assertTrue(jo.has("beneficiaries"));
            JSONArray ja = jo.getJSONArray("beneficiaries");
            assertEquals(1, ja.length());
            ben = ja.getJSONObject(0);
            assertEquals("Doe", ben.getString("lastName"));
            assertEquals("Jane",ben.getString("firstName"));
            assertEquals("SPOUSE",ben.getString("relationship"));
            assertEquals("123 Elm St.",ben.getString("address1"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
