package com.solidify.tests;

import com.solidify.dao.SincOrder;
import org.json.JSONObject;
import org.junit.Test;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by jr1 on 3/13/15.
 */
public class TestSincOrder extends BaseTest {

    @Test
    public void testSincOrder() {
        SincOrder so = null;
        try {
            so = new SincOrder("d03701ae-58de-496e-bc2c-44c7581926fd", con);

            // these 2 are from assurant live
            //so = new SincOrder("c754e6b3-d8ea-4b88-9805-e23fd3a1cc59", con);
            //so = new SincOrder("aa72989c-c12a-48ee-bcf2-dbf37d7a266e", true, con);

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                con.close();
            } catch (SQLException e) {}
        }
        JSONObject order = so.getOrder();
        System.out.println(order.toString());
        JSONObject data = order.getJSONObject("data");
        JSONObject member = data.getJSONObject("member");
        //assertEquals("7b46d85d-0a82-4383-a83b-b51fe4632010",member.getString("id"));
        assertEquals("8cc30962-bab0-4844-9677-7e5f63ca39fc",member.getString("id"));
    }

    /*
    // use this code to force an individual order back into a batch
    @Test
    public void testUpdateBatchFlag() {
        try {
            SincOrder.updateBatchFlag("aa72989c-c12a-48ee-bcf2-dbf37d7a266e",con);
            SincOrder so = new SincOrder("aa72989c-c12a-48ee-bcf2-dbf37d7a266e", con);
            JSONObject order = so.getOrder();
            JSONObject data = order.getJSONObject("data");
            JSONObject member = data.getJSONObject("member");
            assertEquals("8cc30962-bab0-4844-9677-7e5f63ca39fc",member.getString("id"));
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                con.close();
            } catch (SQLException e) {}
        }
    }
    */
}
