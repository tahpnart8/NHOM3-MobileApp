package com.nhom3.pubgapp.ui.admin;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class AdminAccountRulesTest {

    @Test
    public void testIsSelfAdminAction_sameUser_returnsTrue() {
        assertTrue(AdminAccountFragment.isSelfAdminAction("admin123", "admin123"));
    }

    @Test
    public void testIsSelfAdminAction_differentUser_returnsFalse() {
        assertFalse(AdminAccountFragment.isSelfAdminAction("target456", "admin123"));
    }

    @Test
    public void testIsSelfAdminAction_nullCases_returnsFalse() {
        assertFalse(AdminAccountFragment.isSelfAdminAction(null, "admin123"));
        assertFalse(AdminAccountFragment.isSelfAdminAction("target456", null));
        assertFalse(AdminAccountFragment.isSelfAdminAction(null, null));
    }
}
