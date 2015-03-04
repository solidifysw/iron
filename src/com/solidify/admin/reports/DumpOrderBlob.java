package com.solidify.admin.reports;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.sql.*;
import java.util.HashSet;
import java.util.Properties;

import com.solidify.utils.ParsedObject;

public class DumpOrderBlob implements Runnable {
    //private static final Logger log = LogManager.getLogger();
    private String orderId;
    private Connection con;

    public DumpOrderBlob(String orderId, Connection con) {
        this.orderId = orderId;
        this.con = con;
    }

    public static void main(String[] args) {
        Connection con = null;
        String orderId = "d03701ae-58de-496e-bc2c-44c7581926fd";
        Properties connectionProps = new Properties();
        connectionProps.put("user","root");
        connectionProps.put("password", "letmein1");
        if (args != null && args.length > 0) {
            orderId = args[0];
        }
        try {
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/FE", connectionProps);
            DumpOrderBlob blob = new DumpOrderBlob(orderId, con);
            blob.run();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (con != null) try {
                con.close();
            } catch (SQLException e) {}
        }
    }

    public void run() {
        try {
            String json = getSourceJson();
            System.out.println(json);
            JSONObject reducedJson = getReducedJson(json);
            System.out.println(reducedJson.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getSourceJson() throws SQLException {
        PreparedStatement select = null;
        ResultSet rs = null;
        String json = null;
        String sql = "SELECT data FROM sinc.orders WHERE id = ?";
        select = con.prepareStatement(sql);
        select.setString(1, orderId);
        rs = select.executeQuery();
        if (rs.next()) {
            json = rs.getString("data");
        }
        rs.close();
        select.close();
        return json;
    }

    protected JSONObject getReducedJson(String json) {
        HashSet<String> skip = new HashSet<>();
        skip.add("member");
        skip.add("enrollment");
        skip.add("keepCoverage");
        skip.add("prePostTaxSelections");
        skip.add("imported");
        skip.add("current");
        skip.add("data.signature");
        skip.add("data.member.tags");
        skip.add("data.member.rehireEligible");
        skip.add("data.member.terminationNotes");
        skip.add("data.member.payrollId");
        skip.add("data.member.carrierData");
        skip.add("data.member.personal.dependents");
        skip.add("data.member.personal.emergencyContacts");
        skip.add("data.member.personal.beneficiaries");
        ParsedObject po = new ParsedObject(json,skip,ParsedObject.SKIP);
        JSONObject jo = po.get();
        return jo;
    }
}
