package com.nhom3.pubgapp.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.nhom3.pubgapp.model.Conversation;
import com.nhom3.pubgapp.model.Listing;
import com.nhom3.pubgapp.model.Offer;
import com.nhom3.pubgapp.model.enums.ListingStatus;
import com.nhom3.pubgapp.model.enums.OfferType;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

// Unit tests for pure validation rules of ChatRepository.
public class ChatRepositoryTest {

    @Test
    public void validateMessageText_rejectsEmptyOrBlank() {
        assertNotNull(ChatRepository.validateMessageText(null));
        assertNotNull(ChatRepository.validateMessageText(""));
        assertNotNull(ChatRepository.validateMessageText("   "));
        assertNotNull(ChatRepository.validateMessageText("\n\t"));
    }

    @Test
    public void validateMessageText_rejectsTooLong() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 2001; i++) {
            sb.append('a');
        }
        assertNotNull(ChatRepository.validateMessageText(sb.toString()));
    }

    @Test
    public void validateMessageText_acceptsValid() {
        assertNull(ChatRepository.validateMessageText("Xin chào!"));
        assertNull(ChatRepository.validateMessageText("a"));

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 2000; i++) {
            sb.append('x');
        }
        assertNull(ChatRepository.validateMessageText(sb.toString()));
    }

    @Test
    public void isValidMessageText_matchesValidation() {
        assertFalse(ChatRepository.isValidMessageText(null));
        assertFalse(ChatRepository.isValidMessageText(""));
        assertFalse(ChatRepository.isValidMessageText("   "));
        assertTrue(ChatRepository.isValidMessageText("Chào bạn"));
    }

    @Test
    public void isSenderAllowed_allowsOnlyParticipants() {
        Conversation conv = new Conversation();
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
        Listing listing = new Listing();
        listing.setSellerId("user1");
        listing.setStatus(ListingStatus.AVAILABLE);

        Offer offer = new Offer();
        offer.setOfferType(OfferType.MONEY);
        offer.setAmount(100000);

        String error = ChatRepository.validateOffer(offer, listing, "user1");
        assertNotNull(error);
        assertEquals("Không thể gửi đề nghị cho tin đăng của chính mình.", error);
    }

    @Test
    public void validateOffer_rejectsNonAvailableListing() {
        Listing listing = new Listing();
        listing.setSellerId("seller1");
        listing.setStatus(ListingStatus.RESERVED);

        Offer offer = new Offer();
        offer.setOfferType(OfferType.MONEY);
        offer.setAmount(100000);

        String error = ChatRepository.validateOffer(offer, listing, "buyer1");
        assertNotNull(error);
        assertEquals("Tin đăng không còn ở trạng thái khả dụng để gửi đề nghị.", error);
    }

    @Test
    public void validateOffer_rejectsUnacceptedOfferType() {
        Listing listing = new Listing();
        listing.setSellerId("seller1");
        listing.setStatus(ListingStatus.AVAILABLE);
        listing.setAcceptedOfferTypes(Collections.singletonList(OfferType.MONEY));

        Offer offer = new Offer();
        offer.setOfferType(OfferType.ITEM);
        offer.setExchangeItem("Sách");

        String error = ChatRepository.validateOffer(offer, listing, "buyer1");
        assertNotNull(error);
        assertEquals("Người bán không chấp nhận hình thức đề nghị này.", error);
    }

    @Test
    public void validateOffer_rejectsAmountBelowMinOfferPrice() {
        Listing listing = new Listing();
        listing.setSellerId("seller1");
        listing.setStatus(ListingStatus.AVAILABLE);
        listing.setAcceptedOfferTypes(Collections.singletonList(OfferType.MONEY));
        listing.setMinOfferPrice(200000);

        Offer offer = new Offer();
        offer.setOfferType(OfferType.MONEY);
        offer.setAmount(150000);

        String error = ChatRepository.validateOffer(offer, listing, "buyer1");
        assertNotNull(error);
        assertEquals("Giá đề nghị chưa phù hợp, hãy thử mức khác.", error);
    }

    @Test
    public void validateOffer_acceptsValidOffer() {
        Listing listing = new Listing();
        listing.setSellerId("seller1");
        listing.setStatus(ListingStatus.AVAILABLE);
        listing.setAcceptedOfferTypes(Arrays.asList(OfferType.MONEY, OfferType.ITEM));
        listing.setMinOfferPrice(200000);

        Offer offerMoney = new Offer();
        offerMoney.setOfferType(OfferType.MONEY);
        offerMoney.setAmount(250000);
        assertNull(ChatRepository.validateOffer(offerMoney, listing, "buyer1"));

        Offer offerItem = new Offer();
        offerItem.setOfferType(OfferType.ITEM);
        offerItem.setAmount(0);
        offerItem.setExchangeItem("Tai nghe Bluetooth");
        assertNull(ChatRepository.validateOffer(offerItem, listing, "buyer1"));
    }

    @Test
    public void formatOfferSummary_formatsProperly() {
        Offer offerMoney = new Offer();
        offerMoney.setOfferType(OfferType.MONEY);
        offerMoney.setAmount(500000);
        assertTrue(ChatRepository.formatOfferSummary(offerMoney).contains("500.000"));

        Offer offerItem = new Offer();
        offerItem.setOfferType(OfferType.ITEM);
        offerItem.setExchangeItem("Máy tính bảng");
        assertEquals("Đề nghị đổi: Máy tính bảng", ChatRepository.formatOfferSummary(offerItem));
    }

    @Test
    public void filterOtherPendingOffers_excludesCurrentOffer() {
        Offer o1 = new Offer();
        o1.setId("offer1");
        Offer o2 = new Offer();
        o2.setId("offer2");

        List<Offer> filtered = ChatRepository.filterOtherPendingOffers(Arrays.asList(o1, o2), "offer1");
        assertEquals(1, filtered.size());
        assertEquals("offer2", filtered.get(0).getId());
    }
}
