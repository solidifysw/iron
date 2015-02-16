package com.solidify.dao;

import com.solidify.admin.reports.Utils;
import com.solidify.exceptions.MissingProperty;
import com.solidify.exceptions.NoValue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by jrobins on 2/10/15.
 */
public class Coverage {
    private int coverageId;
    private App app;
    private Offer offer;
    private String benefit;
    private int electionTypeId;

    public Coverage(Offer offer, App app, String benefit, int electionTypeId) {
        this.offer = offer;
        this.app = app;
        this.benefit = benefit;
        this.electionTypeId = electionTypeId;
        this.coverageId = -1;
    }

    public int getCoverageId() {
        return coverageId;
    }

    public void save() throws SQLException, MissingProperty {
        if (!offer.isLoaded()) {
            throw new MissingProperty("offer is not loaded");
        }
        if (!app.isLoaded()) {
            throw new MissingProperty("app is not loaded");
        }
        insert();
    }

    private void insert() throws SQLException {
        Connection con = null;
        try {
            con = Utils.getConnection();
            String sql = "INSERT INTO FE.Coverages (appId,offerId,benefit, electionTypeId) VALUES (?,?,?,?)";
            PreparedStatement insert = con.prepareStatement(sql);
            insert.setInt(1, app.getAppId());
            insert.setInt(2, offer.getOfferId());
            insert.setString(3, benefit);
            insert.setInt(4, electionTypeId);
            insert.executeUpdate();
            ResultSet rs = insert.getGeneratedKeys();
            if (rs.next()) {
                this.coverageId = rs.getInt(1);
            }
            insert.close();
            rs.close();
        } finally {
            if (con != null) con.close();
        }
    }

    public static int getElectionTypeId(String name) throws SQLException, NoValue {
        Connection con = null;
        int out = -1;
        try {
            if ("".equals(name) || "Declined".equals(name)) {
                name = "declined";
            } else if (name.equals("opt-out")) {
                // do nothing
            } else {
                name = "enrolled";
            }
            String sql = "SELECT electionTypeId FROM FE.ElectionTypes WHERE name = ?";
            PreparedStatement select = con.prepareStatement(sql);
            select.setString(1, name);
            ResultSet rs = select.executeQuery();
            if (rs.next()) {
                out = rs.getInt("electionTypeId");
            } else {
                throw new NoValue();
            }
            select.close();
            rs.close();
        } finally {
            if (con != null) con.close();
        }
        return out;
    }

    public boolean isLoaded() {
        return coverageId > -1 ? true : false;
    }
}
