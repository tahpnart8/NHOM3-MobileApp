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

    public void loadForYou() {
        listingRepo.getRecentViews(20, new DataCallback<List<String>>() {
            @Override
            public void onSuccess(final List<String> viewedCategoryIds) {
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

            @Override
            public void onError(String err) {
                listingRepo.getRecentSearches(10, new DataCallback<List<String>>() {
                    @Override
                    public void onSuccess(final List<String> recentKeywords) {
                        checkUserAndRecommend(new ArrayList<String>(), recentKeywords);
                    }

                    @Override
                    public void onError(String searchErr) {
                        checkUserAndRecommend(new ArrayList<String>(), new ArrayList<String>());
                    }
                });
            }
        });
    }

    private void checkUserAndRecommend(final List<String> viewedCategoryIds,
                                       final List<String> recentKeywords) {
        if (!SessionManager.getInstance().isLoggedIn()) {
            evaluateAndFetch(viewedCategoryIds, recentKeywords, null, null);
            return;
        }

        final User currentUser = SessionManager.getInstance().getCurrentUser();
        String uid = SessionManager.getInstance().getCurrentUserId();
        if (uid == null || uid.isEmpty()) {
            evaluateAndFetch(viewedCategoryIds, recentKeywords, null, currentUser);
            return;
        }

        userRepo.getSavedIds(uid, new DataCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> savedIds) {
                evaluateAndFetch(viewedCategoryIds, recentKeywords, savedIds, currentUser);
            }

            @Override
            public void onError(String errorMessage) {
                evaluateAndFetch(viewedCategoryIds, recentKeywords, null, currentUser);
            }
        });
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
            forYou.postValue(new ArrayList<Listing>());
            return;
        }

        SearchFilter filter = new SearchFilter();
        listingRepo.getFeed(filter, null, new DataCallback<List<Listing>>() {
            @Override
            public void onSuccess(List<Listing> candidates) {
                if (candidates == null || candidates.isEmpty()) {
                    forYou.postValue(new ArrayList<Listing>());
                    return;
                }
                List<Listing> scored = recommendationEngine.forYou(candidates, viewedCategoryIds,
                        recentKeywords, savedCategoryIds, currentUser);
                forYou.postValue(scored);
            }

            @Override
            public void onError(String err) {
                forYou.postValue(new ArrayList<Listing>());
            }
        });
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
