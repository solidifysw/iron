package com.solidify.dao;

import com.solidify.exceptions.MissingProperty;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.sql.Connection;

/**
 * Created by jr1 on 3/12/15.
 */
public class Beneficiary {
    private Person p;
    private int coverageId;
    private String type;
    private int percentage;
    private String relationship;
    private Connection con;

    public Beneficiary(int coverageId, JSONObject ben, Connection con) {
        this(coverageId,ben.getString("firstName"),ben.getString("lastName"),ben.getString("address1"),ben.getString("address2"),ben.getString("city"),ben.getString("state"),
                ben.getString("zip"),ben.getString("relationship"),ben.getString("type"),ben.getInt("percent"),con);
    }

    public Beneficiary(int coverageId, String firstName, String lastName, String address1, String address2, String city, String state, String zip, String relationship, String type, int percentage, Connection con) {
        Calendar now = Calendar.getInstance();
        this.p = new Person(-1,firstName,lastName,false,null,null,null,now.getTime(),null,con);
        Address address = new Address("",address1,address2,city,state,zip,con);
        p.addAddress(address);
        try {
            p.save();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (MissingProperty missingProperty) {
            missingProperty.printStackTrace();
        }
        if (p.getPersonId() < 0) {
            System.out.println("Problem with Person object");
            return;
        }
        this.coverageId = coverageId;
        this.type = type;
        this.percentage = percentage;
        this.relationship = relationship;
        this.con = con;
    }

    public void save() throws SQLException, MissingProperty {
        insert();

    }

    private void insert() throws SQLException {
        String sql = "INSERT INTO FE.Beneficiaries (coverageId, personId, type, percentage, relationship) VALUES (?,?,?,?,?)";
        PreparedStatement insert = con.prepareStatement(sql);
        insert.setInt(1,coverageId);
        insert.setInt(2,p.getPersonId());
        insert.setString(3,type);
        insert.setInt(4, percentage);
        insert.setString(5,relationship);
        insert.execute();
    }

}
