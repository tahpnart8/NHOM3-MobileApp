package com.nhom3.pubgapp.viewmodel;

import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.nhom3.pubgapp.data.DataCallback;
import com.nhom3.pubgapp.data.MediaRepository;
import com.nhom3.pubgapp.data.ReportRepository;
import com.nhom3.pubgapp.data.ReviewRepository;
import com.nhom3.pubgapp.model.Complaint;
import com.nhom3.pubgapp.model.Report;
import com.nhom3.pubgapp.model.Review;
import com.nhom3.pubgapp.model.enums.ImagePurpose;
import com.nhom3.pubgapp.model.enums.ReportStatus;
import com.nhom3.pubgapp.model.enums.ReportTarget;
import com.nhom3.pubgapp.util.NetworkMonitor;
import com.nhom3.pubgapp.util.SessionManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    public static final int CRITERIA_ACCURACY = 0;
    public static final int CRITERIA_ATTITUDE = 1;
    public static final int CRITERIA_COMMITMENT = 2;
    private static final int CRITERIA_COUNT = 3;
    private static final double ONE_DECIMAL = 10.0;
    private static final String MESSAGE_NOT_SIGNED_IN = "Bạn cần đăng nhập để xem đánh giá.";

    static final int MAX_REPORT_IMAGES = 3;
    static final int REPORT_OK = 0;
    static final int REPORT_MISSING_REASON = 1;
    static final int REPORT_SELF = 2;
    static final int REPORT_TOO_MANY_IMAGES = 3;
    static final int REPORT_MISSING_CONVERSATION = 4;
    private static final String MESSAGE_REPORT_NOT_SIGNED_IN = "Bạn cần đăng nhập để gửi báo cáo.";
    private static final String MESSAGE_MISSING_REASON = "Hãy chọn một lý do báo cáo.";
    private static final String MESSAGE_REPORT_SELF = "Bạn không thể báo cáo chính mình.";
    private static final String MESSAGE_TOO_MANY_IMAGES = "Chỉ đính kèm được tối đa 3 ảnh.";
    private static final String MESSAGE_MISSING_CONVERSATION =
            "Thiếu cuộc trò chuyện của tin nhắn bị báo cáo.";
    private static final String MESSAGE_OFFLINE = "Không có kết nối.";
    private static final String MESSAGE_MY_REPORTS_NOT_SIGNED_IN =
            "Bạn cần đăng nhập để xem báo cáo của mình.";
    private static final String MESSAGE_CANNOT_CANCEL_REPORT =
            "Chỉ hủy được báo cáo của bạn khi báo cáo còn chờ xử lý.";
    private static final String MESSAGE_NOTHING_TO_EXPORT = "Chưa có báo cáo nào để xuất.";
    private static final String MESSAGE_EXPORT_FAILED = "Không tạo được tệp danh sách, hãy thử lại.";

    private final ReviewRepository reviewRepository = new ReviewRepository();
    private final ReportRepository reportRepository = new ReportRepository();
    private final MediaRepository mediaRepository = new MediaRepository();
    private final ExecutorService exportExecutor = Executors.newSingleThreadExecutor();

    // Which list is in reviews ("uid:received"), so a rotation does not read it again.
    private String loadedReviewsKey;
    // The uid whose sent reports are in reports, so a rotation does not query again.
    private String loadedReportsUid;

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
        if (Boolean.TRUE.equals(loading.getValue())) {
            return;
        }
        String uid = SessionManager.getInstance().getCurrentUserId();
        if (uid == null) {
            error.setValue(MESSAGE_REPORT_NOT_SIGNED_IN);
            return;
        }
        List<Uri> images = new ArrayList<Uri>();
        if (imageUris != null) {
            images.addAll(imageUris);
        }
        String formMessage = reportErrorMessage(checkReport(report, images.size(), uid));
        if (formMessage != null) {
            error.setValue(formMessage);
            return;
        }
        if (!NetworkMonitor.getInstance().isOnline()) {
            error.setValue(MESSAGE_OFFLINE);
            return;
        }
        loading.setValue(true);
        saveEvidenceImages(report, images, new ArrayList<String>());
    }

    public void cancelReport(String id) {
        if (Boolean.TRUE.equals(loading.getValue())) {
            return;
        }
        String uid = SessionManager.getInstance().getCurrentUserId();
        if (!canCancelReport(findReport(id), uid)) {
            error.setValue(MESSAGE_CANNOT_CANCEL_REPORT);
            return;
        }
        if (!NetworkMonitor.getInstance().isOnline()) {
            error.setValue(MESSAGE_OFFLINE);
            return;
        }
        final String reporterId = uid;
        loading.setValue(true);
        reportRepository.cancel(id, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                loading.postValue(false);
                refreshMyReports(reporterId);
            }

            @Override
            public void onError(String message) {
                loading.postValue(false);
                error.postValue(message);
            }
        });
    }

    public void loadMyReports() {
        String uid = SessionManager.getInstance().getCurrentUserId();
        if (uid == null || uid.isEmpty()) {
            error.setValue(MESSAGE_MY_REPORTS_NOT_SIGNED_IN);
            return;
        }
        boolean loadingNow = Boolean.TRUE.equals(loading.getValue());
        if (uid.equals(loadedReportsUid) && (reports.getValue() != null || loadingNow)) {
            return;
        }
        fetchMyReports(uid);
    }

    public void exportReports() {
        if (Boolean.TRUE.equals(loading.getValue())) {
            return;
        }
        List<Report> current = reports.getValue();
        if (current == null || current.isEmpty()) {
            error.setValue(MESSAGE_NOTHING_TO_EXPORT);
            return;
        }
        final List<Report> snapshot = new ArrayList<Report>(current);
        loading.setValue(true);
        // exportCsv writes a file, so it runs off the main thread.
        exportExecutor.execute(new Runnable() {
            @Override
            public void run() {
                File file = reportRepository.exportCsv(snapshot);
                loading.postValue(false);
                if (file == null) {
                    error.postValue(MESSAGE_EXPORT_FAILED);
                    return;
                }
                exportedFile.postValue(file);
            }
        });
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

    // The Rules let only the reporter cancel, and only while the report is OPEN (STT 62).
    static boolean canCancelReport(Report report, String currentUid) {
        if (report == null || currentUid == null || currentUid.isEmpty()) {
            return false;
        }
        return report.getStatus() == ReportStatus.OPEN && currentUid.equals(report.getReporterId());
    }

    private Report findReport(String id) {
        List<Report> current = reports.getValue();
        if (current == null || id == null) {
            return null;
        }
        for (Report report : current) {
            if (id.equals(report.getId())) {
                return report;
            }
        }
        return null;
    }

    // Read again after a cancel so the row shows the status the server now holds.
    private void refreshMyReports(String uid) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                fetchMyReports(uid);
            }
        });
    }

    private void fetchMyReports(final String uid) {
        loadedReportsUid = uid;
        loading.setValue(true);
        reportRepository.getMyReports(uid, new DataCallback<List<Report>>() {
            @Override
            public void onSuccess(List<Report> result) {
                if (!uid.equals(loadedReportsUid)) {
                    return;
                }
                reports.postValue(result != null ? result : new ArrayList<Report>());
                loading.postValue(false);
            }

            @Override
            public void onError(String message) {
                loadedReportsUid = null;
                loading.postValue(false);
                error.postValue(message);
            }
        });
    }

    // Rules section 18 also enforce the image cap and the conversation link; the self report rule is the app's.
    static int checkReport(Report report, int imageCount, String currentUid) {
        if (report == null || report.getReason() == null) {
            return REPORT_MISSING_REASON;
        }
        ReportTarget target = report.getTargetType();
        if (target == ReportTarget.USER && currentUid != null && currentUid.equals(report.getTargetId())) {
            return REPORT_SELF;
        }
        if (imageCount > MAX_REPORT_IMAGES) {
            return REPORT_TOO_MANY_IMAGES;
        }
        String conversationId = report.getConversationId();
        boolean hasConversation = conversationId != null && !conversationId.isEmpty();
        if (target == ReportTarget.MESSAGE && !hasConversation) {
            return REPORT_MISSING_CONVERSATION;
        }
        return REPORT_OK;
    }

    private static String reportErrorMessage(int checkResult) {
        if (checkResult == REPORT_MISSING_REASON) {
            return MESSAGE_MISSING_REASON;
        }
        if (checkResult == REPORT_SELF) {
            return MESSAGE_REPORT_SELF;
        }
        if (checkResult == REPORT_TOO_MANY_IMAGES) {
            return MESSAGE_TOO_MANY_IMAGES;
        }
        if (checkResult == REPORT_MISSING_CONVERSATION) {
            return MESSAGE_MISSING_CONVERSATION;
        }
        return null;
    }

    // One image at a time keeps evidenceImageIds in the order the user picked them.
    private void saveEvidenceImages(Report report, List<Uri> imageUris, List<String> savedIds) {
        if (savedIds.size() == imageUris.size()) {
            sendReport(report, savedIds);
            return;
        }
        Uri next = imageUris.get(savedIds.size());
        mediaRepository.saveImage(next, ImagePurpose.EVIDENCE, new DataCallback<String>() {
            @Override
            public void onSuccess(String imageId) {
                savedIds.add(imageId);
                saveEvidenceImages(report, imageUris, savedIds);
            }

            @Override
            public void onError(String message) {
                failReport(savedIds, message);
            }
        });
    }

    private void sendReport(Report report, List<String> imageIds) {
        report.setEvidenceImageIds(imageIds);
        reportRepository.create(report, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                loading.postValue(false);
                done.postValue(true);
            }

            @Override
            public void onError(String message) {
                failReport(imageIds, message);
            }
        });
    }

    // Images saved by a failed attempt are deleted so they do not stay in Firestore unused.
    private void failReport(List<String> savedIds, String message) {
        for (String imageId : savedIds) {
            mediaRepository.delete(imageId, new DataCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                }

                @Override
                public void onError(String deleteMessage) {
                    error.postValue(deleteMessage);
                }
            });
        }
        loading.postValue(false);
        error.postValue(message);
    }

    // Only the overall rating feeds ratingSum, so the three criteria are averaged here from the received reviews.
    public static double[] averageCriteria(List<Review> received) {
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

    @Override
    protected void onCleared() {
        super.onCleared();
        exportExecutor.shutdown();
    }

    public void clearError() {
        error.setValue(null);
    }

    public void clearDone() {
        done.setValue(false);
    }
}
