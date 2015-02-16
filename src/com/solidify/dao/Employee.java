package com.solidify.dao;

import com.solidify.exceptions.MissingProperty;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Date;

/**
 * Created by jrobins on 2/15/15.
 */
public class Employee extends Person {
    private EmploymentInfo employmentInfo;

    public Employee(String firstName, String lastName, String ssn, String dateOfHire, String employerClass, String occupation, String employeeId,
                    String locationCode, String locationDescription, String status, String department, int hoursPerWeek, int deductionsPerYear,
                    String annualSalary, Date start, Date end) {
        super(firstName,lastName,true,ssn,start, end);

        float salary = 0f;
        DecimalFormat df = new DecimalFormat("#,###,###.00");
        try {
            salary = df.parse(annualSalary).floatValue();
        } catch (Exception e) {}

        this.employmentInfo = new EmploymentInfo(this,dateOfHire,employerClass,occupation,employeeId,locationCode,locationDescription,
                "ACTIVE".equals(status)?1:0,department,hoursPerWeek,deductionsPerYear,salary);
    }

    public Employee(String firstName, String lastName, String ssn, String dateOfHire, String employerClass, String occupation, String employeeId,
                    String locationCode, String locationDescription, String status, String department, int hoursPerWeek, int deductionsPerYear,
                    String annualSalary, Date start) {
        this(firstName,lastName,ssn,dateOfHire,employerClass,occupation,employeeId,locationCode,locationDescription,status,department,hoursPerWeek,deductionsPerYear,annualSalary,start,null);
    }

    public void save() throws SQLException, MissingProperty {
        super.save();
        employmentInfo.save();
    }

    public EmploymentInfo getEmploymentInfo() {
        return this.employmentInfo;
    }

    public void setConnection(Connection con) {
        super.setConnection(con);
        employmentInfo.setConnection(con);
    }
}
