package com.solidify.admin.reports.tests;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.solidify.admin.reports.Utils;
import junit.framework.TestCase;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

public class UtilsTestParseJson extends TestCase {
    private JSONObject obj = null;

    @Before
    public void setUp() {
        try {
            JsonFactory f = new JsonFactory();
            //JsonParser jp = f.createParser(new File("/Users/jennifermac/Workspaces/eclipse/iron/src/com/solidify/admin/reports/tests/sample.json"));
            JsonParser jp = f.createParser(new File("/Users/jrobins/Documents/intellij/iron/tests/com/solidify/admin/reports/tests/sample.json"));
            obj = new JSONObject();
            obj.put("groupName","");
            //obj = Utils.buildObject(obj,jp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testParse() {
        assertNotNull(obj.getString("firstName"));
        assertEquals("abc",obj.getString("memberId"));
        assertEquals("abc",obj.getString("occupation"));
    }

    @Test public void testFirstName() {
        assertEquals("Zachary",obj.getString("firstName"));
    }


}