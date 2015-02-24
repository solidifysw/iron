package com.solidify.tests;

import com.solidify.admin.reports.Utils;
import com.solidify.dao.Employee;
import com.solidify.exceptions.MissingProperty;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import static org.junit.Assert.*;

/**
 * Created by jennifermac on 2/16/15.
 */
public class TestEeInsert extends BaseTest{

    @Test
    public void testEeInsert() {
        try {
            Calendar start = Calendar.getInstance();
            start.set(Calendar.DAY_OF_MONTH,10);
            start.set(Calendar.MONTH, 2);
            start.set(Calendar.YEAR, 2015);
            int personId = -1;
            Employee employee = new Employee("Testy","Tester","123-12-1234","1962-03-10","MALE","2008-03-10","2","The Man","man1","1","West","ACTIVE","Sales",40,26,"150000.00",start.getTime(),con);
            employee.save();
            assertTrue(employee.getPersonId()> -1);
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
