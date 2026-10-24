package com.nhom3.pubgapp.ui.admin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.firebase.Timestamp;
import com.nhom3.pubgapp.model.Report;
import com.nhom3.pubgapp.model.enums.ReportReason;
import com.nhom3.pubgapp.model.enums.ReportStatus;
import com.nhom3.pubgapp.model.enums.ReportTarget;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AdminReportGroupingTest {

    private Report report(ReportTarget type, String targetId, ReportReason reason, long seconds) {
        Report report = new Report();
        report.setTargetType(type);
        report.setTargetId(targetId);
        report.setReason(reason);
        report.setStatus(ReportStatus.OPEN);
        report.setCreatedAt(new Timestamp(seconds, 0));
        return report;
    }

    @Test
    public void group_emptyList_returnsEmpty() {
        assertTrue(AdminReportGrouping.group(new ArrayList<Report>()).isEmpty());
    }

    @Test
    public void group_null_returnsEmpty() {
        assertTrue(AdminReportGrouping.group(null).isEmpty());
    }

    @Test
    public void group_threeReportsOfOneListing_countsAndDeduplicatesReasons() {
        List<Report> reports = Arrays.asList(
                report(ReportTarget.LISTING, "l1", ReportReason.SCAM, 10),
                report(ReportTarget.LISTING, "l1", ReportReason.SCAM, 30),
                report(ReportTarget.LISTING, "l1", ReportReason.FAKE_ITEM, 20));

        List<AdminReportGrouping.Group> groups = AdminReportGrouping.group(reports);

        assertEquals(1, groups.size());
        AdminReportGrouping.Group group = groups.get(0);
        assertEquals(3, group.getReportCount());
        assertEquals(2, group.getReasons().size());
        assertEquals(new Timestamp(30, 0), group.getLatestAt());
    }

    @Test
    public void group_moreReportedListingComesFirst() {
        List<Report> reports = Arrays.asList(
                report(ReportTarget.LISTING, "few", ReportReason.OTHER, 100),
                report(ReportTarget.LISTING, "many", ReportReason.OTHER, 1),
                report(ReportTarget.LISTING, "many", ReportReason.OTHER, 2));

        List<AdminReportGrouping.Group> groups = AdminReportGrouping.group(reports);

        assertEquals("many", groups.get(0).getTargetId());
        assertEquals("few", groups.get(1).getTargetId());
    }

    @Test
    public void group_sameCount_newerReportComesFirst() {
        List<Report> reports = Arrays.asList(
                report(ReportTarget.LISTING, "older", ReportReason.OTHER, 5),
                report(ReportTarget.LISTING, "newer", ReportReason.OTHER, 50));

        List<AdminReportGrouping.Group> groups = AdminReportGrouping.group(reports);

        assertEquals("newer", groups.get(0).getTargetId());
        assertEquals("older", groups.get(1).getTargetId());
    }

    @Test
    public void group_userAndListingWithSameId_stayApart() {
        List<Report> reports = Arrays.asList(
                report(ReportTarget.USER, "42", ReportReason.SCAM, 1),
                report(ReportTarget.LISTING, "42", ReportReason.SCAM, 2));

        assertEquals(2, AdminReportGrouping.group(reports).size());
    }

    @Test
    public void group_countsEvidenceAndKeepsNewestDescription() {
        Report first = report(ReportTarget.LISTING, "l1", ReportReason.SCAM, 1);
        first.setEvidenceImageIds(Arrays.asList("a", "b"));
        first.setDescription("old");
        Report second = report(ReportTarget.LISTING, "l1", ReportReason.SCAM, 9);
        second.setDescription("new");

        AdminReportGrouping.Group group = AdminReportGrouping.group(Arrays.asList(first, second)).get(0);

        assertEquals(2, group.getEvidenceCount());
        assertEquals("new", group.getLatestDescription());
    }

    @Test
    public void group_reportWithoutTime_sortsLastAndKeepsNullTime() {
        Report timeless = report(ReportTarget.LISTING, "none", ReportReason.OTHER, 1);
        timeless.setCreatedAt(null);
        Report timed = report(ReportTarget.LISTING, "timed", ReportReason.OTHER, 1);

        List<AdminReportGrouping.Group> groups = AdminReportGrouping.group(Arrays.asList(timeless, timed));

        assertEquals("timed", groups.get(0).getTargetId());
        assertNull(groups.get(1).getLatestAt());
    }
}
