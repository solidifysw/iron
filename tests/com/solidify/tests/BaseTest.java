package com.solidify.tests;

import org.json.JSONObject;
import org.junit.Before;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

/**
 * Created by jrobins on 2/17/15.
 */
public class BaseTest {
    protected Connection con;
    private Properties connectionProps;

    @Before
    public void setUp() {
        connectionProps = new Properties();
        connectionProps.put("user","root");
        connectionProps.put("password", "letmein1");

        try {
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/FE", connectionProps);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
