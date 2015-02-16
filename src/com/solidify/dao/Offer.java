package com.solidify.dao;

import com.solidify.admin.reports.Utils;
import com.solidify.exceptions.MissingProperty;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by jrobins on 2/5/15.
 */
public class Offer {
    private int offerId;
    private Group group;
    private Product product;
    private Pkg pkg;

    public Offer(int offerId, Group group, Product product, Pkg pkg) {
        this.offerId = offerId;
        this.group = group;
        this.product = product;
        this.pkg = pkg;
    }

    public Offer(Group group, Product product, Pkg pkg) {
        this(-1, group, product, pkg);
    }

    public int getOfferId() {
        return offerId;
    }

    public void save() throws SQLException, MissingProperty {
        if (!group.isLoaded()) {
            throw new MissingProperty("group is not loaded");
        }
        if (!product.isLoaded()) {
            throw new MissingProperty("product is not loaded");
        }
        if (!pkg.isLoaded()) {
            throw new MissingProperty("pkg is not loaded");
        }
        insert();
    }

    private void insert() throws SQLException {
        Connection con = null;
        try {
            con = Utils.getConnection();
            String sql = "INSERT INTO FE.Offers(groupId,productId,packageId) VALUES(?,?,?)";
            PreparedStatement insert = con.prepareStatement(sql);
            insert.setInt(1, group.getGroupId());
            insert.setInt(2, product.getProductId());
            insert.setInt(3, pkg.getPackageId());
            insert.executeUpdate();
            ResultSet rs = insert.getGeneratedKeys();
            if (rs.next()) {
                offerId = rs.getInt(1);
            }
            rs.close();
            insert.close();
        } finally {
            if (con != null) con.close();
        }
    }

    public boolean isLoaded() {
        return offerId > -1 ? true : false;
    }
}
