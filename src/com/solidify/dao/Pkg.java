package com.solidify.dao;

import com.solidify.admin.reports.Utils;
import com.solidify.exceptions.MissingProperty;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by jrobins on 2/5/15.
 */
public class Pkg {
    private int packageId;
    private Group group;
    private Date enrollStart;
    private Date enrollEnd;
    private String situsState;

    public Pkg(Group group, String enrollStartStr, String enrollEndStr, String situsState) {
        this.packageId = -1;
        this.group = group;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Date start = null;
        Date end = null;
        try {
            start = df.parse(enrollStartStr);
        } catch (Exception e) {}
        try {
            end = df.parse(enrollEndStr);
        } catch (Exception e) {}
        this.enrollStart = start;
        this.enrollEnd = end;
        this.situsState = situsState;
    }

    public int getPackageId() {
        return packageId;
    }

    public void save() throws SQLException, MissingProperty {
        if (!group.isLoaded()) {
            throw new MissingProperty("Missing groupId");
        }
        if (enrollStart == null) {
            throw new MissingProperty("Missing enrollStart date");
        }
        if (enrollEnd == null) {
            throw new MissingProperty("Missing enrollEnd date");
        }
        insert();
    }

    private void insert() throws SQLException {
        Connection con = null;
        try {
            con = Utils.getConnection();
            String sql = "INSERT INTO FE.Packages (groupId,enrollStart,enrollEnd,situsState) VALUES (?,?,?,?)";
            PreparedStatement insert = con.prepareStatement(sql);
            insert.setInt(1, group.getGroupId());
            java.sql.Date start = new java.sql.Date(enrollStart.getTime());
            java.sql.Date end = new java.sql.Date(enrollEnd.getTime());
            insert.setDate(2, start);
            insert.setDate(3, end);
            insert.setString(4, situsState);
            insert.executeUpdate();
            ResultSet rs = insert.getGeneratedKeys();
            if (rs.next()) {
                this.packageId = rs.getInt(1);
            }
            insert.close();
            rs.close();
        } finally {
            if (con != null) con.close();
        }
    }

    public boolean isLoaded() {
        return packageId > -1 ? true : false;
    }
}
