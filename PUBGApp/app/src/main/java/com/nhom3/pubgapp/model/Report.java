package com.nhom3.pubgapp.model;

import com.nhom3.pubgapp.model.enums.ReportReason;
import com.nhom3.pubgapp.model.enums.ReportStatus;
import com.nhom3.pubgapp.model.enums.ReportTarget;

import java.util.List;

public class Report extends BaseDocument {
    private String reporterId;
    private ReportTarget targetType;
    private String targetId;
    private ReportReason reason;
    private String description;
    private ReportStatus status;
    private List<String> evidenceImageIds;
    private String conversationId;
    private String handledBy;

    public Report() {
    }

    public String getReporterId() {
        return reporterId;
    }

    public void setReporterId(String reporterId) {
        this.reporterId = reporterId;
    }

    public ReportTarget getTargetType() {
        return targetType;
    }

    public void setTargetType(ReportTarget targetType) {
        this.targetType = targetType;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public ReportReason getReason() {
        return reason;
    }

    public void setReason(ReportReason reason) {
        this.reason = reason;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ReportStatus getStatus() {
        return status;
    }

    public void setStatus(ReportStatus status) {
        this.status = status;
    }

    public List<String> getEvidenceImageIds() {
        return evidenceImageIds;
    }

    public void setEvidenceImageIds(List<String> evidenceImageIds) {
        this.evidenceImageIds = evidenceImageIds;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getHandledBy() {
        return handledBy;
    }

    public void setHandledBy(String handledBy) {
        this.handledBy = handledBy;
    }

    public void cancel() {
        requireOpen();
        this.status = ReportStatus.CANCELLED;
    }

    public void resolve(String adminId) {
        requireOpen();
        this.status = ReportStatus.RESOLVED;
        this.handledBy = adminId;
    }

    public void dismiss(String adminId) {
        requireOpen();
        this.status = ReportStatus.DISMISSED;
        this.handledBy = adminId;
    }

    // Every report transition starts from OPEN (trang-thai.md section 5).
    private void requireOpen() {
        if (status != ReportStatus.OPEN) {
            throw new IllegalStateException("Report is not OPEN: " + status);
        }
    }
}
