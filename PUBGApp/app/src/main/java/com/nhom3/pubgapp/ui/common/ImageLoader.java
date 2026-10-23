package com.nhom3.pubgapp.ui.common;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Shows an image in an ImageView, decoding off the main thread. Screens must use this, not their own decode.
// One overload per image source; which to call for what is in workflow/memory/code/ui.md.
public final class ImageLoader {

    private static final String LOG_TAG = "ImageLoader";
    private static final int DECODE_THREADS = 2;
    // Used when the view has no size yet, as in a ViewHolder bound before its first layout.
    private static final int DEFAULT_TARGET_SIZE_PX = 480;
    private static final int MAX_TARGET_SIZE_PX = 1024;

    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(DECODE_THREADS);
    // Built on first use, not in a static field: touching Looper while the class loads breaks JVM unit tests.
    private static Handler mainHandler;

    private ImageLoader() {
    }

    private static synchronized Handler mainHandler() {
        if (mainHandler == null) {
            mainHandler = new Handler(Looper.getMainLooper());
        }
        return mainHandler;
    }

    /** Placeholder first, then the image or the fallback. A null or empty string only shows the placeholder. */
    public static void load(@Nullable String base64, @NonNull ImageView imageView,
                            @DrawableRes int placeholderRes, @DrawableRes int fallbackRes) {
        if (base64 == null || base64.isEmpty()) {
            showPlaceholderOnly(imageView, placeholderRes);
            return;
        }
        start(new Base64Source(base64), base64, imageView, placeholderRes, fallbackRes);
    }

    /** For the bytes MediaRepository.getImage returns. Pass the id those bytes were fetched for: it is the guard. */
    public static void load(@Nullable byte[] bytes, @Nullable String imageId, @NonNull ImageView imageView,
                            @DrawableRes int placeholderRes, @DrawableRes int fallbackRes) {
        if (bytes == null || bytes.length == 0 || imageId == null || imageId.isEmpty()) {
            showPlaceholderOnly(imageView, placeholderRes);
            return;
        }
        start(new BytesSource(bytes), imageId, imageView, placeholderRes, fallbackRes);
    }

    /** For an image the user just picked, before it reaches Firestore. The Uri itself is the recycle guard. */
    public static void load(@Nullable Uri uri, @NonNull ImageView imageView,
                            @DrawableRes int placeholderRes, @DrawableRes int fallbackRes) {
        if (uri == null) {
            showPlaceholderOnly(imageView, placeholderRes);
            return;
        }
        Context context = imageView.getContext().getApplicationContext();
        start(new UriSource(context.getContentResolver(), uri), uri.toString(),
                imageView, placeholderRes, fallbackRes);
    }

    // The tag marks which image this view wants now; a recycled view gets a new tag and drops stale results.
    private static void start(@NonNull Source source, @NonNull String tag, @NonNull ImageView imageView,
                              @DrawableRes int placeholderRes, @DrawableRes int fallbackRes) {
        imageView.setTag(tag);
        imageView.setImageResource(placeholderRes);
        int targetSize = chooseTargetSize(imageView);
        EXECUTOR.execute(new DecodeTask(source, tag, imageView, targetSize, fallbackRes));
    }

    private static void showPlaceholderOnly(@NonNull ImageView imageView, @DrawableRes int placeholderRes) {
        imageView.setTag(null);
        imageView.setImageResource(placeholderRes);
    }

    private static int chooseTargetSize(@NonNull ImageView imageView) {
        int viewSize = Math.max(imageView.getWidth(), imageView.getHeight());
        if (viewSize <= 0) {
            return DEFAULT_TARGET_SIZE_PX;
        }
        return Math.min(viewSize, MAX_TARGET_SIZE_PX);
    }

    /** Two passes over the same bytes: bounds only, then the real decode shrunk to the target size. */
    @Nullable
    private static Bitmap decodeBytes(@NonNull byte[] bytes, int targetSize) {
        try {
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(bytes, 0, bytes.length, bounds);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = calculateSampleSize(bounds.outWidth, bounds.outHeight, targetSize);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
        } catch (IllegalArgumentException e) {
            Log.w(LOG_TAG, "Invalid image bytes", e);
            return null;
        } catch (OutOfMemoryError e) {
            Log.w(LOG_TAG, "Not enough memory to decode image", e);
            return null;
        }
    }

    /** Largest power of two that keeps both sides at or above the target size. */
    static int calculateSampleSize(int width, int height, int targetSize) {
        int sampleSize = 1;
        // A zero target would never end the loop, because integer division stops at 0 and 0 >= 0 stays true.
        if (width <= 0 || height <= 0 || targetSize <= 0) {
            return sampleSize;
        }
        while (width / (sampleSize * 2) >= targetSize && height / (sampleSize * 2) >= targetSize) {
            sampleSize = sampleSize * 2;
        }
        return sampleSize;
    }

    // One implementation per place a screen can get an image from. Each decodes off the main thread.
    private interface Source {
        @Nullable
        Bitmap decode(int targetSize);
    }

    private static final class Base64Source implements Source {
        private final String base64;

        Base64Source(@NonNull String base64) {
            this.base64 = base64;
        }

        @Override
        @Nullable
        public Bitmap decode(int targetSize) {
            try {
                return decodeBytes(Base64.decode(base64, Base64.DEFAULT), targetSize);
            } catch (IllegalArgumentException e) {
                Log.w(LOG_TAG, "Invalid Base64 image", e);
                return null;
            }
        }
    }

    private static final class BytesSource implements Source {
        private final byte[] bytes;

        BytesSource(@NonNull byte[] bytes) {
            this.bytes = bytes;
        }

        @Override
        @Nullable
        public Bitmap decode(int targetSize) {
            return decodeBytes(bytes, targetSize);
        }
    }

    // Opens the picked image twice, because an InputStream cannot be rewound after the bounds pass.
    private static final class UriSource implements Source {
        private final ContentResolver resolver;
        private final Uri uri;

        UriSource(@NonNull ContentResolver resolver, @NonNull Uri uri) {
            this.resolver = resolver;
            this.uri = uri;
        }

        @Override
        @Nullable
        public Bitmap decode(int targetSize) {
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            if (!readBounds(bounds)) {
                return null;
            }
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = calculateSampleSize(bounds.outWidth, bounds.outHeight, targetSize);
            return readBitmap(options);
        }

        private boolean readBounds(@NonNull BitmapFactory.Options options) {
            InputStream stream = null;
            try {
                stream = resolver.openInputStream(uri);
                if (stream == null) {
                    return false;
                }
                BitmapFactory.decodeStream(stream, null, options);
                return options.outWidth > 0 && options.outHeight > 0;
            } catch (IOException e) {
                Log.w(LOG_TAG, "Cannot read picked image", e);
                return false;
            } catch (SecurityException e) {
                Log.w(LOG_TAG, "No permission for picked image", e);
                return false;
            } finally {
                closeQuietly(stream);
            }
        }

        @Nullable
        private Bitmap readBitmap(@NonNull BitmapFactory.Options options) {
            InputStream stream = null;
            try {
                stream = resolver.openInputStream(uri);
                if (stream == null) {
                    return null;
                }
                return BitmapFactory.decodeStream(stream, null, options);
            } catch (IOException e) {
                Log.w(LOG_TAG, "Cannot read picked image", e);
                return null;
            } catch (SecurityException e) {
                Log.w(LOG_TAG, "No permission for picked image", e);
                return null;
            } catch (OutOfMemoryError e) {
                Log.w(LOG_TAG, "Not enough memory to decode picked image", e);
                return null;
            } finally {
                closeQuietly(stream);
            }
        }

        private void closeQuietly(@Nullable InputStream stream) {
            if (stream == null) {
                return;
            }
            try {
                stream.close();
            } catch (IOException e) {
                Log.w(LOG_TAG, "Cannot close picked image stream", e);
            }
        }
    }

    private static final class DecodeTask implements Runnable {
        private final Source source;
        private final String tag;
        private final ImageView imageView;
        private final int targetSize;
        private final int fallbackRes;

        DecodeTask(@NonNull Source source, @NonNull String tag, @NonNull ImageView imageView,
                   int targetSize, int fallbackRes) {
            this.source = source;
            this.tag = tag;
            this.imageView = imageView;
            this.targetSize = targetSize;
            this.fallbackRes = fallbackRes;
        }

        @Override
        public void run() {
            Bitmap bitmap = source.decode(targetSize);
            mainHandler().post(new ShowTask(tag, imageView, bitmap, fallbackRes));
        }
    }

    private static final class ShowTask implements Runnable {
        private final String tag;
        private final ImageView imageView;
        private final Bitmap bitmap;
        private final int fallbackRes;

        ShowTask(@NonNull String tag, @NonNull ImageView imageView, @Nullable Bitmap bitmap, int fallbackRes) {
            this.tag = tag;
            this.imageView = imageView;
            this.bitmap = bitmap;
            this.fallbackRes = fallbackRes;
        }

        @Override
        public void run() {
            if (!tag.equals(imageView.getTag())) {
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
