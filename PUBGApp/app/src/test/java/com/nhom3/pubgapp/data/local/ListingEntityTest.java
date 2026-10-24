package com.nhom3.pubgapp.data.local;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.google.firebase.Timestamp;
import com.nhom3.pubgapp.model.Listing;
import com.nhom3.pubgapp.model.enums.ItemCondition;
import com.nhom3.pubgapp.model.enums.ListingStatus;
import com.nhom3.pubgapp.model.enums.TradeType;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ListingEntityTest {

    @Test
    public void testFromModelAndToModelPreservesFields() {
        Listing listing = new Listing();
        listing.setId("list_room_01");
        listing.setSellerId("seller_01");
        listing.setTitle("Tai nghe Sony WH-1000XM4");
        listing.setDescription("Tai nghe chống ồn tốt, còn đầy đủ hộp");
        listing.setPrice(4500000L);
        listing.setMinOfferPrice(4000000L);
        listing.setCategoryId("audio_cat");
        listing.setProvince("TP.HCM");
        listing.setItemCondition(ItemCondition.LIKE_NEW);
        listing.setStatus(ListingStatus.AVAILABLE);
        listing.setCoverThumb("thumb_base64_data");

        List<String> imageIds = new ArrayList<String>();
        imageIds.add("img_a");
        imageIds.add("img_b");
        listing.setImageIds(imageIds);

        List<TradeType> tradeTypes = new ArrayList<TradeType>();
        tradeTypes.add(TradeType.SELL);
        listing.setTradeTypes(tradeTypes);

        long now = 1700000000123L;
        listing.setCreatedAt(new Timestamp(new Date(now)));

        ListingEntity entity = new ListingEntity().fromModel(listing);
        assertNotNull(entity);
        assertEquals("list_room_01", entity.getId());
        assertEquals("seller_01", entity.getSellerId());
        assertEquals("Tai nghe Sony WH-1000XM4", entity.getTitle());
        assertEquals(4500000L, entity.getPrice());
        assertEquals("AVAILABLE", entity.getStatus());
        assertEquals(now, entity.getCreatedAtMillis());

        Listing restored = entity.toModel();
        assertNotNull(restored);
        assertEquals("list_room_01", restored.getId());
        assertEquals("seller_01", restored.getSellerId());
        assertEquals("Tai nghe Sony WH-1000XM4", restored.getTitle());
        assertEquals("Tai nghe chống ồn tốt, còn đầy đủ hộp", restored.getDescription());
        assertEquals(4500000L, restored.getPrice());
        assertEquals(4000000L, restored.getMinOfferPrice());
        assertEquals("audio_cat", restored.getCategoryId());
        assertEquals("TP.HCM", restored.getProvince());
        assertEquals(ItemCondition.LIKE_NEW, restored.getItemCondition());
        assertEquals(ListingStatus.AVAILABLE, restored.getStatus());
        assertEquals("thumb_base64_data", restored.getCoverThumb());

        assertNotNull(restored.getImageIds());
        assertEquals(2, restored.getImageIds().size());
        assertEquals("img_a", restored.getImageIds().get(0));
        assertEquals("img_b", restored.getImageIds().get(1));

        assertNotNull(restored.getTradeTypes());
        assertEquals(1, restored.getTradeTypes().size());
        assertEquals(TradeType.SELL, restored.getTradeTypes().get(0));

        assertNotNull(restored.getCreatedAt());
        assertEquals(now, restored.getCreatedAt().toDate().getTime());
    }

    @Test
    public void testFromModelWithNullModelReturnsEmptyEntity() {
        ListingEntity entity = new ListingEntity().fromModel(null);
        assertNotNull(entity);
        assertEquals("", entity.getId());
        assertNull(entity.getTitle());
    }
}
