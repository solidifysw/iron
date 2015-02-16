package com.solidify.dao;

import com.solidify.exceptions.MissingProperty;

import java.sql.SQLException;

/**
 * Created by jennifermac on 2/15/15.
 */
public class Dependent extends Person {
    private Person ee;
    private String relationship;

    public Dependent(Person ee, String firstName,String lastName,String ssn,String relationship) {
        super(firstName,lastName,false,ssn);
        this.ee = ee;
        this.relationship = relationship;
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
