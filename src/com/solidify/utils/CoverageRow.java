package com.solidify.utils;

import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by jrobins on 1/22/15.
 */
public class CoverageRow {
    private String name;
    private String memberId;
    private String dob;
    private String ssn;
    private String orderId;
    private String product;
    private String planName;
    private String benefit;
    private String dateSaved;

    public CoverageRow(String name, String memberId, String dob, String ssn, String orderId, String product, String planName, String benefit, String dateSaved) {
        this.name = name;
        this.memberId = memberId;
        this.dob = dob;
        this.ssn = ssn;
        this.orderId = orderId;
        this.product = product;
        this.planName = planName;
        this.benefit = benefit;
        this.dateSaved = dateSaved;
        if (dateSaved.indexOf("-") > 0) {
            dateSaved = dateSaved.replace("-","/");
        }
    }

    public String getBenefit() {
        return benefit;
    }

    public String getPlanName() {
        return planName;
    }

    public String getName() {
        return name;
    }

    public String getSsn() {
        return ssn;
    }

    public String getDob() {
        return dob;
    }

    public String getDateSaved() {
        return dateSaved;
    }

    public boolean equals(CoverageRow cr) {
        boolean out = false;
        String planNameIn = cr.getPlanName();
        if (planNameIn.equals("") && planName.equals("")) {
            out = true;
        } else if (planNameIn.startsWith("Medical Option 1") && planName.startsWith(("Medical Option 1"))) {
            if (cr.getBenefit().equalsIgnoreCase(benefit)) {
                out = true;
            }
        } else if (planNameIn.startsWith("Medical Option 2") && planName.startsWith(("Medical Option 2"))) {
            if (cr.getBenefit().equalsIgnoreCase(benefit)) {
                out = true;
            }
        }

        return out;
    }

    public boolean isAfter(CoverageRow cr) throws ParseException {
        boolean out = false;
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
        Date thisOne = format.parse(dateSaved);
        Date dateIn = format.parse(cr.getDateSaved());
        Calendar thisCal = Calendar.getInstance();
        thisCal.setTime(thisOne);
        Calendar calIn = Calendar.getInstance();
        calIn.setTime(dateIn);
        if (thisCal.compareTo(calIn) > 0) {
            out = true;
        }
        return out;
    }

    public String get(String field) {
        String methodName = "get"+field.substring(0,1).toUpperCase() + field.substring(1);
        String val = "";
        try {
            Class<?> cls = Class.forName("com.solidify.utils.CoverageRow");
            Method method = cls.getDeclaredMethod(methodName,cls);
            val = (String)method.invoke(this);
        } catch (Exception e) {
            System.out.println("Couldn't find CoverageRow class");
        }

        return val;
    }
}
