package com.solidify.tests;

import com.solidify.dao.Coverage;
import com.solidify.exceptions.NoValue;
import org.junit.Test;

import java.sql.SQLException;

import static junit.framework.Assert.assertEquals;

/**
 * Created by jennifermac on 3/5/15.
 */
public class TestCoverage extends BaseTest {

    @Test
    public void testGetElectionTypeId() {
        try {
            assertEquals(2, Coverage.getElectionTypeId("Decline", con));
            assertEquals(1, Coverage.getElectionTypeId("$1,000", con));
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (NoValue noValue) {
            noValue.printStackTrace();
        }

    }
}
