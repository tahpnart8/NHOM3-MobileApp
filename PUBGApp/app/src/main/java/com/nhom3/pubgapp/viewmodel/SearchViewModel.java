package com.nhom3.pubgapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.nhom3.pubgapp.data.DataCallback;
import com.nhom3.pubgapp.model.Category;
import com.nhom3.pubgapp.model.Listing;
import com.nhom3.pubgapp.model.SearchFilter;
import com.nhom3.pubgapp.model.User;

import java.util.List;

// State for keyword search, filters and user search. Action bodies report NOT_READY until the repositories are filled.
public class SearchViewModel extends ViewModel {

    private final MutableLiveData<List<Listing>> results = new MutableLiveData<>();
    private final MutableLiveData<List<User>> userResults = new MutableLiveData<>();
    private final MutableLiveData<List<Category>> categories = new MutableLiveData<>();
    private final MutableLiveData<List<String>> recentSearches = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> done = new MutableLiveData<>();

    // Plain field, not LiveData: the ViewModel itself survives rotation.
    private SearchFilter filter;

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

    public void setKeyword(String text) {
        error.setValue(DataCallback.NOT_READY);
    }

    public void applyFilter(SearchFilter filter) {
        error.setValue(DataCallback.NOT_READY);
    }

    public void search() {
        error.setValue(DataCallback.NOT_READY);
    }

    public void loadNext() {
        error.setValue(DataCallback.NOT_READY);
    }

    public void searchUsers(String keyword) {
        error.setValue(DataCallback.NOT_READY);
    }

    public void loadCategories() {
        error.setValue(DataCallback.NOT_READY);
    }

    public void loadRecentSearches() {
        error.setValue(DataCallback.NOT_READY);
    }

    public void clearError() {
        error.setValue(null);
    }

    public void clearDone() {
        done.setValue(false);
    }
}
