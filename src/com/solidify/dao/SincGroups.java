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
 * Created by jrobins on 2/15/15.
 */
public class SincGroups {
    private HashSet<JSONObject> groups;
    private Connection con;
    private boolean manageConnection = true;

    public SincGroups() throws SQLException {
        this(null);
    }

    public SincGroups(Connection con) throws SQLException {
        this.groups = new HashSet<JSONObject>();
        this.con = con;
        this.manageConnection = this.con == null ? true : false;
        load();
    }

    private void load() throws SQLException {
        try {
            if (manageConnection) {
                con = Utils.getConnection();
            }
            String sql = "SELECT data AS json FROM sinc.groups WHERE deleted = 0 AND id = 'fefc6deb-9c08-47c3-b132-e93a1c9e9554'"; // for testing
            //String sql = "SELECT id, data AS json FROM sinc.groups WHERE deleted = 0 AND id = '1a83f17c-34e3-45c0-b323-d6174400ab05'"; // for testing
            //String sql = "SELECT id, data AS json FROM sinc.groups WHERE deleted = 0";
            PreparedStatement select = con.prepareStatement(sql);
            ResultSet rs = select.executeQuery();
            while (rs.next()) {
                JSONObject obj = new JSONObject(rs.getString("json"));
                groups.add(obj);
            }
            rs.close();
            select.close();
        } finally {
            if (manageConnection && con != null) con.close();
        }
    }

    public static JSONObject getLoginScheme(JSONObject group) {
        if (group.has("loginScheme")) {
            JSONObject loginScheme = group.getJSONObject("loginScheme");
            return loginScheme;
        } else {
            return new JSONObject();
        }
    }

    public HashSet<JSONObject> getGroups() {
        return groups;
    }
}
