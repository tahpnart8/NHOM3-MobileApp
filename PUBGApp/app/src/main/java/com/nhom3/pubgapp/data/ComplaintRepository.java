package com.nhom3.pubgapp.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.nhom3.pubgapp.model.AppNotification;
import com.nhom3.pubgapp.model.Complaint;
import com.nhom3.pubgapp.model.Deal;
import com.nhom3.pubgapp.model.Listing;
import com.nhom3.pubgapp.model.Payment;
import com.nhom3.pubgapp.model.StatusChange;
import com.nhom3.pubgapp.model.enums.ComplaintDecision;
import com.nhom3.pubgapp.model.enums.ComplaintStatus;
import com.nhom3.pubgapp.model.enums.DealStatus;
import com.nhom3.pubgapp.model.enums.DeliveryMethod;
import com.nhom3.pubgapp.model.enums.ListingStatus;
import com.nhom3.pubgapp.model.enums.NotificationType;
import com.nhom3.pubgapp.model.enums.Priority;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Complaints about a deal; the document id is the dealId (firestore-schema.md, complaints and adminNote).
public class ComplaintRepository extends BaseRepository {

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
    private static final String DEAL_COLLECTION = "deals";
    private static final String PAYMENT_COLLECTION = "payments";
    private static final String LISTING_COLLECTION = "listings";
    private static final String FIELD_DEAL_ID = "dealId";
    private static final String FIELD_REASON = "reason";
    private static final String FIELD_DESCRIPTION = "description";
    private static final String FIELD_DECISION = "decision";
    private static final String FIELD_EVIDENCE_REQUEST = "evidenceRequest";
    private static final String FIELD_RESOLUTION_MESSAGE = "resolutionMessage";
    private static final String FIELD_HANDLED_BY = "handledBy";
    private static final String FIELD_STATUS_HISTORY = "statusHistory";
    private static final String FIELD_RECEIVED_AT = "receivedAt";
    private static final String FIELD_RELEASED_AT = "releasedAt";
    private static final String FIELD_RESERVED_DEAL_ID = "reservedDealId";
    private static final int MAX_EVIDENCE_IMAGES = 5;
    private static final int MAX_DESCRIPTION_LENGTH = 2000;

    private static final String ERROR_NOT_SIGNED_IN = "Bạn cần đăng nhập để thực hiện thao tác này.";
    private static final String ERROR_NOT_FOUND = "Không tìm thấy đơn khiếu nại.";
    private static final String ERROR_NOT_NEEDS_EVIDENCE =
            "Đơn khiếu nại này không cần bổ sung bằng chứng.";
    private static final String ERROR_INVALID_EVIDENCE =
            "Mỗi lần bổ sung từ 1 đến 5 ảnh, tổng không quá 10 ảnh.";
    private static final String ERROR_INVALID_COMPLAINT =
            "Đơn khiếu nại cần lý do, mô tả không quá 2000 ký tự và từ 1 đến 5 ảnh.";
    private static final String ERROR_DEAL_NOT_FOUND = "Không tìm thấy giao dịch.";
    private static final String ERROR_CANNOT_COMPLAIN = "Giao dịch này không còn mở khiếu nại được.";
    private static final String ERROR_ALREADY_COMPLAINED =
            "Giao dịch này đã từng có khiếu nại, không mở lại được.";
    private static final String ERROR_NOT_COMPLAINANT = "Chỉ người gửi khiếu nại mới rút được đơn.";
    private static final String ERROR_CLOSED = "Đơn khiếu nại này đã đóng.";
    private static final String ERROR_MESSAGE_REQUIRED = "Vui lòng nhập lời giải thích cho quyết định.";

    private final NotificationRepository notificationRepository = new NotificationRepository();

    public void create(Complaint complaint, DataCallback<Void> cb) {
        final String uid = currentUserId();
        if (uid.isEmpty()) {
            cb.onError(ERROR_NOT_SIGNED_IN);
            return;
        }
        if (!isValidNewComplaint(complaint)) {
            cb.onError(ERROR_INVALID_COMPLAINT);
            return;
        }
        final Complaint request = complaint;
        final DataCallback<Void> callback = cb;
        db.collection(COLLECTION).document(complaint.getDealId()).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot snapshot) {
                        // The document id is the dealId, so an existing one (even WITHDRAWN) means no new complaint.
                        if (snapshot.exists()) {
                            callback.onError(ERROR_ALREADY_COMPLAINED);
                            return;
                        }
                        loadDealForNewComplaint(request, uid, callback);
                    }
                })
                .addOnFailureListener(failure(cb));
    }

    public void getMyComplaints(String uid, DataCallback<List<Complaint>> cb) {
        Query query = db.collection(COLLECTION)
                .whereEqualTo(FIELD_COMPLAINANT_ID, uid)
                .orderBy(FIELD_CREATED_AT, Query.Direction.DESCENDING);
        runListQuery(query, cb);
    }

    public void withdraw(String id, DataCallback<Void> cb) {
        final String uid = currentUserId();
        if (uid.isEmpty()) {
            cb.onError(ERROR_NOT_SIGNED_IN);
            return;
        }
        final DataCallback<Void> callback = cb;
        db.collection(COLLECTION).document(id).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot snapshot) {
                        Complaint complaint = snapshot.toObject(Complaint.class);
                        if (complaint == null) {
                            callback.onError(ERROR_NOT_FOUND);
                            return;
                        }
                        // Early check for a Vietnamese message; the Security Rules still decide.
                        if (!uid.equals(complaint.getComplainantId())) {
                            callback.onError(ERROR_NOT_COMPLAINANT);
                            return;
                        }
                        if (!complaint.isOpen()) {
                            callback.onError(ERROR_CLOSED);
                            return;
                        }
                        complaint.withdraw();
                        loadDealForWithdraw(snapshot.getReference(), complaint, uid, callback);
                    }
                })
                .addOnFailureListener(failure(cb));
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
        final String uid = currentUserId();
        if (uid.isEmpty()) {
            cb.onError(ERROR_NOT_SIGNED_IN);
            return;
        }
        if (decision == null || message == null || message.trim().isEmpty()) {
            cb.onError(ERROR_MESSAGE_REQUIRED);
            return;
        }
        final ComplaintDecision chosen = decision;
        final String text = message.trim();
        final DataCallback<Void> callback = cb;
        db.collection(COLLECTION).document(id).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot snapshot) {
                        applyDecision(snapshot, chosen, text, uid, callback);
                    }
                })
                .addOnFailureListener(failure(cb));
    }

    public void saveAdminNote(String id, String note, DataCallback<Void> cb) {
        String uid = currentUserId();
        if (uid.isEmpty()) {
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

    private void loadDealForNewComplaint(Complaint complaint, String uid, DataCallback<Void> cb) {
        final Complaint request = complaint;
        final String userId = uid;
        final DataCallback<Void> callback = cb;
        db.collection(DEAL_COLLECTION).document(complaint.getDealId()).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot snapshot) {
                        writeNewComplaint(snapshot, request, userId, callback);
                    }
                })
                .addOnFailureListener(failure(cb));
    }

    // Complaint OPEN and deal DISPUTED go out in one batch with the notice to the other party.
    private void writeNewComplaint(DocumentSnapshot dealSnapshot, Complaint complaint, String uid,
                                   DataCallback<Void> cb) {
        Deal deal = dealSnapshot.toObject(Deal.class);
        if (deal == null) {
            cb.onError(ERROR_DEAL_NOT_FOUND);
            return;
        }
        if (!deal.isParticipant(uid) || !deal.canMoveTo(DealStatus.DISPUTED) || isCountdownOver(deal)
                || isConfirmedWithoutMeetUp(deal)) {
            cb.onError(ERROR_CANNOT_COMPLAIN);
            return;
        }
        String dealId = dealSnapshot.getId();
        DealStatus before = deal.getStatus();
        deal.dispute();
        appendHistory(deal, before, uid, complaint.getReason().name());
        WriteBatch batch = db.batch();
        batch.set(db.collection(COLLECTION).document(dealId), buildNewComplaint(complaint, uid, dealId));
        batch.update(dealSnapshot.getReference(), dealChanges(deal));
        List<String> recipients = new ArrayList<>();
        recipients.add(otherParty(deal, uid));
        notifyAndCommit(batch, recipients, 0, NotificationType.DEAL_UPDATE, dealId, cb);
    }

    private void loadDealForWithdraw(DocumentReference complaintReference, Complaint complaint, String uid,
                                     DataCallback<Void> cb) {
        final DocumentReference complaintRef = complaintReference;
        final Complaint withdrawn = complaint;
        final String userId = uid;
        final DataCallback<Void> callback = cb;
        db.collection(DEAL_COLLECTION).document(complaint.getDealId()).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot snapshot) {
                        writeWithdraw(complaintRef, withdrawn, snapshot, userId, callback);
                    }
                })
                .addOnFailureListener(failure(cb));
    }

    // The deal goes back to the status it had before the dispute; autoReleaseAt is left as it was.
    private void writeWithdraw(DocumentReference complaintReference, Complaint complaint,
                               DocumentSnapshot dealSnapshot, String uid, DataCallback<Void> cb) {
        Deal deal = dealSnapshot.toObject(Deal.class);
        if (deal == null) {
            cb.onError(ERROR_DEAL_NOT_FOUND);
            return;
        }
        DealStatus back = deal.statusBeforeDispute();
        deal.resolveDispute(back);
        appendHistory(deal, DealStatus.DISPUTED, uid, null);
        Map<String, Object> complaintChanges = new HashMap<>();
        complaintChanges.put(FIELD_STATUS, complaint.getStatus().name());
        complaintChanges.put(FIELD_UPDATED_AT, FieldValue.serverTimestamp());
        WriteBatch batch = db.batch();
        batch.update(complaintReference, complaintChanges);
        batch.update(dealSnapshot.getReference(), dealChanges(deal));
        batch.commit()
                .addOnSuccessListener(voidSuccess(cb))
                .addOnFailureListener(failure(cb));
    }

    private void applyDecision(DocumentSnapshot snapshot, ComplaintDecision decision, String message, String uid,
                               DataCallback<Void> cb) {
        final Complaint complaint = snapshot.toObject(Complaint.class);
        if (complaint == null) {
            cb.onError(ERROR_NOT_FOUND);
            return;
        }
        try {
            complaint.decide(decision, uid);
        } catch (IllegalStateException error) {
            cb.onError(ERROR_CLOSED);
            return;
        }
        if (decision == ComplaintDecision.REQUEST_EVIDENCE) {
            complaint.setEvidenceRequest(message);
        } else {
            complaint.setResolutionMessage(message);
        }
        final DocumentReference complaintReference = snapshot.getReference();
        final String adminId = uid;
        final DataCallback<Void> callback = cb;
        db.collection(DEAL_COLLECTION).document(complaint.getDealId()).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot dealSnapshot) {
                        writeDecision(complaintReference, complaint, dealSnapshot, adminId, callback);
                    }
                })
                .addOnFailureListener(failure(cb));
    }

    // An evidence request leaves the deal DISPUTED; any other decision ends the dispute (trang-thai.md section 3).
    private void writeDecision(DocumentReference complaintReference, Complaint complaint,
                               DocumentSnapshot dealSnapshot, String adminId, DataCallback<Void> cb) {
        Deal deal = dealSnapshot.toObject(Deal.class);
        if (deal == null) {
            cb.onError(ERROR_DEAL_NOT_FOUND);
            return;
        }
        WriteBatch batch = db.batch();
        batch.update(complaintReference, decisionChanges(complaint));
        if (complaint.getDecision() == ComplaintDecision.REQUEST_EVIDENCE) {
            notifyAndCommit(batch, bothParties(deal), 0, NotificationType.COMPLAINT_RESULT,
                    dealSnapshot.getId(), cb);
            return;
        }
        DealStatus target = DealStatus.CANCELLED;
        if (complaint.getDecision() == ComplaintDecision.REJECT) {
            target = DealStatus.RECEIVED;
        }
        deal.resolveDispute(target);
        appendHistory(deal, DealStatus.DISPUTED, adminId, complaint.getDecision().name());
        Map<String, Object> changes = dealChanges(deal);
        if (target == DealStatus.RECEIVED) {
            changes.put(FIELD_RECEIVED_AT, FieldValue.serverTimestamp());
        }
        batch.update(dealSnapshot.getReference(), changes);
        settlePayment(batch, deal, dealSnapshot.getId(), cb);
    }

    // A held payment is released when the complaint is rejected, refunded otherwise.
    private void settlePayment(WriteBatch batch, Deal deal, String dealId, DataCallback<Void> cb) {
        if (!deal.needsPayment()) {
            releaseListing(batch, deal, dealId, cb);
            return;
        }
        final WriteBatch writes = batch;
        final Deal resolved = deal;
        final String id = dealId;
        final DataCallback<Void> callback = cb;
        db.collection(PAYMENT_COLLECTION).document(dealId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot snapshot) {
                        Payment payment = snapshot.toObject(Payment.class);
                        if (payment != null && payment.isHeld()) {
                            writes.update(snapshot.getReference(), paymentChanges(payment, resolved));
                        }
                        releaseListing(writes, resolved, id, callback);
                    }
                })
                .addOnFailureListener(failure(cb));
    }

    // A cancelled deal frees its listing; a listing that is gone or no longer reserved is skipped.
    private void releaseListing(WriteBatch batch, Deal deal, String dealId, DataCallback<Void> cb) {
        if (deal.getStatus() != DealStatus.CANCELLED) {
            notifyAndCommit(batch, bothParties(deal), 0, NotificationType.COMPLAINT_RESULT, dealId, cb);
            return;
        }
        final WriteBatch writes = batch;
        final Deal resolved = deal;
        final String id = dealId;
        final DataCallback<Void> callback = cb;
        db.collection(LISTING_COLLECTION).document(deal.getListingId()).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot snapshot) {
                        Listing listing = snapshot.toObject(Listing.class);
                        if (listing != null && listing.getStatus() == ListingStatus.RESERVED) {
                            listing.release();
                            writes.update(snapshot.getReference(), listingChanges(listing));
                        }
                        notifyAndCommit(writes, bothParties(resolved), 0, NotificationType.COMPLAINT_RESULT,
                                id, callback);
                    }
                })
                .addOnFailureListener(failure(cb));
    }

    // Adds one notice per recipient to the batch, then commits once so the notices and the change land together.
    private void notifyAndCommit(WriteBatch batch, List<String> recipients, int index, NotificationType type,
                                 String targetId, DataCallback<Void> cb) {
        if (index >= recipients.size()) {
            batch.commit()
                    .addOnSuccessListener(voidSuccess(cb))
                    .addOnFailureListener(failure(cb));
            return;
        }
        final WriteBatch writes = batch;
        final List<String> people = recipients;
        final int next = index + 1;
        final NotificationType noticeType = type;
        final String target = targetId;
        final DataCallback<Void> callback = cb;
        AppNotification notification = new AppNotification();
        notification.setRecipientId(recipients.get(index));
        notification.setType(type);
        notification.setTargetId(targetId);
        notificationRepository.create(notification, batch, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                notifyAndCommit(writes, people, next, noticeType, target, callback);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    private void appendHistory(Deal deal, DealStatus from, String changedBy, @Nullable String reason) {
        StatusChange change = new StatusChange();
        change.setFromStatus(from);
        change.setToStatus(deal.getStatus());
        change.setChangedBy(changedBy);
        change.setReason(reason);
        change.setChangedAt(Timestamp.now());
        List<StatusChange> history = new ArrayList<>();
        if (deal.getStatusHistory() != null) {
            history.addAll(deal.getStatusHistory());
        }
        history.add(change);
        deal.setStatusHistory(history);
    }

    private Map<String, Object> dealChanges(Deal deal) {
        Map<String, Object> changes = new HashMap<>();
        changes.put(FIELD_STATUS, deal.getStatus().name());
        changes.put(FIELD_STATUS_HISTORY, deal.getStatusHistory());
        changes.put(FIELD_UPDATED_AT, FieldValue.serverTimestamp());
        return changes;
    }

    private Map<String, Object> decisionChanges(Complaint complaint) {
        Map<String, Object> changes = new HashMap<>();
        changes.put(FIELD_STATUS, complaint.getStatus().name());
        changes.put(FIELD_DECISION, complaint.getDecision().name());
        changes.put(FIELD_HANDLED_BY, complaint.getHandledBy());
        if (complaint.getDecision() == ComplaintDecision.REQUEST_EVIDENCE) {
            changes.put(FIELD_EVIDENCE_REQUEST, complaint.getEvidenceRequest());
        } else {
            changes.put(FIELD_RESOLUTION_MESSAGE, complaint.getResolutionMessage());
        }
        changes.put(FIELD_UPDATED_AT, FieldValue.serverTimestamp());
        return changes;
    }

    private Map<String, Object> paymentChanges(Payment payment, Deal deal) {
        Map<String, Object> changes = new HashMap<>();
        if (deal.getStatus() == DealStatus.RECEIVED) {
            payment.release();
            changes.put(FIELD_RELEASED_AT, FieldValue.serverTimestamp());
        } else {
            payment.refund();
        }
        changes.put(FIELD_STATUS, payment.getStatus().name());
        changes.put(FIELD_UPDATED_AT, FieldValue.serverTimestamp());
        return changes;
    }

    private Map<String, Object> listingChanges(Listing listing) {
        Map<String, Object> changes = new HashMap<>();
        changes.put(FIELD_STATUS, listing.getStatus().name());
        changes.put(FIELD_RESERVED_DEAL_ID, listing.getReservedDealId());
        changes.put(FIELD_UPDATED_AT, FieldValue.serverTimestamp());
        return changes;
    }

    private Map<String, Object> buildNewComplaint(Complaint complaint, String uid, String dealId) {
        String description = complaint.getDescription();
        if (description == null) {
            description = "";
        }
        Map<String, Object> data = new HashMap<>();
        data.put(FIELD_COMPLAINANT_ID, uid);
        data.put(FIELD_DEAL_ID, dealId);
        data.put(FIELD_REASON, complaint.getReason().name());
        data.put(FIELD_DESCRIPTION, description);
        data.put(FIELD_EVIDENCE_IMAGE_IDS, complaint.getEvidenceImageIds());
        data.put(FIELD_STATUS, ComplaintStatus.OPEN.name());
        data.put(FIELD_PRIORITY, Priority.MEDIUM.name());
        data.put(FIELD_DECISION, null);
        data.put(FIELD_EVIDENCE_REQUEST, "");
        data.put(FIELD_RESOLUTION_MESSAGE, "");
        data.put(FIELD_HANDLED_BY, null);
        data.put(FIELD_CREATED_AT, FieldValue.serverTimestamp());
        return data;
    }

    // Mirrors the create rule so a bad complaint fails here with a clear message.
    private boolean isValidNewComplaint(Complaint complaint) {
        if (complaint == null || complaint.getReason() == null) {
            return false;
        }
        if (complaint.getDealId() == null || complaint.getDealId().isEmpty()) {
            return false;
        }
        if (complaint.getDescription() != null && complaint.getDescription().length() > MAX_DESCRIPTION_LENGTH) {
            return false;
        }
        List<String> evidence = complaint.getEvidenceImageIds();
        return evidence != null && !evidence.isEmpty() && evidence.size() <= MAX_EVIDENCE_IMAGES;
    }

    // A shipped deal can only be disputed before its countdown ends.
    private boolean isCountdownOver(Deal deal) {
        if (deal.getStatus() != DealStatus.SHIPPED || deal.getAutoReleaseAt() == null) {
            return false;
        }
        return Timestamp.now().compareTo(deal.getAutoReleaseAt()) >= 0;
    }

    // canMoveTo follows the state table only; a CONFIRMED deal is disputable only when meeting in person.
    private boolean isConfirmedWithoutMeetUp(Deal deal) {
        if (deal.getStatus() != DealStatus.CONFIRMED) {
            return false;
        }
        return deal.getDeliveryMethod() != DeliveryMethod.MEET_UP;
    }

    private String otherParty(Deal deal, String uid) {
        if (uid.equals(deal.getBuyerId())) {
            return deal.getSellerId();
        }
        return deal.getBuyerId();
    }

    private List<String> bothParties(Deal deal) {
        List<String> parties = new ArrayList<>();
        parties.add(deal.getBuyerId());
        parties.add(deal.getSellerId());
        return parties;
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
                fail(callback, error);
            }
        };
    }
}
