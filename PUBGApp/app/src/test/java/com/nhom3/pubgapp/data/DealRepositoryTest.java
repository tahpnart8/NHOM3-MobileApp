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

    @Test
    public void validatePayment_rejectsNonBuyer() {
        Deal deal = createSampleConfirmedDeal(500000);
        String error = DealRepository.validatePayment(deal, null, "seller1",
                com.nhom3.pubgapp.model.enums.PaymentMethod.CARD);
        assertNotNull(error);
        assertEquals("Chỉ người mua mới được thực hiện thanh toán.", error);
    }

    @Test
    public void validatePayment_rejectsWhenNotConfirmed() {
        Deal deal = createSamplePendingDeal();
        deal.setAgreedPrice(500000);
        String error = DealRepository.validatePayment(deal, null, "buyer1",
                com.nhom3.pubgapp.model.enums.PaymentMethod.CARD);
        assertNotNull(error);
        assertEquals("Giao dịch chưa ở trạng thái đã xác nhận để thanh toán.", error);
    }

    @Test
    public void validatePayment_rejectsWhenAgreedPriceZero() {
        Deal deal = createSampleConfirmedDeal(0);
        String error = DealRepository.validatePayment(deal, null, "buyer1",
                com.nhom3.pubgapp.model.enums.PaymentMethod.CARD);
        assertNotNull(error);
        assertEquals("Giao dịch không yêu cầu thanh toán.", error);
    }

    @Test
    public void validatePayment_rejectsWhenAlreadyPaid() {
        Deal deal = createSampleConfirmedDeal(500000);
        Payment existing = new Payment();
        existing.setStatus(PaymentStatus.HELD);

        String error = DealRepository.validatePayment(deal, existing, "buyer1",
                com.nhom3.pubgapp.model.enums.PaymentMethod.CARD);
        assertNotNull(error);
        assertEquals("Giao dịch đã được thanh toán.", error);
    }

    @Test
    public void validatePayment_rejectsMissingMethod() {
        Deal deal = createSampleConfirmedDeal(500000);
        String error = DealRepository.validatePayment(deal, null, "buyer1", null);
        assertNotNull(error);
        assertEquals("Chưa chọn phương thức thanh toán.", error);
    }

    @Test
    public void validatePayment_acceptsValidPayment() {
        Deal deal = createSampleConfirmedDeal(500000);
        String error = DealRepository.validatePayment(deal, null, "buyer1",
                com.nhom3.pubgapp.model.enums.PaymentMethod.CARD);
        assertNull(error);
    }

    @Test
    public void createHeldPayment_setsHeldStatusAndAmountEqualsAgreedPrice() {
        Deal deal = createSampleConfirmedDeal(750000);
        Payment payment = DealRepository.createHeldPayment(deal,
                com.nhom3.pubgapp.model.enums.PaymentMethod.E_WALLET);

        assertNotNull(payment);
        assertEquals("deal1", payment.getDealId());
        assertEquals("buyer1", payment.getPayerId());
        assertEquals("seller1", payment.getPayeeId());
        assertEquals(750000, payment.getAmount());
        assertEquals(PaymentStatus.HELD, payment.getStatus());
        assertEquals(com.nhom3.pubgapp.model.enums.PaymentMethod.E_WALLET, payment.getMethod());
        assertNotNull(payment.getHeldAt());
    }

    @Test
    public void validateShipping_rejectsMeetUp() {
        Deal deal = createSampleConfirmedDeal(0);
        deal.setDeliveryMethod(DeliveryMethod.MEET_UP);
        String error = DealRepository.validateShipping(deal, null, "seller1",
                "GHTK", "TRACK123", "img1");
        assertNotNull(error);
        assertEquals("Chỉ giao dịch vận chuyển mới có thể xác nhận giao hàng.", error);
    }

    @Test
    public void validateShipping_rejectsMissingEvidence() {
        Deal deal = createSampleConfirmedDeal(0);
        deal.setDeliveryMethod(DeliveryMethod.SHIPPING);

        String err1 = DealRepository.validateShipping(deal, null, "seller1", "", "TRACK123", "img1");
        assertNotNull(err1);

        String err2 = DealRepository.validateShipping(deal, null, "seller1", "GHTK", "", "img1");
        assertNotNull(err2);

        String err3 = DealRepository.validateShipping(deal, null, "seller1", "GHTK", "TRACK123", "");
        assertNotNull(err3);
    }

    @Test
    public void validateShipping_rejectsWhenNeedsPaymentAndNotHeld() {
        Deal deal = createSampleConfirmedDeal(200000);
        deal.setDeliveryMethod(DeliveryMethod.SHIPPING);

        String errNull = DealRepository.validateShipping(deal, null, "seller1",
                "GHTK", "TRACK123", "img1");
        assertNotNull(errNull);
        assertEquals("Giao dịch cần được người mua thanh toán ký quỹ trước khi giao.", errNull);

        Payment unheld = new Payment();
        unheld.setStatus(PaymentStatus.RELEASED);
        String errNotHeld = DealRepository.validateShipping(deal, unheld, "seller1",
                "GHTK", "TRACK123", "img1");
        assertNotNull(errNotHeld);
    }

    @Test
    public void validateShipping_acceptsWhenPriceZeroWithoutPayment() {
        Deal deal = createSampleConfirmedDeal(0);
        deal.setDeliveryMethod(DeliveryMethod.SHIPPING);
        String error = DealRepository.validateShipping(deal, null, "seller1",
                "GHTK", "TRACK123", "img1");
        assertNull(error);
    }

    @Test
    public void validateShipping_acceptsWhenNeedsPaymentAndHeld() {
        Deal deal = createSampleConfirmedDeal(300000);
        deal.setDeliveryMethod(DeliveryMethod.SHIPPING);
        Payment held = new Payment();
        held.setStatus(PaymentStatus.HELD);

        String error = DealRepository.validateShipping(deal, held, "seller1",
                "GHTK", "TRACK123", "img1");
        assertNull(error);
    }

    @Test
    public void calculateAutoReleaseAt_addsSevenDays() {
        Timestamp shippedAt = new Timestamp(new java.util.Date(1700000000000L));
        Timestamp autoReleaseAt = DealRepository.calculateAutoReleaseAt(shippedAt);
        assertNotNull(autoReleaseAt);

        long expected = 1700000000000L + 7L * 24 * 60 * 60 * 1000;
        assertEquals(expected, autoReleaseAt.toDate().getTime());
        assertNull(DealRepository.calculateAutoReleaseAt(null));
    }

    @Test
    public void validateReceiving_acceptsShippedAndConfirmedMeetUp() {
        Deal shippingDeal = createSampleConfirmedDeal(0);
        shippingDeal.setDeliveryMethod(DeliveryMethod.SHIPPING);
        shippingDeal.setStatus(DealStatus.SHIPPED);

        String errShipping = DealRepository.validateReceiving(shippingDeal, null, "buyer1", "img_recv");
        assertNull(errShipping);

        Deal meetUpDeal = createSampleConfirmedDeal(0);
        meetUpDeal.setDeliveryMethod(DeliveryMethod.MEET_UP);
        meetUpDeal.setStatus(DealStatus.CONFIRMED);

        String errMeetUp = DealRepository.validateReceiving(meetUpDeal, null, "buyer1", "img_recv");
        assertNull(errMeetUp);
    }

    @Test
    public void validateReceiving_rejectsConfirmedShipping() {
        Deal deal = createSampleConfirmedDeal(0);
        deal.setDeliveryMethod(DeliveryMethod.SHIPPING);
        deal.setStatus(DealStatus.CONFIRMED);

        String error = DealRepository.validateReceiving(deal, null, "buyer1", "img_recv");
        assertNotNull(error);
        assertEquals("Người bán chưa giao hàng.", error);
    }

    @Test
    public void validateReceiving_rejectsSeller() {
        Deal deal = createSampleConfirmedDeal(0);
        deal.setDeliveryMethod(DeliveryMethod.SHIPPING);
        deal.setStatus(DealStatus.SHIPPED);

        String error = DealRepository.validateReceiving(deal, null, "seller1", "img_recv");
        assertNotNull(error);
        assertEquals("Chỉ người mua mới có quyền xác nhận đã nhận hàng.", error);
    }

    @Test
    public void validateReceiving_rejectsMissingImage() {
        Deal deal = createSampleConfirmedDeal(0);
        deal.setDeliveryMethod(DeliveryMethod.SHIPPING);
        deal.setStatus(DealStatus.SHIPPED);

        String errNull = DealRepository.validateReceiving(deal, null, "buyer1", null);
        assertNotNull(errNull);

        String errEmpty = DealRepository.validateReceiving(deal, null, "buyer1", "   ");
        assertNotNull(errEmpty);
    }

    @Test
    public void validateReceiving_rejectsWhenNeedsPaymentAndNotHeld() {
        Deal deal = createSampleConfirmedDeal(500000);
        deal.setDeliveryMethod(DeliveryMethod.SHIPPING);
        deal.setStatus(DealStatus.SHIPPED);

        String errNull = DealRepository.validateReceiving(deal, null, "buyer1", "img_recv");
        assertNotNull(errNull);

        Payment payment = new Payment();
        payment.setStatus(PaymentStatus.RELEASED);
        String errNotHeld = DealRepository.validateReceiving(deal, payment, "buyer1", "img_recv");
        assertNotNull(errNotHeld);
    }

    @Test
    public void validateReceiving_acceptsWhenNeedsPaymentAndHeld() {
        Deal deal = createSampleConfirmedDeal(500000);
        deal.setDeliveryMethod(DeliveryMethod.SHIPPING);
        deal.setStatus(DealStatus.SHIPPED);

        Payment payment = new Payment();
        payment.setStatus(PaymentStatus.HELD);
        String error = DealRepository.validateReceiving(deal, payment, "buyer1", "img_recv");
        assertNull(error);
    }

    private Deal createSampleConfirmedDeal(long price) {
        Deal deal = new Deal();
        deal.setId("deal1");
        deal.setBuyerId("buyer1");
        deal.setSellerId("seller1");
        deal.setStatus(DealStatus.CONFIRMED);
        deal.setAgreedPrice(price);
        deal.setDeliveryMethod(DeliveryMethod.MEET_UP);
        deal.setStatusHistory(new ArrayList<>());
        return deal;
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
