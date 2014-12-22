package com.solidify.admin.reports;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
            bw.write("\"Group\",\"Carrier\",\"Underwriter\"");
            bw.newLine();

            String sql = "SELECT groups.name, productConfigurations.data FROM sinc.groups, sinc.productConfigurations " +
                    "WHERE productConfigurations.groupId = groups.id AND productConfigurations.data " +
                    "LIKE '%\"type\":\"MEDICAL\"%';";
            select = con.prepareStatement(sql);
            rs = select.executeQuery();
            HashMap<String,HashSet> rows = new HashMap<String,HashSet>();
            HashSet<String> carriers;
            while(rs.next()) {
                String groupName = rs.getString("name");
                if (!rows.containsKey(groupName)) {
                    carriers = new HashSet<String>();
                } else {
                    carriers = rows.get(groupName);
                }
                JSONObject data = new JSONObject(new JSONTokener(rs.getString("data")));
                JSONObject configuration = data.getJSONObject("configuration");
                JSONObject companies = configuration.getJSONObject("companies");
                String carrier = companies.getString("carrier");
                String underwriter = companies.getString("underwriter");
                if (!carriers.contains(carrier)) {
                    carriers.add(carrier);
                    if (rows.containsKey(groupName)) {
                        rows.remove(groupName);
                    }
                    rows.put(groupName,carriers);
                    log.info(groupName+" "+carrier+" "+underwriter);
                    bw.write("\""+groupName+"\",\""+carrier+"\",\""+underwriter+"\"");
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
