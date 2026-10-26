package com.nhom3.pubgapp.util;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.nhom3.pubgapp.data.DataCallback;
import com.nhom3.pubgapp.data.DealRepository;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/** Periodic job that releases and completes due deals; it may call DealRepository. */
public class AutoReleaseWorker extends Worker {

    public static final String WORK_NAME = "auto_release_deals";
    private static final String TAG = "AutoReleaseWorker";
    private static final long TIMEOUT_SECONDS = 30L;

    public AutoReleaseWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    public static boolean shouldRun(boolean hasUser, boolean isOnline) {
        return hasUser && isOnline;
    }

    @NonNull
    @Override
    public Result doWork() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        boolean hasUser = user != null;
        boolean online = NetworkMonitor.getInstance().isOnline();

        if (!hasUser) {
            return Result.success();
        }
        if (!online) {
            return Result.retry();
        }

        final DealRepository dealRepository = new DealRepository();
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicBoolean success = new AtomicBoolean(true);

        dealRepository.releaseDueDeals(new DataCallback<Integer>() {
            @Override
            public void onSuccess(Integer result) {
                dealRepository.completeDueDeals(new DataCallback<Integer>() {
                    @Override
                    public void onSuccess(Integer res2) {
                        dealRepository.expirePendingDeals(new DataCallback<Integer>() {
                            @Override
                            public void onSuccess(Integer res3) {
                                latch.countDown();
                            }

                            @Override
                            public void onError(String message) {
                                Log.w(TAG, "expirePendingDeals failed: " + message);
                                success.set(false);
                                latch.countDown();
                            }
                        });
                    }

                    @Override
                    public void onError(String message) {
                        Log.w(TAG, "completeDueDeals failed: " + message);
                        success.set(false);
                        latch.countDown();
                    }
                });
            }

            @Override
            public void onError(String message) {
                Log.w(TAG, "releaseDueDeals failed: " + message);
                success.set(false);
                latch.countDown();
            }
        });

        try {
            boolean completed = latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!completed) {
                Log.w(TAG, "AutoReleaseWorker timed out waiting for repository callbacks");
                return Result.retry();
            }
            if (!success.get()) {
                return Result.retry();
            }
            return Result.success();
        } catch (InterruptedException e) {
            Log.w(TAG, "AutoReleaseWorker interrupted", e);
            Thread.currentThread().interrupt();
            return Result.retry();
        }
    }
}
