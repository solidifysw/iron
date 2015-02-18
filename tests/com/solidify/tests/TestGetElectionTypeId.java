package com.solidify.tests;


import com.solidify.dao.Coverage;
import com.solidify.exceptions.NoValue;
import org.junit.Test;

import java.sql.SQLException;

import static org.junit.Assert.*;
/**
 * Created by jrobins on 2/17/15.
 */
public class TestGetElectionTypeId extends BaseTest {

    @Test
    public void testGetElectionTypeId() {
        try {
            assertEquals(2,Coverage.getElectionTypeId("Decline",con));
            assertEquals(1,Coverage.getElectionTypeId("John Doe, Mary Doe",con));
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (NoValue noValue) {
            noValue.printStackTrace();
        } finally {
            try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
