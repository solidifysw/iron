package com.solidify.admin.reports;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Stack;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

/*
 * Runs through all of the completed orders in the system looking for potential dupes or missing coverage lines in the coverages table.
 * This was created because of the iPad sync issue with double tapping the upload orders button on a bad Internet connection.  The
 * server code maintains a single set of coverage records for an employee that is updated when an order blob is saved.  The original
 * order blobs are saved in the orders table so we have the source order data. There is a time 
 * comparison in that code that is trying to determine the latest version of the record being saved and updating the orderId that the
 * coverage record is associated with.  The issue is that the timestamp wasn't stored in the database correctly so it wasn't evaluating
 * the time comparison correctly which resulted in coverage records being moved to the wrong order or double entered in the coverage table.
 * 
 * This audit code pulls the order blobs for a group and builds in memory copies of them that contain only the necessary values for auditing
 * dramatically reducing their size.  If 2 orders are within a couple of minutes of each other and have the same coverage lines, they are assumed
 * to be dupes and the coverage table is checked to make sure they match.  If the blob data doesn't match what is written in the coverages table,
 * the data is written to a file in the system temp folder as dupeReport.txt.
 * 
 * To run this code, use run the order-audit servlet from the admin login i.e.: http://localhost:8080/sinc/admin/order-audit
 * 
 */
public class AuditProcessor implements Runnable {
	private static final Logger log = LogManager.getLogger();
	
	private String groupId;
	
	public AuditProcessor(String groupId) {
		this.groupId = groupId;
	}
	
	/**
	 * For testing the parser on an order blob stored in a file (/tmp/sample.json)
	 * @param args
	 */
	/*
	public static void main(String[] args) {
		try {
			log.info("started");
			JsonFactory f = new JsonFactory();
			JsonParser jp = f.createParser(new File("/tmp/sample.json"));
	
		    JsonToken current = null;
		    HashSet<String> skip = new HashSet<String>();
		    skip.add("declineReasons"); skip.add("keepCoverage"); skip.add("disclosureQuestions"); skip.add("prePostTaxSelections"); skip.add("questionAnswers"); skip.add("enrollment"); skip.add("imported"); skip.add("current");
	
		    current = jp.nextToken();
		    if (current != JsonToken.START_OBJECT) {
		      System.out.println("Error: root should be object: quiting.");
		      return;
		    }
	
		    String field = null;
		    JSONObject order = new JSONObject();
		    while ((current = jp.nextToken()) != JsonToken.END_OBJECT) {
		    	if (current == JsonToken.FIELD_NAME) {
		    		field = jp.getCurrentName();
		    		//log.info(field);
		    		if ("member".equals(field)) {
		    			Utils.buildMember(order,jp);
					} else if ("data".equals(field)) {
						current = jp.nextToken();
						Utils.buildCovs(order,jp);
					} else if ("dateSaved".equals(field)) {
						jp.nextToken();
						Long dateSaved = jp.getValueAsLong();
						order.put("dateSaved", dateSaved);
						SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
						long dateSavedL = dateSaved.longValue();
						Date dt = new Date(dateSavedL);
						String date = df.format(dt);
						order.put("date",date); 
					} else if (skip.contains(field)) {
						current = jp.nextToken();
						if (current == JsonToken.START_ARRAY || current == JsonToken.START_OBJECT) {
							jp.skipChildren();
						}
					}
		    	}
		    }
		    log.info(order.toString());
		    log.info("end");
		} catch (Exception e) {
			log.error("Problem",e);
		}
	}
	*/
	public void run() {
		Connection con = null;
		log.info("start");
		try {
			String groupName = "groupName";
			String groupId = "groupId";
			con = Utils.getConnection();
			String sql = "SELECT id, productId, benefit, data FROM sinc.coverages WHERE orderId = ? AND deleted = 0";
			PreparedStatement select = con.prepareStatement(sql);
			select.setString(1, "2344d203-47bf-42b2-b36c-bcec51b9660a");
			ResultSet rs = select.executeQuery();
			JSONArray covs = new JSONArray();
			while(rs.next()) {
				String data = rs.getString("data");
				JSONObject cov = new JSONObject(new JSONTokener(data));
				//log.info(cov.toJSONString());
				//log.info(rs.getString("productId")+" : "+rs.getString("benefit"));
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
	
	/**
	 * Test version that works a single order.

	public void run4() {
		Connection con = null;
		try {
			con = Utils.getConnection();
			Statement stmt1 = con.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,java.sql.ResultSet.CONCUR_READ_ONLY);
			stmt1.setFetchSize(Integer.MIN_VALUE);
			ResultSet res = stmt1.executeQuery("SELECT data,id,dateSaved FROM sinc.orders WHERE memberId ='16cab9fc-0ff6-4763-8473-0519a82f6dc2'");
			String groupId = "2be84712-45c5-43df-95b5-2f06063b6894";
			String groupName = "Southern Craft Mfg";
			//String orderId = "8be936fb-8db0-445f-9660-87482498adaa"; // this one is working
			HashSet<String> ids = new HashSet<String>();
			while (res.next()) {
				String orderId = res.getString("id");
				ids.add(orderId);
				Blob b = res.getBlob("data");
				byte[] bdata = b.getBytes(1, (int) b.length());
				//String tmp = new String(bdata);
				//log.info(tmp);
				try {
					JSONObject slimOrder = Utils.buildObject(groupId, groupName, orderId, "16cab9fc-0ff6-4763-8473-0519a82f6dc2", bdata);
					if (slimOrder != null) {
						log.info(slimOrder.toString());
					}
				} catch (Exception e) {
					log.info("parse problem skipping this order");
					log.error("parse",e);
				}
			}
			res.close();
			if (!ids.isEmpty()) {
				for (Iterator<String> it = ids.iterator(); it.hasNext();) {
					String orderId = it.next();
					String sql = "SELECT productId, benefit FROM sinc.coverages WHERE orderId = ? AND deleted = 0";
					PreparedStatement select = con.prepareStatement(sql);
					select.setString(1, orderId);
					ResultSet rs = select.executeQuery();
					while (rs.next()) {
						log.info(orderId+" : "+rs.getString("productId")+" "+rs.getString("benefit"));
					}
					rs.close();
				}
			}
			log.info("done");
		} catch (Exception e) {
			log.error("Audit Processor", e);
		} finally {
			try {
				con.close();
			} catch (Exception e) {}
		}
	} */

	/*
	public void run6() {
		Connection con = null;
		try {
			con = Utils.getConnection();
			HashMap<String,String> ids = new HashMap<String,String>();
			String sql = "SELECT id, name FROM sinc.groups WHERE deleted = 0";

			//PreparedStatement grpQ = con.prepareStatement(sql);
			//ResultSet grpIds = grpQ.executeQuery();
			//while(grpIds.next()) {
				//ids.put(grpIds.getString("id"),grpIds.getString("name"));
			//}
			//grpIds.close();

			ids.put("2a367e1b-bef2-4fc6-b657-d79da60c2e0d", "Hotelier Linen");
			sql = "SELECT id FROM sinc.orders WHERE groupId = '2a367e1b-bef2-4fc6-b657-d79da60c2e0d' AND completed = 1 AND deleted = 0";
			PreparedStatement select = con.prepareStatement(sql);
			ResultSet ords = select.executeQuery();
			while(ords.next()) {
				String orderId = ords.getString("id");
				
				Statement stmt1 = con.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,java.sql.ResultSet.CONCUR_READ_ONLY);
				stmt1.setFetchSize(Integer.MIN_VALUE);
				ResultSet res = stmt1.executeQuery("SELECT data,memberId FROM sinc.orders WHERE id = '"+orderId+"'");
				String groupId = "2a367e1b-bef2-4fc6-b657-d79da60c2e0d";
				String groupName = "Hotelier Linen";
				if (res.next()) {
					Blob b = res.getBlob("data");
					byte[] bdata = b.getBytes(1, (int) b.length());
					String tmp = new String(bdata);
					//log.info(tmp);
					try {
						JSONObject slimOrder = Utils.buildObject(groupId, groupName, orderId, res.getString("memberId"), bdata);
						
						if (slimOrder.get("ssn").equals("683-35-2580")) {
							log.info(slimOrder.toString());
						}
					} catch (Exception e) {
						log.info("parse problem skipping this order");
						log.error("parse",e);
					}
				}
				res.close();
			}
			log.info("done");
		} catch (Exception e) {
			log.error("Audit Processor", e);
		} finally {
			try {
				con.close();
			} catch (Exception e) {}
		}
	}
	*/

	/**
	 * Main code where the full audit process runs for all groups in the system.
	 */
    /*
	public void run8() {
		Connection con = null;
		BufferedWriter bw = null;
		log.info("started");
		int cnt = 0;
		try {
			File results = new File("/tmp/dupeReport.txt");
			bw = new BufferedWriter(new FileWriter(results));
			
			con = Utils.getConnection();
			HashMap<String,String> ids = new HashMap<String,String>();
			String sql = "SELECT id, name FROM sinc.groups WHERE deleted = 0";

			//PreparedStatement grpQ = con.prepareStatement(sql);
			//ResultSet grpIds = grpQ.executeQuery();
			//while(grpIds.next()) {
				//ids.put(grpIds.getString("id"),grpIds.getString("name"));
			//}
			//grpIds.close();

			ids.put("71573da2-1d84-468d-94f4-490b47516602", "Pinebrook Community Answers");
			//ids.put("140ca2f1-8b55-4bd6-b4f1-4c546371594c", "West Coast Dental Services");
			//ids.put("2a367e1b-bef2-4fc6-b657-d79da60c2e0d", "Hotelier Linen");
			//ids.put("5ac1dbf7-8103-467a-ae93-9cd4b781c3b0", "Holmes Foods");
			//ids.put("c1129f9e-e57f-4053-abd4-6d5e5ac6913e", "Atrium Innovations");
			//ids.put("cc528706-ac81-42c2-96bc-135d55bc9dcb","STAT Source, Inc.");
			//groupId: cc528706-ac81-42c2-96bc-135d55bc9dcb
			//groupName: STAT Source, Inc.
			
			
			for (Iterator<String> gId = ids.keySet().iterator(); gId.hasNext();) { // for each groupId, pull the orders and look for dupes
				JSONArray groupOrders = new JSONArray();
				String groupId = gId.next();
				//log.info("groupId: "+groupId);
				String groupName = ids.get(groupId);
				//log.info("groupName: "+groupName);
				
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
				
				for (Iterator<String> oit = orderIds.iterator(); oit.hasNext();) {
					String orderId = oit.next();
					//log.info(orderId);
					Statement stmt1 = con.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,java.sql.ResultSet.CONCUR_READ_ONLY);
					stmt1.setFetchSize(Integer.MIN_VALUE);
					ResultSet orderRes = stmt1.executeQuery("SELECT data,memberId FROM sinc.orders WHERE id ='"+orderId+"'");
					
					while (orderRes.next()) {
						Blob b = orderRes.getBlob("data");
						byte[] bdata = b.getBytes(1, (int) b.length());
						try {
							JSONObject slimOrder = Utils.buildObject(groupId, groupName, orderId, orderRes.getString("memberId"), bdata);
							//log.info(slimOrder.toJSONString());
							groupOrders.put(slimOrder);
						} catch (Exception e) {
							// If problem parsing, just skip the order
							String tmp = new String(bdata);
							log.info(tmp);
							log.error("parse",e);
							bw.write("Problem parsing data in order: "+orderId+"\n");
							bw.write("--- PARSE ERROR ---\n");
							bw.write(tmp);
							bw.write("\n--- END PARSE ERROR ---\n");
						}
					}
					orderRes.close();
				}
				
				//log.info(groupName+" - groupOrders size: "+groupOrders.size());
				bw.write(groupName+" has "+groupOrders.length()+" orders.\n");
				
				JSONArray dupes = null;
				if (groupOrders.length() > 0) {
					HashSet<Integer> matched = new HashSet<Integer>();
					int len = groupOrders.length(); 
					// loop through each order and look for dupes
					for (int i=0; i<len; i++) {
						cnt++;
						if (cnt%100 == 0) log.info("Processed "+cnt+" orders.");
						if (matched.contains(new Integer(i))) {  //no need to test one if it has already been matched.
							continue;
						}
						JSONObject obj1 = (JSONObject)groupOrders.get(i);
						if (obj1.get("testUser").equals("YES")) {
							continue;
						}
						//bw.write("testing index i: "+i+"\n");
						dupes = new JSONArray();
						JSONArray sameMem = new JSONArray();
						boolean first = true;
						if (i < len-1) {
							for (int j=i+1; j<len; j++) {
								JSONObject obj2 = (JSONObject)groupOrders.get(j);
								if (sameTime(obj1, obj2)) {
									//bw.write("index j same member: "+j+"\n");
									if (first) {
										dupes.put(obj1);
										first = false;
										//bw.write("i: "+i+" "+obj1.toJSONString()+"\n");
									}
									dupes.put(obj2);
									//bw.write("j: "+j+" "+obj2.toJSONString()+"\n");
									matched.add(new Integer(j));
								} else if (sameMember(obj1,obj2)) {
									sameMem.put(obj2);
								}
							}
							// At this point, have compared the source order to rest of the orders for the group and found all of its dupes
							boolean dupeIsLatest = true;
							if (obj1.get("memberId").equals("7c70b87e-cf4a-49af-add0-7da4f975df13")) {
								log.info(obj1.toString());
							}
							log.info(obj1.get("firstName")+" "+obj1.get("lastName")+" ssn: "+obj1.get("ssn"));
							log.info("dupes.size: "+dupes.length());
							log.info("sameMem.size: "+sameMem.length());
							if (dupes.length() > 0) {
								// Check to see if the dupes are the latest order by comparing the save time for dupes to the orders in sameMem.
								if (sameMem.length()>0) {
									JSONObject dupe1 = (JSONObject)dupes.get(0);  // just grab the first dupe and compare to the samMem entries.
									for (int x = 0; x<sameMem.length(); x++) {
										Calendar dupeCal = Calendar.getInstance();
										long dupeTime = ((Long)dupe1.get("dateSaved")).longValue();
										dupeCal.setTime(new Date(dupeTime));
										Calendar sameCal = Calendar.getInstance();
										JSONObject sameObj = (JSONObject)sameMem.get(x);
										long sameTime = ((Long)sameObj.get("dateSaved")).longValue();
										sameCal.setTime(new Date(sameTime));
										if (sameCal.compareTo(dupeCal) > 0) {
											dupeIsLatest = false;
											break;
										}
									}
								}
								if (!dupeIsLatest) {
									continue;  // there is a later order than the potential dupe so move on
								}
								
								
								//bw.write("found "+dupes.size()+" dupes for order: "+obj1.get("orderId")+" memberId: "+obj1.get("memberId")+".\n");
								int length = dupes.length();
								Calendar latest = null;
								JSONObject latestOrder = null;
								for (int x=0; x<length; x++) {
									JSONObject o = (JSONObject)dupes.get(x);
									long dtSaved = ((Long)o.get("dateSaved")).longValue();
									if (latest == null) {
										latest = Calendar.getInstance();
										latest.setTime(new Date(dtSaved));
										latestOrder = o;
									} else {
										Calendar srcDt = Calendar.getInstance();
										srcDt.setTime(new Date(dtSaved));
										if (srcDt.after(latest)) {
											latest = srcDt;
											latestOrder = o;
										}
									}
									
									//bw.write("orderId: "+o.get("orderId")+" "+o.get("firstName")+" "+o.get("lastName")+" "+o.get("dateOfBirth")+" "+o.get("ssn")+" "+o.get("date")+"\n");
									//JSONArray cs = (JSONArray)o.get("covs");
									//for (int y=0; y<cs.size(); y++) {
										//JSONObject cov = (JSONObject)cs.get(y);
										//bw.write("\t"+cov.get("productId")+": "+cov.get("benefit")+"\n");
									//}
								}
								//bw.write(" ---------- \n");
								// now have the latest order object.  Get the coverage lines.
								sql = "SELECT productId, benefit FROM sinc.coverages WHERE orderId = ? AND deleted = 0";
								PreparedStatement select = con.prepareStatement(sql);
								String orderId = (String)latestOrder.get("orderId");
								select.setString(1, orderId);
								ResultSet rs = select.executeQuery();
								JSONArray covsTable = new JSONArray();
								//bw.write("--- coverage recs ---\n");
								while (rs.next()) {
									String productId = rs.getString("productId");
									String benefit = rs.getString("benefit");
									//bw.write("\t"+productId+": "+benefit+"\n");
									JSONObject tmp = new JSONObject();
									tmp.put("productId", productId);
									tmp.put("benefit",benefit);
									covsTable.put(tmp);
								}
								rs.close();
								//compare covs to covsTable
								if (covsTable.length() > 0) {
									
									JSONArray covs = (JSONArray)latestOrder.get("covs");
									int covsLen = covs.length();
									int covsTableLen = covsTable.length();
									if (covsLen != covsTableLen) {
										dump(bw, latestOrder, covsTable, dupes);
									} else {
										for (int x=0; x<covsLen; x++) {
											boolean match = false;
											for (int y=0; y<covsTableLen; y++) {
												if (areEqual((JSONObject)covs.get(x), (JSONObject)covsTable.get(y))) {
													match = true;
													break;
												}
											}
											if (!match) {
												dump(bw, latestOrder, covsTable, dupes);
												break;
											}
										}
									}
								}
							}
						}
					}
					//bw.write(" --- End same member ---\n");
					// looped through the remaining array elements, see if we found dupes for this one
					
				}
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
	} */

    /*
	public void run5() {
		Connection con = null;
		BufferedWriter bw = null;
		log.info("started");
		try {
			File results = new File("/tmp/missingReport.csv");
			bw = new BufferedWriter(new FileWriter(results));
			bw.write("\"groupName\",\"groupId\",\"memberId\",\"OrderId\"\n");
			con = Utils.getConnection();
			HashMap<String,String> ids = new HashMap<String,String>();
			String sql = "SELECT id, name FROM sinc.groups WHERE deleted = 0";
			
			PreparedStatement grpQ = con.prepareStatement(sql);
			ResultSet grpIds = grpQ.executeQuery();
			while(grpIds.next()) {
				ids.put(grpIds.getString("id"),grpIds.getString("name"));
			}
			grpIds.close();
			
			//ids.put("ed674445-5683-4ac1-ab72-0baa2f17c5a4","Dale Medical Center");
			//ids.put("5ac1dbf7-8103-467a-ae93-9cd4b781c3b0", "Holmes Foods");
			
			for (Iterator<String> gId = ids.keySet().iterator(); gId.hasNext();) { // for each groupId, pull the orders and look for dupes
				String groupId = gId.next();
				//log.info("groupId: "+groupId);
				String groupName = ids.get(groupId);
				//log.info("groupName: "+groupName);
				
				sql = "SELECT id, memberId FROM sinc.orders WHERE completed = 1 AND deleted = 0 AND isBatchable = 0 AND groupId = ?";
				PreparedStatement idSelect = con.prepareStatement(sql);
				idSelect.setString(1, groupId);
				ResultSet oIds = idSelect.executeQuery();
				HashMap<String,String> orderIds = new HashMap<String,String>();
				while (oIds.next()) {
					orderIds.put(oIds.getString("id"),oIds.getString("memberId"));
				}
				oIds.close();
				
				bw.write(groupName+" has "+orderIds.size()+" orders with isBatchable = 0.\n");
				
				HashSet<String> inBatchOrders = new HashSet<String>();
				HashMap<String,String> notInBatchOrders = new HashMap<String,String>();
				if (orderIds.size() == 0) {
					continue;
				}
				int len = orderIds.size();
				for (Iterator<String> it = orderIds.keySet().iterator(); it.hasNext();) {
					String id = it.next();
					String memId = orderIds.get(id);
					if (inBatchOrders.contains(memId)) {
						continue;  // if this member already has a batched order, skip it
					}
					String batchSql = "SELECT COUNT(*) AS cnt FROM batchOrders WHERE orderId = ?";
					PreparedStatement batchOrdersCnt = con.prepareStatement(batchSql);
					batchOrdersCnt.setString(1, id);
					ResultSet rs = batchOrdersCnt.executeQuery();
					if (rs.next()) {
						int num = rs.getInt("cnt");
						if (num > 0) {
							inBatchOrders.add(memId);
							if (notInBatchOrders.containsKey(memId)) {
								notInBatchOrders.remove(memId);
							}
						} else {
							notInBatchOrders.put(memId,id);
						}
					}
				}
				
				if (notInBatchOrders.size() > 0) {
					for (Iterator<String>it = notInBatchOrders.keySet().iterator(); it.hasNext();) {
						String memId = it.next();
						String id = notInBatchOrders.get(memId);
						String upSql = "UPDATE sinc.orders SET isBatchable = 1 WHERE id = ?";
						PreparedStatement update = con.prepareStatement(upSql);
						update.setString(1, id);
						int numUpdated = update.executeUpdate();
						log.info("orderId: "+id+" updated: "+numUpdated);
						bw.write("\""+groupName+"\",\""+groupId+"\",\""+memId+"\",\""+id+"\"\n");
					}
				}
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
	*/

	/*
	public static JSONObject buildObject(String groupId, String groupName, String orderId, String memberId, byte[] bdata) throws Exception {
		
		HashSet<String> skip = new HashSet<String>();
		skip.add("declineReasons"); skip.add("keepCoverage"); skip.add("disclosureQuestions"); skip.add("prePostTaxSelections"); skip.add("questionAnswers"); skip.add("enrollment"); skip.add("imported"); skip.add("current");
		JSONObject order = new JSONObject();
		order.put("groupId", groupId); // ***
		order.put("name",groupName);
		order.put("orderId", orderId);
		order.put("memberId",memberId);
		//log.info(orderId);
		//if (orderId.equals("8be936fb-8db0-445f-9660-87482498adaa")) log.info(blob);
		JsonFactory factory = new JsonFactory();
		JsonParser jp = factory.createParser(bdata);
		JsonToken current = null;
		
		// start of json {
		current = jp.nextToken();
		if (current != JsonToken.START_OBJECT) {
			log.info("error parsing");
			return null;
		}
		// loop through the json 
		String field = null;
		while((current = jp.nextToken()) != JsonToken.END_OBJECT) {
			//log.info(jp.getCurrentName());
			if (current == JsonToken.FIELD_NAME) {
				field = jp.getCurrentName();
				//log.info(field);
				if ("member".equals(field)) {
					buildMember(order,jp);
					if (order.get("memberId").equals("7c70b87e-cf4a-49af-add0-7da4f975df13")) {
						String tmp = new String(bdata);
						log.info(tmp);
					}
				} else if ("data".equals(field)) {
					current = jp.nextToken();
					buildCovs(order,jp);
				} else if ("dateSaved".equals(field)) {
					//log.info("found dateSaved");
					jp.nextToken();
					Long dateSaved = jp.getValueAsLong();
					order.put("dateSaved", dateSaved);
					SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
					long dateSavedL = dateSaved.longValue();
					Date dt = new Date(dateSavedL);
					String date = df.format(dt);
					order.put("date",date); 
				} else if (skip.contains(field)) {
					//log.info("skip: "+field);
					jp.nextToken();
					jp.skipChildren();
				}
			}
		}
		return order;
	}
	
	private static void buildCovs(JSONObject order, JsonParser jp) throws JsonParseException, IOException {
		String field;
		JsonToken current = null;
		JSONObject cov = null;
		JSONArray covs = new JSONArray();
		HashSet<String> skips = new HashSet<String>();
		skips.add("member"); skips.add("dependents"); skips.add("emergencyContacts"); skips.add("beneficiaries"); skips.add("signature"); skips.add("listBillAdjustments"); skips.add("dependents"); 
		skips.add("coveredDependents"); skips.add("beneficiaries"); skips.add("premiums"); skips.add("carrierElectionData");
		HashSet<String> saves = new HashSet<String>();
		saves.add("productId"); saves.add("planName"); saves.add("startDate"); saves.add("benefit"); saves.add("endDate"); saves.add("type"); saves.add("subType"); saves.add("deduction"); saves.add("totalYearly"); saves.add("electionTier");
		
		while ((current = jp.nextToken()) != JsonToken.END_OBJECT) {
			if (current == JsonToken.FIELD_NAME) {
				field = jp.getCurrentName();
				//log.info("-- "+field);
				if (skips.contains(field)) {
					// skip all this crap
					current = jp.nextToken();
					if (current == JsonToken.START_ARRAY || current == JsonToken.START_OBJECT) {
						jp.skipChildren();
					}
				}  else {
					// these are coverages
					cov = new JSONObject();
					jp.nextToken(); // open tag uuid : {
					while((current = jp.nextToken()) != JsonToken.END_OBJECT) {
						if (current == JsonToken.FIELD_NAME) {
							field = jp.getCurrentName();
							//log.info("** "+field);
							if (saves.contains(field)) {
								current = jp.nextToken();
								cov.put(field, jp.getValueAsString());
							} else if (skips.contains(field)) {
								current = jp.nextToken();
								if (current == JsonToken.START_ARRAY || current == JsonToken.START_OBJECT) {
									jp.skipChildren();
								}
							}
						}
					}
					covs.put(cov);
				} 
			}
		} 
		order.put("covs", covs);
	}
	
	private static void buildMember(JSONObject order, JsonParser jp) throws JsonParseException, IOException {
		String field = null;
		JsonToken current = null;
		HashSet<String> fields = new HashSet<String>();
		fields.add("firstName"); fields.add("lastName"); fields.add("dateOfBirth"); fields.add("ssn");
		HashSet<String> skips = new HashSet<String>();
		skips.add("dependents"); skips.add("emergencyContacts"); skips.add("beneficiaries");
		while((current = jp.nextToken()) != JsonToken.END_OBJECT) {
			//log.info(jp.getCurrentName());
			if(current == JsonToken.FIELD_NAME) {
				field = jp.getCurrentName();
				if (field == "personal") {
					while((current = jp.nextToken()) != JsonToken.END_OBJECT) {
						if (current == JsonToken.FIELD_NAME) {
							field = jp.getCurrentName();
							if (fields.contains(field)) {
								current = jp.nextToken();
								order.put(field, jp.getValueAsString());
							} else if (skips.contains(field)) {
								current = jp.nextToken();
								if (current == JsonToken.START_ARRAY || current == JsonToken.START_OBJECT) {
									jp.skipChildren();
								}
							}
						}
					}
				} else if ("tags".equals(field) || "newHire".equals(field)) {
					current = jp.nextToken();
					if (current == JsonToken.START_ARRAY || current == JsonToken.START_OBJECT) {
						jp.skipChildren();
					}
				} else if ("testUser".equals(field)) {
					current = jp.nextToken();
					order.put("testUser", jp.getValueAsString());
				}
			}
		}
	}
	*/
	private static void dump(BufferedWriter bw, JSONObject latestOrder, JSONArray covRecs, JSONArray dupes) throws IOException {
		String orderId = (String)latestOrder.get("orderId");
		JSONArray covs = (JSONArray)latestOrder.get("covs");
		bw.write("\norderId: "+orderId+" in group "+(String)latestOrder.get("name")+" has a problem.\n");
		
		bw.write("Duplicate blobs:\n");
		for (int i=0; i<dupes.length(); i++) {
			JSONObject o = (JSONObject)dupes.get(i);
			bw.write("orderId: "+o.get("orderId")+" "+o.get("firstName")+" "+o.get("lastName")+" "+o.get("dateOfBirth")+" "+o.get("ssn")+" "+o.get("date")+"\n");
			JSONArray cs = (JSONArray)o.get("covs");
			for (int y=0; y<cs.length(); y++) {
				JSONObject cov = (JSONObject)cs.get(y);
				bw.write("\t"+cov.get("productId")+": "+cov.get("benefit")+"\n");
			}
		}
		bw.write("--- End of Duplicate Blobs ---\n");
		
		bw.write("---- Latest Blob Lines: ----\n");
		for (int i=0; i<covs.length(); i++) {
			JSONObject tmp = (JSONObject)covs.get(i);
			String productId = (String)tmp.get("productId");
			String benefit = (String)tmp.get("benefit");
			bw.write("\tproductId: "+productId+" benefit: "+benefit+"\n");
		}
		bw.write("---- coverages table lines ----\n");
		for (int i=0; i<covRecs.length(); i++) {
			JSONObject tmp = (JSONObject)covRecs.get(i);
			String productId = (String)tmp.get("productId");
			String benefit = (String)tmp.get("benefit");
			bw.write("\tproductId: "+productId+" benefit: "+benefit+"\n");
		}
		bw.write("**************************\n");
	}
	
	private static boolean sameTime(JSONObject one, JSONObject two) {
		boolean out = false;
		Long dateSaved = (Long)one.get("dateSaved");
		Date dt = new Date(dateSaved.longValue());
		Calendar oneCal = Calendar.getInstance();
		oneCal.setTime(dt);
		
		dateSaved = (Long)two.get("dateSaved");
		dt = new Date(dateSaved.longValue());
		Calendar twoCal = Calendar.getInstance();
		twoCal.setTime(dt);
		
		Calendar twoMinus = (Calendar)twoCal.clone();
		Calendar twoPlus = (Calendar)twoCal.clone();
		twoMinus.add(Calendar.MINUTE, -1);
		twoPlus.add(Calendar.MINUTE, 1);
		if (oneCal.compareTo(twoMinus) >= 0 && oneCal.compareTo(twoPlus) <= 0) {
			out = true;
		}
		return out;
	}
	
	private static boolean areEqual(JSONObject one, JSONObject two) {
		boolean out = false;
		if (one.get("productId").equals(two.get("productId")) && one.get("benefit").equals(two.get("benefit"))) {
			out = true;
		}
		return out;
	}
	
	private static boolean sameMember(JSONObject one, JSONObject two) {
		if (one.get("ssn").equals(two.get("ssn"))) {
			return true;
		} else {
			return false;
		}
	}

}
