package com.solidify.admin.reports.tests;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import com.solidify.admin.reports.ReportRow;

/**
 * Created by jennifermac on 12/22/14.
 */
public class ReportRowTest {

    private ReportRow row;

    @Before public void setUp() {
        row = new ReportRow();
    }

    @Test public void testSet() {
        row.set("Col1","abc");
        assertEquals("abc",row.get("Col1"));
    }
}
