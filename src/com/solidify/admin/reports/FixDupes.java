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
 * This puppy searches for the rows in the coverages table per order looking for duplicate coverage lines.
 * The code queries the coverages table filtering on groupId, excluding imported coverages, and orders by orderId.
 * It loops through the results and keeps track of the orderId so it can compare just the rows for each orderId.
 * It builds a HasMap of the coverage lines for each group with the productId being the key and a JSONObject of the data blob for this row.
 * When looking at each row, it skips rows where the benefit is declined, and checks to see if 2 rows with the same productId are equal.
 * For some products, it has to check to see if the subTypes are equal as well, like with vol life where you can get coverage rows
 * for EE, SP and CH.
 */
public class FixDupes implements Runnable {
    private static final Logger log = LogManager.getLogger();
    private static String groupId;

    public FixDupes(String groupId) {
        this.groupId = groupId;
    }

    @Override
    public void run() {
        log.info("FixDupes thread started.");
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

                if (!benefit.equals("Decline")) { // skip if the declined
                    if (covs.containsKey(productId)) {
                        JSONObject covInMap = covs.get(productId);
                        String benefitFromQuery = covFromQuery.getString("benefit");
                        String benefitInMap = covInMap.getString("benefit");
                        if (benefitFromQuery.equals(benefitInMap)) { // if the benefits are equal, check the subTypes
                            String subTypeFromMap = covInMap.getString("subType");
                            String subTypeInQuery = covFromQuery.getString("subType");
                            if ((subTypeFromMap != null && subTypeInQuery != null) && ((!subTypeFromMap.equals("") && !subTypeInQuery.equals("") && subTypeFromMap.equals(subTypeInQuery)) || (subTypeFromMap.equals("") && subTypeInQuery.equals("")))) {
                                System.out.println("Dupe: " + rs.getString("orderId")+ " "+productId);
                                sql = "UPDATE sinc.coverages SET deleted = 1 WHERE id = ?";
                                PreparedStatement update = con.prepareStatement(sql);
                                update.setString(1,covFromQuery.getString("id"));
                                update.executeUpdate();
                                update.close();
                                System.out.println(covInMap.toString());
                                System.out.println(covFromQuery.toString());
                            }
                        }
                    } else {
                        covs.put(productId, covFromQuery);
                    }
                }
            }
            select.close();
            rs.close();
            log.info("FixDupes thread has finished");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                con.close();
            } catch (Exception e) {}
        }
    }
}
