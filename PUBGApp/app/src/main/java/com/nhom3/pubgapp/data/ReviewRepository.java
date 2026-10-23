package com.nhom3.pubgapp.data;

import com.nhom3.pubgapp.model.Review;
import java.util.List;

// Skeleton: the module owner fills each body as the issue assigns it.
public class ReviewRepository extends BaseRepository {

    public void create(Review review, DataCallback<Void> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void getByDeal(String dealId, DataCallback<List<Review>> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void getReceived(String uid, DataCallback<List<Review>> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void getGiven(String uid, DataCallback<List<Review>> cb) {
        cb.onError(DataCallback.NOT_READY);
    }
}
