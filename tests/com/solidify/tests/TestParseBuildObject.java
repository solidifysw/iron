package com.solidify.tests;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.solidify.admin.reports.Utils;
import com.solidify.dao.SincOrders;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by jrobins on 2/23/15.
 */
public class TestParseBuildObject extends BaseTest {

    @Test
    public void testBuildObject() {
        JsonFactory factory = new JsonFactory();

        JsonToken current = null;
        JSONObject obj = new JSONObject();
        JSONObject emptyObject = new JSONObject();
        obj.put("member",emptyObject);
        obj.put("declineReasons",emptyObject);
        obj.put("attended",true);
        obj.put("classId","12345");
        obj.put("disclosureQuestions", emptyObject);
        obj.put("id","abcdef");

        JSONObject data = new JSONObject();
        JSONObject member = new JSONObject();
        member.put("id","abc123");
        JSONObject personal = new JSONObject();
        personal.put("firstName","John");
        member.put("personal",personal);
        data.put("member",member);

        JSONObject cov = new JSONObject();
        cov.put("planName","test");
        data.put("0ee34f3e=05ac",cov);

        JSONArray array = new JSONArray();
        JSONObject sp = new JSONObject();
        sp.put("firstName","Jane");
        sp.put("lastName","Doe");
        sp.put("relationship","SPOUSE");
        sp.put("id","123");
        sp.put("zip","");
        sp.put("address1","123 Elm St.");
        sp.put("city","Fairfax");
        sp.put("state", "VA");

        array.put(sp);
        data.put("beneficiaries",array);
        data.put("dependents",array);

        obj.put("data",data);
        obj.put("enrollment",emptyObject);

        try {
            JSONObject jo = SincOrders.buildObject(obj.toString().getBytes());
            //System.out.println(jo.toString());
            assertTrue(jo.has("beneficiaries"));
            JSONArray ja = jo.getJSONArray("beneficiaries");
            assertEquals(1, ja.length());
            JSONObject ben = ja.getJSONObject(0);
            assertEquals("Doe", ben.getString("lastName"));
            assertEquals("Jane",ben.getString("firstName"));
            assertEquals("SPOUSE",ben.getString("relationship"));
            assertEquals("123 Elm St.",ben.getString("address1"));
            assertEquals("abcdef",jo.getString("orderId"));
            assertTrue(jo.has("memberId"));
            assertEquals("abc123",jo.getString("memberId"));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                con.close();
            } catch (SQLException e) {}
        }
    }
}
