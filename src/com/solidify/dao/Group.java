package com.solidify.dao;

import com.solidify.admin.reports.Utils;
import com.solidify.exceptions.MissingProperty;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by jrobins on 2/14/15.
 */
public class Group {
    private int groupId;
    private String name;
    private String alias;
    private int active;
    private ArrayList<Address> addresses;
    private Connection con;
    private boolean manageConnection;

    //private ArrayList<Phone> phoneNumbers;
    //private Person mainContact;

    public Group(String name, String alias, int active) {
        this(-1,name,alias,active,null);
    }

    public Group(String name, String alias, int active, Connection con) {
        this(-1,name,alias,active,con);
    }

    public Group(int groupId, String name, String alias, int active, Connection con) {
        this.groupId = groupId;
        this.name = name;
        this.alias = alias;
        this.active = active;
        this.addresses = new ArrayList<Address>();
        this.con = con;
        this.manageConnection = this.con == null ? true : false;
    }

    public int getGroupId() {
        return groupId;
    }

    public String getName() {
        return name;
    }

    public String getAlias() {
        return alias;
    }

    public ArrayList<Address> getAddresses() {
        return addresses;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public void setAddresses(ArrayList<Address> addresses) {
        this.addresses = addresses;
    }

    public void addAddress(Address address) {
        addresses.add(address);
    }

    public void save() throws SQLException, MissingProperty {
        if (name == null || "".equals(name)) {
            throw new MissingProperty("missing group name");
        }
        if (alias == null || "".equals(alias)) {
            throw new MissingProperty("missing group alias");
        }
        if (groupId < 0) {
            insert();
        }
    }

    private void insert() throws SQLException {
        try {
            if (manageConnection) {
                con = Utils.getConnection();
            }
            String sql = "INSERT INTO FE.Groups (name,alias,active) VALUES (?,?,?)";
            PreparedStatement insert = con.prepareStatement(sql);
            insert.setString(1,name);
            insert.setString(2,alias);
            insert.setInt(3,active);
            insert.executeUpdate();
            ResultSet rs = insert.getGeneratedKeys();
            if (rs.next()) groupId = rs.getInt(1);
            insert.close();
            rs.close();
            if (manageConnection) con.close();
            if (!addresses.isEmpty()) {
                for (Address address : addresses) {
                    try {
                        if (!address.hasAssociation()) {
                            address.setAssociation(Address.GROUP, groupId);
                        }
                        address.save();
                    } catch (MissingProperty m) {} // ignore it
                }
            }
        } finally {
            if (manageConnection && con != null) {
                con.close();
            }
        }
    }

    public boolean isLoaded() {
        return groupId > -1 ? true : false;
    }
}
