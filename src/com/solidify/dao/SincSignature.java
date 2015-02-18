package com.solidify.dao;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.solidify.admin.reports.Utils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.*;

/**
 * Created by jrobins on 2/18/15.
 */
public class SincSignature {
    private String orderId;
    private JSONObject json;
    private Connection con;
    private boolean manageConnection = true;

    public SincSignature(String orderId, Connection con) throws IOException, SQLException {
        this.orderId = orderId;
        this.con = con;
        if (con != null) {
            this.manageConnection = false;
        }
        load();
    }

    public SincSignature(String orderId) throws IOException, SQLException {
        this(orderId,null);
    }

    private void load() throws SQLException, IOException {
        try {
            if (manageConnection) {
                con = Utils.getConnection();
            }
            Statement stmt1 = con.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY);
            stmt1.setFetchSize(Integer.MIN_VALUE);
            ResultSet rs = stmt1.executeQuery("SELECT data FROM sinc.orders WHERE id ='" + orderId + "'");
            String rawJson = null;
            if (rs.next()) {
                Blob b = rs.getBlob("data");
                byte[] bdata = b.getBytes(1, (int) b.length());
                rawJson = new String(bdata, "UTF-8");
            }
            rs.close();
            stmt1.close();
            if (rawJson != null) {
                this.json = parseSignature(rawJson);
            }

        } finally {
            if (manageConnection && con != null) con.close();
        }
    }

    public static JSONObject parseSignature(String rawJson) throws IOException {
        JSONObject out = new JSONObject();
        JsonFactory factory = new JsonFactory();
        JsonParser jp = factory.createParser(rawJson);
        JsonToken current = jp.nextToken();
        String field = null;
        JSONArray sig = new JSONArray();

        if (current != JsonToken.START_OBJECT) {
            return null;
        }
        while((current = jp.nextToken()) != JsonToken.END_OBJECT) {
            if (current == JsonToken.FIELD_NAME) {
                field = jp.getCurrentName();
                if (!"data".equals(field)) {
                    jp.nextToken();
                    jp.skipChildren();
                } else {
                    jp.nextToken(); // data: {
                    while((current = jp.nextToken()) != JsonToken.END_OBJECT) {
                        if (current == JsonToken.FIELD_NAME) {
                            field = jp.getCurrentName();
                            if (!"signature".equals(field)) {
                                jp.nextToken();
                                jp.skipChildren();
                            } else {
                                while((current = jp.nextToken()) != JsonToken.END_ARRAY) { // signature: [
                                    JSONObject jo = new JSONObject();
                                    while((current = jp.nextToken()) != JsonToken.END_OBJECT) { // {
                                        if (current == JsonToken.FIELD_NAME) {
                                            field = jp.getCurrentName();
                                            int val = jp.nextIntValue(0);
                                            jo.put(field,val);
                                        }
                                    }
                                    sig.put(jo);
                                }

                            }
                        }
                    }
                }
            }
        }
        out.put("signature",sig);
        return out;
    }

    public JSONObject getSignatureJson() {
        return json;
    }
}
