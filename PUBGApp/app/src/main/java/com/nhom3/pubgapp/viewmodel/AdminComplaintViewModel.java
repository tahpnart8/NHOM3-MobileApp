package com.nhom3.pubgapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.Timestamp;
import com.nhom3.pubgapp.data.ChatRepository;
import com.nhom3.pubgapp.data.ComplaintRepository;
import com.nhom3.pubgapp.data.DataCallback;
import com.nhom3.pubgapp.data.DealRepository;
import com.nhom3.pubgapp.model.ChatMessage;
import com.nhom3.pubgapp.model.Complaint;
import com.nhom3.pubgapp.model.Deal;
import com.nhom3.pubgapp.model.enums.ComplaintDecision;
import com.nhom3.pubgapp.model.enums.ComplaintStatus;
import com.nhom3.pubgapp.model.enums.Priority;
import com.nhom3.pubgapp.util.NetworkMonitor;
import com.nhom3.pubgapp.util.SessionManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

// State for admin complaint handling. Action bodies report NOT_READY until the repositories are filled.
public class AdminComplaintViewModel extends ViewModel {

    private static final String MESSAGE_NOT_ADMIN = "Chỉ tài khoản admin mới xem được khiếu nại.";
    private static final String MESSAGE_OFFLINE = "Không có kết nối.";
    private static final String MESSAGE_CHAT_FAILED = "Không tải được lịch sử chat của giao dịch.";

    private final MutableLiveData<List<Complaint>> complaints = new MutableLiveData<>();
    private final MutableLiveData<Complaint> selected = new MutableLiveData<>();
    private final MutableLiveData<Deal> deal = new MutableLiveData<>();
    private final MutableLiveData<List<ChatMessage>> messages = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> done = new MutableLiveData<>();

    private final ComplaintRepository complaintRepository = new ComplaintRepository();
    private final DealRepository dealRepository = new DealRepository();
    private final ChatRepository chatRepository = new ChatRepository();

    // The filter whose answer is in complaints, so a rotation or a chip restore does not query again.
    private String loadedFilterKey;

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
        // The Rules decide too; this keeps a non-admin from even sending the query (R12).
        if (!SessionManager.getInstance().isAdmin()) {
            error.setValue(MESSAGE_NOT_ADMIN);
            return;
        }
        final String key = status + ":" + priority;
        if (key.equals(loadedFilterKey) && complaints.getValue() != null) {
            return;
        }
        loadedFilterKey = key;
        complaints.setValue(null);
        loading.setValue(true);
        complaintRepository.getByStatus(status, priority, new DataCallback<List<Complaint>>() {
            @Override
            public void onSuccess(List<Complaint> result) {
                if (!key.equals(loadedFilterKey)) {
                    return;
                }
                complaints.postValue(sortForAdmin(result));
                loading.postValue(false);
            }

            @Override
            public void onError(String message) {
                if (key.equals(loadedFilterKey)) {
                    loadedFilterKey = null;
                }
                loading.postValue(false);
                error.postValue(message);
            }
        });
    }

    public void loadDetail(String id) {
        if (!SessionManager.getInstance().isAdmin()) {
            error.setValue(MESSAGE_NOT_ADMIN);
            return;
        }
        if (id == null || id.isEmpty() || Boolean.TRUE.equals(loading.getValue())) {
            return;
        }
        loading.setValue(true);
        complaintRepository.getDetail(id, new DataCallback<Complaint>() {
            @Override
            public void onSuccess(Complaint result) {
                if (result.getId() == null) {
                    result.setId(id);
                }
                selected.postValue(result);
                loadDealOf(result);
            }

            @Override
            public void onError(String message) {
                loading.postValue(false);
                error.postValue(message);
            }
        });
    }

    public void setPriority(String id, Priority priority) {
        if (!SessionManager.getInstance().isAdmin()) {
            error.setValue(MESSAGE_NOT_ADMIN);
            return;
        }
        if (!NetworkMonitor.getInstance().isOnline()) {
            error.setValue(MESSAGE_OFFLINE);
            restoreSelected();
            return;
        }
        loading.setValue(true);
        complaintRepository.setPriority(id, priority, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Complaint current = selected.getValue();
                if (current != null) {
                    current.setPriority(priority);
                    selected.postValue(current);
                }
                loading.postValue(false);
            }

            @Override
            public void onError(String message) {
                loading.postValue(false);
                error.postValue(message);
                restoreSelected();
            }
        });
    }

    // Posting the unchanged complaint again puts the chips back on the saved priority.
    private void restoreSelected() {
        Complaint current = selected.getValue();
        if (current != null) {
            selected.postValue(current);
        }
    }

    // The deal of the complaint (same id) and then the chat of its two parties, for the file (STT 88).
    private void loadDealOf(Complaint complaint) {
        dealRepository.getById(complaint.getDealId(), new DataCallback<Deal>() {
            @Override
            public void onSuccess(Deal result) {
                deal.postValue(result);
                loadChatOf(result);
            }

            @Override
            public void onError(String message) {
                loading.postValue(false);
                error.postValue(message);
            }
        });
    }

    private void loadChatOf(Deal dealOfComplaint) {
        chatRepository.getMessagesOnce(conversationIdOf(dealOfComplaint), new DataCallback<List<ChatMessage>>() {
            @Override
            public void onSuccess(List<ChatMessage> result) {
                messages.postValue(result != null ? result : new ArrayList<ChatMessage>());
                loading.postValue(false);
            }

            // The file is still useful without the chat, so only a short note is shown.
            @Override
            public void onError(String message) {
                messages.postValue(new ArrayList<ChatMessage>());
                loading.postValue(false);
                error.postValue(MESSAGE_CHAT_FAILED);
            }
        });
    }

    // firestore-schema.md, conversations: the document id is listingId_buyerId.
    static String conversationIdOf(Deal dealOfComplaint) {
        return dealOfComplaint.getListingId() + "_" + dealOfComplaint.getBuyerId();
    }

    public void decide(String id, ComplaintDecision decision, String message) {
        error.setValue(DataCallback.NOT_READY);
    }

    public void saveNote(String id, String note) {
        error.setValue(DataCallback.NOT_READY);
    }

    // High priority first, then the newest; a missing priority counts as MEDIUM, the default (trang-thai.md 6).
    static List<Complaint> sortForAdmin(List<Complaint> source) {
        List<Complaint> sorted = new ArrayList<Complaint>();
        if (source != null) {
            sorted.addAll(source);
        }
        Collections.sort(sorted, new Comparator<Complaint>() {
            @Override
            public int compare(Complaint first, Complaint second) {
                int byPriority = Integer.compare(priorityRank(second.getPriority()), priorityRank(first.getPriority()));
                if (byPriority != 0) {
                    return byPriority;
                }
                return compareNewestFirst(first.getCreatedAt(), second.getCreatedAt());
            }
        });
        return sorted;
    }

    private static int priorityRank(Priority priority) {
        if (priority == Priority.HIGH) {
            return 2;
        }
        if (priority == Priority.LOW) {
            return 0;
        }
        return 1;
    }

    // A complaint with no server time yet was just written, so it counts as the newest.
    private static int compareNewestFirst(Timestamp first, Timestamp second) {
        if (first == null && second == null) {
            return 0;
        }
        if (first == null) {
            return -1;
        }
        if (second == null) {
            return 1;
        }
        return second.compareTo(first);
    }

    public void clearError() {
        error.setValue(null);
    }

    public void clearDone() {
        done.setValue(false);
    }
}
