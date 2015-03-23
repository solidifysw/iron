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
        //connectionProps.put("user","root");
        //connectionProps.put("password", "letmein1");
        connectionProps.put("user","sinc");
        connectionProps.put("password","K6fqxTT3X6Ri5w4N3bC7hMb75nn90WaZ");
        try {
            //con = DriverManager.getConnection("jdbc:mysql://localhost:3306/FE", connectionProps);
            con = DriverManager.getConnection("jdbc:mysql://assurantworks-mysql-upgrade.solidifyhr.com/sinc", connectionProps);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
