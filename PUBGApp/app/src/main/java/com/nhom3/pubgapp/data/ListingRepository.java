package com.nhom3.pubgapp.data;

import com.google.firebase.Timestamp;
import com.nhom3.pubgapp.model.BannedKeyword;
import com.nhom3.pubgapp.model.Listing;
import com.nhom3.pubgapp.model.SearchFilter;
import com.nhom3.pubgapp.model.enums.ListingStatus;
import java.util.List;

// Skeleton: the module owner fills each body as the issue assigns it.
public class ListingRepository extends BaseRepository {

    public void create(Listing listing, DataCallback<String> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void saveDraft(Listing listing, DataCallback<String> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void publish(Listing listing, DataCallback<String> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void update(Listing listing, DataCallback<Void> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void setHidden(String id, boolean hidden, DataCallback<Void> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void setStatus(String id, ListingStatus from, ListingStatus to, DataCallback<Void> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void delete(String id, DataCallback<Void> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void getById(String id, DataCallback<Listing> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void getBySeller(String sellerId, DataCallback<List<Listing>> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void getFeed(SearchFilter filter, Timestamp after, DataCallback<List<Listing>> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void search(SearchFilter filter, Timestamp after, DataCallback<List<Listing>> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void getSimilar(Listing listing, DataCallback<List<Listing>> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void recordView(Listing listing) {
        throw new UnsupportedOperationException("NOT_READY");
    }

    public void recordSearch(String keyword) {
        throw new UnsupportedOperationException("NOT_READY");
    }

    public void getBannedKeywords(DataCallback<List<BannedKeyword>> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void getRecentViews(int limit, DataCallback<List<String>> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void getRecentSearches(int limit, DataCallback<List<String>> cb) {
        cb.onError(DataCallback.NOT_READY);
    }
}
