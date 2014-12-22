package com.solidify.admin.reports;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;

/**
 * Created by jrobins on 12/17/14.
 */
public class DumpGroupOrders implements Runnable {

    private static final Logger log = LogManager.getLogger();
    private final String groupId;

    public DumpGroupOrders(String groupId) {
        this.groupId = groupId;
    }

    @Override
    public void run() {
        BufferedWriter bw = null;
        try {
            String groupName = "";
            groupName = Utils.getGroupName(groupId);

            File results = new File("/tmp/"+groupName+"_dumpOrders.csv");
            bw = new BufferedWriter(new FileWriter(results));
            bw.write("\"memberId\",\"EE Name\",\"EE dob\",\"EE SSN\",\"Date\",\"OrderId\",\"All Declined\"");
            bw.newLine();

            log.info("Dump Group Orders thread has started.");
            Collection<JSONObject> orders;
            orders = Utils.getLatestOrdersForGroup(groupId);
            //orders = Utils.getAllOrdersForGroup(groupId);

            log.info(orders.size() + " orders found.");

            for (JSONObject order : orders) {
                JSONArray covs = (JSONArray) order.get("covs");
                boolean allDeclined = true;
                if (covs != null && covs.length() > 0) {
                    for (int i=0; i<covs.length(); i++) {
                        JSONObject cov = (JSONObject) covs.get(i);
                        if (!cov.get("benefit").equals("Decline")) {
                            allDeclined = false;
                            break;
                        }
                    }
                }
                String declinedEverything = allDeclined ? "YES" : "";

                bw.write("\"" + order.get("memberId") + "\",\"" + order.get("firstName") + " " + order.get("lastName") + "\",\"" + order.get("dateOfBirth") + "\",\"" + order.get("ssn") + "\",\"" + order.get("date") + "\",\"" + order.get("orderId") + "\",\"" + declinedEverything + "\"");
                bw.newLine();
                //log.info(order.toString());
            }
            log.info("Dump Group Orders thread has finished.");
        } catch (Exception e) {
            log.error("error",e);
        } finally {
            try {
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
