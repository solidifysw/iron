package com.solidify.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by jrobins on 2/13/15.
 */
public class ClassOffer {
    private int classId;
    private int offerId;
    private Connection con;

    public ClassOffer(int classId, int offerId, Connection con) {
        this.classId = classId;
        this.offerId = offerId;
        this.con = con;
    }

    public void save() throws SQLException {
        insert();
    }

    private void insert() throws SQLException {
        String sql = "INSERT INTO FE.ClassOffers (classId, offerId) VALUES (?,?)";
        PreparedStatement insert = con.prepareStatement(sql);
        insert.setInt(1,classId);
        insert.setInt(2,offerId);
        insert.executeUpdate();
    }
}
