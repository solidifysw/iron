package com.solidify.dao;

import com.solidify.admin.reports.Utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by jrobins on 2/5/15.
 */
public class Offer {
    private int offerId;
    private int groupId;
    private int productId;
    private int packageId;
    private Connection con;

    public Offer(int offerId, int groupId, int productId, int packageId, Connection con) {
        this.offerId = offerId;
        this.groupId = groupId;
        this.productId = productId;
        this.packageId = packageId;
        this.con = con;
    }

    public Offer(int groupId, int productId, int packageId, Connection con) {
        this(-1,groupId,productId,packageId,con);
    }

    public int getOfferId() {
        return offerId;
    }

    public void setOfferId(int offerId) {
        this.offerId = offerId;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getPackageId() {
        return packageId;
    }

    public void setPackageId(int packageId) {
        this.packageId = packageId;
    }

    public void save() throws SQLException {
        if (offerId == -1 && groupId >= 0 && productId >= 0 && packageId >= 0) {
            insert();
        }
        //else if (offerId >=0 &&  groupId >= 0 && productId >= 0 && packageId >= 0) {
            //update();
        //}
    }

    private void insert() throws SQLException {

            String sql = "INSERT INTO FE.Offers(groupId,productId,packageId) VALUES(?,?,?)";
            PreparedStatement insert = con.prepareStatement(sql);
            insert.setInt(1,groupId);
            insert.setInt(2,productId);
            insert.setInt(3,packageId);
            insert.executeUpdate();
            ResultSet rs = insert.getGeneratedKeys();
            if (rs.next()) {
                offerId = rs.getInt(1);
            }
            rs.close();
            insert.close();
    }

    private void update() throws SQLException {

            String sql = "UPDATE FE.Offers SET groupId = ?,productId=?,packageId=? WHERE offerId = ?";
            PreparedStatement update = con.prepareStatement(sql);

    }
}
