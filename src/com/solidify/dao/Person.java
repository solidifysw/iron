package com.solidify.dao;

import com.solidify.admin.reports.Utils;
import com.solidify.exceptions.MissingProperty;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;

/**
 * Created by jrobins on 2/9/15.
 */
public class Person {
    private int personId;
    private String firstName;
    private String lastName;
    private boolean isEmployee;
    private String ssn;
    private HashSet<Address> addresses;

    public Person(int personId, String firstName, String lastName, boolean isEmployee, String ssn) {
        this.personId = personId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.isEmployee = isEmployee;
        this.ssn = ssn;
        this.addresses = new HashSet<Address>();
    }

    public Person(String firstName, String lastName, boolean isEmployee, String ssn) {
        this(-1, firstName, lastName, isEmployee, ssn);
    }

    public void save() throws SQLException, MissingProperty {
        String error = "";
        if (firstName == null || "".equals(firstName)) {
            error += "missing firstName ";
        }
        if (lastName == null || "".equals(lastName)) {
            error += "missing lastName ";
        }
        if (!"".equals(error)) {
            throw new MissingProperty(error);
        } else {
            insert();
        }
    }

    private void insert() throws SQLException {
        Connection con = null;
        try {
            con = Utils.getConnection();
            String sql = "INSERT INTO FE.people (firstName,lastName,isEmployee,ssn) VALUES (?,?,?,?)";
            PreparedStatement insert = con.prepareStatement(sql);
            insert.setString(1, firstName);
            insert.setString(2, lastName);
            insert.setBoolean(3, isEmployee);
            insert.setString(4, ssn);
            insert.executeUpdate();
            ResultSet rs = insert.getGeneratedKeys();
            if (rs.next()) {
                this.personId = rs.getInt(1);
            }
            insert.close();
            rs.close();
            con.close();
            if (!addresses.isEmpty()) {
                for (Address address : addresses) {
                    try {
                        if (!address.hasAssociation()) {
                            address.setAssociation(Address.PERSON,personId);
                            address.save();
                        }
                    } catch (MissingProperty m) {} // ignore
                }
            }

        } finally {
            if (con != null) con.close();
        }
    }

    public void addAddress(Address address) {
        addresses.add(address);
    }

    public boolean isLoaded() {
        return personId > -1 ? true : false;
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
