package com.solidify.admin.reports;

import java.io.UnsupportedEncodingException;
import java.util.*;
import java.io.IOException;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;

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

	public static int getMemberCount(String groupId) throws SQLException {
		Connection con = getConnection();
		int cnt = 0;
		try {
			cnt = getMemberCount(groupId,con);
		} finally {
			if (con != null) con.close();
		}
		return cnt;
	}

	public static int getMemberCount(String groupId, Connection con) throws SQLException {
		PreparedStatement getCnt = null;
		ResultSet cntRs = null;
		int cnt = 0;
		try {
			String sql = "SELECT COUNT(*) AS cnt FROM sinc.members WHERE groupId = ? and deleted = 0";
			getCnt = con.prepareStatement(sql);
			getCnt.setString(1, groupId);
			cntRs = getCnt.executeQuery();
			if (cntRs.next()) {
				cnt = cntRs.getInt("cnt");
			}

		} finally {
			if (getCnt != null) getCnt.close();
			if (cntRs != null) cntRs.close();
		}
		return cnt;
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
			sql += " ORDER BY name, active ASC";
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
				String tmp = new String(bdata,"UTF-8");
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
	 */
	public static void dumpOrderBlob(String orderId) {
		boolean includeSourceBlob = false;
		dumpOrderBlob(orderId,includeSourceBlob);
	}
	
	/**
	 * Print's the order data blob in the database to the log
	 * @param orderId
	 */
	public static JSONObject dumpOrderBlob(String orderId, boolean includeSourceBlob) {
		Connection con = null;
		PreparedStatement select = null;
		ResultSet rs = null;
		JSONObject slimOrder = null;
		try {
			con = getConnection();
			
			String sql = "SELECT data, memberId, isBatchable FROM sinc.orders WHERE id = ?";
			select = con.prepareStatement(sql);
			select .setString(1, orderId);
			rs = select.executeQuery();
			if (rs.next()) {
				String memberId = rs.getString("memberId");
				boolean isBatchable = rs.getBoolean("isBatchable");
				if (isBatchable) {
					log.info("isBatchable column is true");
				} else {
					log.info("isBatchable column is false");
				}
				Blob b = rs.getBlob("data");
				byte[] bdata = b.getBytes(1, (int) b.length());
				if (includeSourceBlob) {
					String tmp = new String(bdata);
					log.info(tmp);
				}
				try {
					slimOrder = buildObject("groupId", "groupName", orderId, memberId, bdata);
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
		return slimOrder;
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

	public static int numberOfOrdersForGroup(String groupId) {
		int out = 0;
		try {

		} catch (Exception e) {

		} finally {

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
			int cnt = 0;
			for (Iterator<String> it = orderIds.iterator(); it.hasNext();) {
				String orderId = it.next();
				stmt1 = con.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,java.sql.ResultSet.CONCUR_READ_ONLY);
				stmt1.setFetchSize(Integer.MIN_VALUE);
				orderRes = stmt1.executeQuery("SELECT data,memberId FROM sinc.orders WHERE id ='"+orderId+"'");
				
				while (orderRes.next()) {
					cnt++;
					Blob b = orderRes.getBlob("data");
					byte[] bdata = b.getBytes(1, (int) b.length());
					//String tmp1 = new String(bdata);
					//log.info(tmp1);
					try {
						JSONObject slimOrder = buildObject(groupId, groupName, orderId, orderRes.getString("memberId"), bdata);
						if (slimOrder.get("testUser").equals("NO")) {
							groupOrders.put(slimOrder);
						}
					} catch (Exception e) {
						String tmp = new String(bdata);
                        log.error(e);
						log.error(tmp);
					}
				}
				orderRes.close();
				stmt1.close();
			}
			//log.info("Found "+cnt+" total orders before removing test users.");
			
			// groupOrders contains the slim versions of the orders for this group
			// Find the latest order for each individual
			//log.info("groupOrders.length: "+groupOrders.length());
			if (groupOrders.length() > 0) {
				//log.info(groupName+" has "+groupOrders.length()+" total completed orders.");
				out = findLatestOrders(groupOrders);
				//log.info("latest orders: "+out.size());

				//out = new ArrayList<JSONObject>();
				//for (int i=0; i<groupOrders.length(); i++) {
					//out.add((JSONObject) groupOrders.get(i));
				//}
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
		//log.info(groupName+" "+out.size()+" real orders.");
		return out;
	}

	public static ArrayList<JSONObject> getAllOrdersForGroup(String groupId) {
		Connection con = null;
		ArrayList out = null;
		try {
			con = getConnection();
			out = getAllOrdersForGroup(groupId,con);
		} catch (Exception e) {
			log.error("error",e);
		} finally {
			try {
				con.close();
			} catch (Exception e) {}
		}
		return out;
	}

	public static ArrayList<JSONObject> getAllOrdersForGroup(String groupId, Connection con) {
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
			sql = "SELECT id FROM sinc.orders WHERE completed = 1 AND deleted = 0 AND type != 'IMPORTED' AND groupId = ?";
			idSelect = con.prepareStatement(sql);
			idSelect.setString(1, groupId);
			oIds = idSelect.executeQuery();
			while (oIds.next()) {
				orderIds.add(oIds.getString("id"));
			}
			oIds.close();
			idSelect.close();

			// query each order and parse the data blob for pertinent info
			int cnt = 0;
			for (Iterator<String> it = orderIds.iterator(); it.hasNext();) {
				String orderId = it.next();
				stmt1 = con.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY);
				stmt1.setFetchSize(Integer.MIN_VALUE);
				orderRes = stmt1.executeQuery("SELECT data,memberId FROM sinc.orders WHERE id ='" + orderId + "'");

				while (orderRes.next()) {
					cnt++;
					Blob b = orderRes.getBlob("data");
					byte[] bdata = b.getBytes(1, (int) b.length());

					//String tmp1 = new String(bdata);
					//log.info(tmp1);
					try {
						JSONObject slimOrder = buildObject(groupId, groupName, orderId, orderRes.getString("memberId"), bdata);
						if (slimOrder.get("testUser").equals("NO")) {
							groupOrders.put(slimOrder);
						}
					} catch (Exception e) {
						String tmp = new String(bdata);
						log.error(tmp);
					}
				}
				orderRes.close();
				stmt1.close();
			}
			log.info("Found "+cnt+" total orders before removing test users.");
			if (groupOrders.length() > 0) {
				for (int i=0; i<groupOrders.length(); i++) {
					out.add((JSONObject)groupOrders.get(i));
				}
			}
			return out;
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
		log.info(groupName+" "+out.size()+" total orders.");
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
				JSONObject obj1 = (JSONObject) groupOrders.get(i);
				latest = obj1;
				if (i < groupOrders.length() - 1) {
					for (int j = i + 1; j < groupOrders.length(); j++) {
						if (!matched.contains(new Integer(j))) {
							JSONObject obj2 = (JSONObject) groupOrders.get(j);
							if (sameMember(obj1, obj2)) {
								matched.add(new Integer(j));
								latest = laterOf(latest, obj2);
								//log.info(obj1);
								//log.info(obj2);
								//log.info(latest.get("orderId"));
								//log.info("-----");
							} else {

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
		String ssn1 = obj1.getString("ssn");
		if (ssn1 == null) {
			ssn1 = "";
		}
		String dob1 = obj1.getString("dateOfBirth");
		if (dob1 == null) {
			dob1 = "";
		}

		String ssn2 = obj2.getString("ssn");
		if (ssn2 == null) {
			ssn2 = "";
		}
		String dob2 = obj2.getString("dateOfBirth");
		if (dob2 == null) {
			dob2 = "";
		}

		if (!"".equals(ssn1) && !"".equals(ssn2) && !"".equals(dob1) && !"".equals(dob2)) {
			return ssn1.equals(ssn2) && dob1.equals(dob2);
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
		JSONObject order = new JSONObject();
		order.put("groupId", groupId); // ***
		order.put("name",groupName);
		order.put("orderId", orderId);
		order.put("memberId",memberId);
		return buildObject(order,bdata);
	}

	public static JSONObject buildObject(JSONObject order, byte[] bdata) {
		JsonFactory factory = new JsonFactory();
		JSONObject out = null;
		try {
			String tmp = new String(bdata,"UTF-8");
			JsonParser jp = factory.createParser(tmp);
			out = buildObject(order,jp);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return out;
	}
	/**
	 * Stream parses the order blob from the orders table and builds a small JSONObject containing the minimum amount of information
	 * @return
	 * @throws Exception
	 */
	public static JSONObject buildObject(JSONObject order, JsonParser jp) throws IOException, SQLException {
		
		HashSet<String> skip = new HashSet<String>();
		//skip.add("declineReasons"); 
		skip.add("keepCoverage"); skip.add("disclosureQuestions"); skip.add("prePostTaxSelections"); skip.add("enrollment"); skip.add("imported"); skip.add("current"); skip.add("usedDefinedContributions");
		skip.add("lifeChangeTypes"); skip.add("member");

		JsonToken current = null;
		HashMap<String,String> declineReasons = null;
		String field = null;
		// start of json {
		//try {
			current = jp.nextToken();
			if (current != JsonToken.START_OBJECT) {
				log.info("error parsing");
				return null;
			}
			while((current = jp.nextToken()) != JsonToken.END_OBJECT) {
				if (current == JsonToken.FIELD_NAME) {
					field = jp.getCurrentName();
					//if ("member".equals(field)) {
						//buildMember(order,jp);
					//} else
                    if ("data".equals(field)) {
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
					} else if ("isBatchable".equals(field)) {
						jp.nextToken();
						order.put("isBatchable",jp.getValueAsBoolean());
					} else if ("questionAnswers".equals(field)) {
                        getQuestionAnswers(order,jp);
                    }
				}
			}
		//} catch (Exception e) {
		//	e.printStackTrace();
			//log.error(field);
		//}
		if (declineReasons != null && !declineReasons.isEmpty()) {
			//try {
				JSONArray covs = (JSONArray) order.get("covs");
				for (int i = 0; i < covs.length(); i++) {
					JSONObject cov = (JSONObject) covs.get(i);
                    // log.info(cov.toString());
					String covUuid = (String) cov.get("splitId");
					if (declineReasons.containsKey(covUuid)) {
						cov.put("declineReason", declineReasons.get(covUuid));
					}
				}
			//} catch (Exception e) {
				//log.error(order.toString());
			//}
		}
        findClass(order);
		return order;
	}

    private static void getQuestionAnswers(JSONObject order, JsonParser jp)  throws JsonParseException, IOException {
        // questionAnswers: { weight-ee: {id:xxx,questionText:xxx...},weight-sp:...}
        JSONObject answers = new JSONObject();
        JsonToken current = null;
        String field = null;
        String objectName = null;
        if ((current = jp.nextToken()) == JsonToken.START_OBJECT) {
            while((current = jp.nextToken()) != JsonToken.END_OBJECT) {
                if (current == JsonToken.FIELD_NAME) {
                    objectName = jp.getCurrentName();
                    JSONObject jo = new JSONObject();
                    while((current = jp.nextToken()) != JsonToken.END_OBJECT) {
                        if (current == JsonToken.FIELD_NAME) {
                            field = jp.getCurrentName();
                            jp.nextToken();
                            try {
                                jo.put(field, jp.getValueAsString());
                            } catch (Exception e) {
                                jo.put(field, jp.getValueAsInt());
                            }
                        }
                    }
                    answers.put(objectName,jo);
                }
            }
        }
        Set set = answers.keySet();
        if (!set.isEmpty()) {
            order.put("questionAnswers", answers);
        }
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
		skips.add("emergencyContacts"); skips.add("beneficiaries"); skips.add("signature"); skips.add("listBillAdjustments");
		skips.add("premiums"); skips.add("carrierElectionData");
		HashSet<String> saves = new HashSet<String>();
		saves.add("productId"); saves.add("planName"); saves.add("startDate"); saves.add("benefit"); saves.add("endDate"); saves.add("type"); saves.add("subType"); saves.add("deduction"); saves.add("totalYearly"); saves.add("electionTier"); saves.add("splitId");
        saves.add("benefitLevel");

		while ((current = jp.nextToken()) != JsonToken.END_OBJECT) {
			if (current == JsonToken.FIELD_NAME) {
				field = jp.getCurrentName();
                //log.info("field: "+field);
                if (field.equals("member")) {
                    buildMember(order, jp);
                } else if (skips.contains(field)) {
                    // skip all this crap
                    current = jp.nextToken();
                    if (current == JsonToken.START_ARRAY || current == JsonToken.START_OBJECT) {
                        jp.skipChildren();
                    }
                } else if (field.equals("dependents")) {
                    JSONArray deps = new JSONArray();
                    current = jp.nextToken();
                    if (current == JsonToken.START_ARRAY) {
                        while (current != JsonToken.END_ARRAY) {
                            current = jp.nextToken();
                            if (current == JsonToken.START_OBJECT) {
                                JSONObject dep = new JSONObject();
                                while (current != JsonToken.END_OBJECT) {
                                    current = jp.nextToken();
                                    if (current == JsonToken.FIELD_NAME) {
                                        field = jp.getCurrentName();
                                        if (field.equals("deleted") && !jp.getValueAsBoolean()) {
                                            dep.put("deleted", false);
                                        } else if (field.equals("deleted")) {
                                            dep.put("deleted", true);
                                        } else {
                                            jp.nextToken();
                                            dep.put(field, jp.getValueAsString());
                                        }
                                    }
                                }
                                deps.put(dep);
                            }
                        }
                    }
                    if (order.has("dependents")) {
                        JSONArray dps = order.getJSONArray("dependents");
                        if (dps.length() == 0) {
                            order.put("dependents", deps);
                        }
                    } else {
                        order.put("dependents", deps);
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
							} else if ("beneficiaries".equals(field)) {
                                JSONArray bens = new JSONArray();
								while((current = jp.nextToken()) != JsonToken.END_ARRAY) {
									JSONObject ben = new JSONObject();
									current = jp.nextToken();
									if (current == JsonToken.END_ARRAY) { // just break if empty array
										break;
									}
									while ((current = jp.nextToken()) != JsonToken.END_OBJECT) {
										if (current != JsonToken.START_OBJECT) {
											field = jp.getCurrentName();
											jp.nextToken();
											if (field.equals("percent")) {
												ben.put(field, jp.getValueAsInt());
											} else {
												ben.put(field, jp.getValueAsString());
											}
										}
									}
									bens.put(ben);

								}
                                if (bens.length()>0) {
                                    cov.put("beneficiaries", bens);
                                }
                            } else if (field.equals("coveredDependents")) {
                                JSONArray coveredDeps = new JSONArray();
                                while((current = jp.nextToken()) != JsonToken.END_ARRAY) {
                                    if (current != JsonToken.START_ARRAY) {
                                        coveredDeps.put(jp.getValueAsString());
                                    }
                                }
                                cov.put("coveredDependents",coveredDeps);
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
        HashSet<String> info = new HashSet();
        info.add("occupation"); info.add("locationCode"); info.add("locationDescription"); info.add("occupation"); info.add("status"); info.add("deductionsPerYear");
        info.add("department"); info.add("dateOfHire"); info.add("hoursPerWeek"); info.add("annualSalary"); info.add("employeeId");
		HashSet<String> personal = new HashSet();
		personal.add("firstName"); personal.add("lastName"); personal.add("dateOfBirth"); personal.add("ssn"); personal.add("gender");
		personal.add("address1"); personal.add("address2"); personal.add("city"); personal.add("state"); personal.add("zip"); personal.add("phone");
        personal.add("email");
		HashSet<String> skips = new HashSet();
		skips.add("emergencyContacts"); skips.add("beneficiaries"); skips.add("dependents");
		JSONArray deps = new JSONArray();

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
								if (personal.contains(field)) {
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
					}  else if (info.contains(field)) {
						current = jp.nextToken();
						order.put(field, jp.getValueAsString());
					}
				}
			}
		}
	}

    private static void findClass(JSONObject order) throws SQLException, IOException {
        String orderId = order.getString("orderId");
        String memberId = order.getString("memberId");
        Connection con = getConnection();
        Statement stmt1 = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        stmt1.setFetchSize(Integer.MIN_VALUE);
        ResultSet rs = stmt1.executeQuery("SELECT data, memberId FROM sinc.orders WHERE id ='" + orderId + "'");
        JSONArray classes = null;
        while (rs.next()) {
            Blob b = rs.getBlob("data");
            byte[] bdata = b.getBytes(1, (int) b.length());
            String json = new String(bdata,"UTF-8");
            JSONObject jo = parseEnrollment(json);
             classes = jo.getJSONArray("classes");
        }
        rs.close();
        stmt1.close();
        // for each class, see if the member is in it and get the class name
        if (classes == null) {
            return;
        }
        boolean found = false;
        for (int i=0; i<classes.length(); i++) {
            if (found) {
                break;
            }
            JSONObject cls = null;
            String classId = classes.getString(i);
            String sql = "SELECT data FROM sinc.classes WHERE deleted = 0 AND id = ?";
            PreparedStatement select = con.prepareStatement(sql);
            select.setString(1,classId);
            rs = select.executeQuery();
            if (rs.next()) {
                cls = new JSONObject(rs.getString("data"));
            }
            select.close();
            rs.close();
            if (cls == null) {
                return;
            }
            JSONArray members = cls.getJSONArray("members");
            for (int j=0; j<members.length(); j++) {
                if (members.getString(j).equals(memberId)) {
                    order.put("class",cls.getString("name"));
                    found = true;
                    break;
                }
            }
        }
    }

    public static JSONObject parseEnrollment(String json) throws IOException {
        JSONObject out = new JSONObject();
        JsonFactory factory = new JsonFactory();
        JsonParser jp = factory.createParser(json);
        JsonToken current = jp.nextToken();
        String field = null;
        HashSet<String> skip = new HashSet<String>();
        skip.add("packages"); skip.add("employer"); skip.add("loginScheme");
        JSONArray productConfigs = new JSONArray();
        JSONArray classes = new JSONArray();

        // make sure there is a {
        if (current != JsonToken.START_OBJECT) {
            log.info("error parsing");
            return null;
        }

        while((current = jp.nextToken()) != JsonToken.END_OBJECT) {
            if (current == JsonToken.FIELD_NAME) {
                field = jp.getCurrentName();
                if (field.equals("enrollment")) {
                    while ((current = jp.nextToken()) != JsonToken.END_OBJECT) {
                        if (current == JsonToken.FIELD_NAME) {
                            field = jp.getCurrentName();
                            if (field.equals("classes")) {
                                current = jp.nextToken();
                                if (current == JsonToken.START_ARRAY) {
                                    while((current = jp.nextToken()) != JsonToken.END_ARRAY) {
                                        classes.put(jp.getValueAsString());
                                    }
                                }
                            } else if (field.equals("productConfigs")) {
                                current = jp.nextToken();
                                if (current == JsonToken.START_ARRAY) {
                                    while ((current = jp.nextToken()) != JsonToken.END_ARRAY) {
                                        productConfigs.put(jp.getValueAsString());
                                    }
                                }
                            } else {
                                current = jp.nextToken();
                                if (current == JsonToken.START_OBJECT || current == JsonToken.START_ARRAY) {
                                    jp.skipChildren();
                                }
                            }
                        }
                    }
                } else if ((current = jp.nextToken()) == JsonToken.START_OBJECT || current == JsonToken.START_ARRAY) {
                    jp.skipChildren();
                }
            }
        }
        out .put("classes",classes);
        out.put("productConfigs",productConfigs);
        return out;
    }

}
