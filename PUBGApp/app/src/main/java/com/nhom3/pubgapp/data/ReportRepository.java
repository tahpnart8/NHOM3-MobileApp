package com.nhom3.pubgapp.data;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.AggregateQuerySnapshot;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.nhom3.pubgapp.model.Report;
import com.nhom3.pubgapp.model.enums.ReportStatus;
import com.nhom3.pubgapp.model.enums.ReportTarget;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

// Reports of listings, users and messages (firestore-schema.md, reports; security-rules.md section 18).
public class ReportRepository extends BaseRepository {

    private static final String TAG = "ReportRepository";
    private static final String COLLECTION = "reports";
    private static final String FIELD_REPORTER_ID = "reporterId";
    private static final String FIELD_TARGET_TYPE = "targetType";
    private static final String FIELD_TARGET_ID = "targetId";
    private static final String FIELD_REASON = "reason";
    private static final String FIELD_DESCRIPTION = "description";
    private static final String FIELD_STATUS = "status";
    private static final String FIELD_EVIDENCE_IMAGE_IDS = "evidenceImageIds";
    private static final String FIELD_CONVERSATION_ID = "conversationId";
    private static final String FIELD_HANDLED_BY = "handledBy";
    private static final String FIELD_CREATED_AT = "createdAt";
    private static final String FIELD_UPDATED_AT = "updatedAt";

    private static final int MAX_EVIDENCE_IMAGES = 3;
    private static final int MAX_DESCRIPTION_LENGTH = 1000;
    private static final String EXPORT_FOLDER = "exports";
    private static final String EXPORT_FILE_PREFIX = "bao-cao-";
    private static final String CSV_HEADER =
            "Ngày gửi,Loại đối tượng,Mã đối tượng,Lý do,Trạng thái,Mô tả";

    private static final String ERROR_NOT_SIGNED_IN = "Bạn cần đăng nhập để thực hiện thao tác này.";
    private static final String ERROR_INVALID_REPORT =
            "Báo cáo chưa hợp lệ, vui lòng kiểm tra lại lý do, mô tả và ảnh.";
    private static final String ERROR_NOT_FOUND = "Không tìm thấy báo cáo.";
    private static final String ERROR_NOT_OPEN = "Báo cáo này đã được xử lý hoặc đã hủy.";
    private static final String ERROR_GENERIC = "Không kết nối được máy chủ, vui lòng thử lại.";

    public void create(Report report, DataCallback<Void> cb) {
        String uid = signedInUserId();
        if (uid == null) {
            cb.onError(ERROR_NOT_SIGNED_IN);
            return;
        }
        if (!isValidNewReport(report)) {
            cb.onError(ERROR_INVALID_REPORT);
            return;
        }
        DocumentReference reference = db.collection(COLLECTION).document();
        reference.set(buildNewReport(report, uid))
                .addOnSuccessListener(voidSuccess(cb))
                .addOnFailureListener(failure(cb));
    }

    public void cancel(String reportId, DataCallback<Void> cb) {
        final DataCallback<Void> callback = cb;
        final DocumentReference reference = db.collection(COLLECTION).document(reportId);
        reference.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot snapshot) {
                        writeCancel(reference, snapshot, callback);
                    }
                })
                .addOnFailureListener(failure(cb));
    }

    public void getMyReports(String uid, DataCallback<List<Report>> cb) {
        Query query = db.collection(COLLECTION)
                .whereEqualTo(FIELD_REPORTER_ID, uid)
                .orderBy(FIELD_CREATED_AT, Query.Direction.DESCENDING);
        runListQuery(query, cb);
    }

    // Runs on a background thread; returns null when the file could not be written.
    @Nullable
    public File exportCsv(List<Report> reports) {
        File folder = new File(FirebaseApp.getInstance().getApplicationContext().getCacheDir(), EXPORT_FOLDER);
        if (!folder.exists() && !folder.mkdirs()) {
            Log.e(TAG, "Cannot create the export folder");
            return null;
        }
        File file = new File(folder, EXPORT_FILE_PREFIX + System.currentTimeMillis() + ".csv");
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            writer.write(CSV_HEADER);
            writer.write("\n");
            for (Report report : reports) {
                writer.write(toCsvRow(report));
                writer.write("\n");
            }
            return file;
        } catch (IOException error) {
            Log.e(TAG, "Cannot write the report export", error);
            return null;
        }
    }

    public void getByStatus(ReportStatus status, ReportTarget targetType, DataCallback<List<Report>> cb) {
        Query query = db.collection(COLLECTION).whereEqualTo(FIELD_STATUS, status.name());
        if (targetType != null) {
            query = query.whereEqualTo(FIELD_TARGET_TYPE, targetType.name());
        }
        query = query.orderBy(FIELD_CREATED_AT, Query.Direction.ASCENDING);
        runListQuery(query, cb);
    }

    public void countByTarget(String targetId, DataCallback<Integer> cb) {
        final DataCallback<Integer> callback = cb;
        db.collection(COLLECTION)
                .whereEqualTo(FIELD_TARGET_ID, targetId)
                .whereEqualTo(FIELD_STATUS, ReportStatus.OPEN.name())
                .count()
                .get(AggregateSource.SERVER)
                .addOnSuccessListener(new OnSuccessListener<AggregateQuerySnapshot>() {
                    @Override
                    public void onSuccess(AggregateQuerySnapshot snapshot) {
                        callback.onSuccess((int) snapshot.getCount());
                    }
                })
                .addOnFailureListener(failure(cb));
    }

    public void closeByTarget(String targetId, boolean dismissed, String adminId, DataCallback<Void> cb) {
        final boolean isDismissed = dismissed;
        final String admin = adminId;
        final DataCallback<Void> callback = cb;
        db.collection(COLLECTION)
                .whereEqualTo(FIELD_TARGET_ID, targetId)
                .whereEqualTo(FIELD_STATUS, ReportStatus.OPEN.name())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot snapshot) {
                        writeClose(snapshot, isDismissed, admin, callback);
                    }
                })
                .addOnFailureListener(failure(cb));
    }

    private void writeCancel(DocumentReference reference, DocumentSnapshot snapshot, DataCallback<Void> cb) {
        Report report = snapshot.toObject(Report.class);
        if (report == null) {
            cb.onError(ERROR_NOT_FOUND);
            return;
        }
        if (report.getStatus() != ReportStatus.OPEN) {
            cb.onError(ERROR_NOT_OPEN);
            return;
        }
        report.cancel();
        Map<String, Object> changes = new HashMap<>();
        changes.put(FIELD_STATUS, report.getStatus().name());
        changes.put(FIELD_UPDATED_AT, FieldValue.serverTimestamp());
        reference.update(changes)
                .addOnSuccessListener(voidSuccess(cb))
                .addOnFailureListener(failure(cb));
    }

    // Closes every OPEN report of one target in one batch, through Report.resolve or Report.dismiss.
    private void writeClose(QuerySnapshot snapshot, boolean dismissed, String adminId, DataCallback<Void> cb) {
        if (snapshot.isEmpty()) {
            cb.onSuccess(null);
            return;
        }
        WriteBatch batch = db.batch();
        for (QueryDocumentSnapshot document : snapshot) {
            Report report = document.toObject(Report.class);
            if (dismissed) {
                report.dismiss(adminId);
            } else {
                report.resolve(adminId);
            }
            Map<String, Object> changes = new HashMap<>();
            changes.put(FIELD_STATUS, report.getStatus().name());
            changes.put(FIELD_HANDLED_BY, adminId);
            changes.put(FIELD_UPDATED_AT, FieldValue.serverTimestamp());
            batch.update(document.getReference(), changes);
        }
        batch.commit()
                .addOnSuccessListener(voidSuccess(cb))
                .addOnFailureListener(failure(cb));
    }

    private void runListQuery(Query query, DataCallback<List<Report>> cb) {
        final DataCallback<List<Report>> callback = cb;
        query.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot snapshot) {
                        callback.onSuccess(snapshot.toObjects(Report.class));
                    }
                })
                .addOnFailureListener(failure(cb));
    }

    // Mirrors the create rule so a bad report fails here with a clear message, not as a permission error.
    private boolean isValidNewReport(Report report) {
        if (report == null || report.getTargetType() == null || report.getReason() == null) {
            return false;
        }
        if (report.getTargetId() == null || report.getTargetId().isEmpty()) {
            return false;
        }
        if (report.getDescription() != null && report.getDescription().length() > MAX_DESCRIPTION_LENGTH) {
            return false;
        }
        if (report.getEvidenceImageIds() != null && report.getEvidenceImageIds().size() > MAX_EVIDENCE_IMAGES) {
            return false;
        }
        boolean isMessage = report.getTargetType() == ReportTarget.MESSAGE;
        boolean hasConversation = report.getConversationId() != null && !report.getConversationId().isEmpty();
        return isMessage == hasConversation;
    }

    private Map<String, Object> buildNewReport(Report report, String uid) {
        List<String> evidence = report.getEvidenceImageIds();
        if (evidence == null) {
            evidence = new ArrayList<>();
        }
        String description = report.getDescription();
        if (description == null) {
            description = "";
        }
        Map<String, Object> data = new HashMap<>();
        data.put(FIELD_REPORTER_ID, uid);
        data.put(FIELD_TARGET_TYPE, report.getTargetType().name());
        data.put(FIELD_TARGET_ID, report.getTargetId());
        data.put(FIELD_REASON, report.getReason().name());
        data.put(FIELD_DESCRIPTION, description);
        data.put(FIELD_STATUS, ReportStatus.OPEN.name());
        data.put(FIELD_EVIDENCE_IMAGE_IDS, evidence);
        data.put(FIELD_CONVERSATION_ID, report.getConversationId());
        data.put(FIELD_HANDLED_BY, null);
        data.put(FIELD_CREATED_AT, FieldValue.serverTimestamp());
        return data;
    }

    private String toCsvRow(Report report) {
        String createdAt = "";
        if (report.getCreatedAt() != null) {
            Date date = report.getCreatedAt().toDate();
            createdAt = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(date);
        }
        StringBuilder row = new StringBuilder();
        row.append(csvCell(createdAt)).append(',');
        row.append(csvCell(enumName(report.getTargetType()))).append(',');
        row.append(csvCell(report.getTargetId())).append(',');
        row.append(csvCell(enumName(report.getReason()))).append(',');
        row.append(csvCell(enumName(report.getStatus()))).append(',');
        row.append(csvCell(report.getDescription()));
        return row.toString();
    }

    private String enumName(@Nullable Enum<?> value) {
        if (value == null) {
            return "";
        }
        return value.name();
    }

    // Quotes every cell and doubles inner quotes, so commas and line breaks in a description stay in one cell.
    private String csvCell(@Nullable String value) {
        if (value == null) {
            return "\"\"";
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
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
