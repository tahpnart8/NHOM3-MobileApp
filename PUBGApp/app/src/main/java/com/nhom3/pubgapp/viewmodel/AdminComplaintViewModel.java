package com.nhom3.pubgapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.nhom3.pubgapp.data.DataCallback;
import com.nhom3.pubgapp.model.ChatMessage;
import com.nhom3.pubgapp.model.Complaint;
import com.nhom3.pubgapp.model.Deal;
import com.nhom3.pubgapp.model.enums.ComplaintDecision;
import com.nhom3.pubgapp.model.enums.ComplaintStatus;
import com.nhom3.pubgapp.model.enums.Priority;

import java.util.List;

// State for admin complaint handling. Action bodies report NOT_READY until the repositories are filled.
public class AdminComplaintViewModel extends ViewModel {

    private final MutableLiveData<List<Complaint>> complaints = new MutableLiveData<>();
    private final MutableLiveData<Complaint> selected = new MutableLiveData<>();
    private final MutableLiveData<Deal> deal = new MutableLiveData<>();
    private final MutableLiveData<List<ChatMessage>> messages = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> done = new MutableLiveData<>();

    public LiveData<List<Complaint>> getComplaints() {
        return complaints;
    }

    public LiveData<Complaint> getSelected() {
        return selected;
    }

    public LiveData<Deal> getDeal() {
        return deal;
    }

    public LiveData<List<ChatMessage>> getMessages() {
        return messages;
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

    public void loadComplaints(ComplaintStatus status, Priority priority) {
        error.setValue(DataCallback.NOT_READY);
    }

    public void loadDetail(String id) {
        error.setValue(DataCallback.NOT_READY);
    }

    public void setPriority(String id, Priority priority) {
        error.setValue(DataCallback.NOT_READY);
    }

    public void decide(String id, ComplaintDecision decision, String message) {
        error.setValue(DataCallback.NOT_READY);
    }

    public void saveNote(String id, String note) {
        error.setValue(DataCallback.NOT_READY);
    }

    public void clearError() {
        error.setValue(null);
    }

    public void clearDone() {
        done.setValue(false);
    }
}
