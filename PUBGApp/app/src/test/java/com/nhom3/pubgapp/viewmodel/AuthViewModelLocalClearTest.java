package com.nhom3.pubgapp.viewmodel;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class AuthViewModelLocalClearTest {

    private static final long SLOW_CLEAR_MS = 200L;
    private static final long WAIT_SECONDS = 5L;

    @Test
    public void queueAfterLocalClear_runsOnlyAfterEarlierClearFinished() throws InterruptedException {
        final AtomicBoolean clearFinished = new AtomicBoolean(false);
        final AtomicBoolean sawClearFinished = new AtomicBoolean(false);
        final CountDownLatch nextRan = new CountDownLatch(1);

        // Stands in for a slow Room clear queued by logout.
        AuthViewModel.queueAfterLocalClear(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(SLOW_CLEAR_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                clearFinished.set(true);
            }
        });
        // Stands in for the next sign-in starting its session.
        AuthViewModel.queueAfterLocalClear(new Runnable() {
            @Override
            public void run() {
                sawClearFinished.set(clearFinished.get());
                nextRan.countDown();
            }
        });

        assertTrue(nextRan.await(WAIT_SECONDS, TimeUnit.SECONDS));
        assertTrue(sawClearFinished.get());
    }
}
