package com.solidify.tests;

import com.solidify.dao.QuestionResponses;
import org.json.JSONObject;
import org.junit.Test;

import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by jrobins on 2/18/15.
 */
public class TestQuestionResponses extends BaseTest {
    
    @Test
    public void testQuestionResponses() {
        try {
            QuestionResponses qr = new QuestionResponses(1,con);
            JSONObject jo = new JSONObject(qr.getJson());
            assertTrue(jo.has("weight-sp"));
            JSONObject spWeight = jo.getJSONObject("weight-sp");
            assertEquals("190",spWeight.getString("answer"));
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
