package com.nhom3.pubgapp.viewmodel;

import static org.junit.Assert.assertEquals;

import com.nhom3.pubgapp.model.Report;
import com.nhom3.pubgapp.model.Review;
import com.nhom3.pubgapp.model.enums.ReportReason;
import com.nhom3.pubgapp.model.enums.ReportTarget;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class TrustViewModelTest {

    private static final double DELTA = 0.0001;
    private static final String ME = "user-me";
    private static final String OTHER = "user-other";

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
