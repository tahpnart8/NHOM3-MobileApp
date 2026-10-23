package com.nhom3.pubgapp.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
import com.nhom3.pubgapp.model.enums.Gender;

public class UserIdentity extends BaseDocument {
    private String userId;
    private String nationalId;
    private Timestamp dateOfBirth;
    private Gender gender;
    private String permanentAddress;

    public UserIdentity() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNationalId() {
        return nationalId;
    }

    public void setNationalId(String nationalId) {
        this.nationalId = nationalId;
    }

    public Timestamp getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Timestamp dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getPermanentAddress() {
        return permanentAddress;
    }

    public void setPermanentAddress(String permanentAddress) {
        this.permanentAddress = permanentAddress;
    }

    @Exclude
    public boolean isComplete() {
        if (nationalId == null || nationalId.trim().length() != 12) {
            return false;
        }
        for (int i = 0; i < nationalId.length(); i++) {
            if (!Character.isDigit(nationalId.charAt(i))) {
                return false;
            }
        }
        if (dateOfBirth == null) {
            return false;
        }
        return gender != null;
    }
}

