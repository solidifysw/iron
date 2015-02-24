package com.solidify.dao;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.solidify.admin.reports.Utils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by jrobins on 2/24/15.
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
        this(groupId,null);
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
                slimOrder = buildObject(bdata);
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

    public static JSONObject buildObject(byte[] blob) throws IOException, SQLException {
        JSONObject order = new JSONObject();
        JsonFactory factory = new JsonFactory();
        JsonParser jp = factory.createParser(new String(blob, "UTF-8"));
        HashSet<String> skip = new HashSet<String>();
        //skip.add("declineReasons");
        skip.add("keepCoverage"); skip.add("disclosureQuestions"); skip.add("prePostTaxSelections"); skip.add("enrollment"); skip.add("imported"); skip.add("current"); skip.add("usedDefinedContributions");
        skip.add("lifeChangeTypes"); skip.add("member");

        JsonToken current = null;
        HashMap<String,String> declineReasons = null;
        String field = null;
        // start of json {

        current = jp.nextToken();
        if (current != JsonToken.START_OBJECT) {
            return null;
        }
        while((current = jp.nextToken()) != JsonToken.END_OBJECT) {
            if (current == JsonToken.FIELD_NAME) {
                field = jp.getCurrentName();
                if ("data".equals(field)) {
                    current = jp.nextToken();
                    buildCovs(order,jp);
                } else if ("dateSaved".equals(field)) {
                    //log.info("found dateSaved");
                    jp.nextToken();
                    Long dateSaved = jp.getValueAsLong();
                    order.put("dateSaved", dateSaved);
                    SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
                    long dateSavedL = dateSaved.longValue();
                    java.util.Date dt = new java.util.Date(dateSavedL);
                    String date = df.format(dt);
                    order.put("date",date);
                } else if (skip.contains(field)) {
                    jp.nextToken();
                    jp.skipChildren();
                } else if ("declineReasons".equals(field)) {
                    declineReasons = getDeclineReasons(order,jp);
                } else if ("isBatchable".equals(field)) {
                    jp.nextToken();
                    order.put("isBatchable",jp.getValueAsBoolean());
                } else if ("questionAnswers".equals(field)) {
                    getQuestionAnswers(order,jp);
                } else if ("userName".equals(field)) {
                    jp.nextToken();
                    order.put("enroller",jp.getValueAsString());
                } else if ("id".equals(field)) {
                    jp.nextToken();
                    order.put("orderId",jp.getValueAsString());
                }
            }
        }

        if (declineReasons != null && !declineReasons.isEmpty()) {
            JSONArray covs = (JSONArray) order.get("covs");
            for (int i = 0; i < covs.length(); i++) {
                JSONObject cov = (JSONObject) covs.get(i);
                // log.info(cov.toString());
                String covUuid = (String) cov.get("splitId");
                if (declineReasons.containsKey(covUuid)) {
                    cov.put("declineReason", declineReasons.get(covUuid));
                }
            }
        }

        return order;
    }

    private static void buildCovs(JSONObject order, JsonParser jp) throws JsonParseException, IOException {
        String field;
        JsonToken current = null;
        JSONObject cov = null;
        JSONObject premium = null;
        JSONArray covs = new JSONArray();
        HashSet<String> skips = new HashSet<String>();
        skips.add("emergencyContacts"); skips.add("signature"); skips.add("listBillAdjustments"); skips.add("carrierElectionData");
        HashSet<String> saves = new HashSet<String>();
        saves.add("productId"); saves.add("planName"); saves.add("startDate"); saves.add("benefit"); saves.add("endDate"); saves.add("type"); saves.add("subType"); saves.add("deduction"); saves.add("totalYearly"); saves.add("electionTier"); saves.add("splitId");
        saves.add("benefitLevel");

        while ((current = jp.nextToken()) != JsonToken.END_OBJECT) {
            if (current == JsonToken.FIELD_NAME) {
                field = jp.getCurrentName();
                //log.info("field: "+field);
                if (field.equals("member")) {
                    buildMember(order, jp);
                } else if (skips.contains(field)) {
                    // skip all this crap
                    current = jp.nextToken();
                    if (current == JsonToken.START_ARRAY || current == JsonToken.START_OBJECT) {
                        jp.skipChildren();
                    }
                } else if (field.equals("dependents")) {
                    buildDependents(order, jp);
                } else if ("beneficiaries".equals(field)) {
                    buildBeneficiaries(order,jp);
                }  else {
                    // these are coverages
                    cov = new JSONObject();
                    JSONArray prems;
                    cov.put("id", field);
                    jp.nextToken(); // open tag uuid : {
                    while((current = jp.nextToken()) != JsonToken.END_OBJECT) {
                        if (current == JsonToken.FIELD_NAME) {
                            field = jp.getCurrentName();
                            if (saves.contains(field)) {
                                current = jp.nextToken();
                                cov.put(field, jp.getValueAsString());
                            } else if ("premiums".equals(field)) {
                                prems = new JSONArray();
                                while((current = jp.nextToken()) != JsonToken.END_ARRAY) {
                                    premium = new JSONObject();
                                    while((current = jp.nextToken()) != JsonToken.END_OBJECT) {
                                        if (current == JsonToken.FIELD_NAME) {
                                            field = jp.getCurrentName();
                                            current = jp.nextToken();
                                            String fVal = jp.getValueAsString();
                                            premium.put(field, fVal);
                                        }
                                    }
                                    prems.put(premium);
                                }
                                if (prems.length() > 0) {
                                    cov.put("premiums",prems);
                                }
                            } else if ("beneficiaries".equals(field)) {
                                JSONArray bens = new JSONArray();
                                while((current = jp.nextToken()) != JsonToken.END_ARRAY) {
                                    JSONObject ben = new JSONObject();
                                    current = jp.nextToken();
                                    if (current == JsonToken.END_ARRAY) {
                                        break;
                                    }
                                    while (current != JsonToken.END_OBJECT) {
                                        if (current == JsonToken.FIELD_NAME) {
                                            field = jp.getCurrentName();
                                            current = jp.nextToken();
                                            if (current.isNumeric()) {
                                                ben.put(field, jp.getValueAsInt());
                                            } else {
                                                ben.put(field, jp.getValueAsString());
                                            }
                                        }
                                        current = jp.nextToken();
                                    }
                                    bens.put(ben);
                                }
                                if (bens.length()>0) {
                                    cov.put("beneficiaries", bens);
                                }
                            } else if (field.equals("coveredDependents")) {
                                JSONArray coveredDeps = new JSONArray();
                                while((current = jp.nextToken()) != JsonToken.END_ARRAY) {
                                    if (current != JsonToken.START_ARRAY) {
                                        coveredDeps.put(jp.getValueAsString());
                                    }
                                }
                                cov.put("coveredDependents",coveredDeps);
                            } else if (skips.contains(field)) {
                                current = jp.nextToken();
                                if (current == JsonToken.START_ARRAY || current == JsonToken.START_OBJECT) {
                                    jp.skipChildren();
                                }
                            }
                        }
                    }
                    covs.put(cov);
                }
            }
        }
        order.put("covs", covs);
    }

    public static void buildBeneficiaries(JSONObject order, JsonParser jp) throws JsonParseException, IOException {
        JsonToken current = null;
        String field = null;
        JSONArray bens = new JSONArray();
        current = jp.nextToken();
        if (current == JsonToken.START_ARRAY) {
            while (current != JsonToken.END_ARRAY) {
                current = jp.nextToken();
                if (current == JsonToken.START_OBJECT) {
                    JSONObject ben = new JSONObject();
                    while (current != JsonToken.END_OBJECT) {
                        current = jp.nextToken();
                        if (current == JsonToken.FIELD_NAME) {
                            field = jp.getCurrentName();
                            jp.nextToken();
                            ben.put(field, jp.getValueAsString());
                        }
                    }
                    bens.put(ben);
                }
            }
        }
        order.put("beneficiaries",bens);
    }

    public static void buildDependents(JSONObject order, JsonParser jp) throws JsonParseException, IOException {
        JsonToken current = null;
        String field = null;
        JSONArray deps = new JSONArray();
        current = jp.nextToken();
        if (current == JsonToken.START_ARRAY) {
            while (current != JsonToken.END_ARRAY) {
                current = jp.nextToken();
                if (current == JsonToken.START_OBJECT) {
                    JSONObject dep = new JSONObject();
                    while (current != JsonToken.END_OBJECT) {
                        current = jp.nextToken();
                        if (current == JsonToken.FIELD_NAME) {
                            field = jp.getCurrentName();
                            if (field.equals("deleted") && !jp.getValueAsBoolean()) {
                                dep.put("deleted", false);
                            } else if (field.equals("deleted")) {
                                dep.put("deleted", true);
                            } else {
                                jp.nextToken();
                                dep.put(field, jp.getValueAsString());
                            }
                        }
                    }
                    deps.put(dep);
                }
            }
        }
        if (order.has("dependents")) {
            JSONArray dps = order.getJSONArray("dependents");
            if (dps.length() == 0) {
                order.put("dependents", deps);
            }
        } else {
            order.put("dependents", deps);
        }
    }

    /**
     * Parses out the member information from the order data blob and places relevant data into a slimmed down order blob.
     * member: {employeeId:"123", dependents:[],...personal:{firstName:"John",dateOfBirth:"03/10/1962",..}}
     * @param order the object to populate
     * @param jp
     * @throws JsonParseException
     * @throws IOException
     */
    private static void buildMember(JSONObject order, JsonParser jp) throws JsonParseException, IOException {
        String field = null;
        JsonToken current = null;
        HashSet<String> info = new HashSet();
        info.add("occupation"); info.add("locationCode"); info.add("locationDescription"); info.add("occupation"); info.add("status"); info.add("deductionsPerYear");
        info.add("department"); info.add("dateOfHire"); info.add("hoursPerWeek"); info.add("annualSalary"); info.add("employeeId"); info.add("id");
        HashSet<String> personal = new HashSet();
        personal.add("firstName"); personal.add("lastName"); personal.add("dateOfBirth"); personal.add("ssn"); personal.add("gender");
        personal.add("address1"); personal.add("address2"); personal.add("city"); personal.add("state"); personal.add("zip"); personal.add("phone");
        personal.add("email");
        HashSet<String> skips = new HashSet();
        skips.add("emergencyContacts"); skips.add("beneficiaries"); skips.add("dependents");
        JSONArray deps = new JSONArray();

        if (jp.nextToken() == JsonToken.START_OBJECT) {
            while((current = jp.nextToken()) != JsonToken.END_OBJECT) {
                if(current == JsonToken.FIELD_NAME) {
                    field = jp.getCurrentName();
                    if (skips.contains(field)) {
                        jp.nextToken();
                        jp.skipChildren();
                    } else if (field.equals("personal")) {
                        while((current = jp.nextToken()) != JsonToken.END_OBJECT) {
                            if (current == JsonToken.FIELD_NAME) {
                                field = jp.getCurrentName();
                                if (personal.contains(field)) {
                                    current = jp.nextToken();
                                    order.put(field, jp.getValueAsString());
                                } else if (skips.contains(field)) {
                                    current = jp.nextToken();
                                    if (current == JsonToken.START_ARRAY || current == JsonToken.START_OBJECT) {
                                        jp.skipChildren();
                                    }
                                }
                            }
                        }
                    } else if ("tags".equals(field) || "newHire".equals(field)) {
                        current = jp.nextToken();
                        if (current == JsonToken.START_ARRAY || current == JsonToken.START_OBJECT) {
                            jp.skipChildren();
                        }
                    } else if ("testUser".equals(field)) {
                        current = jp.nextToken();
                        order.put("testUser", jp.getValueAsString());
                    }  else if (info.contains(field)) {
                        current = jp.nextToken();
                        if ("id".equals(field)) {
                            order.put("memberId",jp.getValueAsString());
                        } else {
                            order.put(field, jp.getValueAsString());
                        }
                    }
                }
            }
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

    private static void getQuestionAnswers(JSONObject order, JsonParser jp)  throws JsonParseException, IOException {
        // questionAnswers: { weight-ee: {id:xxx,questionText:xxx...},weight-sp:...}
        JSONObject answers = new JSONObject();
        JsonToken current = null;
        String field = null;
        String objectName = null;
        if ((current = jp.nextToken()) == JsonToken.START_OBJECT) {
            while((current = jp.nextToken()) != JsonToken.END_OBJECT) {
                if (current == JsonToken.FIELD_NAME) {
                    objectName = jp.getCurrentName();
                    JSONObject jo = new JSONObject();
                    while((current = jp.nextToken()) != JsonToken.END_OBJECT) {
                        if (current == JsonToken.FIELD_NAME) {
                            field = jp.getCurrentName();
                            jp.nextToken();
                            try {
                                jo.put(field, jp.getValueAsString());
                            } catch (Exception e) {
                                jo.put(field, jp.getValueAsInt());
                            }
                        }
                    }
                    answers.put(objectName,jo);
                }
            }
        }
        Set set = answers.keySet();
        if (!set.isEmpty()) {
            order.put("questionAnswers", answers);
        }
    }

    private static HashMap<String,String> getDeclineReasons(JSONObject order, JsonParser jp) throws JsonParseException, IOException {
        // declineReasons: {UUID1:reason1,UUID2:reason2...}
        HashMap<String,String> declineReasons = new HashMap<String,String>();
        if (jp.nextToken() == JsonToken.START_OBJECT) {
            while (jp.nextToken() != JsonToken.END_OBJECT) {
                String covUuid = jp.getCurrentName();
                jp.nextToken();
                String declineReason = jp.getValueAsString();
                declineReasons.put(covUuid, declineReason);
            }
        }
        return declineReasons;
    }

    public static JSONObject parseEnrollment(String json) throws IOException {
        JSONObject out = new JSONObject();
        JsonFactory factory = new JsonFactory();
        JsonParser jp = factory.createParser(json);
        JsonToken current = jp.nextToken();
        String field = null;
        HashSet<String> skip = new HashSet<String>();
        skip.add("packages"); skip.add("employer"); skip.add("loginScheme");
        JSONArray productConfigs = new JSONArray();
        JSONArray classes = new JSONArray();

        // make sure there is a {
        if (current != JsonToken.START_OBJECT) {
            return null;
        }

        while((current = jp.nextToken()) != JsonToken.END_OBJECT) {
            if (current == JsonToken.FIELD_NAME) {
                field = jp.getCurrentName();
                if (field.equals("enrollment")) {
                    while ((current = jp.nextToken()) != JsonToken.END_OBJECT) {
                        if (current == JsonToken.FIELD_NAME) {
                            field = jp.getCurrentName();
                            if (field.equals("classes")) {
                                current = jp.nextToken();
                                if (current == JsonToken.START_ARRAY) {
                                    while((current = jp.nextToken()) != JsonToken.END_ARRAY) {
                                        classes.put(jp.getValueAsString());
                                    }
                                }
                            } else if (field.equals("productConfigs")) {
                                current = jp.nextToken();
                                if (current == JsonToken.START_ARRAY) {
                                    while ((current = jp.nextToken()) != JsonToken.END_ARRAY) {
                                        productConfigs.put(jp.getValueAsString());
                                    }
                                }
                            } else {
                                current = jp.nextToken();
                                if (current == JsonToken.START_OBJECT || current == JsonToken.START_ARRAY) {
                                    jp.skipChildren();
                                }
                            }
                        }
                    }
                } else if ((current = jp.nextToken()) == JsonToken.START_OBJECT || current == JsonToken.START_ARRAY) {
                    jp.skipChildren();
                }
            }
        }
        out .put("classes",classes);
        out.put("productConfigs",productConfigs);
        return out;
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
