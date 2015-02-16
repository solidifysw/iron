package com.solidify.dao;

import com.solidify.exceptions.MissingProperty;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Date;

/**
 * Created by jennifermac on 2/15/15.
 */
public class Employee extends Person {
    private EmploymentInfo employmentInfo;

    public Employee(String firstName, String lastName, String ssn, String dateOfHire, String employerClass, String occupation, String employeeId,
                    String locationCode, String locationDescription, String status, String department, int hoursPerWeek, int deductionsPerYear,
                    String annualSalary, Date start, Date end) {
        super(firstName,lastName,true,ssn);

        float salary = 0f;
        DecimalFormat df = new DecimalFormat("#,###,###.00");
        try {
            salary = df.parse(annualSalary).floatValue();
        } catch (Exception e) {}

        this.employmentInfo = new EmploymentInfo(this,dateOfHire,employerClass,occupation,employeeId,locationCode,locationDescription,
                "ACTIVE".equals(status)?1:0,department,hoursPerWeek,deductionsPerYear,salary,start,end);
    }

    public void save() throws SQLException, MissingProperty {
        super.save();
        employmentInfo.save();
    }
}
