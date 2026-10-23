package com.nhom3.pubgapp.model;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.GeoPoint;
import com.nhom3.pubgapp.model.enums.ItemCondition;
import com.nhom3.pubgapp.model.enums.ListingStatus;
import com.nhom3.pubgapp.model.enums.OfferType;
import com.nhom3.pubgapp.model.enums.TradeType;

import java.util.List;
import java.util.Map;

public class Listing extends BaseDocument {
    private String sellerId;
    private String categoryId;
    private String title;
    private String description;
    private List<String> imageIds;
    private String coverThumb;
    private ItemCondition itemCondition;
    private Map<String, String> conditionValues;
    private List<TradeType> tradeTypes;
    private long price;
    private List<OfferType> acceptedOfferTypes;
    private long minOfferPrice;
    private String province;
    private GeoPoint location;
    private ListingStatus status;
    private String statusReason;
    private List<String> searchKeywords;
    private boolean flagged;
    private String reservedDealId;

    public Listing() {
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
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

    public List<String> getImageIds() {
        return imageIds;
    }

    public void setImageIds(List<String> imageIds) {
        this.imageIds = imageIds;
    }

    public String getCoverThumb() {
        return coverThumb;
    }

    public void setCoverThumb(String coverThumb) {
        this.coverThumb = coverThumb;
    }

    public ItemCondition getItemCondition() {
        return itemCondition;
    }

    public void setItemCondition(ItemCondition itemCondition) {
        this.itemCondition = itemCondition;
    }

    public Map<String, String> getConditionValues() {
        return conditionValues;
    }

    public void setConditionValues(Map<String, String> conditionValues) {
        this.conditionValues = conditionValues;
    }

    public List<TradeType> getTradeTypes() {
        return tradeTypes;
    }

    public void setTradeTypes(List<TradeType> tradeTypes) {
        this.tradeTypes = tradeTypes;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public List<OfferType> getAcceptedOfferTypes() {
        return acceptedOfferTypes;
    }

    public void setAcceptedOfferTypes(List<OfferType> acceptedOfferTypes) {
        this.acceptedOfferTypes = acceptedOfferTypes;
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

    public GeoPoint getLocation() {
        return location;
    }

    public void setLocation(GeoPoint location) {
        this.location = location;
    }

    public ListingStatus getStatus() {
        return status;
    }

    public void setStatus(ListingStatus status) {
        this.status = status;
    }

    public String getStatusReason() {
        return statusReason;
    }

    public void setStatusReason(String statusReason) {
        this.statusReason = statusReason;
    }

    public List<String> getSearchKeywords() {
        return searchKeywords;
    }

    public void setSearchKeywords(List<String> searchKeywords) {
        this.searchKeywords = searchKeywords;
    }

    public boolean isFlagged() {
        return flagged;
    }

    public void setFlagged(boolean flagged) {
        this.flagged = flagged;
    }

    public String getReservedDealId() {
        return reservedDealId;
    }

    public void setReservedDealId(String reservedDealId) {
        this.reservedDealId = reservedDealId;
    }

    @Exclude
    public boolean isAvailable() {
        throw new UnsupportedOperationException("NOT_READY");
    }

    @Exclude
    public boolean isDraft() {
        throw new UnsupportedOperationException("NOT_READY");
    }

    public void publish() {
        throw new UnsupportedOperationException("NOT_READY");
    }

    public boolean acceptsTradeType(TradeType tradeType) {
        throw new UnsupportedOperationException("NOT_READY");
    }

    public boolean acceptsOffer(Offer offer) {
        throw new UnsupportedOperationException("NOT_READY");
    }

    public void markSold() {
        throw new UnsupportedOperationException("NOT_READY");
    }

    public void hide(String reason) {
        throw new UnsupportedOperationException("NOT_READY");
    }

    public void restore() {
        throw new UnsupportedOperationException("NOT_READY");
    }

    public void remove(String reason) {
        throw new UnsupportedOperationException("NOT_READY");
    }

    public void reserve(String dealId) {
        throw new UnsupportedOperationException("NOT_READY");
    }

    public void release() {
        throw new UnsupportedOperationException("NOT_READY");
    }

    public void buildSearchKeywords() {
        throw new UnsupportedOperationException("NOT_READY");
    }
}
