package com.nhom3.pubgapp.data;

import com.nhom3.pubgapp.model.Report;
import com.nhom3.pubgapp.model.enums.ReportStatus;
import com.nhom3.pubgapp.model.enums.ReportTarget;
import java.io.File;
import java.util.List;

// Skeleton: the module owner fills each body as the issue assigns it.
public class ReportRepository extends BaseRepository {

    public void create(Report report, DataCallback<Void> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void cancel(String reportId, DataCallback<Void> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void getMyReports(String uid, DataCallback<List<Report>> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public File exportCsv(List<Report> reports) {
        throw new UnsupportedOperationException("NOT_READY");
    }

    public void getByStatus(ReportStatus status, ReportTarget targetType, DataCallback<List<Report>> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void countByTarget(String targetId, DataCallback<Integer> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void closeByTarget(String targetId, boolean dismissed, String adminId, DataCallback<Void> cb) {
        cb.onError(DataCallback.NOT_READY);
    }
}
