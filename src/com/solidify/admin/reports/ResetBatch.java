package com.solidify.admin.reports;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

public class ResetBatch implements Runnable {
	private static final Logger log = LogManager.getLogger();
	private String groupId;
	
	public ResetBatch(String groupId) {
		this.groupId = groupId;
	}
	
	public void run() {
		log.info("start");
		if (groupId == null || "".equals("groupId")) {
			log.info("done.  No groupId");
			return;
		}
		Connection con = null;
		String sql = "";
		
		try {
			con = Utils.getConnection();
			int cnt = 0;
			PreparedStatement update = null;

			// clear everthing that was marked to batch.
			sql = "UPDATE sinc.orders SET isBatchable = 0 WHERE groupId = ?";
			update = con.prepareStatement(sql);
			update.setString(1,groupId);
			update.executeUpdate();
			update.close();

			// Get the good orders to batch
			ArrayList<JSONObject> orders = Utils.getLatestOrdersForGroup(groupId, con);
			log.info("number of orders: "+orders.size());

			if (!orders.isEmpty()) {
				sql = "UPDATE sinc.orders SET isBatchable = 1 WHERE id = ?";
				update = con.prepareStatement(sql);
				for (Iterator<JSONObject> it = orders.iterator(); it.hasNext();) {
					JSONObject obj = it.next();
					String id = obj.getString("orderId");
					//log.info(id);
					update.setString(1, id);
					try {
						cnt += update.executeUpdate();
					} catch (Exception e) {
						log.info(obj.toString());
						throw e;
					}
					//log.info(cnt);
				}
				update.close();
				
				log.info("updated "+cnt+" order records to isBatchable = 1");
				
				sql = "UPDATE sinc.batchOrders SET deleted = 1 WHERE groupId = ?";
				update = con.prepareStatement(sql);
				update.setString(1, groupId);
				cnt = update.executeUpdate();
				log.info("set "+cnt+" records deleted in batchOrders");
				
				sql = "UPDATE sinc.batches SET deleted = 1 WHERE groupId = ?";
				update = con.prepareStatement(sql);
				update.setString(1, groupId);
				cnt = update.executeUpdate();
				log.info("set "+cnt+" records deleted in batches");
			}
			log.info("end");
		} catch (Exception e) {
			log.error("error",e);
		} finally {
			try {
				con.close();
			} catch (Exception e) {}
		}
	}

}
