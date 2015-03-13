package com.solidify.dao;

import com.solidify.utils.ParsedObject;
import com.solidify.utils.Skip;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Created by jr1 on 3/13/15.
 */
public class SincOrder {

    private String id;
    private JSONObject order;
    private Connection con;
    private HashSet<String> skips;
    private boolean testUser;
    private boolean printToConsole;
    public static final boolean PRINT = true;
    public static final boolean DO_NOT_PRINT = false;

    public SincOrder(String id, boolean printToConsole, Connection con) throws SQLException {
        this.id = id;
        this.con = con;
        this.skips = new HashSet<>();
        this.skips.add("member"); skips.add("enrollment");
        this.skips.add("keepCoverage");
        this.skips.add("prePostTaxSelections");
        this.skips.add("imported");
        this.skips.add("current");
        this.skips.add("data.signature");
        this.skips.add("data.member.tags");
        this.skips.add("data.member.rehireEligible");
        this.skips.add("data.member.terminationNotes");
        this.skips.add("data.member.payrollId");
        this.skips.add("data.member.carrierData");
        this.skips.add("data.member.personal.dependents");
        this.skips.add("data.member.personal.emergencyContacts");
        this.skips.add("data.member.personal.beneficiaries");
        this.testUser = false;
        this.printToConsole = printToConsole;
        load();
    }
    public SincOrder(String id, Connection con) throws SQLException {
        this(id,DO_NOT_PRINT,con);
    }

    private void load() throws SQLException {
        String json = null;
        String sql = "SELECT data FROM sinc.orders WHERE id = ?";
        PreparedStatement select = con.prepareStatement(sql);
        select.setString(1,id);
        ResultSet rs = select.executeQuery();
        if (rs.next()) {
            json = rs.getString("data");
        }
        rs.close();
        select.close();
        if (printToConsole) {
            System.out.println(json);
        }
        ParsedObject po = new ParsedObject(json, skips, new Skip());
        order = po.get();

        if (order != null && order.has("data")) {
            JSONObject data = order.getJSONObject("data");
            if (data.has("member")) {
                JSONObject member = data.getJSONObject("member");
                if (member.has("testUser") && member.getString("testUser").equalsIgnoreCase("yes")) {
                    testUser = true;
                } else {
                    populateCovs(order);
                }
            }
            if (printToConsole) {
                System.out.println(order.toString());
                System.out.println("--------------------------");
            }
        }
    }

    public void populateCovs(JSONObject slimOrder) {
        HashSet<String> ignores = new HashSet<>();
        ignores.add("member");
        ignores.add("beneficiaries");
        ignores.add("dependents");
        ignores.add("emergencyContacts");
        ignores.add("version");
        JSONArray covs = new JSONArray();
        JSONObject declineReasons = new JSONObject();

        if (slimOrder.has("declineReasons")) {
            declineReasons = slimOrder.getJSONObject("declineReasons");
        }

        if (slimOrder.has("data")) {
            JSONObject data = slimOrder.getJSONObject("data");
            JSONArray bens = new JSONArray();
            if (data.has("beneficiaries")) {
                bens = data.getJSONArray("beneficiaries");
            }

            for (Iterator<Object> it = data.keys(); it.hasNext();) {
                String key = (String)it.next();
                if (!ignores.contains(key)) {
                    JSONObject cov = data.getJSONObject(key);
                    // if declined, look for a decline reason and set it in the cov object
                    if (cov.getString("benefit").equalsIgnoreCase("decline") && declineReasons.has(key)) {
                        cov.put("declineReason", declineReasons.getString(key));
                    }
                    JSONArray newBens = null;
                    if (cov.has("beneficiaries")) {
                        newBens = new JSONArray();
                        JSONArray covBens = cov.getJSONArray("beneficiaries");

                        for (int i=0; i<covBens.length(); i++) {
                            JSONObject ben = covBens.getJSONObject(i);
                            //System.out.println(i+": "+ben.toString());
                            String beneficiaryId = ben.getString("beneficiaryId");
                            for (int j=0; j<bens.length(); j++) {
                                JSONObject b = bens.getJSONObject(j);
                                //System.out.println(b.toString());
                                if (beneficiaryId.equals(b.getString("id"))) {
                                    ben.put("firstName",b.getString("firstName"));
                                    ben.put("lastName",b.getString("lastName"));
                                    ben.put("relationship",b.getString("relationship"));
                                    ben.put("address1",b.getString("address1"));
                                    ben.put("address2",b.getString("address2"));
                                    ben.put("city",b.getString("city"));
                                    ben.put("state",b.getString("state"));
                                    ben.put("zip",b.getString("zip"));
                                    newBens.put(ben);
                                    break;
                                }
                            }
                        }
                        cov.remove("beneficiaries");
                        cov.put("beneficiaries",newBens);
                    }
                    covs.put(cov);
                    it.remove();
                }
            }
        }
        slimOrder.put("covs", covs);
    }

    public JSONObject getOrder() {
        return order;
    }
}
