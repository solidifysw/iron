package com.solidify.tests;

import com.solidify.dao.Load;
import org.junit.Test;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import static org.junit.Assert.assertEquals;

/**
 * Created by jr1 on 3/13/15.
 */
public class TestLoad extends BaseTest {
    @Test
    public void testLoad() {
        String user = "JR";

        Load load = new Load(user,con);
        try {
            load.saveLoadTime();
            Date loadDate = Load.getLastTimeLoadWasDone(con);
            //Calendar cal = Calendar.getInstance();
            //cal.setTime(loadDate);
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String time = df.format(loadDate).toString();
            System.out.println(time);
            //assertEquals("2015-03-13 15:41:50",time);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
