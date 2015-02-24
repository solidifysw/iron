package com.solidify.tests;

import com.solidify.utils.MoveOrders;
import org.junit.Test;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by jrobins on 2/24/15.
 */
public class TestDataTransfer extends BaseTest {

    @Test
    public void testDataTransfer() {
        try {
            MoveOrders.run(con);
            String sql = "SELECT name, active, alias FROM FE.groups";
            PreparedStatement select = con.prepareStatement(sql);
            ResultSet rs = select.executeQuery();
            assertTrue(rs.next());
            assertEquals("Export Test",rs.getString("name"));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (con != null) try {
                con.close();
            } catch (SQLException e) {}
        }

    }
}
