package com.nhom3.pubgapp.ui.admin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.nhom3.pubgapp.model.ChatMessage;
import com.nhom3.pubgapp.model.Report;
import com.nhom3.pubgapp.model.enums.ReportTarget;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AdminReportEvidenceTest {

    private Report report(ReportTarget type, String targetId, List<String> imageIds, String conversationId) {
        Report report = new Report();
        report.setTargetType(type);
        report.setTargetId(targetId);
        report.setEvidenceImageIds(imageIds);
        report.setConversationId(conversationId);
        return report;
    }

    private List<ChatMessage> chat(int count) {
        List<ChatMessage> messages = new ArrayList<ChatMessage>();
        for (int index = 0; index < count; index++) {
            ChatMessage message = new ChatMessage();
            message.setId("m" + index);
            messages.add(message);
        }
        return messages;
    }

    @Test
    public void evidenceOf_joinsImagesOfTheSameTargetOnly() {
        List<Report> reports = new ArrayList<Report>();
        reports.add(report(ReportTarget.LISTING, "l1", Arrays.asList("a", "b"), null));
        reports.add(report(ReportTarget.LISTING, "l1", Arrays.asList("b", "c"), null));
        reports.add(report(ReportTarget.LISTING, "l2", Arrays.asList("x"), null));
        reports.add(report(ReportTarget.USER, "l1", Arrays.asList("y"), null));

        Report combined = AdminModerationFragment.evidenceOf(reports, ReportTarget.LISTING, "l1");

        assertEquals(Arrays.asList("a", "b", "c"), combined.getEvidenceImageIds());
        assertNull(combined.getConversationId());
    }

    @Test
    public void evidenceOf_takesTheFirstConversationNamed() {
        List<Report> reports = new ArrayList<Report>();
        reports.add(report(ReportTarget.MESSAGE, "m1", null, ""));
        reports.add(report(ReportTarget.MESSAGE, "m1", null, "c1"));
        reports.add(report(ReportTarget.MESSAGE, "m1", null, "c2"));

        Report combined = AdminModerationFragment.evidenceOf(reports, ReportTarget.MESSAGE, "m1");

        assertEquals("c1", combined.getConversationId());
        assertTrue(combined.getEvidenceImageIds().isEmpty());
    }

    @Test
    public void evidenceOf_noReports_givesNoEvidence() {
        Report combined = AdminModerationFragment.evidenceOf(null, ReportTarget.LISTING, "l1");
        assertTrue(combined.getEvidenceImageIds().isEmpty());
        assertNull(combined.getConversationId());
    }

    @Test
    public void messagesAround_keepsTheReportedMessageInTheMiddle() {
        List<ChatMessage> around = AdminModerationFragment.messagesAround(chat(20), "m10", 2);
        assertEquals(5, around.size());
        assertEquals("m8", around.get(0).getId());
        assertEquals("m12", around.get(4).getId());
    }

    @Test
    public void messagesAround_stopsAtTheEdges() {
        List<ChatMessage> around = AdminModerationFragment.messagesAround(chat(4), "m0", 2);
        assertEquals(3, around.size());
        assertEquals("m0", around.get(0).getId());
    }

    @Test
    public void messagesAround_unknownMessage_givesTheLastOnes() {
        List<ChatMessage> around = AdminModerationFragment.messagesAround(chat(10), "gone", 2);
        assertEquals(3, around.size());
        assertEquals("m7", around.get(0).getId());
        assertEquals("m9", around.get(2).getId());
    }

    @Test
    public void messagesAround_emptyChat_givesNothing() {
        assertTrue(AdminModerationFragment.messagesAround(null, "m1", 2).isEmpty());
        assertTrue(AdminModerationFragment.messagesAround(new ArrayList<ChatMessage>(), "m1", 2).isEmpty());
    }
}
