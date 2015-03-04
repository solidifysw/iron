package com.solidify.tests;

import com.solidify.dao.SincGroups;
import org.json.JSONObject;
import org.junit.Test;

import java.sql.SQLException;
import java.util.HashSet;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by jrobins on 3/4/15.
 */
public class TestSincGroups extends BaseTest {

    @Test
    public void testSincGroups() {
        try {
            SincGroups sg = new SincGroups(con);
            HashSet<JSONObject> groups = sg.getGroups();
            for (JSONObject group : groups) {
                assertTrue(group.has("id"));
                assertTrue(group.has("name"));
                assertTrue(group.has("alias"));
                assertTrue(group.has("status"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
