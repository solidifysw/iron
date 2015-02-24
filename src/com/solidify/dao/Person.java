package com.solidify.dao;

import com.solidify.admin.reports.Utils;
import com.solidify.exceptions.MissingProperty;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
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
    private String gender;
    private String dateOfBirth;
    private HashSet<Address> addresses;
    private Date start;
    private Date end;
    private Connection con;
    private boolean manageConnection = true;

    public Person(int personId, String firstName, String lastName, boolean isEmployee, String ssn, String dateOfBirth, String gender, Date start, Date end, Connection con) {
        this.personId = personId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.isEmployee = isEmployee;
        this.ssn = ssn;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.addresses = new HashSet<Address>();
        this.start = start;
        this.end = end;
        this.con = con;
        this.manageConnection = con == null ? true : false;
    }

    public Person(String firstName, String lastName, boolean isEmployee, String ssn, String dateOfBirth, String gender, Date start, Connection con) {
        this(-1, firstName, lastName, isEmployee, ssn,dateOfBirth,gender,start,null,con);
    }

    public Person(String firstName, String lastName, boolean isEmployee, String ssn, String dateOfBirth, String gender, Date start, Date end, Connection con) {
        this(-1, firstName, lastName, isEmployee, ssn,dateOfBirth,gender,start,end, con);
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
        try {
            if (manageConnection) {
                con = Utils.getConnection();
            }
            String sql = "INSERT INTO FE.people (firstName, lastName, isEmployee, ssn, dateOfBirth, gender, start, end) VALUES (?,?,?,?,?,?,?,?)";
            PreparedStatement insert = con.prepareStatement(sql);
            insert.setString(1, firstName);
            insert.setString(2, lastName);
            insert.setBoolean(3, isEmployee);
            insert.setString(4, ssn);
            insert.setString(5, dateOfBirth);
            insert.setString(6, gender);
            insert.setDate(7, new java.sql.Date(start.getTime()));
            if (end == null) {
                insert.setDate(8, null);
            } else {
                insert.setDate(8, new java.sql.Date(end.getTime()));
            }
            insert.executeUpdate();
            ResultSet rs = insert.getGeneratedKeys();
            if (rs.next()) {
                this.personId = rs.getInt(1);
            }
            insert.close();
            rs.close();
            if (manageConnection) {
                con.close();
            }
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
            if (manageConnection && con != null) con.close();
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

    public HashSet<Address> getAddresses() {
        return addresses;
    }

    public Date getStart() {
        return start;
    }

    public Date getEnd() {
        return end;
    }
}
