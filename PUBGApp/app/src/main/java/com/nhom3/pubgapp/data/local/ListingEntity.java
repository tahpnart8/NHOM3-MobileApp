package com.nhom3.pubgapp.data.local;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.nhom3.pubgapp.model.Listing;

// Copy of a viewed listing so the feed still opens offline.
@Entity(tableName = "listing_cache")
public class ListingEntity {

    @PrimaryKey
    @NonNull
    private String id = "";

    private String sellerId;

    private String title;

    private String description;

    private String imageIdsJson;

    private String coverThumb;

    private long price;

    private String categoryId;

    private String tradeTypesJson;

    private String itemCondition;

    private long minOfferPrice;

    private String province;

    private String status;

    private long createdAtMillis;

    private long cachedAtMillis;

    // Room needs a public constructor it can call, then fills the fields through the setters.
    public ListingEntity() {
    }

    public ListingEntity fromModel(Listing model) {
        throw new UnsupportedOperationException("NOT_READY");
    }

    public Listing toModel() {
        throw new UnsupportedOperationException("NOT_READY");
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageIdsJson() {
        return imageIdsJson;
    }

    public void setImageIdsJson(String imageIdsJson) {
        this.imageIdsJson = imageIdsJson;
    }

    public String getCoverThumb() {
        return coverThumb;
    }

    public void setCoverThumb(String coverThumb) {
        this.coverThumb = coverThumb;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getTradeTypesJson() {
        return tradeTypesJson;
    }

    public void setTradeTypesJson(String tradeTypesJson) {
        this.tradeTypesJson = tradeTypesJson;
    }

    public String getItemCondition() {
        return itemCondition;
    }

    public void setItemCondition(String itemCondition) {
        this.itemCondition = itemCondition;
    }

    public long getMinOfferPrice() {
        return minOfferPrice;
    }

    public void setMinOfferPrice(long minOfferPrice) {
        this.minOfferPrice = minOfferPrice;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getCreatedAtMillis() {
        return createdAtMillis;
    }

    public void setCreatedAtMillis(long createdAtMillis) {
        this.createdAtMillis = createdAtMillis;
    }

    public long getCachedAtMillis() {
        return cachedAtMillis;
    }

    public void setCachedAtMillis(long cachedAtMillis) {
        this.cachedAtMillis = cachedAtMillis;
    }
}
