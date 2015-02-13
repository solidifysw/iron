package com.solidify.dao;

import com.solidify.exceptions.MissingProperty;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by jrobins on 2/10/15.
 */
public class AddressesToPeople {
    private int addressId;
    private int personId;
    private String type;
    private Connection con;

    public AddressesToPeople(int addressId, int personId, String type, Connection con) {
        this.addressId = addressId;
        this.personId = personId;
        this.type = type;
        this.con = con;
    }

    public void save() throws SQLException, MissingProperty {
        String error = "";
        if (addressId < 0) {
            error += "Missing addressId ";
        }
        if (personId < 0) {
            error = "Missing personId ";
        }
        if (type == null || "".equals(type)) {
            error = "Missing address type ";
        }
        if (con == null) {
            error = "Missing database connection ";
        }
        if (!"".equals(error)) {
            throw new MissingProperty(error);
        } else {
            insert();
        }
    }

    private void insert() throws SQLException {
        String sql = "INSERT INTO FE.AddressesToPeople (addressId, personId, type) VALUES (?,?,?)";
        PreparedStatement insert = con.prepareStatement(sql);
        insert.setInt(1,addressId);
        insert.setInt(2,personId);
        insert.setString(3, type);
        insert.executeUpdate();
    }
}
