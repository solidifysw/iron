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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by jennifermac on 12/22/14.
 */
public class MedicalEnrolled implements Runnable {
    private static final Logger log = LogManager.getLogger();
    BufferedWriter bw = null;

    @Override
    public void run() {
        log.info("Medical Enrolled Thread Started.");
        Connection con = null;
        PreparedStatement select = null;
        ResultSet rs = null;
        try {
            con = Utils.getConnection();
            File out = new File("/tmp/medicalCarriersEnrolled.csv");
            bw = new BufferedWriter(new FileWriter(out));
            bw.write("\"Group\",\"Carrier\",\"Underwriter\",\"Total Yearly Prem\"");
            bw.newLine();

            String sql = "SELECT groups.id AS groupId, groups.name, productConfigurations.id AS configurationId, productConfigurations.data FROM sinc.groups, sinc.productConfigurations " +
                    "WHERE productConfigurations.groupId = groups.id AND productConfigurations.data " +
                    "LIKE '%\"type\":\"MEDICAL\"%' ORDER BY groups.name ASC;";
            select = con.prepareStatement(sql);
            rs = select.executeQuery();
            HashMap<String,HashSet> rows = new HashMap<String,HashSet>();
            HashSet<String> carriers;
            String currentId = "";
            Collection<JSONObject> orders = null;
            while(rs.next()) {
                String groupName = rs.getString("name");
                String groupId = rs.getString("groupId");
                String configurationId = rs.getString("configurationId");
                if (!currentId.equals(groupId)) {
                    currentId = groupId;
                    orders = Utils.getLatestOrdersForGroup(groupId,con);
                }
                if (!rows.containsKey(groupName)) {
                    carriers = new HashSet<String>();
                } else {
                    carriers = rows.get(groupName);
                }
                JSONObject data = new JSONObject(new JSONTokener(rs.getString("data")));
                JSONObject configuration = data.getJSONObject("configuration");
                JSONObject companies = configuration.getJSONObject("companies");
                String carrier = companies.getString("carrier");
                String underwriter = "";
                try {
                    underwriter = companies.getString("underwriter");
                } catch (Exception e){}

                if (!carriers.contains(carrier)) {
                    carriers.add(carrier);
                    if (rows.containsKey(groupName)) {
                        rows.remove(groupName);
                    }
                    rows.put(groupName,carriers);
                    // Get number of lives

                    // Sum up premium
                    float sum = 0f;
                    DecimalFormat df = new DecimalFormat("#,###,###.00");
                    DecimalFormat money = new DecimalFormat("$#,###,###.00");
                    for (JSONObject order : orders) {
                        JSONArray covs = order.getJSONArray("covs");
                        for (int i=0; i<covs.length(); i++) {
                            JSONObject cov = (JSONObject) covs.get(i);
                            String splitId = cov.getString("splitId");
                            String benefit = cov.getString("benefit");
                            if (splitId.equals(configurationId) && !"DECLINED".equals(benefit)) {
                                String totalYearly = cov.getString("totalYearly");
                                sum += df.parse(totalYearly).floatValue();
                            }
                        }
                    }
                    log.info(groupName+" "+carrier+" "+underwriter+" "+money.format(sum));
                    bw.write("\""+groupName+"\",\""+carrier+"\",\""+underwriter+"\",\""+money.format(sum)+"\"");
                    bw.newLine();
                }
            }
            rs.close();
            select.close();
            log.info("Medical Enrolled Thread Finished.");
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
