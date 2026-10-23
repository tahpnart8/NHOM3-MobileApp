package com.nhom3.pubgapp.model;

import com.google.firebase.firestore.GeoPoint;
import com.nhom3.pubgapp.model.enums.ItemCondition;
import com.nhom3.pubgapp.model.enums.SortOrder;
import com.nhom3.pubgapp.model.enums.TradeType;

import java.util.List;

public class SearchFilter {
    private String keyword;
    private String categoryId;
    private long minPrice;
    private long maxPrice;
    private List<ItemCondition> conditions;
    private String province;
    private GeoPoint center;
    private int radiusKm;
    private List<TradeType> tradeTypes;
    private SortOrder sortOrder;
    private boolean followingOnly;

    public SearchFilter() {
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public long getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(long minPrice) {
        this.minPrice = minPrice;
    }

    public long getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(long maxPrice) {
        this.maxPrice = maxPrice;
    }

    public List<ItemCondition> getConditions() {
        return conditions;
    }

    public void setConditions(List<ItemCondition> conditions) {
        this.conditions = conditions;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public GeoPoint getCenter() {
        return center;
    }

    public void setCenter(GeoPoint center) {
        this.center = center;
    }

    public int getRadiusKm() {
        return radiusKm;
    }

    public void setRadiusKm(int radiusKm) {
        this.radiusKm = radiusKm;
    }

    public List<TradeType> getTradeTypes() {
        return tradeTypes;
    }

    public void setTradeTypes(List<TradeType> tradeTypes) {
        this.tradeTypes = tradeTypes;
    }

    public SortOrder getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(SortOrder sortOrder) {
        this.sortOrder = sortOrder;
    }

    public boolean isFollowingOnly() {
        return followingOnly;
    }

    public void setFollowingOnly(boolean followingOnly) {
        this.followingOnly = followingOnly;
    }

    public boolean hasPriceRange() {
        throw new UnsupportedOperationException("NOT_READY");
    }

    public boolean hasRadius() {
        throw new UnsupportedOperationException("NOT_READY");
    }

    public void reset() {
        throw new UnsupportedOperationException("NOT_READY");
    }
}
