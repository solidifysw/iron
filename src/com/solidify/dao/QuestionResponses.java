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

    public QuestionResponses(int questionResponseId, App app, String json) {
        this.questionResponseId = questionResponseId;
        this.app = app;
        this.json = json;
    }

    public QuestionResponses(App app, String json) {
        this(-1,app,json);
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
        Connection con = null;
        try {
            con = Utils.getConnection();
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
            if (con != null) con.close();
        }
    }
}
