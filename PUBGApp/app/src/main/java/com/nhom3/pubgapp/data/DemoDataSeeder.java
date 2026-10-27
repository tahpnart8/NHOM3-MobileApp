package com.nhom3.pubgapp.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.nhom3.pubgapp.model.Category;
import com.nhom3.pubgapp.model.Listing;
import com.nhom3.pubgapp.model.enums.ImagePurpose;
import com.nhom3.pubgapp.util.SessionManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Debug-only demo data: writes through the repositories, every document id starts with "demo_".
public class DemoDataSeeder {

    /** Called after each listing is written or hidden, with how many are done out of how many. */
    public interface ProgressListener {
        void onProgress(int done, int total);
    }

    private interface Step {
        void run();
    }

    private static final String TAG = "DemoDataSeeder";
    private static final String COLLECTION_CATEGORIES = "categories";
    private static final String COLLECTION_LISTINGS = "listings";
    private static final String STATUS_FIELD = "status";
    private static final String STATUS_AVAILABLE = "AVAILABLE";
    private static final String STATUS_HIDDEN = "HIDDEN";
    private static final String PREFIX_END = "";
    private static final String IMAGE_FOLDER = "demo_seed";
    private static final String MESSAGE_NOT_ADMIN =
            "Chỉ tài khoản admin mới tạo hoặc xóa được dữ liệu mẫu.";
    private static final String MESSAGE_BUSY = "Đang xử lý dữ liệu mẫu, hãy đợi xong.";
    private static final String MESSAGE_IMAGE_FAILED = "Không tạo được ảnh mẫu trên máy.";
    private static final int IMAGE_WIDTH = 640;
    private static final int IMAGE_HEIGHT = 480;
    private static final int JPEG_QUALITY = 85;
    private static final float IMAGE_TEXT_SIZE = 72f;
    private static final int[] IMAGE_COLORS = {
        0xFF1E88E5, 0xFF43A047, 0xFFF4511E, 0xFF8E24AA, 0xFF00897B, 0xFFFB8C00
    };

    private static volatile boolean running = false;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final ListingRepository listingRepository = new ListingRepository();
    private final MediaRepository mediaRepository = new MediaRepository();
    private final ExecutorService imageExecutor = Executors.newSingleThreadExecutor();

    public static boolean isRunning() {
        return running;
    }

    /** Creates the categories and listings that are missing and shows again those that were hidden. */
    public void seed(final ProgressListener progress, final DataCallback<Integer> cb) {
        if (!startRun(cb)) {
            return;
        }
        final Step planListings = new Step() {
            @Override
            public void run() {
                planAndWrite(progress, cb);
            }
        };
        final Step writeChildren = new Step() {
            @Override
            public void run() {
                writeMissingCategories(DemoDataBuilder.buildChildCategories(), planListings, cb);
            }
        };
        writeMissingCategories(DemoDataBuilder.buildRootCategories(), writeChildren, cb);
    }

    /** Hides every demo listing that is still on sale and deletes the demo categories. */
    public void clear(final ProgressListener progress, final DataCallback<Integer> cb) {
        if (!startRun(cb)) {
            return;
        }
        db.collection(COLLECTION_LISTINGS).orderBy(FieldPath.documentId())
                .startAt(DemoDataBuilder.LISTING_ID_PREFIX)
                .endAt(DemoDataBuilder.LISTING_ID_PREFIX + PREFIX_END).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (!task.isSuccessful()) {
                            finishError(cb, task.getException());
                            return;
                        }
                        List<String> toHide = new ArrayList<String>();
                        for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                            if (STATUS_AVAILABLE.equals(doc.getString(STATUS_FIELD))) {
                                toHide.add(doc.getId());
                            }
                        }
                        hideNext(toHide, 0, progress, cb);
                    }
                });
    }

    private boolean startRun(DataCallback<Integer> cb) {
        if (!SessionManager.getInstance().isAdmin()) {
            cb.onError(MESSAGE_NOT_ADMIN);
            return false;
        }
        if (running) {
            cb.onError(MESSAGE_BUSY);
            return false;
        }
        running = true;
        return true;
    }

    private void finishOk(DataCallback<Integer> cb, int count) {
        running = false;
        imageExecutor.shutdown();
        cb.onSuccess(count);
    }

    private void finishError(DataCallback<Integer> cb, Exception error) {
        String message = error != null && error.getMessage() != null ? error.getMessage() : "Lỗi không rõ";
        finishErrorText(cb, message);
    }

    private void finishErrorText(DataCallback<Integer> cb, String message) {
        running = false;
        imageExecutor.shutdown();
        cb.onError(message);
    }

    // Categories first: a child's rule reads its parent, so roots and children go in two commits.
    private void writeMissingCategories(final List<Category> wanted, final Step next, final DataCallback<Integer> cb) {
        db.collection(COLLECTION_CATEGORIES).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (!task.isSuccessful()) {
                    finishError(cb, task.getException());
                    return;
                }
                Map<String, Boolean> existing = new HashMap<String, Boolean>();
                for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                    existing.put(doc.getId(), Boolean.TRUE);
                }
                WriteBatch batch = db.batch();
                int missing = 0;
                for (Category category : wanted) {
                    if (!existing.containsKey(category.getId())) {
                        batch.set(db.collection(COLLECTION_CATEGORIES).document(category.getId()), category);
                        missing++;
                    }
                }
                if (missing == 0) {
                    next.run();
                    return;
                }
                commitThen(batch, next, cb);
            }
        });
    }

    private void commitThen(WriteBatch batch, final Step next, final DataCallback<Integer> cb) {
        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (!task.isSuccessful()) {
                    finishError(cb, task.getException());
                    return;
                }
                next.run();
            }
        });
    }

    private void planAndWrite(final ProgressListener progress, final DataCallback<Integer> cb) {
        db.collection(COLLECTION_LISTINGS).orderBy(FieldPath.documentId())
                .startAt(DemoDataBuilder.LISTING_ID_PREFIX)
                .endAt(DemoDataBuilder.LISTING_ID_PREFIX + PREFIX_END).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (!task.isSuccessful()) {
                            finishError(cb, task.getException());
                            return;
                        }
                        Map<String, String> statusById = new HashMap<String, String>();
                        for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                            statusById.put(doc.getId(), doc.getString(STATUS_FIELD));
                        }
                        List<String> toCreate = new ArrayList<String>();
                        List<String> toShow = new ArrayList<String>();
                        for (String id : DemoDataBuilder.allListingIds()) {
                            if (!statusById.containsKey(id)) {
                                toCreate.add(id);
                            } else if (STATUS_HIDDEN.equals(statusById.get(id))) {
                                toShow.add(id);
                            }
                        }
                        writeListings(toCreate, toShow, progress, cb);
                    }
                });
    }

    private void writeListings(final List<String> toCreate, final List<String> toShow,
            final ProgressListener progress, final DataCallback<Integer> cb) {
        final int total = toCreate.size() + toShow.size();
        if (total == 0) {
            finishOk(cb, 0);
            return;
        }
        final Step showHidden = new Step() {
            @Override
            public void run() {
                showNext(toShow, 0, toCreate.size(), total, progress, cb);
            }
        };
        if (toCreate.isEmpty()) {
            showHidden.run();
            return;
        }
        createImages(new ImagesReady() {
            @Override
            public void onReady(List<String> imageIds, List<String> thumbs) {
                createNext(toCreate, DemoDataBuilder.buildListings(imageIds, thumbs), 0, total,
                        showHidden, progress, cb);
            }
        }, cb);
    }

    private interface ImagesReady {
        void onReady(List<String> imageIds, List<String> thumbs);
    }

    private void createNext(final List<String> toCreate, final List<Listing> all, final int index, final int total,
            final Step afterAll, final ProgressListener progress, final DataCallback<Integer> cb) {
        if (index >= toCreate.size()) {
            afterAll.run();
            return;
        }
        Listing listing = findById(all, toCreate.get(index));
        listingRepository.create(listing, new DataCallback<String>() {
            @Override
            public void onSuccess(String id) {
                progress.onProgress(index + 1, total);
                createNext(toCreate, all, index + 1, total, afterAll, progress, cb);
            }

            @Override
            public void onError(String message) {
                finishErrorText(cb, message);
            }
        });
    }

    private Listing findById(List<Listing> listings, String id) {
        for (Listing listing : listings) {
            if (id.equals(listing.getId())) {
                return listing;
            }
        }
        return null;
    }

    private void showNext(final List<String> toShow, final int index, final int created, final int total,
            final ProgressListener progress, final DataCallback<Integer> cb) {
        if (index >= toShow.size()) {
            finishOk(cb, total);
            return;
        }
        listingRepository.setHidden(toShow.get(index), false, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                progress.onProgress(created + index + 1, total);
                showNext(toShow, index + 1, created, total, progress, cb);
            }

            @Override
            public void onError(String message) {
                finishErrorText(cb, message);
            }
        });
    }

    private void hideNext(final List<String> toHide, final int index, final ProgressListener progress,
            final DataCallback<Integer> cb) {
        if (index >= toHide.size()) {
            deleteCategories(toHide.size(), cb);
            return;
        }
        listingRepository.setHidden(toHide.get(index), true, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                progress.onProgress(index + 1, toHide.size());
                hideNext(toHide, index + 1, progress, cb);
            }

            @Override
            public void onError(String message) {
                finishErrorText(cb, message);
            }
        });
    }

    private void deleteCategories(final int hiddenCount, final DataCallback<Integer> cb) {
        db.collection(COLLECTION_CATEGORIES).orderBy(FieldPath.documentId())
                .startAt(DemoDataBuilder.CATEGORY_ID_PREFIX)
                .endAt(DemoDataBuilder.CATEGORY_ID_PREFIX + PREFIX_END).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (!task.isSuccessful()) {
                            finishError(cb, task.getException());
                            return;
                        }
                        WriteBatch batch = db.batch();
                        for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                            batch.delete(doc.getReference());
                        }
                        commitThen(batch, new Step() {
                            @Override
                            public void run() {
                                finishOk(cb, hiddenCount);
                            }
                        }, cb);
                    }
                });
    }

    // Sample images are drawn on the device and saved through MediaRepository (Base64 in Firestore, no Storage).
    private void createImages(final ImagesReady ready, final DataCallback<Integer> cb) {
        imageExecutor.execute(new Runnable() {
            @Override
            public void run() {
                List<Uri> files = writeSampleFiles();
                if (files == null) {
                    finishErrorText(cb, MESSAGE_IMAGE_FAILED);
                    return;
                }
                saveNextImage(files, 0, new ArrayList<String>(), new ArrayList<String>(), ready, cb);
            }
        });
    }

    private void saveNextImage(final List<Uri> files, final int index, final List<String> imageIds,
            final List<String> thumbs, final ImagesReady ready, final DataCallback<Integer> cb) {
        if (index >= files.size()) {
            ready.onReady(imageIds, thumbs);
            return;
        }
        final Uri file = files.get(index);
        mediaRepository.saveImage(file, ImagePurpose.LISTING, new DataCallback<String>() {
            @Override
            public void onSuccess(final String imageId) {
                mediaRepository.makeThumb(file, new DataCallback<String>() {
                    @Override
                    public void onSuccess(String thumb) {
                        imageIds.add(imageId);
                        thumbs.add(thumb);
                        saveNextImage(files, index + 1, imageIds, thumbs, ready, cb);
                    }

                    @Override
                    public void onError(String message) {
                        finishErrorText(cb, message);
                    }
                });
            }

            @Override
            public void onError(String message) {
                finishErrorText(cb, message);
            }
        });
    }

    private List<Uri> writeSampleFiles() {
        Context context = FirebaseApp.getInstance().getApplicationContext();
        File folder = new File(context.getCacheDir(), IMAGE_FOLDER);
        if (!folder.exists() && !folder.mkdirs()) {
            Log.w(TAG, "Cannot create the sample image folder");
            return null;
        }
        List<Uri> files = new ArrayList<Uri>();
        for (int variant = 0; variant < DemoDataBuilder.IMAGE_VARIANTS; variant++) {
            File file = new File(folder, "demo_" + variant + ".jpg");
            if (!drawSampleImage(file, variant)) {
                return null;
            }
            files.add(Uri.fromFile(file));
        }
        return files;
    }

    private boolean drawSampleImage(File file, int variant) {
        Bitmap bitmap = Bitmap.createBitmap(IMAGE_WIDTH, IMAGE_HEIGHT, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(IMAGE_COLORS[variant % IMAGE_COLORS.length]);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.WHITE);
        paint.setTextSize(IMAGE_TEXT_SIZE);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("DEMO " + (variant + 1), IMAGE_WIDTH / 2f, IMAGE_HEIGHT / 2f, paint);
        try (FileOutputStream out = new FileOutputStream(file)) {
            return bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out);
        } catch (IOException e) {
            Log.w(TAG, "Cannot write a sample image", e);
            return false;
        }
    }
}
