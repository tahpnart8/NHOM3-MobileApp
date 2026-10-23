package com.nhom3.pubgapp.viewmodel;

import android.net.Uri;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.nhom3.pubgapp.data.DataCallback;
import com.nhom3.pubgapp.model.Category;
import com.nhom3.pubgapp.model.Listing;
import com.nhom3.pubgapp.model.enums.ListingStatus;
import com.nhom3.pubgapp.model.enums.SortOrder;
import com.nhom3.pubgapp.model.enums.TradeType;

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
        error.setValue(DataCallback.NOT_READY);
    }

    public void createListing(Listing listing, List<Uri> imageUris) {
        error.setValue(DataCallback.NOT_READY);
    }

    public void saveDraft(Listing listing, List<Uri> imageUris) {
        error.setValue(DataCallback.NOT_READY);
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
}
