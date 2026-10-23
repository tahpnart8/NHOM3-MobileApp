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

import java.util.ArrayList;
import java.util.List;

// State for the seller shelf and the listing form. Action bodies report NOT_READY until the repositories are filled.
public class ShopViewModel extends ViewModel {

    public static final String FIELD_TITLE = "title";
    public static final String FIELD_CATEGORY = "categoryId";
    public static final String FIELD_CONDITION = "itemCondition";
    public static final String FIELD_IMAGES = "images";
    public static final String FIELD_TRADE_TYPE = "tradeTypes";
    public static final String FIELD_PRICE = "price";

    private final ListingRepository listingRepository = new ListingRepository();
    private final CategoryRepository categoryRepository = new CategoryRepository();
    private final MediaRepository mediaRepository = new MediaRepository();
    private final KeywordFilter keywordFilter = new KeywordFilter();

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

    public void loadMyListings(SortOrder order, String categoryId) {
        error.setValue(DataCallback.NOT_READY);
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
            error.postValue("Vui lòng điền đủ các trường bắt buộc và chọn từ 1 đến 5 ảnh");
            return;
        }

        loading.postValue(true);
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
            error.postValue("Hãy nhập tiêu đề, mô tả hoặc chọn ít nhất 1 ảnh để lưu nháp");
            return;
        }

        loading.postValue(true);
        clearError();

        if (imageCount > 0) {
            final List<String> savedIds = new ArrayList<>();
            saveNextDraftImage(0, imageUris, savedIds, listing);
        } else {
            submitDraft(listing, new ArrayList<String>());
        }
    }

    private void saveNextDraftImage(final int index, final List<Uri> imageUris,
                                    final List<String> savedIds, final Listing listing) {
        if (index >= imageUris.size()) {
            makeThumbAndSubmitDraft(listing, imageUris, savedIds);
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

    private void makeThumbAndSubmitDraft(final Listing listing, final List<Uri> imageUris,
                                         final List<String> savedIds) {
        Uri firstUri = imageUris.get(0);
        mediaRepository.makeThumb(firstUri, new DataCallback<String>() {
            @Override
            public void onSuccess(String thumbBase64) {
                listing.setImageIds(savedIds);
                listing.setCoverThumb(thumbBase64);
                submitDraft(listing, savedIds);
            }

            @Override
            public void onError(String errorMessage) {
                rollbackImages(savedIds);
                loading.postValue(false);
                error.postValue(errorMessage);
            }
        });
    }

    private void submitDraft(Listing listing, final List<String> savedIds) {
        listingRepository.saveDraft(listing, new DataCallback<String>() {
            @Override
            public void onSuccess(String id) {
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

    public void publishDraft(String id) {
        error.setValue(DataCallback.NOT_READY);
    }

    public void updateListing(Listing listing, List<Uri> imageUris) {
        error.setValue(DataCallback.NOT_READY);
    }

    public void changeStatus(String id, ListingStatus status) {
        error.setValue(DataCallback.NOT_READY);
    }

    public void setHidden(String id, boolean hidden) {
        error.setValue(DataCallback.NOT_READY);
    }

    public void deleteListing(String id) {
        error.setValue(DataCallback.NOT_READY);
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
