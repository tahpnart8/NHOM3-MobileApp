package com.nhom3.pubgapp.viewmodel;

import static org.junit.Assert.assertEquals;

import com.nhom3.pubgapp.model.Review;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class TrustViewModelTest {

    private static final double DELTA = 0.0001;

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

    private static Review review(int accuracy, int attitude, int commitment) {
        Review review = new Review();
        review.setAccuracyRating(accuracy);
        review.setAttitudeRating(attitude);
        review.setCommitmentRating(commitment);
        return review;
    }
}
