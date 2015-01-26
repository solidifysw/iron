package com.solidify.admin.reports;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.solidify.admin.reports.Utils;
import org.json.JSONArray;
import org.json.JSONObject;

public class AllDeclinations implements Runnable {
	private static final Logger log = LogManager.getLogger();
	
	private String groupId;
	
	public AllDeclinations(String groupId) {
		this.groupId = groupId;
	}
	
	public void run() {
		log.info("all declinations thread started");
		BufferedWriter bw = null;
		try {
			String groupName = "";
			groupName = Utils.getGroupName(groupId);
			File out = new File("/tmp/Baxley_declinations.csv");
			bw = new BufferedWriter(new FileWriter(out));
			bw.write("\"First\",\"Last\",\"DOB\",\"SSN\",\"Product\",\"Reason\"");
			bw.newLine();
			ArrayList<JSONObject> orders = Utils.getLatestOrdersForGroup(groupId);
			if (!orders.isEmpty()) {
				for (Iterator<JSONObject> it = orders.iterator(); it.hasNext();) {
					JSONObject obj = it.next();
					//log.info(obj.get("firstName")+" "+obj.get("lastName")+" "+obj.get("dateOfBirth")+" "+obj.get("ssn")+" "+obj.get("date")+" "+obj.get("orderId"));
					JSONArray covs = (JSONArray)obj.get("covs");
					for(int i=0; i<covs.length(); i++) {
						JSONObject cov = (JSONObject)covs.get(i);
						String benefit = (String)cov.get("benefit");
						String declineReason = "";
						if (benefit.equals("Decline")) {
							declineReason = cov.has("declineReason") ? cov.getString("declineReason") : "";
							bw.write("\""+obj.get("firstName")+"\",\""+obj.get("lastName")+"\",\""+obj.get("dateOfBirth")+"\",\""+obj.get("ssn")+"\",\""+cov.get("productId")+"\",\""+declineReason+"\"");
							bw.newLine();
							//log.info(obj.get("orderId")+" "+obj.get("firstName")+" "+obj.get("lastName")+" "+obj.get("dateOfBirth")+" "+obj.get("ssn")+" "+cov.get("productId")+" "+benefit+" "+declineReason);
						}
					}
				}
			}
			log.info("all declinations thread done");
		} catch (Exception e) {
			log.error("error",e);
		} finally {
			try {
				bw.close();
			} catch (Exception e){}
		}
	}
}
