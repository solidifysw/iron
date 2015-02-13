package com.solidify.dao;

import com.solidify.exceptions.MissingProperty;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * Created by jrobins on 2/9/15.
 */
public class App {

    private int appId;
    private int groupId;
    private String orderId;
    private Date dateSaved;
    private int appSourceId;
    private Connection con;

    public App(int groupId, String orderId, int appSourceId, Connection con) {
        this.appId = -1;
        this.groupId = groupId;
        this.orderId = orderId;
        this.appSourceId = appSourceId;
        this.con = con;
    }

    public int getAppId() {
        return appId;
    }

    public int getGroupId() {
        return groupId;
    }

    public String getOrderId() {
        return orderId;
    }

    public Date getDateSaved() {
        return dateSaved;
    }

    public int getAppSourceId() { return appSourceId; }

    public void save() throws MissingProperty, SQLException {
        String error = "";
        if (groupId < 0) {
            error += "missing groupId ";
        }
        if (orderId == null || "".equals(orderId)) {
            error += "missing orderId";
        }
        if (!"".equals(error)) {
            throw new MissingProperty(error);
        } else {
            insert();
        }
    }

    private void insert() throws SQLException {
        String sql = "INSERT INTO FE.apps (groupId,orderId,appSourceId) VALUES (?,?,?)";
        PreparedStatement insert = con.prepareStatement(sql);
        insert.setInt(1, groupId);
        insert.setString(2,orderId);
        insert.setInt(3,appSourceId);
        insert.executeUpdate();
        ResultSet rs = insert.getGeneratedKeys();
        if (rs.next()) {
            this.appId = rs.getInt(1);
        }
    }
}
