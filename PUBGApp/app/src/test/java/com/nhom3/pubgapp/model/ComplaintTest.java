package com.nhom3.pubgapp.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.nhom3.pubgapp.model.enums.ComplaintDecision;
import com.nhom3.pubgapp.model.enums.ComplaintStatus;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class ComplaintTest {

    private Complaint complaintWith(ComplaintStatus status) {
        Complaint complaint = new Complaint();
        complaint.setStatus(status);
        List<String> images = new ArrayList<>();
        images.add("image_01");
        complaint.setEvidenceImageIds(images);
        return complaint;
    }

    @Test
    public void isOpen_onlyForOpenAndNeedsEvidence() {
        assertTrue(complaintWith(ComplaintStatus.OPEN).isOpen());
        assertTrue(complaintWith(ComplaintStatus.NEEDS_EVIDENCE).isOpen());
        assertFalse(complaintWith(ComplaintStatus.RESOLVED).isOpen());
        assertFalse(complaintWith(ComplaintStatus.WITHDRAWN).isOpen());
    }

    @Test
    public void decide_requestEvidence_movesToNeedsEvidence() {
        Complaint complaint = complaintWith(ComplaintStatus.OPEN);
        complaint.decide(ComplaintDecision.REQUEST_EVIDENCE, "admin_01");
        assertEquals(ComplaintStatus.NEEDS_EVIDENCE, complaint.getStatus());
        assertEquals("admin_01", complaint.getHandledBy());
    }

    @Test
    public void decide_refundFromNeedsEvidence_resolves() {
        Complaint complaint = complaintWith(ComplaintStatus.NEEDS_EVIDENCE);
        complaint.decide(ComplaintDecision.REFUND, "admin_01");
        assertEquals(ComplaintStatus.RESOLVED, complaint.getStatus());
        assertEquals(ComplaintDecision.REFUND, complaint.getDecision());
    }

    @Test(expected = IllegalStateException.class)
    public void decide_requestEvidenceTwice_throws() {
        complaintWith(ComplaintStatus.NEEDS_EVIDENCE).decide(ComplaintDecision.REQUEST_EVIDENCE, "admin_01");
    }

    @Test
    public void addEvidence_appendsAndReopens() {
        Complaint complaint = complaintWith(ComplaintStatus.NEEDS_EVIDENCE);
        List<String> added = new ArrayList<>();
        added.add("image_02");
        complaint.addEvidence(added);
        assertEquals(2, complaint.getEvidenceImageIds().size());
        assertEquals("image_01", complaint.getEvidenceImageIds().get(0));
        assertEquals(ComplaintStatus.OPEN, complaint.getStatus());
    }

    @Test(expected = IllegalStateException.class)
    public void withdraw_resolved_throws() {
        complaintWith(ComplaintStatus.RESOLVED).withdraw();
    }
}
