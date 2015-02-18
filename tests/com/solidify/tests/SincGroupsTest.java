package com.solidify.tests;

import com.solidify.dao.SincGroups;
import org.json.JSONObject;
import org.junit.Test;

import java.sql.SQLException;
import java.util.HashSet;

import static org.junit.Assert.*;

/**
 * Created by jrobins on 2/17/15.
 */
public class SincGroupsTest extends BaseTest {

    @Test
    public void testSincGroups() {
        try {
            SincGroups sg = new SincGroups(con);
            HashSet<JSONObject> groups = sg.getGroups();
            assertFalse(groups.isEmpty());
            assertEquals(1,groups.size());
            for (JSONObject group : groups) {
                assertEquals("et",group.getString("alias"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }
}
