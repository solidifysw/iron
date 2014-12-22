package com.solidify.admin.reports;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by jrobins on 12/18/14.
 */
public class AllOrders extends GroupOrders {

    public AllOrders(String groupId, String path) {
        super(groupId,path);
    }

    public void run() {
        orders = new ArrayList<JSONObject>();
        orders.add(new JSONObject());

    }
}
