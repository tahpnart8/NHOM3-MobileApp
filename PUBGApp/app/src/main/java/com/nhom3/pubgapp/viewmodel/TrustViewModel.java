package com.nhom3.pubgapp.viewmodel;

import android.net.Uri;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.nhom3.pubgapp.data.DataCallback;
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
        error.setValue(DataCallback.NOT_READY);
    }

    public void loadReviews(String uid, boolean received) {
        error.setValue(DataCallback.NOT_READY);
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

    public void clearError() {
        error.setValue(null);
    }

    public void clearDone() {
        done.setValue(false);
    }
}
