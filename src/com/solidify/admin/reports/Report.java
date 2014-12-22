package com.solidify.admin.reports;

import java.util.LinkedHashSet;

/**
 * Created by jennifermac on 12/22/14.
 */
public class Report {

    private String title;
    private ReportColumns cols;
    private LinkedHashSet<ReportRow> rows;

    public Report(String title, ReportColumns cols) {
        this.title = title;
        this.cols = cols;
    }
}
