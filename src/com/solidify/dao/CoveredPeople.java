package com.solidify.dao;

import com.solidify.exceptions.MissingProperty;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by jrobins on 2/10/15.
 */
public class CoveredPeople {
    private int coverageId;
    private int personId;
    private Connection con;

    public CoveredPeople(int coverageId, int personId, Connection con) {
        this.coverageId = coverageId;
        this.personId = personId;
        this.con = con;
    }

    public void save() throws SQLException, MissingProperty {
        String error = "";
        if (coverageId < 0) {
            error += "missing coverageId ";
        }
        if (personId < 0) {
            error += "missing personId ";
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
        String sql = "INSERT INTO FE.CoveredPeople (coverageId,personId) VALUES (?,?)";
        PreparedStatement insert = con.prepareStatement(sql);
        insert.setInt(1,coverageId);
        insert.setInt(2,personId);
        insert.executeUpdate();
    }
}
