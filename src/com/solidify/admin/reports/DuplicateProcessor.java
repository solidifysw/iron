package com.solidify.admin.reports;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;


public class DuplicateProcessor implements Runnable {
	private static final Logger log = LogManager.getLogger();
	public DuplicateProcessor() {
		// TODO Auto-generated constructor stub
	}

	public void run3() {
		Connection con = null;
		log.info("start");
		try {
			con = Utils.getConnection();
			String sql = "SELECT id, productId, benefit, data FROM sinc.coverages WHERE orderId = ? AND deleted = 0";
			PreparedStatement select = con.prepareStatement(sql);
			select.setString(1, "2344d203-47bf-42b2-b36c-bcec51b9660a");
			ResultSet rs = select.executeQuery();
			JSONArray covs = new JSONArray();
			while(rs.next()) {
				Blob b = rs.getBlob("data");
				byte[] bdata = b.getBytes(1, (int) b.length());
				String data = new String(bdata);
				JSONObject cov = new JSONObject(new JSONTokener(data));
				covs.put(cov);
			}
			rs.close();
			if (covs.length() > 0) {
				for (int i=0; i<covs.length(); i++) {
					JSONObject obj1 = (JSONObject)covs.get(i);
					if (i < covs.length()-1) {
						for (int j=i+1; j<covs.length(); j++) {
							JSONObject obj2 = (JSONObject)covs.get(j);
							// if obj1 == obj2 dupe record.
							if (obj1.get("productId").equals(obj2.get("productId")) && obj1.get("key").equals(obj2.get("key")) && obj1.get("benefit").equals(obj2.get("benefit"))) {
								log.info(obj1.get("id")+" : "+obj1.get("productId")+" "+obj1.get("benefit")+" key: "+obj1.get("key"));
								log.info(obj2.get("id")+" : "+obj2.get("productId")+" "+obj2.get("benefit")+" key: "+obj2.get("key"));
								
								String upSql = "UPDATE sinc.coverages SET deleted = 1 WHERE id = ?";
								PreparedStatement update = con.prepareStatement(upSql);
								String id = (String)obj2.get("id");
								update.setString(1, id);
								int cnt = update.executeUpdate();
								log.info("Updated id: "+id+" cnt: "+cnt);
								//String upSql = "UPDATE sinc.orders SET isBatchable = 1 WHERE id = ?";
								//PreparedStatement update = con.prepareStatement(upSql);
								//update.setString(1, "2344d203-47bf-42b2-b36c-bcec51b9660a");
								//int cnt = update.executeUpdate();
								//log.info(cnt+" updated in sinc.orders");
								
								
								//upSql = "UPDATE sinc.batchOrders SET deleted = 1 WHERE orderId = ?";
								//update = con.prepareStatement(upSql);
								//update.setString(1, "2344d203-47bf-42b2-b36c-bcec51b9660a");
								//cnt = update.executeUpdate();
								//log.info(cnt+" updated in sinc.batchOrders");
								log.info("******");
							}
						}
					}
				}
			}
			
			
			log.info("end");
		} catch (Exception e) {
			log.error("error",e);
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (Exception e) {}
			}
		}
	}
	
	public void run() {
		Connection con = null;
		BufferedWriter bw = null;
		log.info("started");
		String sql;
		try {
			File results = new File("/tmp/dupeCoverage.csv");
			bw = new BufferedWriter(new FileWriter(results));
			bw.write("\"group\",\"groupId\",\"orderId\",\"covId\",\"productId\",\"benefit\",\"key\"");
			bw.newLine();
			
			con = Utils.getConnection();
			TreeMap<String,String> groups = Utils.getActiveGroups();
			HashMap<String,String> ids = new HashMap<String,String>();
			
			for (Iterator<String> it = groups.keySet().iterator(); it.hasNext();) {
				String groupName = it.next();
				ids.put(groups.get(groupName), groupName);
			}
			
			int num = 0;
			int dupeCnt = 0;
			
			for (Iterator<String> gId = ids.keySet().iterator(); gId.hasNext();) { // for each groupId, pull the orders and look for dupes
				boolean groupHasDupes = false;
				String groupId = gId.next();
				String groupName = ids.get(groupId);
				
				sql = "SELECT id FROM sinc.orders WHERE completed = 1 AND deleted = 0 AND groupId = ?";
				PreparedStatement idSelect = con.prepareStatement(sql);
				idSelect.setString(1, groupId);
				ResultSet oIds = idSelect.executeQuery();
				HashSet<String> orderIds = new HashSet<String>();
				while (oIds.next()) {
					orderIds.add(oIds.getString("id"));
				}
				oIds.close();
				
				log.info(groupName+" has "+orderIds.size()+" completed orders.");
				
				sql = "SELECT id, orderId, productId, benefit, data FROM sinc.coverages WHERE orderId = ? AND deleted = 0";
				PreparedStatement select = con.prepareStatement(sql);
				for (Iterator<String> oit = orderIds.iterator(); oit.hasNext();) {
					num++;
					if (num%100 == 0) {
						log.info("Processed "+num+" orders.");
					}
					String orderId = oit.next();
					select.setString(1, orderId);
					ResultSet rs = select.executeQuery();
					JSONArray covs = new JSONArray();
					while(rs.next()) {
						Blob b = rs.getBlob("data");
						byte[] bdata = b.getBytes(1, (int) b.length());
						String data = new String(bdata);
						JSONObject cov = new JSONObject(new JSONTokener(data));
						covs.put(cov);
					}
					rs.close();
					JSONObject obj1 = null, obj2 = null;
					if (covs.length() > 0) {
						boolean hasDupes = false;
						String delSql = "UPDATE sinc.coverages SET deleted = 1 WHERE id = ?";
						PreparedStatement del = con.prepareStatement(delSql);
						for (int i=0; i<covs.length(); i++) {
							obj1 = (JSONObject)covs.get(i);
							if (i < covs.length()-1) {
								for (int j=i+1; j<covs.length(); j++) {
									obj2 = (JSONObject)covs.get(j);
									// if obj1 == obj2 dupe record.
									if (orderId.equals("7300003c-58b7-4fa7-af52-25f9d2f23bf7")) {
										log.info(obj1.get("id")+" : "+obj1.get("productId")+" "+obj1.get("benefit")+" key: "+obj1.get("key"));
										log.info(obj2.get("id")+" : "+obj2.get("productId")+" "+obj2.get("benefit")+" key: "+obj2.get("key"));
										log.info("-----------");
									}
									if (obj1.get("productId").equals(obj2.get("productId")) && obj1.get("key").equals(obj2.get("key")) && obj1.get("benefit").equals(obj2.get("benefit"))) {
										//log.info(obj1.get("id")+" : "+obj1.get("productId")+" "+obj1.get("benefit")+" key: "+obj1.get("key"));
										//log.info(obj2.get("id")+" : "+obj2.get("productId")+" "+obj2.get("benefit")+" key: "+obj2.get("key"));
										hasDupes = true;
										groupHasDupes = true;
										bw.write("\""+groupName+"\",\""+groupId+"\",\""+obj1.get("orderId")+"\",\""+obj1.get("id")+"\",\""+obj1.get("productId")+"\",\""+obj1.get("benefit")+"\",\""+obj1.get("key")+"\"");
										bw.newLine();
										bw.write("\""+groupName+"\",\""+groupId+"\",\""+obj2.get("orderId")+"\",\""+obj2.get("id")+"\",\""+obj2.get("productId")+"\",\""+obj2.get("benefit")+"\",\""+obj2.get("key")+"\"");
										bw.newLine();
										
										// remove dupe coverage line
										String id = (String)obj2.get("id");
										del.setString(1, id);
										del.executeUpdate();
									}
								}
							}
							obj2 = null;
						}
						if (del != null) {
							del.close();
						}
						obj1 = null;
						if (hasDupes) {
							dupeCnt++;
							if (dupeCnt%100 == 0) {
								log.info("So far found "+dupeCnt+" orders with duplicate coverage.");
							}
						}
					}
				}
				// check to see if this group had any dupes and rebatch all orders for the group.
				if (groupHasDupes) {
					new Thread(new ResetBatch(groupId)).start();
				}
			}
			log.info("Found "+dupeCnt+" orders with dupe coverages.");
            log.info("done");
		} catch (Exception e) {
			log.error("AuditProcessor Error",e);
		} finally {
			try {
				con.close();
			} catch (Exception e) {}
			try {
				bw.close();
			} catch (Exception e) {}
		}

	}
	
	public void run5() {
		Connection con = null;
		BufferedWriter bw = null;
		log.info("started");
		try {
			con = Utils.getConnection();
			HashMap<String,String> ids = new HashMap<String,String>();
			
			String sql = "SELECT id, name FROM sinc.groups WHERE deleted = 0 AND active = 1";
			/*
			PreparedStatement grpQ = con.prepareStatement(sql);
			ResultSet grpIds = grpQ.executeQuery();
			while(grpIds.next()) {
				ids.put(grpIds.getString("id"),grpIds.getString("name"));
			}
			grpIds.close();
			*/
			
			// For Testing
			//ids.put("71573da2-1d84-468d-94f4-490b47516602", "Pinebrook Community Answers");
			//ids.put("140ca2f1-8b55-4bd6-b4f1-4c546371594c", "West Coast Dental Services");
			//ids.put("2a367e1b-bef2-4fc6-b657-d79da60c2e0d", "Hotelier Linen");
			//ids.put("5ac1dbf7-8103-467a-ae93-9cd4b781c3b0", "Holmes Foods");
			//ids.put("c1129f9e-e57f-4053-abd4-6d5e5ac6913e", "Atrium Innovations");
			//ids.put("cc528706-ac81-42c2-96bc-135d55bc9dcb","STAT Source, Inc.");
			//ids.put("140ca2f1-8b55-4bd6-b4f1-4c546371594c", "Do not know");
			//ids.put("36e62a1b-2389-4920-a72e-130fba073d2e", "Apple Bus Company");
			//ids.put("2be84712-45c5-43df-95b5-2f06063b6894", "Southern Craft Manufacturing");
			ids.put("2be84712-45c5-43df-95b5-2f06063b6894", "Southern Craft Mfg.");
			
			for (Iterator<String> gId = ids.keySet().iterator(); gId.hasNext();) { // for each groupId, pull the orders and look for dupes
				String groupId = gId.next();
				String groupName = ids.get(groupId);
				
				sql = "SELECT memberId, dateSaved, id FROM sinc.orders WHERE completed = 1 AND deleted = 0 AND type != 'IMPORTED' and groupId = ?";
				PreparedStatement select = con.prepareStatement(sql);
				
				select.setString(1, groupId);
				ResultSet rs = select.executeQuery();
				
				ArrayList<OrderInfo> orders = new ArrayList<OrderInfo>();
				while(rs.next()) {
					String memberId = rs.getString("memberId");
					String orderId = rs.getString("id");
					Date dt = rs.getTimestamp("dateSaved");
					Calendar day = Calendar.getInstance();
					day.setTime(dt);
					OrderInfo info = new OrderInfo(orderId,memberId,day);
					//log.info(info.toString());
					orders.add(info);
				}
				rs.close();
				log.info(groupName+" has "+orders.size()+" completed orders.");
				if (!orders.isEmpty()) {
					HashSet<OrderInfo> batchables = new HashSet<OrderInfo>();
					int size = orders.size();
					OrderInfo[] infos = new OrderInfo[size];
					infos = (OrderInfo[])orders.toArray(infos);
					OrderInfo latest = null;
					HashSet<Integer> matched = new HashSet<Integer>();
					for (int i=0; i<infos.length; i++) {
						if (matched.contains(i)) {
							continue;
						}
						latest = infos[i];
						if (i<infos.length-1) {
							for (int j=i+1; j<infos.length; j++) {
								if (!matched.contains(new Integer(j)) && infos[i].sameMember(infos[j])) {
									//log.info(infos[i].toString());
									//log.info(infos[j].toString());
									//log.info("------");
									matched.add(new Integer(j));
									if (infos[i].compareTo(infos[j]) < 0) {
										latest = infos[j];
									}
								}
							}
						}
						batchables.add(latest);
					}
					if (!batchables.isEmpty()) {
						for (Iterator<OrderInfo> it = batchables.iterator(); it.hasNext();) {
							OrderInfo latestOne = it.next();
							//String upSql = "UPDATE sinc.orders SET isBatchable = 1 WHERE id = ?";
							//PreparedStatement update = con.prepareStatement(upSql);
							//update.setString(1, latestOne.getOrderId());
							//int cnt = update.executeUpdate();
						}
					}
				}
			
			}
            log.info("done");
		} catch (Exception e) {
			log.error("AuditProcessor Error",e);
		} finally {
			try {
				con.close();
			} catch (Exception e) {}
		}

	}
	
	class OrderInfo {
		String orderId;
		String memberId;
		Calendar dateSaved;
		
		public OrderInfo(String orderId, String memberId, Calendar dateSaved) {
			this.orderId = orderId;
			this.memberId = memberId;
			this.dateSaved = dateSaved;
		}
		public String getOrderId() {
			return orderId;
		}
		
		public String getMemberId() {
			return memberId;
		}
		
		public Calendar getDateSaved() {
			return dateSaved;
		}
		
		public int compareTo(OrderInfo obj) {
			Calendar cal = obj.getDateSaved();
			return dateSaved.compareTo(cal);
		}
		
		public boolean sameMember(OrderInfo obj) {
			if (obj.getMemberId().equals(memberId)) {
				return true;
			} else {
				return false;
			}
		}
		
		public String toString() {
			StringBuffer out = new StringBuffer();
			if (orderId != null) {
				out.append("orderId: "+orderId+" ");
			}
			if (memberId != null) {
				out.append("memberId: "+memberId+" ");
			}
			if (dateSaved != null) {
				Date dt = dateSaved.getTime();
				out.append("dateSaved: "+dt);
			}
			return out.toString();
		}
	}

}
