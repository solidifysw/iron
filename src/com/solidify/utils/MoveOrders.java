package com.solidify.utils;

import com.solidify.admin.reports.Utils;
import com.solidify.dao.*;
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
        Connection con = null;
        int groupId = -1;
        int packageId = -1;
        int offerId = -1;
        HashMap<String,Integer> offers = new HashMap<String, Integer>(); // productConfigUUID, offerId

        try {
            con = Utils.getConnection();

            // get the list of groups from sinc and loop through them
            String groupUUID = "1a83f17c-34e3-45c0-b323-d6174400ab05";
            String groupName = "BatchTest";

            // Add the group to FE.groups if it doesn't exist
            String sql = "INSERT INTO `FE`.`Groups` (`name`,active) VALUES ('"+groupName+"',1)";
            PreparedStatement groupInsert = con.prepareStatement(sql);
            groupInsert.executeUpdate();

            ResultSet rs = groupInsert.getGeneratedKeys();
            if (rs.next()) {
                groupId = rs.getInt(1);
            }

            if (groupId < 0) {
                return;
            }
            rs.close();
            groupInsert.close();

            // For each group, get active packages
            sql = "SELECT id, data FROM sinc.packages WHERE deleted = 0 AND groupId = ?";
            PreparedStatement packs = con.prepareStatement(sql);
            packs.setString(1,groupUUID);
            rs = packs.executeQuery();
            HashMap<String,JSONObject> pkgs = new HashMap();
            while(rs.next()) {
                // Loop through each package
                String data = rs.getString("data");
                JSONObject pkg = new JSONObject(data);
                String pkgUUID = rs.getString("id");
                pkgs.put(pkgUUID,pkg);
            }
            rs.close();
            packs.close();

            // loop through each package and write a package record and then an offer record for each product in the package
            // Create a HashMap of productId,offerId for use later when writing the coverage objects
            if (pkgs.size() > 0) {
                for (Iterator<String> it = pkgs.keySet().iterator(); it.hasNext();) {
                    String pkgUUID = it.next();
                    // write a Package record to get a packageId
                    JSONObject pkg = pkgs.get(pkgUUID);
                    JSONObject openEnrollment = pkg.getJSONObject("openEnrollment");
                    Pkg savePkg = new Pkg(groupId,openEnrollment.getString("startDate"),openEnrollment.getString("endDate"),pkg.getString("situsState"),con);
                    savePkg.save();
                    packageId = savePkg.getPackageId();

                    // write the classes for this package
                    sql = "SELECT data FROM sinc.classes WHERE packageId = ? AND groupId = ? AND deleted = 0";
                    PreparedStatement select = con.prepareStatement(sql);
                    select.setString(1,pkgUUID);
                    select.setString(2,groupUUID);
                    rs = select.executeQuery();
                    JSONObject cls = null;
                    HashSet<JSONObject> classes = new HashSet<JSONObject>();
                    while (rs.next()) {
                        cls = new JSONObject(rs.getString("data"));
                        classes.add(cls);
                    }
                    rs.close();
                    select.close();
                    HashSet<Cls> savedClasses = new HashSet<Cls>();
                    if (classes.size() > 0) {
                        for (JSONObject cl : classes) {
                            Cls saveClass = new Cls(groupId,packageId,"","employerClass","=",cl.getString("name"),con);
                            saveClass.save();
                            saveClass.setSourceData(cl);  // put the source json data in this object to locate the product configs later
                            savedClasses.add(saveClass);
                        }
                    }

                    // Now write the offer records
                    sql = "SELECT id, data FROM sinc.productConfigurations WHERE packageId = ? AND groupId = ? AND deleted = 0";
                    PreparedStatement pack = con.prepareStatement(sql);
                    pack.setString(1,pkgUUID);
                    pack.setString(2,groupUUID);
                    rs = pack.executeQuery();
                    HashSet<JSONObject> productConfigs = new HashSet<JSONObject>();
                    while(rs.next()) {
                        String data = rs.getString("data");
                        JSONObject config = new JSONObject(data);
                        productConfigs.add(config);
                    }
                    rs.close();
                    pack.close();

                    if (!productConfigs.isEmpty()) {
                        for (JSONObject config : productConfigs) {
                            String productConfigUUID = config.getString("id");
                            String solidifyId = config.getString("solidifyId");
                            if (!"".equals(solidifyId)) {
                                Product prod = Product.findProduct(solidifyId, con);
                                if (prod != null) {
                                    // write an offer record
                                    Offer offer = new Offer(groupId, prod.getProductId(), packageId, con);
                                    offer.save();
                                    offerId = offer.getOfferId();
                                    offers.put(productConfigUUID, offerId); // used later when writing app coverage lines.
                                }
                            }
                        }
                    }

                    // write the classOffers records
                    for (String configUUID : offers.keySet()) {
                        for (Cls c : savedClasses) {
                            if (c.hasProductConfig(configUUID)) {
                                Integer oId = offers.get(configUUID);
                                ClassOffer clsOffer = new ClassOffer(c.getClassId(), oId.intValue(), con);
                                clsOffer.save();
                            }
                        }
                    }
                }
            }

            List<JSONObject> apps = Utils.getLatestOrdersForGroup(groupUUID,con);

            for (JSONObject app : apps) {
                HashMap<String,Person> dependents = new HashMap(); // queue dependents for writing coverages after they have been added to db
                Person ee = new Person(app.getString("firstName"),app.getString("lastName"),true,app.getString("ssn"),con);
                ee.save();
                int employeeId = ee.getPersonId();
                EmploymentInfo ei = new EmploymentInfo(employeeId);
                ei.setAnnualSalary(app.getString("annualSalary"));
                ei.setDatabaseConnection(con);
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
                ei.save();

                App a = new App(groupId, app.getString("orderId"), 2, con);
                a.save();
                int appId = a.getAppId();
                AppsToEmployees ate = new AppsToEmployees(appId,employeeId,con);
                ate.save();

                // write dependents
                JSONArray deps = app.getJSONArray("dependents");
                for (int i = 0; i<deps.length(); i++) {
                    JSONObject dep = deps.getJSONObject(i);
                    Person p = new Person(dep.getString("firstName"),dep.getString("lastName"),false,dep.getString("ssn"),con);
                    p.save();
                    dependents.put(dep.getString("id"),p);
                    int depId = p.getPersonId();
                    DependentsToEmployees dte = new DependentsToEmployees(employeeId,depId,dep.getString("relationship"),con);
                    dte.save();
                }

                // write coverages
                JSONArray covs = app.getJSONArray("covs");
                for (int i=0; i<covs.length(); i++) {
                    JSONObject cov = covs.getJSONObject(i);


                    // need to get the offers for the ee's class
                    String solidifyId = cov.getString("productId");
                    Product prod = Product.findProduct(solidifyId,con);
                    Integer prodId = new Integer(prod.getProductId());
                    offerId = new Integer(-1);
                    if (offers.containsKey(prodId)) {
                        offerId = offers.get(prodId);
                    }
                    if (offerId < 0) {
                        throw new Exception("Can't find the appId.  Can't write coverage record");
                    }
                    String election = null;
                    int electionTypeId = -1;
                    if (!cov.has("benefit")) {
                        throw new Exception("Coverage object doesn't have benefit field.  Can't write coverage record.");
                    }
                    election = cov.getString("benefit");
                    electionTypeId = Coverage.getElectionTypeId(election,con);
                    Coverage c = new Coverage(offerId,appId, cov.getString("benefit"), electionTypeId, con);
                    c.save();
                    int coverageId = c.getCoverageId();
                    // VTL
                    if (cov.getString("type").equals("VTL") && cov.getString("subType").equals("ee")) {
                        CoveredPeople cp = new CoveredPeople(coverageId,employeeId,con);
                        cp.save();
                    } else if ((cov.getString("type")).equals("VTL") && !(cov.getString("subType")).equals("ee")) {
                        JSONArray covered = cov.getJSONArray("coveredDependents");
                        for (int j = 0; j < covered.length(); j++) {
                            String depId = covered.getString(j);
                            if (!dependents.containsKey(depId)) {
                                throw new Exception("Can't find the covered dependent: " + depId);
                            }
                            Person dep = dependents.get(depId);
                            CoveredPeople cp = new CoveredPeople(coverageId, dep.getPersonId(), con);
                            cp.save();
                        }
                    } else if ((cov.getString("type")).equals("MEDICAL") && electionTypeId == 1) {
                        // Medical enrolled
                        CoveredPeople cp = new CoveredPeople(coverageId, employeeId, con);
                        cp.save();
                        JSONArray covered = cov.getJSONArray("coveredDependents");
                        for (int j = 0; j < covered.length(); j++) {
                            String depId = covered.getString(j);
                            if (!dependents.containsKey(depId)) {
                                throw new Exception("Can't find the covered dependent: " + depId);
                            }
                            Person dep = dependents.get(depId);
                            cp = new CoveredPeople(coverageId, dep.getPersonId(), con);
                            cp.save();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (con != null) con.close();
            } catch (Exception e) {}
        }
    }

}
