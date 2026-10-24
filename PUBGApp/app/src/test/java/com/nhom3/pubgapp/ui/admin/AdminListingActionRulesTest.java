package com.nhom3.pubgapp.ui.admin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.nhom3.pubgapp.model.enums.ListingStatus;

import org.junit.Test;

public class AdminListingActionRulesTest {

    @Test
    public void canHide_onlyAvailable() {
        for (ListingStatus status : ListingStatus.values()) {
            assertEquals(status.name(), status == ListingStatus.AVAILABLE,
                    AdminListingActionRules.canHide(status));
        }
        assertFalse(AdminListingActionRules.canHide(null));
    }

    @Test
    public void canRemove_draftAvailableHiddenOnly() {
        for (ListingStatus status : ListingStatus.values()) {
            boolean expected = status == ListingStatus.DRAFT
                    || status == ListingStatus.AVAILABLE
                    || status == ListingStatus.HIDDEN;
            assertEquals(status.name(), expected, AdminListingActionRules.canRemove(status));
        }
        assertFalse(AdminListingActionRules.canRemove(null));
    }

    @Test
    public void canRestore_hiddenWithReason() {
        assertTrue(AdminListingActionRules.canRestore(ListingStatus.HIDDEN, "Hàng giả"));
    }

    @Test
    public void canRestore_hiddenByOwnerHasNoReason() {
        assertFalse(AdminListingActionRules.canRestore(ListingStatus.HIDDEN, null));
        assertFalse(AdminListingActionRules.canRestore(ListingStatus.HIDDEN, ""));
        assertFalse(AdminListingActionRules.canRestore(ListingStatus.HIDDEN, "   "));
    }

    @Test
    public void canRestore_otherStatusesNever() {
        assertFalse(AdminListingActionRules.canRestore(ListingStatus.AVAILABLE, "reason"));
        assertFalse(AdminListingActionRules.canRestore(ListingStatus.REMOVED, "reason"));
        assertFalse(AdminListingActionRules.canRestore(null, "reason"));
    }
}
