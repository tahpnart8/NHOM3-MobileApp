package com.nhom3.pubgapp.viewmodel;

import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.Timestamp;
import com.nhom3.pubgapp.data.ComplaintRepository;
import com.nhom3.pubgapp.data.DataCallback;
import com.nhom3.pubgapp.data.DealRepository;
import com.nhom3.pubgapp.data.MediaRepository;
import com.nhom3.pubgapp.data.ReportRepository;
import com.nhom3.pubgapp.data.ReviewRepository;
import com.nhom3.pubgapp.model.Complaint;
import com.nhom3.pubgapp.model.Deal;
import com.nhom3.pubgapp.model.Report;
import com.nhom3.pubgapp.model.Review;
import com.nhom3.pubgapp.model.enums.DealStatus;
import com.nhom3.pubgapp.model.enums.DeliveryMethod;
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

    // STT 64 and trang-thai.md section 6: at least one evidence image, at most five.
    static final int MAX_COMPLAINT_IMAGES = 5;
    static final int COMPLAINT_OK = 0;
    static final int COMPLAINT_MISSING_REASON = 1;
    static final int COMPLAINT_NO_IMAGE = 2;
    static final int COMPLAINT_TOO_MANY_IMAGES = 3;
    static final int COMPLAINT_NO_DEAL = 4;
    static final int COMPLAINT_NOT_PARTY = 5;
    static final int COMPLAINT_WRONG_STATUS = 6;
    static final int COMPLAINT_COUNTDOWN_OVER = 7;
    private static final String MESSAGE_COMPLAINT_NOT_SIGNED_IN =
            "Bạn cần đăng nhập để gửi khiếu nại.";
    private static final String MESSAGE_COMPLAINT_REASON = "Hãy chọn một lý do khiếu nại.";
    private static final String MESSAGE_COMPLAINT_NO_IMAGE = "Cần ít nhất 1 ảnh bằng chứng.";
    private static final String MESSAGE_COMPLAINT_TOO_MANY = "Chỉ đính kèm được tối đa 5 ảnh.";
    private static final String MESSAGE_COMPLAINT_NO_DEAL = "Không tìm thấy giao dịch.";
    private static final String MESSAGE_COMPLAINT_NOT_PARTY =
            "Chỉ người mua hoặc người bán của giao dịch mới khiếu nại được.";
    private static final String MESSAGE_COMPLAINT_WRONG_STATUS =
            "Chỉ khiếu nại được khi giao dịch đã giao và còn đếm ngược, "
            + "hoặc đã xác nhận với hình thức gặp trực tiếp.";
    private static final String MESSAGE_COMPLAINT_COUNTDOWN_OVER =
            "Đã hết thời gian đếm ngược, giao dịch không còn khiếu nại được.";
    private static final String MESSAGE_MY_COMPLAINTS_NOT_SIGNED_IN =
            "Bạn cần đăng nhập để xem khiếu nại của mình.";
    private static final String MESSAGE_COMPLAINT_NOT_FOUND = "Không tìm thấy khiếu nại này.";

    private final ReviewRepository reviewRepository = new ReviewRepository();
    private final ReportRepository reportRepository = new ReportRepository();
    private final MediaRepository mediaRepository = new MediaRepository();
    private final ExecutorService exportExecutor = Executors.newSingleThreadExecutor();
    private final ComplaintRepository complaintRepository = new ComplaintRepository();
    private final DealRepository dealRepository = new DealRepository();

    // Which list is in reviews ("uid:received"), so a rotation does not read it again.
    private String loadedReviewsKey;
    // The uid whose sent reports are in reports, so a rotation does not query again.
    private String loadedReportsUid;
    // The uid whose sent complaints are in complaints, for the same reason.
    private String loadedComplaintsUid;

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
        if (Boolean.TRUE.equals(loading.getValue())) {
            return;
        }
        String uid = SessionManager.getInstance().getCurrentUserId();
        if (uid == null || uid.isEmpty()) {
            error.setValue(MESSAGE_COMPLAINT_NOT_SIGNED_IN);
            return;
        }
        List<Uri> images = new ArrayList<Uri>();
        if (evidenceUris != null) {
            images.addAll(evidenceUris);
        }
        String formMessage = complaintErrorMessage(checkComplaintForm(complaint, images.size()));
        if (formMessage != null) {
            error.setValue(formMessage);
            return;
        }
        if (!NetworkMonitor.getInstance().isOnline()) {
            error.setValue(MESSAGE_OFFLINE);
            return;
        }
        loading.setValue(true);
        checkDealThenSubmit(complaint, images, uid);
    }

    // ComplaintRepository.addEvidence checks NEEDS_EVIDENCE and the owner; here only the image count is checked.
    public void addEvidence(String id, List<Uri> evidenceUris) {
        if (Boolean.TRUE.equals(loading.getValue())) {
            return;
        }
        List<Uri> images = new ArrayList<Uri>();
        if (evidenceUris != null) {
            images.addAll(evidenceUris);
        }
        if (images.isEmpty()) {
            error.setValue(MESSAGE_COMPLAINT_NO_IMAGE);
            return;
        }
        if (images.size() > MAX_COMPLAINT_IMAGES) {
            error.setValue(MESSAGE_COMPLAINT_TOO_MANY);
            return;
        }
        if (!NetworkMonitor.getInstance().isOnline()) {
            error.setValue(MESSAGE_OFFLINE);
            return;
        }
        loading.setValue(true);
        saveImagesThen(images, new ArrayList<String>(), new ImagesSaved() {
            @Override
            public void onSaved(List<String> imageIds) {
                sendEvidence(id, imageIds);
            }
        });
    }

    private void sendEvidence(String id, List<String> imageIds) {
        complaintRepository.addEvidence(id, imageIds, new DataCallback<Void>() {
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

    public void loadMyComplaints() {
        String uid = SessionManager.getInstance().getCurrentUserId();
        if (uid == null || uid.isEmpty()) {
            error.setValue(MESSAGE_MY_COMPLAINTS_NOT_SIGNED_IN);
            return;
        }
        if (uid.equals(loadedComplaintsUid)) {
            return;
        }
        fetchMyComplaints(uid);
    }

    // getDetail also reads the admin-only note the Rules refuse to users, so the row already read is shown.
    public void loadComplaint(String id) {
        Complaint complaint = findComplaint(id);
        if (complaint == null) {
            error.setValue(MESSAGE_COMPLAINT_NOT_FOUND);
            return;
        }
        selectedComplaint.setValue(complaint);
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

    private Complaint findComplaint(String id) {
        List<Complaint> current = complaints.getValue();
        if (current == null || id == null) {
            return null;
        }
        for (Complaint complaint : current) {
            if (id.equals(complaint.getId())) {
                return complaint;
            }
        }
        return null;
    }

    private void fetchMyComplaints(final String uid) {
        loadedComplaintsUid = uid;
        loading.setValue(true);
        complaintRepository.getMyComplaints(uid, new DataCallback<List<Complaint>>() {
            @Override
            public void onSuccess(List<Complaint> result) {
                if (!uid.equals(loadedComplaintsUid)) {
                    return;
                }
                List<Complaint> list = result != null ? result : new ArrayList<Complaint>();
                complaints.postValue(list);
                refreshSelectedComplaint(list);
                loading.postValue(false);
            }

            @Override
            public void onError(String message) {
                loadedComplaintsUid = null;
                loading.postValue(false);
                error.postValue(message);
            }
        });
    }

    // An open detail follows the list, so it shows the status the server holds after a reload.
    private void refreshSelectedComplaint(List<Complaint> list) {
        Complaint shown = selectedComplaint.getValue();
        if (shown == null || shown.getId() == null) {
            return;
        }
        for (Complaint complaint : list) {
            if (shown.getId().equals(complaint.getId())) {
                selectedComplaint.postValue(complaint);
                return;
            }
        }
    }

    // The deal is read first, so no image is uploaded for a complaint the deal would refuse.
    private void checkDealThenSubmit(Complaint complaint, List<Uri> images, String uid) {
        dealRepository.getById(complaint.getDealId(), new DataCallback<Deal>() {
            @Override
            public void onSuccess(Deal deal) {
                String dealMessage = complaintErrorMessage(checkComplaintDeal(deal, uid, Timestamp.now()));
                if (dealMessage != null) {
                    loading.postValue(false);
                    error.postValue(dealMessage);
                    return;
                }
                saveImagesThen(images, new ArrayList<String>(), new ImagesSaved() {
                    @Override
                    public void onSaved(List<String> imageIds) {
                        sendComplaint(complaint, imageIds);
                    }
                });
            }

            @Override
            public void onError(String message) {
                loading.postValue(false);
                error.postValue(message);
            }
        });
    }

    private void sendComplaint(Complaint complaint, List<String> imageIds) {
        complaint.setEvidenceImageIds(imageIds);
        complaintRepository.create(complaint, new DataCallback<Void>() {
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

    /** Called once every picked image has been saved, with their ids in the picked order. */
    private interface ImagesSaved {
        void onSaved(List<String> imageIds);
    }

    // One image at a time keeps the ids in the order the user picked them; a failure deletes the saved ones.
    private void saveImagesThen(List<Uri> imageUris, List<String> savedIds, ImagesSaved next) {
        if (savedIds.size() == imageUris.size()) {
            next.onSaved(savedIds);
            return;
        }
        Uri image = imageUris.get(savedIds.size());
        mediaRepository.saveImage(image, ImagePurpose.EVIDENCE, new DataCallback<String>() {
            @Override
            public void onSuccess(String imageId) {
                savedIds.add(imageId);
                saveImagesThen(imageUris, savedIds, next);
            }

            @Override
            public void onError(String message) {
                failReport(savedIds, message);
            }
        });
    }

    static int checkComplaintForm(Complaint complaint, int imageCount) {
        if (complaint == null || complaint.getReason() == null) {
            return COMPLAINT_MISSING_REASON;
        }
        if (imageCount < 1) {
            return COMPLAINT_NO_IMAGE;
        }
        if (imageCount > MAX_COMPLAINT_IMAGES) {
            return COMPLAINT_TOO_MANY_IMAGES;
        }
        return COMPLAINT_OK;
    }

    // trang-thai.md section 3: SHIPPED before autoReleaseAt, or CONFIRMED when meeting in person.
    static int checkComplaintDeal(Deal deal, String uid, Timestamp now) {
        if (deal == null) {
            return COMPLAINT_NO_DEAL;
        }
        if (uid == null || !deal.isParticipant(uid)) {
            return COMPLAINT_NOT_PARTY;
        }
        DealStatus status = deal.getStatus();
        if (status == DealStatus.CONFIRMED && deal.getDeliveryMethod() == DeliveryMethod.MEET_UP) {
            return COMPLAINT_OK;
        }
        if (status != DealStatus.SHIPPED) {
            return COMPLAINT_WRONG_STATUS;
        }
        Timestamp releaseAt = deal.getAutoReleaseAt();
        if (releaseAt != null && now.compareTo(releaseAt) >= 0) {
            return COMPLAINT_COUNTDOWN_OVER;
        }
        return COMPLAINT_OK;
    }

    private static String complaintErrorMessage(int checkResult) {
        switch (checkResult) {
            case COMPLAINT_MISSING_REASON:
                return MESSAGE_COMPLAINT_REASON;
            case COMPLAINT_NO_IMAGE:
                return MESSAGE_COMPLAINT_NO_IMAGE;
            case COMPLAINT_TOO_MANY_IMAGES:
                return MESSAGE_COMPLAINT_TOO_MANY;
            case COMPLAINT_NO_DEAL:
                return MESSAGE_COMPLAINT_NO_DEAL;
            case COMPLAINT_NOT_PARTY:
                return MESSAGE_COMPLAINT_NOT_PARTY;
            case COMPLAINT_WRONG_STATUS:
                return MESSAGE_COMPLAINT_WRONG_STATUS;
            case COMPLAINT_COUNTDOWN_OVER:
                return MESSAGE_COMPLAINT_COUNTDOWN_OVER;
            default:
                return null;
        }
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
