package com.solidify.utils;

import com.solidify.admin.reports.Utils;
import com.solidify.dao.*;
import com.solidify.exceptions.NoValue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.annotation.WebServlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Created by jrobins on 2/5/15.
 */
@WebServlet("/utils/moveOrders")
public class MoveOrders extends HttpServlet {

    private static final Logger log = LogManager.getLogger();

    public static void main(String[] args){
        Connection con = null;
        Properties connectionProps;
        connectionProps = new Properties();
        connectionProps.put("user","root");
        connectionProps.put("password", "letmein1");
        try {
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/FE", connectionProps);
            run(con);
            System.out.println("Finished moving orders");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (con != null) try {
                con.close();
            } catch (SQLException e) {}
        }

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Connection con = null;
        try {
            con = Utils.getConnection();
            run(con);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (con != null) try {
                con.close();
            } catch (SQLException e) {}
        }
    }

    public static void run(Connection con) throws Exception {
        int groupId = -1;
        int packageId = -1;
        int offerId = -1;
        String sql = null;
        ResultSet rs = null;
        HashMap<String,Offer> offers = new HashMap<String,Offer>(); // productConfigUUID, offerId
        Calendar cal = Calendar.getInstance();
        Date saveDate = cal.getTime();

        // get the list of groups from sinc and loop through them
        SincGroups sg = new SincGroups(con);
        HashSet<JSONObject> sincGroups = sg.getGroups();
        for (JSONObject sincGroup : sincGroups) {

            String groupUUID = sincGroup.getString("id");
            String groupName = sincGroup.getString("name");
            String alias = sincGroup.getString("alias");
            String status = sincGroup.getString("status");
            int active = status != null && "ACTIVE".equals(status) ? 1 : 0;
            JSONObject emp = sincGroup.getJSONObject("employer");

            // Add the group to FE.groups if it doesn't exist
            Group group = new Group(groupName, alias, active,con);
            Address address = new Address("Main", emp.getString("address1"), emp.getString("address2"), emp.getString("city"), emp.getString("state"), emp.getString("zip"), con);
            group.addAddress(address);
            group.save();
            //groupId = group.getGroupId();

            // For each group, get active packages
            SincPackages sp = new SincPackages(groupUUID,con);
            HashMap<String, JSONObject> pkgs = sp.getPackages(); // <pkgUUID,json>

            // loop through each package and write a package record and then an offer record for each product in the package
            // Create a HashMap of productId,offerId for use later when writing the coverage objects
            if (pkgs.size() > 0) for (String pkgUUID : pkgs.keySet()) {
                // write a Package record to get a packageId
                JSONObject pkg = pkgs.get(pkgUUID);
                JSONObject openEnrollment = pkg.getJSONObject("openEnrollment");
                JSONObject loginScheme = SincGroups.getLoginScheme(sincGroup);

                String login1 = null, login1Label = null, login2 = null, login2Label = null;
                if (loginScheme.has("first")) {
                    JSONObject first = loginScheme.getJSONObject("first");
                    login1 = first.getString("type");
                    login1Label = first.getString("displayText");
                    JSONObject second = loginScheme.getJSONObject("second");
                    login2 = second.getString("type");
                    login2Label = second.getString("displayText");
                }
                int deductionsPerYear = 0;
                if (sincGroup.has("deductionsPerYear")) {
                    deductionsPerYear = sincGroup.getInt("deductionsPerYear");
                }
                String password = null;
                if (sincGroup.has("password")) {
                    password = sincGroup.getString("password");
                }
                Pkg savePkg = new Pkg(group, pkg.getString("situsState"),deductionsPerYear,login1,login1Label,login2,login2Label,password,con);
                EnrollmentDates eDates = new EnrollmentDates(openEnrollment.getString("startDate"), openEnrollment.getString("startDate"), openEnrollment.getString("endDate"),1,con);
                savePkg.addEnrollmentDates(eDates);
                savePkg.save();
                //packageId = savePkg.getPackageId();

                // write the classes for this package
                SincClasses sc = new SincClasses(groupUUID,pkgUUID,con);
                HashSet<JSONObject> classes = sc.getClasses();

                HashSet<Cls> savedClasses = new HashSet<Cls>();
                if (classes.size() > 0) for (JSONObject cl : classes) {
                    Cls saveClass = new Cls(group, savePkg, cl.getString("description"), "employerClass", "=", cl.getString("name"), con);
                    saveClass.save();
                    saveClass.setSourceData(cl);  // put the source json data in this object to locate the product configs later
                    savedClasses.add(saveClass);
                }

                // Now write the offer records
                SincProductConfigurations spc = new SincProductConfigurations(groupUUID,pkgUUID,con);
                HashSet<JSONObject> productConfigs = spc.getProductConfigurations();

                if (!productConfigs.isEmpty()) {
                    for (JSONObject config : productConfigs) {
                        String productConfigUUID = config.getString("id");
                        String solidifyId = config.getString("solidifyId");
                        JSONObject configParams = config.getJSONObject("configuration");
                        if (!"".equals(solidifyId)) {
                            Product prod = new Product(solidifyId, con);
                            // write an offer record
                            Offer offer = new Offer(group, prod, configParams.getString("displayName"), savePkg, config.toString(), con);
                            offer.save();
                            offers.put(productConfigUUID, offer); // used later when writing app coverage lines.
                        }
                    }
                }

                // write the classOffers records
                for (String configUUID : offers.keySet()) {
                    for (Cls c : savedClasses) {
                        if (c.hasProductConfig(configUUID)) {
                            Offer off = offers.get(configUUID);
                            ClassOffer clsOffer = new ClassOffer(c, off, con);
                            clsOffer.save();
                        }
                    }
                }
            }
            SincOrders SincOrders = new SincOrders(groupUUID,con);
            List<JSONObject> apps = SincOrders.getOrders();
            //List<JSONObject> apps = Utils.getLatestOrdersForGroup(groupUUID, con);

            for (JSONObject app : apps) {  // apps from sinc
                JSONObject data = app.getJSONObject("data");
                JSONObject member = data.getJSONObject("member");
                JSONObject personal = member.getJSONObject("personal");
                if (member.has("testUser") && member.getString("testUser").equalsIgnoreCase("yes")) {
                    continue;
                }
                //log.info(app.toString());
                HashMap<String, Person> dependents = new HashMap(); // queue dependents for writing coverages after they have been added to db
                Employee ee = new Employee(personal.getString("firstName"), personal.getString("lastName"), personal.getString("ssn"),personal.getString("dateOfBirth"),
                        personal.getString("gender"),member.getString("dateOfHire"),app.getString("classId"),
                        member.getString("occupation"),member.getString("employeeId"),member.getString("locationCode"),member.getString("locationDescription"),member.getString("status"),
                        member.getString("department"),member.getInt("hoursPerWeek"),member.getInt("deductionsPerYear"),member.getString("annualSalary"),saveDate, con);

                Address addr = new Address("home",personal.getString("address1"),personal.getString("address2"),personal.getString("city"),personal.getString("state"),personal.getString("zip"),con);
                ee.addAddress(addr);
                ee.save();

                // Write the app
                Date dateSaved = null;
                if (app.has("dateSaved")) {
                    dateSaved = new Date(app.getLong("dateSaved"));
                }
                String enroller = null;
                if (app.has("userName")) {
                    enroller = app.getString("userName");
                }
                String orderId = null;
                if (app.has("id")) {
                    orderId = app.getString("id");
                }
                int appSourceId = 2;
                if (enroller == null) {
                    appSourceId = 1;
                }
                App a = new App(group, orderId, dateSaved, enroller, appSourceId, con);
                a.save();

                // Write the app signature
                SincSignature ss = new SincSignature(orderId, con);
                JSONObject sigJson = ss.getSignatureJson();
                if (sigJson.has("data")) {
                    JSONObject sigData = sigJson.getJSONObject("data");
                    if (sigData.has("signature")) {
                        Signature sig = new Signature(a, sigData, con);
                        sig.save();
                    }
                }

                AppsToEmployees ate = new AppsToEmployees(a, ee, con);
                ate.save();

                if (app.has("questionAnswers")) {
                    JSONObject answers = app.getJSONObject("questionAnswers");
                    if (!answers.keySet().isEmpty()) {
                        QuestionResponses qr = new QuestionResponses(a, answers.toString(), con);
                        qr.save();
                    }
                }

                // write dependents
                JSONArray deps = data.getJSONArray("dependents");
                for (int i = 0; i < deps.length(); i++) {
                    JSONObject dep = deps.getJSONObject(i);
                    Dependent d = new Dependent(ee, dep.getString("firstName"), dep.getString("lastName"),dep.getString("ssn"),"".equals(dep.getString("dateOfBirth"))?"":dep.getString("dateOfBirth"),dep.getString("gender"),dep.getString("relationship"),saveDate,con);
                    d.save();

                    dependents.put(dep.getString("id"), d); // queue dependents for writing coverages later
                }

                // holds the beneficiaries master list for writing beneficiaries later
                JSONArray bens = data.getJSONArray("beneficiaries");

                // write coverages
                HashSet<String> tieredProducts = new HashSet<String>();
                tieredProducts.add("MEDICAL"); tieredProducts.add("ACCIDENT"); tieredProducts.add("GAP"); tieredProducts.add("VISION"); tieredProducts.add("DENTAL"); tieredProducts.add("CANCER");

                HashSet<String> lifeProducts = new HashSet<String>();
                lifeProducts.add("VTL"); lifeProducts.add("BASIC_LIFE"); lifeProducts.add("CI");

                JSONArray covs = app.getJSONArray("covs");
                for (int i = 0; i < covs.length(); i++) {
                    JSONObject cov = covs.getJSONObject(i);
                    String splitId = cov.getString("splitId");
                    Offer o = null;
                    if (offers.containsKey(splitId)) {
                        o = offers.get(splitId);
                    }

                    if (o == null) {
                        throw new Exception("Can't write coverage record no offer found");
                    }
                    String election = null;
                    int electionTypeId = -1;
                    if (!cov.has("benefit")) {
                        throw new Exception("Coverage object doesn't have benefit field.  Can't write coverage record.");
                    }
                    election = cov.getString("benefit");

                    try {
                        electionTypeId = Coverage.getElectionTypeId(election, con);
                    } catch (NoValue n) {
                        log.error("Couldn't find an electionTypeId for election: "+election);
                        break;
                    }
                    DecimalFormat df = new DecimalFormat("#,###,###.00");
                    float annualPremium = 0f, modalPremium = 0f;
                    String effectiveDate = null;
                    if (cov.has("premiums")) {
                        JSONArray premiums = cov.getJSONArray("premiums");
                        if (premiums.length() > 1) {
                            log.debug("premiums array is longer than 1");
                        }
                        JSONObject prem = premiums.getJSONObject(0);
                        if (prem.has("totalYearly")) {
                            try {
                                annualPremium = df.parse(prem.getString("totalYearly")).floatValue();
                            } catch (Exception e) {}
                        }
                        if (prem.has("deduction")) {
                            try {
                                modalPremium = df.parse(prem.getString("deduction")).floatValue();
                            } catch (Exception e) {}
                        }
                        if (prem.has("effectiveDate")) {
                            effectiveDate = prem.getString("effectiveDate");
                        }
                    }

                    Coverage c = new Coverage(o, a, cov, electionTypeId, Coverage.NOT_PENDED, annualPremium, modalPremium,effectiveDate,null,con);
                    c.save();

                    // Write the covered people records if elected
                    // VTL
                    if (lifeProducts.contains(cov.getString("type")) && cov.getString("subType").equals("ee")) {
                        CoveredPeople cp = new CoveredPeople(c, ee, con);
                        cp.save();
                    } else if (lifeProducts.contains(cov.getString("type")) && !(cov.getString("subType")).equals("ee")) {
                        JSONArray covered = cov.getJSONArray("coveredDependents");
                        for (int j = 0; j < covered.length(); j++) {
                            String depId = covered.getString(j);
                            if (!dependents.containsKey(depId)) {
                                throw new Exception("Can't find the covered dependent: " + depId);
                            }
                            Person dep = dependents.get(depId);
                            CoveredPeople cp = new CoveredPeople(c, dep, con);
                            cp.save();
                        }
                    } else if (tieredProducts.contains(cov.getString("type"))) {
                        // Medical enrolled
                        CoveredPeople cp = new CoveredPeople(c, ee, con);
                        cp.save();
                        if (electionTypeId == 1) {
                            JSONArray covered = cov.getJSONArray("coveredDependents");
                            for (int j = 0; j < covered.length(); j++) {
                                String depId = covered.getString(j);
                                if (!dependents.containsKey(depId)) {
                                    throw new Exception("Can't find the covered dependent: " + depId);
                                }
                                Person dep = dependents.get(depId);
                                cp = new CoveredPeople(c, dep, con);
                                cp.save();
                            }
                        }
                    } else {
                        CoveredPeople cp = new CoveredPeople(c,ee,con);
                        cp.save();
                    }

                    // Write Beneficiaries

                }
            }
        }
    }
}
