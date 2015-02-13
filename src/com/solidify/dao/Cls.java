package com.solidify.dao;

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
    private int groupId;
    private int packageId;
    private String name;
    private String field;
    private String operator;
    private String value;
    private Connection con;
    private JSONObject sourceData;

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

    public Cls(int classId, int groupId, int packageId, String name, String field, String operator, String value, Connection con) {
        this.classId = classId;
        this.groupId = groupId;
        this.packageId = packageId;
        this.name = name;
        this.field = field;
        this.operator = operator;
        this.value = value;
        this.con = con;
    }

    public Cls(int groupId, int packageId, String name, String field, String operator, String value, Connection con) {
        this(-1,groupId,packageId,name,field,operator,value,con);
    }

    public int getClassId() {
        return classId;
    }

    public int getGroupId() {
        return groupId;
    }

    public int getPackageId() {
        return packageId;
    }

    public String getName() {
        return name;
    }

    public String getField() {
        return field;
    }

    public String getOperator() {
        return operator;
    }

    public String getValue() {
        return value;
    }

    public void save() throws SQLException {
        insert();
    }

    private void insert() throws SQLException {
        String sql = "INSERT INTO FE.Classes (groupId,packageId,name,field,operator,value) VALUES (?,?,?,?,?,?)";
        PreparedStatement insert = con.prepareStatement(sql);
        insert.setInt(1,groupId);
        insert.setInt(2,packageId);
        insert.setString(3,name);
        insert.setString(4,field);
        insert.setString(5,operator);
        insert.setString(6,value);
        insert.executeUpdate();
        ResultSet rs = insert.getGeneratedKeys();
        if (rs.next()) {
            classId = rs.getInt(1);
        }
    }
}
