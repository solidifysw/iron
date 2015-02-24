package com.solidify.tests;

import com.solidify.dao.SincOrders;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
/**
 * Created by jrobins on 2/24/15.
 */
public class TestSincOrders extends BaseTest {
    @Test
    public void testSincOrders() {
        try {
            SincOrders so = new SincOrders("fefc6deb-9c08-47c3-b132-e93a1c9e9554",con);
            for (JSONObject order : so.getOrders()) {
                if (order.getString("orderId").equals("d03701ae-58de-496e-bc2c-44c7581926fd")) {
                    assertEquals("7b46d85d-0a82-4383-a83b-b51fe4632010",order.getString("memberId"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
