package com.solidify.dao;

import com.solidify.admin.reports.Utils;
import com.solidify.exceptions.MissingProperty;
import com.solidify.exceptions.NoValue;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by jrobins on 2/10/15.
 */
public class Coverage {
    private int coverageId;
    private App app;
    private Offer offer;
    private String benefit;
    private int electionTypeId;
    private int pending;
    private String declineReason;
    private float annualPremium;
    private float modalPremium;
    private String start;
    private String end;
    private Connection con;
    private ArrayList<Beneficiary> beneficiaries;
    private boolean manageConnection = true;
    public static final int PENDED = 1;
    public static final int NOT_PENDED = 0;
    public static final String NA = "";

    public Coverage(int coverageId, Offer offer, App app, String benefit, int electionTypeId, int pending, float annualPremium, float modalPremium, String start, String end, Connection con) {
        this.coverageId = coverageId;
        this.offer = offer;
        this.app = app;
        this.benefit = benefit;
        this.electionTypeId = electionTypeId;
        this.pending = pending;
        this.declineReason = null;
        this.coverageId = -1;
        this.annualPremium = annualPremium;
        this.modalPremium = modalPremium;
        this.start = start;
        this.end = end;
        this.con = con;
        this.beneficiaries = new ArrayList<>();
        this.manageConnection = con == null ? true : false;
    }

    /**
     * Use this constructor when moving from sinc database.  Converts benefit in the cancer order object to LEVEL_ONE or LEVEL_TWO from TBD
     * @param offer
     * @param app
     * @param json
     * @param electionTypeId
     */
    public Coverage(Offer offer, App app, JSONObject json, int electionTypeId, int pending, float annualPremium, float modalPremium,String start, String end, Connection con) {
        this(-1,offer,app,json.getString("benefit"),electionTypeId, pending,annualPremium,modalPremium,start,end,con);
        if (json.getString("type").equals("CANCER")) {
            this.benefit = json.getString("benefitLevel");
        }
        if (electionTypeId == 2 && json.has("declineReason")) {
            this.declineReason = json.getString("declineReason");
        } else if (electionTypeId == 2) { // this sets the electionType to opt-out if there is no decline reason
            this.electionTypeId = 3;
        }
    }

    public void addBeneficiary(Beneficiary ben) {
        beneficiaries.add(ben);
    }

    public void setDeclineReason(String reason) {
        this.declineReason = reason;
    }

    public int getCoverageId() {
        return coverageId;
    }

    public void save() throws SQLException, MissingProperty {
        if (!offer.isLoaded()) {
            throw new MissingProperty("offer is not loaded");
        }
        if (!app.isLoaded()) {
            throw new MissingProperty("app is not loaded");
        }
        insert();
    }

    private void insert() throws SQLException {
        try {
            if (manageConnection) con = Utils.getConnection();
            String sql = "INSERT INTO FE.Coverages (appId,offerId,benefit, electionTypeId, pending, declineReason, annualPremium, modalPremium, start, end) VALUES (?,?,?,?,?,?,?,?,?,?)";
            PreparedStatement insert = con.prepareStatement(sql);
            insert.setInt(1, app.getAppId());
            insert.setInt(2, offer.getOfferId());
            insert.setString(3, benefit);
            insert.setInt(4, electionTypeId);
            insert.setInt(5,pending);
            insert.setString(6, declineReason);
            insert.setFloat(7,annualPremium);
            insert.setFloat(8,modalPremium);
            insert.setString(9,start);
            insert.setString(10,end);
            insert.executeUpdate();
            ResultSet rs = insert.getGeneratedKeys();
            if (rs.next()) {
                this.coverageId = rs.getInt(1);
            }
            insert.close();
            rs.close();
        } finally {
            if (manageConnection && con != null) con.close();
        }
    }

    public static int getElectionTypeId(String benefitString, Connection con) throws SQLException, NoValue {
        int out = -1;
        boolean manageConnection = true;
        if (con != null) {
            manageConnection = false;
        }
        try {
            if ("".equals(benefitString) || "Decline".equals(benefitString)) {
                benefitString = "declined";
            } else if (benefitString.equals("opt-out")) {
                // do nothing
            } else {
                benefitString = "enrolled";
            }
            if (manageConnection) {
                con = Utils.getConnection();
            }
            String sql = "SELECT electionTypeId FROM FE.ElectionTypes WHERE name = ?";
            PreparedStatement select = con.prepareStatement(sql);
            select.setString(1, benefitString);
            ResultSet rs = select.executeQuery();
            if (rs.next()) {
                out = rs.getInt("electionTypeId");
            } else {
                throw new NoValue();
            }
            select.close();
            rs.close();
        } finally {
            if (manageConnection && con != null) con.close();
        }
        return out;
    }

    public boolean isLoaded() {
        return coverageId > -1 ? true : false;
    }

    public App getApp() {
        return app;
    }

    public Offer getOffer() {
        return offer;
    }

    public String getBenefit() {
        return benefit;
    }

    public int getElectionTypeId() {
        return electionTypeId;
    }

    public int getPending() {
        return pending;
    }

    public String getDeclineReason() {
        return declineReason;
    }

    public float getAnnualPremium() {
        return annualPremium;
    }

    public float getModalPremium() {
        return modalPremium;
    }

    public void setCoverageId(int coverageId) {
        this.coverageId = coverageId;
    }

    public void setAnnualPremium(float annualPremium) {
        this.annualPremium = annualPremium;
    }

    public void setModalPremium(float modalPremium) {
        this.modalPremium = modalPremium;
    }
}
