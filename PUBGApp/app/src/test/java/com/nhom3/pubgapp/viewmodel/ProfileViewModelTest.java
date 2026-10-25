package com.nhom3.pubgapp.viewmodel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.nhom3.pubgapp.model.enums.VerificationStatus;

import org.junit.Test;

public class ProfileViewModelTest {

    private static final String VALID_ID = "079201001234";

    @Test
    public void emptyNationalIdIsMissing() {
        int result = ProfileViewModel.checkVerificationForm("", true, true);
        assertEquals(ProfileViewModel.FORM_MISSING_NATIONAL_ID, result);
    }

    @Test
    public void blankNationalIdIsMissing() {
        int result = ProfileViewModel.checkVerificationForm("   ", true, true);
        assertEquals(ProfileViewModel.FORM_MISSING_NATIONAL_ID, result);
    }

    @Test
    public void elevenDigitsAreRejected() {
        int result = ProfileViewModel.checkVerificationForm("07920100123", true, true);
        assertEquals(ProfileViewModel.FORM_BAD_NATIONAL_ID, result);
    }

    @Test
    public void thirteenDigitsAreRejected() {
        int result = ProfileViewModel.checkVerificationForm("0792010012345", true, true);
        assertEquals(ProfileViewModel.FORM_BAD_NATIONAL_ID, result);
    }

    @Test
    public void lettersAreRejected() {
        int result = ProfileViewModel.checkVerificationForm("07920100123A", true, true);
        assertEquals(ProfileViewModel.FORM_BAD_NATIONAL_ID, result);
    }

    @Test
    public void missingIdImageIsReported() {
        int result = ProfileViewModel.checkVerificationForm(VALID_ID, false, true);
        assertEquals(ProfileViewModel.FORM_MISSING_ID_IMAGE, result);
    }

    @Test
    public void missingPortraitImageIsReported() {
        int result = ProfileViewModel.checkVerificationForm(VALID_ID, true, false);
        assertEquals(ProfileViewModel.FORM_MISSING_PORTRAIT_IMAGE, result);
    }

    @Test
    public void twelveDigitsAndTwoImagesPass() {
        int result = ProfileViewModel.checkVerificationForm(VALID_ID, true, true);
        assertEquals(ProfileViewModel.FORM_OK, result);
    }

    @Test
    public void noneWithVerifiedEmailMaySubmit() {
        assertTrue(ProfileViewModel.canSubmitVerification(VerificationStatus.NONE, true));
    }

    @Test
    public void rejectedWithVerifiedEmailMaySubmit() {
        assertTrue(ProfileViewModel.canSubmitVerification(VerificationStatus.REJECTED, true));
    }

    @Test
    public void pendingMayNotSubmit() {
        assertFalse(ProfileViewModel.canSubmitVerification(VerificationStatus.PENDING, true));
    }

    @Test
    public void approvedMayNotSubmit() {
        assertFalse(ProfileViewModel.canSubmitVerification(VerificationStatus.APPROVED, true));
    }

    @Test
    public void unverifiedEmailMayNotSubmit() {
        assertFalse(ProfileViewModel.canSubmitVerification(VerificationStatus.NONE, false));
    }

    @Test
    public void ownPageWithoutStatsReadsAgain() {
        assertTrue(ProfileViewModel.needsStatsRead(true, false));
    }

    @Test
    public void ownPageWithStatsIsNotReadAgain() {
        assertFalse(ProfileViewModel.needsStatsRead(true, true));
    }

    @Test
    public void otherPageNeverReadsStats() {
        assertFalse(ProfileViewModel.needsStatsRead(false, false));
    }

    @Test
    public void emptyReceiverNameIsRejected() {
        int result = ProfileViewModel.checkAddressForm(
                "", "0912345678", "Hà Nội", "Cầu Giấy", "123 Đường Láng");
        assertEquals(ProfileViewModel.ADDRESS_FORM_MISSING_RECEIVER, result);
    }

    @Test
    public void emptyPhoneIsRejected() {
        int result = ProfileViewModel.checkAddressForm(
                "Nguyễn Văn A", "", "Hà Nội", "Cầu Giấy", "123 Đường Láng");
        assertEquals(ProfileViewModel.ADDRESS_FORM_MISSING_PHONE, result);
    }

    @Test
    public void phoneWithLettersIsRejected() {
        int result = ProfileViewModel.checkAddressForm(
                "Nguyễn Văn A", "091234567A", "Hà Nội", "Cầu Giấy", "123 Đường Láng");
        assertEquals(ProfileViewModel.ADDRESS_FORM_BAD_PHONE, result);
    }

    @Test
    public void phoneWithNineDigitsIsRejected() {
        int result = ProfileViewModel.checkAddressForm(
                "Nguyễn Văn A", "091234567", "Hà Nội", "Cầu Giấy", "123 Đường Láng");
        assertEquals(ProfileViewModel.ADDRESS_FORM_BAD_PHONE, result);
    }

    @Test
    public void phoneWithElevenDigitsIsRejected() {
        int result = ProfileViewModel.checkAddressForm(
                "Nguyễn Văn A", "09123456789", "Hà Nội", "Cầu Giấy", "123 Đường Láng");
        assertEquals(ProfileViewModel.ADDRESS_FORM_BAD_PHONE, result);
    }

    @Test
    public void phoneNotStartingWithZeroIsRejected() {
        int result = ProfileViewModel.checkAddressForm(
                "Nguyễn Văn A", "1912345678", "Hà Nội", "Cầu Giấy", "123 Đường Láng");
        assertEquals(ProfileViewModel.ADDRESS_FORM_BAD_PHONE, result);
    }

    @Test
    public void emptyProvinceIsRejected() {
        int result = ProfileViewModel.checkAddressForm(
                "Nguyễn Văn A", "0912345678", "", "Cầu Giấy", "123 Đường Láng");
        assertEquals(ProfileViewModel.ADDRESS_FORM_MISSING_PROVINCE, result);
    }

    @Test
    public void emptyDistrictIsRejected() {
        int result = ProfileViewModel.checkAddressForm(
                "Nguyễn Văn A", "0912345678", "Hà Nội", "", "123 Đường Láng");
        assertEquals(ProfileViewModel.ADDRESS_FORM_MISSING_DISTRICT, result);
    }

    @Test
    public void emptyDetailIsRejected() {
        int result = ProfileViewModel.checkAddressForm(
                "Nguyễn Văn A", "0912345678", "Hà Nội", "Cầu Giấy", "");
        assertEquals(ProfileViewModel.ADDRESS_FORM_MISSING_DETAIL, result);
    }

    @Test
    public void validAddressPasses() {
        int result = ProfileViewModel.checkAddressForm(
                "Nguyễn Văn A", "0912345678", "Hà Nội", "Cầu Giấy", "123 Đường Láng");
        assertEquals(ProfileViewModel.ADDRESS_FORM_OK, result);
    }
}
