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

    @Test
    public void hanoiCoordinatesAreFound() {
        com.google.firebase.firestore.GeoPoint point = ProfileViewModel.findCoordinatesByProvince("Hà Nội");
        org.junit.Assert.assertNotNull(point);
        assertEquals(21.0285, point.getLatitude(), 0.001);
        assertEquals(105.8542, point.getLongitude(), 0.001);
    }

    @Test
    public void hcmcCoordinatesAreFound() {
        com.google.firebase.firestore.GeoPoint point = ProfileViewModel.findCoordinatesByProvince("TP.HCM");
        org.junit.Assert.assertNotNull(point);
        assertEquals(10.7769, point.getLatitude(), 0.001);
        assertEquals(106.7009, point.getLongitude(), 0.001);
    }

    @Test
    public void danangCoordinatesAreFound() {
        com.google.firebase.firestore.GeoPoint point = ProfileViewModel.findCoordinatesByProvince("Đà Nẵng");
        org.junit.Assert.assertNotNull(point);
        assertEquals(16.0544, point.getLatitude(), 0.001);
        assertEquals(108.2022, point.getLongitude(), 0.001);
    }

    @Test
    public void caseInsensitiveAndWithoutAccentsMatches() {
        com.google.firebase.firestore.GeoPoint point = ProfileViewModel.findCoordinatesByProvince("can tho");
        org.junit.Assert.assertNotNull(point);
        assertEquals(10.0452, point.getLatitude(), 0.001);
        assertEquals(105.7469, point.getLongitude(), 0.001);
    }

    @Test
    public void unknownProvinceReturnsNull() {
        com.google.firebase.firestore.GeoPoint point = ProfileViewModel.findCoordinatesByProvince("TinhKhongTonTai");
        org.junit.Assert.assertNull(point);
    }

    @Test
    public void nullOrEmptyProvinceReturnsNull() {
        org.junit.Assert.assertNull(ProfileViewModel.findCoordinatesByProvince(null));
        org.junit.Assert.assertNull(ProfileViewModel.findCoordinatesByProvince("   "));
    }
}
