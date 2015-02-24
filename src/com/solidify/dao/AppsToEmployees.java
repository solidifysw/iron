package com.solidify.dao;

import com.solidify.admin.reports.Utils;
import com.solidify.exceptions.MissingProperty;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by jrobins on 2/10/15.
 */
public class AppsToEmployees {
    private App app;
    private Person ee;
    private Connection con;
    private boolean manageConnection;

    public AppsToEmployees(App app, Person ee, Connection con) {
        this.app = app;
        this.ee = ee;
        this.con = con;
        this.manageConnection = con == null ? true : false;
    }

    public void save() throws SQLException, MissingProperty {
        if (!app.isLoaded()) {
            throw new MissingProperty("app is not loaded");
        }
        if (!ee.isLoaded()) {
            throw new MissingProperty("ee is not loaded");
        }
        insert();
    }

    private void insert() throws SQLException {
        try {
            if (manageConnection) con = Utils.getConnection();
            String sql = "INSERT INTO FE.AppsToEmployees (appId,personId) VALUES (?,?)";
            PreparedStatement insert = con.prepareStatement(sql);
            insert.setInt(1, app.getAppId());
            insert.setInt(2, ee.getPersonId());
            insert.executeUpdate();
            insert.close();
        } finally {
            if (manageConnection && con != null) con.close();
        }
    }
}
