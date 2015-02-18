package com.solidify.dao;

import com.solidify.admin.reports.Utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by jrobins on 2/17/15.
 */
public class QuestionResponses {
    private int questionResponseId;
    private App app;
    private String json;
    private Connection con;
    private boolean manageConnection = true;

    public QuestionResponses(int questionResponseId, App app, String json, Connection con) throws SQLException {
        this.questionResponseId = questionResponseId;
        this.app = app;
        this.json = json;
        this.con = con;
        if (con != null) {
            manageConnection = false;
        }
        if (questionResponseId > -1) {
            fetch();
        }
    }

    public QuestionResponses(int questionResponseId) throws SQLException {
        this(questionResponseId,null,null,null);
    }

    public QuestionResponses(int questionResponseId,Connection con) throws SQLException {
        this(questionResponseId,null,null,con);
    }

    public QuestionResponses(App app, String json) throws SQLException {
        this(-1,app,json,null);
    }

    public QuestionResponses(App app, String json, Connection con) throws SQLException {
        this(-1,app,json,con);
    }

    public boolean isValid() {
        if (app.getAppId() > -1 && json != null && !"".equals(json)) {
            return true;
        } else {
            return false;
        }
    }

    public void save() throws SQLException {
        if (isValid()) {
            if (questionResponseId < 0) {
                insert();
            }
        }
    }

    private void insert() throws SQLException {
        try {
            if (manageConnection) {
                con = Utils.getConnection();
            }
            String sql = "INSERT INTO FE.QuestionResponses (appId,json) VALUES (?,?)";
            PreparedStatement insert = con.prepareStatement(sql);
            insert.setInt(1,app.getAppId());
            insert.setString(2, json);
            insert.executeUpdate();
            ResultSet rs = insert.getGeneratedKeys();
            if (rs.next()) {
                questionResponseId = rs.getInt(1);
            }
        } finally {
            if (manageConnection && con != null) con.close();
        }
    }

    private void fetch() throws SQLException {
        try {
            if (manageConnection) {
                con = Utils.getConnection();
            }
            String sql = "SELECT appId, json FROM FE.QuestionResponses WHERE responseId =  ?";
            PreparedStatement select = con.prepareStatement(sql);
            select.setInt(1, questionResponseId);
            ResultSet rs = select.executeQuery();
            if (rs.next()) {
                this.json = rs.getString("json");
            }
        } finally {
            if (manageConnection && con != null) con.close();
        }
    }

    public int getQuestionResponseId() {
        return questionResponseId;
    }

    public String getJson() {
        return json;
    }

    public App getApp() {
        return app;
    }

    public boolean isManageConnection() {
        return manageConnection;
    }
}
