package com.solidify.dao;

import com.solidify.admin.reports.Utils;
import com.solidify.exceptions.MissingProperty;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by jrobins on 2/5/15.
 */
public class Pkg {
    private int packageId;
    private Group group;
    private String situsState;
    private int deductionsPerYear;
    private String login1;
    private String login1Label;
    private String login2;
    private String login2Label;
    private String password;
    private ArrayList<EnrollmentDates> enrollmentDates;
    private Connection con;
    private boolean manageConnection;

    public Pkg(Group group, String situsState, int deductionsPerYear, String login1, String login1Label, String login2, String login2Label, String password, Connection con) {
        this.packageId = -1;
        this.group = group;
        this.situsState = situsState;
        this.deductionsPerYear = deductionsPerYear;
        this.login1 = login1;
        this.login1Label = login1Label;
        this.login2 = login2;
        this.login2Label = login2Label;
        this.password = password;
        this.enrollmentDates = new ArrayList<>();
        this.con = con;
        this.manageConnection = con == null ? true : false;
    }

    public int getPackageId() {
        return packageId;
    }

    public void addEnrollmentDates(EnrollmentDates eDates) {
        enrollmentDates.add(eDates);
    }

    public void save() throws SQLException, MissingProperty {
        if (!group.isLoaded()) {
            throw new MissingProperty("Missing groupId");
        }
        insert();
    }

    private void insert() throws SQLException, MissingProperty {
        try {
            if (manageConnection) {
                con = Utils.getConnection();
            }
            String sql = "INSERT INTO FE.Packages (groupId, situsState, deductionsPerYear, login1, login1Label, login2, login2Label, password) VALUES (?,?,?,?,?,?,?,?)";
            PreparedStatement insert = con.prepareStatement(sql);
            insert.setInt(1, group.getGroupId());
            insert.setString(2, situsState);
            insert.setInt(3,deductionsPerYear);
            insert.setString(4,login1);
            insert.setString(5,login1Label);
            insert.setString(6,login2);
            insert.setString(7,login2Label);
            insert.setString(8,password);
            insert.executeUpdate();
            ResultSet rs = insert.getGeneratedKeys();
            if (rs.next()) {
                this.packageId = rs.getInt(1);
            }
            insert.close();
            rs.close();
            if (!enrollmentDates.isEmpty()) {
                for (EnrollmentDates eDates : enrollmentDates) {
                    eDates.setPackageId(packageId);
                    eDates.save();
                }
            }
        } finally {
            if (manageConnection && con != null) con.close();
        }
    }

    public boolean isLoaded() {
        return packageId > -1 ? true : false;
    }
}
