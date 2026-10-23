package com.nhom3.pubgapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.nhom3.pubgapp.data.DataCallback;
import com.nhom3.pubgapp.model.BannedKeyword;
import com.nhom3.pubgapp.model.Category;
import com.nhom3.pubgapp.model.ChatMessage;
import com.nhom3.pubgapp.model.Listing;
import com.nhom3.pubgapp.model.Report;
import com.nhom3.pubgapp.model.enums.ReportStatus;
import com.nhom3.pubgapp.model.enums.ReportTarget;

import java.util.List;
import java.util.Map;

// State for admin content moderation, banned keywords and categories. Action bodies report NOT_READY until the repositories are filled.
public class AdminContentViewModel extends ViewModel {

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

    public void loadReports(ReportStatus status, ReportTarget targetType) {
        error.setValue(DataCallback.NOT_READY);
    }

    public void loadReportedListings() {
        error.setValue(DataCallback.NOT_READY);
    }

    public void loadListing(String listingId) {
        error.setValue(DataCallback.NOT_READY);
    }

    public void loadReportEvidence(Report report) {
        error.setValue(DataCallback.NOT_READY);
    }

    public void loadReportedConversation(String conversationId) {
        error.setValue(DataCallback.NOT_READY);
    }

    public void closeReports(String listingId, boolean dismissed) {
        error.setValue(DataCallback.NOT_READY);
    }

    public void hide(String listingId, String reason) {
        error.setValue(DataCallback.NOT_READY);
    }

    public void restore(String listingId) {
        error.setValue(DataCallback.NOT_READY);
    }

    public void remove(String listingId, String reason) {
        error.setValue(DataCallback.NOT_READY);
    }

    public void removeAllOf(String userId) {
        error.setValue(DataCallback.NOT_READY);
    }

    public void loadFlagged() {
        error.setValue(DataCallback.NOT_READY);
    }

    public void clearFlag(String listingId) {
        error.setValue(DataCallback.NOT_READY);
    }

    public void loadKeywords() {
        error.setValue(DataCallback.NOT_READY);
    }

    public void addKeyword(String word) {
        error.setValue(DataCallback.NOT_READY);
    }

    public void removeKeyword(String id) {
        error.setValue(DataCallback.NOT_READY);
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
}
