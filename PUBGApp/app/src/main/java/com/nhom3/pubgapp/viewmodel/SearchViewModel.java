package com.nhom3.pubgapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;
import com.nhom3.pubgapp.data.CategoryRepository;
import com.nhom3.pubgapp.data.DataCallback;
import com.nhom3.pubgapp.data.ListingRepository;
import com.nhom3.pubgapp.model.Category;
import com.nhom3.pubgapp.model.Listing;
import com.nhom3.pubgapp.model.SearchFilter;
import com.nhom3.pubgapp.model.User;
import com.nhom3.pubgapp.model.enums.SortOrder;
import com.nhom3.pubgapp.util.GeoUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

// State for keyword search, filters and search results.
public class SearchViewModel extends ViewModel {

    private final MutableLiveData<List<Listing>> results = new MutableLiveData<>();
    private final MutableLiveData<List<User>> userResults = new MutableLiveData<>();
    private final MutableLiveData<List<Category>> categories = new MutableLiveData<>();
    private final MutableLiveData<List<String>> recentSearches = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> done = new MutableLiveData<>();

    private final ListingRepository listingRepo = new ListingRepository();
    private final CategoryRepository categoryRepo = new CategoryRepository();
    private final GeoUtils geoUtils = new GeoUtils();

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

    static List<Listing> sortResults(List<Listing> list, SortOrder order) {
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
        List<Listing> sorted = sortResults(processed, filter != null ? filter.getSortOrder() : null);
        if (filter != null && filter.getCenter() != null) {
            sorted = sortByDistance(sorted, filter.getCenter(), geoUtils);
        }
        return sorted;
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

    public void applyFilter(SearchFilter newFilter) {
        this.filter = newFilter != null ? newFilter : new SearchFilter();
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
                loading.postValue(false);
                if (list == null || list.size() < 20) {
                    hasMore = false;
                } else {
                    hasMore = true;
                }
                if (list != null && !list.isEmpty()) {
                    lastCreatedAt = list.get(list.size() - 1).getCreatedAt();
                }
                List<Listing> sorted = processResults(list);
                results.postValue(sorted);
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
                List<Listing> sorted = processResults(merged);
                results.postValue(sorted);
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
