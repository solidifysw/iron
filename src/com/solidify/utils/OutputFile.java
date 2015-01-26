package com.solidify.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashSet;

/**
 * Created by jrobins on 1/23/15.
 */
public class OutputFile {
    private final String path;
    private BufferedWriter out;
    private final int type;
    private String delim;
    private final LinkedHashSet<String> columns;
    public static final int CSV = 1;
    public static final int PIPE = 2;

    public OutputFile(String path, int type, LinkedHashSet columns) {
        this.path = path;
        this.type = type;
        this.columns = columns;
        if (type == CSV) {
            this.delim = ",";
        } else if (type == PIPE) {
            this.delim = "|";
        }
        try {
            openFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openFile() throws IOException {
        out = new BufferedWriter(new FileWriter(path));
        StringBuilder sb = new StringBuilder();
        for (Iterator<String> it = columns.iterator(); it.hasNext();) {
            String col = it.next();
            // if the a csv and there is a comma in the field name, put ""'s around the column name
            if (type == CSV && col.indexOf(",")>0) {
                sb.append("\""+col+"\"");
                if (it.hasNext()) {
                    sb.append(delim);
                }
            } else {
                sb.append(col);
                if (it.hasNext()) {
                    sb.append(delim);
                }
            }
        }
        out.write(sb.toString());
        out.newLine();
    }

    public void write(WriteToFile obj) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (Iterator<String> it = columns.iterator(); it.hasNext();) {
            String col = it.next();
            String val = obj.get(col) != null ? obj.get(col) : "";
            if (type == CSV && val.indexOf(",") > 0) {
                sb.append("\""+val+"\"");
                if (it.hasNext()) {
                    sb.append(delim);
                }
            } else {
                sb.append(val);
                if (it.hasNext()) {
                    sb.append(delim);
                }
            }
        }
        out.write(sb.toString());
        out.newLine();
    }
}
