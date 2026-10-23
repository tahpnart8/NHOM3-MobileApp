package com.nhom3.pubgapp;

import com.google.firebase.Timestamp;
import com.nhom3.pubgapp.data.local.ListingEntity;
import com.nhom3.pubgapp.model.Listing;
import com.nhom3.pubgapp.model.enums.ItemCondition;
import com.nhom3.pubgapp.model.enums.ListingStatus;
import com.nhom3.pubgapp.model.enums.TradeType;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ListingEntityTest {

    @Test
    public void testFromModelAndToModel() {
        Listing listing = new Listing();
        listing.setId("list_123");
        listing.setSellerId("seller_456");
        listing.setTitle("Laptop Dell XPS 13");
        listing.setDescription("Máy nguyên bản, dùng văn phòng");
        listing.setPrice(18000000L);
        listing.setMinOfferPrice(16000000L);
        listing.setCategoryId("laptop_01");
        listing.setProvince("Hà Nội");
        listing.setItemCondition(ItemCondition.LIKE_NEW);
        listing.setStatus(ListingStatus.AVAILABLE);
        listing.setCoverThumb("base64_thumb_str");

        List<String> imageIds = new ArrayList<String>();
        imageIds.add("img_1");
        imageIds.add("img_2");
        listing.setImageIds(imageIds);

        List<TradeType> tradeTypes = new ArrayList<TradeType>();
        tradeTypes.add(TradeType.SELL);
        tradeTypes.add(TradeType.EXCHANGE);
        listing.setTradeTypes(tradeTypes);

        long nowMillis = System.currentTimeMillis();
        listing.setCreatedAt(new Timestamp(new Date(nowMillis)));

        ListingEntity entity = new ListingEntity().fromModel(listing);
        assertNotNull(entity);
        assertEquals("list_123", entity.getId());
        assertEquals("seller_456", entity.getSellerId());
        assertEquals("Laptop Dell XPS 13", entity.getTitle());
        assertEquals(18000000L, entity.getPrice());
        assertEquals("AVAILABLE", entity.getStatus());

        Listing restored = entity.toModel();
        assertNotNull(restored);
        assertEquals("list_123", restored.getId());
        assertEquals("seller_456", restored.getSellerId());
        assertEquals("Laptop Dell XPS 13", restored.getTitle());
        assertEquals("Máy nguyên bản, dùng văn phòng", restored.getDescription());
        assertEquals(18000000L, restored.getPrice());
        assertEquals(16000000L, restored.getMinOfferPrice());
        assertEquals("laptop_01", restored.getCategoryId());
        assertEquals("Hà Nội", restored.getProvince());
        assertEquals(ItemCondition.LIKE_NEW, restored.getItemCondition());
        assertEquals(ListingStatus.AVAILABLE, restored.getStatus());
        assertEquals("base64_thumb_str", restored.getCoverThumb());

        assertNotNull(restored.getImageIds());
        assertEquals(2, restored.getImageIds().size());
        assertEquals("img_1", restored.getImageIds().get(0));

        assertNotNull(restored.getTradeTypes());
        assertEquals(2, restored.getTradeTypes().size());
        assertEquals(TradeType.SELL, restored.getTradeTypes().get(0));
        assertEquals(TradeType.EXCHANGE, restored.getTradeTypes().get(1));
    }
}
