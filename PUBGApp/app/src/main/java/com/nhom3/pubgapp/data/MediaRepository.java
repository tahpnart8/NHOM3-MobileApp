package com.nhom3.pubgapp.data;

import android.net.Uri;
import com.nhom3.pubgapp.model.enums.ImagePurpose;

// Skeleton: the module owner fills each body as the issue assigns it.
public class MediaRepository extends BaseRepository {

    public void saveImage(Uri uri, ImagePurpose purpose, DataCallback<String> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void makeThumb(Uri uri, DataCallback<String> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void getImage(String id, DataCallback<byte[]> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void delete(String id, DataCallback<Void> cb) {
        cb.onError(DataCallback.NOT_READY);
    }
}
