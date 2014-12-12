package com.solidify.admin.reports;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GroupsToBatch implements Runnable {
	private static final Logger log = LogManager.getLogger();
	private String outputFile = null;
	
	public GroupsToBatch() {
		this.outputFile = "/tmp/groupsToBatch.csv";
	}

	public void run() {
		Connection con = null;
		BufferedWriter bw = null;
		log.info("started");
		try {
			File results = new File(outputFile);
			bw = new BufferedWriter(new FileWriter(results));
			bw.write("\"groupName\",\"groupId\"");
			bw.newLine();
			con = Utils.getConnection();
			HashMap<String,String> ids = new HashMap<String,String>();
			String sql = "SELECT id, name FROM sinc.groups WHERE deleted = 0 AND active = 1";
			
			PreparedStatement grpQ = con.prepareStatement(sql);
			ResultSet grpIds = grpQ.executeQuery();
			while(grpIds.next()) {
				ids.put(grpIds.getString("id"),grpIds.getString("name"));
			}
			grpIds.close();
			
			for (Iterator<String> gId = ids.keySet().iterator(); gId.hasNext();) { // for each groupId, check to see if any orders have isBatchable set
				String groupId = gId.next();
				//log.info("groupId: "+groupId);
				String groupName = ids.get(groupId);
				//log.info("groupName: "+groupName);
				
				sql = "SELECT COUNT(*) AS cnt FROM sinc.orders WHERE completed = 1 AND deleted = 0 AND isBatchable = 1 AND groupId = ?";
				PreparedStatement idSelect = con.prepareStatement(sql);
				idSelect.setString(1, groupId);
				ResultSet res = idSelect.executeQuery();
				int cnt = 0;
				if (res.next()) {
					cnt = res.getInt("cnt");
					if (cnt > 0) {
						bw.write("\""+groupName+"\",\""+groupId+"\"");
						bw.newLine();
					}
				}
				res.close();
			}
            log.info("done");
		} catch (Exception e) {
			log.error("AuditProcessor Error",e);
		} finally {
			try {
				con.close();
				bw.close();
			} catch (Exception e) {}
		}

	}

}
