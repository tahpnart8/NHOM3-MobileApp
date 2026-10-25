package com.nhom3.pubgapp.ui.admin;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.nhom3.pubgapp.viewmodel.AdminUserViewModel;

import org.junit.Test;

public class AdminAccountRulesTest {

    @Test
    public void testIsSelfAdminAction_sameUser_returnsTrue() {
        assertTrue(AdminUserViewModel.isSelfAdminAction("admin123", "admin123"));
    }

    @Test
    public void testIsSelfAdminAction_differentUser_returnsFalse() {
        assertFalse(AdminUserViewModel.isSelfAdminAction("target456", "admin123"));
    }

    @Test
    public void testIsSelfAdminAction_nullCases_returnsFalse() {
        assertFalse(AdminUserViewModel.isSelfAdminAction(null, "admin123"));
        assertFalse(AdminUserViewModel.isSelfAdminAction("target456", null));
        assertFalse(AdminUserViewModel.isSelfAdminAction(null, null));
    }
}
