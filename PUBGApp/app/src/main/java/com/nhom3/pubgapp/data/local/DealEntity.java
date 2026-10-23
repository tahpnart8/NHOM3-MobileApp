package com.nhom3.pubgapp.data.local;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.nhom3.pubgapp.model.Deal;

// Copy of the user's deal history.
@Entity(tableName = "deal_cache")
public class DealEntity {

    @PrimaryKey
    @NonNull
    private String id = "";

    private String listingId;

    private String buyerId;

    private String sellerId;

    private String itemTitle;

    private String itemThumb;

    private long agreedPrice;

    private String status;

    private String tradeType;

    private String deliveryMethod;

    private long autoReleaseAtMillis;

    private long receivedAtMillis;

    private long updatedAtMillis;

    // Room needs a public constructor it can call, then fills the fields through the setters.
    public DealEntity() {
    }

    public DealEntity fromModel(Deal model) {
        if (model == null) {
            return this;
        }
        this.id = model.getId() != null ? model.getId() : "";
        this.listingId = model.getListingId();
        this.buyerId = model.getBuyerId();
        this.sellerId = model.getSellerId();
        this.itemTitle = model.getItemTitle();
        this.itemThumb = model.getItemThumb();
        this.agreedPrice = model.getAgreedPrice();
        if (model.getStatus() != null) {
            this.status = model.getStatus().name();
        }
        if (model.getTradeType() != null) {
            this.tradeType = model.getTradeType().name();
        }
        if (model.getDeliveryMethod() != null) {
            this.deliveryMethod = model.getDeliveryMethod().name();
        }
        if (model.getAutoReleaseAt() != null) {
            this.autoReleaseAtMillis = model.getAutoReleaseAt().toDate().getTime();
        }
        if (model.getReceivedAt() != null) {
            this.receivedAtMillis = model.getReceivedAt().toDate().getTime();
        }
        if (model.getUpdatedAt() != null) {
            this.updatedAtMillis = model.getUpdatedAt().toDate().getTime();
        }
        return this;
    }

    public Deal toModel() {
        Deal deal = new Deal();
        deal.setId(this.id);
        deal.setListingId(this.listingId);
        deal.setBuyerId(this.buyerId);
        deal.setSellerId(this.sellerId);
        deal.setItemTitle(this.itemTitle);
        deal.setItemThumb(this.itemThumb);
        deal.setAgreedPrice(this.agreedPrice);
        if (this.status != null) {
            try {
                deal.setStatus(com.nhom3.pubgapp.model.enums.DealStatus.valueOf(this.status));
            } catch (IllegalArgumentException e) {
                // Ignore
            }
        }
        if (this.tradeType != null) {
            try {
                deal.setTradeType(com.nhom3.pubgapp.model.enums.TradeType.valueOf(this.tradeType));
            } catch (IllegalArgumentException e) {
                // Ignore
            }
        }
        if (this.deliveryMethod != null) {
            try {
                deal.setDeliveryMethod(com.nhom3.pubgapp.model.enums.DeliveryMethod.valueOf(this.deliveryMethod));
            } catch (IllegalArgumentException e) {
                // Ignore
            }
        }
        if (this.autoReleaseAtMillis > 0) {
            java.util.Date date = new java.util.Date(this.autoReleaseAtMillis);
            deal.setAutoReleaseAt(new com.google.firebase.Timestamp(date));
        }
        if (this.receivedAtMillis > 0) {
            java.util.Date date = new java.util.Date(this.receivedAtMillis);
            deal.setReceivedAt(new com.google.firebase.Timestamp(date));
        }
        if (this.updatedAtMillis > 0) {
            java.util.Date date = new java.util.Date(this.updatedAtMillis);
            deal.setUpdatedAt(new com.google.firebase.Timestamp(date));
        }
        return deal;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getListingId() {
        return listingId;
    }

    public void setListingId(String listingId) {
        this.listingId = listingId;
    }

    public String getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(String buyerId) {
        this.buyerId = buyerId;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getItemTitle() {
        return itemTitle;
    }

    public void setItemTitle(String itemTitle) {
        this.itemTitle = itemTitle;
    }

    public String getItemThumb() {
        return itemThumb;
    }

    public void setItemThumb(String itemThumb) {
        this.itemThumb = itemThumb;
    }

    public long getAgreedPrice() {
        return agreedPrice;
    }

    public void setAgreedPrice(long agreedPrice) {
        this.agreedPrice = agreedPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTradeType() {
        return tradeType;
    }

    public void setTradeType(String tradeType) {
        this.tradeType = tradeType;
    }

    public String getDeliveryMethod() {
        return deliveryMethod;
    }

    public void setDeliveryMethod(String deliveryMethod) {
        this.deliveryMethod = deliveryMethod;
    }

    public long getAutoReleaseAtMillis() {
        return autoReleaseAtMillis;
    }

    public void setAutoReleaseAtMillis(long autoReleaseAtMillis) {
        this.autoReleaseAtMillis = autoReleaseAtMillis;
    }

    public long getReceivedAtMillis() {
        return receivedAtMillis;
    }

    public void setReceivedAtMillis(long receivedAtMillis) {
        this.receivedAtMillis = receivedAtMillis;
    }

    public long getUpdatedAtMillis() {
        return updatedAtMillis;
    }

    public void setUpdatedAtMillis(long updatedAtMillis) {
        this.updatedAtMillis = updatedAtMillis;
    }
}
