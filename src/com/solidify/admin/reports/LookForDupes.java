package com.solidify.admin.reports;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;

/**
 * Created by jrobins on 1/26/15.
 */
public class LookForDupes implements Runnable {
    private static final Logger log = LogManager.getLogger();
    private static String groupId;

    public LookForDupes(String groupId) {
        this.groupId = groupId;
    }

    @Override
    public void run() {
        log.info("LookForDupes thread started.");
        Connection con = null;
        try {
            con = Utils.getConnection();
            String sql = "SELECT id, memberId, productId, benefit, orderId, data FROM sinc.coverages WHERE type != 'IMPORTED' AND deleted = 0 AND groupId = ? ORDER BY orderId";
            PreparedStatement select = con.prepareStatement(sql);
            //noinspection JpaQueryApiInspection
            select.setString(1, groupId);
            ResultSet rs = select.executeQuery();
            String curOrderId = "";
            HashMap<String,JSONObject> covs = null;
            while (rs.next()) {
                String orderId = rs.getString("orderId");
                if (!curOrderId.equals(orderId)) {
                    curOrderId = orderId;
                    covs = new HashMap<String,JSONObject>();
                }
                String productId = rs.getString("productId");
                String benefit = rs.getString("benefit");
                String data = rs.getString("data");
                JSONObject covFromQuery = new JSONObject(data);

                if (!benefit.equals("Decline")) {
                    if (covs.containsKey(productId)) {
                        JSONObject covInMap = covs.get(productId);
                        String benefitFromQuery = covFromQuery.getString("benefit");
                        String benefitInMap = covInMap.getString("benefit");
                        if (benefitFromQuery.equals(benefitInMap)) {
                            String subTypeFromMap = covInMap.getString("subType");
                            String subTypeInQuery = covFromQuery.getString("subType");
                            if (subTypeFromMap != null && subTypeInQuery != null && subTypeFromMap.equals(subTypeInQuery)) {
                                System.out.println("Dupe: " + rs.getString("orderId"));
                            }
                        }
                    } else {
                        covs.put(productId, covFromQuery);
                    }
                }
            }
            log.info("LookForDupes thread has finished");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                con.close();
            } catch (Exception e) {}
        }
    }
}
