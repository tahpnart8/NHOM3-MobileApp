package com.nhom3.pubgapp.viewmodel;

import android.net.Uri;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.nhom3.pubgapp.data.CategoryRepository;
import com.nhom3.pubgapp.data.DataCallback;
import com.nhom3.pubgapp.data.ListingRepository;
import com.nhom3.pubgapp.data.MediaRepository;
import com.nhom3.pubgapp.model.BannedKeyword;
import com.nhom3.pubgapp.model.Category;
import com.nhom3.pubgapp.model.Listing;
import com.nhom3.pubgapp.model.enums.ImagePurpose;
import com.nhom3.pubgapp.model.enums.ListingStatus;
import com.nhom3.pubgapp.model.enums.SortOrder;
import com.nhom3.pubgapp.model.enums.TradeType;
import com.nhom3.pubgapp.util.KeywordFilter;
import com.nhom3.pubgapp.util.SessionManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

// State for the seller shelf and the listing form. Action bodies report NOT_READY until the repositories are filled.
public class ShopViewModel extends ViewModel {

    public static final String FIELD_TITLE = "title";
    public static final String FIELD_CATEGORY = "categoryId";
    public static final String FIELD_CONDITION = "itemCondition";
    public static final String FIELD_IMAGES = "images";
    public static final String FIELD_TRADE_TYPE = "tradeTypes";
    public static final String FIELD_PRICE = "price";

    public static final int TAB_ACTIVE = 0;
    public static final int TAB_SOLD = 1;
    public static final int TAB_HIDDEN = 2;
    public static final int TAB_DRAFT = 3;

    public static final String ACTION_EDIT = "ACTION_EDIT";
    public static final String ACTION_HIDE = "ACTION_HIDE";
    public static final String ACTION_SHOW = "ACTION_SHOW";
    public static final String ACTION_MARK_SOLD = "ACTION_MARK_SOLD";
    public static final String ACTION_REOPEN = "ACTION_REOPEN";
    public static final String ACTION_PUBLISH = "ACTION_PUBLISH";
    public static final String ACTION_DELETE = "ACTION_DELETE";
    public static final String ACTION_VIEW_REASON = "ACTION_VIEW_REASON";

    private final ListingRepository listingRepository = new ListingRepository();
    private final CategoryRepository categoryRepository = new CategoryRepository();
    private final MediaRepository mediaRepository = new MediaRepository();
    private final KeywordFilter keywordFilter = new KeywordFilter();

    private List<Listing> rawListings;
    private String cachedUid;
    private SortOrder currentSortOrder = SortOrder.NEWEST;
    private String currentCategoryId = "";
    private final MutableLiveData<List<Listing>> myListings = new MutableLiveData<>();
    private final MutableLiveData<Boolean> keywordFlag = new MutableLiveData<>();
    private final MutableLiveData<List<Category>> categories = new MutableLiveData<>();
    private final MutableLiveData<String> createdListingId = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> done = new MutableLiveData<>();

    public LiveData<List<Listing>> getMyListings() {
        return myListings;
    }

    public LiveData<Boolean> getKeywordFlag() {
        return keywordFlag;
    }

    public LiveData<List<Category>> getCategories() {
        return categories;
    }

    public LiveData<String> getCreatedListingId() {
        return createdListingId;
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

    public void loadMyListings(final SortOrder order, final String categoryId) {
        final String uid = SessionManager.getInstance().getCurrentUserId();
        if (uid == null || uid.isEmpty()) {
            error.setValue("Chưa đăng nhập");
            return;
        }

        currentSortOrder = order != null ? order : SortOrder.NEWEST;
        currentCategoryId = categoryId != null ? categoryId : "";

        if (rawListings != null && uid.equals(cachedUid)) {
            List<Listing> filtered = sortAndFilter(rawListings, currentSortOrder, currentCategoryId);
            myListings.setValue(filtered);
            return;
        }

        loading.setValue(true);
        listingRepository.getBySeller(uid, new DataCallback<List<Listing>>() {
            @Override
            public void onSuccess(List<Listing> data) {
                cachedUid = uid;
                rawListings = data;
                List<Listing> filtered = sortAndFilter(rawListings, currentSortOrder, currentCategoryId);
                myListings.postValue(filtered);
                loading.postValue(false);
            }

            @Override
            public void onError(String errorMessage) {
                loading.postValue(false);
                error.postValue(errorMessage);
            }
        });
    }

    public void loadCategories() {
        categoryRepository.getAll(new DataCallback<List<Category>>() {
            @Override
            public void onSuccess(List<Category> data) {
                categories.postValue(data);
            }

            @Override
            public void onError(String errorMessage) {
                error.postValue(errorMessage);
            }
        });
    }

    public void createListing(final Listing listing, final List<Uri> imageUris) {
        if (listing != null) {
            listing.setPrice(normalizePrice(listing.getTradeTypes(), listing.getPrice()));
        }
        int imageCount = imageUris != null ? imageUris.size() : 0;
        List<String> missing = findMissingFields(listing, imageCount);
        if (!missing.isEmpty()) {
            error.setValue("Vui lòng điền đủ các trường bắt buộc và chọn từ 1 đến 5 ảnh");
            return;
        }

        loading.setValue(true);
        clearError();

        // Check banned keywords before creating
        listingRepository.getBannedKeywords(new DataCallback<List<BannedKeyword>>() {
            @Override
            public void onSuccess(List<BannedKeyword> keywords) {
                checkKeywordsAndProceed(listing, imageUris, keywords);
            }

            @Override
            public void onError(String errorMessage) {
                keywordFlag.postValue(false);
                saveImagesAndCreate(listing, imageUris);
            }
        });
    }

    private void checkKeywordsAndProceed(Listing listing, List<Uri> imageUris,
                                         List<BannedKeyword> keywords) {
        String title = listing.getTitle() != null ? listing.getTitle() : "";
        String desc = listing.getDescription() != null ? listing.getDescription() : "";
        String combined = title + " " + desc;

        boolean hasBanned = false;
        try {
            keywordFilter.setKeywords(keywords);
            hasBanned = keywordFilter.containsBanned(combined);
        } catch (UnsupportedOperationException e) {
            if (keywords != null) {
                for (BannedKeyword k : keywords) {
                    if (k != null && k.matches(combined)) {
                        hasBanned = true;
                        break;
                    }
                }
            }
        }
        keywordFlag.postValue(hasBanned);
        saveImagesAndCreate(listing, imageUris);
    }

    private void saveImagesAndCreate(final Listing listing, final List<Uri> imageUris) {
        final List<String> savedIds = new ArrayList<>();
        saveNextImage(0, imageUris, savedIds, listing);
    }

    private void saveNextImage(final int index, final List<Uri> imageUris,
                               final List<String> savedIds, final Listing listing) {
        if (index >= imageUris.size()) {
            createListingAfterImages(listing, imageUris, savedIds);
            return;
        }

        Uri uri = imageUris.get(index);
        mediaRepository.saveImage(uri, ImagePurpose.LISTING, new DataCallback<String>() {
            @Override
            public void onSuccess(String imageId) {
                savedIds.add(imageId);
                saveNextImage(index + 1, imageUris, savedIds, listing);
            }

            @Override
            public void onError(String errorMessage) {
                rollbackImages(savedIds);
                loading.postValue(false);
                error.postValue(errorMessage);
            }
        });
    }

    private void createListingAfterImages(final Listing listing, final List<Uri> imageUris,
                                          final List<String> savedIds) {
        Uri firstUri = imageUris.get(0);
        mediaRepository.makeThumb(firstUri, new DataCallback<String>() {
            @Override
            public void onSuccess(String thumbBase64) {
                listing.setImageIds(savedIds);
                listing.setCoverThumb(thumbBase64);
                submitListing(listing, savedIds);
            }

            @Override
            public void onError(String errorMessage) {
                rollbackImages(savedIds);
                loading.postValue(false);
                error.postValue(errorMessage);
            }
        });
    }

    private void submitListing(Listing listing, final List<String> savedIds) {
        listingRepository.create(listing, new DataCallback<String>() {
            @Override
            public void onSuccess(String id) {
                rawListings = null;
                createdListingId.postValue(id);
                loading.postValue(false);
                done.postValue(true);
            }

            @Override
            public void onError(String errorMessage) {
                rollbackImages(savedIds);
                loading.postValue(false);
                error.postValue(errorMessage);
            }
        });
    }

    private void rollbackImages(List<String> imageIds) {
        DataCallback<Void> noop = new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
            }

            @Override
            public void onError(String errorMessage) {
            }
        };
        for (String id : imageIds) {
            mediaRepository.delete(id, noop);
        }
    }

    public void saveDraft(final Listing listing, final List<Uri> imageUris) {
        int imageCount = imageUris != null ? imageUris.size() : 0;
        if (!canSaveDraft(listing, imageCount)) {
            error.setValue("Hãy nhập tiêu đề, mô tả hoặc chọn ít nhất 1 ảnh để lưu nháp");
            return;
        }
        loading.setValue(true);
        clearError();

        if (imageUris == null || imageUris.isEmpty()) {
            listingRepository.saveDraft(listing, new DataCallback<String>() {
                @Override
                public void onSuccess(String id) {
                    rawListings = null;
                    createdListingId.postValue(id);
                    loading.postValue(false);
                    done.postValue(true);
                }

                @Override
                public void onError(String errorMessage) {
                    loading.postValue(false);
                    error.postValue(errorMessage);
                }
            });
            return;
        }

        final List<String> savedIds = new ArrayList<String>();
        saveNextDraftImage(0, imageUris, savedIds, listing);
    }

    private void saveNextDraftImage(final int index, final List<Uri> imageUris,
                                    final List<String> savedIds, final Listing listing) {
        if (index >= imageUris.size()) {
            finishSaveDraft(listing, imageUris, savedIds);
            return;
        }

        Uri uri = imageUris.get(index);
        mediaRepository.saveImage(uri, ImagePurpose.LISTING, new DataCallback<String>() {
            @Override
            public void onSuccess(String imageId) {
                savedIds.add(imageId);
                saveNextDraftImage(index + 1, imageUris, savedIds, listing);
            }

            @Override
            public void onError(String errorMessage) {
                rollbackImages(savedIds);
                loading.postValue(false);
                error.postValue(errorMessage);
            }
        });
    }

    private void finishSaveDraft(final Listing listing, final List<Uri> imageUris,
                                 final List<String> savedIds) {
        listing.setImageIds(savedIds);
        Uri firstUri = imageUris.get(0);
        mediaRepository.makeThumb(firstUri, new DataCallback<String>() {
            @Override
            public void onSuccess(String thumbBase64) {
                listing.setCoverThumb(thumbBase64);
                submitSaveDraft(listing, savedIds);
            }

            @Override
            public void onError(String errorMessage) {
                rollbackImages(savedIds);
                loading.postValue(false);
                error.postValue(errorMessage);
            }
        });
    }

    private void submitSaveDraft(final Listing listing, final List<String> savedIds) {
        listingRepository.saveDraft(listing, new DataCallback<String>() {
            @Override
            public void onSuccess(String id) {
                rawListings = null;
                createdListingId.postValue(id);
                loading.postValue(false);
                done.postValue(true);
            }

            @Override
            public void onError(String errorMessage) {
                rollbackImages(savedIds);
                loading.postValue(false);
                error.postValue(errorMessage);
            }
        });
    }

    public void publishDraft(final String id) {
        Listing target = null;
        if (rawListings != null) {
            for (Listing item : rawListings) {
                if (item != null && id != null && id.equals(item.getId())) {
                    target = item;
                    break;
                }
            }
        }
        if (target == null) {
            error.setValue("Không tìm thấy bản nháp");
            return;
        }

        loading.setValue(true);
        clearError();
        listingRepository.publish(target, new DataCallback<String>() {
            @Override
            public void onSuccess(String resultId) {
                loading.postValue(false);
                done.postValue(true);
                rawListings = null;
                loadMyListings(currentSortOrder, currentCategoryId);
            }

            @Override
            public void onError(String errorMessage) {
                loading.postValue(false);
                error.postValue(errorMessage);
            }
        });
    }

    public void updateListing(final Listing listing, final List<Uri> imageUris) {
        if (listing == null || listing.getId() == null || listing.getId().isEmpty()) {
            error.setValue("Mã tin đăng không được để trống");
            return;
        }
        loading.setValue(true);
        clearError();

        Listing target = null;
        if (rawListings != null) {
            for (Listing item : rawListings) {
                if (item != null && listing.getId().equals(item.getId())) {
                    target = item;
                    break;
                }
            }
        }
        final List<String> removedImageIds = new ArrayList<String>();
        if (target != null && target.getImageIds() != null) {
            List<String> keptIds = listing.getImageIds();
            for (String oldId : target.getImageIds()) {
                if (keptIds == null || !keptIds.contains(oldId)) {
                    removedImageIds.add(oldId);
                }
            }
        }

        if (imageUris == null || imageUris.isEmpty()) {
            listingRepository.update(listing, new DataCallback<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    deleteRemovedImages(removedImageIds);
                    loading.postValue(false);
                    done.postValue(true);
                    rawListings = null;
                    loadMyListings(currentSortOrder, currentCategoryId);
                }

                @Override
                public void onError(String errorMessage) {
                    loading.postValue(false);
                    error.postValue(errorMessage);
                }
            });
            return;
        }

        final List<String> newSavedIds = new ArrayList<String>();
        saveNextUpdateImage(0, imageUris, newSavedIds, listing, removedImageIds);
    }

    private void saveNextUpdateImage(final int index, final List<Uri> imageUris,
                                     final List<String> newSavedIds, final Listing listing,
                                     final List<String> removedImageIds) {
        if (index >= imageUris.size()) {
            finishUpdateListing(listing, imageUris, newSavedIds, removedImageIds);
            return;
        }

        Uri uri = imageUris.get(index);
        mediaRepository.saveImage(uri, ImagePurpose.LISTING, new DataCallback<String>() {
            @Override
            public void onSuccess(String imageId) {
                newSavedIds.add(imageId);
                saveNextUpdateImage(index + 1, imageUris, newSavedIds, listing, removedImageIds);
            }

            @Override
            public void onError(String errorMessage) {
                rollbackImages(newSavedIds);
                loading.postValue(false);
                error.postValue(errorMessage);
            }
        });
    }

    private void finishUpdateListing(final Listing listing, final List<Uri> imageUris,
                                     final List<String> newSavedIds,
                                     final List<String> removedImageIds) {
        List<String> currentIds = listing.getImageIds();
        if (currentIds == null) {
            currentIds = new ArrayList<String>();
        }
        boolean wasEmpty = currentIds.isEmpty();
        currentIds.addAll(newSavedIds);
        listing.setImageIds(currentIds);

        if (wasEmpty && imageUris != null && !imageUris.isEmpty()) {
            mediaRepository.makeThumb(imageUris.get(0), new DataCallback<String>() {
                @Override
                public void onSuccess(String thumbBase64) {
                    listing.setCoverThumb(thumbBase64);
                    submitUpdateListing(listing, newSavedIds, removedImageIds);
                }

                @Override
                public void onError(String errorMessage) {
                    rollbackImages(newSavedIds);
                    loading.postValue(false);
                    error.postValue(errorMessage);
                }
            });
            return;
        }
        submitUpdateListing(listing, newSavedIds, removedImageIds);
    }

    private void submitUpdateListing(final Listing listing, final List<String> newSavedIds,
                                     final List<String> removedImageIds) {
        listingRepository.update(listing, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                deleteRemovedImages(removedImageIds);
                loading.postValue(false);
                done.postValue(true);
                rawListings = null;
                loadMyListings(currentSortOrder, currentCategoryId);
            }

            @Override
            public void onError(String errorMessage) {
                rollbackImages(newSavedIds);
                loading.postValue(false);
                error.postValue(errorMessage);
            }
        });
    }

    private void deleteRemovedImages(List<String> removedImageIds) {
        if (removedImageIds == null) {
            return;
        }
        DataCallback<Void> noop = new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
            }

            @Override
            public void onError(String errorMessage) {
            }
        };
        for (String id : removedImageIds) {
            mediaRepository.delete(id, noop);
        }
    }

    public void changeStatus(final String id, final ListingStatus status) {
        Listing target = null;
        if (rawListings != null) {
            for (Listing item : rawListings) {
                if (item != null && id != null && id.equals(item.getId())) {
                    target = item;
                    break;
                }
            }
        }
        if (target == null) {
            error.setValue("Không tìm thấy tin đăng");
            return;
        }
        ListingStatus from = target.getStatus();
        loading.setValue(true);
        clearError();
        listingRepository.setStatus(id, from, status, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                loading.postValue(false);
                done.postValue(true);
                rawListings = null;
                loadMyListings(currentSortOrder, currentCategoryId);
            }

            @Override
            public void onError(String errorMessage) {
                loading.postValue(false);
                error.postValue(errorMessage);
            }
        });
    }

    public void setHidden(final String id, final boolean hidden) {
        loading.setValue(true);
        clearError();
        listingRepository.setHidden(id, hidden, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                loading.postValue(false);
                done.postValue(true);
                rawListings = null;
                loadMyListings(currentSortOrder, currentCategoryId);
            }

            @Override
            public void onError(String errorMessage) {
                loading.postValue(false);
                error.postValue(errorMessage);
            }
        });
    }

    public void deleteListing(final String id) {
        loading.setValue(true);
        clearError();
        listingRepository.delete(id, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                loading.postValue(false);
                done.postValue(true);
                rawListings = null;
                loadMyListings(currentSortOrder, currentCategoryId);
            }

            @Override
            public void onError(String errorMessage) {
                loading.postValue(false);
                error.postValue(errorMessage);
            }
        });
    }

    public void clearError() {
        error.setValue(null);
    }

    public void clearDone() {
        done.setValue(false);
        createdListingId.setValue(null);
    }

    // Validates form fields and returns missing field names in order.
    static List<String> findMissingFields(Listing listing, int imageCount) {
        List<String> missing = new ArrayList<>();
        if (listing == null || listing.getTitle() == null || listing.getTitle().trim().isEmpty()) {
            missing.add(FIELD_TITLE);
        }
        if (listing == null || listing.getCategoryId() == null || listing.getCategoryId().trim().isEmpty()) {
            missing.add(FIELD_CATEGORY);
        }
        if (listing == null || listing.getItemCondition() == null) {
            missing.add(FIELD_CONDITION);
        }
        if (imageCount < 1 || imageCount > 5) {
            missing.add(FIELD_IMAGES);
        }
        if (listing == null || listing.getTradeTypes() == null || listing.getTradeTypes().isEmpty()) {
            missing.add(FIELD_TRADE_TYPE);
        }
        if (listing != null && listing.getTradeTypes() != null
                && listing.getTradeTypes().contains(TradeType.SELL)
                && listing.getPrice() <= 0) {
            missing.add(FIELD_PRICE);
        }
        return missing;
    }

    // Clears the price when the listing is not for sale or is a giveaway.
    static long normalizePrice(List<TradeType> tradeTypes, long typedPrice) {
        if (tradeTypes == null || !tradeTypes.contains(TradeType.SELL)
                || tradeTypes.contains(TradeType.GIVEAWAY)) {
            return 0L;
        }
        return typedPrice;
    }

    // Filters listings for one of the four tabs on the seller shelf screen.
    static List<Listing> filterByTab(List<Listing> listings, int tab) {
        List<Listing> result = new ArrayList<Listing>();
        if (listings == null) {
            return result;
        }
        for (Listing listing : listings) {
            if (listing == null || listing.getStatus() == null) {
                continue;
            }
            ListingStatus status = listing.getStatus();
            if (tab == TAB_ACTIVE) {
                if (status == ListingStatus.AVAILABLE || status == ListingStatus.RESERVED) {
                    result.add(listing);
                }
            } else if (tab == TAB_SOLD) {
                if (status == ListingStatus.SOLD) {
                    result.add(listing);
                }
            } else if (tab == TAB_HIDDEN) {
                if (status == ListingStatus.HIDDEN || status == ListingStatus.REMOVED) {
                    result.add(listing);
                }
            } else if (tab == TAB_DRAFT) {
                if (status == ListingStatus.DRAFT) {
                    result.add(listing);
                }
            }
        }
        return result;
    }

    // Filters listings by category and sorts them by date descending for NEWEST.
    static List<Listing> sortAndFilter(List<Listing> listings, SortOrder order, String categoryId) {
        List<Listing> result = new ArrayList<Listing>();
        if (listings == null) {
            return result;
        }
        boolean hasCategory = categoryId != null && !categoryId.trim().isEmpty();
        for (Listing listing : listings) {
            if (listing == null) {
                continue;
            }
            if (hasCategory) {
                if (listing.getCategoryId() != null && listing.getCategoryId().equals(categoryId.trim())) {
                    result.add(listing);
                }
            } else {
                result.add(listing);
            }
        }

        Collections.sort(result, new Comparator<Listing>() {
            @Override
            public int compare(Listing o1, Listing o2) {
                if (o1 == null && o2 == null) {
                    return 0;
                }
                if (o1 == null || o1.getCreatedAt() == null) {
                    return 1;
                }
                if (o2 == null || o2.getCreatedAt() == null) {
                    return -1;
                }
                return o2.getCreatedAt().compareTo(o1.getCreatedAt());
            }
        });
        return result;
    }

    // Returns action codes permitted on the card menu for a listing in its current state.
    static List<String> allowedActions(Listing listing) {
        List<String> actions = new ArrayList<String>();
        if (listing == null || listing.getStatus() == null) {
            return actions;
        }
        ListingStatus status = listing.getStatus();
        if (status == ListingStatus.AVAILABLE) {
            actions.add(ACTION_EDIT);
            actions.add(ACTION_MARK_SOLD);
            actions.add(ACTION_HIDE);
        } else if (status == ListingStatus.RESERVED) {
            // Under reservation, seller cannot change state directly.
        } else if (status == ListingStatus.HIDDEN) {
            String reason = listing.getStatusReason();
            if (reason == null || reason.trim().isEmpty()) {
                actions.add(ACTION_SHOW);
                actions.add(ACTION_EDIT);
            } else {
                actions.add(ACTION_VIEW_REASON);
            }
        } else if (status == ListingStatus.SOLD) {
            actions.add(ACTION_REOPEN);
        } else if (status == ListingStatus.DRAFT) {
            actions.add(ACTION_EDIT);
            actions.add(ACTION_PUBLISH);
            actions.add(ACTION_DELETE);
        } else if (status == ListingStatus.REMOVED) {
            // Removed listing is read-only.
        }
        return actions;
    }

    // Checks whether transitioning between two listing statuses is permitted directly.
    static boolean isAllowedStatusChange(ListingStatus from, ListingStatus to) {
        if (from == ListingStatus.AVAILABLE && to == ListingStatus.SOLD) {
            return true;
        }
        if (from == ListingStatus.SOLD && to == ListingStatus.AVAILABLE) {
            return true;
        }
        return false;
    }

    // Checks whether a draft has minimum required content (title, description, or at least 1 image).
    static boolean canSaveDraft(Listing listing, int imageCount) {
        if (listing == null) {
            return false;
        }
        boolean hasTitle = listing.getTitle() != null && !listing.getTitle().trim().isEmpty();
        boolean hasDesc = listing.getDescription() != null && !listing.getDescription().trim().isEmpty();
        boolean hasImages = imageCount > 0;
        return hasTitle || hasDesc || hasImages;
    }
}
