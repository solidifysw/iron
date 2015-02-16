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
    private HashSet<Address> addresses;
    private Date start;
    private Date end;
    private Connection con;
    private boolean manageConnection = true;

    public Person(int personId, String firstName, String lastName, boolean isEmployee, String ssn, Date start, Date end) {
        this.personId = personId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.isEmployee = isEmployee;
        this.ssn = ssn;
        this.addresses = new HashSet<Address>();
        this.con = null;
        this.start = start;
        this.end = end;
    }

    public Person(String firstName, String lastName, boolean isEmployee, String ssn, Date start) {
        this(-1, firstName, lastName, isEmployee, ssn,start,null);
    }

    public Person(String firstName, String lastName, boolean isEmployee, String ssn, Date start, Date end) {
        this(-1, firstName, lastName, isEmployee, ssn,start,end);
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
            if (con == null) {
                con = Utils.getConnection();
            }
            String sql = "INSERT INTO FE.people (firstName,lastName,isEmployee,ssn,start,end) VALUES (?,?,?,?,?,?)";
            PreparedStatement insert = con.prepareStatement(sql);
            insert.setString(1, firstName);
            insert.setString(2, lastName);
            insert.setBoolean(3, isEmployee);
            insert.setString(4, ssn);
            insert.setDate(5, new java.sql.Date(start.getTime()));
            if (end == null) {
                insert.setDate(6, null);
            } else {
                insert.setDate(6, new java.sql.Date(end.getTime()));
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

    public void setConnection (Connection con) {
        this.con = con;
        manageConnection = false;
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
