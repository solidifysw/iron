package com.solidify.dao;

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
    private int appId;
    private int offerId;
    private String benefit;
    private int electionTypeId;
    private Connection con;

    public Coverage(int offerId, int appId, String benefit, int electionTypeId, Connection con) {
        this.offerId = offerId;
        this.appId = appId;
        this.benefit = benefit;
        this.electionTypeId = electionTypeId;
        this.con = con;
        this.coverageId = -1;
    }

    public int getCoverageId() {
        return coverageId;
    }

    public void save() throws SQLException, MissingProperty {
        String error = "";
        if (appId < 0) {
            error += "missing appId ";
        }
        if (offerId < 0) {
            error += "missing offerId ";
        }
        if (electionTypeId < 0) {
            error += "missing electionType ";
        }
        if (!"".equals(error)) {
            throw new MissingProperty(error);
        } else {
            insert();
        }
    }

    private void insert() throws SQLException {
        String sql = "INSERT INTO FE.Coverages (appId,offerId,benefit, electionTypeId) VALUES (?,?,?,?)";
        PreparedStatement insert = con.prepareStatement(sql);
        insert.setInt(1,appId);
        insert.setInt(2,offerId);
        insert.setString(3,benefit);
        insert.setInt(4,electionTypeId);
        insert.executeUpdate();
        ResultSet rs = insert.getGeneratedKeys();
        if (rs.next()) {
            this.coverageId = rs.getInt(1);
        }
    }

    public static int getElectionTypeId(String name, Connection con) throws SQLException, NoValue {
        int out = -1;
        if ("".equals(name) || "Declined".equals(name)) {
            name = "declined";
        } else if (name.equals("opt-out")) {
            // do nothing
        } else {
            name = "enrolled";
        }
        String sql = "SELECT electionTypeId FROM FE.ElectionTypes WHERE name = ?";
        PreparedStatement select = con.prepareStatement(sql);
        select.setString(1,name);
        ResultSet rs = select.executeQuery();
        if (rs.next()) {
            out = rs.getInt("electionTypeId");
        } else {
            throw new NoValue();
        }
        return out;
    }
}
