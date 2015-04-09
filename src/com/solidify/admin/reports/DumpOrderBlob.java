package com.solidify.admin.reports;

import com.solidify.dao.SincOrder;
import com.solidify.utils.Skip;
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

    public DumpOrderBlob(String orderId) throws SQLException {
        this.orderId = orderId;
    }

//    public static void main(String[] args) {
//        Connection con = null;
//        String orderId = "d03701ae-58de-496e-bc2c-44c7581926fd";
//        Properties connectionProps = new Properties();
//        connectionProps.put("user","root");
//        connectionProps.put("password", "letmein1");
//        if (args != null && args.length > 0) {
//            orderId = args[0];
//        }
//        try {
//            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/FE", connectionProps);
//            DumpOrderBlob blob = new DumpOrderBlob(orderId, true, con);
//            blob.run();
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if (con != null) try {
//                con.close();
//            } catch (SQLException e) {}
//        }
//    }

    public void run() {
        Connection con = null;
        try {
            con = Utils.getConnection();
            SincOrder sincOrder = new SincOrder(orderId, true, con);
            System.out.println(sincOrder.getOrder().toString());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (con != null){
                try {
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
