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

    public CoveredPeople(Coverage coverage, Person coveredPerson) {
        this.coverage = coverage;
        this.coveredPerson = coveredPerson;
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
        Connection con = null;
        try {
            con = Utils.getConnection();
            String sql = "INSERT INTO FE.CoveredPeople (coverageId,personId) VALUES (?,?)";
            PreparedStatement insert = con.prepareStatement(sql);
            insert.setInt(1, coverage.getCoverageId());
            insert.setInt(2, coveredPerson.getPersonId());
            insert.executeUpdate();
            insert.close();
        } finally {
            if (con != null) con.close();
        }
    }
}
