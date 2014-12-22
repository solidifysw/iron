package com.solidify.admin.reports.tests;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import com.solidify.admin.reports.AllOrders;

import java.util.Collection;

/**
 * Created by jrobins on 12/18/14.
 */
public class AllOrdersTest {

    private String groupId;
    private String path;
    private AllOrders ao;

    @Before
    public void setUp() {
        groupId = "theid";
        path = "thepath";
        ao = new AllOrders(groupId, path);
    }

    @Test public void setGroupIdAndPathTest() {
        assertEquals(groupId,ao.getGroupId());
        assertEquals(path,ao.getPath());
    }

    @Test public void getOrdersTest() {
        ao.run();
        Collection<JSONObject> orders = ao.getOrders();
        assertTrue(orders.size() > 0);
    }
}
