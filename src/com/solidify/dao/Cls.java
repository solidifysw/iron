package com.solidify.dao;

import com.solidify.admin.reports.Utils;
import com.solidify.exceptions.MissingProperty;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by jrobins on 2/13/15.
 */
public class Cls {
    private int classId;
    private Group group;
    private Pkg pkg;
    private String name;
    private String field;
    private String operator;
    private String value;
    private JSONObject sourceData;

    public Cls(int classId, Group group, Pkg pkg, String name, String field, String operator, String value) {
        this.classId = classId;
        this.group = group;
        this.pkg = pkg;
        this.name = name;
        this.field = field;
        this.operator = operator;
        this.value = value;
    }

    public Cls(Group group, Pkg pkg, String name, String field, String operator, String value) {
        this(-1, group, pkg, name, field, operator, value);
    }

    public void setSourceData(JSONObject sourceData) {
        this.sourceData = sourceData;
    }

    public JSONObject getSourceData() {
        return sourceData;
    }

    public boolean hasProductConfig(String productConfigUUID) {
        boolean out = false;
        if (sourceData.has("productConfigs")) {
            JSONArray prodConfigs = sourceData.getJSONArray("productConfigs");
            for (int i=0; i<prodConfigs.length(); i++) {
                if (productConfigUUID.equals(prodConfigs.getString(i))) {
                    out = true;
                    break;
                }
            }
        }
        return out;
    }

    public int getClassId() {
        return classId;
    }

    public void save() throws SQLException, MissingProperty {
        if (!group.isLoaded()) {
            throw new MissingProperty("group is not loaded");
        }
        if (!pkg.isLoaded()) {
            throw new MissingProperty("pkg is not loaded");
        }
        //if (name == null || "".equals(name)) {
            //throw new MissingProperty("no class name");
        //}
        if (field == null || "".equals(field)) {
            throw new MissingProperty("no field specified for class rule");
        }
        if (operator == null || "".equals(operator)) {
            throw new MissingProperty("no operator specified for class rule");
        }
        if (value == null || "".equals(value)) {
            throw new MissingProperty("no value specified for class rule");
        }
        insert();
    }

    private void insert() throws SQLException {
        Connection con = null;
        try {
            con = Utils.getConnection();
            String sql = "INSERT INTO FE.Classes (groupId,packageId,name,field,operator,value) VALUES (?,?,?,?,?,?)";
            PreparedStatement insert = con.prepareStatement(sql);
            insert.setInt(1, group.getGroupId());
            insert.setInt(2, pkg.getPackageId());
            insert.setString(3, name);
            insert.setString(4, field);
            insert.setString(5, operator);
            insert.setString(6, value);
            insert.executeUpdate();
            ResultSet rs = insert.getGeneratedKeys();
            if (rs.next()) {
                classId = rs.getInt(1);
            }
        } finally {
            if (con != null) con.close();
        }
    }

    public boolean isLoaded() {
        return classId > -1 ? true : false;
    }
}
