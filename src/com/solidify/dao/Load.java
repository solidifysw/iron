package com.solidify.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * Created by jr1 on 3/13/15.
 */
public class Load {
    private int loadId;
    private Date lastLoad;
    private String user;
    private Connection con;

    public Load(String user,Connection con) {
        this.user = user;
        this.con = con;
    }

    public static Date getLastTimeLoadWasDone(Connection con) throws SQLException {
        Date out = null;
        String sql = "SELECT * FROM FE.Loads ORDER BY lastLoad DESC LIMIT 1";
        PreparedStatement select = con.prepareStatement(sql);
        ResultSet rs = select.executeQuery();
        if (rs.next()) {
            out = rs.getTimestamp("lastLoad");
        }
        rs.close();
        select.close();
        return out;
    }

    public void saveLoadTime() throws SQLException {
        String sql = "INSERT INTO FE.Loads (user) VALUES (?)";
        PreparedStatement insert = con.prepareStatement(sql);
        insert.setString(1,user);
        insert.executeUpdate();
        ResultSet rs = insert.getGeneratedKeys();
        if (rs.next()) {
            this.loadId = rs.getInt(1);
        }
        insert.close();
    }

    public int getLoadId() {
        return loadId;
    }

    public String getUser() {
        return user;
    }
}
