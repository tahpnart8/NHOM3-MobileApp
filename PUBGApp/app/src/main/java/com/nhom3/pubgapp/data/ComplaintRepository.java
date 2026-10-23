package com.nhom3.pubgapp.data;

import com.nhom3.pubgapp.model.Complaint;
import com.nhom3.pubgapp.model.enums.ComplaintDecision;
import com.nhom3.pubgapp.model.enums.ComplaintStatus;
import com.nhom3.pubgapp.model.enums.Priority;
import java.util.List;

// Skeleton: the module owner fills each body as the issue assigns it.
public class ComplaintRepository extends BaseRepository {

    public void create(Complaint complaint, DataCallback<Void> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void getMyComplaints(String uid, DataCallback<List<Complaint>> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void withdraw(String id, DataCallback<Void> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void getByStatus(ComplaintStatus status, Priority priority, DataCallback<List<Complaint>> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void getDetail(String id, DataCallback<Complaint> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void decide(String id, ComplaintDecision decision, String message, DataCallback<Void> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void saveAdminNote(String id, String note, DataCallback<Void> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void addEvidence(String id, List<String> imageIds, DataCallback<Void> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void setPriority(String id, Priority priority, DataCallback<Void> cb) {
        cb.onError(DataCallback.NOT_READY);
    }
}
