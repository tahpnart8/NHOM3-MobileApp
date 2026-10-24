package com.nhom3.pubgapp.ui.admin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.nhom3.pubgapp.model.Report;
import com.nhom3.pubgapp.model.SellerVerification;
import com.nhom3.pubgapp.model.enums.ReportStatus;
import com.nhom3.pubgapp.model.enums.ReportTarget;
import com.nhom3.pubgapp.model.enums.VerificationStatus;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class AdminDashboardRulesTest {

    @Test
    public void nullListsGiveNoRows() {
        assertTrue(AdminDashboardRules.buildRows(null, null).isEmpty());
    }

    @Test
    public void reportsOnSameObjectBecomeOneRowWithCount() {
        List<Report> reports = new ArrayList<Report>();
        reports.add(report(ReportTarget.LISTING, "L1", ReportStatus.OPEN));
        reports.add(report(ReportTarget.LISTING, "L2", ReportStatus.OPEN));
        reports.add(report(ReportTarget.LISTING, "L1", ReportStatus.OPEN));

        List<AdminDashboardRules.TaskRow> rows = AdminDashboardRules.buildReportRows(reports);

        assertEquals(2, rows.size());
        assertEquals("L1", rows.get(0).getTargetId());
        assertEquals(2, rows.get(0).getCount());
        assertEquals("L2", rows.get(1).getTargetId());
        assertEquals(1, rows.get(1).getCount());
    }

    @Test
    public void sameIdOfDifferentTargetTypesStaysSeparate() {
        List<Report> reports = new ArrayList<Report>();
        reports.add(report(ReportTarget.LISTING, "X", ReportStatus.OPEN));
        reports.add(report(ReportTarget.USER, "X", ReportStatus.OPEN));

        assertEquals(2, AdminDashboardRules.buildReportRows(reports).size());
    }

    @Test
    public void closedReportsAreNotTasks() {
        List<Report> reports = new ArrayList<Report>();
        reports.add(report(ReportTarget.USER, "U1", ReportStatus.RESOLVED));
        reports.add(report(ReportTarget.USER, "U2", ReportStatus.DISMISSED));
        reports.add(report(ReportTarget.USER, "U3", ReportStatus.OPEN));

        List<AdminDashboardRules.TaskRow> rows = AdminDashboardRules.buildReportRows(reports);

        assertEquals(1, rows.size());
        assertEquals("U3", rows.get(0).getTargetId());
    }

    @Test
    public void reportRowsAreCappedPerKind() {
        List<Report> reports = new ArrayList<Report>();
        for (int index = 0; index < AdminDashboardRules.MAX_ROWS_PER_KIND + 3; index++) {
            reports.add(report(ReportTarget.LISTING, "L" + index, ReportStatus.OPEN));
        }

        assertEquals(AdminDashboardRules.MAX_ROWS_PER_KIND,
                AdminDashboardRules.buildReportRows(reports).size());
    }

    @Test
    public void onlyPendingVerificationsAreTasks() {
        List<SellerVerification> requests = new ArrayList<SellerVerification>();
        requests.add(verification("U1", VerificationStatus.PENDING, 2));
        requests.add(verification("U2", VerificationStatus.APPROVED, 0));
        requests.add(verification("U3", VerificationStatus.REJECTED, 1));

        List<AdminDashboardRules.TaskRow> rows = AdminDashboardRules.buildVerificationRows(requests);

        assertEquals(1, rows.size());
        assertEquals(AdminDashboardRules.KIND_VERIFICATION, rows.get(0).getKind());
        assertEquals("U1", rows.get(0).getTargetId());
        assertEquals(2, rows.get(0).getCount());
    }

    @Test
    public void verificationFallsBackToDocumentIdWhenUserIdIsMissing() {
        SellerVerification request = verification(null, VerificationStatus.PENDING, 0);
        request.setId("U9");
        List<SellerVerification> requests = new ArrayList<SellerVerification>();
        requests.add(request);

        assertEquals("U9", AdminDashboardRules.buildVerificationRows(requests).get(0).getTargetId());
    }

    @Test
    public void rowsListReportsBeforeVerifications() {
        List<Report> reports = new ArrayList<Report>();
        reports.add(report(ReportTarget.MESSAGE, "M1", ReportStatus.OPEN));
        List<SellerVerification> requests = new ArrayList<SellerVerification>();
        requests.add(verification("U1", VerificationStatus.PENDING, 0));

        List<AdminDashboardRules.TaskRow> rows = AdminDashboardRules.buildRows(reports, requests);

        assertEquals(2, rows.size());
        assertEquals(AdminDashboardRules.KIND_REPORT, rows.get(0).getKind());
        assertEquals(AdminDashboardRules.KIND_VERIFICATION, rows.get(1).getKind());
    }

    private Report report(ReportTarget target, String targetId, ReportStatus status) {
        Report report = new Report();
        report.setTargetType(target);
        report.setTargetId(targetId);
        report.setStatus(status);
        return report;
    }

    private SellerVerification verification(String userId, VerificationStatus status, int resubmits) {
        SellerVerification request = new SellerVerification();
        request.setUserId(userId);
        request.setStatus(status);
        request.setResubmitCount(resubmits);
        return request;
    }
}
