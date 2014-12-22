package com.solidify.admin.reports;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

public class MissingOrders implements Runnable {
	private static final Logger log = LogManager.getLogger();
	
	private String groupId;
	
	public MissingOrders(String groupId) {
		this.groupId = groupId;
	}
	
	public void run() {
		Connection con = null;
		BufferedWriter bw = null;
		BufferedWriter myLog = null;
		log.info("started");
		ResultSet rs = null;
		try {
			String groupName = "";
			groupName = Utils.getGroupName(groupId);
			File results = new File("/tmp/"+groupName+"_missingReport.csv");
			File ml = new File("/tmp/missingReportLog.txt");
			myLog = new BufferedWriter(new FileWriter(ml));
			
			bw = new BufferedWriter(new FileWriter(results));
			bw.write("\"groupName\",\"groupId\",\"memberId\",\"EE Name\",\"EE dob\",\"EE SSN\"");
			bw.newLine();
			
			con = Utils.getConnection();

			JSONArray groupOrders = new JSONArray();
			log.info(groupId+" : "+groupName);
			
			ArrayList<JSONObject> orders = Utils.getLatestOrdersForGroup(groupId, con, false);  // false puts isBatchable = 0 on the query so only the unbatched latest orders are returned.
			
			myLog.write(groupName+" has "+orders.size()+" orders with isBatchable = 0.");
			myLog.newLine();
			
			log.info(groupName+" has "+orders.size()+" orders with isBatchable = 0.");
			
			HashSet<String> inBatchOrders = new HashSet<String>();
			HashMap<String, JSONObject> notInBatchOrders = new HashMap<String, JSONObject>();
			
			// Load the latest orders into groupOrders
			for (Iterator<JSONObject> it = orders.iterator(); it.hasNext();) {
				groupOrders.put(it.next());
			}
			
			int len = groupOrders.length();
			String batchSql = "SELECT COUNT(*) AS cnt FROM batchOrders WHERE orderId = ? and deleted = 0";
			PreparedStatement batchOrdersCnt = con.prepareStatement(batchSql);
			for (int i=0; i<len; i++) {
				JSONObject obj = (JSONObject)groupOrders.get(i);
				String oId = (String)obj.get("orderId");
				String memId = (String)obj.get("memberId");
				if (inBatchOrders.contains(memId)) {
					continue;  // if this member already has a batched order, skip it
				}
				
				batchOrdersCnt.setString(1, oId);
				rs = batchOrdersCnt.executeQuery();
				if (rs.next()) {
					int num = rs.getInt("cnt");
					myLog.write("orderId: "+oId+" has "+num+" in batchOrders");
					myLog.newLine();
					//log.info("orderId: "+oId+" has "+num+" in batchOrders");
					if (num > 0) {
						inBatchOrders.add(memId);
						if (notInBatchOrders.containsKey(memId)) {
							notInBatchOrders.remove(memId);
						}
					} else {
						notInBatchOrders.put(memId, obj);
					}
				} else {
					myLog.write("orderId: "+oId+" has 0 in batchOrders");
					myLog.newLine();
					//log.info("orderId: "+oId+" has 0 in batchOrders");
					notInBatchOrders.put(memId, obj);
				}
				rs.close();
			}
			batchOrdersCnt.close();
			log.info(groupId+" has "+notInBatchOrders.size()+" to update");
			// done looping through the orders for this group
			// print out the members that have orders that are not in the batchOrders table
			if (notInBatchOrders.size() > 0) {
				String upSql = "UPDATE sinc.orders SET isBatchable = 1 WHERE id = ?";
				PreparedStatement update = con.prepareStatement(upSql);
				for (Iterator<String>it = notInBatchOrders.keySet().iterator(); it.hasNext();) {
					String key = it.next();
					JSONObject obj = notInBatchOrders.get(key);
					String id = (String)obj.get("orderId");
					update.setString(1, id);
					update.executeUpdate();
					myLog.write("orderId: "+id+" updated");
					myLog.newLine();
					log.info("orderId: "+id+" updated");
					bw.write("\""+obj.get("name")+"\",\""+obj.get("groupId")+"\",\""+obj.get("memberId")+"\",\""+obj.get("firstName")+" "+obj.get("lastName")+"\",\""+obj.get("dateOfBirth")+"\",\""+obj.get("ssn")+"\"");
					bw.newLine();
				}
				update.close();
			}
            log.info("done");
		} catch (Exception e) {
			log.error("AuditProcessor Error",e);
		} finally {
			try {
				rs.close();
			} catch (Exception e) {}
			try {
				con.close();
			} catch (Exception e) {}
			try {
				bw.close();
			} catch (Exception e) {}
			try {
				myLog.close();
			} catch (Exception e) {}
		}

	}

}
