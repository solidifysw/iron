package com.solidify.dao;

import com.solidify.admin.reports.Utils;

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
    private int groupId;
    private Date enrollStart;
    private Date enrollEnd;
    private String situsState;
    private Connection con;

    public Pkg(int groupId, String enrollStartStr, String enrollEndStr, String situsState, Connection con) {
        this.packageId = -1;
        this.groupId = groupId;
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
        this.con = con;
    }

    public int getPackageId() {
        return packageId;
    }

    public void save() throws SQLException {
        if (groupId >= 0 && enrollStart != null && enrollEnd != null) {
            String sql = "INSERT INTO FE.Packages (groupId,enrollStart,enrollEnd,situsState) VALUES (?,?,?,?)";
            PreparedStatement insert = con.prepareStatement(sql);
            insert.setInt(1,groupId);
            java.sql.Date start = new java.sql.Date(enrollStart.getTime());
            java.sql.Date end = new java.sql.Date(enrollEnd.getTime());
            insert.setDate(2,start);
            insert.setDate(3,end);
            insert.setString(4,situsState);
            insert.executeUpdate();
            ResultSet rs = insert.getGeneratedKeys();
            if (rs.next()) {
                this.packageId = rs.getInt(1);
            }
        }
    }
}
