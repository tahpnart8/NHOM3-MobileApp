package com.nhom3.pubgapp.model;

import com.google.firebase.firestore.Exclude;
import com.nhom3.pubgapp.model.enums.ComplaintDecision;
import com.nhom3.pubgapp.model.enums.ComplaintReason;
import com.nhom3.pubgapp.model.enums.ComplaintStatus;
import com.nhom3.pubgapp.model.enums.Priority;

import java.util.ArrayList;
import java.util.List;

public class Complaint extends BaseDocument {
    // Images added after an evidence request, per request (firestore-schema.md, complaints).
    private static final int MAX_ADDED_EVIDENCE = 5;

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
        if (!isOpen()) {
            throw new IllegalStateException("Complaint is closed: " + status);
        }
        this.status = ComplaintStatus.WITHDRAWN;
    }

    // The repository writes the admin's text into evidenceRequest or resolutionMessage.
    public void decide(ComplaintDecision decision, String adminId) {
        if (decision == null) {
            throw new IllegalArgumentException("A decision is required");
        }
        if (decision == ComplaintDecision.REQUEST_EVIDENCE) {
            if (status != ComplaintStatus.OPEN) {
                throw new IllegalStateException("Evidence can only be requested on OPEN: " + status);
            }
            this.status = ComplaintStatus.NEEDS_EVIDENCE;
        } else {
            if (!isOpen()) {
                throw new IllegalStateException("Complaint is closed: " + status);
            }
            this.status = ComplaintStatus.RESOLVED;
        }
        this.decision = decision;
        this.handledBy = adminId;
    }

    public void addEvidence(List<String> imageIds) {
        if (status != ComplaintStatus.NEEDS_EVIDENCE) {
            throw new IllegalStateException("Complaint does not need evidence: " + status);
        }
        if (imageIds == null || imageIds.isEmpty() || imageIds.size() > MAX_ADDED_EVIDENCE) {
            throw new IllegalArgumentException("Add between 1 and " + MAX_ADDED_EVIDENCE + " images");
        }
        List<String> merged = new ArrayList<>();
        if (evidenceImageIds != null) {
            merged.addAll(evidenceImageIds);
        }
        merged.addAll(imageIds);
        this.evidenceImageIds = merged;
        this.status = ComplaintStatus.OPEN;
    }

    @Exclude
    public boolean isOpen() {
        return status == ComplaintStatus.OPEN || status == ComplaintStatus.NEEDS_EVIDENCE;
    }
}
