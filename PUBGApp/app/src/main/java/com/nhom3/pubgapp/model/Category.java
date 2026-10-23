package com.nhom3.pubgapp.model;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;

import java.util.List;

public class Category extends BaseDocument {
    private String name;
    private String parentId;
    private List<String> conditionFields;
    private boolean isActive;

    public Category() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public List<String> getConditionFields() {
        return conditionFields;
    }

    public void setConditionFields(List<String> conditionFields) {
        this.conditionFields = conditionFields;
    }

    @PropertyName("isActive")
    public boolean isActive() {
        return isActive;
    }

    @PropertyName("isActive")
    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    @Exclude
    public boolean isRoot() {
        return parentId == null || parentId.trim().isEmpty();
    }

    public boolean hasConditionField(String fieldName) {
        if (conditionFields == null || fieldName == null) {
            return false;
        }
        return conditionFields.contains(fieldName);
    }
}
