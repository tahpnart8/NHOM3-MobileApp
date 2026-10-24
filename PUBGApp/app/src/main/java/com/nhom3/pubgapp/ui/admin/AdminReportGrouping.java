package com.nhom3.pubgapp.ui.admin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.Timestamp;
import com.nhom3.pubgapp.model.Report;
import com.nhom3.pubgapp.model.enums.ReportReason;
import com.nhom3.pubgapp.model.enums.ReportStatus;
import com.nhom3.pubgapp.model.enums.ReportTarget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Folds the reports of one target into one row for E04 and orders the rows, most reported first.
public final class AdminReportGrouping {

    private static final String KEY_SEPARATOR = ":";

    private AdminReportGrouping() {
    }

    public static final class Group {
        private final ReportTarget targetType;
        private final String targetId;
        private final ReportStatus status;
        private int reportCount;
        private final List<ReportReason> reasons = new ArrayList<ReportReason>();
        private Timestamp latestAt;
        private String latestDescription;
        private int evidenceCount;

        Group(ReportTarget targetType, String targetId, ReportStatus status) {
            this.targetType = targetType;
            this.targetId = targetId;
            this.status = status;
        }

        public ReportTarget getTargetType() {
            return targetType;
        }

        public String getTargetId() {
            return targetId;
        }

        public ReportStatus getStatus() {
            return status;
        }

        public int getReportCount() {
            return reportCount;
        }

        public List<ReportReason> getReasons() {
            return reasons;
        }

        @Nullable
        public Timestamp getLatestAt() {
            return latestAt;
        }

        @Nullable
        public String getLatestDescription() {
            return latestDescription;
        }

        public int getEvidenceCount() {
            return evidenceCount;
        }

        void add(Report report) {
            reportCount++;
            ReportReason reason = report.getReason();
            if (reason != null && !reasons.contains(reason)) {
                reasons.add(reason);
            }
            List<String> evidence = report.getEvidenceImageIds();
            if (evidence != null) {
                evidenceCount += evidence.size();
            }
            Timestamp createdAt = report.getCreatedAt();
            if (isNewer(createdAt, latestAt)) {
                latestAt = createdAt;
                latestDescription = report.getDescription();
            }
        }
    }

    // The key holds the target type too, so a user and a listing with the same id never merge.
    @NonNull
    public static List<Group> group(@Nullable List<Report> reports) {
        List<Group> result = new ArrayList<Group>();
        if (reports == null) {
            return result;
        }
        Map<String, Group> byKey = new HashMap<String, Group>();
        for (Report report : reports) {
            String key = keyOf(report);
            Group group = byKey.get(key);
            if (group == null) {
                group = new Group(report.getTargetType(), report.getTargetId(), report.getStatus());
                byKey.put(key, group);
                result.add(group);
            }
            group.add(report);
        }
        Collections.sort(result, new Comparator<Group>() {
            @Override
            public int compare(Group first, Group second) {
                if (first.reportCount != second.reportCount) {
                    return second.reportCount - first.reportCount;
                }
                return compareNewestFirst(first.latestAt, second.latestAt);
            }
        });
        return result;
    }

    private static String keyOf(Report report) {
        String type = report.getTargetType() == null ? "" : report.getTargetType().name();
        return type + KEY_SEPARATOR + report.getTargetId();
    }

    private static boolean isNewer(@Nullable Timestamp candidate, @Nullable Timestamp current) {
        if (candidate == null) {
            return false;
        }
        return current == null || candidate.compareTo(current) > 0;
    }

    // A missing time counts as the oldest.
    private static int compareNewestFirst(@Nullable Timestamp first, @Nullable Timestamp second) {
        if (first == null && second == null) {
            return 0;
        }
        if (first == null) {
            return 1;
        }
        if (second == null) {
            return -1;
        }
        return second.compareTo(first);
    }
}
