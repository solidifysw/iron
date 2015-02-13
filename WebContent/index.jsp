<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="org.json.JSONArray" %>
<%@page import="org.json.JSONObject" %>
<%@page import="java.util.HashMap" %>
<%@page import="java.util.TreeMap" %>
<%@page import="java.util.Iterator" %>
<%@ page import="com.solidify.admin.reports.*" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%
String message = null;
TreeMap<String,String> groups = Utils.getGroupList();
String reportToRun = request.getParameter("reportToRun");
if (reportToRun != null && reportToRun.equals("declinations")) {
	String groupId = request.getParameter("groupId");
	new Thread(new AllDeclinations(groupId)).start();
	message = "";
} else if (reportToRun != null && reportToRun.equals("missingCoverage")) {
	String groupId = request.getParameter("groupId");
	new Thread(new MissingOrders(groupId)).start();
} else if (reportToRun != null && reportToRun.equals("resetBatches")) {
	String groupId = request.getParameter("groupId");
	new Thread(new ResetBatch(groupId)).start();
} else if (reportToRun != null && reportToRun.equals("dumpBlob")) {
	String orderId = request.getParameter("orderId");
	new Thread(new DumpBlob(orderId,true)).start();
} else if (reportToRun != null && reportToRun.equals("dumpGroupOrders")) {
	String groupId = request.getParameter("groupId");
	new Thread(new DumpGroupOrders(groupId)).start();
} else if (reportToRun != null && reportToRun.equals("medicalEnrolled")) {
	new Thread(new MedicalEnrolled()).start();
} else if (reportToRun != null && reportToRun.equals("ancillaryEnrolled")) {
	new Thread(new AncillaryEnrolled()).start();
} else if (reportToRun != null && reportToRun.equals("lookForDupes")) {
	String groupId = request.getParameter("groupId");
	new Thread(new FixDupes(groupId)).start();
}
%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Admin Reports</title>
</head>
<body>
<h1>Reports</h1>
<% if (message != null) { %>
	<br><span><font color="green"><%=message %></font></span><br>
<% } %>
<h2>Declined Coverages for Group</h2>
<form action="index.jsp" method="POST">
	<input type="hidden" name="reportToRun" value="declinations">
	<table>
		<tr>
			<td>
				<select name="groupId">
				<% if (groups != null) { %>
					<% for (Iterator<String> it = groups.keySet().iterator(); it.hasNext();) { %>
						<% String name = it.next(); %>
						<% String id = groups.get(name); %>
						<option value="<%=id %>"><%=name%></option>
					<% } %>
				<% } %>
				</select>
			</td>
			<td><input type="submit" value="Run"></td>
		</tr>
	</table>
</form>
<hr>
<h2>Rebatch Missing Coverages</h2>
<form action="index.jsp" method="POST">
	<input type="hidden" name="reportToRun" value="missingCoverage">
	<table>
		<tr>
			<td>
				<select name="groupId">
				<% if (groups != null) { %>
					<% for (Iterator<String> it = groups.keySet().iterator(); it.hasNext();) { %>
						<% String name = it.next(); %>
						<% String id = groups.get(name); %>
						<option value="<%=id %>"><%=name%></option>
					<% } %>
				<% } %>
				</select>
			</td>
			<td><input type="submit" value="Run"></td>
		</tr>
	</table>
</form>
<hr>
<h2>Fix Dupe Coverage Lines</h2>
<form action="index.jsp" method="POST">
	<input type="hidden" name="reportToRun" value="lookForDupes">
	<table>
		<tr>
			<td>
				<select name="groupId">
					<% if (groups != null) { %>
					<% for (Iterator<String> it = groups.keySet().iterator(); it.hasNext();) { %>
					<% String name = it.next(); %>
					<% String id = groups.get(name); %>
					<option value="<%=id %>"><%=name%></option>
					<% } %>
					<% } %>
				</select>
			</td>
			<td><input type="submit" value="Run"></td>
		</tr>
	</table>
</form>
<hr>
<h2>Reset Batches for a Group</h2>
<form action="index.jsp" method="POST">
	<input type="hidden" name="reportToRun" value="resetBatches">
	<table>
		<tr>
			<td>
				<select name="groupId">
				<% if (groups != null) { %>
					<% for (Iterator<String> it = groups.keySet().iterator(); it.hasNext();) { %>
						<% String name = it.next(); %>
						<% String id = groups.get(name); %>
						<option value="<%=id %>"><%=name%></option>
					<% } %>
				<% } %>
				</select>
			</td>
			<td><input type="submit" value="Run"></td>
		</tr>
	</table>
</form>
<hr>
<h2>Dump Order for OrderId</h2>
<form action="index.jsp" method="POST">
	<input type="hidden" name="reportToRun" value="dumpBlob">
	<table>
		<tr>
			<td>OrderId:&nbsp;</td>
			<td>
				<input type="text" name="orderId" />
			</td>
			<td><input type="submit" value="Run" /></td>
		</tr>
	</table>
</form>
<hr>
<h2>Dump Latest Orders for Group</h2>
<form action="index.jsp" method="POST">
	<input type="hidden" name="reportToRun" value="dumpGroupOrders">
	<table>
		<tr>
			<td>
				<select name="groupId">
					<% if (groups != null) { %>
					<% for (Iterator<String> it = groups.keySet().iterator(); it.hasNext();) { %>
					<% String name = it.next(); %>
					<% String id = groups.get(name); %>
					<option value="<%=id %>"><%=name%></option>
					<% } %>
					<% } %>
				</select>
			</td>
			<td><input type="submit" value="Run"></td>
		</tr>
	</table>
</form>
<hr>
<h2>Run Medical Enrolled Report</h2>
<form action="index.jsp" method="POST">
	<input type="hidden" name="reportToRun" value="medicalEnrolled">
	<table>
		<tr>
			<td><input type="submit" value="Run" /></td>
		</tr>
	</table>
</form>
<hr>
<h2>Run Ancillary Enrolled Report</h2>
<form action="index.jsp" method="POST">
	<input type="hidden" name="reportToRun" value="ancillaryEnrolled">
	<table>
		<tr>
			<td><input type="submit" value="Run" /></td>
		</tr>
	</table>
</form>
<hr>
</body>
</html>