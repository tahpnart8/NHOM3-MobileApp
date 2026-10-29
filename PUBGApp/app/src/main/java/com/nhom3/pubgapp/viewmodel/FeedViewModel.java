package com.nhom3.pubgapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.Timestamp;
import com.nhom3.pubgapp.data.CategoryRepository;
import com.nhom3.pubgapp.data.DataCallback;
import com.nhom3.pubgapp.data.ListingRepository;
import com.nhom3.pubgapp.data.UserRepository;
import com.nhom3.pubgapp.model.Category;
import com.nhom3.pubgapp.model.Listing;
import com.nhom3.pubgapp.model.SearchFilter;
import com.nhom3.pubgapp.model.User;
import com.nhom3.pubgapp.model.enums.SortOrder;
import com.nhom3.pubgapp.util.RecommendationEngine;
import com.nhom3.pubgapp.util.SessionManager;

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
    private final UserRepository userRepo = new UserRepository();
    private final RecommendationEngine recommendationEngine = new RecommendationEngine();

    private boolean currentFollowingOnly = false;
    private SortOrder currentSortOrder = SortOrder.NEWEST;
    private Timestamp lastCreatedAt = null;
    private boolean hasMore = true;
    private boolean isLoadingNext = false;

    // For You costs about 40 reads, so the list is kept for a while instead of read on every return to Home.
    static final long FOR_YOU_MAX_AGE_MS = 10L * 60L * 1000L;
    private long forYouLoadedAtMs = 0L;
    private boolean forYouLoading = false;

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

    // Note: getFeed may call cb.onSuccess twice (cached items first, then fresh items).
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
                loading.postValue(false);
                if (list == null || list.size() < 20) {
                    hasMore = false;
                } else {
                    hasMore = true;
                }
                if (list != null && !list.isEmpty()) {
                    lastCreatedAt = list.get(list.size() - 1).getCreatedAt();
                }
                feed.postValue(list != null ? list : new ArrayList<Listing>());
            }

            @Override
            public void onError(String err) {
                loading.postValue(false);
                // Keep showing cached data if network request fails after cache load
                List<Listing> current = feed.getValue();
                if (current == null || current.isEmpty()) {
                    error.postValue(err);
                }
            }
        });
    }

    public void loadCategories() {
        categoryRepo.getAll(new DataCallback<List<Category>>() {
            @Override
            public void onSuccess(List<Category> list) {
                categories.postValue(list != null ? list : new ArrayList<Category>());
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
                feed.postValue(updated);
            }

            @Override
            public void onError(String err) {
                isLoadingNext = false;
                error.postValue(err);
            }
        });
    }

    // Does nothing while a load runs or while the kept list is younger than FOR_YOU_MAX_AGE_MS.
    // An empty list is not kept: right after the user views a first listing, For You should appear.
    public void loadForYou() {
        List<Listing> current = forYou.getValue();
        boolean hasData = current != null && !current.isEmpty();
        if (forYouLoading || isForYouFresh(hasData, forYouLoadedAtMs, System.currentTimeMillis())) {
            return;
        }
        forYouLoading = true;
        listingRepo.getRecentViews(20, new DataCallback<List<String>>() {
            @Override
            public void onSuccess(final List<String> viewedCategoryIds) {
                loadSearchesAndRecommend(viewedCategoryIds);
            }

            @Override
            public void onError(String err) {
                loadSearchesAndRecommend(new ArrayList<String>());
            }
        });
    }

    private void loadSearchesAndRecommend(final List<String> viewedCategoryIds) {
        listingRepo.getRecentSearches(10, new DataCallback<List<String>>() {
            @Override
            public void onSuccess(final List<String> recentKeywords) {
                checkUserAndRecommend(viewedCategoryIds, recentKeywords);
            }

            @Override
            public void onError(String err) {
                checkUserAndRecommend(viewedCategoryIds, new ArrayList<String>());
            }
        });
    }

    private void checkUserAndRecommend(final List<String> viewedCategoryIds,
                                       final List<String> recentKeywords) {
        if (!SessionManager.getInstance().isLoggedIn()) {
            evaluateAndFetch(viewedCategoryIds, recentKeywords, new ArrayList<String>(), null);
            return;
        }

        final User currentUser = SessionManager.getInstance().getCurrentUser();
        String uid = SessionManager.getInstance().getCurrentUserId();
        if (uid == null || uid.isEmpty()) {
            evaluateAndFetch(viewedCategoryIds, recentKeywords, new ArrayList<String>(), currentUser);
            return;
        }

        userRepo.getSavedIds(uid, new DataCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> savedIds) {
                resolveSavedCategories(savedIds, viewedCategoryIds, recentKeywords, currentUser);
            }

            @Override
            public void onError(String errorMessage) {
                evaluateAndFetch(viewedCategoryIds, recentKeywords, new ArrayList<String>(), currentUser);
            }
        });
    }

    private void resolveSavedCategories(final List<String> savedIds,
                                        final List<String> viewedCategoryIds,
                                        final List<String> recentKeywords,
                                        final User currentUser) {
        if (savedIds == null || savedIds.isEmpty()) {
            evaluateAndFetch(viewedCategoryIds, recentKeywords, new ArrayList<String>(), currentUser);
            return;
        }

        // Limit to 20 most recent saved listings to conserve Firestore read quota on Spark plan
        final int limit = Math.min(20, savedIds.size());
        final List<String> savedCategoryIds = new ArrayList<String>();
        final int[] remaining = new int[] { limit };
        final Object lock = new Object();

        for (int i = 0; i < limit; i++) {
            String savedId = savedIds.get(i);
            listingRepo.getById(savedId, new DataCallback<Listing>() {
                @Override
                public void onSuccess(Listing result) {
                    synchronized (lock) {
                        if (result != null && result.getCategoryId() != null
                                && !savedCategoryIds.contains(result.getCategoryId())) {
                            savedCategoryIds.add(result.getCategoryId());
                        }
                        checkDone();
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    synchronized (lock) {
                        checkDone();
                    }
                }

                private void checkDone() {
                    remaining[0]--;
                    if (remaining[0] <= 0) {
                        evaluateAndFetch(viewedCategoryIds, recentKeywords, savedCategoryIds, currentUser);
                    }
                }
            });
        }
    }

    private void evaluateAndFetch(final List<String> viewedCategoryIds,
                                  final List<String> recentKeywords,
                                  final List<String> savedCategoryIds,
                                  final User currentUser) {
        boolean hasViews = viewedCategoryIds != null && !viewedCategoryIds.isEmpty();
        boolean hasSearches = recentKeywords != null && !recentKeywords.isEmpty();
        boolean hasSaved = savedCategoryIds != null && !savedCategoryIds.isEmpty();
        boolean hasInterested = currentUser != null && currentUser.getInterestedCategoryIds() != null
                && !currentUser.getInterestedCategoryIds().isEmpty();

        if (!hasViews && !hasSearches && !hasSaved && !hasInterested) {
            publishForYou(new ArrayList<Listing>());
            return;
        }

        SearchFilter filter = new SearchFilter();
        listingRepo.getFeed(filter, null, new DataCallback<List<Listing>>() {
            @Override
            public void onSuccess(List<Listing> candidates) {
                if (candidates == null || candidates.isEmpty()) {
                    publishForYou(new ArrayList<Listing>());
                    return;
                }
                List<Listing> scored = recommendationEngine.forYou(candidates, viewedCategoryIds,
                        recentKeywords, savedCategoryIds, currentUser);
                publishForYou(scored);
            }

            @Override
            public void onError(String err) {
                publishForYou(new ArrayList<Listing>());
            }
        });
    }

    static boolean isForYouFresh(boolean hasData, long loadedAtMs, long nowMs) {
        return hasData && nowMs - loadedAtMs < FOR_YOU_MAX_AGE_MS;
    }

    private void publishForYou(List<Listing> list) {
        forYouLoadedAtMs = System.currentTimeMillis();
        forYouLoading = false;
        forYou.postValue(list);
    }

    // The user asked for fresh data (Reload or Retry), so For You is read again even if it is still fresh.
    public void refresh() {
        loadFeed(currentFollowingOnly, currentSortOrder);
        forYouLoadedAtMs = 0L;
        loadForYou();
    }

    public void clearError() {
        error.setValue(null);
    }

    public void clearDone() {
        done.setValue(false);
    }
}
