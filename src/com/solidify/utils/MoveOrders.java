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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * Created by jrobins on 2/5/15.
 */
@WebServlet("/utils/moveOrders")
public class MoveOrders extends HttpServlet{
    private static final Logger log = LogManager.getLogger();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int groupId = -1;
        int packageId = -1;
        int offerId = -1;
        String sql = null;
        ResultSet rs = null;
        HashMap<String,Offer> offers = new HashMap<String,Offer>(); // productConfigUUID, offerId

        try {
            // get the list of groups from sinc and loop through them
            SincGroups sg = new SincGroups();
            HashSet<JSONObject> sincGroups = sg.getGroups();
            for (JSONObject sincGroup : sincGroups) {

                String groupUUID = sincGroup.getString("id");
                String groupName = sincGroup.getString("name");
                String alias = sincGroup.getString("alias");
                JSONObject emp = sincGroup.getJSONObject("employer");

                // Add the group to FE.groups if it doesn't exist
                Group group = new Group(groupName, alias);
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
                    Pkg savePkg = new Pkg(group, openEnrollment.getString("startDate"), openEnrollment.getString("endDate"), pkg.getString("situsState"));
                    savePkg.save();
                    //packageId = savePkg.getPackageId();

                    // write the classes for this package
                    SincClasses sc = new SincClasses(groupUUID,pkgUUID);
                    HashSet<JSONObject> classes = sc.getClasses();

                    HashSet<Cls> savedClasses = new HashSet<Cls>();
                    if (classes.size() > 0) for (JSONObject cl : classes) {
                        Cls saveClass = new Cls(group, savePkg, "", "employerClass", "=", cl.getString("name"));
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

                for (JSONObject app : apps) {
                    HashMap<String, Person> dependents = new HashMap(); // queue dependents for writing coverages after they have been added to db
                    Employee ee = new Employee(app.getString("firstName"), app.getString("lastName"), app.getString("ssn"),app.getString("dateOfHire"),app.getString("class"),
                            app.getString("occupation"),app.getString("employeeId"),app.getString("locationCode"),app.getString("locationDescription"),app.getString("status"),
                            app.getString("department"),app.getInt("hoursPerWeek"),app.getInt("deductionsPerYear"),app.getString("annualSalary"),null,null);

                    Address addr = new Address("home",app.getString("address1"),app.getString("address2"),app.getString("city"),app.getString("state"),app.getString("zip"));
                    ee.addAddress(addr);
                    /*
                    EmploymentInfo ei = new EmploymentInfo(ee);
                    ei.setAnnualSalary(app.getString("annualSalary"));
                    ei.setDateOfHire(app.getString("dateOfHire"));
                    ei.setDeductionsPerYear(app.getInt("deductionsPerYear"));
                    ei.setDepartment(app.getString("department"));
                    ei.setEmployeeId(app.getString("employeeId"));
                    ei.setEmployerClass(app.getString("class"));
                    ei.setHoursPerWeek(app.getInt("hoursPerWeek"));
                    ei.setLocationCode(app.getString("locationCode"));
                    ei.setLocationDescription(app.getString("locationDescription"));
                    ei.setOccupation(app.getString("occupation"));
                    ei.setStatus(app.getString("status"));
                    ee.setEmploymentInfo(ei);
                    */
                    ee.save();

                    App a = new App(group, app.getString("orderId"), 2);
                    a.save();
                    int appId = a.getAppId();
                    AppsToEmployees ate = new AppsToEmployees(a, ee);
                    ate.save();

                    // write dependents
                    JSONArray deps = app.getJSONArray("dependents");
                    for (int i = 0; i < deps.length(); i++) {
                        JSONObject dep = deps.getJSONObject(i);
                        Dependent d = new Dependent(ee, dep.getString("firstName"), dep.getString("lastName"),dep.getString("ssn"),dep.getString("relationship"));
                        d.save();

                        dependents.put(dep.getString("id"), d); // queue dependents for writing coverages later
                    }

                    // write coverages
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
                        Coverage c = new Coverage(o, a, cov.getString("benefit"), electionTypeId);
                        c.save();

                        // VTL
                        if (cov.getString("type").equals("VTL") && cov.getString("subType").equals("ee")) {
                            CoveredPeople cp = new CoveredPeople(c, ee);
                            cp.save();
                        } else if ((cov.getString("type")).equals("VTL") && !(cov.getString("subType")).equals("ee")) {
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
                        } else if ((cov.getString("type")).equals("MEDICAL") && electionTypeId == 1) {
                            // Medical enrolled
                            CoveredPeople cp = new CoveredPeople(c, ee);
                            cp.save();
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
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
