package com.solidify.tests;

import com.solidify.dao.SincSignature;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by jrobins on 2/18/15.
 */
public class TestParseSignature extends BaseTest {

    @Test
    public void testParseSignature() {
        String rawJson = "{\"member\":{},\"isBatchable\":true,\"declineReasons\":{},\"data\":{\"member\":{},\"signature\":[{\"abc\":1,\"def\":2},{\"abc\":3,\"def\":4}],\"dependents\":[]}}";
        try {
            JSONObject jo = SincSignature.parseSignature(rawJson);
            assertTrue(jo.has("signature"));
            JSONArray ja = jo.getJSONArray("signature");
            JSONObject t1 = ja.getJSONObject(0);
            assertEquals(1,t1.getInt("abc"));
            assertEquals(2,t1.getInt("def"));
            JSONObject t2 = ja.getJSONObject(1);
            assertEquals(3,t2.getInt("abc"));
            assertEquals(4,t2.getInt("def"));

            SincSignature ss = new SincSignature("bb10a2ce-3ae8-40fe-a255-c40f87be1f8d",con);
            JSONObject sig = ss.getSignatureJson();
            assertTrue(sig.has("signature"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
