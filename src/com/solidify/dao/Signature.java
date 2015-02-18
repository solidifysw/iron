package com.solidify.dao;

import com.solidify.admin.reports.Utils;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by jrobins on 2/18/15.
 */
public class Signature {
    private App app;
    private JSONObject signature;
    private Connection con;
    private boolean manageConnection = true;

    public Signature(App app, JSONObject signature, Connection con) {
        this.app = app;
        this.signature = signature;
        this.con = con;
        if (con != null) {
            this.manageConnection = false;
        }
    }

    public Signature(App app, JSONObject signature) {
        this(app,signature,null);
    }

    public void save() throws SQLException {
        if (isValid()) {
            insert();
        }
    }

    private void insert() throws SQLException {
        try {
            if (manageConnection) {
                con = Utils.getConnection();
            }
            String sql = "INSERT INTO FE.Signatures (appId,json) VALUES (?,?)";
            PreparedStatement insert = con.prepareStatement(sql);
            insert.setInt(1, app.getAppId());
            insert.setString(2, (signature.getJSONArray("signature")).toString());
        } finally {
            if (manageConnection && con!=null) con.close();
        }
    }

    public boolean isValid() {
        if (app.isLoaded() && signature.has("signature")) {
            return true;
        } else {
            return false;
        }
    }
}
