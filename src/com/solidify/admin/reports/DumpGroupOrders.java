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
    private final boolean orderType;
    public static final boolean ALL_ORDERS = false;
    public static final boolean LATEST_ORDERS = true;
    private static final String delim = "|";

    public DumpGroupOrders(String groupId) {
        this.groupId = groupId;
        //this.orderType = LATEST_ORDERS;
        this.orderType = ALL_ORDERS;
    }

    public DumpGroupOrders(String groupId, boolean orderType) {
        this.groupId = groupId;
        this.orderType = orderType;
    }

    @Override
    public void run() {
        BufferedWriter bw = null;
        try {
            String groupName = "";
            groupName = Utils.getGroupName(groupId);

            File results = new File("/tmp/"+groupName+"_dumpOrders.csv");
            bw = new BufferedWriter(new FileWriter(results));
            //bw.write("\"memberId\""+delim+"\"EE Name\"\"+delim+\"\"EE dob\"\"+delim+\"\"EE SSN\"\"+delim+\"\"Date\"\"+delim+\"\"OrderId\"\"+delim+\"\"Product\"\"+delim+\"\"Plan Name\"\"+delim+\"\"Benefit\"\"+delim+\"\"Total Yearly\"");
            bw.write("memberId"+delim+"EE Name"+delim+"EE dob"+delim+"EE SSN"+delim+"Date"+delim+"OrderId"+delim+"Product"+delim+"Plan Name"+delim+"Benefit"+delim+"Total Yearly");
            bw.newLine();

            log.info("Dump Group Orders thread has started.");
            Collection<JSONObject> orders;

            if (orderType == ALL_ORDERS) {
                orders = Utils.getAllOrdersForGroup(groupId);
            } else {
                orders = Utils.getLatestOrdersForGroup(groupId);
            }

            //log.info(orders.size() + " orders found.");

            for (JSONObject order : orders) {
                String ssn = order.getString("ssn");
                String last4 = "";
                int len = ssn.length();
                if (len >= 4) {
                    last4 = ssn.substring(len-4);
                }
                if (order.has("covs")) {
                    JSONArray covs = order.getJSONArray("covs");
                    if (covs != null && covs.length() > 0) {
                        for (int i = 0; i < covs.length(); i++) {
                            JSONObject cov = (JSONObject) covs.get(i);
                            //bw.write("\"" + order.get("memberId") + "\"\"+delim+\"\"" + order.get("firstName") + " " + order.get("lastName") + "\"\"+delim+\"\"" + order.get("dateOfBirth") + "\"\"+delim+\"\"" + last4 + "\"\"+delim+\"\"" + order.get("date") + "\"\"+delim+\"" +
                                   // "\"" + order.get("orderId") + "\"\"+delim+\"\"" + cov.getString("productId") + "\"\"+delim+\"\"" + cov.getString("planName") + "\"\"+delim+\"\"" + cov.getString("benefit") + "\"\"+delim+\"\"" + cov.getString("totalYearly") + "\"");
                            bw.write(order.get("memberId")+delim+ order.get("firstName") + " " + order.get("lastName")+delim+order.get("dateOfBirth")+delim+last4+delim+order.get("date")+delim+
                                    order.get("orderId")+delim+cov.getString("productId")+delim+cov.getString("planName")+delim+cov.getString("benefit")+delim+cov.getString("totalYearly"));

                            bw.newLine();
                        }
                    }
                }
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
