package com.nhom3.pubgapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;
import com.nhom3.pubgapp.data.CategoryRepository;
import com.nhom3.pubgapp.data.DataCallback;
import com.nhom3.pubgapp.data.ListingRepository;
import com.nhom3.pubgapp.data.UserRepository;
import com.nhom3.pubgapp.model.Category;
import com.nhom3.pubgapp.model.Listing;
import com.nhom3.pubgapp.model.SearchFilter;
import com.nhom3.pubgapp.model.User;
import com.nhom3.pubgapp.model.enums.SortOrder;
import com.nhom3.pubgapp.util.GeoUtils;
import com.nhom3.pubgapp.util.NetworkMonitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

// State for keyword search, filters and search results.
public class SearchViewModel extends ViewModel {

    private static final String MESSAGE_OFFLINE_SELLER_RATING =
            "Không có kết nối mạng. Không thể tải đánh giá người bán";
    private static final String MESSAGE_ERROR_SELLER_RATING =
            "Lỗi khi tải thông tin người bán";

    private final MutableLiveData<List<Listing>> results = new MutableLiveData<>();
    private final MutableLiveData<List<User>> userResults = new MutableLiveData<>();
    private final MutableLiveData<List<Category>> categories = new MutableLiveData<>();
    private final MutableLiveData<List<String>> recentSearches = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> done = new MutableLiveData<>();

    private final ListingRepository listingRepo = new ListingRepository();
    private final CategoryRepository categoryRepo = new CategoryRepository();
    private final UserRepository userRepo = new UserRepository();
    private final GeoUtils geoUtils = new GeoUtils();

    // In-memory cache of seller average ratings to avoid redundant network reads.
    final Map<String, Double> sellerRatingsCache = new ConcurrentHashMap<String, Double>();

    // Plain field, not LiveData: the ViewModel itself survives rotation.
    private SearchFilter filter = new SearchFilter();
    private Timestamp lastCreatedAt = null;
    private boolean hasMore = true;
    private boolean isLoadingNext = false;

    static boolean isPriceRangeValid(long minPrice, long maxPrice) {
        if (minPrice <= 0 || maxPrice <= 0) {
            return true;
        }
        return maxPrice >= minPrice;
    }

    static List<Listing> filterByRadius(List<Listing> list, GeoPoint center, int radiusKm, GeoUtils geoUtils) {
        if (list == null) {
            return new ArrayList<Listing>();
        }
        if (center == null || radiusKm <= 0) {
            return new ArrayList<Listing>(list);
        }
        GeoUtils utils = geoUtils != null ? geoUtils : new GeoUtils();
        List<Listing> filtered = new ArrayList<Listing>();
        for (int i = 0; i < list.size(); i++) {
            Listing item = list.get(i);
            if (item != null && item.getLocation() != null
                    && utils.isWithinRadius(item.getLocation(), center, radiusKm)) {
                filtered.add(item);
            }
        }
        return filtered;
    }

    static List<Listing> sortByDistance(List<Listing> list, GeoPoint center, GeoUtils geoUtils) {
        if (list == null) {
            return new ArrayList<Listing>();
        }
        List<Listing> copy = new ArrayList<Listing>(list);
        if (center == null) {
            return copy;
        }
        GeoUtils utils = geoUtils != null ? geoUtils : new GeoUtils();
        utils.sortByDistance(copy, center);
        return copy;
    }

    static double calculateSellerRating(User user) {
        if (user == null) {
            return 0.0;
        }
        return calculateSellerRating(user.getRatingSum(), user.getRatingCount());
    }

    static double calculateSellerRating(int ratingSum, int ratingCount) {
        if (ratingCount <= 0) {
            return 0.0;
        }
        return (double) ratingSum / (double) ratingCount;
    }

    static List<Listing> sortResults(List<Listing> list, SortOrder order) {
        return sortResults(list, order, null);
    }

    static List<Listing> sortResults(List<Listing> list, SortOrder order,
                                     final Map<String, Double> sellerRatings) {
        if (list == null) {
            return new ArrayList<Listing>();
        }
        List<Listing> sorted = new ArrayList<Listing>(list);
        if (order == SortOrder.PRICE_ASC) {
            Collections.sort(sorted, new Comparator<Listing>() {
                @Override
                public int compare(Listing o1, Listing o2) {
                    return Long.compare(o1.getPrice(), o2.getPrice());
                }
            });
        } else if (order == SortOrder.PRICE_DESC) {
            Collections.sort(sorted, new Comparator<Listing>() {
                @Override
                public int compare(Listing o1, Listing o2) {
                    return Long.compare(o2.getPrice(), o1.getPrice());
                }
            });
        } else if (order == SortOrder.TOP_RATED) {
            Collections.sort(sorted, new Comparator<Listing>() {
                @Override
                public int compare(Listing o1, Listing o2) {
                    double r1 = 0.0;
                    double r2 = 0.0;
                    if (sellerRatings != null) {
                        if (o1.getSellerId() != null && sellerRatings.containsKey(o1.getSellerId())) {
                            Double val1 = sellerRatings.get(o1.getSellerId());
                            if (val1 != null) {
                                r1 = val1.doubleValue();
                            }
                        }
                        if (o2.getSellerId() != null && sellerRatings.containsKey(o2.getSellerId())) {
                            Double val2 = sellerRatings.get(o2.getSellerId());
                            if (val2 != null) {
                                r2 = val2.doubleValue();
                            }
                        }
                    }
                    int ratingCompare = Double.compare(r2, r1);
                    if (ratingCompare != 0) {
                        return ratingCompare;
                    }
                    if (o1.getCreatedAt() == null && o2.getCreatedAt() == null) {
                        return 0;
                    }
                    if (o1.getCreatedAt() == null) {
                        return 1;
                    }
                    if (o2.getCreatedAt() == null) {
                        return -1;
                    }
                    return o2.getCreatedAt().compareTo(o1.getCreatedAt());
                }
            });
        } else {
            Collections.sort(sorted, new Comparator<Listing>() {
                @Override
                public int compare(Listing o1, Listing o2) {
                    if (o1.getCreatedAt() == null && o2.getCreatedAt() == null) {
                        return 0;
                    }
                    if (o1.getCreatedAt() == null) {
                        return 1;
                    }
                    if (o2.getCreatedAt() == null) {
                        return -1;
                    }
                    return o2.getCreatedAt().compareTo(o1.getCreatedAt());
                }
            });
        }
        return sorted;
    }

    private List<Listing> processResults(List<Listing> list) {
        if (list == null) {
            return new ArrayList<Listing>();
        }
        List<Listing> processed = list;
        if (filter != null && filter.getRadiusKm() > 0 && filter.getCenter() != null) {
            processed = filterByRadius(processed, filter.getCenter(), filter.getRadiusKm(), geoUtils);
        }
        return processed;
    }

    public LiveData<List<Listing>> getResults() {
        return results;
    }

    public LiveData<List<User>> getUserResults() {
        return userResults;
    }

    public LiveData<List<Category>> getCategories() {
        return categories;
    }

    public LiveData<List<String>> getRecentSearches() {
        return recentSearches;
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

    public SearchFilter getFilter() {
        if (filter == null) {
            filter = new SearchFilter();
        }
        return filter;
    }

    public void setKeyword(String text) {
        if (filter == null) {
            filter = new SearchFilter();
        }
        filter.setKeyword(text);
    }

    static boolean canReorderExistingResults(SearchFilter oldF, SearchFilter newF) {
        if (oldF == null || newF == null) {
            return false;
        }
        SortOrder oldOrder = oldF.getSortOrder() != null ? oldF.getSortOrder() : SortOrder.NEWEST;
        SortOrder newOrder = newF.getSortOrder() != null ? newF.getSortOrder() : SortOrder.NEWEST;
        if (oldOrder == newOrder) {
            return false;
        }
        boolean oldIsCreatedAt = (oldOrder == SortOrder.NEWEST || oldOrder == SortOrder.TOP_RATED);
        boolean newIsCreatedAt = (newOrder == SortOrder.NEWEST || newOrder == SortOrder.TOP_RATED);
        if (!oldIsCreatedAt || !newIsCreatedAt) {
            return false;
        }
        if (!equalsOrBothEmpty(oldF.getKeyword(), newF.getKeyword())) {
            return false;
        }
        if (!equalsOrBothNull(oldF.getCategoryId(), newF.getCategoryId())) {
            return false;
        }
        if (oldF.getMinPrice() != newF.getMinPrice() || oldF.getMaxPrice() != newF.getMaxPrice()) {
            return false;
        }
        if (!equalsOrBothNull(oldF.getProvince(), newF.getProvince())) {
            return false;
        }
        if (!equalsList(oldF.getConditions(), newF.getConditions())) {
            return false;
        }
        if (!equalsList(oldF.getTradeTypes(), newF.getTradeTypes())) {
            return false;
        }
        if (oldF.isFollowingOnly() != newF.isFollowingOnly()) {
            return false;
        }
        if (oldF.getRadiusKm() != newF.getRadiusKm()) {
            return false;
        }
        return true;
    }

    private static boolean equalsOrBothEmpty(String s1, String s2) {
        String t1 = s1 != null ? s1.trim() : "";
        String t2 = s2 != null ? s2.trim() : "";
        return t1.equals(t2);
    }

    private static boolean equalsOrBothNull(String s1, String s2) {
        if (s1 == null && s2 == null) {
            return true;
        }
        if (s1 == null || s2 == null) {
            return false;
        }
        return s1.equals(s2);
    }

    private static <T> boolean equalsList(List<T> l1, List<T> l2) {
        boolean empty1 = l1 == null || l1.isEmpty();
        boolean empty2 = l2 == null || l2.isEmpty();
        if (empty1 && empty2) {
            return true;
        }
        if (empty1 || empty2) {
            return false;
        }
        return l1.equals(l2);
    }

    void processAndPublishResults(final List<Listing> rawList, final SortOrder order) {
        final List<Listing> list = processResults(rawList);
        if (order != SortOrder.TOP_RATED) {
            List<Listing> sorted = sortResults(list, order, sellerRatingsCache);
            if (filter != null && filter.getCenter() != null
                    && order != SortOrder.PRICE_ASC && order != SortOrder.PRICE_DESC) {
                sorted = sortByDistance(sorted, filter.getCenter(), geoUtils);
            }
            results.postValue(sorted);
            loading.postValue(false);
            return;
        }

        if (list.isEmpty()) {
            results.postValue(new ArrayList<Listing>());
            loading.postValue(false);
            return;
        }

        List<String> distinctSellerIds = new ArrayList<String>();
        for (int i = 0; i < list.size(); i++) {
            Listing item = list.get(i);
            if (item != null) {
                String sId = item.getSellerId();
                if (sId != null && !sId.trim().isEmpty() && !distinctSellerIds.contains(sId)) {
                    distinctSellerIds.add(sId);
                    if (distinctSellerIds.size() >= 30) {
                        break;
                    }
                }
            }
        }

        final List<String> toFetch = new ArrayList<String>();
        for (int i = 0; i < distinctSellerIds.size(); i++) {
            String sId = distinctSellerIds.get(i);
            if (!sellerRatingsCache.containsKey(sId)) {
                toFetch.add(sId);
            }
        }

        if (toFetch.isEmpty()) {
            List<Listing> sorted = sortResults(list, SortOrder.TOP_RATED, sellerRatingsCache);
            results.postValue(sorted);
            loading.postValue(false);
            return;
        }

        boolean online = NetworkMonitor.getInstance().isOnline();
        if (!online) {
            error.postValue(MESSAGE_OFFLINE_SELLER_RATING);
            loading.postValue(false);
            List<Listing> sorted = sortResults(list, SortOrder.TOP_RATED, sellerRatingsCache);
            results.postValue(sorted);
            return;
        }

        loading.postValue(true);
        final AtomicInteger remaining = new AtomicInteger(toFetch.size());

        for (int i = 0; i < toFetch.size(); i++) {
            final String sId = toFetch.get(i);
            userRepo.getProfile(sId, new DataCallback<User>() {
                @Override
                public void onSuccess(User user) {
                    double rating = calculateSellerRating(user);
                    sellerRatingsCache.put(sId, rating);
                    if (remaining.decrementAndGet() == 0) {
                        loading.postValue(false);
                        List<Listing> sorted = sortResults(list, SortOrder.TOP_RATED, sellerRatingsCache);
                        results.postValue(sorted);
                    }
                }

                @Override
                public void onError(String err) {
                    sellerRatingsCache.put(sId, 0.0);
                    error.postValue(err != null ? err : MESSAGE_ERROR_SELLER_RATING);
                    if (remaining.decrementAndGet() == 0) {
                        loading.postValue(false);
                        List<Listing> sorted = sortResults(list, SortOrder.TOP_RATED, sellerRatingsCache);
                        results.postValue(sorted);
                    }
                }
            });
        }
    }

    public void applyFilter(SearchFilter newFilter) {
        SearchFilter prevFilter = this.filter;
        this.filter = newFilter != null ? newFilter : new SearchFilter();
        if (canReorderExistingResults(prevFilter, this.filter)) {
            List<Listing> current = results.getValue();
            if (current != null && !current.isEmpty()) {
                processAndPublishResults(current, this.filter.getSortOrder());
                return;
            }
        }
        search();
    }

    public void search() {
        lastCreatedAt = null;
        hasMore = true;
        isLoadingNext = false;
        loading.setValue(true);

        if (filter == null) {
            filter = new SearchFilter();
        }

        final String keyword = filter.getKeyword();
        if (keyword != null && !keyword.trim().isEmpty()) {
            listingRepo.recordSearch(keyword.trim());
            loadRecentSearches();
        }

        listingRepo.search(filter, null, new DataCallback<List<Listing>>() {
            @Override
            public void onSuccess(List<Listing> list) {
                if (list == null || list.size() < 20) {
                    hasMore = false;
                } else {
                    hasMore = true;
                }
                if (list != null && !list.isEmpty()) {
                    lastCreatedAt = list.get(list.size() - 1).getCreatedAt();
                }
                processAndPublishResults(list, filter.getSortOrder());
            }

            @Override
            public void onError(String err) {
                loading.postValue(false);
                error.postValue(err);
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

        listingRepo.search(filter, lastCreatedAt, new DataCallback<List<Listing>>() {
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
                List<Listing> current = results.getValue();
                List<Listing> merged = new ArrayList<Listing>();
                if (current != null) {
                    merged.addAll(current);
                }
                merged.addAll(page);
                processAndPublishResults(merged, filter.getSortOrder());
            }

            @Override
            public void onError(String err) {
                isLoadingNext = false;
                error.postValue(err);
            }
        });
    }

    public void searchUsers(String keyword) {
        error.setValue(DataCallback.NOT_READY);
    }

    public void loadCategories() {
        categoryRepo.getAll(new DataCallback<List<Category>>() {
            @Override
            public void onSuccess(List<Category> list) {
                categories.postValue(list != null ? list : new ArrayList<Category>());
            }

            @Override
            public void onError(String err) {
                // Category load is optional for search filter panel
            }
        });
    }

    public void loadRecentSearches() {
        listingRepo.getRecentSearches(20, new DataCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> list) {
                recentSearches.postValue(list != null ? list : new ArrayList<String>());
            }

            @Override
            public void onError(String err) {
                recentSearches.postValue(new ArrayList<String>());
            }
        });
    }

    public void clearError() {
        error.setValue(null);
    }

    public void clearDone() {
        done.setValue(false);
    }
}
