package com.nhom3.pubgapp.model;

import com.google.firebase.firestore.Exclude;
import com.nhom3.pubgapp.model.enums.ComplaintDecision;
import com.nhom3.pubgapp.model.enums.ComplaintReason;
import com.nhom3.pubgapp.model.enums.ComplaintStatus;
import com.nhom3.pubgapp.model.enums.Priority;

import java.util.List;

public class Complaint extends BaseDocument {
    private String complainantId;
    private String dealId;
    private ComplaintReason reason;
    private String description;
    private List<String> evidenceImageIds;
    private ComplaintStatus status;
    private Priority priority;
    private ComplaintDecision decision;
    private String evidenceRequest;
    private String resolutionMessage;
    private String adminNote;
    private String handledBy;

    public Complaint() {
    }

    public String getComplainantId() {
        return complainantId;
    }

    public void setComplainantId(String complainantId) {
        this.complainantId = complainantId;
    }

    public String getDealId() {
        return dealId;
    }

    public void setDealId(String dealId) {
        this.dealId = dealId;
    }

    public ComplaintReason getReason() {
        return reason;
    }

    public void setReason(ComplaintReason reason) {
        this.reason = reason;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getEvidenceImageIds() {
        return evidenceImageIds;
    }

    public void setEvidenceImageIds(List<String> evidenceImageIds) {
        this.evidenceImageIds = evidenceImageIds;
    }

    public ComplaintStatus getStatus() {
        return status;
    }

    public void setStatus(ComplaintStatus status) {
        this.status = status;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public ComplaintDecision getDecision() {
        return decision;
    }

    public void setDecision(ComplaintDecision decision) {
        this.decision = decision;
    }

    public String getEvidenceRequest() {
        return evidenceRequest;
    }

    public void setEvidenceRequest(String evidenceRequest) {
        this.evidenceRequest = evidenceRequest;
    }

    public String getResolutionMessage() {
        return resolutionMessage;
    }

    public void setResolutionMessage(String resolutionMessage) {
        this.resolutionMessage = resolutionMessage;
    }

    @Exclude
    public String getAdminNote() {
        return adminNote;
    }

    @Exclude
    public void setAdminNote(String adminNote) {
        this.adminNote = adminNote;
    }

    public String getHandledBy() {
        return handledBy;
    }

    public void setHandledBy(String handledBy) {
        this.handledBy = handledBy;
    }

    public void withdraw() {
        throw new UnsupportedOperationException("NOT_READY");
    }

    public void decide(ComplaintDecision decision, String adminId) {
        throw new UnsupportedOperationException("NOT_READY");
    }

    public void addEvidence(List<String> imageIds) {
        throw new UnsupportedOperationException("NOT_READY");
    }

    @Exclude
    public boolean isOpen() {
        throw new UnsupportedOperationException("NOT_READY");
    }
}
