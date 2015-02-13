package com.solidify.dao;

import com.solidify.exceptions.MissingProperty;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.LinkedHashSet;

/**
 * Created by jrobins on 2/11/15.
 */
public class EmploymentInfo {
    private int employmentInfoId;
    private int personId;
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
    private Connection con;
    private LinkedHashSet<String> fields;

    public EmploymentInfo(int personId, int employmentInfoId) {
        fields = new LinkedHashSet();
        this.personId = personId;
        this.employmentInfoId = employmentInfoId;
        if (personId > -1) {
            fields.add("personId");
        }
        if (employmentInfoId > -1) {
            fields.add("employmentInfoId");
        }
    }

    public EmploymentInfo(int personId) {
        this(personId,-1);
    }

    public void save() throws SQLException, MissingProperty {
        insert();
    }

    private void insert() throws SQLException {
        if (fields.size() == 0) {
            return;
        }

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
        PreparedStatement insert = con.prepareStatement(sql);
        int idx = 1;
        for (String field : fields) {
            if (field.equals("personId")) {
                insert.setInt(idx,personId);
            } else if (field.equals("active")) {
                insert.setInt(idx,active);
            } else if (field.equals("hoursPerWeek")) {
                insert.setInt(idx,hoursPerWeek);
            } else if (field.equals("deductionsPerYear")) {
                insert.setInt(idx,deductionsPerYear);
            } else if (field.equals("dateOfHire")) {
                insert.setString(idx,dateOfHire);
            } else if (field.equals("employerClass")) {
                insert.setString(idx,employerClass);
            } else if (field.equals("occupation")) {
                insert.setString(idx,occupation);
            } else if (field.equals("employeeId")) {
                insert.setString(idx,employeeId);
            } else if (field.equals("locationCode")) {
                insert.setString(idx,locationCode);
            } else if (field.equals("locationDescription")) {
                insert.setString(idx,locationDescription);
            } else if (field.equals("department")) {
                insert.setString(idx,department);
            } else if (field.equals("annualSalary")) {
                insert.setFloat(idx,annualSalary);
            }
            idx++;
        }
        insert.executeUpdate();
        ResultSet rs = insert.getGeneratedKeys();
        if (rs.next()) {
            employmentInfoId = rs.getInt(1);
        }
    }

    public void setDateOfHire(String dateOfHire) {
        if (fields.contains("dateOfHire")) {
            fields.remove("dateOfHire");
        }

        if (dateOfHire != null && !dateOfHire.equals("")) {
            this.dateOfHire = dateOfHire;
            fields.add("dateOfHire");
        }
    }

    public void setEmployerClass(String employerClass) {
        if (fields.contains("employerClass")) {
            fields.remove("employerClass");
        }
        if (employerClass != null && !employerClass.equals("")) {
            this.employerClass = employerClass;
            fields.add("employerClass");
        }
    }

    public void setOccupation(String occupation) {
        if (fields.contains("occupation")) {
            fields.remove("occupation");
        }
        if (occupation != null && !occupation.equals("")) {
            this.occupation = occupation;
            fields.add("occupation");
        }
    }

    public void setEmployeeId(String employeeId) {
        if (fields.contains("employeeId")) {
            fields.remove("employeeId");
        }
        if (employeeId != null && !employeeId.equals("")) {
            this.employeeId = employeeId;
            fields.add("employeeId");
        }
    }

    public void setLocationCode(String locationCode) {
        if (fields.contains("locationCode")) {
            fields.remove("locationCode");
        }
        if (locationCode != null && !locationCode.equals("")) {
            this.locationCode = locationCode;
            fields.add("locationCode");
        }
    }

    public void setLocationDescription(String locationDescription) {
        if (fields.contains("LocationDescription")) {
            fields.remove("locationDescription");
        }
        if (locationDescription != null && !locationDescription.equals("")) {
            this.locationDescription = locationDescription;
            fields.add("locationDescription");
        }
    }

    public void setStatus(String status) {
        if (fields.contains("active")) {
            fields.remove("active");
        }
        this.active = status != null && status.equals("ACTIVE") ? 1 : 0;
        fields.add("active");
    }

    public void setDepartment(String department) {
        if (fields.contains("department")) {
            fields.remove("department");
        }
        if (department != null && !department.equals("")) {
            this.department = department;
            fields.add("department");
        }
    }

    public void setHoursPerWeek(int hoursPerWeek) {
        if (fields.contains("hoursPerWeek")) {
            fields.remove("hoursPerWeek");
        }
        this.hoursPerWeek = hoursPerWeek;
        if (this.hoursPerWeek > -1) {
            fields.add("hoursPerWeek");
        }
    }

    public void setDeductionsPerYear(int deductionsPerYear) {
        if (fields.contains("deductionsPerYear")) {
            fields.remove("deductionsPerYear");
        }
        this.deductionsPerYear = deductionsPerYear;
        if (this.deductionsPerYear > 0) {
            fields.add("deductionsPerYear");
        }
    }

    public void setAnnualSalary(String salary) {
        if (fields.contains("annualSalary")) {
            fields.remove("annualSalary");
        }
        if (salary == null) {
            return;
        }
        DecimalFormat df = new DecimalFormat("#,###,###.00");
        try {
            this.annualSalary = df.parse(salary).floatValue();
        } catch (Exception e) {
            this.annualSalary = 0f;
        }
        fields.add("annualSalary");
    }

    public void setStart(Date start) {
        if (fields.contains("start")) {
            fields.remove("start");
        }
        this.start = start;
        fields.add("start");
    }

    public void setEnd(Date end) {
        if (fields.contains("end")) {
            fields.remove("end");
        }
        this.end = end;
        fields.add("end");
    }

    public void setDatabaseConnection(Connection con) {
        this.con = con;
    }

    public int getEmploymentInfoId() {
        return employmentInfoId;
    }

    public int getPersonId() {
        return personId;
    }

    public String getDateOfHire() {
        return dateOfHire;
    }

    public String getEmployerClass() {
        return employerClass;
    }

    public String getOccupation() {
        return occupation;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public String getLocationCode() {
        return locationCode;
    }

    public String getLocationDescription() {
        return locationDescription;
    }

    public int getActive() {
        return active;
    }

    public String getDepartment() {
        return department;
    }

    public int getHoursPerWeek() {
        return hoursPerWeek;
    }

    public int getDeductionsPerYear() {
        return deductionsPerYear;
    }

    public float getAnnualSalary() {
        return annualSalary;
    }

    public Date getStart() {
        return start;
    }

    public Date getEnd() {
        return end;
    }
}
