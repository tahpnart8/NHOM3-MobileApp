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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.nhom3.pubgapp.model.MediaImage;
import com.nhom3.pubgapp.model.enums.ImagePurpose;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MediaRepository extends BaseRepository {

    private static final String COLLECTION_IMAGES = "images";
    private static final int MAX_IMAGE_DIMENSION = 1024;
    private static final int MAX_IMAGE_BYTES = 300 * 1024;
    private static final int MAX_THUMB_DIMENSION = 320;
    private static final int MAX_THUMB_BYTES = 20 * 1024;

    private static final LruCache<String, byte[]> MEMORY_CACHE =
            new LruCache<String, byte[]>(5 * 1024 * 1024) {
                @Override
                protected int sizeOf(String key, byte[] value) {
                    return value != null ? value.length : 0;
                }
            };

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public void saveImage(final Uri uri, final ImagePurpose purpose, final DataCallback<String> cb) {
        final String uid = currentUserId();
        if (uid.isEmpty()) {
            cb.onError("Chưa đăng nhập");
            return;
        }
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Context context = FirebaseApp.getInstance().getApplicationContext();
                    Bitmap bitmap = decodeSampledBitmap(context, uri, MAX_IMAGE_DIMENSION);
                    if (bitmap == null) {
                        cb.onError("Không thể giải mã hình ảnh");
                        return;
                    }
                    byte[] bytes = compressBitmap(bitmap, MAX_IMAGE_BYTES);
                    if (bytes.length == 0 || bytes.length > MAX_IMAGE_BYTES) {
                        cb.onError("Ảnh vượt quá kích thước cho phép");
                        return;
                    }
                    int width = bitmap.getWidth();
                    int height = bitmap.getHeight();
                    String base64 = Base64.encodeToString(bytes, Base64.NO_WRAP);

                    final DocumentReference ref = db.collection(COLLECTION_IMAGES).document();
                    final String imageId = ref.getId();

                    Map<String, Object> image = buildImageDocument(uid, purpose, base64, width, height, bytes.length);

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
                                    fail(cb, e);
                                }
                            });
                } catch (Exception e) {
                    fail(cb, e);
                }
            }
        });
    }

    // A Map, not a MediaImage: BaseDocument would add updatedAt, and the images rule allows only these keys.
    static Map<String, Object> buildImageDocument(String ownerId, ImagePurpose purpose, String base64,
                                                  int width, int height, int sizeBytes) {
        Map<String, Object> image = new HashMap<String, Object>();
        image.put("ownerId", ownerId);
        image.put("purpose", purpose.name());
        image.put("data", base64);
        image.put("width", width);
        image.put("height", height);
        image.put("sizeBytes", sizeBytes);
        image.put("createdAt", FieldValue.serverTimestamp());
        return image;
    }

    public void makeThumb(final Uri uri, final DataCallback<String> cb) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Context context = FirebaseApp.getInstance().getApplicationContext();
                    Bitmap bitmap = decodeSampledBitmap(context, uri, MAX_THUMB_DIMENSION);
                    if (bitmap == null) {
                        cb.onError("Không thể tạo ảnh thu nhỏ");
                        return;
                    }
                    byte[] bytes = compressBitmap(bitmap, MAX_THUMB_BYTES);
                    if (bytes.length == 0 || bytes.length > MAX_THUMB_BYTES) {
                        cb.onError("Ảnh thu nhỏ vượt quá kích thước cho phép");
                        return;
                    }
                    String base64 = Base64.encodeToString(bytes, Base64.NO_WRAP);
                    cb.onSuccess(base64);
                } catch (Exception e) {
                    fail(cb, e);
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
        db.collection(COLLECTION_IMAGES).document(id).get()
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
                        cb.onError("Không tìm thấy hình ảnh");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        fail(cb, e);
                    }
                });
    }

    public void delete(final String id, final DataCallback<Void> cb) {
        MEMORY_CACHE.remove(id);
        db.collection(COLLECTION_IMAGES).document(id).delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        cb.onSuccess(null);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        fail(cb, e);
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
        Bitmap current = bitmap;
        for (int attempt = 0; attempt < 4; attempt++) {
            int quality = 85;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            current.compress(Bitmap.CompressFormat.JPEG, quality, baos);
            while (baos.size() > maxSizeBytes && quality > 15) {
                baos.reset();
                quality -= 15;
                current.compress(Bitmap.CompressFormat.JPEG, quality, baos);
            }
            if (baos.size() <= maxSizeBytes) {
                if (current != bitmap) {
                    current.recycle();
                }
                return baos.toByteArray();
            }
            int newW = Math.max(1, (int) (current.getWidth() * 0.8f));
            int newH = Math.max(1, (int) (current.getHeight() * 0.8f));
            Bitmap scaled = Bitmap.createScaledBitmap(current, newW, newH, true);
            if (current != bitmap) {
                current.recycle();
            }
            current = scaled;
        }
        return new byte[0];
    }
}
