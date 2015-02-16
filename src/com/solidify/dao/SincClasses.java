package com.solidify.dao;

import com.solidify.admin.reports.Utils;
import com.solidify.exceptions.MissingProducts;
import com.solidify.exceptions.MissingProperty;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;

/**
 * Created by jennifermac on 2/15/15.
 */
public class SincClasses {
    private HashSet classes;
    private String groupUUID;
    private String packageUUID;

    public SincClasses(String groupUUID, String packageUUID) throws MissingProperty, SQLException {
        this.groupUUID = groupUUID;
        this.packageUUID = packageUUID;
        classes = new HashSet<JSONObject>();
        if (groupUUID == null || "".equals(groupUUID)) {
            throw new MissingProperty("Missing groupUUID");
        } else if (packageUUID == null || "".equals(packageUUID)) {
            throw new MissingProperty("Missing packageUUID");
        }
        load();
    }

    private void load() throws SQLException {
        Connection con = null;
        try {
            con = Utils.getConnection();
            String sql = "SELECT data AS json FROM sinc.classes WHERE packageId = ? AND groupId = ? AND deleted = 0";
            PreparedStatement select = con.prepareStatement(sql);
            select.setString(1, packageUUID);
            select.setString(2, groupUUID);
            ResultSet rs = select.executeQuery();
            JSONObject cls = null;
            while (rs.next()) {
                cls = new JSONObject(rs.getString("json"));
                classes.add(cls);
            }
            rs.close();
            select.close();
        } finally {
            if (con != null) con.close();
        }
    }

    public HashSet<JSONObject> getClasses() {
        return classes;
    }
}
