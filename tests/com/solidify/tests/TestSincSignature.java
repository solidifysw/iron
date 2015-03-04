package com.solidify.tests;

import com.solidify.dao.SincSignature;
import org.json.JSONObject;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.Assert.assertTrue;

/**
 * Created by jennifermac on 3/3/15.
 */
public class TestSincSignature extends BaseTest {

    @Test
    public void testSincSignature() {
        String orderId = "d03701ae-58de-496e-bc2c-44c7581926fd";
        try {
            SincSignature ss = new SincSignature(orderId,con);
            JSONObject jo = ss.getSignatureJson();
            //System.out.println(jo.toString());
            assertTrue(jo.has("data"));
            JSONObject data = jo.getJSONObject("data");
            assertTrue(data.has("signature"));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                con.close();
            } catch (SQLException e) {}
        }
    }
}
