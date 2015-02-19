package com.solidify.tests;

import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;
import com.fasterxml.jackson.core.*;
//import org.codehaus.jackson.map.ObjectMapper;
//import org.codehaus.jackson.map.type.MapType;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created by jennifermac on 2/19/15.
 */
public class TestJsonParsing extends BaseTest {

    @Test
    public void testParsing() {
        try {
            String json = "{}";
            Statement stmt1 = con.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,java.sql.ResultSet.CONCUR_READ_ONLY);
            stmt1.setFetchSize(Integer.MIN_VALUE);
            ResultSet rs = stmt1.executeQuery("SELECT data,memberId FROM sinc.orders WHERE id ='bb10a2ce-3ae8-40fe-a255-c40f87be1f8d'");
            if (rs.next()) {
                Blob b = rs.getBlob("data");
                byte[] bdata = b.getBytes(1, (int) b.length());
                json = new String(bdata);
            }
            stmt1.close();
            rs.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
