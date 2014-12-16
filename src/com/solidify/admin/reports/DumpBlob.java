package com.solidify.admin.reports;

import org.json.JSONObject;

public class DumpBlob implements Runnable {

    private String orderId;
    private boolean includeSourceBlob;

    public DumpBlob(String orderId) {
        this.orderId = orderId;
        this.includeSourceBlob = false;
    }

    public DumpBlob(String orderId, boolean includeSourceBlob) {
        this.orderId = orderId;
        this.includeSourceBlob = includeSourceBlob;
    }

    public void run() {
        JSONObject obj = Utils.dumpOrderBlob(orderId,includeSourceBlob);
    }
}
