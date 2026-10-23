package com.nhom3.pubgapp.ui.common;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Shows a Base64 image in an ImageView, decoding off the main thread. Screens must use this, not their own decode.
public final class ImageLoader {

    private static final String LOG_TAG = "ImageLoader";
    private static final int DECODE_THREADS = 2;
    // Used when the view has no size yet, as in a ViewHolder bound before its first layout.
    private static final int DEFAULT_TARGET_SIZE_PX = 480;
    private static final int MAX_TARGET_SIZE_PX = 1024;

    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(DECODE_THREADS);
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    private ImageLoader() {
    }

    /** Placeholder first, then the image or the fallback. A null or empty string only shows the placeholder. */
    public static void load(@Nullable String base64, @NonNull ImageView imageView,
                            @DrawableRes int placeholderRes, @DrawableRes int fallbackRes) {
        // The tag marks which image this view wants now; a recycled view gets a new tag and drops stale results.
        imageView.setTag(base64);
        imageView.setImageResource(placeholderRes);
        if (base64 == null || base64.isEmpty()) {
            return;
        }
        int targetSize = chooseTargetSize(imageView);
        EXECUTOR.execute(new DecodeTask(base64, imageView, targetSize, fallbackRes));
    }

    private static int chooseTargetSize(@NonNull ImageView imageView) {
        int viewSize = Math.max(imageView.getWidth(), imageView.getHeight());
        if (viewSize <= 0) {
            return DEFAULT_TARGET_SIZE_PX;
        }
        return Math.min(viewSize, MAX_TARGET_SIZE_PX);
    }

    @Nullable
    private static Bitmap decode(@NonNull String base64, int targetSize) {
        try {
            byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(bytes, 0, bytes.length, bounds);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = calculateSampleSize(bounds.outWidth, bounds.outHeight, targetSize);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
        } catch (IllegalArgumentException e) {
            Log.w(LOG_TAG, "Invalid Base64 image", e);
            return null;
        } catch (OutOfMemoryError e) {
            Log.w(LOG_TAG, "Not enough memory to decode image", e);
            return null;
        }
    }

    /** Largest power of two that keeps both sides at or above the target size. */
    private static int calculateSampleSize(int width, int height, int targetSize) {
        int sampleSize = 1;
        if (width <= 0 || height <= 0) {
            return sampleSize;
        }
        while (width / (sampleSize * 2) >= targetSize && height / (sampleSize * 2) >= targetSize) {
            sampleSize = sampleSize * 2;
        }
        return sampleSize;
    }

    private static final class DecodeTask implements Runnable {
        private final String base64;
        private final ImageView imageView;
        private final int targetSize;
        private final int fallbackRes;

        DecodeTask(@NonNull String base64, @NonNull ImageView imageView, int targetSize, int fallbackRes) {
            this.base64 = base64;
            this.imageView = imageView;
            this.targetSize = targetSize;
            this.fallbackRes = fallbackRes;
        }

        @Override
        public void run() {
            Bitmap bitmap = decode(base64, targetSize);
            MAIN_HANDLER.post(new ShowTask(base64, imageView, bitmap, fallbackRes));
        }
    }

    private static final class ShowTask implements Runnable {
        private final String base64;
        private final ImageView imageView;
        private final Bitmap bitmap;
        private final int fallbackRes;

        ShowTask(@NonNull String base64, @NonNull ImageView imageView, @Nullable Bitmap bitmap, int fallbackRes) {
            this.base64 = base64;
            this.imageView = imageView;
            this.bitmap = bitmap;
            this.fallbackRes = fallbackRes;
        }

        @Override
        public void run() {
            if (!base64.equals(imageView.getTag())) {
                return;
            }
            if (bitmap == null) {
                imageView.setImageResource(fallbackRes);
                return;
            }
            imageView.setImageBitmap(bitmap);
        }
    }
}
