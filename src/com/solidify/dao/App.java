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
    private int appSourceId;

    public App(Group group, String sincOrderId, int appSourceId) {
        this.appId = -1;
        this.group = group;
        this.sincOrderId = sincOrderId;
        this.appSourceId = appSourceId;
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
        Connection con = null;
        try {
            con = Utils.getConnection();
            String sql = "INSERT INTO FE.apps (groupId,orderId,appSourceId) VALUES (?,?,?)";
            PreparedStatement insert = con.prepareStatement(sql);
            insert.setInt(1, group.getGroupId());
            insert.setString(2, sincOrderId);
            insert.setInt(3, appSourceId);
            insert.executeUpdate();
            ResultSet rs = insert.getGeneratedKeys();
            if (rs.next()) {
                this.appId = rs.getInt(1);
            }
        } finally {
            if (con != null) con.close();
        }
    }

    public boolean isLoaded() {
        return appId > -1 ? true : false;
    }
}
