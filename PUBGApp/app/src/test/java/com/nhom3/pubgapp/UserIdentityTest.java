package com.nhom3.pubgapp;

import com.google.firebase.Timestamp;
import com.nhom3.pubgapp.model.UserIdentity;
import com.nhom3.pubgapp.model.enums.Gender;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UserIdentityTest {

    @Test
    public void testIsCompleteSuccess() {
        UserIdentity identity = new UserIdentity();
        identity.setNationalId("079123456789");
        identity.setDateOfBirth(new Timestamp(new Date(946684800000L)));
        identity.setGender(Gender.FEMALE);
        assertTrue(identity.isComplete());
    }

    @Test
    public void testIsCompleteFailsShortNationalId() {
        UserIdentity identity = new UserIdentity();
        identity.setNationalId("123456");
        identity.setDateOfBirth(new Timestamp(new Date()));
        identity.setGender(Gender.FEMALE);
        assertFalse(identity.isComplete());
    }

    @Test
    public void testIsCompleteFailsNonDigits() {
        UserIdentity identity = new UserIdentity();
        identity.setNationalId("07912345678A");
        identity.setDateOfBirth(new Timestamp(new Date()));
        identity.setGender(Gender.MALE);
        assertFalse(identity.isComplete());
    }

    @Test
    public void testIsCompleteFailsMissingDateOfBirth() {
        UserIdentity identity = new UserIdentity();
        identity.setNationalId("079123456789");
        identity.setGender(Gender.OTHER);
        assertFalse(identity.isComplete());
    }

    @Test
    public void testIsCompleteFailsMissingGender() {
        UserIdentity identity = new UserIdentity();
        identity.setNationalId("079123456789");
        identity.setDateOfBirth(new Timestamp(new Date()));
        assertFalse(identity.isComplete());
    }
}
