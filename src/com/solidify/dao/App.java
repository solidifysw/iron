package com.solidify.dao;

import com.solidify.admin.reports.Utils;
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
    private Group group;
    private String sincOrderId;
    private Date dateSaved;
    private String enroller;
    private int appSourceId;
    private Connection con;
    private boolean manageConnection;

    public App(int appId, Group group, String sincOrderId, Date dateSaved, String enroller, int appSourceId, Connection con) {
        this.appId = appId;
        this.group = group;
        this.sincOrderId = sincOrderId;
        this.dateSaved = dateSaved;
        this.enroller = enroller;
        this.appSourceId = appSourceId;
        this.con = con;
        this.manageConnection = con == null ? true : false;
    }

    public App(Group group, String sincOrderId, Date dateSaved, String enroller, int appSourceId, Connection con) {
        this(-1,group,sincOrderId,dateSaved,enroller,appSourceId, con);
    }

    public App(Group group, Date dateSaved, String enroller, int appSourceId, Connection con) {
        this(-1,group,null,dateSaved,enroller,appSourceId,con);
    }

    public int getAppId() {
        return appId;
    }

    public void save() throws MissingProperty, SQLException {
        if (!group.isLoaded()) {
           throw new MissingProperty("group is not loaded");
        }
        if (sincOrderId == null || "".equals(sincOrderId)) {
            throw new MissingProperty("missing the sinc orderId");
        }
        insert();
    }

    private void insert() throws SQLException {
        try {
            if (manageConnection) con = Utils.getConnection();
            String sql = "INSERT INTO FE.apps (groupId,orderId,appSourceId,enroller,dateSaved) VALUES (?,?,?,?,?)";
            PreparedStatement insert = con.prepareStatement(sql);
            insert.setInt(1, group.getGroupId());
            insert.setString(2, sincOrderId);
            insert.setInt(3, appSourceId);
            insert.setString(4,enroller);
            if (dateSaved != null) {
                insert.setDate(5, new java.sql.Date(dateSaved.getTime()));
            } else {
                insert.setDate(5,null);
            }
            insert.executeUpdate();
            ResultSet rs = insert.getGeneratedKeys();
            if (rs.next()) {
                this.appId = rs.getInt(1);
            }
            insert.close();
            rs.close();
        } finally {
            if (manageConnection && con != null) con.close();
        }
    }

    public boolean isLoaded() {
        return appId > -1 ? true : false;
    }
}
