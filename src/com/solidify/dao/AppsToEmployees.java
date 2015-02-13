package com.solidify.dao;

import com.solidify.exceptions.MissingProperty;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by jrobins on 2/10/15.
 */
public class AppsToEmployees {
    private int appId;
    private int personId;
    private Connection con;

    public AppsToEmployees(int appId, int personId, Connection con) {
        this.appId = appId;
        this.personId = personId;
        this.con = con;
    }

    public void save() throws SQLException, MissingProperty {
        String error = "";
        if (appId < 0) {
            error += "missing appId ";
        }
        if (personId < 0) {
            error += "missing personId";
        }
        if (!"".equals(error)) {
            throw new MissingProperty(error);
        } else {
            insert();
        }
    }

    private void insert() throws SQLException {
        String sql = "INSERT INTO FE.AppsToEmployees (appId,personId) VALUES (?,?)";
        PreparedStatement insert = con.prepareStatement(sql);
        insert.setInt(1,appId);
        insert.setInt(2,personId);
        insert.executeUpdate();
    }
}
