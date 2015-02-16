package com.solidify.dao;

import com.solidify.admin.reports.Utils;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by jennifermac on 2/15/15.
 */
public class SincGroups {
    private HashSet<JSONObject> groups;

    public SincGroups() throws SQLException {
        this.groups = new HashSet<JSONObject>();
        load();
    }

    private void load() throws SQLException {
        Connection con = null;
        try {
            con = Utils.getConnection();
            String sql = "SELECT id, data AS json FROM sinc.groups WHERE deleted = 0 AND id = '1a83f17c-34e3-45c0-b323-d6174400ab05'"; // for testing
            //String sql = "SELECT id, data AS json FROM sinc.groups WHERE deleted = 0";
            PreparedStatement select = con.prepareStatement(sql);
            ResultSet rs = select.executeQuery();
            while (rs.next()) {
                String groupUUID = rs.getString("id");
                JSONObject obj = new JSONObject(rs.getString("json"));
                groups.add(obj);
            }
            rs.close();
            select.close();
        } finally {
            if (con != null) con.close();
        }
    }

    public HashSet<JSONObject> getGroups() {
        return groups;
    }
}
