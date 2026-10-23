package com.nhom3.pubgapp.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.util.LruCache;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.nhom3.pubgapp.model.MediaImage;
import com.nhom3.pubgapp.model.enums.ImagePurpose;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MediaRepository extends BaseRepository {

    private static final LruCache<String, byte[]> MEMORY_CACHE =
            new LruCache<String, byte[]>(5 * 1024 * 1024) {
                @Override
                protected int sizeOf(String key, byte[] value) {
                    return value != null ? value.length : 0;
                }
            };

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public void saveImage(final Uri uri, final ImagePurpose purpose, final DataCallback<String> cb) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Context context = FirebaseApp.getInstance().getApplicationContext();
                    Bitmap bitmap = decodeSampledBitmap(context, uri, 1024);
                    if (bitmap == null) {
                        cb.onError("Cannot decode image");
                        return;
                    }
                    byte[] bytes = compressBitmap(bitmap, 300 * 1024);
                    int width = bitmap.getWidth();
                    int height = bitmap.getHeight();
                    String base64 = Base64.encodeToString(bytes, Base64.NO_WRAP);

                    final DocumentReference ref = db.collection("images").document();
                    final String imageId = ref.getId();

                    MediaImage image = new MediaImage();
                    image.setId(imageId);
                    image.setOwnerId(auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "");
                    image.setPurpose(purpose);
                    image.setData(base64);
                    image.setWidth(width);
                    image.setHeight(height);
                    image.setSizeBytes(bytes.length);
                    image.setCreatedAt(Timestamp.now());

                    MEMORY_CACHE.put(imageId, bytes);

                    ref.set(image)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    cb.onSuccess(imageId);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(Exception e) {
                                    cb.onError(e.getMessage() != null ? e.getMessage() : "Error saving image");
                                }
                            });
                } catch (Exception e) {
                    cb.onError(e.getMessage() != null ? e.getMessage() : "Error processing image");
                }
            }
        });
    }

    public void makeThumb(final Uri uri, final DataCallback<String> cb) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Context context = FirebaseApp.getInstance().getApplicationContext();
                    Bitmap bitmap = decodeSampledBitmap(context, uri, 320);
                    if (bitmap == null) {
                        cb.onError("Cannot decode thumbnail");
                        return;
                    }
                    byte[] bytes = compressBitmap(bitmap, 20 * 1024);
                    String base64 = Base64.encodeToString(bytes, Base64.NO_WRAP);
                    cb.onSuccess(base64);
                } catch (Exception e) {
                    cb.onError(e.getMessage() != null ? e.getMessage() : "Error creating thumbnail");
                }
            }
        });
    }

    public void getImage(final String id, final DataCallback<byte[]> cb) {
        byte[] cached = MEMORY_CACHE.get(id);
        if (cached != null) {
            cb.onSuccess(cached);
            return;
        }
        db.collection("images").document(id).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot doc) {
                        if (doc != null && doc.exists()) {
                            MediaImage image = doc.toObject(MediaImage.class);
                            if (image != null) {
                                byte[] bytes = image.toBytes();
                                if (bytes != null && bytes.length > 0) {
                                    MEMORY_CACHE.put(id, bytes);
                                    cb.onSuccess(bytes);
                                    return;
                                }
                            }
                        }
                        cb.onError("Image not found");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        cb.onError(e.getMessage() != null ? e.getMessage() : "Error fetching image");
                    }
                });
    }

    public void delete(final String id, final DataCallback<Void> cb) {
        MEMORY_CACHE.remove(id);
        db.collection("images").document(id).delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        cb.onSuccess(null);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        cb.onError(e.getMessage() != null ? e.getMessage() : "Error deleting image");
                    }
                });
    }

    private Bitmap decodeSampledBitmap(Context context, Uri uri, int maxDimension) throws Exception {
        InputStream input = context.getContentResolver().openInputStream(uri);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(input, null, options);
        if (input != null) {
            input.close();
        }

        int inSampleSize = 1;
        int maxSrc = Math.max(options.outWidth, options.outHeight);
        while (maxSrc / (inSampleSize * 2) >= maxDimension) {
            inSampleSize *= 2;
        }

        BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
        decodeOptions.inSampleSize = inSampleSize;
        input = context.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, decodeOptions);
        if (input != null) {
            input.close();
        }
        if (bitmap == null) {
            return null;
        }

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int maxBmp = Math.max(width, height);
        if (maxBmp > maxDimension) {
            float scale = ((float) maxDimension) / maxBmp;
            int targetW = Math.round(width * scale);
            int targetH = Math.round(height * scale);
            Bitmap scaled = Bitmap.createScaledBitmap(bitmap, targetW, targetH, true);
            if (scaled != bitmap) {
                bitmap.recycle();
                bitmap = scaled;
            }
        }
        return bitmap;
    }

    private byte[] compressBitmap(Bitmap bitmap, int maxSizeBytes) {
        int quality = 85;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        while (baos.size() > maxSizeBytes && quality > 15) {
            baos.reset();
            quality -= 15;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        }
        return baos.toByteArray();
    }
}
