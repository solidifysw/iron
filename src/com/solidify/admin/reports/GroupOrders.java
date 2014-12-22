package com.solidify.admin.reports;

import org.json.JSONObject;

import java.util.Collection;

public abstract class GroupOrders implements Runnable {
    protected final String groupId;
    protected final String path;
    protected Collection<JSONObject> orders;

    public GroupOrders(String groupId, String path) {
        this.groupId = groupId;
        this.path = path;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getPath() {
        return path;
    }

    public Collection<JSONObject> getOrders() {
        return orders;
    }
}
