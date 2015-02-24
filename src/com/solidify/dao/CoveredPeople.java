package com.solidify.dao;

import com.solidify.admin.reports.Utils;
import com.solidify.exceptions.MissingProperty;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by jrobins on 2/10/15.
 */
public class CoveredPeople {
    private Coverage coverage;
    private Person coveredPerson;
    private Connection con;
    private boolean manageConnection;

    public CoveredPeople(Coverage coverage, Person coveredPerson, Connection con) {
        this.coverage = coverage;
        this.coveredPerson = coveredPerson;
        this.con = con;
        this.manageConnection = con == null ? true : false;
    }

    public void save() throws SQLException, MissingProperty {
        if (!coverage.isLoaded()) {
            throw new MissingProperty("coverage is not loaded");
        }
        if (!coveredPerson.isLoaded()) {
            throw new MissingProperty("coveredPerson is not loaded");
        }
        insert();
    }

    private void insert() throws SQLException {
        try {
            if (manageConnection) con = Utils.getConnection();
            String sql = "INSERT INTO FE.CoveredPeople (coverageId,personId) VALUES (?,?)";
            PreparedStatement insert = con.prepareStatement(sql);
            insert.setInt(1, coverage.getCoverageId());
            insert.setInt(2, coveredPerson.getPersonId());
            insert.executeUpdate();
            insert.close();
        } finally {
            if (manageConnection && con != null) con.close();
        }
    }
}
