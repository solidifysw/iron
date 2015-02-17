package com.solidify.dao;

import com.solidify.exceptions.MissingProperty;

import java.sql.SQLException;
import java.util.Date;

/**
 * Created by jennifermac on 2/15/15.
 */
public class Dependent extends Person {
    private Person ee;
    private String relationship;

    public Dependent(Person ee, String firstName,String lastName,String ssn, String dateOfBirth, String gender, String relationship, Date start, Date end) {
        super(firstName,lastName,false,ssn,dateOfBirth, gender, start);
        this.ee = ee;
        this.relationship = relationship;
    }

    public Dependent(Person ee, String firstName,String lastName,String ssn, String dateOfBirth, String gender, String relationship, Date start) {
        this(ee,firstName,lastName,ssn,dateOfBirth,gender,relationship,start,null);
    }

    public String getRelationship() {
        return relationship;
    }

    public void save() throws SQLException, MissingProperty {
        super.save();
        DependentsToEmployees dte = new DependentsToEmployees(ee,this);
        dte.save();
    }
}
