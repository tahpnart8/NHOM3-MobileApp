package com.nhom3.pubgapp.model;

import com.google.firebase.firestore.Exclude;
import com.nhom3.pubgapp.model.enums.ImagePurpose;

public class MediaImage extends BaseDocument {
    private String ownerId;
    private ImagePurpose purpose;
    private String data;
    private int width;
    private int height;
    private int sizeBytes;

    public MediaImage() {
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public ImagePurpose getPurpose() {
        return purpose;
    }

    public void setPurpose(ImagePurpose purpose) {
        this.purpose = purpose;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getSizeBytes() {
        return sizeBytes;
    }

    public void setSizeBytes(int sizeBytes) {
        this.sizeBytes = sizeBytes;
    }

    @Exclude
    public byte[] toBytes() {
        throw new UnsupportedOperationException("NOT_READY");
    }
}
