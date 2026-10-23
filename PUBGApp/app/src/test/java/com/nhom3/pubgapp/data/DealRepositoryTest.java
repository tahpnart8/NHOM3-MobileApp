package com.nhom3.pubgapp.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.firebase.Timestamp;
import com.nhom3.pubgapp.model.Deal;
import com.nhom3.pubgapp.model.Listing;
import com.nhom3.pubgapp.model.Offer;
import com.nhom3.pubgapp.model.Payment;
import com.nhom3.pubgapp.model.enums.DealStatus;
import com.nhom3.pubgapp.model.enums.DeliveryMethod;
import com.nhom3.pubgapp.model.enums.ListingStatus;
import com.nhom3.pubgapp.model.enums.OfferType;
import com.nhom3.pubgapp.model.enums.PaymentStatus;
import com.nhom3.pubgapp.model.enums.TradeType;

import org.junit.Test;

import java.util.ArrayList;

// Tests DealRepository decision logic without Firebase.
public class DealRepositoryTest {

    @Test
    public void buildFromOffer_mapsMoneyOfferToSellTradeType() {
        Listing listing = new Listing();
        listing.setTitle("Bàn phím cơ");
        listing.setCoverThumb("thumb_key");

        Offer offer = new Offer();
        offer.setId("offer1");
        offer.setListingId("listing1");
        offer.setBuyerId("buyer1");
        offer.setSellerId("seller1");
        offer.setOfferType(OfferType.MONEY);
        offer.setAmount(500000);

        Deal deal = DealRepository.createDealFromOffer(offer, listing);
        assertNotNull(deal);
        assertEquals("listing1", deal.getListingId());
        assertEquals("offer1", deal.getOfferId());
        assertEquals("buyer1", deal.getBuyerId());
        assertEquals("seller1", deal.getSellerId());
        assertEquals(TradeType.SELL, deal.getTradeType());
        assertEquals(500000, deal.getAgreedPrice());
        assertEquals("Bàn phím cơ", deal.getItemTitle());
        assertEquals("thumb_key", deal.getItemThumb());
        assertEquals(DealStatus.PENDING_CONFIRM, deal.getStatus());
        assertTrue(deal.isSellerConfirmed());
        assertFalse(deal.isBuyerConfirmed());
        assertNotNull(deal.getStatusHistory());
        assertEquals(1, deal.getStatusHistory().size());
        assertEquals(DealStatus.PENDING_CONFIRM, deal.getStatusHistory().get(0).getToStatus());
        assertEquals("seller1", deal.getStatusHistory().get(0).getChangedBy());
    }

    @Test
    public void buildFromOffer_mapsItemOfferToExchangeTradeType() {
        Listing listing = new Listing();
        listing.setTitle("Tai nghe");
        listing.setCoverThumb("thumb_headphone");

        Offer offer = new Offer();
        offer.setId("offer2");
        offer.setListingId("listing2");
        offer.setBuyerId("buyer2");
        offer.setSellerId("seller2");
        offer.setOfferType(OfferType.ITEM);
        offer.setAmount(0);
        offer.setExchangeItem("Loa bluetooth");

        Deal deal = DealRepository.createDealFromOffer(offer, listing);
        assertNotNull(deal);
        assertEquals(TradeType.EXCHANGE, deal.getTradeType());
        assertEquals(0, deal.getAgreedPrice());
        assertEquals("Loa bluetooth", deal.getExchangeItem());
    }

    @Test
    public void updateTerms_resetsBothConfirmationsAndDoesNotAddHistory() {
        Deal deal = createSamplePendingDeal();
        deal.setSellerConfirmed(true);
        deal.setBuyerConfirmed(true);

        int originalHistoryCount = deal.getStatusHistory().size();
        DealRepository.applyUpdateTerms(deal);

        assertFalse(deal.isSellerConfirmed());
        assertFalse(deal.isBuyerConfirmed());
        assertEquals(originalHistoryCount, deal.getStatusHistory().size());
        assertEquals(DealStatus.PENDING_CONFIRM, deal.getStatus());
    }

    @Test
    public void confirm_firstConfirmationOnlyEnablesOwnFlag() {
        Deal deal = createSamplePendingDeal();
        deal.setSellerConfirmed(false);
        deal.setBuyerConfirmed(false);
        deal.setDeliveryMethod(DeliveryMethod.MEET_UP);

        int originalHistoryCount = deal.getStatusHistory().size();
        boolean moved = DealRepository.applyConfirm(deal, "buyer1");

        assertFalse(moved);
        assertTrue(deal.isBuyerConfirmed());
        assertFalse(deal.isSellerConfirmed());
        assertEquals(DealStatus.PENDING_CONFIRM, deal.getStatus());
        assertEquals(originalHistoryCount, deal.getStatusHistory().size());
    }

    @Test
    public void confirm_secondConfirmationTransitionsToConfirmedAndAddsOneHistoryRow() {
        Deal deal = createSamplePendingDeal();
        deal.setSellerConfirmed(true);
        deal.setBuyerConfirmed(false);
        deal.setDeliveryMethod(DeliveryMethod.MEET_UP);

        int originalHistoryCount = deal.getStatusHistory().size();
        boolean moved = DealRepository.applyConfirm(deal, "buyer1");

        assertTrue(moved);
        assertTrue(deal.isBuyerConfirmed());
        assertTrue(deal.isSellerConfirmed());
        assertEquals(DealStatus.CONFIRMED, deal.getStatus());
        assertEquals(originalHistoryCount + 1, deal.getStatusHistory().size());
        int lastIndex = deal.getStatusHistory().size() - 1;
        assertEquals(DealStatus.CONFIRMED, deal.getStatusHistory().get(lastIndex).getToStatus());
    }

    @Test
    public void confirm_rejectsWhenDeliveryMethodMissing() {
        Deal deal = createSamplePendingDeal();
        deal.setDeliveryMethod(null);

        String error = DealRepository.validateConfirm(deal, "buyer1");
        assertNotNull(error);
        assertEquals("Chưa chọn phương thức giao nhận.", error);
    }

    @Test
    public void confirm_shippingRejectsEmptyAddressOnSecondConfirm() {
        Deal deal = createSamplePendingDeal();
        deal.setDeliveryMethod(DeliveryMethod.SHIPPING);
        deal.setDeliveryAddress("");
        deal.setSellerConfirmed(true);

        String error = DealRepository.validateConfirm(deal, "buyer1");
        assertNotNull(error);
        assertEquals("Chưa có địa chỉ giao hàng.", error);
    }

    @Test
    public void cancel_rejectsEmptyReason() {
        Deal deal = createSamplePendingDeal();

        String error = DealRepository.validateCancel(deal, "buyer1", "   ");
        assertNotNull(error);
        assertEquals("Lý do hủy không được để trống.", error);
    }

    @Test
    public void cancel_rejectsFromShippedReceivedCompletedDisputed() {
        Deal deal = createSamplePendingDeal();

        deal.setStatus(DealStatus.SHIPPED);
        assertNotNull(DealRepository.validateCancel(deal, "buyer1", "bận việc"));

        deal.setStatus(DealStatus.RECEIVED);
        assertNotNull(DealRepository.validateCancel(deal, "buyer1", "bận việc"));

        deal.setStatus(DealStatus.COMPLETED);
        assertNotNull(DealRepository.validateCancel(deal, "buyer1", "bận việc"));

        deal.setStatus(DealStatus.DISPUTED);
        assertNotNull(DealRepository.validateCancel(deal, "buyer1", "bận việc"));
    }

    @Test
    public void cancel_allowsFromPendingConfirmAndConfirmed() {
        Deal deal = createSamplePendingDeal();

        deal.setStatus(DealStatus.PENDING_CONFIRM);
        assertNull(DealRepository.validateCancel(deal, "buyer1", "Đổi ý"));

        deal.setStatus(DealStatus.CONFIRMED);
        assertNull(DealRepository.validateCancel(deal, "seller1", "Hết hàng"));
    }

    @Test
    public void cancel_recordsHistoryAndReason() {
        Deal deal = createSamplePendingDeal();
        int initialCount = deal.getStatusHistory().size();

        DealRepository.applyCancel(deal, "buyer1", "Không còn nhu cầu");

        assertEquals(DealStatus.CANCELLED, deal.getStatus());
        assertEquals("Không còn nhu cầu", deal.getCancelReason());
        assertEquals(initialCount + 1, deal.getStatusHistory().size());
        int lastIdx = deal.getStatusHistory().size() - 1;
        assertEquals(DealStatus.CANCELLED, deal.getStatusHistory().get(lastIdx).getToStatus());
        assertEquals("Không còn nhu cầu", deal.getStatusHistory().get(lastIdx).getReason());
    }

    @Test
    public void shouldRefundPayment_onlyWhenHeld() {
        Payment heldPayment = new Payment();
        heldPayment.setStatus(PaymentStatus.HELD);
        assertTrue(DealRepository.shouldRefundPayment(heldPayment));

        Payment releasedPayment = new Payment();
        releasedPayment.setStatus(PaymentStatus.RELEASED);
        assertFalse(DealRepository.shouldRefundPayment(releasedPayment));

        Payment refundedPayment = new Payment();
        refundedPayment.setStatus(PaymentStatus.REFUNDED);
        assertFalse(DealRepository.shouldRefundPayment(refundedPayment));

        assertFalse(DealRepository.shouldRefundPayment(null));
    }

    @Test
    public void shouldReleaseListing_onlyWhenReservedByCurrentDeal() {
        Listing listing = new Listing();
        listing.setStatus(ListingStatus.RESERVED);
        listing.setReservedDealId("deal123");

        assertTrue(DealRepository.shouldReleaseListing(listing, "deal123"));
        assertFalse(DealRepository.shouldReleaseListing(listing, "otherDeal"));

        listing.setStatus(ListingStatus.AVAILABLE);
        assertFalse(DealRepository.shouldReleaseListing(listing, "deal123"));

        assertFalse(DealRepository.shouldReleaseListing(null, "deal123"));
    }

    private Deal createSamplePendingDeal() {
        Deal deal = new Deal();
        deal.setId("deal1");
        deal.setBuyerId("buyer1");
        deal.setSellerId("seller1");
        deal.setStatus(DealStatus.PENDING_CONFIRM);
        deal.setDeliveryMethod(DeliveryMethod.MEET_UP);
        deal.setStatusHistory(new ArrayList<>());
        return deal;
    }
}
