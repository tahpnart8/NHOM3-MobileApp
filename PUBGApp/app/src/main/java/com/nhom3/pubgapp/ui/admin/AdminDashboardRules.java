package com.nhom3.pubgapp.ui.admin;

import com.nhom3.pubgapp.model.Complaint;
import com.nhom3.pubgapp.model.Report;
import com.nhom3.pubgapp.model.SellerVerification;
import com.nhom3.pubgapp.model.enums.ComplaintReason;
import com.nhom3.pubgapp.model.enums.ComplaintStatus;
import com.nhom3.pubgapp.model.enums.ReportStatus;
import com.nhom3.pubgapp.model.enums.ReportTarget;
import com.nhom3.pubgapp.model.enums.VerificationStatus;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Turns the loaded complaints, reports and verification requests into the "Cần xử lý" rows of E01.
public final class AdminDashboardRules {

    public static final int KIND_REPORT = 0;
    public static final int KIND_VERIFICATION = 1;
    public static final int KIND_COMPLAINT = 2;
    public static final int MAX_ROWS_PER_KIND = 5;

    private AdminDashboardRules() {
    }

    // One row of the task list. count is the number of reports, or the resubmit count of a request.
    // complaintReason is set only on a complaint row, so the row can say what the complaint is about.
    public static class TaskRow {

        private final int kind;
        private final ReportTarget targetType;
        private final String targetId;
        private final int count;
        private ComplaintReason complaintReason;

        public TaskRow(int kind, ReportTarget targetType, String targetId, int count) {
            this.kind = kind;
            this.targetType = targetType;
            this.targetId = targetId;
            this.count = count;
        }

        public ComplaintReason getComplaintReason() {
            return complaintReason;
        }

        public int getKind() {
            return kind;
        }

        public ReportTarget getTargetType() {
            return targetType;
        }

        public String getTargetId() {
            return targetId;
        }

        public int getCount() {
            return count;
        }
    }

    // Same order as the E01 mockup: new complaints, then reports, then verification requests.
    public static List<TaskRow> buildRows(List<Complaint> complaints, List<Report> reports,
            List<SellerVerification> verifications) {
        List<TaskRow> rows = new ArrayList<TaskRow>();
        rows.addAll(buildComplaintRows(complaints));
        rows.addAll(buildReportRows(reports));
        rows.addAll(buildVerificationRows(verifications));
        return rows;
    }

    // Only an OPEN complaint is new work; one waiting for evidence is waiting for the user, not the admin.
    static List<TaskRow> buildComplaintRows(List<Complaint> complaints) {
        List<TaskRow> rows = new ArrayList<TaskRow>();
        if (complaints == null) {
            return rows;
        }
        for (Complaint complaint : complaints) {
            if (rows.size() >= MAX_ROWS_PER_KIND) {
                break;
            }
            if (complaint.getStatus() != ComplaintStatus.OPEN) {
                continue;
            }
            TaskRow row = new TaskRow(KIND_COMPLAINT, null, complaint.getDealId(), 0);
            row.complaintReason = complaint.getReason();
            rows.add(row);
        }
        return rows;
    }

    // Open reports are grouped by object, so one object reported many times is one row.
    static List<TaskRow> buildReportRows(List<Report> reports) {
        List<TaskRow> rows = new ArrayList<TaskRow>();
        if (reports == null) {
            return rows;
        }
        Map<String, Integer> counts = new LinkedHashMap<String, Integer>();
        Map<String, Report> firstReports = new LinkedHashMap<String, Report>();
        for (Report report : reports) {
            if (report.getStatus() != ReportStatus.OPEN) {
                continue;
            }
            String key = report.getTargetType() + ":" + report.getTargetId();
            Integer previous = counts.get(key);
            int next = previous == null ? 1 : previous.intValue() + 1;
            counts.put(key, Integer.valueOf(next));
            if (!firstReports.containsKey(key)) {
                firstReports.put(key, report);
            }
        }
        for (Map.Entry<String, Report> entry : firstReports.entrySet()) {
            if (rows.size() >= MAX_ROWS_PER_KIND) {
                break;
            }
            Report first = entry.getValue();
            int count = counts.get(entry.getKey()).intValue();
            rows.add(new TaskRow(KIND_REPORT, first.getTargetType(), first.getTargetId(), count));
        }
        return rows;
    }

    static List<TaskRow> buildVerificationRows(List<SellerVerification> verifications) {
        List<TaskRow> rows = new ArrayList<TaskRow>();
        if (verifications == null) {
            return rows;
        }
        for (SellerVerification request : verifications) {
            if (rows.size() >= MAX_ROWS_PER_KIND) {
                break;
            }
            if (request.getStatus() != VerificationStatus.PENDING) {
                continue;
            }
            rows.add(new TaskRow(KIND_VERIFICATION, null, requesterId(request),
                    request.getResubmitCount()));
        }
        return rows;
    }

    // The document id of a request is the requester's uid, so it backs up a missing userId field.
    private static String requesterId(SellerVerification request) {
        String userId = request.getUserId();
        if (userId == null || userId.isEmpty()) {
            return request.getId();
        }
        return userId;
    }
}
