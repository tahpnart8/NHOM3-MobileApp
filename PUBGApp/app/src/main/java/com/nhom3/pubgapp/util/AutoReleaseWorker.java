package com.nhom3.pubgapp.util;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

/** Periodic job that releases and completes due deals; it may call DealRepository. */
public class AutoReleaseWorker extends Worker {

    public AutoReleaseWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        throw new UnsupportedOperationException("NOT_READY");
    }
}
