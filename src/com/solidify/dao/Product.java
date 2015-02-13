package com.solidify.dao;

import com.solidify.admin.reports.Utils;
import com.solidify.exceptions.MissingProducts;
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
    private String name;
    private String carrierName;
    private int carrierId;
    private Connection con;
    private static ArrayList<Product> products;

    public Product(int productId, String name, int carrierId, String carrierName, Connection con) {
        this.productId = productId;
        this.name = name;
        this.carrierId = carrierId;
        this.carrierName = carrierName;
        this.con = con;
    }

    public Product(int productId, String name, int carrierId, String carrierName) {
        this(productId,name,carrierId,carrierName,null);
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public String getCarrierName() {
        return carrierName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCarrierId() {
        return carrierId;
    }

    public void setCarrierId(int carrierId) {
        this.carrierId = carrierId;
    }

    public static ArrayList<Product> getAllProducts(Connection con) throws SQLException, MissingProducts {
        if (products == null) {
            products = new ArrayList();
            String sql = "SELECT FE.Products.name, FE.Products.carrierId, FE.Carriers.name AS carrierName, FE.Products.productId FROM FE.Products, FE.Carriers WHERE FE.Products.carrierId = FE.Carriers.carrierId AND FE.Carriers.active = 1;";
            PreparedStatement select = con.prepareStatement(sql);
            ResultSet rs = select.executeQuery();
            while (rs.next()) {
                Product p = new Product(rs.getInt("productId"), rs.getString("name"), rs.getInt("carrierId"),rs.getString("carrierName"));
                products.add(p);
            }
            if (products.size() == 0) throw new MissingProducts();
        }
        return products;
    }

    public static Product findProduct(String name, Connection con) throws SQLException, MissingProducts {
        if (products == null) {
            products = getAllProducts(con);
        }
        Product out = null;
        for (Product prod : products) {
            if (prod.getName().equals(name)) {
                out = prod;
                break;
            }
        }
        return out;
    }
}
