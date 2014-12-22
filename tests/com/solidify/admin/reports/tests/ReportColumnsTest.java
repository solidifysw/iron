package com.solidify.admin.reports.tests;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import com.solidify.admin.reports.ReportColumns;

/**
 * Created by jennifermac on 12/22/14.
 */
public class ReportColumnsTest {

    private ReportColumns headings;

    @Before public void setUp() {
        headings = new ReportColumns("Col1","Col2","Col3");
    }

    @Test public void setColumnNames() {
        assertEquals("\"Col1\",\"Col2\",\"Col3\"",headings.toString());
    }

    @Test public void testGetColumns() {
        String[] cols = headings.getColumnNames();
        assertEquals("Col1",cols[0]);
        assertEquals("Col2",cols[1]);
        assertEquals("Col3",cols[2]);
    }
}
