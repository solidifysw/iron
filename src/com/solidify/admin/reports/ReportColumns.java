package com.solidify.admin.reports;

/**
 * Created by jennifermac on 12/22/14.
 */
public class ReportColumns {
    private String[] columnNames;

    public ReportColumns(String ... columnNames) {
        this.columnNames = columnNames;
    }

    public String toString() {
        String out = "";
        StringBuilder sb = new StringBuilder();
        for (String columnName : columnNames) {
            sb.append("\""+columnName+"\",");
        }
        if (sb.length() > 0) {
            int idx = sb.lastIndexOf(",");
            out = sb.substring(0,idx);
        }
        return out;
    }

    public String[] getColumnNames() {
        return columnNames;
    }
}
