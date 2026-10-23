package com.nhom3.pubgapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.Timestamp;
import com.nhom3.pubgapp.data.CategoryRepository;
import com.nhom3.pubgapp.data.DataCallback;
import com.nhom3.pubgapp.data.ListingRepository;
import com.nhom3.pubgapp.model.Category;
import com.nhom3.pubgapp.model.Listing;
import com.nhom3.pubgapp.model.SearchFilter;
import com.nhom3.pubgapp.model.enums.SortOrder;

import java.util.ArrayList;
import java.util.List;

// State for the home feed and the For You section.
public class FeedViewModel extends ViewModel {

    private final MutableLiveData<List<Listing>> feed = new MutableLiveData<>();
    private final MutableLiveData<List<Listing>> forYou = new MutableLiveData<>();
    private final MutableLiveData<List<Category>> categories = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> done = new MutableLiveData<>();

    private final ListingRepository listingRepo = new ListingRepository();
    private final CategoryRepository categoryRepo = new CategoryRepository();

    private boolean currentFollowingOnly = false;
    private SortOrder currentSortOrder = SortOrder.NEWEST;
    private Timestamp lastCreatedAt = null;
    private boolean hasMore = true;
    private boolean isLoadingNext = false;

    public LiveData<List<Listing>> getFeed() {
        return feed;
    }

    public LiveData<List<Listing>> getForYou() {
        return forYou;
    }

    public LiveData<List<Category>> getCategories() {
        return categories;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<Boolean> getDone() {
        return done;
    }

    public void loadFeed(boolean followingOnly, SortOrder order) {
        currentFollowingOnly = followingOnly;
        currentSortOrder = order != null ? order : SortOrder.NEWEST;
        lastCreatedAt = null;
        hasMore = true;
        isLoadingNext = false;
        loading.setValue(true);

        SearchFilter filter = new SearchFilter();
        filter.setFollowingOnly(followingOnly);
        filter.setSortOrder(currentSortOrder);

        listingRepo.getFeed(filter, null, new DataCallback<List<Listing>>() {
            @Override
            public void onSuccess(List<Listing> list) {
                loading.setValue(false);
                if (list == null || list.size() < 20) {
                    hasMore = false;
                } else {
                    hasMore = true;
                }
                if (list != null && !list.isEmpty()) {
                    lastCreatedAt = list.get(list.size() - 1).getCreatedAt();
                }
                feed.setValue(list != null ? list : new ArrayList<Listing>());
            }

            @Override
            public void onError(String err) {
                loading.setValue(false);
                error.setValue(err);
            }
        });
    }

    public void loadCategories() {
        categoryRepo.getAll(new DataCallback<List<Category>>() {
            @Override
            public void onSuccess(List<Category> list) {
                categories.setValue(list != null ? list : new ArrayList<Category>());
            }

            @Override
            public void onError(String err) {
                // Categories load is optional on home screen
            }
        });
    }

    public void loadNext() {
        if (isLoadingNext || !hasMore || Boolean.TRUE.equals(loading.getValue())) {
            return;
        }
        if (lastCreatedAt == null) {
            return;
        }
        isLoadingNext = true;

        SearchFilter filter = new SearchFilter();
        filter.setFollowingOnly(currentFollowingOnly);
        filter.setSortOrder(currentSortOrder);

        listingRepo.getFeed(filter, lastCreatedAt, new DataCallback<List<Listing>>() {
            @Override
            public void onSuccess(List<Listing> page) {
                isLoadingNext = false;
                if (page == null || page.isEmpty()) {
                    hasMore = false;
                    return;
                }
                if (page.size() < 20) {
                    hasMore = false;
                }
                lastCreatedAt = page.get(page.size() - 1).getCreatedAt();
                List<Listing> current = feed.getValue();
                List<Listing> updated = new ArrayList<Listing>();
                if (current != null) {
                    updated.addAll(current);
                }
                updated.addAll(page);
                feed.setValue(updated);
            }

            @Override
            public void onError(String err) {
                isLoadingNext = false;
                error.setValue(err);
            }
        });
    }

    public void loadForYou() {
        error.setValue(DataCallback.NOT_READY);
    }

    public void refresh() {
        loadFeed(currentFollowingOnly, currentSortOrder);
    }

    public void clearError() {
        error.setValue(null);
    }

    public void clearDone() {
        done.setValue(false);
    }
}
