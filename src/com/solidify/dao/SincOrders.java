package com.solidify.dao;

import com.solidify.admin.reports.Utils;
import com.solidify.utils.ParsedObject;
import com.solidify.utils.Skip;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.*;
import java.util.*;

/**
 * Created by jrobins on 2/25/15.
 */
public class SincOrders {

    private ArrayList<JSONObject> orders;
    private Connection con;
    private boolean manageConnection;
    private String groupId;
    private boolean printToConsole;
    public static final boolean PRINT = true;
    public static final boolean DO_NOT_PRINT = false;

    public SincOrders(String groupId, boolean printToConsole, Connection con) throws Exception {
        this.groupId = groupId;
        this.printToConsole = printToConsole;
        this.con = con;
        this.manageConnection = con == null ? true : false;
        load();
    }
    public SincOrders(String groupId, Connection con) throws Exception {
        this(groupId,DO_NOT_PRINT,con);
    }

    public SincOrders(String groupId) throws Exception {
        this(groupId, DO_NOT_PRINT, null);
    }

    private void load() throws Exception {
        PreparedStatement select = null;
        ResultSet rs = null;
        PreparedStatement idSelect = null;
        ResultSet oIds = null;
        Statement stmt1 = null;
        ResultSet orderRes = null;

        HashSet<String> orderIds = new HashSet<String>();
        JSONArray groupOrders = new JSONArray();
        String sql = null;

        if (manageConnection) con = Utils.getConnection();
        // Get the list of orderId's for this group
        sql = "SELECT id FROM sinc.orders WHERE completed = 1 AND deleted = 0 AND type != 'IMPORTED' AND groupId = ?";

        idSelect = con.prepareStatement(sql);
        idSelect.setString(1, groupId);
        oIds = idSelect.executeQuery();
        while (oIds.next()) {
            orderIds.add(oIds.getString("id"));
        }
        oIds.close();
        idSelect.close();
        // query each order and parse the data blob for pertinent info
        String json = null;
        int cnt = 0;
        for (Iterator<String> it = orderIds.iterator(); it.hasNext();) {
            String orderId = it.next();
            SincOrder so = new SincOrder(orderId, printToConsole, con);
            groupOrders.put(so.getOrder());
        }

        // groupOrders contains the slim versions of the orders for this group
        // Find the latest order for each individual
        if (groupOrders.length() > 0) {
            orders = findLatestOrders(groupOrders);
        }
    }

    public static ArrayList<JSONObject> findLatestOrders(JSONArray groupOrders) {
        ArrayList<JSONObject> out = new ArrayList<JSONObject>();
        if (groupOrders.length() > 0) {
            JSONObject latest = null;
            HashSet<Integer> matched = new HashSet<Integer>();
            for (int i=0; i<groupOrders.length(); i++) {
                if (matched.contains(i)) {
                    continue;
                }
                JSONObject obj1 = (JSONObject) groupOrders.get(i);
                latest = obj1;
                if (i < groupOrders.length() - 1) {
                    for (int j = i + 1; j < groupOrders.length(); j++) {
                        if (!matched.contains(new Integer(j))) {
                            JSONObject obj2 = (JSONObject) groupOrders.get(j);
                            if (sameMember(obj1, obj2)) {
                                matched.add(new Integer(j));
                                latest = laterOf(latest, obj2);
                            } else {

                            }
                        }
                    }
                }
                out.add(latest);
            }
        }
        return out;
    }

    /**
     * Compares the 2 json objects for the ssn field and returns true if they are equal
     * @param obj1
     * @param obj2
     * @return
     */
    public static boolean sameMember(JSONObject obj1, JSONObject obj2) {
        JSONObject data1 = obj1.getJSONObject("data");
        JSONObject member1 = data1.getJSONObject("member");
        JSONObject personal1 = member1.getJSONObject("personal");

        String ssn1 = personal1.getString("ssn");
        if (ssn1 == null) {
            ssn1 = "";
        }
        String dob1 = personal1.getString("dateOfBirth");
        if (dob1 == null) {
            dob1 = "";
        }

        JSONObject data2 = obj2.getJSONObject("data");
        JSONObject member2 = data2.getJSONObject("member");
        JSONObject personal2 = member2.getJSONObject("personal");
        String ssn2 = personal2.getString("ssn");
        if (ssn2 == null) {
            ssn2 = "";
        }
        String dob2 = personal2.getString("dateOfBirth");
        if (dob2 == null) {
            dob2 = "";
        }

        if (!"".equals(ssn1) && !"".equals(ssn2) && !"".equals(dob1) && !"".equals(dob2)) {
            return ssn1.equals(ssn2) && dob1.equals(dob2);
        } else {
            return false;
        }
    }

    /**
     * Compares the dateSaved fields in the 2 objects and returns the one with the later dateSaved date
     * @param obj1
     * @param obj2
     * @return the later dateSaved of the 2 objects
     */
    public static JSONObject laterOf(JSONObject obj1, JSONObject obj2) {
        Long saved1 = (Long)obj1.getLong("dateSaved");
        Long saved2 = (Long)obj2.getLong("dateSaved");
        java.util.Date dt1 = new java.util.Date(saved1);
        java.util.Date dt2 = new java.util.Date(saved2);
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(dt1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(dt2);
        if (cal1.compareTo(cal2) >= 0) {
            return obj1;
        } else {
            return obj2;
        }
    }

    public ArrayList<JSONObject> getOrders() {
        return orders;
    }

    public String getGroupId() {
        return groupId;
    }
}
