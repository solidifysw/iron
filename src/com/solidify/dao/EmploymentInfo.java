package com.solidify.dao;

import com.solidify.admin.reports.Utils;
import com.solidify.exceptions.MissingProperty;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;

/**
 * Created by jrobins on 2/11/15.
 */
public class EmploymentInfo {
    private int employmentInfoId;
    private Person person;
    private String dateOfHire;
    private String employerClass;
    private String occupation;
    private String employeeId;
    private String locationCode;
    private String locationDescription;
    private int active;
    private String department;
    private int hoursPerWeek;
    private int deductionsPerYear;
    private float annualSalary;
    private Date start;
    private Date end;

    public EmploymentInfo(int employmentInfoId, Person person, String dateOfHire, String employerClass, String occupation, String employeeId, String locationCode, String locationDescription,
                          int active, String department, int hoursPerWeek, int deductionsPerYear, float annualSalary, Date start, Date end) {
        this.employmentInfoId = employmentInfoId;
        this.person = person;
        this.dateOfHire = dateOfHire;
        this.employerClass = employerClass;
        this.occupation = occupation;
        this.employeeId = employeeId;
        this.locationCode = locationCode;
        this.locationDescription = locationDescription;
        this.active = active;
        this.department = department;
        this.hoursPerWeek = hoursPerWeek;
        this.deductionsPerYear = deductionsPerYear;
        this.annualSalary = annualSalary;
        this.start = start;
        this.end = end;
    }

    public EmploymentInfo(Person person, String dateOfHire, String employerClass, String occupation, String employeeId, String locationCode, String locationDescription,
                          int active, String department, int hoursPerWeek, int deductionsPerYear, float annualSalary, Date start, Date end) {
        this(-1, person,dateOfHire,employerClass,occupation,employeeId,locationCode,locationDescription,active,department,hoursPerWeek,deductionsPerYear,
                annualSalary,start,end);
    }

    public EmploymentInfo(Person person) {
        this(-1,person,null,null,null,null,null,null,1,null,0,0,0f,null,null);
    }

    public void save() throws SQLException, MissingProperty {
        if (!person.isLoaded()) {
            throw new MissingProperty("person is not loaded");
        }
        insert();
    }

    private void insert() throws SQLException {
        HashSet<String> fields = new HashSet<String>();
        fields.add("personId");
        if (dateOfHire != null) fields.add("dateOfHire");
        if (employerClass != null) fields.add("employerClass");
        if (occupation != null) fields.add("occupation");
        if (employeeId != null) fields.add("employeeId");
        if (locationCode != null) fields.add("locationCode");
        if (locationDescription != null) fields.add("locationDescription");
        fields.add("active");
        if (department != null) fields.add("department");
        if (hoursPerWeek > 0) fields.add("hoursPerWeek");
        if (deductionsPerYear > 0) fields.add("deductionsPerYear");
        if (annualSalary > 0f) fields.add("annualSalary");
        if (start != null) fields.add("start");
        if (end != null) fields.add("end");

        String sql = "INSERT INTO FE.EmploymentInfo (";

        int cnt = 0;
        for (String field : fields) {
            sql += field;
            if (cnt < fields.size()-1) {
                sql += ",";
            }
            cnt++;
        }
        sql += ") VALUES (";
        for (int i=0; i<fields.size(); i++) {
            sql += "?";
            if (i < fields.size()-1) {
                sql += ",";
            }
        }
        sql += ")";
        System.out.println(sql);
        Connection con = null;
        try {
            con = Utils.getConnection();
            PreparedStatement insert = con.prepareStatement(sql);
            int idx = 1;
            for (String field : fields) {
                if (field.equals("personId")) {
                    insert.setInt(idx, person.getPersonId());
                } else if (field.equals("active")) {
                    insert.setInt(idx, active);
                } else if (field.equals("hoursPerWeek")) {
                    insert.setInt(idx, hoursPerWeek);
                } else if (field.equals("deductionsPerYear")) {
                    insert.setInt(idx, deductionsPerYear);
                } else if (field.equals("dateOfHire")) {
                    insert.setString(idx, dateOfHire);
                } else if (field.equals("employerClass")) {
                    insert.setString(idx, employerClass);
                } else if (field.equals("occupation")) {
                    insert.setString(idx, occupation);
                } else if (field.equals("employeeId")) {
                    insert.setString(idx, employeeId);
                } else if (field.equals("locationCode")) {
                    insert.setString(idx, locationCode);
                } else if (field.equals("locationDescription")) {
                    insert.setString(idx, locationDescription);
                } else if (field.equals("department")) {
                    insert.setString(idx, department);
                } else if (field.equals("annualSalary")) {
                    insert.setFloat(idx, annualSalary);
                }
                idx++;
            }
            insert.executeUpdate();
            ResultSet rs = insert.getGeneratedKeys();
            if (rs.next()) {
                employmentInfoId = rs.getInt(1);
            }
            insert.close();
            rs.close();
        } finally {
            if (con != null) con.close();
        }
    }

    public void setDateOfHire(String dateOfHire) {
        this.dateOfHire = !"".equals(dateOfHire) ? dateOfHire : null;

    }

    public void setEmployerClass(String employerClass) {
        this.employerClass = !"".equals(employerClass) ? employerClass : null;

    }

    public void setOccupation(String occupation) {
        this.occupation = !"".equals(occupation) ? occupation : null;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = !"".equals(employeeId) ? employeeId : null;
    }

    public void setLocationCode(String locationCode) {
        this.locationCode = !"".equals(locationCode) ? locationCode : null;
    }

    public void setLocationDescription(String locationDescription) {
        this.locationDescription = !"".equals(locationDescription) ? locationDescription : null;
    }

    public void setStatus(String status) {
        this.active = status != null && status.equals("ACTIVE") ? 1 : 0;
    }

    public void setDepartment(String department) {
        this.department = !"".equals(department) ? department : null;
    }

    public void setHoursPerWeek(int hoursPerWeek) {
        this.hoursPerWeek = hoursPerWeek;
    }

    public void setDeductionsPerYear(int deductionsPerYear) {
        this.deductionsPerYear = deductionsPerYear;
    }

    public void setAnnualSalary(String salary) {
        if (salary == null || "".equals(salary)) {
            this.annualSalary = 0f;
        } else {
            DecimalFormat df = new DecimalFormat("#,###,###.00");
            try {
                this.annualSalary = df.parse(salary).floatValue();
            } catch (Exception e) {
                this.annualSalary = 0f;
            }
        }
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public void setEnd(Date end) {
        this.end = end;
    }
}
