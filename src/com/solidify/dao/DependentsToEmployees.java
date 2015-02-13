package com.solidify.dao;

import com.solidify.exceptions.MissingProperty;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.prefs.PreferenceChangeEvent;

/**
 * Created by jrobins on 2/10/15.
 */
public class DependentsToEmployees {
    private int employeeId;
    private int dependentId;
    private String relationship;
    private Connection con;

    public DependentsToEmployees(int employeeId, int dependentId, String relationship, Connection con) {
        this.employeeId = employeeId;
        this.dependentId = dependentId;
        this.relationship = relationship;
        this.con = con;
    }

    public void save() throws MissingProperty, SQLException {
        String error = "";
        if (employeeId < 0) {
            error += "missing employeeId ";
        }
        if (dependentId < 0) {
            error += "missing dependentId ";
        }
        if (relationship == null || "".equals(relationship)) {
            error += "missing relationship ";
        }
        if (con == null) {
            error += "missing connection object ";
        }
        if (!"".equals(error)) {
            throw new MissingProperty(error);
        } else {
            insert();
        }
    }

    private void insert() throws SQLException {
        String sql = "INSERT INTO FE.DependentsToEmployees (employeeId,dependentId,relationship) VALUES (?,?,?)";
        PreparedStatement insert = con.prepareStatement(sql);
        insert.setInt(1,employeeId);
        insert.setInt(2,dependentId);
        insert.setString(3,relationship);
        insert.executeUpdate();
    }
}
