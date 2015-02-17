package com.solidify.tests;

import com.solidify.dao.Address;
import org.junit.Test;

import static org.junit.Assert.*;
/**
 * Created by jrobins on 2/17/15.
 */
public class TestAddressIsValid {

    @Test
    public void testIsValid() {
        Address address = new Address("Main","123 Elm St.","","Fairfax","VA","22030",Address.GROUP,1);
        assertTrue(address.isValid());
        assertFalse(address.isLoaded()); // Must have addressId to be loaded

        address = new Address("Main","123 Elm St.","","Fairfax","VA","22030");  // must have association set to be valid
        assertFalse(address.isValid());

        address.setAssociation(-4,1); // association type must be Address.PERSON or Address.GROUP
        assertFalse(address.isValid());

        address.setAssociation(Address.PERSON,1);
        assertTrue(address.isValid());

        address.setAddressId(1);
        assertTrue(address.isLoaded());
    }
}
