package com.nhom3.pubgapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.nhom3.pubgapp.data.DataCallback;
import com.nhom3.pubgapp.model.Category;
import com.nhom3.pubgapp.model.Listing;
import com.nhom3.pubgapp.model.enums.SortOrder;

import java.util.List;

// State for the home feed and the For You section. Action bodies report NOT_READY until the repositories are filled.
public class FeedViewModel extends ViewModel {

    private final MutableLiveData<List<Listing>> feed = new MutableLiveData<>();
    private final MutableLiveData<List<Listing>> forYou = new MutableLiveData<>();
    private final MutableLiveData<List<Category>> categories = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> done = new MutableLiveData<>();

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
        error.setValue(DataCallback.NOT_READY);
    }

    public void loadCategories() {
        error.setValue(DataCallback.NOT_READY);
    }

    public void loadNext() {
        error.setValue(DataCallback.NOT_READY);
    }

    public void loadForYou() {
        error.setValue(DataCallback.NOT_READY);
    }

    public void refresh() {
        error.setValue(DataCallback.NOT_READY);
    }

    public void clearError() {
        error.setValue(null);
    }

    public void clearDone() {
        done.setValue(false);
    }
}
