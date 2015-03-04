package com.solidify.dao;

import com.solidify.admin.reports.Utils;
import com.solidify.exceptions.MissingProperty;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by jrobins on 3/4/15.
 */
public class EnrollmentDates {
    private int enrollmentDateid;
    private int packageId;
    private Date cutOff;
    private Date start;
    private Date end;
    private int isOpen;
    private Connection con;
    private boolean manageConnection;

    public EnrollmentDates(int enrollmentDateid, int packageId, String cutOff, String start, String end, int isOpen, Connection con) {
        this.enrollmentDateid = enrollmentDateid;
        this.packageId = packageId;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        try {
            this.cutOff = df.parse(cutOff);
        } catch (ParseException e) {
            this.cutOff = null;
        }
        try {
            this.start = df.parse(start);
        } catch (ParseException e) {
            this.start = null;
        }
        try {
            this.end = df.parse(end);
        } catch (ParseException e) {
            this.end = null;
        }
        this.isOpen = isOpen;
        this.con = con;
        this.manageConnection = con == null ? true : false;
    }

    public EnrollmentDates(String cutOff, String start, String end, int isOpen, Connection con) {
        this(-1, -1, cutOff, start, end, isOpen, con);
    }

    public EnrollmentDates(String cutOff, String start, String end, Connection con) {
        this(-1, -1, cutOff, start, end, 0, con);
    }

    public void save() throws SQLException, MissingProperty {
        if (packageId < 0) {
            throw new MissingProperty("Missing packageId");
        }
        if (cutOff == null) {
            throw new MissingProperty("Missing cutOff - the date EE's date of hire must be less than to enroll in this period.");
        }
        if (start == null) {
            throw new MissingProperty("Missing start");
        }
        if (end == null) {
            throw new MissingProperty("Missing end");
        }
        if (enrollmentDateid < 0) {
            insert();
        }
    }

    private void insert() throws SQLException {
        try {
            if (manageConnection) {
                con = Utils.getConnection();
            }
            String sql = "INSERT INTO FE.EnrollmentDates (packageId, cutOff, start, end, isOpen) VALUES (?,?,?,?,?)";
            PreparedStatement insert = con.prepareStatement(sql);
            java.sql.Date cutOffSql = new java.sql.Date(cutOff.getTime());
            java.sql.Date startSql = new java.sql.Date(start.getTime());
            java.sql.Date endSql = new java.sql.Date(end.getTime());
            insert.setInt(1, packageId);
            insert.setDate(2, cutOffSql);
            insert.setDate(3, startSql);
            insert.setDate(4, endSql);
            insert.setInt(5, isOpen);
            insert.executeUpdate();
            ResultSet rs = insert.getGeneratedKeys();
            if (rs.next()) {
                this.enrollmentDateid = rs.getInt(1);
            }
            insert.close();
            rs.close();
        } finally {
            if (manageConnection && con != null) con.close();
        }
    }

    public void setPackageId(int packageId) {
        this.packageId = packageId;
    }
}
