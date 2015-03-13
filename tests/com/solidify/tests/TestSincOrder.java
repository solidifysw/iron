package com.solidify.tests;

import com.solidify.dao.SincOrder;
import org.json.JSONObject;
import org.junit.Test;

import java.sql.SQLException;

import static org.junit.Assert.assertEquals;

/**
 * Created by jr1 on 3/13/15.
 */
public class TestSincOrder extends BaseTest {

    @Test
    public void testSincOrder() {
        SincOrder so = null;
        try {
            so = new SincOrder("d03701ae-58de-496e-bc2c-44c7581926fd", con);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        JSONObject order = so.getOrder();
        JSONObject data = order.getJSONObject("data");
        JSONObject member = data.getJSONObject("member");
        assertEquals("7b46d85d-0a82-4383-a83b-b51fe4632010",member.getString("id"));
    }
}
