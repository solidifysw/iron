package com.solidify.dao;

import com.solidify.exceptions.MissingProperty;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by jrobins on 2/10/15.
 */
public class Address {
    private int addressId;
    private String line1;
    private String line2;
    private String city;
    private String state;
    private String zip;
    private Connection con;

    public Address(int addressId, String line1, String line2, String city, String state, String zip, Connection con) {
        this.addressId = addressId;
        this.line1 = line1;
        this.line2 = line2;
        this.city = city;
        this.state = state;
        this.zip = zip;
        this.con = con;
    }

    public Address(String line1, String line2, String city, String state, String zip, Connection con) {
        this(-1,line1,line2,city,state,zip,con);
    }

    public int getAddressId() {
        return addressId;
    }

    public String getLine1() {
        return line1;
    }

    public String getLine2() {
        return line2;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getZip() {
        return zip;
    }

    public void save() throws SQLException, MissingProperty {
        if (line1 == null && line2 == null && city == null && state == null && zip == null) {
            throw new MissingProperty("All of the properties are null.  Nothing to save.");
        }
        insert();
    }

    private void insert() throws SQLException {
        String sql = "INSERT INTO FE.Addresses (line1, line2, city, state, zip) VALUES (?,?,?,?,?)";
    }
}
