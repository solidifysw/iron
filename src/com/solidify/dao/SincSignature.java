package com.solidify.dao;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.solidify.admin.reports.Utils;
import com.solidify.utils.ParsedObject;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.*;
import java.util.HashSet;

/**
 * Created by jrobins on 2/18/15.
 */
public class SincSignature {
    private String orderId;
    private JSONObject json;
    private Connection con;
    private boolean manageConnection;

    public SincSignature(String orderId, Connection con) throws IOException, SQLException {
        this.orderId = orderId;
        this.con = con;
        this.manageConnection = con == null ? true : false;
        load();
    }

    private void load() throws SQLException, IOException {
        try {
            if (manageConnection) {
                con = Utils.getConnection();
            }
            PreparedStatement select = con.prepareStatement("SELECT data FROM sinc.orders WHERE id = ?");
            select.setString(1,orderId);
            ResultSet rs = select.executeQuery();
            String rawJson = null;
            if (rs.next()) {
                rawJson = rs.getString("data");
            }
            rs.close();
            select.close();
            if (rawJson != null) {
                HashSet<String> incs = new HashSet<>();
                incs.add("data.signature");
                ParsedObject po = new ParsedObject(rawJson,incs,ParsedObject.INCLUDE);
                this.json = po.get();
            }

        } finally {
            if (manageConnection && con != null) con.close();
        }
    }

    /*
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
    */

    public JSONObject getSignatureJson() {
        return json;
    }
}
