package com.solidify.admin.reports;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

public class DumpBlob implements Runnable {
    private static final Logger log = LogManager.getLogger();
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
        log.info("DumpBlob thread started.");
       // JSONObject obj = Utils.dumpOrderBlob(orderId,includeSourceBlob);
        log.info("DumpBlob thread finished.");
    }
}
