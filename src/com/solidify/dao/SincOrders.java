package com.solidify.dao;

import com.solidify.admin.reports.Utils;
import com.solidify.utils.ParsedObject;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.*;
import java.util.*;

/**
 * Created by jennifermac on 2/25/15.
 */
public class SincOrders {

    private ArrayList<JSONObject> orders;
    private Connection con;
    private boolean manageConnection;
    private String groupId;

    public SincOrders(String groupId, Connection con) throws Exception {
        this.groupId = groupId;
        this.con = con;
        this.manageConnection = con == null ? true : false;
        load();
    }

    public SincOrders(String groupId) throws Exception {
        this(groupId, null);
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
        skips.add("keepCoverage");
        skips.add("prePostTaxSelections");
        skips.add("imported");
        skips.add("current");
        skips.add("data.signature");
        skips.add("data.member.tags");
        skips.add("data.member.rehireEligible");
        skips.add("data.member.terminationNotes");
        skips.add("data.member.payrollId");
        skips.add("data.member.carrierData");
        skips.add("data.member.personal.dependents");
        skips.add("data.member.personal.emergencyContacts");
        skips.add("data.member.personal.beneficiaries");

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
        sql = "SELECT data FROM sinc.orders WHERE id = ?";
        select = con.prepareStatement(sql);
        for (Iterator<String> it = orderIds.iterator(); it.hasNext();) {
            String orderId = it.next();
            select.setString(1,orderId);
            orderRes = select.executeQuery();
            if (orderRes.next()) {
                cnt++;
                json = orderRes.getString("data");
            }
            orderRes.close();

            JSONObject slimOrder = null;
            ParsedObject po = new ParsedObject(json,skips, ParsedObject.SKIP);
            slimOrder = po.get();

            if (slimOrder != null && slimOrder.has("data")) {
                JSONObject data = slimOrder.getJSONObject("data");
                if (data.has("member")) {
                    JSONObject member = data.getJSONObject("member");
                    if (member.has("testUser") && member.getString("testUser").equalsIgnoreCase("no")) {
                        // populate covs
                        groupOrders.put(slimOrder);
                    }
                }
            }
        }
        select.close();

        // groupOrders contains the slim versions of the orders for this group
        // Find the latest order for each individual
        if (groupOrders.length() > 0) {
            orders = findLatestOrders(groupOrders);
        }
    }

    public void populateCovs(JSONObject slimOrder) {
        HashSet<String> ignores = new HashSet<>();
        ignores.add("member");
        ignores.add("beneficiaries");
        ignores.add("dependents");
        ignores.add("emergencyContacts");
        JSONArray covs = new JSONArray();

        if (slimOrder.has("data")) {
            JSONObject data = slimOrder.getJSONObject("data");

            for (Iterator<Object> it = data.keys(); it.hasNext();) {
                String key = (String)it.next();
                if (!ignores.contains(key)) {
                    JSONObject cov = data.getJSONObject(key);
                    covs.put(cov);
                }
            }
        }
        slimOrder.put("covs",covs);
    }

    /*
     * Finds the class for this memberId.  enrollment.classes may be the old style that has the list of memberId's in
     * the members object inside the classes array. old style classes:[{...},{...}]
     * new style classes is an array of classIds.  new style classes:['a','b'...] Have to query the classes table to
     * find the members.
     * @param classes
     * @param memberId
     * @param con
     * @return
     * @throws SQLException

    public String processClasses(JSONArray classes, String memberId, Connection con) throws SQLException {
        String out = null;
        boolean found = false;
        for (int i = 0; i < classes.length(); i++) {
            if (found)  break;
            Object tmp = classes.get(i);
            if (tmp.getClass().equals(String.class)) {
                out = getClassVal(classes,memberId,con);
            } else if (tmp.getClass().equals(JSONObject.class)) {
                JSONObject jo = classes.getJSONObject(i);
                JSONArray mems = jo.getJSONArray("members");
                for (int j=0; j<mems.length(); j++) {
                    if (mems.getString(i).equals(memberId)) {
                        out = jo.getString("id");
                        found = true;
                        break;
                    }
                }
            }
        }
        return out;
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
     */
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
