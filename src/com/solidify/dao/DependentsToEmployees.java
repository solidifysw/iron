package com.solidify.dao;

import com.solidify.admin.reports.Utils;
import com.solidify.exceptions.MissingProperty;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.prefs.PreferenceChangeEvent;

/**
 * Created by jrobins on 2/10/15.
 */
public class DependentsToEmployees {
    private Person ee;
    private Dependent dp;
    private Connection con;
    private boolean manageConnection;

    public DependentsToEmployees(Person ee, Dependent dp, Connection con) {
        this.ee = ee;
        this.dp = dp;
        this.con = con;
        this.manageConnection = con == null ? true : false;
    }

    public void save() throws MissingProperty, SQLException {
        if (!ee.isLoaded()) {
            throw new MissingProperty("ee is not loaded");
        }
        if (!dp.isLoaded()) {
            throw new MissingProperty("dp is not loaded");
        }
        insert();
    }

    private void insert() throws SQLException {
        try {
            if (manageConnection) con = Utils.getConnection();
            String sql = "INSERT INTO FE.DependentsToEmployees (employeeId,dependentId,relationship) VALUES (?,?,?)";
            PreparedStatement insert = con.prepareStatement(sql);
            insert.setInt(1, ee.getPersonId());
            insert.setInt(2, dp.getPersonId());
            insert.setString(3, dp.getRelationship());
            insert.executeUpdate();
            insert.close();
        } finally {
            if (manageConnection && con != null) con.close();
        }
    }
}
