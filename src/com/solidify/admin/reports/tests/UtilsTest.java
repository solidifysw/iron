package com.solidify.admin.reports.tests;

import static org.junit.Assert.*;

import com.solidify.admin.reports.Utils;
import org.json.JSONObject;
import org.junit.Test;

public class UtilsTest {

	@Test
	public void testSameMember() {
		JSONObject obj1 = new JSONObject();
		JSONObject obj2 = new JSONObject();
		obj1.put("ssn","123");
		obj2.put("ssn", "123");
		assertTrue(Utils.sameMember(obj1, obj2));
	}

	@Test public void testSameMemberEmptySSNs() {
		JSONObject obj1 = new JSONObject();
		JSONObject obj2 = new JSONObject();
		obj1.put("ssn","");
		obj2.put("ssn", "");
		assertFalse(Utils.sameMember(obj1, obj2));
	}

}
