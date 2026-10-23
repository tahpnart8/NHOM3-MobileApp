package com.nhom3.pubgapp.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.nhom3.pubgapp.model.enums.ListingStatus;

import org.junit.Test;

import java.util.List;

// Tests pure validation rules for conversation creation in ChatRepository without Firebase.
public class ChatRepositoryTest {

    @Test
    public void buildConversationId_concatenatesListingAndBuyer() {
        String id = ChatRepository.buildConversationId("list123", "buyer456");
        assertEquals("list123_buyer456", id);
    }

    @Test
    public void validateNewConversation_rejectsSameUser() {
        String error = ChatRepository.validateNewConversation("user1", "user1", ListingStatus.AVAILABLE);
        assertNotNull(error);
        assertEquals("Không thể nhắn tin cho chính mình.", error);
    }

    @Test
    public void validateNewConversation_rejectsEmptyBuyer() {
        String error = ChatRepository.validateNewConversation("", "seller1", ListingStatus.AVAILABLE);
        assertNotNull(error);
        assertEquals("Vui lòng đăng nhập để nhắn tin.", error);
    }

    @Test
    public void validateNewConversation_rejectsNonAvailableStatus() {
        String errorSold = ChatRepository.validateNewConversation("buyer1", "seller1", ListingStatus.SOLD);
        assertNotNull(errorSold);
        assertEquals("Tin đăng không còn hoạt động.", errorSold);

        String errorDraft = ChatRepository.validateNewConversation("buyer1", "seller1", ListingStatus.DRAFT);
        assertNotNull(errorDraft);
        assertEquals("Tin đăng không còn hoạt động.", errorDraft);
    }

    @Test
    public void validateNewConversation_acceptsAvailableAndReserved() {
        String resultAvail = ChatRepository.validateNewConversation("buyer1", "seller1", ListingStatus.AVAILABLE);
        assertNull(resultAvail);

        String resultReserved = ChatRepository.validateNewConversation("buyer1", "seller1", ListingStatus.RESERVED);
        assertNull(resultReserved);
    }

    @Test
    public void isListingStatusAllowed_returnsTrueOnlyForAvailableAndReserved() {
        assertTrue(ChatRepository.isListingStatusAllowed(ListingStatus.AVAILABLE));
        assertTrue(ChatRepository.isListingStatusAllowed(ListingStatus.RESERVED));
        assertFalse(ChatRepository.isListingStatusAllowed(ListingStatus.DRAFT));
        assertFalse(ChatRepository.isListingStatusAllowed(ListingStatus.SOLD));
        assertFalse(ChatRepository.isListingStatusAllowed(ListingStatus.HIDDEN));
        assertFalse(ChatRepository.isListingStatusAllowed(ListingStatus.REMOVED));
    }

    @Test
    public void validateMessageText_rejectsEmptyOrWhitespace() {
        assertNotNull(ChatRepository.validateMessageText(null));
        assertNotNull(ChatRepository.validateMessageText(""));
        assertNotNull(ChatRepository.validateMessageText("   "));
        assertFalse(ChatRepository.isValidMessageText(""));
        assertFalse(ChatRepository.isValidMessageText("   "));
    }

    @Test
    public void validateMessageText_rejectsOver2000Characters() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 2001; i++) {
            sb.append("a");
        }
        String tooLong = sb.toString();
        assertNotNull(ChatRepository.validateMessageText(tooLong));
        assertFalse(ChatRepository.isValidMessageText(tooLong));
    }

    @Test
    public void validateMessageText_acceptsValidRange() {
        assertNull(ChatRepository.validateMessageText("A"));
        assertTrue(ChatRepository.isValidMessageText("A"));

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 2000; i++) {
            sb.append("x");
        }
        String maxLength = sb.toString();
        assertNull(ChatRepository.validateMessageText(maxLength));
        assertTrue(ChatRepository.isValidMessageText(maxLength));
    }

    @Test
    public void isSenderAllowed_verifiesParticipant() {
        com.nhom3.pubgapp.model.Conversation conv = new com.nhom3.pubgapp.model.Conversation();
        conv.setBuyerId("buyer1");
        conv.setSellerId("seller1");

        assertTrue(ChatRepository.isSenderAllowed(conv, "buyer1"));
        assertTrue(ChatRepository.isSenderAllowed(conv, "seller1"));
        assertFalse(ChatRepository.isSenderAllowed(conv, "stranger"));
        assertFalse(ChatRepository.isSenderAllowed(conv, null));
        assertFalse(ChatRepository.isSenderAllowed(null, "buyer1"));
    }

    @Test
    public void validateOffer_rejectsSelfOffer() {
        com.nhom3.pubgapp.model.Listing listing = new com.nhom3.pubgapp.model.Listing();
        listing.setSellerId("user1");
        listing.setStatus(ListingStatus.AVAILABLE);

        com.nhom3.pubgapp.model.Offer offer = new com.nhom3.pubgapp.model.Offer();
        offer.setOfferType(com.nhom3.pubgapp.model.enums.OfferType.MONEY);
        offer.setAmount(100000);

        String error = ChatRepository.validateOffer(offer, listing, "user1");
        assertNotNull(error);
        assertEquals("Không thể gửi đề nghị cho tin đăng của chính mình.", error);
    }

    @Test
    public void validateOffer_rejectsNonAvailableListing() {
        com.nhom3.pubgapp.model.Listing listing = new com.nhom3.pubgapp.model.Listing();
        listing.setSellerId("seller1");
        listing.setStatus(ListingStatus.RESERVED);

        com.nhom3.pubgapp.model.Offer offer = new com.nhom3.pubgapp.model.Offer();
        offer.setOfferType(com.nhom3.pubgapp.model.enums.OfferType.MONEY);
        offer.setAmount(100000);

        String error = ChatRepository.validateOffer(offer, listing, "buyer1");
        assertNotNull(error);
        assertEquals("Tin đăng không còn ở trạng thái khả dụng để gửi đề nghị.", error);
    }

    @Test
    public void validateOffer_rejectsUnacceptedOfferType() {
        com.nhom3.pubgapp.model.Listing listing = new com.nhom3.pubgapp.model.Listing();
        listing.setSellerId("seller1");
        listing.setStatus(ListingStatus.AVAILABLE);
        listing.setAcceptedOfferTypes(java.util.Collections.singletonList(
                com.nhom3.pubgapp.model.enums.OfferType.MONEY));

        com.nhom3.pubgapp.model.Offer offer = new com.nhom3.pubgapp.model.Offer();
        offer.setOfferType(com.nhom3.pubgapp.model.enums.OfferType.ITEM);
        offer.setExchangeItem("Sách");

        String error = ChatRepository.validateOffer(offer, listing, "buyer1");
        assertNotNull(error);
        assertEquals("Người bán không chấp nhận hình thức đề nghị này.", error);
    }

    @Test
    public void validateOffer_rejectsAmountBelowMinOfferPrice() {
        com.nhom3.pubgapp.model.Listing listing = new com.nhom3.pubgapp.model.Listing();
        listing.setSellerId("seller1");
        listing.setStatus(ListingStatus.AVAILABLE);
        listing.setAcceptedOfferTypes(java.util.Collections.singletonList(
                com.nhom3.pubgapp.model.enums.OfferType.MONEY));
        listing.setMinOfferPrice(200000);

        com.nhom3.pubgapp.model.Offer offer = new com.nhom3.pubgapp.model.Offer();
        offer.setOfferType(com.nhom3.pubgapp.model.enums.OfferType.MONEY);
        offer.setAmount(150000);

        String error = ChatRepository.validateOffer(offer, listing, "buyer1");
        assertNotNull(error);
        assertEquals("Giá đề nghị chưa phù hợp, hãy thử mức khác.", error);
    }

    @Test
    public void validateOffer_acceptsValidOffer() {
        com.nhom3.pubgapp.model.Listing listing = new com.nhom3.pubgapp.model.Listing();
        listing.setSellerId("seller1");
        listing.setStatus(ListingStatus.AVAILABLE);
        listing.setAcceptedOfferTypes(java.util.Arrays.asList(
                com.nhom3.pubgapp.model.enums.OfferType.MONEY,
                com.nhom3.pubgapp.model.enums.OfferType.ITEM));
        listing.setMinOfferPrice(200000);

        com.nhom3.pubgapp.model.Offer offerMoney = new com.nhom3.pubgapp.model.Offer();
        offerMoney.setOfferType(com.nhom3.pubgapp.model.enums.OfferType.MONEY);
        offerMoney.setAmount(250000);
        assertNull(ChatRepository.validateOffer(offerMoney, listing, "buyer1"));

        com.nhom3.pubgapp.model.Offer offerItem = new com.nhom3.pubgapp.model.Offer();
        offerItem.setOfferType(com.nhom3.pubgapp.model.enums.OfferType.ITEM);
        offerItem.setAmount(0);
        offerItem.setExchangeItem("Tai nghe Bluetooth");
        assertNull(ChatRepository.validateOffer(offerItem, listing, "buyer1"));
    }

    @Test
    public void formatOfferSummary_formatsProperly() {
        com.nhom3.pubgapp.model.Offer offerMoney = new com.nhom3.pubgapp.model.Offer();
        offerMoney.setOfferType(com.nhom3.pubgapp.model.enums.OfferType.MONEY);
        offerMoney.setAmount(500000);
        assertTrue(ChatRepository.formatOfferSummary(offerMoney).contains("500.000"));

        com.nhom3.pubgapp.model.Offer offerItem = new com.nhom3.pubgapp.model.Offer();
        offerItem.setOfferType(com.nhom3.pubgapp.model.enums.OfferType.ITEM);
        offerItem.setExchangeItem("Máy tính bảng");
        assertEquals("Đề nghị đổi: Máy tính bảng", ChatRepository.formatOfferSummary(offerItem));
    }

    @Test
    public void filterOtherPendingOffers_excludesCurrentOffer() {
        com.nhom3.pubgapp.model.Offer o1 = new com.nhom3.pubgapp.model.Offer();
        o1.setId("offer1");
        com.nhom3.pubgapp.model.Offer o2 = new com.nhom3.pubgapp.model.Offer();
        o2.setId("offer2");

        List<com.nhom3.pubgapp.model.Offer> filtered =
                ChatRepository.filterOtherPendingOffers(java.util.Arrays.asList(o1, o2), "offer1");
        assertEquals(1, filtered.size());
        assertEquals("offer2", filtered.get(0).getId());
    }
}
