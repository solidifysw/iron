package com.solidify.tests;

import com.solidify.dao.SincPackages;
import com.solidify.exceptions.MissingProperty;
import org.json.JSONObject;
import org.junit.Test;

import java.sql.SQLException;
import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * Created by jrobins on 2/17/15.
 */
public class SincPackagesTest extends BaseTest {

    @Test
    public void testSincPackages() {
        try {
            SincPackages sp = new SincPackages("fefc6deb-9c08-47c3-b132-e93a1c9e9554",con);
            HashMap<String, JSONObject> packages = sp.getPackages();
            assertFalse(packages.isEmpty());
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (MissingProperty missingProperty) {
            missingProperty.printStackTrace();
        } finally {
            try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
