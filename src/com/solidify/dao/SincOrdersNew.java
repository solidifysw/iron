package com.solidify.dao;

import com.solidify.admin.reports.Utils;
import com.solidify.utils.ParsedObject;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Created by jennifermac on 2/25/15.
 */
public class SincOrdersNew {

    private ArrayList<JSONObject> orders;
    private Connection con;
    private boolean manageConnection;
    private String groupId;

    public SincOrdersNew(String groupId, Connection con) throws Exception {
        this.groupId = groupId;
        this.con = con;
        this.manageConnection = con == null ? true : false;
        load();
    }

    public SincOrdersNew(String groupId) throws Exception {
        this(groupId,null);
    }

    private void load() throws Exception {
        PreparedStatement select = null;
        ResultSet rs = null;
        PreparedStatement idSelect = null;
        ResultSet oIds = null;
        Statement stmt1 = null;
        ResultSet orderRes = null;
        HashSet<String> skips = new HashSet<>();
        skips.add("member"); skips.add("enrollment");

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
        int cnt = 0;
        for (Iterator<String> it = orderIds.iterator(); it.hasNext();) {
            String orderId = it.next();
            stmt1 = con.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,java.sql.ResultSet.CONCUR_READ_ONLY);
            stmt1.setFetchSize(Integer.MIN_VALUE);
            orderRes = stmt1.executeQuery("SELECT data,memberId FROM sinc.orders WHERE id ='"+orderId+"'");
            byte[] bdata = null;
            if (orderRes.next()) {
                cnt++;
                Blob b = orderRes.getBlob("data");
                bdata = b.getBytes(1, (int) b.length());
            }
            orderRes.close();
            stmt1.close();

            JSONObject slimOrder = null;
            try {

                ParsedObject po = new ParsedObject(new String(bdata,"UTF-8"),skips,ParsedObject.SKIP);
                slimOrder = po.get();
                if (slimOrder != null && slimOrder.get("testUser").equals("NO")) {
                    JSONObject enrollment = parseEnrollment(new String(bdata,"UTF-8"));
                    JSONArray classes = enrollment.getJSONArray("classes");
                    String cls = getClassVal(classes, slimOrder.getString("memberId"), con);
                    if (cls != null) {
                        slimOrder.put("class",cls);
                    }
                    groupOrders.put(slimOrder);
                }
            } catch (Exception e) {
                String tmp = new String(bdata);
                e.printStackTrace();
            }

        }

        // groupOrders contains the slim versions of the orders for this group
        // Find the latest order for each individual
        if (groupOrders.length() > 0) {
            orders = findLatestOrders(groupOrders);
        }
    }

    public String getClassVal(JSONArray classes, String memberId, Connection con) throws SQLException {
        String classVal = null;
        boolean found = false;
        String sql = "SELECT data FROM sinc.classes WHERE deleted = 0 AND id = ?";
        PreparedStatement select = con.prepareStatement(sql);
        ResultSet rs = null;

        for (int i=0; i<classes.length(); i++) {
            if (found) {
                break;
            }
            JSONObject cls = null;
            String classId = classes.getString(i);
            select.setString(1,classId);
            rs = select.executeQuery();
            if (rs.next()) {
                cls = new JSONObject(rs.getString("data"));
            }
            rs.close();
            if (cls == null) {
                continue;
            }
            JSONArray members = cls.getJSONArray("members");
            for (int j=0; j<members.length(); j++) {
                if (members.getString(j).equals(memberId)) {
                    classVal = cls.getString("name");
                    found = true;
                    break;
                }
            }
        }
        select.close();
        return classVal;
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
        String ssn1 = obj1.getString("ssn");
        if (ssn1 == null) {
            ssn1 = "";
        }
        String dob1 = obj1.getString("dateOfBirth");
        if (dob1 == null) {
            dob1 = "";
        }

        String ssn2 = obj2.getString("ssn");
        if (ssn2 == null) {
            ssn2 = "";
        }
        String dob2 = obj2.getString("dateOfBirth");
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
        Long saved1 = (Long)obj1.get("dateSaved");
        Long saved2 = (Long)obj2.get("dateSaved");
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
