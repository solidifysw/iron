package com.solidify.tests;

import com.solidify.utils.ParsedObject;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.util.HashSet;

import static org.junit.Assert.assertTrue;

/**
 * Created by jrobins on 2/24/15.
 */
public class TestParsedObject {
    @Test
    public void testParsedObject() {
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

        HashSet<String> skips = new HashSet();
        skips.add("data.member.personal");
        skips.add("enrollment");
        skips.add("member");
        ParsedObject po = new ParsedObject(obj.toString(), skips);
        JSONObject testObj = po.get();
        System.out.println(testObj.toString());
        assertTrue(obj.has("member"));
    }
}
