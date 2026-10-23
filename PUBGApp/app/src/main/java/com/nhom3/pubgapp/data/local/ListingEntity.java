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
        if (model == null) {
            return this;
        }
        this.id = model.getId() != null ? model.getId() : "";
        this.sellerId = model.getSellerId();
        this.title = model.getTitle();
        this.description = model.getDescription();
        if (model.getImageIds() != null) {
            this.imageIdsJson = serializeStringList(model.getImageIds());
        }
        this.coverThumb = model.getCoverThumb();
        this.price = model.getPrice();
        this.categoryId = model.getCategoryId();
        if (model.getTradeTypes() != null) {
            java.util.List<String> types = new java.util.ArrayList<String>();
            for (int i = 0; i < model.getTradeTypes().size(); i++) {
                com.nhom3.pubgapp.model.enums.TradeType type = model.getTradeTypes().get(i);
                if (type != null) {
                    types.add(type.name());
                }
            }
            this.tradeTypesJson = serializeStringList(types);
        }
        if (model.getItemCondition() != null) {
            this.itemCondition = model.getItemCondition().name();
        }
        this.minOfferPrice = model.getMinOfferPrice();
        this.province = model.getProvince();
        if (model.getStatus() != null) {
            this.status = model.getStatus().name();
        }
        if (model.getCreatedAt() != null) {
            this.createdAtMillis = model.getCreatedAt().toDate().getTime();
        }
        this.cachedAtMillis = System.currentTimeMillis();
        return this;
    }

    public Listing toModel() {
        Listing listing = new Listing();
        listing.setId(id);
        listing.setSellerId(sellerId);
        listing.setTitle(title);
        listing.setDescription(description);
        if (imageIdsJson != null && !imageIdsJson.isEmpty()) {
            listing.setImageIds(deserializeStringList(imageIdsJson));
        }
        listing.setCoverThumb(coverThumb);
        listing.setPrice(price);
        listing.setCategoryId(categoryId);
        if (tradeTypesJson != null && !tradeTypesJson.isEmpty()) {
            java.util.List<String> typeStrings = deserializeStringList(tradeTypesJson);
            java.util.List<com.nhom3.pubgapp.model.enums.TradeType> list =
                    new java.util.ArrayList<com.nhom3.pubgapp.model.enums.TradeType>();
            for (int i = 0; i < typeStrings.size(); i++) {
                try {
                    list.add(com.nhom3.pubgapp.model.enums.TradeType.valueOf(typeStrings.get(i)));
                } catch (IllegalArgumentException ex) {
                    android.util.Log.w("ListingEntity", "Unknown TradeType in cache", ex);
                }
            }
            listing.setTradeTypes(list);
        }
        if (itemCondition != null && !itemCondition.isEmpty()) {
            try {
                listing.setItemCondition(com.nhom3.pubgapp.model.enums.ItemCondition.valueOf(itemCondition));
            } catch (IllegalArgumentException ex) {
                android.util.Log.w("ListingEntity", "Unknown ItemCondition in cache", ex);
            }
        }
        listing.setMinOfferPrice(minOfferPrice);
        listing.setProvince(province);
        if (status != null && !status.isEmpty()) {
            try {
                listing.setStatus(com.nhom3.pubgapp.model.enums.ListingStatus.valueOf(status));
            } catch (IllegalArgumentException ex) {
                android.util.Log.w("ListingEntity", "Unknown ListingStatus in cache", ex);
            }
        }
        if (createdAtMillis > 0) {
            listing.setCreatedAt(new com.google.firebase.Timestamp(new java.util.Date(createdAtMillis)));
        }
        return listing;
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

    private String serializeStringList(java.util.List<String> list) {
        if (list == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append("\"").append(list.get(i).replace("\"", "\\\"")).append("\"");
        }
        sb.append("]");
        return sb.toString();
    }

    private java.util.List<String> deserializeStringList(String json) {
        java.util.List<String> list = new java.util.ArrayList<String>();
        if (json == null) {
            return list;
        }
        String trimmed = json.trim();
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            String inner = trimmed.substring(1, trimmed.length() - 1).trim();
            if (!inner.isEmpty()) {
                String[] parts = inner.split(",");
                for (int i = 0; i < parts.length; i++) {
                    String part = parts[i].trim();
                    if (part.startsWith("\"") && part.endsWith("\"") && part.length() >= 2) {
                        part = part.substring(1, part.length() - 1);
                    }
                    list.add(part.replace("\\\"", "\""));
                }
            }
        }
        return list;
    }
}
