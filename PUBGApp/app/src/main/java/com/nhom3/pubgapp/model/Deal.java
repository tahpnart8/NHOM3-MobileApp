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
        if (target == null) {
            return false;
        }
        if (status == null) {
            return target == DealStatus.PENDING_CONFIRM;
        }
        if (status == DealStatus.PENDING_CONFIRM) {
            return target == DealStatus.CONFIRMED || target == DealStatus.CANCELLED;
        }
        if (status == DealStatus.CONFIRMED) {
            return target == DealStatus.CANCELLED
                    || target == DealStatus.SHIPPED
                    || target == DealStatus.RECEIVED
                    || target == DealStatus.DISPUTED;
        }
        if (status == DealStatus.SHIPPED) {
            return target == DealStatus.RECEIVED || target == DealStatus.DISPUTED;
        }
        if (status == DealStatus.RECEIVED) {
            return target == DealStatus.COMPLETED;
        }
        if (status == DealStatus.DISPUTED) {
            return target == DealStatus.CANCELLED
                    || target == DealStatus.RECEIVED
                    || target == DealStatus.SHIPPED
                    || target == DealStatus.CONFIRMED;
        }
        return false;
    }

    public void confirmBy(String userId) {
        if (userId == null) {
            return;
        }
        if (userId.equals(buyerId)) {
            buyerConfirmed = true;
        } else if (userId.equals(sellerId)) {
            sellerConfirmed = true;
        }
        if (buyerConfirmed && sellerConfirmed && status == DealStatus.PENDING_CONFIRM) {
            status = DealStatus.CONFIRMED;
        }
    }

    public void markShipped(String carrier, String trackingCode, String imageId) {
        this.carrierName = carrier;
        this.trackingCode = trackingCode;
        this.trackingImageId = imageId;
        if (status == DealStatus.CONFIRMED && deliveryMethod == DeliveryMethod.SHIPPING && hasShippingProof()) {
            this.status = DealStatus.SHIPPED;
            this.shippedAt = Timestamp.now();
            long sevenDaysInMillis = (long) AUTO_RELEASE_DAYS * 24 * 60 * 60 * 1000;
            long releaseTime = this.shippedAt.toDate().getTime() + sevenDaysInMillis;
            this.autoReleaseAt = new Timestamp(new java.util.Date(releaseTime));
        }
    }

    public boolean hasShippingProof() {
        return carrierName != null && !carrierName.trim().isEmpty()
                && trackingCode != null && !trackingCode.trim().isEmpty()
                && trackingImageId != null && !trackingImageId.trim().isEmpty();
    }

    public void markReceived(String imageId) {
        this.receivedImageId = imageId;
        this.status = DealStatus.RECEIVED;
        this.receivedAt = Timestamp.now();
    }

    public boolean isAutoReleaseDue(Timestamp now) {
        if (status != DealStatus.SHIPPED || autoReleaseAt == null || now == null) {
            return false;
        }
        return now.compareTo(autoReleaseAt) >= 0;
    }

    public void autoReceive() {
        if (status == DealStatus.SHIPPED) {
            this.status = DealStatus.RECEIVED;
            this.receivedAt = Timestamp.now();
            this.receivedImageId = "";
        }
    }

    public boolean isAutoCompleteDue(Timestamp now) {
        if (status != DealStatus.RECEIVED || receivedAt == null || now == null) {
            return false;
        }
        long completeDaysInMillis = (long) AUTO_COMPLETE_DAYS * 24 * 60 * 60 * 1000;
        long dueTime = receivedAt.toDate().getTime() + completeDaysInMillis;
        return now.toDate().getTime() >= dueTime;
    }

    public void complete() {
        this.status = DealStatus.COMPLETED;
    }

    public boolean isPendingExpired(Timestamp now) {
        if (status != DealStatus.PENDING_CONFIRM || getCreatedAt() == null || now == null) {
            return false;
        }
        long threeDaysInMillis = 3L * 24 * 60 * 60 * 1000;
        long expiredTime = getCreatedAt().toDate().getTime() + threeDaysInMillis;
        return now.toDate().getTime() >= expiredTime;
    }

    public DealStatus statusBeforeDispute() {
        if (statusHistory == null || statusHistory.isEmpty()) {
            return null;
        }
        for (int i = statusHistory.size() - 1; i >= 0; i--) {
            StatusChange sc = statusHistory.get(i);
            if (sc.getToStatus() == DealStatus.DISPUTED) {
                return sc.getFromStatus();
            }
        }
        return null;
    }

    public void cancel(String userId, String reason) {
        this.status = DealStatus.CANCELLED;
        this.cancelReason = reason;
    }

    public void dispute() {
        this.status = DealStatus.DISPUTED;
    }

    public void resolveDispute(DealStatus target) {
        if (canMoveTo(target)) {
            this.status = target;
        }
    }

    public boolean isParticipant(String userId) {
        if (userId == null) {
            return false;
        }
        return userId.equals(buyerId) || userId.equals(sellerId);
    }

    public boolean needsPayment() {
        return agreedPrice > 0;
    }
}
