package com.nhom3.pubgapp.ui.admin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.nhom3.pubgapp.model.Complaint;
import com.nhom3.pubgapp.model.Report;
import com.nhom3.pubgapp.model.SellerVerification;
import com.nhom3.pubgapp.model.enums.ComplaintReason;
import com.nhom3.pubgapp.model.enums.ComplaintStatus;
import com.nhom3.pubgapp.model.enums.ReportStatus;
import com.nhom3.pubgapp.model.enums.ReportTarget;
import com.nhom3.pubgapp.model.enums.VerificationStatus;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class AdminDashboardRulesTest {

    @Test
    public void nullListsGiveNoRows() {
        assertTrue(AdminDashboardRules.buildRows(null, null, null).isEmpty());
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
    public void rowsListComplaintsThenReportsThenVerifications() {
        List<Complaint> complaints = new ArrayList<Complaint>();
        complaints.add(complaint("D1", ComplaintStatus.OPEN));
        List<Report> reports = new ArrayList<Report>();
        reports.add(report(ReportTarget.MESSAGE, "M1", ReportStatus.OPEN));
        List<SellerVerification> requests = new ArrayList<SellerVerification>();
        requests.add(verification("U1", VerificationStatus.PENDING, 0));

        List<AdminDashboardRules.TaskRow> rows = AdminDashboardRules.buildRows(complaints, reports, requests);

        assertEquals(3, rows.size());
        assertEquals(AdminDashboardRules.KIND_COMPLAINT, rows.get(0).getKind());
        assertEquals(AdminDashboardRules.KIND_REPORT, rows.get(1).getKind());
        assertEquals(AdminDashboardRules.KIND_VERIFICATION, rows.get(2).getKind());
    }

    @Test
    public void onlyOpenComplaintsAreTasks() {
        List<Complaint> complaints = new ArrayList<Complaint>();
        complaints.add(complaint("D1", ComplaintStatus.NEEDS_EVIDENCE));
        complaints.add(complaint("D2", ComplaintStatus.OPEN));
        complaints.add(complaint("D3", ComplaintStatus.RESOLVED));
        complaints.add(complaint("D4", ComplaintStatus.WITHDRAWN));

        List<AdminDashboardRules.TaskRow> rows = AdminDashboardRules.buildComplaintRows(complaints);

        assertEquals(1, rows.size());
        assertEquals("D2", rows.get(0).getTargetId());
        assertEquals(ComplaintReason.DAMAGED, rows.get(0).getComplaintReason());
    }

    @Test
    public void complaintRowsAreCappedPerKind() {
        List<Complaint> complaints = new ArrayList<Complaint>();
        for (int index = 0; index < AdminDashboardRules.MAX_ROWS_PER_KIND + 2; index++) {
            complaints.add(complaint("D" + index, ComplaintStatus.OPEN));
        }

        assertEquals(AdminDashboardRules.MAX_ROWS_PER_KIND,
                AdminDashboardRules.buildComplaintRows(complaints).size());
    }

    private Complaint complaint(String dealId, ComplaintStatus status) {
        Complaint complaint = new Complaint();
        complaint.setDealId(dealId);
        complaint.setStatus(status);
        complaint.setReason(ComplaintReason.DAMAGED);
        return complaint;
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
