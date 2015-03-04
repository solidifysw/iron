package com.solidify.tests;

import com.solidify.utils.Include;
import com.solidify.utils.ParsedObject;
import com.solidify.utils.Skip;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by jrobins on 2/24/15.
 */
public class TestParsedObject extends BaseTest {
    @Test
    public void testParsedObjectWithSkips() {
        long num = 123456789123456789l;

       /* JSONObject jo = new JSONObject();
        jo.put("a",1);
        jo.put("b",2.35);
        jo.put("ab",true);
        jo.put("de",false);
        jo.put("ge",num);
        JSONObject jo2 = new JSONObject();
        jo2.put("c","3");
        JSONObject jo3 = new JSONObject();
        jo3.put("d","4");
        jo2.put("y",jo3);
        jo.put("x",jo2);
        JSONArray ja = new JSONArray();
        ja.put("entry1");
        ja.put("entry2");
        jo.put("array",ja);
        ParsedObject po = new ParsedObject(jo.toString());
        JSONObject obj = po.get();
        */

        /*
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
        System.out.println(obj.toString());
        */

        PreparedStatement select = null;
        ResultSet rs = null;
        String json = null;
        String sql = "SELECT data FROM sinc.orders WHERE id = ?";
        try {
            select = con.prepareStatement(sql);
            select.setString(1, "d03701ae-58de-496e-bc2c-44c7581926fd");
            rs = select.executeQuery();
            if (rs.next()) {
                json = rs.getString("data");
                //System.out.println(json);
            }
            rs.close();
            select.close();
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        HashSet<String> skips = new HashSet();
        skips.add("enrollment");
        skips.add("member");
        skips.add("data.signature");
        skips.add("attended");
        skips.add("prePostTaxSelections");
        skips.add("imported");
        skips.add("current");
        skips.add("data.member.personal.dependents");
        skips.add("data.member.personal.emergencyContacts");
        skips.add("data.member.personal.beneficiaries");

        ParsedObject po = new ParsedObject(json, skips, new Skip());
        JSONObject testObj = po.get();
        //System.out.println(testObj.toString());
        assertFalse(testObj.has("member"));
        assertFalse(testObj.has("enrollment"));
        assertFalse(testObj.has("attended"));
        assertFalse(testObj.has("imported"));

        JSONObject data = testObj.getJSONObject("data");
        JSONObject member = data.getJSONObject("member");
        JSONObject personal = member.getJSONObject("personal");
        assertFalse(personal.has("dependents"));
        assertFalse(personal.has("emergencyContacts"));
        assertFalse(personal.has("beneficiaries"));
    }

    @Test
    public void testParseObjectsWithIncludes() {
        PreparedStatement select = null;
        ResultSet rs = null;
        String json = null;
        String sql = "SELECT data FROM sinc.orders WHERE id = ?";
        try {
            select = con.prepareStatement(sql);
            select.setString(1, "d03701ae-58de-496e-bc2c-44c7581926fd");
            rs = select.executeQuery();
            if (rs.next()) {
                json = rs.getString("data");
                //System.out.println(json);
            }
            rs.close();
            select.close();
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        HashSet<String> incs = new HashSet<>();
        incs.add("data.member.personal");
        incs.add("enrollment");
        ParsedObject po = new ParsedObject(json,incs,new Include());
        JSONObject testObj = po.get();
        //System.out.println(testObj.toString());
        assertTrue(testObj.has("enrollment"));
        assertTrue(testObj.has("data"));

        JSONObject data = testObj.getJSONObject("data");
        assertTrue(data.has("member"));

        JSONObject member = data.getJSONObject("member");
        assertFalse(member.has("id"));

        JSONObject personal = member.getJSONObject("personal");
        assertTrue(personal.has("firstName"));

    }

    @Test
    public void testParsedSignature() {
        PreparedStatement select = null;
        ResultSet rs = null;
        String json = null;
        String sql = "SELECT data FROM sinc.orders WHERE id = ?";
        try {
            select = con.prepareStatement(sql);
            select.setString(1, "d03701ae-58de-496e-bc2c-44c7581926fd");
            rs = select.executeQuery();
            if (rs.next()) {
                json = rs.getString("data");
                //System.out.println(json);
            }
            rs.close();
            select.close();
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        HashSet<String> incs = new HashSet<>();
        incs.add("data.signature");

        ParsedObject po = new ParsedObject(json,incs, new Include());
        JSONObject obj = po.get();
        //System.out.println(obj.toString());
        assertTrue(obj.has("data"));
        JSONObject data = obj.getJSONObject("data");
        assertTrue(data.has("signature"));
    }
}
