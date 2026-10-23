package com.nhom3.pubgapp.data;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.nhom3.pubgapp.model.Complaint;
import com.nhom3.pubgapp.model.enums.ComplaintDecision;
import com.nhom3.pubgapp.model.enums.ComplaintStatus;
import com.nhom3.pubgapp.model.enums.Priority;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Complaints about a deal; the document id is the dealId (firestore-schema.md, complaints and adminNote).
public class ComplaintRepository extends BaseRepository {

    private static final String TAG = "ComplaintRepository";
    private static final String COLLECTION = "complaints";
    private static final String ADMIN_NOTE_COLLECTION = "adminNote";
    private static final String ADMIN_NOTE_DOCUMENT = "note";
    private static final String FIELD_COMPLAINANT_ID = "complainantId";
    private static final String FIELD_STATUS = "status";
    private static final String FIELD_PRIORITY = "priority";
    private static final String FIELD_EVIDENCE_IMAGE_IDS = "evidenceImageIds";
    private static final String FIELD_CREATED_AT = "createdAt";
    private static final String FIELD_UPDATED_AT = "updatedAt";
    private static final String FIELD_NOTE_COMPLAINT_ID = "complaintId";
    private static final String FIELD_NOTE_TEXT = "text";
    private static final String FIELD_NOTE_ADMIN_ID = "adminId";

    private static final String ERROR_NOT_SIGNED_IN = "Bạn cần đăng nhập để thực hiện thao tác này.";
    private static final String ERROR_NOT_FOUND = "Không tìm thấy đơn khiếu nại.";
    private static final String ERROR_NOT_NEEDS_EVIDENCE =
            "Đơn khiếu nại này không cần bổ sung bằng chứng.";
    private static final String ERROR_INVALID_EVIDENCE = "Mỗi lần bổ sung từ 1 đến 5 ảnh.";
    private static final String ERROR_GENERIC = "Không kết nối được máy chủ, vui lòng thử lại.";

    public void create(Complaint complaint, DataCallback<Void> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void getMyComplaints(String uid, DataCallback<List<Complaint>> cb) {
        Query query = db.collection(COLLECTION)
                .whereEqualTo(FIELD_COMPLAINANT_ID, uid)
                .orderBy(FIELD_CREATED_AT, Query.Direction.DESCENDING);
        runListQuery(query, cb);
    }

    public void withdraw(String id, DataCallback<Void> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void getByStatus(ComplaintStatus status, Priority priority, DataCallback<List<Complaint>> cb) {
        Query query = db.collection(COLLECTION).whereEqualTo(FIELD_STATUS, status.name());
        if (priority != null) {
            query = query.whereEqualTo(FIELD_PRIORITY, priority.name());
        }
        query = query.orderBy(FIELD_CREATED_AT, Query.Direction.ASCENDING);
        runListQuery(query, cb);
    }

    // Admin only: the internal note lives in a sub-document users cannot read.
    public void getDetail(String id, DataCallback<Complaint> cb) {
        final DataCallback<Complaint> callback = cb;
        final DocumentReference reference = db.collection(COLLECTION).document(id);
        reference.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot snapshot) {
                        Complaint complaint = snapshot.toObject(Complaint.class);
                        if (complaint == null) {
                            callback.onError(ERROR_NOT_FOUND);
                            return;
                        }
                        readAdminNote(reference, complaint, callback);
                    }
                })
                .addOnFailureListener(failure(cb));
    }

    public void decide(String id, ComplaintDecision decision, String message, DataCallback<Void> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void saveAdminNote(String id, String note, DataCallback<Void> cb) {
        String uid = signedInUserId();
        if (uid == null) {
            cb.onError(ERROR_NOT_SIGNED_IN);
            return;
        }
        Map<String, Object> data = new HashMap<>();
        data.put(FIELD_NOTE_COMPLAINT_ID, id);
        data.put(FIELD_NOTE_TEXT, note);
        data.put(FIELD_NOTE_ADMIN_ID, uid);
        data.put(FIELD_UPDATED_AT, FieldValue.serverTimestamp());
        adminNoteReference(db.collection(COLLECTION).document(id))
                .set(data)
                .addOnSuccessListener(voidSuccess(cb))
                .addOnFailureListener(failure(cb));
    }

    public void addEvidence(String id, List<String> imageIds, DataCallback<Void> cb) {
        final List<String> added = imageIds;
        final DataCallback<Void> callback = cb;
        final DocumentReference reference = db.collection(COLLECTION).document(id);
        reference.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot snapshot) {
                        writeEvidence(reference, snapshot, added, callback);
                    }
                })
                .addOnFailureListener(failure(cb));
    }

    public void setPriority(String id, Priority priority, DataCallback<Void> cb) {
        Map<String, Object> changes = new HashMap<>();
        changes.put(FIELD_PRIORITY, priority.name());
        changes.put(FIELD_UPDATED_AT, FieldValue.serverTimestamp());
        db.collection(COLLECTION).document(id)
                .update(changes)
                .addOnSuccessListener(voidSuccess(cb))
                .addOnFailureListener(failure(cb));
    }

    private void readAdminNote(DocumentReference reference, Complaint complaint, DataCallback<Complaint> cb) {
        final Complaint result = complaint;
        final DataCallback<Complaint> callback = cb;
        adminNoteReference(reference).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot snapshot) {
                        result.setAdminNote(snapshot.getString(FIELD_NOTE_TEXT));
                        callback.onSuccess(result);
                    }
                })
                .addOnFailureListener(failure(cb));
    }

    private void writeEvidence(DocumentReference reference, DocumentSnapshot snapshot, List<String> imageIds,
                               DataCallback<Void> cb) {
        Complaint complaint = snapshot.toObject(Complaint.class);
        if (complaint == null) {
            cb.onError(ERROR_NOT_FOUND);
            return;
        }
        if (complaint.getStatus() != ComplaintStatus.NEEDS_EVIDENCE) {
            cb.onError(ERROR_NOT_NEEDS_EVIDENCE);
            return;
        }
        try {
            complaint.addEvidence(imageIds);
        } catch (IllegalArgumentException error) {
            cb.onError(ERROR_INVALID_EVIDENCE);
            return;
        }
        Map<String, Object> changes = new HashMap<>();
        changes.put(FIELD_EVIDENCE_IMAGE_IDS, complaint.getEvidenceImageIds());
        changes.put(FIELD_STATUS, complaint.getStatus().name());
        changes.put(FIELD_UPDATED_AT, FieldValue.serverTimestamp());
        reference.update(changes)
                .addOnSuccessListener(voidSuccess(cb))
                .addOnFailureListener(failure(cb));
    }

    private DocumentReference adminNoteReference(DocumentReference complaintReference) {
        return complaintReference.collection(ADMIN_NOTE_COLLECTION).document(ADMIN_NOTE_DOCUMENT);
    }

    private void runListQuery(Query query, DataCallback<List<Complaint>> cb) {
        final DataCallback<List<Complaint>> callback = cb;
        query.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot snapshot) {
                        callback.onSuccess(snapshot.toObjects(Complaint.class));
                    }
                })
                .addOnFailureListener(failure(cb));
    }

    // BaseRepository.currentUserId is still a skeleton, so the uid is read here.
    @Nullable
    private String signedInUserId() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            return null;
        }
        return user.getUid();
    }

    private OnSuccessListener<Void> voidSuccess(DataCallback<Void> cb) {
        final DataCallback<Void> callback = cb;
        return new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void result) {
                callback.onSuccess(null);
            }
        };
    }

    private OnFailureListener failure(DataCallback<?> cb) {
        final DataCallback<?> callback = cb;
        return new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception error) {
                Log.e(TAG, "Firestore call failed", error);
                callback.onError(ERROR_GENERIC);
            }
        };
    }
}
