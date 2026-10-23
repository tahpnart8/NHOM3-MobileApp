package com.nhom3.pubgapp.model;

import com.google.firebase.Timestamp;
import com.nhom3.pubgapp.model.enums.DealStatus;
import com.nhom3.pubgapp.model.enums.DeliveryMethod;
import com.nhom3.pubgapp.model.enums.TradeType;

import java.util.List;

public class Deal extends BaseDocument {
    private static final int AUTO_RELEASE_DAYS = 7;
    private static final int AUTO_COMPLETE_DAYS = 3;

    private String listingId;
    private String offerId;
    private String buyerId;
    private String sellerId;
    private TradeType tradeType;
    private long agreedPrice;
    private String exchangeItem;
    private DeliveryMethod deliveryMethod;
    private String deliveryAddress;
    private Timestamp scheduledAt;
    private String agreedTerms;
    private String itemTitle;
    private String itemThumb;
    private DealStatus status;
    private boolean buyerConfirmed;
    private boolean sellerConfirmed;
    private String cancelReason;
    private String carrierName;
    private String trackingCode;
    private String trackingImageId;
    private Timestamp shippedAt;
    private Timestamp autoReleaseAt;
    private Timestamp receivedAt;
    private String receivedImageId;
    private List<StatusChange> statusHistory;

    public Deal() {
    }

    public String getListingId() {
        return listingId;
    }

    public void setListingId(String listingId) {
        this.listingId = listingId;
    }

    public String getOfferId() {
        return offerId;
    }

    public void setOfferId(String offerId) {
        this.offerId = offerId;
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

    public TradeType getTradeType() {
        return tradeType;
    }

    public void setTradeType(TradeType tradeType) {
        this.tradeType = tradeType;
    }

    public long getAgreedPrice() {
        return agreedPrice;
    }

    public void setAgreedPrice(long agreedPrice) {
        this.agreedPrice = agreedPrice;
    }

    public String getExchangeItem() {
        return exchangeItem;
    }

    public void setExchangeItem(String exchangeItem) {
        this.exchangeItem = exchangeItem;
    }

    public DeliveryMethod getDeliveryMethod() {
        return deliveryMethod;
    }

    public void setDeliveryMethod(DeliveryMethod deliveryMethod) {
        this.deliveryMethod = deliveryMethod;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public Timestamp getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(Timestamp scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public String getAgreedTerms() {
        return agreedTerms;
    }

    public void setAgreedTerms(String agreedTerms) {
        this.agreedTerms = agreedTerms;
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

    public DealStatus getStatus() {
        return status;
    }

    public void setStatus(DealStatus status) {
        this.status = status;
    }

    public boolean isBuyerConfirmed() {
        return buyerConfirmed;
    }

    public void setBuyerConfirmed(boolean buyerConfirmed) {
        this.buyerConfirmed = buyerConfirmed;
    }

    public boolean isSellerConfirmed() {
        return sellerConfirmed;
    }

    public void setSellerConfirmed(boolean sellerConfirmed) {
        this.sellerConfirmed = sellerConfirmed;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    public String getCarrierName() {
        return carrierName;
    }

    public void setCarrierName(String carrierName) {
        this.carrierName = carrierName;
    }

    public String getTrackingCode() {
        return trackingCode;
    }

    public void setTrackingCode(String trackingCode) {
        this.trackingCode = trackingCode;
    }

    public String getTrackingImageId() {
        return trackingImageId;
    }

    public void setTrackingImageId(String trackingImageId) {
        this.trackingImageId = trackingImageId;
    }

    public Timestamp getShippedAt() {
        return shippedAt;
    }

    public void setShippedAt(Timestamp shippedAt) {
        this.shippedAt = shippedAt;
    }

    public Timestamp getAutoReleaseAt() {
        return autoReleaseAt;
    }

    public void setAutoReleaseAt(Timestamp autoReleaseAt) {
        this.autoReleaseAt = autoReleaseAt;
    }

    public Timestamp getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(Timestamp receivedAt) {
        this.receivedAt = receivedAt;
    }

    public String getReceivedImageId() {
        return receivedImageId;
    }

    public void setReceivedImageId(String receivedImageId) {
        this.receivedImageId = receivedImageId;
    }

    public List<StatusChange> getStatusHistory() {
        return statusHistory;
    }

    public void setStatusHistory(List<StatusChange> statusHistory) {
        this.statusHistory = statusHistory;
    }

    public boolean canMoveTo(DealStatus target) {
        throw new UnsupportedOperationException("NOT_READY");
    }

    public void confirmBy(String userId) {
        throw new UnsupportedOperationException("NOT_READY");
    }

    public void markShipped(String carrier, String trackingCode, String imageId) {
        throw new UnsupportedOperationException("NOT_READY");
    }

    public boolean hasShippingProof() {
        throw new UnsupportedOperationException("NOT_READY");
    }

    public void markReceived(String imageId) {
        throw new UnsupportedOperationException("NOT_READY");
    }

    public boolean isAutoReleaseDue(Timestamp now) {
        throw new UnsupportedOperationException("NOT_READY");
    }

    public void autoReceive() {
        throw new UnsupportedOperationException("NOT_READY");
    }

    public boolean isAutoCompleteDue(Timestamp now) {
        throw new UnsupportedOperationException("NOT_READY");
    }

    public void complete() {
        throw new UnsupportedOperationException("NOT_READY");
    }

    public boolean isPendingExpired(Timestamp now) {
        throw new UnsupportedOperationException("NOT_READY");
    }

    public DealStatus statusBeforeDispute() {
        throw new UnsupportedOperationException("NOT_READY");
    }

    public void cancel(String userId, String reason) {
        throw new UnsupportedOperationException("NOT_READY");
    }

    public void dispute() {
        throw new UnsupportedOperationException("NOT_READY");
    }

    public void resolveDispute(DealStatus target) {
        throw new UnsupportedOperationException("NOT_READY");
    }

    public boolean isParticipant(String userId) {
        throw new UnsupportedOperationException("NOT_READY");
    }

    public boolean needsPayment() {
        throw new UnsupportedOperationException("NOT_READY");
    }
}
