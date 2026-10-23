package com.nhom3.pubgapp.viewmodel;

import android.net.Uri;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.nhom3.pubgapp.data.DataCallback;
import com.nhom3.pubgapp.data.ReviewRepository;
import com.nhom3.pubgapp.model.Complaint;
import com.nhom3.pubgapp.model.Report;
import com.nhom3.pubgapp.model.Review;

import java.io.File;
import java.util.List;

// State for reviews, reports and complaints on the user side. Action bodies report NOT_READY until the repositories are filled.
public class TrustViewModel extends ViewModel {

    private final MutableLiveData<List<Review>> reviews = new MutableLiveData<>();
    private final MutableLiveData<List<Report>> reports = new MutableLiveData<>();
    private final MutableLiveData<List<Complaint>> complaints = new MutableLiveData<>();
    private final MutableLiveData<Complaint> selectedComplaint = new MutableLiveData<>();
    private final MutableLiveData<File> exportedFile = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> done = new MutableLiveData<>();

    static final int CRITERIA_ACCURACY = 0;
    static final int CRITERIA_ATTITUDE = 1;
    static final int CRITERIA_COMMITMENT = 2;
    private static final int CRITERIA_COUNT = 3;
    private static final double ONE_DECIMAL = 10.0;
    private static final String MESSAGE_NOT_SIGNED_IN = "Bạn cần đăng nhập để xem đánh giá.";

    private final ReviewRepository reviewRepository = new ReviewRepository();

    // Which list is in reviews ("uid:received"), so a rotation does not read it again.
    private String loadedReviewsKey;

    public LiveData<List<Review>> getReviews() {
        return reviews;
    }

    public LiveData<List<Report>> getReports() {
        return reports;
    }

    public LiveData<List<Complaint>> getComplaints() {
        return complaints;
    }

    public LiveData<Complaint> getSelectedComplaint() {
        return selectedComplaint;
    }

    public LiveData<File> getExportedFile() {
        return exportedFile;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<Boolean> getDone() {
        return done;
    }

    public void submitReview(Review review) {
        if (Boolean.TRUE.equals(loading.getValue())) {
            return;
        }
        loading.setValue(true);
        reviewRepository.create(review, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                loading.postValue(false);
                done.postValue(true);
            }

            @Override
            public void onError(String message) {
                loading.postValue(false);
                error.postValue(message);
            }
        });
    }

    public void loadReviews(String uid, boolean received) {
        if (uid == null || uid.isEmpty()) {
            error.setValue(MESSAGE_NOT_SIGNED_IN);
            return;
        }
        String key = uid + ":" + received;
        if (key.equals(loadedReviewsKey) && reviews.getValue() != null) {
            return;
        }
        loadedReviewsKey = key;
        reviews.setValue(null);
        loading.setValue(true);
        DataCallback<List<Review>> callback = new ReviewsCallback(key);
        if (received) {
            reviewRepository.getReceived(uid, callback);
        } else {
            reviewRepository.getGiven(uid, callback);
        }
    }

    public void submitReport(Report report, List<Uri> imageUris) {
        error.setValue(DataCallback.NOT_READY);
    }

    public void cancelReport(String id) {
        error.setValue(DataCallback.NOT_READY);
    }

    public void loadMyReports() {
        error.setValue(DataCallback.NOT_READY);
    }

    public void exportReports() {
        error.setValue(DataCallback.NOT_READY);
    }

    public void submitComplaint(Complaint complaint, List<Uri> evidenceUris) {
        error.setValue(DataCallback.NOT_READY);
    }

    public void addEvidence(String id, List<Uri> evidenceUris) {
        error.setValue(DataCallback.NOT_READY);
    }

    public void loadMyComplaints() {
        error.setValue(DataCallback.NOT_READY);
    }

    public void loadComplaint(String id) {
        error.setValue(DataCallback.NOT_READY);
    }

    public void withdrawComplaint(String id) {
        error.setValue(DataCallback.NOT_READY);
    }

    // Only the overall rating feeds ratingSum, so the three criteria are averaged here from the received reviews.
    static double[] averageCriteria(List<Review> received) {
        double[] averages = new double[CRITERIA_COUNT];
        if (received == null || received.isEmpty()) {
            return averages;
        }
        double accuracySum = 0;
        double attitudeSum = 0;
        double commitmentSum = 0;
        for (Review review : received) {
            accuracySum = accuracySum + review.getAccuracyRating();
            attitudeSum = attitudeSum + review.getAttitudeRating();
            commitmentSum = commitmentSum + review.getCommitmentRating();
        }
        int count = received.size();
        averages[CRITERIA_ACCURACY] = roundToOneDecimal(accuracySum / count);
        averages[CRITERIA_ATTITUDE] = roundToOneDecimal(attitudeSum / count);
        averages[CRITERIA_COMMITMENT] = roundToOneDecimal(commitmentSum / count);
        return averages;
    }

    private static double roundToOneDecimal(double value) {
        return Math.round(value * ONE_DECIMAL) / ONE_DECIMAL;
    }

    // A late answer for a list the screen no longer shows is dropped.
    private final class ReviewsCallback implements DataCallback<List<Review>> {
        private final String key;

        ReviewsCallback(String key) {
            this.key = key;
        }

        @Override
        public void onSuccess(List<Review> result) {
            if (!key.equals(loadedReviewsKey)) {
                return;
            }
            reviews.postValue(result);
            loading.postValue(false);
        }

        @Override
        public void onError(String message) {
            if (key.equals(loadedReviewsKey)) {
                loadedReviewsKey = null;
            }
            loading.postValue(false);
            error.postValue(message);
        }
    }

    public void clearError() {
        error.setValue(null);
    }

    public void clearDone() {
        done.setValue(false);
    }
}
