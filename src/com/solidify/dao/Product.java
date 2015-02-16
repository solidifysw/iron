package com.solidify.dao;

import com.solidify.admin.reports.Utils;
import com.solidify.exceptions.MissingProducts;
import com.solidify.exceptions.MissingProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jrobins on 2/6/15.
 */
public class Product {
    private static final Logger log = LogManager.getLogger();
    private int productId;
    private String solidifyId;
    private String displayName;
    private int carrierId;
    private String carrierName;

    public Product(int productId, String solidifyId, String displayName, int carrierId, String carrierName) {
        this.productId = productId;
        this.solidifyId = solidifyId;
        this.displayName = displayName;
        this.carrierId = carrierId;
        this.carrierName = carrierName;
    }

    public Product(String solidifyId) throws SQLException, MissingProperty {
        this.solidifyId = solidifyId;
        load();
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getSolidifyId() {
        return solidifyId;
    }

    public String getCarrierName() {
        return carrierName;
    }

    public void setSolidifyId(String solidifyId) {
        this.solidifyId = solidifyId;
    }

    public int getCarrierId() {
        return carrierId;
    }

    public void setCarrierId(int carrierId) {
        this.carrierId = carrierId;
    }

    private void load() throws SQLException, MissingProperty {
        if (solidifyId == null || "".equals(solidifyId)) {
            throw new MissingProperty("Missing the solidifyId");
        }
       Connection con = null;
        try {
            String sql = "SELECT FE.Products.productId, FE.Products.displayName, FE.Products.carrierId, FE.Carriers.name AS carrierName FROM FE.Products, FE.Carriers " +
                    "WHERE FE.Products.carrierId = FE.Carriers.carrierId AND FE.Carriers.active = 1 AND FE.Products.solidifyId = ?";
            PreparedStatement select = con.prepareStatement(sql);
            select.setString(1,solidifyId);
            ResultSet rs = select.executeQuery();
            if (rs.next()) {
                productId = rs.getInt("productId");
                displayName = rs.getString("displayName");
                carrierId = rs.getInt("carrierId");
                carrierName = rs.getString("carrierName");
            }
        } finally {
            if (con != null) con.close();
        }
    }

    public boolean isLoaded() {
        return productId > -1 ? true : false;
    }
}
