package com.nhom3.pubgapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.nhom3.pubgapp.data.DataCallback;
import com.nhom3.pubgapp.data.DealRepository;
import com.nhom3.pubgapp.model.Deal;
import com.nhom3.pubgapp.model.Payment;
import com.nhom3.pubgapp.model.User;
import com.nhom3.pubgapp.model.enums.DealStatus;
import com.nhom3.pubgapp.util.SessionManager;

import java.util.ArrayList;
import java.util.List;

// State for the admin deal list and detail screens. Action bodies report NOT_READY until the repositories are filled.
public class AdminDealViewModel extends ViewModel {

    private final DealRepository dealRepository = new DealRepository();
    private final MutableLiveData<List<Deal>> deals = new MutableLiveData<>();
    private final MutableLiveData<Deal> selected = new MutableLiveData<>();
    private final MutableLiveData<Payment> payment = new MutableLiveData<>();
    private final MutableLiveData<User> buyer = new MutableLiveData<>();
    private final MutableLiveData<User> seller = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> done = new MutableLiveData<>();

    public LiveData<List<Deal>> getDeals() {
        return deals;
    }

    public LiveData<Deal> getSelected() {
        return selected;
    }

    public LiveData<Payment> getPayment() {
        return payment;
    }

    public LiveData<User> getBuyer() {
        return buyer;
    }

    public LiveData<User> getSeller() {
        return seller;
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

    public void loadDeals(DealStatus status) {
        if (!SessionManager.getInstance().isAdmin()) {
            error.setValue("Chỉ quản trị viên mới có quyền xem giao dịch");
            return;
        }
        loading.setValue(true);
        dealRepository.listAll(status, new DataCallback<List<Deal>>() {
            @Override
            public void onSuccess(List<Deal> result) {
                loading.postValue(false);
                deals.postValue(result != null ? result : new ArrayList<Deal>());
            }

            @Override
            public void onError(String message) {
                loading.postValue(false);
                error.postValue(message);
            }
        });
    }

    public void loadDetail(String dealId) {
        error.setValue(DataCallback.NOT_READY);
    }

    public void clearError() {
        error.setValue(null);
    }

    public void clearDone() {
        done.setValue(false);
    }
}
