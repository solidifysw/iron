package com.solidify.tests;


import com.solidify.admin.reports.Utils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
/**
 * Created by jrobins on 2/24/15.
 */
public class TestDumpOrderBlob extends BaseTest {

    @Test
    public void testDumpOrderBlob() {
        String orderId = "d03701ae-58de-496e-bc2c-44c7581926fd";
        try {
            byte[] blob = Utils.getOrderBlob(orderId, con);
            String blobStr = new String(blob,"UTF-8");
            JSONObject slimOrder = Utils.buildObject(blob, con);
            if (slimOrder != null) {
                // find the classes
                JSONObject enrollment = Utils.parseEnrollment(blobStr);
                JSONArray classes = enrollment.getJSONArray("classes");
                String cls = Utils.getClassVal(classes,slimOrder.getString("memberId"), con);
                if (cls != null) {
                    slimOrder.put("class",cls);
                }
            }
            System.out.println(slimOrder);
            assertEquals("1",slimOrder.getString("class"));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                con.close();
            } catch (SQLException e) {}
        }
    }
}
