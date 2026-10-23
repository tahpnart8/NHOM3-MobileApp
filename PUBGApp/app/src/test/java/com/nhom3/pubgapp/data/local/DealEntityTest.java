package com.nhom3.pubgapp.data.local;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.google.firebase.Timestamp;
import com.nhom3.pubgapp.model.Deal;
import com.nhom3.pubgapp.model.enums.DealStatus;
import com.nhom3.pubgapp.model.enums.DeliveryMethod;
import com.nhom3.pubgapp.model.enums.TradeType;

import org.junit.Test;

import java.util.Date;

// Unit test verifying Room DealEntity conversion round-trip.
public class DealEntityTest {

    @Test
    public void fromModelAndToModel_preservesAllFields() {
        Deal original = new Deal();
        original.setId("deal99");
        original.setListingId("listing12");
        original.setBuyerId("buyerA");
        original.setSellerId("sellerB");
        original.setItemTitle("Điện thoại Pixel 8");
        original.setItemThumb("thumb_base64_pixel");
        original.setAgreedPrice(12000000);
        original.setStatus(DealStatus.CONFIRMED);
        original.setTradeType(TradeType.SELL);
        original.setDeliveryMethod(DeliveryMethod.SHIPPING);

        Date now = new Date();
        original.setAutoReleaseAt(new Timestamp(now));
        original.setReceivedAt(new Timestamp(now));
        original.setUpdatedAt(new Timestamp(now));

        DealEntity entity = new DealEntity().fromModel(original);
        assertNotNull(entity);
        assertEquals("deal99", entity.getId());
        assertEquals("listing12", entity.getListingId());
        assertEquals("buyerA", entity.getBuyerId());
        assertEquals("sellerB", entity.getSellerId());
        assertEquals("Điện thoại Pixel 8", entity.getItemTitle());
        assertEquals("thumb_base64_pixel", entity.getItemThumb());
        assertEquals(12000000, entity.getAgreedPrice());
        assertEquals("CONFIRMED", entity.getStatus());
        assertEquals("SELL", entity.getTradeType());
        assertEquals("SHIPPING", entity.getDeliveryMethod());
        assertEquals(now.getTime(), entity.getAutoReleaseAtMillis());
        assertEquals(now.getTime(), entity.getReceivedAtMillis());
        assertEquals(now.getTime(), entity.getUpdatedAtMillis());

        Deal restored = entity.toModel();
        assertNotNull(restored);
        assertEquals("deal99", restored.getId());
        assertEquals("listing12", restored.getListingId());
        assertEquals("buyerA", restored.getBuyerId());
        assertEquals("sellerB", restored.getSellerId());
        assertEquals("Điện thoại Pixel 8", restored.getItemTitle());
        assertEquals("thumb_base64_pixel", restored.getItemThumb());
        assertEquals(12000000, restored.getAgreedPrice());
        assertEquals(DealStatus.CONFIRMED, restored.getStatus());
        assertEquals(TradeType.SELL, restored.getTradeType());
        assertEquals(DeliveryMethod.SHIPPING, restored.getDeliveryMethod());
        assertNotNull(restored.getAutoReleaseAt());
        assertEquals(now.getTime(), restored.getAutoReleaseAt().toDate().getTime());
        assertNotNull(restored.getReceivedAt());
        assertEquals(now.getTime(), restored.getReceivedAt().toDate().getTime());
        assertNotNull(restored.getUpdatedAt());
        assertEquals(now.getTime(), restored.getUpdatedAt().toDate().getTime());
    }
}
