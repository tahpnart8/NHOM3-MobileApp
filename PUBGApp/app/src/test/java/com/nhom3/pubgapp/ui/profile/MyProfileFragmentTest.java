package com.nhom3.pubgapp.ui.profile;

import static org.junit.Assert.assertEquals;

import com.nhom3.pubgapp.R;

import org.junit.Test;

public class MyProfileFragmentTest {

    @Test
    public void emptyNationalIdIsMissing() {
        assertEquals(R.string.profile_my_profile_verify_missing_national_id, MyProfileFragment.nationalIdProblem(""));
        assertEquals(R.string.profile_my_profile_verify_missing_national_id, MyProfileFragment.nationalIdProblem("  "));
        assertEquals(R.string.profile_my_profile_verify_missing_national_id, MyProfileFragment.nationalIdProblem(null));
    }

    @Test
    public void wrongLengthIsBad() {
        assertEquals(R.string.profile_my_profile_verify_bad_national_id,
                MyProfileFragment.nationalIdProblem("07920100123"));
        assertEquals(R.string.profile_my_profile_verify_bad_national_id,
                MyProfileFragment.nationalIdProblem("0792010012345"));
    }

    @Test
    public void letterIsBad() {
        assertEquals(R.string.profile_my_profile_verify_bad_national_id,
                MyProfileFragment.nationalIdProblem("07920100123A"));
    }

    @Test
    public void twelveDigitsPass() {
        assertEquals(0, MyProfileFragment.nationalIdProblem("079201001234"));
    }
}
