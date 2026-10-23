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

import java.util.List;

// State for the seller shelf and the listing form. Action bodies report NOT_READY until the repositories are filled.
public class ShopViewModel extends ViewModel {

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
}
