package com.nhom3.pubgapp.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.nhom3.pubgapp.model.enums.ReportStatus;

import org.junit.Test;

public class ReportTest {

    private Report reportWith(ReportStatus status) {
        Report report = new Report();
        report.setStatus(status);
        return report;
    }

    @Test
    public void resolve_open_becomesResolvedWithAdmin() {
        Report report = reportWith(ReportStatus.OPEN);
        report.resolve("admin_01");
        assertEquals(ReportStatus.RESOLVED, report.getStatus());
        assertEquals("admin_01", report.getHandledBy());
    }

    @Test(expected = IllegalStateException.class)
    public void resolve_cancelled_throws() {
        reportWith(ReportStatus.CANCELLED).resolve("admin_01");
    }

    @Test(expected = IllegalStateException.class)
    public void resolve_alreadyResolved_throws() {
        reportWith(ReportStatus.RESOLVED).resolve("admin_01");
    }

    @Test
    public void dismiss_open_becomesDismissed() {
        Report report = reportWith(ReportStatus.OPEN);
        report.dismiss("admin_01");
        assertEquals(ReportStatus.DISMISSED, report.getStatus());
        assertEquals("admin_01", report.getHandledBy());
    }

    @Test
    public void cancel_open_becomesCancelledWithoutAdmin() {
        Report report = reportWith(ReportStatus.OPEN);
        report.cancel();
        assertEquals(ReportStatus.CANCELLED, report.getStatus());
        assertNull(report.getHandledBy());
    }

    @Test(expected = IllegalStateException.class)
    public void cancel_dismissed_throws() {
        reportWith(ReportStatus.DISMISSED).cancel();
    }
}
