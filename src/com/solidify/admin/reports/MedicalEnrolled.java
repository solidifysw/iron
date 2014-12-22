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

/**
 * Created by jennifermac on 12/22/14.
 */
public class MedicalEnrolled implements Runnable {
    private static final Logger log = LogManager.getLogger();
    BufferedWriter bw = null;

    @Override
    public void run() {
        log.info("")
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
            while(rs.next()) {
                String groupName = rs.getString("name");
                JSONObject data = new JSONObject(new JSONTokener(rs.getString("data")));
                JSONObject configuration = data.getJSONObject("configuration");
                JSONObject companies = configuration.getJSONObject("companies");
                String carrier = companies.getString("carrier");
                String underwriter = companies.getString("underwriter");
                bw.write("\""+groupName+"\",\""+carrier+"\",\""+underwriter+"\"");
                bw.newLine();
            }
            rs.close();
            select.close();
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
        }
    }
}
