package com.solidify.dao;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.solidify.admin.reports.Utils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by jrobins on 1/12/15.
 */
public class SignatureOld {
    private static final Logger log = LogManager.getLogger();
    private final String orderId;
    private JSONObject dataPoints = new JSONObject();

    public SignatureOld(String orderId) {
        this.orderId = orderId;
        load();
    }

    public JSONObject getDataPoints() {
        return dataPoints;
    }

    private void load() {
        Connection con = null;
        PreparedStatement select = null;
        ResultSet rs = null;
        JSONObject slimOrder = null;
        try {
            con = Utils.getConnection();

            String sql = "SELECT data FROM sinc.orders WHERE id = ?";
            select = con.prepareStatement(sql);
            select .setString(1, orderId);
            rs = select.executeQuery();
            if (rs.next()) {
                Blob b = rs.getBlob("data");
                byte[] bdata = b.getBytes(1, (int) b.length());
                parse(bdata);
            }
            rs.close();
            select.close();
        } catch (Exception e) {
            log.error("error",e);
        } finally {
            try {
                con.close();
            } catch (Exception e) {}
            try {
                select.close();
            } catch (Exception e) {}
            try {
                rs.close();
            } catch (Exception e) {}
        }
    }

    private void parse(byte[] bdata) {
        JsonFactory factory = new JsonFactory();
        JsonToken current = null;
        String field = null;
        try {
            JSONArray ja = new JSONArray();
            String tmp = new String(bdata,"UTF-8");
            JsonParser jp = factory.createParser(tmp);
            current = jp.nextToken();
            if (current != JsonToken.START_OBJECT) {
                log.info("error parsing");
                return;
            }
            // signature is in the data object
            while((current = jp.nextToken()) != JsonToken.END_OBJECT) {
                if (current == JsonToken.FIELD_NAME) {
                    field = jp.getCurrentName();
                    if ("data".equals(field)) {
                        // parse for signature
                        while ((current = jp.nextToken()) != JsonToken.END_OBJECT) {
                            if (current == JsonToken.FIELD_NAME) {
                                field = jp.getCurrentName();
                                // signature:[ {lx,ly,mx,my},{lx,ly,mx,my}...]
                                if ("signature".equals(field)) {
                                    if (jp.nextToken() == JsonToken.START_ARRAY) {
                                        while((current = jp.nextToken()) != JsonToken.END_ARRAY) {
                                            if (current == JsonToken.START_OBJECT) {
                                                JSONObject jo = new JSONObject();
                                                while((current = jp.nextToken()) != JsonToken.END_OBJECT) {
                                                    jo.put(jp.getCurrentName(),jp.getValueAsInt());
                                                }
                                                ja.put(jo);
                                            }
                                        }
                                    }

                                } else {
                                    // skip all this crap
                                    current = jp.nextToken();
                                    if (current == JsonToken.START_ARRAY || current == JsonToken.START_OBJECT) {
                                        jp.skipChildren();
                                    }
                                }
                            }
                        }

                    } else {
                        current = jp.nextToken();
                        if (current == JsonToken.START_ARRAY || current == JsonToken.START_OBJECT) {
                            jp.skipChildren();
                        }
                    }
                }
            }
            dataPoints = new JSONObject();
            dataPoints.put("signature",ja);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
