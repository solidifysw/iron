package com.solidify.dao;

import com.solidify.exceptions.MissingProperty;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

/**
 * Created by jrobins on 2/15/15.
 */
public class Dependent extends Person {
    private Person ee;
    private String relationship;
    private DependentsToEmployees dte;

    public Dependent(Person ee, String firstName,String lastName,String ssn, String dateOfBirth, String gender, String relationship, Date start, Date end, Connection con) {
        super(firstName,lastName,false,ssn,dateOfBirth, gender, start, con);
        this.ee = ee;
        this.relationship = relationship;
        this.dte = new DependentsToEmployees(ee,this,con);
    }

    public Dependent(Person ee, String firstName,String lastName,String ssn, String dateOfBirth, String gender, String relationship, Date start, Connection con) {
        this(ee,firstName,lastName,ssn,dateOfBirth,gender,relationship,start,null,con);
    }

    public String getRelationship() {
        return relationship;
    }

    public void save() throws SQLException, MissingProperty {
        super.save();
        dte.save();
    }
}
