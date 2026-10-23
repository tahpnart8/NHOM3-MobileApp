package com.nhom3.pubgapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.nhom3.pubgapp.data.DataCallback;
import com.nhom3.pubgapp.model.Listing;
import com.nhom3.pubgapp.model.ProfileStats;
import com.nhom3.pubgapp.model.User;

import java.util.List;

// State for the listing detail screen. Action bodies report NOT_READY until the repositories are filled.
public class ListingDetailViewModel extends ViewModel {

    private final MutableLiveData<Listing> listing = new MutableLiveData<>();
    private final MutableLiveData<User> seller = new MutableLiveData<>();
    private final MutableLiveData<ProfileStats> sellerStats = new MutableLiveData<>();
    private final MutableLiveData<List<byte[]>> images = new MutableLiveData<>();
    private final MutableLiveData<List<Listing>> similar = new MutableLiveData<>();
    private final MutableLiveData<Boolean> saved = new MutableLiveData<>();
    private final MutableLiveData<Boolean> followingSeller = new MutableLiveData<>();
    private final MutableLiveData<long[]> priceRange = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> done = new MutableLiveData<>();

    public LiveData<Listing> getListing() {
        return listing;
    }

    public LiveData<User> getSeller() {
        return seller;
    }

    public LiveData<ProfileStats> getSellerStats() {
        return sellerStats;
    }

    public LiveData<List<byte[]>> getImages() {
        return images;
    }

    public LiveData<List<Listing>> getSimilar() {
        return similar;
    }

    public LiveData<Boolean> getSaved() {
        return saved;
    }

    public LiveData<Boolean> getFollowingSeller() {
        return followingSeller;
    }

    public LiveData<long[]> getPriceRange() {
        return priceRange;
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

    public void load(String listingId) {
        error.setValue(DataCallback.NOT_READY);
    }

    public void loadImages() {
        error.setValue(DataCallback.NOT_READY);
    }

    public void toggleSave() {
        error.setValue(DataCallback.NOT_READY);
    }

    public void toggleFollowSeller() {
        error.setValue(DataCallback.NOT_READY);
    }

    public void loadSimilar() {
        error.setValue(DataCallback.NOT_READY);
    }

    public void loadPriceRange() {
        error.setValue(DataCallback.NOT_READY);
    }

    public void clearError() {
        error.setValue(null);
    }

    public void clearDone() {
        done.setValue(false);
    }
}
