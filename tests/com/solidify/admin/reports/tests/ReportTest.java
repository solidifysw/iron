package com.solidify.admin.reports.tests;

import com.solidify.admin.reports.ReportColumns;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import com.solidify.admin.reports.Report;

public class ReportTest {

    private Report report;

    @Before public void setUp() {
        ReportColumns headings = new ReportColumns("Col1","Col2","Col3");
        report = new Report("test",headings);
    }

    //@Test public void testToString() {
     //   assertEquals("\"Col1\",\"Col2\",\"Col3\"",report.write());
   // }
}
