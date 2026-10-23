package com.nhom3.pubgapp.model;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.PropertyName;

public class UserAddress extends BaseDocument {
    private String userId;
    private String label;
    private String receiverName;
    private String phone;
    private String province;
    private String district;
    private String detail;
    private GeoPoint location;
    private boolean isDefault;

    public UserAddress() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public GeoPoint getLocation() {
        return location;
    }

    public void setLocation(GeoPoint location) {
        this.location = location;
    }

    @PropertyName("isDefault")
    public boolean isDefault() {
        return isDefault;
    }

    @PropertyName("isDefault")
    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    @Exclude
    public String getFullText() {
        StringBuilder builder = new StringBuilder();
        if (detail != null && !detail.trim().isEmpty()) {
            builder.append(detail.trim());
        }
        if (district != null && !district.trim().isEmpty()) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(district.trim());
        }
        if (province != null && !province.trim().isEmpty()) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(province.trim());
        }
        return builder.toString();
    }
}

