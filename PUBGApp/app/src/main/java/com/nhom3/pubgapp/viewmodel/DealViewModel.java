package com.nhom3.pubgapp.viewmodel;

import android.net.Uri;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.nhom3.pubgapp.data.DataCallback;
import com.nhom3.pubgapp.model.Deal;
import com.nhom3.pubgapp.model.Payment;
import com.nhom3.pubgapp.model.Review;
import com.nhom3.pubgapp.model.enums.PaymentMethod;

import java.util.List;

// State for the deal screens, from creation to completion. Action bodies report NOT_READY until the repositories are filled.
public class DealViewModel extends ViewModel {

    private final MutableLiveData<Deal> deal = new MutableLiveData<>();
    private final MutableLiveData<List<Deal>> deals = new MutableLiveData<>();
    private final MutableLiveData<Payment> payment = new MutableLiveData<>();
    private final MutableLiveData<List<Payment>> payments = new MutableLiveData<>();
    private final MutableLiveData<Review> myReview = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> done = new MutableLiveData<>();

    public LiveData<Deal> getDeal() {
        return deal;
    }

    public LiveData<List<Deal>> getDeals() {
        return deals;
    }

    public LiveData<Payment> getPayment() {
        return payment;
    }

    public LiveData<List<Payment>> getPayments() {
        return payments;
    }

    public LiveData<Review> getMyReview() {
        return myReview;
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

    public void loadDeal(String id) {
        error.setValue(DataCallback.NOT_READY);
    }

    public void updateTerms(Deal deal) {
        error.setValue(DataCallback.NOT_READY);
    }

    public void confirm() {
        error.setValue(DataCallback.NOT_READY);
    }

    public void pay(PaymentMethod method) {
        error.setValue(DataCallback.NOT_READY);
    }

    public void loadMyReview() {
        error.setValue(DataCallback.NOT_READY);
    }

    public void markShipped(String carrier, String trackingCode, Uri photoUri) {
        error.setValue(DataCallback.NOT_READY);
    }

    public void markReceived(Uri photoUri) {
        error.setValue(DataCallback.NOT_READY);
    }

    public void checkAutoRelease() {
        error.setValue(DataCallback.NOT_READY);
    }

    public void cancel(String reason) {
        error.setValue(DataCallback.NOT_READY);
    }

    public void loadHistory() {
        error.setValue(DataCallback.NOT_READY);
    }

    public void loadPayments() {
        error.setValue(DataCallback.NOT_READY);
    }

    public void clearError() {
        error.setValue(null);
    }

    public void clearDone() {
        done.setValue(false);
    }
}
