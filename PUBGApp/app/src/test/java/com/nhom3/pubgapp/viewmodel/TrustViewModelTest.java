package com.nhom3.pubgapp.viewmodel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.firebase.Timestamp;
import com.nhom3.pubgapp.model.Complaint;
import com.nhom3.pubgapp.model.Deal;
import com.nhom3.pubgapp.model.Report;
import com.nhom3.pubgapp.model.Review;
import com.nhom3.pubgapp.model.enums.ComplaintReason;
import com.nhom3.pubgapp.model.enums.ComplaintStatus;
import com.nhom3.pubgapp.model.enums.DealStatus;
import com.nhom3.pubgapp.model.enums.DeliveryMethod;
import com.nhom3.pubgapp.model.enums.ReportReason;
import com.nhom3.pubgapp.model.enums.ReportStatus;
import com.nhom3.pubgapp.model.enums.ReportTarget;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class TrustViewModelTest {

    private static final double DELTA = 0.0001;
    private static final String ME = "user-me";
    private static final String OTHER = "user-other";
    private static final long ONE_DAY_SECONDS = 24L * 60 * 60;
    private static final Timestamp NOW = new Timestamp(1_800_000_000L, 0);

    @Test
    public void emptyListGivesZeroForAllCriteria() {
        double[] averages = TrustViewModel.averageCriteria(new ArrayList<Review>());
        assertEquals(0.0, averages[TrustViewModel.CRITERIA_ACCURACY], DELTA);
        assertEquals(0.0, averages[TrustViewModel.CRITERIA_ATTITUDE], DELTA);
        assertEquals(0.0, averages[TrustViewModel.CRITERIA_COMMITMENT], DELTA);
    }

    @Test
    public void oneReviewGivesItsOwnScores() {
        List<Review> reviews = new ArrayList<Review>();
        reviews.add(review(5, 4, 3));
        double[] averages = TrustViewModel.averageCriteria(reviews);
        assertEquals(5.0, averages[TrustViewModel.CRITERIA_ACCURACY], DELTA);
        assertEquals(4.0, averages[TrustViewModel.CRITERIA_ATTITUDE], DELTA);
        assertEquals(3.0, averages[TrustViewModel.CRITERIA_COMMITMENT], DELTA);
    }

    @Test
    public void manyReviewsAreRoundedToOneDecimal() {
        List<Review> reviews = new ArrayList<Review>();
        reviews.add(review(5, 5, 4));
        reviews.add(review(4, 5, 4));
        reviews.add(review(4, 3, 5));
        double[] averages = TrustViewModel.averageCriteria(reviews);
        assertEquals(4.3, averages[TrustViewModel.CRITERIA_ACCURACY], DELTA);
        assertEquals(4.3, averages[TrustViewModel.CRITERIA_ATTITUDE], DELTA);
        assertEquals(4.3, averages[TrustViewModel.CRITERIA_COMMITMENT], DELTA);
    }

    @Test
    public void reportWithoutReasonIsRefused() {
        Report report = report(ReportTarget.LISTING, "listing-1", null);
        assertEquals(TrustViewModel.REPORT_MISSING_REASON, TrustViewModel.checkReport(report, 0, ME));
    }

    @Test
    public void reportWithReasonIsAccepted() {
        Report report = report(ReportTarget.LISTING, "listing-1", ReportReason.SCAM);
        assertEquals(TrustViewModel.REPORT_OK, TrustViewModel.checkReport(report, 0, ME));
    }

    @Test
    public void reportingYourselfIsRefused() {
        Report report = report(ReportTarget.USER, ME, ReportReason.OTHER);
        assertEquals(TrustViewModel.REPORT_SELF, TrustViewModel.checkReport(report, 0, ME));
    }

    @Test
    public void reportingAnotherUserIsAccepted() {
        Report report = report(ReportTarget.USER, OTHER, ReportReason.OTHER);
        assertEquals(TrustViewModel.REPORT_OK, TrustViewModel.checkReport(report, 0, ME));
    }

    @Test
    public void threeImagesAreAcceptedAndFourAreRefused() {
        Report report = report(ReportTarget.LISTING, "listing-1", ReportReason.FAKE_ITEM);
        assertEquals(TrustViewModel.REPORT_OK, TrustViewModel.checkReport(report, 3, ME));
        assertEquals(TrustViewModel.REPORT_TOO_MANY_IMAGES, TrustViewModel.checkReport(report, 4, ME));
    }

    @Test
    public void messageReportNeedsConversation() {
        Report report = report(ReportTarget.MESSAGE, "message-1", ReportReason.INAPPROPRIATE);
        assertEquals(TrustViewModel.REPORT_MISSING_CONVERSATION, TrustViewModel.checkReport(report, 0, ME));
        report.setConversationId("conversation-1");
        assertEquals(TrustViewModel.REPORT_OK, TrustViewModel.checkReport(report, 0, ME));
    }

    @Test
    public void ownOpenReportMayBeCancelled() {
        Report report = sentReport(ME, ReportStatus.OPEN);
        assertTrue(TrustViewModel.canCancelReport(report, ME));
    }

    @Test
    public void handledReportsMayNotBeCancelled() {
        assertFalse(TrustViewModel.canCancelReport(sentReport(ME, ReportStatus.RESOLVED), ME));
        assertFalse(TrustViewModel.canCancelReport(sentReport(ME, ReportStatus.DISMISSED), ME));
        assertFalse(TrustViewModel.canCancelReport(sentReport(ME, ReportStatus.CANCELLED), ME));
    }

    @Test
    public void someoneElsesReportMayNotBeCancelled() {
        Report report = sentReport(OTHER, ReportStatus.OPEN);
        assertFalse(TrustViewModel.canCancelReport(report, ME));
    }

    @Test
    public void missingReportOrUserMayNotCancel() {
        assertFalse(TrustViewModel.canCancelReport(null, ME));
        assertFalse(TrustViewModel.canCancelReport(sentReport(ME, ReportStatus.OPEN), null));
    }

    private static Report sentReport(String reporterId, ReportStatus status) {
        Report report = report(ReportTarget.LISTING, "listing-1", ReportReason.SCAM);
        report.setReporterId(reporterId);
        report.setStatus(status);
        return report;
    }

    @Test
    public void complaintNeedsReasonAndOneToFiveImages() {
        Complaint complaint = new Complaint();
        assertEquals(TrustViewModel.COMPLAINT_MISSING_REASON, TrustViewModel.checkComplaintForm(complaint, 1));
        complaint.setReason(ComplaintReason.DAMAGED);
        assertEquals(TrustViewModel.COMPLAINT_NO_IMAGE, TrustViewModel.checkComplaintForm(complaint, 0));
        assertEquals(TrustViewModel.COMPLAINT_OK, TrustViewModel.checkComplaintForm(complaint, 1));
        assertEquals(TrustViewModel.COMPLAINT_OK, TrustViewModel.checkComplaintForm(complaint, 5));
        assertEquals(TrustViewModel.COMPLAINT_TOO_MANY_IMAGES, TrustViewModel.checkComplaintForm(complaint, 6));
    }

    @Test
    public void shippedDealBeforeCountdownEndsMayBeDisputed() {
        Deal deal = deal(DealStatus.SHIPPED, DeliveryMethod.SHIPPING, later());
        assertEquals(TrustViewModel.COMPLAINT_OK, TrustViewModel.checkComplaintDeal(deal, ME, NOW));
    }

    @Test
    public void shippedDealAfterCountdownMayNotBeDisputed() {
        Deal deal = deal(DealStatus.SHIPPED, DeliveryMethod.SHIPPING, earlier());
        assertEquals(TrustViewModel.COMPLAINT_COUNTDOWN_OVER, TrustViewModel.checkComplaintDeal(deal, ME, NOW));
    }

    @Test
    public void confirmedDealOnlyWhenMeetingInPerson() {
        Deal meetUp = deal(DealStatus.CONFIRMED, DeliveryMethod.MEET_UP, null);
        Deal shipping = deal(DealStatus.CONFIRMED, DeliveryMethod.SHIPPING, null);
        assertEquals(TrustViewModel.COMPLAINT_OK, TrustViewModel.checkComplaintDeal(meetUp, ME, NOW));
        assertEquals(TrustViewModel.COMPLAINT_WRONG_STATUS, TrustViewModel.checkComplaintDeal(shipping, ME, NOW));
    }

    @Test
    public void receivedOrDisputedDealMayNotBeDisputed() {
        Deal received = deal(DealStatus.RECEIVED, DeliveryMethod.SHIPPING, later());
        Deal disputed = deal(DealStatus.DISPUTED, DeliveryMethod.SHIPPING, later());
        assertEquals(TrustViewModel.COMPLAINT_WRONG_STATUS, TrustViewModel.checkComplaintDeal(received, ME, NOW));
        assertEquals(TrustViewModel.COMPLAINT_WRONG_STATUS, TrustViewModel.checkComplaintDeal(disputed, ME, NOW));
    }

    @Test
    public void onlyAPartyMayComplain() {
        Deal deal = deal(DealStatus.SHIPPED, DeliveryMethod.SHIPPING, later());
        assertEquals(TrustViewModel.COMPLAINT_NOT_PARTY, TrustViewModel.checkComplaintDeal(deal, "stranger", NOW));
        assertEquals(TrustViewModel.COMPLAINT_OK, TrustViewModel.checkComplaintDeal(deal, OTHER, NOW));
        assertEquals(TrustViewModel.COMPLAINT_NO_DEAL, TrustViewModel.checkComplaintDeal(null, ME, NOW));
    }

    @Test
    public void ownWaitingComplaintMayBeWithdrawn() {
        assertTrue(TrustViewModel.canWithdrawComplaint(sentComplaint(ME, ComplaintStatus.OPEN), ME));
        assertTrue(TrustViewModel.canWithdrawComplaint(sentComplaint(ME, ComplaintStatus.NEEDS_EVIDENCE), ME));
    }

    @Test
    public void closedComplaintsMayNotBeWithdrawn() {
        assertFalse(TrustViewModel.canWithdrawComplaint(sentComplaint(ME, ComplaintStatus.RESOLVED), ME));
        assertFalse(TrustViewModel.canWithdrawComplaint(sentComplaint(ME, ComplaintStatus.WITHDRAWN), ME));
    }

    @Test
    public void someoneElsesComplaintMayNotBeWithdrawn() {
        assertFalse(TrustViewModel.canWithdrawComplaint(sentComplaint(OTHER, ComplaintStatus.OPEN), ME));
    }

    @Test
    public void missingComplaintOrUserMayNotWithdraw() {
        assertFalse(TrustViewModel.canWithdrawComplaint(null, ME));
        assertFalse(TrustViewModel.canWithdrawComplaint(sentComplaint(ME, ComplaintStatus.OPEN), null));
        assertFalse(TrustViewModel.canWithdrawComplaint(sentComplaint(ME, ComplaintStatus.OPEN), ""));
    }

    private static Complaint sentComplaint(String complainantId, ComplaintStatus status) {
        Complaint complaint = new Complaint();
        complaint.setComplainantId(complainantId);
        complaint.setStatus(status);
        return complaint;
    }

    private static Deal deal(DealStatus status, DeliveryMethod method, Timestamp autoReleaseAt) {
        Deal deal = new Deal();
        deal.setBuyerId(ME);
        deal.setSellerId(OTHER);
        deal.setStatus(status);
        deal.setDeliveryMethod(method);
        deal.setAutoReleaseAt(autoReleaseAt);
        return deal;
    }

    private static Timestamp later() {
        return new Timestamp(NOW.getSeconds() + ONE_DAY_SECONDS, 0);
    }

    private static Timestamp earlier() {
        return new Timestamp(NOW.getSeconds() - ONE_DAY_SECONDS, 0);
    }

    private static Report report(ReportTarget target, String targetId, ReportReason reason) {
        Report report = new Report();
        report.setTargetType(target);
        report.setTargetId(targetId);
        report.setReason(reason);
        return report;
    }

    private static Review review(int accuracy, int attitude, int commitment) {
        Review review = new Review();
        review.setAccuracyRating(accuracy);
        review.setAttitudeRating(attitude);
        review.setCommitmentRating(commitment);
        return review;
    }
}
