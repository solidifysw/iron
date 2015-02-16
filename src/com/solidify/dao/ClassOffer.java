package com.solidify.dao;

import com.solidify.admin.reports.Utils;
import com.solidify.exceptions.MissingProperty;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by jrobins on 2/13/15.
 */
public class ClassOffer {
    private Cls cls;
    private Offer offer;

    public ClassOffer(Cls cls, Offer offer) {
        this.cls = cls;
        this.offer = offer;
    }

    public void save() throws SQLException, MissingProperty {
        if (!cls.isLoaded()) {
            throw new MissingProperty("cls is not loaded");
        }
        if (!offer.isLoaded()) {
            throw new MissingProperty("offer is not loaded");
        }
        insert();
    }

    private void insert() throws SQLException {
        Connection con = null;
        try {
            con = Utils.getConnection();
            String sql = "INSERT INTO FE.ClassOffers (classId, offerId) VALUES (?,?)";
            PreparedStatement insert = con.prepareStatement(sql);
            insert.setInt(1, cls.getClassId());
            insert.setInt(2, offer.getOfferId());
            insert.executeUpdate();
            insert.close();
        } finally {
            if (con != null) con.close();
        }
    }
}
