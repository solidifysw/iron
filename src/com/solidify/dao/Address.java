package com.solidify.dao;

import com.solidify.admin.reports.Utils;
import com.solidify.exceptions.MissingProperty;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by jrobins on 2/10/15.
 */
public class Address {
    private int addressId;
    private String type;
    private String line1;
    private String line2;
    private String city;
    private String state;
    private String zip;
    private int associationType;
    private int associationId;
    public static final int GROUP = 1;
    public static final int PERSON = 2;

    public Address(int addressId, String type, String line1, String line2, String city, String state, String zip, int associationType, int associationId) {
        this.addressId = addressId;
        this.type = type == null ? "" : type;
        this.line1 = line1 == null ? "" : line1;
        this.line2 = line2 == null ? "" : line2;
        this.city = city == null ? "" : city;
        this.state = state == null ? "" : state;
        this.zip = zip == null ? "" : zip;
        this.associationType = associationType;
        this.associationId = associationId;
    }

    public Address(String type, String line1, String line2, String city, String state, String zip, int associationType, int associationId) {
        this(-1,type,line1,line2,city,state,zip,associationType,associationId);
    }

    public Address(String type, String line1, String line2, String city, String state, String zip) {
        this(-1,type,line1,line2,city,state,zip,-1,-1);
    }

    public void setAssociation(int associationType, int associationId) {
        this.associationType = associationType;
        this.associationId = associationId;
    }

    public boolean hasAssociation() {
        if (associationId > -1 && (associationType == GROUP || associationType == PERSON)) {
            return true;
        } else {
            return false;
        }
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

    public void setAddressId(int addressId) {
        this.addressId = addressId;
    }

    public void save() throws SQLException, MissingProperty {
        if (!isValid()) {
            throw new MissingProperty("No values to save");
        }
        if (addressId < 0) {
            insert();
        }

    }

    private void insert() throws SQLException {
        String sql = "INSERT INTO FE.Addresses (line1, line2, city, state, zip, type) VALUES (?,?,?,?,?,?)";
        Connection con = null;
        try {
            con = Utils.getConnection();
            PreparedStatement insert = con.prepareStatement(sql);
            insert.setString(1,line1);
            insert.setString(2,line2);
            insert.setString(3,city);
            insert.setString(4,state);
            insert.setString(5,zip);
            insert.setString(6,type);
            insert.executeUpdate();
            ResultSet rs = insert.getGeneratedKeys();
            if (rs.next()) addressId = rs.getInt(1);
            insert.close();
            rs.close();

            if (associationType == GROUP) {
                sql = "INSERT INTO FE.AddressesToGroups (groupId,addressId) VALUES (?,?)";
            } else if (associationType == PERSON) {
                sql = "INSERT INTO FE.AddressesToPeople (personId,addressId) VALUES (?,?)";
            }
            insert = con.prepareStatement(sql);
            insert.setInt(1,associationId);
            insert.setInt(2,addressId);
            insert.executeUpdate();
            insert.close();
        } finally {
            if (con != null) con.close();
        }
    }

    public boolean isValid() {
        if (associationType != GROUP && associationType != PERSON) {
            return false;
        }
        if (associationId < 0) {
            return false;
        }
        if (line1 != null && !"".equals(line1)) {
            return true;
        }
        if (line2 != null && !"".equals(line2)) {
            return true;
        }
        if (city != null && !"".equals(city)) {
            return true;
        }
        if (state != null && !"".equals(state)) {
            return true;
        }
        if (zip != null && !"".equals(zip)) {
            return true;
        }
        return false;
    }

    public boolean isLoaded() {
        return addressId > -1 ? true : false;
    }
}
