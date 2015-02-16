package com.solidify.dao;

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

    public SincPackages(String groupUUID) throws SQLException, MissingProperty {
        this.groupUUID = groupUUID;
        this.packages = new HashMap<String,JSONObject>();
        if (groupUUID == null || "".equals(groupUUID)) {
            throw new MissingProperty("groupUUID is missing.");
        }
        load();
    }

    private void load() throws SQLException {
        Connection con = null;
        try {
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
            if (con != null) con.close();
        }
    }

    public HashMap getPackages() {
        return packages;
    }
}
