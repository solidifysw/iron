package com.solidify.admin.reports;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

public class AncillaryEnrolled implements Runnable {
    private static final Logger log = LogManager.getLogger();
    BufferedWriter bw = null;

    @Override
    public void run() {
        log.info("Ancillary Enrolled Thread Started.");
        Connection con = null;
        PreparedStatement select = null;
        ResultSet rs = null;
        JSONObject data = null;
        HashSet<String> skip = new HashSet<String>();
        Collection<JSONObject> orders = null;
        try {
            skip.add("MEDICAL"); skip.add("FSA"); skip.add("HRA"); skip.add("HSA"); skip.add("DEFAULTGENERIC"); skip.add("DEFAULTINFORMATIONAL");
            con = Utils.getConnection();
            File out = new File("/tmp/volProdsEnrolled.csv");
            bw = new BufferedWriter(new FileWriter(out));

            bw.write("\"Group\",\"Product\",\"Carrier\",\"Underwriter\",\"Plan Name\",\"Total Yearly Prem\",\"Lives\"");
            bw.newLine();

            String sql = "SELECT groups.id AS groupId, groups.name, productConfigurations.id AS configurationId, productConfigurations.data FROM sinc.groups, sinc.productConfigurations " +
                         "WHERE productConfigurations.groupId = groups.id ORDER BY groups.name ASC";

            select = con.prepareStatement(sql);
            rs = select.executeQuery();
            String currentId = "";
            int cnt = 0;

            while(rs.next()) {
                String groupName = rs.getString("name");
                String groupId = rs.getString("groupId");

                String configurationId = rs.getString("configurationId");
                data = new JSONObject(new JSONTokener(rs.getString("data")));
                //log.info(data.toString());
                String type = data.getString("type");

               // skip medical, fsa, etc.
                if (skip.contains(type)) {
                    continue;
                }

                JSONObject configuration = data.getJSONObject("configuration");
                String displayName = configuration.getString("displayName");
                JSONObject companies = configuration.getJSONObject("companies");
                String carrier = companies.getString("carrier");

                // skip AEB products
                if (carrier.trim().equals("Assurant Employee Benefits")) {
                    continue;
                }

                // Keep track of the group.  No need to pull orders if for same group
                if (!currentId.equals(groupId)) {
                    currentId = groupId;
                    orders = Utils.getLatestOrdersForGroup(groupId,con);
                    cnt = Utils.getMemberCount(groupId,con);
                }
                String underwriter = companies.getString("underwriter");

                // Loop through all orders and their coverages looking for this productConfigurationId (splitId).
                // Total up the annual premium enrolled.
                float sum = 0f;
                DecimalFormat df = new DecimalFormat("#,###,###.00");
                DecimalFormat money = new DecimalFormat("$#,###,###.00");
                for (JSONObject order : orders) {
                    JSONArray covs = order.getJSONArray("covs");
                    for (int i=0; i<covs.length(); i++) {
                        JSONObject cov = covs.getJSONObject(i);
                        String splitId = cov.getString("splitId");
                        String benefit = cov.getString("benefit");
                        if (splitId.equals(configurationId) && !"Decline".equals(benefit)) {
                            String totalYearly = cov.getString("totalYearly");
                            sum += df.parse(totalYearly).floatValue();
                        }
                    }
                }
                log.info(groupName+" "+type+" "+carrier+" "+underwriter+" "+displayName+" "+money.format(sum)+" "+cnt);
                bw.write("\"" + groupName + "\",\""+type+"\",\"" + carrier + "\",\"" + underwriter + "\",\"" + displayName + "\",\"" + money.format(sum) + "\",\""+cnt+"\"");
                bw.newLine();
            }
            rs.close();
            select.close();
            log.info("Ancillary Enrolled Thread Finished.");
        } catch (Exception e) {
            log.error("error",e);
        } finally {
            try {
                con.close();
            } catch (Exception e) {}
            try {
                select.close();
            } catch (Exception e) {}
            try {
                rs.close();
            } catch (Exception e) {}
            try {
                bw.close();
            } catch (Exception e) {}
        }
    }
}
