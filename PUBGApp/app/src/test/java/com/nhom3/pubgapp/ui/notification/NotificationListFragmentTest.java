package com.nhom3.pubgapp.ui.notification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.model.enums.NotificationType;

import org.junit.Test;

// Tests navigation action mapping for all 10 NotificationType values.
public class NotificationListFragmentTest {

    @Test
    public void getDestinationAction_acceptedOfferWithDealId_returnsDealDetailAction() {
        int action = NotificationListFragment.getDestinationAction(
                NotificationType.OFFER_RESULT, "m3gA1YBYs5GrG6FeBOIY");
        assertEquals(R.id.action_global_deal_detail, action);
    }

    @Test
    public void getDestinationAction_rejectedOfferWithConversationId_returnsChatAction() {
        int action = NotificationListFragment.getDestinationAction(
                NotificationType.OFFER_RESULT, "listing1_buyer1");
        assertEquals(R.id.action_global_chat, action);
    }

    @Test
    public void getDestinationAction_dealUpdate_returnsDealDetailAction() {
        int action = NotificationListFragment.getDestinationAction(NotificationType.DEAL_UPDATE);
        assertEquals(R.id.action_global_deal_detail, action);
    }

    @Test
    public void getDestinationAction_paymentUpdate_returnsDealDetailAction() {
        int action = NotificationListFragment.getDestinationAction(NotificationType.PAYMENT_UPDATE);
        assertEquals(R.id.action_global_deal_detail, action);
    }

    @Test
    public void getDestinationAction_newReview_returnsSellerTrustAction() {
        int action = NotificationListFragment.getDestinationAction(NotificationType.NEW_REVIEW);
        assertEquals(R.id.action_global_seller_trust, action);
    }

    @Test
    public void getDestinationAction_listingModerated_returnsMyShopAction() {
        int action = NotificationListFragment.getDestinationAction(NotificationType.LISTING_MODERATED);
        assertEquals(R.id.action_global_my_shop, action);
    }

    @Test
    public void getDestinationAction_warning_returnsMyProfileAction() {
        int action = NotificationListFragment.getDestinationAction(NotificationType.WARNING);
        assertEquals(R.id.action_global_my_profile, action);
    }

    @Test
    public void getDestinationAction_verificationResult_returnsMyProfileAction() {
        int action = NotificationListFragment.getDestinationAction(NotificationType.VERIFICATION_RESULT);
        assertEquals(R.id.action_global_my_profile, action);
    }

    @Test
    public void getDestinationAction_complaintResult_returnsMyProfileAction() {
        int action = NotificationListFragment.getDestinationAction(NotificationType.COMPLAINT_RESULT);
        assertEquals(R.id.action_global_my_profile, action);
    }

    @Test
    public void getDestinationAction_newMessage_returnsChatAction() {
        int action = NotificationListFragment.getDestinationAction(NotificationType.NEW_MESSAGE);
        assertEquals(R.id.action_global_chat, action);
    }

    @Test
    public void getDestinationAction_newOffer_returnsChatAction() {
        int action = NotificationListFragment.getDestinationAction(NotificationType.NEW_OFFER);
        assertEquals(R.id.action_global_chat, action);
    }

    @Test
    public void getDestinationAction_offerResult_returnsChatAction() {
        int action = NotificationListFragment.getDestinationAction(NotificationType.OFFER_RESULT);
        assertEquals(R.id.action_global_chat, action);
    }

    @Test
    public void getDestinationAction_nullType_returnsUnknown() {
        int action = NotificationListFragment.getDestinationAction(null);
        assertEquals(NotificationListFragment.ACTION_UNKNOWN, action);
    }

    @Test
    public void getDestinationAction_allTenNotificationTypesCovered() {
        NotificationType[] types = NotificationType.values();
        assertEquals(10, types.length);
        for (int i = 0; i < types.length; i++) {
            NotificationType type = types[i];
            int action = NotificationListFragment.getDestinationAction(type);
            assertNotEquals(NotificationListFragment.ACTION_UNKNOWN, action);
        }
    }
}
