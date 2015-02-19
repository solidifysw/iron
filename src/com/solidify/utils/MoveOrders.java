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
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Created by jrobins on 2/5/15.
 */
@WebServlet("/utils/moveOrders")
public class MoveOrders extends HttpServlet {
    private static final Logger log = LogManager.getLogger();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int groupId = -1;
        int packageId = -1;
        int offerId = -1;
        String sql = null;
        ResultSet rs = null;
        HashMap<String,Offer> offers = new HashMap<String,Offer>(); // productConfigUUID, offerId
        Calendar cal = Calendar.getInstance();
        Date saveDate = cal.getTime();

        try {
            // get the list of groups from sinc and loop through them
            SincGroups sg = new SincGroups();
            HashSet<JSONObject> sincGroups = sg.getGroups();
            for (JSONObject sincGroup : sincGroups) {

                String groupUUID = sincGroup.getString("id");
                String groupName = sincGroup.getString("name");
                String alias = sincGroup.getString("alias");
                String status = sincGroup.getString("status");
                int active = status != null && "ACTIVE".equals(status) ? 1 : 0;
                JSONObject emp = sincGroup.getJSONObject("employer");

                // Add the group to FE.groups if it doesn't exist
                Group group = new Group(groupName, alias, active);
                Address address = new Address("Main", emp.getString("address1"), emp.getString("address2"), emp.getString("city"), emp.getString("state"), emp.getString("zip"));
                group.addAddress(address);
                group.save();
                //groupId = group.getGroupId();

                // For each group, get active packages
                SincPackages sp = new SincPackages(groupUUID);
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
                    Pkg savePkg = new Pkg(group, openEnrollment.getString("startDate"), openEnrollment.getString("endDate"), pkg.getString("situsState"),deductionsPerYear,login1,login1Label,login2,login2Label,password);
                    savePkg.save();
                    //packageId = savePkg.getPackageId();

                    // write the classes for this package
                    SincClasses sc = new SincClasses(groupUUID,pkgUUID);
                    HashSet<JSONObject> classes = sc.getClasses();

                    HashSet<Cls> savedClasses = new HashSet<Cls>();
                    if (classes.size() > 0) for (JSONObject cl : classes) {
                        Cls saveClass = new Cls(group, savePkg, cl.getString("description"), "employerClass", "=", cl.getString("name"));
                        saveClass.save();
                        saveClass.setSourceData(cl);  // put the source json data in this object to locate the product configs later
                        savedClasses.add(saveClass);
                    }

                    // Now write the offer records
                    SincProductConfigurations spc = new SincProductConfigurations(groupUUID,pkgUUID);
                    HashSet<JSONObject> productConfigs = spc.getProductConfigurations();

                    if (!productConfigs.isEmpty()) {
                        for (JSONObject config : productConfigs) {
                            String productConfigUUID = config.getString("id");
                            String solidifyId = config.getString("solidifyId");
                            if (!"".equals(solidifyId)) {
                                Product prod = new Product(solidifyId);
                                // write an offer record
                                Offer offer = new Offer(group, prod, savePkg);
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
                                ClassOffer clsOffer = new ClassOffer(c, off);
                                clsOffer.save();
                            }
                        }
                    }
                }

                List<JSONObject> apps = Utils.getLatestOrdersForGroup(groupUUID);

                for (JSONObject app : apps) {  // apps from sinc
                    //log.info(app.toString());
                    HashMap<String, Person> dependents = new HashMap(); // queue dependents for writing coverages after they have been added to db
                    Employee ee = new Employee(app.getString("firstName"), app.getString("lastName"), app.getString("ssn"),app.getString("dateOfBirth"), app.getString("gender"),app.getString("dateOfHire"),app.getString("class"),
                            app.getString("occupation"),app.getString("employeeId"),app.getString("locationCode"),app.getString("locationDescription"),app.getString("status"),
                            app.getString("department"),app.getInt("hoursPerWeek"),app.getInt("deductionsPerYear"),app.getString("annualSalary"),saveDate);

                    Address addr = new Address("home",app.getString("address1"),app.getString("address2"),app.getString("city"),app.getString("state"),app.getString("zip"));
                    ee.addAddress(addr);
                    ee.save();

                    // Write the app
                    Date dateSaved = null;
                    if (app.has("dateSaved")) {
                        dateSaved = new Date(app.getLong("dateSaved"));
                    }
                    String enroller = null;
                    if (app.has("enroller")) {
                        enroller = app.getString("enroller");
                    }
                    String orderId = null;
                    if (app.has("orderId")) {
                        orderId = app.getString("orderId");
                    }
                    int appSourceId = 2;
                    if (enroller == null) {
                        appSourceId = 1;
                    }
                    App a = new App(group, orderId, dateSaved, enroller, appSourceId);
                    a.save();

                    // Write the app signature
                    SincSignature ss = new SincSignature(orderId);
                    Signature sig = new Signature(a,ss.getSignatureJson());
                    sig.save();

                    AppsToEmployees ate = new AppsToEmployees(a, ee);
                    ate.save();

                    if (app.has("questionAnswers")) {
                        JSONObject answers = app.getJSONObject("questionAnswers");
                        QuestionResponses qr = new QuestionResponses(a, answers.toString());
                        qr.save();
                    }

                    // write dependents
                    JSONArray deps = app.getJSONArray("dependents");
                    for (int i = 0; i < deps.length(); i++) {
                        JSONObject dep = deps.getJSONObject(i);
                        Dependent d = new Dependent(ee, dep.getString("firstName"), dep.getString("lastName"),dep.getString("ssn"),"".equals(dep.getString("dateOfBirth"))?"":dep.getString("dateOfBirth"),dep.getString("gender"),dep.getString("relationship"),saveDate);
                        d.save();

                        dependents.put(dep.getString("id"), d); // queue dependents for writing coverages later
                    }

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
                            electionTypeId = Coverage.getElectionTypeId(election);
                        } catch (NoValue n) {
                            log.error("Couldn't find an electionTypeId for election: "+election);
                            break;
                        }
                        DecimalFormat df = new DecimalFormat("#,###,###.00");
                        float annualPremium = 0f, modalPremium = 0f;
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
                        }

                        Coverage c = new Coverage(o, a, cov, electionTypeId, Coverage.NOT_PENDED, annualPremium, modalPremium);
                        c.save();

                        // Write the covered people records if elected
                        // VTL
                        if (lifeProducts.contains(cov.getString("type")) && cov.getString("subType").equals("ee")) {
                            CoveredPeople cp = new CoveredPeople(c, ee);
                            cp.save();
                        } else if (lifeProducts.contains(cov.getString("type")) && !(cov.getString("subType")).equals("ee")) {
                            JSONArray covered = cov.getJSONArray("coveredDependents");
                            for (int j = 0; j < covered.length(); j++) {
                                String depId = covered.getString(j);
                                if (!dependents.containsKey(depId)) {
                                    throw new Exception("Can't find the covered dependent: " + depId);
                                }
                                Person dep = dependents.get(depId);
                                CoveredPeople cp = new CoveredPeople(c, dep);
                                cp.save();
                            }
                        } else if (tieredProducts.contains(cov.getString("type"))) {
                            // Medical enrolled
                            CoveredPeople cp = new CoveredPeople(c, ee);
                            cp.save();
                            if (electionTypeId == 1) {
                                JSONArray covered = cov.getJSONArray("coveredDependents");
                                for (int j = 0; j < covered.length(); j++) {
                                    String depId = covered.getString(j);
                                    if (!dependents.containsKey(depId)) {
                                        throw new Exception("Can't find the covered dependent: " + depId);
                                    }
                                    Person dep = dependents.get(depId);
                                    cp = new CoveredPeople(c, dep);
                                    cp.save();
                                }
                            }
                        } else {
                            CoveredPeople cp = new CoveredPeople(c,ee);
                            cp.save();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            log.info("MoveOrders finished");
        }
    }
}
