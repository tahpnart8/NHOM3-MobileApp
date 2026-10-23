package com.nhom3.pubgapp.model;

import com.nhom3.pubgapp.model.enums.OfferStatus;
import com.nhom3.pubgapp.model.enums.OfferType;

public class Offer extends BaseDocument {
    private String listingId;
    private String conversationId;
    private String buyerId;
    private String sellerId;
    private OfferType offerType;
    private long amount;
    private String exchangeItem;
    private OfferStatus status;

    public Offer() {
    }

    public String getListingId() {
        return listingId;
    }

    public void setListingId(String listingId) {
        this.listingId = listingId;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
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

    public OfferType getOfferType() {
        return offerType;
    }

    public void setOfferType(OfferType offerType) {
        this.offerType = offerType;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public String getExchangeItem() {
        return exchangeItem;
    }

    public void setExchangeItem(String exchangeItem) {
        this.exchangeItem = exchangeItem;
    }

    public OfferStatus getStatus() {
        return status;
    }

    public void setStatus(OfferStatus status) {
        this.status = status;
    }

    public void accept() {
        this.status = OfferStatus.ACCEPTED;
    }

    public void reject() {
        this.status = OfferStatus.REJECTED;
    }
}
