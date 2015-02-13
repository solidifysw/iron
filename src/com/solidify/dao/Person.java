package com.solidify.dao;

import com.solidify.exceptions.MissingProperty;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by jrobins on 2/9/15.
 */
public class Person {
    private String firstName;
    private String lastName;
    private boolean isEmployee;
    private String ssn;
    private Connection con;
    private int personId;

    public Person(String firstName, String lastName, boolean isEmployee, String ssn, Connection con) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.isEmployee = isEmployee;
        this.ssn = ssn;
        this.con = con;
        personId = -1;
    }

    public Person(String firstName, String lastName, boolean isEmployee, Connection con) {
        this(firstName,lastName,isEmployee,null,con);
    }

    public void save() throws SQLException, MissingProperty {
        String error = "";
        if (firstName == null || "".equals(firstName)) {
            error += "missing firstName ";
        }
        if (lastName == null || "".equals(lastName)) {
            error += "missing lastName ";
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
        String sql = "INSERT INTO FE.people (firstName,lastName,isEmployee,ssn) VALUES (?,?,?,?)";
        PreparedStatement insert = con.prepareStatement(sql);
        insert.setString(1, firstName);
        insert.setString(2,lastName);
        insert.setBoolean(3,isEmployee);
        insert.setString(4,ssn);
        insert.executeUpdate();
        ResultSet rs = insert.getGeneratedKeys();
        if (rs.next()) {
            this.personId = rs.getInt(1);
        }
    }

    public int getPersonId() {
        return personId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public boolean isEmployee() {
        return isEmployee;
    }

    public String getSsn() {
        return ssn;
    }
}
