package com.nhom3.pubgapp.viewmodel;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.Timestamp;
import com.nhom3.pubgapp.data.AdminRepository;
import com.nhom3.pubgapp.data.DataCallback;
import com.nhom3.pubgapp.data.ListingRepository;
import com.nhom3.pubgapp.data.MediaRepository;
import com.nhom3.pubgapp.data.ReportRepository;
import com.nhom3.pubgapp.model.BannedKeyword;
import com.nhom3.pubgapp.model.Category;
import com.nhom3.pubgapp.model.ChatMessage;
import com.nhom3.pubgapp.model.Listing;
import com.nhom3.pubgapp.model.Report;
import com.nhom3.pubgapp.model.enums.ReportStatus;
import com.nhom3.pubgapp.model.enums.ReportTarget;
import com.nhom3.pubgapp.util.SessionManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

// State for admin moderation, banned keywords and categories. Methods not yet filled report NOT_READY.
public class AdminContentViewModel extends ViewModel {

    private static final long REQUEST_TIMEOUT_MS = 15000L;
    private static final String MESSAGE_TIMEOUT =
            "Máy chủ chưa trả lời sau 15 giây. Hãy kiểm tra mạng rồi làm mới màn hình.";
    private static final String MESSAGE_NOT_SIGNED_IN =
            "Phiên đăng nhập đã hết hạn. Hãy đăng nhập lại.";
    private static final String MESSAGE_ID_REQUIRED = "Thiếu mã của đối tượng cần xử lý.";
    private static final String MESSAGE_REASON_REQUIRED = "Hãy nhập lý do.";
    private static final String MESSAGE_ACTION_SELF =
            "Bạn không thể cảnh báo hoặc khóa chính tài khoản của mình.";
    private static final String MESSAGE_KEYWORD_REQUIRED = "Hãy nhập từ khóa.";
    private static final String MESSAGE_FILTER_REQUIRED = "Hãy chọn trạng thái báo cáo cần xem.";
    private static final String MESSAGE_REPORTS_NOT_CLOSED =
            "Đã xử lý tin nhưng chưa đóng được các báo cáo của tin. Hãy làm mới màn hình. ";

    private final ReportRepository reportRepository = new ReportRepository();
    private final ListingRepository listingRepository = new ListingRepository();
    private final AdminRepository adminRepository = new AdminRepository();
    private final MediaRepository mediaRepository = new MediaRepository();
    private final Handler timeoutHandler = new Handler(Looper.getMainLooper());
    // One guard per kind of work, so loading a listing never cancels loading its reports.
    private final RequestGuard reportsGuard = new RequestGuard();
    private final RequestGuard listingGuard = new RequestGuard();
    private final RequestGuard writeGuard = new RequestGuard();
    private final RequestGuard flaggedGuard = new RequestGuard();
    private final RequestGuard keywordsGuard = new RequestGuard();
    // The evidence dialog has its own guards and stays out of refreshLoading, so the list keeps its spinner state.
    private final RequestGuard evidenceGuard = new RequestGuard();
    // Set once a tab asked for its list, so a write refreshes only the lists the admin has opened.
    private volatile boolean flaggedAsked;
    private volatile boolean keywordsAsked;
    private volatile ReportStatus lastStatus;
    private volatile ReportTarget lastTargetType;
    private volatile String lastListingId;

    private final MutableLiveData<List<Report>> reports = new MutableLiveData<>();
    private final MutableLiveData<List<Listing>> reportedListings = new MutableLiveData<>();
    private final MutableLiveData<Listing> selectedListing = new MutableLiveData<>();
    private final MutableLiveData<List<Listing>> flagged = new MutableLiveData<>();
    private final MutableLiveData<List<byte[]>> reportEvidence = new MutableLiveData<>();
    private final MutableLiveData<List<ChatMessage>> reportedMessages = new MutableLiveData<>();
    private final MutableLiveData<List<BannedKeyword>> keywords = new MutableLiveData<>();
    private final MutableLiveData<List<Category>> categories = new MutableLiveData<>();
    private final MutableLiveData<Map<String, Integer>> categoryCounts = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> done = new MutableLiveData<>();

    public LiveData<List<Report>> getReports() {
        return reports;
    }

    public LiveData<List<Listing>> getReportedListings() {
        return reportedListings;
    }

    public LiveData<Listing> getSelectedListing() {
        return selectedListing;
    }

    public LiveData<List<Listing>> getFlagged() {
        return flagged;
    }

    public LiveData<List<byte[]>> getReportEvidence() {
        return reportEvidence;
    }

    public LiveData<List<ChatMessage>> getReportedMessages() {
        return reportedMessages;
    }

    public LiveData<List<BannedKeyword>> getKeywords() {
        return keywords;
    }

    public LiveData<List<Category>> getCategories() {
        return categories;
    }

    public LiveData<Map<String, Integer>> getCategoryCounts() {
        return categoryCounts;
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

    // A null targetType means every kind of target. The listings are read first so a row never shows a missing title.
    public void loadReports(ReportStatus status, ReportTarget targetType) {
        if (status == null) {
            error.postValue(MESSAGE_FILTER_REQUIRED);
            return;
        }
        lastStatus = status;
        lastTargetType = targetType;
        final int token = reportsGuard.begin();
        refreshLoading();
        reportRepository.getByStatus(status, targetType, new DataCallback<List<Report>>() {
            @Override
            public void onSuccess(List<Report> result) {
                publishWithListings(result, token, true);
            }

            @Override
            public void onError(String message) {
                failRequest(reportsGuard, token, message);
            }
        });
    }

    public void loadReportedListings() {
        final int token = reportsGuard.begin();
        refreshLoading();
        reportRepository.getByStatus(ReportStatus.OPEN, ReportTarget.LISTING, new DataCallback<List<Report>>() {
            @Override
            public void onSuccess(List<Report> result) {
                publishWithListings(result, token, false);
            }

            @Override
            public void onError(String message) {
                failRequest(reportsGuard, token, message);
            }
        });
    }

    public void loadListing(String listingId) {
        if (isBlank(listingId)) {
            error.postValue(MESSAGE_ID_REQUIRED);
            return;
        }
        lastListingId = listingId;
        final int token = listingGuard.begin();
        refreshLoading();
        listingRepository.getById(listingId, new DataCallback<Listing>() {
            @Override
            public void onSuccess(Listing result) {
                if (!listingGuard.finish(token)) {
                    return;
                }
                selectedListing.postValue(result);
                refreshLoading();
            }

            @Override
            public void onError(String message) {
                failRequest(listingGuard, token, message);
            }
        });
    }

    // Null while reading; an empty list means the report has no image. Order follows evidenceImageIds.
    public void loadReportEvidence(Report report) {
        reportEvidence.setValue(null);
        List<String> ids = new ArrayList<String>();
        if (report != null && report.getEvidenceImageIds() != null) {
            ids.addAll(report.getEvidenceImageIds());
        }
        // Beginning a request also drops the answer of any earlier one still on its way.
        int token = evidenceGuard.begin();
        if (ids.isEmpty()) {
            evidenceGuard.finish(token);
            reportEvidence.setValue(new ArrayList<byte[]>());
            return;
        }
        readEvidenceFrom(ids, new ArrayList<byte[]>(), token);
    }

    public void loadReportedConversation(String conversationId) {
        error.setValue(DataCallback.NOT_READY);
    }

    // The parameter is named listingId in the specification; a user or message report passes its own targetId.
    public void closeReports(String listingId, boolean dismissed) {
        String adminId = SessionManager.getInstance().getCurrentUserId();
        if (isBlank(listingId) || adminId.isEmpty()) {
            error.postValue(isBlank(listingId) ? MESSAGE_ID_REQUIRED : MESSAGE_NOT_SIGNED_IN);
            return;
        }
        final int token = writeGuard.begin();
        refreshLoading();
        reportRepository.closeByTarget(listingId, dismissed, adminId, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                finishWrite(token);
            }

            @Override
            public void onError(String message) {
                failRequest(writeGuard, token, message);
            }
        });
    }

    public void hide(final String listingId, String reason) {
        if (!canStartListingWrite(listingId, reason, true)) {
            return;
        }
        final int token = writeGuard.begin();
        refreshLoading();
        adminRepository.hideListing(listingId, reason, closeReportsAfter(listingId, token));
    }

    public void restore(String listingId) {
        if (!canStartListingWrite(listingId, null, false)) {
            return;
        }
        final int token = writeGuard.begin();
        refreshLoading();
        adminRepository.restoreListing(listingId, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                finishWrite(token);
            }

            @Override
            public void onError(String message) {
                failRequest(writeGuard, token, message);
            }
        });
    }

    public void remove(String listingId, String reason) {
        if (!canStartListingWrite(listingId, reason, true)) {
            return;
        }
        final int token = writeGuard.begin();
        refreshLoading();
        adminRepository.removeListing(listingId, reason, closeReportsAfter(listingId, token));
    }

    public void removeAllOf(String userId) {
        if (isBlank(userId)) {
            error.postValue(MESSAGE_ID_REQUIRED);
            return;
        }
        if (userId.equals(SessionManager.getInstance().getCurrentUserId())) {
            error.postValue(MESSAGE_ACTION_SELF);
            return;
        }
        final int token = writeGuard.begin();
        refreshLoading();
        adminRepository.removeAllListingsOf(userId, plainWrite(token));
    }

    public void loadFlagged() {
        flaggedAsked = true;
        final int token = flaggedGuard.begin();
        refreshLoading();
        adminRepository.getFlaggedListings(new DataCallback<List<Listing>>() {
            @Override
            public void onSuccess(List<Listing> result) {
                if (!flaggedGuard.finish(token)) {
                    return;
                }
                flagged.postValue(result);
                refreshLoading();
            }

            @Override
            public void onError(String message) {
                failRequest(flaggedGuard, token, message);
            }
        });
    }

    public void clearFlag(String listingId) {
        if (isBlank(listingId)) {
            error.postValue(MESSAGE_ID_REQUIRED);
            return;
        }
        final int token = writeGuard.begin();
        refreshLoading();
        adminRepository.clearFlag(listingId, plainWrite(token));
    }

    public void loadKeywords() {
        keywordsAsked = true;
        final int token = keywordsGuard.begin();
        refreshLoading();
        adminRepository.getKeywords(new DataCallback<List<BannedKeyword>>() {
            @Override
            public void onSuccess(List<BannedKeyword> result) {
                if (!keywordsGuard.finish(token)) {
                    return;
                }
                List<BannedKeyword> sorted = new ArrayList<BannedKeyword>(result);
                sortNewestFirst(sorted);
                keywords.postValue(sorted);
                refreshLoading();
            }

            @Override
            public void onError(String message) {
                failRequest(keywordsGuard, token, message);
            }
        });
    }

    public void addKeyword(String word) {
        if (isBlank(word)) {
            error.postValue(MESSAGE_KEYWORD_REQUIRED);
            return;
        }
        final int token = writeGuard.begin();
        refreshLoading();
        adminRepository.addKeyword(word, plainWrite(token));
    }

    public void removeKeyword(String id) {
        if (isBlank(id)) {
            error.postValue(MESSAGE_ID_REQUIRED);
            return;
        }
        final int token = writeGuard.begin();
        refreshLoading();
        adminRepository.removeKeyword(id, plainWrite(token));
    }

    public void loadCategories() {
        error.setValue(DataCallback.NOT_READY);
    }

    public void loadCategoryCounts() {
        error.setValue(DataCallback.NOT_READY);
    }

    public void saveCategory(Category category) {
        error.setValue(DataCallback.NOT_READY);
    }

    public void deleteCategory(String id) {
        error.setValue(DataCallback.NOT_READY);
    }

    public void setConditionFields(String id, List<String> fields) {
        error.setValue(DataCallback.NOT_READY);
    }

    public void clearError() {
        error.setValue(null);
    }

    public void clearDone() {
        done.setValue(false);
    }

    private boolean isBlank(@Nullable String text) {
        return text == null || text.trim().isEmpty();
    }

    // A listing write that closes reports also needs a reason and the signed-in admin id.
    private boolean canStartListingWrite(String listingId, String reason, boolean closesReports) {
        if (isBlank(listingId)) {
            error.postValue(MESSAGE_ID_REQUIRED);
            return false;
        }
        if (closesReports && isBlank(reason)) {
            error.postValue(MESSAGE_REASON_REQUIRED);
            return false;
        }
        if (closesReports && SessionManager.getInstance().getCurrentUserId().isEmpty()) {
            error.postValue(MESSAGE_NOT_SIGNED_IN);
            return false;
        }
        return true;
    }

    // After the listing changed, the reports of that listing are closed as handled (UC37).
    private DataCallback<Void> closeReportsAfter(final String listingId, final int token) {
        return new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                String adminId = SessionManager.getInstance().getCurrentUserId();
                reportRepository.closeByTarget(listingId, false, adminId, new DataCallback<Void>() {
                    @Override
                    public void onSuccess(Void closed) {
                        finishWrite(token);
                    }

                    @Override
                    public void onError(String message) {
                        if (writeGuard.isCurrent(token)) {
                            error.postValue(MESSAGE_REPORTS_NOT_CLOSED + message);
                        }
                        finishWrite(token);
                    }
                });
            }

            @Override
            public void onError(String message) {
                failRequest(writeGuard, token, message);
            }
        };
    }

    // A write with nothing to do after it but to report and reload.
    private DataCallback<Void> plainWrite(final int token) {
        return new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                finishWrite(token);
            }

            @Override
            public void onError(String message) {
                failRequest(writeGuard, token, message);
            }
        };
    }

    private void sortNewestFirst(List<BannedKeyword> list) {
        Collections.sort(list, new Comparator<BannedKeyword>() {
            @Override
            public int compare(BannedKeyword first, BannedKeyword second) {
                Timestamp firstTime = first.getCreatedAt();
                Timestamp secondTime = second.getCreatedAt();
                if (firstTime == null || secondTime == null) {
                    return firstTime == null ? (secondTime == null ? 0 : -1) : 1;
                }
                return secondTime.compareTo(firstTime);
            }
        });
    }

    private void finishWrite(int token) {
        if (!writeGuard.finish(token)) {
            return;
        }
        done.postValue(true);
        refreshLoading();
        reloadAfterWrite();
    }

    private void reloadAfterWrite() {
        ReportStatus status = lastStatus;
        if (status != null) {
            loadReports(status, lastTargetType);
        }
        String listingId = lastListingId;
        if (listingId != null) {
            loadListing(listingId);
        }
        if (flaggedAsked) {
            loadFlagged();
        }
        if (keywordsAsked) {
            loadKeywords();
        }
    }

    // One image at a time keeps the order; an image that cannot be read stays as empty bytes in its place.
    private void readEvidenceFrom(final List<String> ids, final List<byte[]> read, final int token) {
        if (!evidenceGuard.isCurrent(token)) {
            return;
        }
        if (read.size() == ids.size()) {
            if (evidenceGuard.finish(token)) {
                reportEvidence.postValue(read);
            }
            return;
        }
        mediaRepository.getImage(ids.get(read.size()), new DataCallback<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                read.add(bytes != null ? bytes : new byte[0]);
                readEvidenceFrom(ids, read, token);
            }

            @Override
            public void onError(String message) {
                read.add(new byte[0]);
                readEvidenceFrom(ids, read, token);
            }
        });
    }

    private void failRequest(RequestGuard guard, int token, String message) {
        if (!guard.finish(token)) {
            return;
        }
        error.postValue(message);
        refreshLoading();
    }

    private void refreshLoading() {
        boolean busy = reportsGuard.isActive() || listingGuard.isActive() || writeGuard.isActive()
                || flaggedGuard.isActive() || keywordsGuard.isActive();
        loading.postValue(busy);
    }

    private List<String> listingIdsOf(List<Report> source) {
        List<String> ids = new ArrayList<String>();
        for (Report report : source) {
            String id = report.getTargetId();
            boolean isListing = report.getTargetType() == ReportTarget.LISTING;
            if (isListing && !isBlank(id) && !ids.contains(id)) {
                ids.add(id);
            }
        }
        return ids;
    }

    // Reads every reported listing so the rows have a title and thumbnail; a listing that cannot be read is left out.
    private void publishWithListings(List<Report> source, int token, boolean publishReports) {
        if (!reportsGuard.isCurrent(token)) {
            return;
        }
        List<String> ids = listingIdsOf(source);
        if (ids.isEmpty()) {
            completeListingRead(new ArrayList<Listing>(), source, token, publishReports);
            return;
        }
        final ListingBatch batch = new ListingBatch(ids.size(), source, token, publishReports);
        for (String id : ids) {
            listingRepository.getById(id, new DataCallback<Listing>() {
                @Override
                public void onSuccess(Listing result) {
                    arrive(batch, result);
                }

                @Override
                public void onError(String message) {
                    arrive(batch, null);
                }
            });
        }
    }

    private void arrive(ListingBatch batch, @Nullable Listing listing) {
        if (batch.arrive(listing)) {
            completeListingRead(batch.copyFound(), batch.source, batch.token, batch.publishReports);
        }
    }

    // Listings go out before the reports: the screen redraws on the reports and must already see the titles.
    private void completeListingRead(List<Listing> listings, List<Report> source, int token,
            boolean publishReports) {
        if (!reportsGuard.finish(token)) {
            return;
        }
        reportedListings.postValue(listings);
        if (publishReports) {
            reports.postValue(source);
        }
        refreshLoading();
    }

    // Collects the answers of the parallel listing reads; the last one to arrive publishes.
    private static final class ListingBatch {
        private final List<Report> source;
        private final int token;
        private final boolean publishReports;
        private final List<Listing> found = new ArrayList<Listing>();
        private int pending;

        ListingBatch(int pending, List<Report> source, int token, boolean publishReports) {
            this.pending = pending;
            this.source = source;
            this.token = token;
            this.publishReports = publishReports;
        }

        synchronized boolean arrive(@Nullable Listing listing) {
            if (listing != null) {
                found.add(listing);
            }
            pending--;
            return pending == 0;
        }

        synchronized List<Listing> copyFound() {
            return new ArrayList<Listing>(found);
        }
    }

    // Lets a newer request replace an older one and gives up on a request after 15 seconds.
    private final class RequestGuard {
        private int token;
        private boolean active;

        synchronized int begin() {
            token++;
            active = true;
            final int issued = token;
            timeoutHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (finish(issued)) {
                        error.setValue(MESSAGE_TIMEOUT);
                        refreshLoading();
                    }
                }
            }, REQUEST_TIMEOUT_MS);
            return issued;
        }

        synchronized boolean finish(int issued) {
            if (!active || issued != token) {
                return false;
            }
            active = false;
            return true;
        }

        synchronized boolean isCurrent(int issued) {
            return active && issued == token;
        }

        synchronized boolean isActive() {
            return active;
        }
    }
}
