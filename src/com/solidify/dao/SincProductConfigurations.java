package com.solidify.dao;

import com.solidify.admin.reports.Utils;
import com.solidify.exceptions.MissingProperty;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;

/**
 * Created by jennifermac on 2/15/15.
 */
public class SincProductConfigurations {
    private String groupUUID;
    private String packageUUID;
    private HashSet<JSONObject> productConfigurations;

    public SincProductConfigurations(String groupUUID, String packageUUID) throws MissingProperty, SQLException {
        this.groupUUID = groupUUID;
        this.packageUUID = packageUUID;
        this.productConfigurations = new HashSet<JSONObject>();
        if (groupUUID == null || "".equals(groupUUID)) {
            throw new MissingProperty("Missing groupUUID");
        } else if (packageUUID == null || "".equals(packageUUID)) {
            throw new MissingProperty("Missing packageUUID");
        }
        load();
    }

    private void load() throws SQLException {
        Connection con = null;
        try {
            con = Utils.getConnection();
            String sql = "SELECT id, data FROM sinc.productConfigurations WHERE packageId = ? AND groupId = ? AND deleted = 0";
            PreparedStatement pack = con.prepareStatement(sql);
            pack.setString(1, packageUUID);
            pack.setString(2, groupUUID);
            ResultSet rs = pack.executeQuery();

            while (rs.next()) {
                String data = rs.getString("data");
                JSONObject config = new JSONObject(data);
                productConfigurations.add(config);
            }
            rs.close();
            pack.close();
        } finally {
            if(con!=null)con.close();
        }
    }

    public HashSet<JSONObject> getProductConfigurations() {
        return productConfigurations;
    }
}
