package com.solidify.admin.reports;

import java.util.ArrayList;
import java.io.IOException;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeMap;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.naming.Context;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

public class Utils {
	private static final Logger log = LogManager.getLogger();
	private static DataSource dataSource;
	private static final String ACTIVE = "1";
	private static final String INACTIVE = "0";
	private static final String ALL = "ALL";
	
	public static TreeMap<String,String> getInactiveGroups() {
		return getGroupList(INACTIVE);
	}
	
	public static TreeMap<String,String> getActiveGroups() {
		return getGroupList(ACTIVE);
	}
	
	public static TreeMap<String,String> getGroupList() {
		return getGroupList(ALL);
	}
	
	/**
	 * Returns the list of groupNames and groupIds in a TreeMap to preserve the order.
	 * @param activeFilter indicates which groups to include, active, inactive or all
	 * @return
	 */
	public static TreeMap<String,String> getGroupList(String activeFilter) {
		TreeMap<String,String> groups = new TreeMap<String,String>();
		Connection con = null;
		PreparedStatement select = null;
		ResultSet rs = null;
		try {
			con = getConnection();
			
			String sql = "SELECT id, name FROM sinc.groups WHERE deleted = 0 ";
			if (activeFilter.equals(ACTIVE)) {
				sql += "AND active = 1 ";
			} else if (activeFilter.equals(INACTIVE)) {
				sql += "AND active = 0";
			}
			sql += " ORDER BY name ASC";
			select = con.prepareStatement(sql);
			rs = select.executeQuery();
			while (rs.next()) {
				groups.put(rs.getString("name"), rs.getString("id"));
			}
			rs.close();
			select.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				con.close();
			} catch (Exception e) {}
			try {
				select.close();
			} catch (Exception e) {}
			try {
				rs.close();
			} catch (Exception e) {}
		}
		return groups;
	}
	
	/**
	 * Gets the groupName for this groupId
	 * @param groupId
	 * @return
	 * @throws SQLException
	 */
	public static String getGroupName(String groupId) throws SQLException {
		String groupName = null;
		Connection con = null;
		PreparedStatement select = null;
		try {
			con = Utils.getConnection();
			String sql = "SELECT name FROM sinc.groups WHERE id = ?";
			select = con.prepareStatement(sql);
			select.setString(1, groupId);
			ResultSet rs = select.executeQuery();
			if (rs.next()) {
				groupName = rs.getString("name");
			}
			rs.close();
			select.close();
		} catch (SQLException e) {
			throw e;
		} finally {
			try {
				select.close();
			} catch (Exception e) {}
			try {
				con.close();
			} catch (Exception e) {}
		}
		return groupName;
	}
	
	/**
	 * Provides database connection objects for building queries
	 * @return database connection object
	 */
	public static Connection getConnection() {
		Connection con = null;
		Context context = null;
		try {
			if (dataSource == null) {
				context = new InitialContext();
				dataSource = (DataSource)context.lookup("java:/comp/env/jdbc/MyLocalDB");
			}
			con = dataSource.getConnection();
		} catch (Exception e) {
			log.error("Can't get connections",e);
		}
		return con;
	}

	/**
	 * Prints the member's data blob in the database to the log
	 * @param memberId
	 */
	public static void dumpMemberBlob(String memberId) {
		Connection con = null;
		PreparedStatement select = null;
		ResultSet rs = null;
		try {
			con = getConnection();
			
			String sql = "SELECT data FROM sinc.members WHERE id = ?";
			select = con.prepareStatement(sql);
			select .setString(1, memberId);
			rs = select.executeQuery();
			if (rs.next()) {
				Blob b = rs.getBlob("data");
				byte[] bdata = b.getBytes(1, (int) b.length());
				String tmp = new String(bdata);
				log.info(tmp);
			}
			rs.close();
			select.close();
		} catch (Exception e) {
			log.error("error",e);
		} finally {
			try {
				con.close();
			} catch (Exception e) {}
			try {
				select.close();
			} catch (Exception e) {}
			try {
				rs.close();
			} catch (Exception e) {}
		}
	}
	
	/**
	 * Print's the data stored in the order data blob in the database to the log.  
	 * @param orderId the id of the order to dump
	 * @param includeSourceBlob will include the full data blob along with the slimmed-down version if true
	 */
	public static void dumpOrderBlob(String orderId) {
		boolean includeSourceBlob = false;
		dumpOrderBlob(orderId,includeSourceBlob);
	}
	
	/**
	 * Print's the order data blob in the database to the log
	 * @param orderId
	 */
	public static void dumpOrderBlob(String orderId, boolean includeSourceBlob) {
		Connection con = null;
		PreparedStatement select = null;
		ResultSet rs = null;
		try {
			con = getConnection();
			
			String sql = "SELECT data, memberId FROM sinc.orders WHERE id = ?";
			select = con.prepareStatement(sql);
			select .setString(1, orderId);
			rs = select.executeQuery();
			if (rs.next()) {
				String memberId = rs.getString("memberId");
				Blob b = rs.getBlob("data");
				byte[] bdata = b.getBytes(1, (int) b.length());
				if (includeSourceBlob) {
					String tmp = new String(bdata);
					log.info(tmp);
				}
				try {
					JSONObject slimOrder = buildObject("groupId", "groupName", orderId, memberId, bdata);
					if (slimOrder != null) {
						log.info(slimOrder.toString());
					}
				} catch (Exception e) {
					log.info("parse problem skipping this order");
					log.error("parse",e);
				}
			}
			rs.close();
			select.close();
		} catch (Exception e) {
			log.error("error",e);
		} finally {
			try {
				con.close();
			} catch (Exception e) {}
			try {
				select.close();
			} catch (Exception e) {}
			try {
				rs.close();
			} catch (Exception e) {}
		}
	}
	
	/**
	 * Returns the latest order submitted for the member id.  This code pulls all of the completed orders for this member
	 * and determines the one that was saved to the server last and returns a slimmed down version of the JSONObject (not the full blob).
	 * @param memberId
	 * @return JSONObject that contains name, dob, ssn, coverages, save date, etc.
	 */
	public static JSONObject getLatestOrderForMember(String memberId) {
		JSONObject out = null;
		Connection con = null;
		PreparedStatement select = null;
		ResultSet rs = null;
		JSONArray orders = new JSONArray();
		try {
			con = getConnection();
			
			String sql = ("SELECT id, data FROM sinc.orders WHERE memberId = ?");	
			select = con.prepareStatement(sql);
			select.setString(1, memberId);
			rs = select.executeQuery();
			if (rs.next()) {
				String orderId = rs.getString("id");
				Blob b = rs.getBlob("data");
				byte[] bdata = b.getBytes(1, (int) b.length());
				try {
					JSONObject slimOrder = buildObject("groupId", "groupName", orderId, memberId, bdata);
					orders.put(slimOrder);
				} catch (Exception e) {
					log.info("parse problem skipping this order");
					log.error("parse",e);
				}
			}
			rs.close();
			select.close();
			if (orders.length() > 0) {
				ArrayList<JSONObject> tmp = findLatestOrders(orders);
				out = tmp.get(0);
			}
		} catch (Exception e) {
			log.error("error",e);
		} finally {
			try {
				con.close();
			} catch (Exception e) {}
			try {
				select.close();
			} catch (Exception e) {}
			try {
				rs.close();
			} catch (Exception e) {}
		}
		return out;
	}
	
	/**
	 * Gets the latest orders for the group.  Will get the database connection for you.
	 * @param groupId the id of the group in the groups table.
	 * @return
	 */
	public static ArrayList<JSONObject> getLatestOrdersForGroup(String groupId) {
		Connection con = null;
		ArrayList<JSONObject> out = null;
		try {
			con = getConnection();
			out = getLatestOrdersForGroup(groupId,con);
		} catch (Exception e) {
			log.error("error",e);
		} finally {
			try {
				con.close();
			} catch (Exception e) {}
		}
		return out;
	}
	
	/**
	 *  Returns the latest order for members in this group.  
	 * @param groupId
	 * @param con The database connection object.
	 * @return JSONObject containing the stripped down blob data
	 */
	public static ArrayList<JSONObject> getLatestOrdersForGroup(String groupId, Connection con) {
		boolean ignoreIsBatchable = true;
		return getLatestOrdersForGroup(groupId, con, ignoreIsBatchable);
	}
	
	/**
	 * Returns the latest orders for the members in this group.
	 * @param groupId
	 * @param con
	 * @param ignoreIsBatchable if false, will only include orders that are not batchable by concatenating "AND isBatchable = 0" onto the query.
	 * @return
	 */
	public static ArrayList<JSONObject> getLatestOrdersForGroup(String groupId, Connection con, boolean ignoreIsBatchable) {
		ArrayList<JSONObject> out = new ArrayList<JSONObject>();
		PreparedStatement select = null;
		ResultSet rs = null;
		PreparedStatement idSelect = null;
		ResultSet oIds = null;
		Statement stmt1 = null;
		ResultSet orderRes = null;
		
		HashSet<String> orderIds = new HashSet<String>();
		JSONArray groupOrders = new JSONArray();
		String groupName = null;
		
		try {
			String sql = "SELECT name FROM sinc.groups WHERE id = ?";
			select = con.prepareStatement(sql);
			select.setString(1,groupId);
			rs = select.executeQuery();
			if (rs.next()) {
				groupName = rs.getString("name");
			}
			rs.close();
			select.close();
			
			// Get the list of orderId's for this group
			if (ignoreIsBatchable) {
				sql = "SELECT id FROM sinc.orders WHERE completed = 1 AND deleted = 0 AND type != 'IMPORTED' AND groupId = ?";
			} else {
				sql = "SELECT id FROM sinc.orders WHERE completed = 1 AND deleted = 0 AND type != 'IMPORTED' AND groupId = ? AND isBatchable = 0";
			}
			idSelect = con.prepareStatement(sql);
			idSelect.setString(1, groupId);
			oIds = idSelect.executeQuery();
			while (oIds.next()) {
				orderIds.add(oIds.getString("id"));
			}
			oIds.close();
			idSelect.close();
			
			// query each order and parse the data blob for pertinent info
			for (Iterator<String> it = orderIds.iterator(); it.hasNext();) {
				String orderId = it.next();
				stmt1 = con.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,java.sql.ResultSet.CONCUR_READ_ONLY);
				stmt1.setFetchSize(Integer.MIN_VALUE);
				orderRes = stmt1.executeQuery("SELECT data,memberId FROM sinc.orders WHERE id ='"+orderId+"'");
				
				while (orderRes.next()) {
					Blob b = orderRes.getBlob("data");
					byte[] bdata = b.getBytes(1, (int) b.length());
					//String tmp1 = new String(bdata);
					//log.info(tmp1);
					try {
						JSONObject slimOrder = buildObject(groupId, groupName, orderId, orderRes.getString("memberId"), bdata);
						groupOrders.put(slimOrder);
					} catch (Exception e) {
						String tmp = new String(bdata);
						log.error(tmp);
					}
				}
				orderRes.close();
				stmt1.close();
			}
			
			// groupOrders contains the slim versions of the orders for this group
			// Find the latest order for each individual
			if (groupOrders.length() > 0) {
				log.info(groupName+" has "+groupOrders.length()+" total completed orders.");
				out = findLatestOrders(groupOrders);
			}
		} catch (Exception e) {
			log.error("error",e);
		} finally {
			try {
				select.close();
			} catch (Exception e) {}
			try {
				rs.close();
			} catch (Exception e) {}
			try {
				idSelect.close();
			} catch (Exception e) {}
			try {
				stmt1.close();
			} catch (Exception e) {}
			try {
				oIds.close();
			} catch (Exception e) {}
			try {
				orderRes.close();
			} catch (Exception e) {}
		}
		log.info(groupName+" "+out.size()+" real orders.");
		return out;
	}
	
	/**
	 * Takes all of the orders for a group and filters out only the latest one for each member.
	 * @param groupOrders
	 * @return
	 */
	public static ArrayList<JSONObject> findLatestOrders(JSONArray groupOrders) {
		ArrayList<JSONObject> out = new ArrayList<JSONObject>();
		if (groupOrders.length() > 0) {
			JSONObject latest = null;
			HashSet<Integer> matched = new HashSet<Integer>();
			for (int i=0; i<groupOrders.length(); i++) {
				if (matched.contains(i)) {
					continue;
				}
				JSONObject obj1 = (JSONObject)groupOrders.get(i);
				latest = obj1;
				if (i<groupOrders.length()-1) {
					for (int j=i+1; j<groupOrders.length(); j++) {
						if (!matched.contains(new Integer(j))) {
							JSONObject obj2 = (JSONObject)groupOrders.get(j);
							if (sameMember(obj1,obj2)) {
								matched.add(new Integer(j));
								latest = laterOf(latest,obj2);
								//log.info(obj1);
								//log.info(obj2);
								//log.info(latest.get("orderId"));
								//log.info("-----");
							}
						}
					}
				}
				out.add(latest);
			}
		}
		return out;
	}
	
	/**
	 * Compares the 2 json objects for the ssn field and returns true if they are equal
	 * @param obj1
	 * @param obj2
	 * @return
	 */
	public static boolean sameMember(JSONObject obj1, JSONObject obj2) {
		String ssn1 = (String)obj1.get("ssn");
		String ssn2 = (String)obj2.get("ssn");
		if (ssn1 != null && ssn2 != null && !"".equals(ssn1) && !"".equals(ssn2)) {
			return ssn1.equals(ssn2);
		} else {
			return false;
		}
	}
	
	/**
	 * Compares the dateSaved fields in the 2 objects and returns the one with the later dateSaved date
	 * @param obj1
	 * @param obj2
	 * @return the later dateSaved of the 2 objects
	 */
	public static JSONObject laterOf(JSONObject obj1, JSONObject obj2) {
		Long saved1 = (Long)obj1.get("dateSaved");
		Long saved2 = (Long)obj2.get("dateSaved");
		Date dt1 = new Date(saved1);
		Date dt2 = new Date(saved2);
		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(dt1);
		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(dt2);
		if (cal1.compareTo(cal2) >= 0) {
			return obj1;
		} else {
			return obj2;
		}
	}
	
	/**
	 * Stream parses the order blob from the orders table and builds a small JSONObject containing the minimum amount of information
	 * @param groupId
	 * @param groupName
	 * @param orderId
	 * @param memberId
	 * @param bdata
	 * @return
	 * @throws Exception
	 */
	public static JSONObject buildObject(String groupId, String groupName, String orderId, String memberId, byte[] bdata) throws Exception {
		
		HashSet<String> skip = new HashSet<String>();
		//skip.add("declineReasons"); 
		skip.add("keepCoverage"); skip.add("disclosureQuestions"); skip.add("prePostTaxSelections"); skip.add("questionAnswers"); skip.add("enrollment"); skip.add("imported"); skip.add("current");
		JSONObject order = new JSONObject();
		order.put("groupId", groupId); // ***
		order.put("name",groupName);
		order.put("orderId", orderId);
		order.put("memberId",memberId);
		JsonFactory factory = new JsonFactory();
		JsonParser jp = factory.createParser(bdata);
		JsonToken current = null;
		HashMap<String,String> declineReasons = null;
		String field = null;
		// start of json {
		try {
			current = jp.nextToken();
			if (current != JsonToken.START_OBJECT) {
				log.info("error parsing");
				return null;
			}
			while((current = jp.nextToken()) != JsonToken.END_OBJECT) {
				if (current == JsonToken.FIELD_NAME) {
					field = jp.getCurrentName();
					if ("member".equals(field)) {
						buildMember(order,jp);
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
						jp.nextToken();
						jp.skipChildren();
					} else if ("declineReasons".equals(field)) {
						declineReasons = getDeclineReasons(order,jp);
					}
				}
			}
		} catch (Exception e) {
			log.error(field);
		}
		if (declineReasons != null && !declineReasons.isEmpty()) {
			JSONArray covs = (JSONArray)order.get("covs");
			for (int i=0; i<covs.length(); i++) {
				JSONObject cov = (JSONObject)covs.get(i);
				String covUuid = (String)cov.get("splitId");
				if (declineReasons.containsKey(covUuid)) {
					cov.put("declineReason", declineReasons.get(covUuid));
				}
			}
		}
		return order;
	}
	
	/**
	 * Retrieves the declineReasons from the order data blob
	 * @param order
	 * @param jp
	 * @return
	 * @throws JsonParseException
	 * @throws IOException
	 */
	private static HashMap<String,String> getDeclineReasons(JSONObject order, JsonParser jp) throws JsonParseException, IOException {
		// declineReasons: {UUID1:reason1,UUID2:reason2...}
		HashMap<String,String> declineReasons = new HashMap<String,String>();
		if (jp.nextToken() == JsonToken.START_OBJECT) {
			while (jp.nextToken() != JsonToken.END_OBJECT) {
				String covUuid = jp.getCurrentName();
				jp.nextToken();
				String declineReason = jp.getValueAsString();
				if (declineReason == null || "".equals(declineReason)) {
					log.info(covUuid);
					log.info("orderId: "+order.getString("orderId"));
				}
				declineReasons.put(covUuid, declineReason);
			}
		}
		return declineReasons;
	}
	
	/**
	 * retrieves the coverage lines from the order data blob.  Builds and array of JSONObjects and adds them to the slimmed down order object as the covs JSONArray
	 * @param order
	 * @param jp
	 * @throws JsonParseException
	 * @throws IOException
	 */
	private static void buildCovs(JSONObject order, JsonParser jp) throws JsonParseException, IOException {
		String field;
		JsonToken current = null;
		JSONObject cov = null;
		JSONArray covs = new JSONArray();
		HashSet<String> skips = new HashSet<String>();
		skips.add("member"); skips.add("dependents"); skips.add("emergencyContacts"); skips.add("beneficiaries"); skips.add("signature"); skips.add("listBillAdjustments"); skips.add("dependents"); 
		skips.add("coveredDependents"); skips.add("beneficiaries"); skips.add("premiums"); skips.add("carrierElectionData");
		HashSet<String> saves = new HashSet<String>();
		saves.add("productId"); saves.add("planName"); saves.add("startDate"); saves.add("benefit"); saves.add("endDate"); saves.add("type"); saves.add("subType"); saves.add("deduction"); saves.add("totalYearly"); saves.add("electionTier"); saves.add("splitId");
		
		while ((current = jp.nextToken()) != JsonToken.END_OBJECT) {
			if (current == JsonToken.FIELD_NAME) {
				field = jp.getCurrentName();
				if (skips.contains(field)) {
					// skip all this crap
					current = jp.nextToken();
					if (current == JsonToken.START_ARRAY || current == JsonToken.START_OBJECT) {
						jp.skipChildren();
					}
				}  else {
					// these are coverages
					cov = new JSONObject();
					cov.put("id", field);
					jp.nextToken(); // open tag uuid : {
					while((current = jp.nextToken()) != JsonToken.END_OBJECT) {
						if (current == JsonToken.FIELD_NAME) {
							field = jp.getCurrentName();
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
	
	/**
	 * Parses out the member information from the order data blob and places relevant data into a slimmed down order blob.
	 * member: {employeeId:"123", dependents:[],...personal:{firstName:"John",dateOfBirth:"03/10/1962",..}}
	 * @param order the object to populate
	 * @param jp
	 * @throws JsonParseException
	 * @throws IOException
	 */
	private static void buildMember(JSONObject order, JsonParser jp) throws JsonParseException, IOException {
		String field = null;
		JsonToken current = null;
		HashSet<String> fields = new HashSet<String>();
		fields.add("firstName"); fields.add("lastName"); fields.add("dateOfBirth"); fields.add("ssn");
		HashSet<String> skips = new HashSet<String>();
		skips.add("dependents"); skips.add("emergencyContacts"); skips.add("beneficiaries");
		
		if (jp.nextToken() == JsonToken.START_OBJECT) {
			while((current = jp.nextToken()) != JsonToken.END_OBJECT) {
				if(current == JsonToken.FIELD_NAME) {
					field = jp.getCurrentName();
					if (skips.contains(field)) {
						jp.nextToken();
						jp.skipChildren();
					} else if (field.equals("personal")) {
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
	}

}
