package com.solidify.dao;

import com.solidify.admin.reports.Utils;
import com.solidify.exceptions.MissingProperty;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

/**
 * Created by jennifermac on 2/15/15.
 */
public class SincPackages {
    private HashMap<String,JSONObject> packages;
    private String groupUUID;
    private Connection con;
    private boolean manageConnection = true;

    public SincPackages(String groupUUID) throws SQLException, MissingProperty {
        this(groupUUID, null);
    }

    public SincPackages(String groupUUID, Connection con) throws SQLException, MissingProperty {
        this.groupUUID = groupUUID;
        this.con = con;
        this.packages = new HashMap<String,JSONObject>();
        if (groupUUID == null || "".equals(groupUUID)) {
            throw new MissingProperty("groupUUID is missing.");
        }
        if (this.con != null) {
            manageConnection = false;
        }
        load();
    }

    private void load() throws SQLException {
        try {
            if (manageConnection) {
                con = Utils.getConnection();
            }
            String sql = "SELECT id, data FROM sinc.packages WHERE deleted = 0 AND groupId = ?";
            PreparedStatement packs = con.prepareStatement(sql);
            packs.setString(1,groupUUID);
            ResultSet rs = packs.executeQuery();

            while(rs.next()) {
                // Loop through each package
                String data = rs.getString("data");
                JSONObject pkg = new JSONObject(data);
                String pkgUUID = rs.getString("id");
                packages.put(pkgUUID, pkg);
            }
            rs.close();
            packs.close();
        } finally {
            if (manageConnection && con != null) con.close();
        }
    }

    public HashMap<String, JSONObject> getPackages() {
        return packages;
    }
}
