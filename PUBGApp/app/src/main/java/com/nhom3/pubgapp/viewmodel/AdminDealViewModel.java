package com.nhom3.pubgapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.nhom3.pubgapp.data.DataCallback;
import com.nhom3.pubgapp.data.DealRepository;
import com.nhom3.pubgapp.data.UserRepository;
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

    public void loadDetail(final String dealId) {
        if (!SessionManager.getInstance().isAdmin()) {
            error.setValue("Chỉ quản trị viên mới có quyền xem giao dịch");
            return;
        }
        if (dealId == null || dealId.trim().isEmpty()) {
            return;
        }
        loading.setValue(true);
        dealRepository.getById(dealId, new DataCallback<Deal>() {
            @Override
            public void onSuccess(final Deal result) {
                selected.postValue(result);
                if (result == null) {
                    loading.postValue(false);
                    return;
                }
                if (result.getBuyerId() != null && !result.getBuyerId().isEmpty()) {
                    new UserRepository().getProfile(result.getBuyerId(), new DataCallback<User>() {
                        @Override
                        public void onSuccess(User userResult) {
                            buyer.postValue(userResult);
                        }

                        @Override
                        public void onError(String message) {
                        }
                    });
                }
                if (result.getSellerId() != null && !result.getSellerId().isEmpty()) {
                    new UserRepository().getProfile(result.getSellerId(), new DataCallback<User>() {
                        @Override
                        public void onSuccess(User userResult) {
                            seller.postValue(userResult);
                        }

                        @Override
                        public void onError(String message) {
                        }
                    });
                }
                if (result.needsPayment()) {
                    dealRepository.getPayment(dealId, new DataCallback<Payment>() {
                        @Override
                        public void onSuccess(Payment paymentResult) {
                            loading.postValue(false);
                            payment.postValue(paymentResult);
                        }

                        @Override
                        public void onError(String message) {
                            loading.postValue(false);
                        }
                    });
                } else {
                    loading.postValue(false);
                }
            }

            @Override
            public void onError(String message) {
                loading.postValue(false);
                error.postValue(message);
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
