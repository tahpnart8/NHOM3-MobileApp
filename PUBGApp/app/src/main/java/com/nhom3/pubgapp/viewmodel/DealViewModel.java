package com.nhom3.pubgapp.viewmodel;

import android.net.Uri;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.nhom3.pubgapp.data.DataCallback;
import com.nhom3.pubgapp.data.DealRepository;
import com.nhom3.pubgapp.data.ReviewRepository;
import com.nhom3.pubgapp.model.Deal;
import com.nhom3.pubgapp.model.Payment;
import com.nhom3.pubgapp.model.Review;
import com.nhom3.pubgapp.model.enums.PaymentMethod;

import java.util.List;

// State for the deal screens, from creation to completion. Action bodies report NOT_READY until the repositories are filled.
public class DealViewModel extends ViewModel {

    private final DealRepository dealRepository;
    // Built lazily, so unit tests that pass a fake DealRepository never reach Firestore.
    private ReviewRepository reviewRepository;

    private final MutableLiveData<Deal> deal = new MutableLiveData<>();
    private final MutableLiveData<List<Deal>> deals = new MutableLiveData<>();
    private final MutableLiveData<Payment> payment = new MutableLiveData<>();
    private final MutableLiveData<List<Payment>> payments = new MutableLiveData<>();
    private final MutableLiveData<Review> myReview = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> done = new MutableLiveData<>();

    public DealViewModel() {
        this(new DealRepository());
    }

    DealViewModel(DealRepository dealRepository) {
        this.dealRepository = dealRepository;
    }

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
        if (id == null || id.trim().isEmpty()) {
            return;
        }
        loading.setValue(true);
        dealRepository.getById(id, new DataCallback<Deal>() {
            @Override
            public void onSuccess(Deal result) {
                deal.postValue(result);
                if (result != null && result.needsPayment()) {
                    dealRepository.getPayment(id, new DataCallback<Payment>() {
                        @Override
                        public void onSuccess(Payment paymentResult) {
                            payment.postValue(paymentResult);
                            loading.postValue(false);
                        }

                        @Override
                        public void onError(String message) {
                            payment.postValue(null);
                            loading.postValue(false);
                        }
                    });
                } else {
                    payment.postValue(null);
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

    public void updateTerms(Deal dealToUpdate) {
        if (dealToUpdate == null) {
            return;
        }
        loading.setValue(true);
        dealRepository.updateTerms(dealToUpdate, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                loading.postValue(false);
                done.postValue(true);
                loadDeal(dealToUpdate.getId());
            }

            @Override
            public void onError(String message) {
                loading.postValue(false);
                error.postValue(message);
            }
        });
    }

    public void confirm() {
        Deal current = deal.getValue();
        if (current == null) {
            error.setValue("Không tìm thấy giao dịch để xác nhận.");
            return;
        }
        loading.setValue(true);
        dealRepository.confirm(current.getId(), new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                loading.postValue(false);
                done.postValue(true);
                loadDeal(current.getId());
            }

            @Override
            public void onError(String message) {
                loading.postValue(false);
                error.postValue(message);
            }
        });
    }

    public void pay(PaymentMethod method) {
        Deal current = deal.getValue();
        if (current == null) {
            error.setValue("Không tìm thấy giao dịch để thanh toán.");
            return;
        }
        loading.setValue(true);
        dealRepository.pay(current.getId(), method, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                loading.postValue(false);
                done.postValue(true);
                loadDeal(current.getId());
            }

            @Override
            public void onError(String message) {
                loading.postValue(false);
                error.postValue(message);
                if (message != null && message.contains("đã được thanh toán")) {
                    loadDeal(current.getId());
                }
            }
        });
    }

    public void loadMyReview() {
        Deal current = deal.getValue();
        if (current == null || current.getId() == null || current.getId().trim().isEmpty()) {
            return;
        }
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            myReview.setValue(null);
            return;
        }
        final String uid = user.getUid();
        if (reviewRepository == null) {
            reviewRepository = new ReviewRepository();
        }
        reviewRepository.getByDeal(current.getId(), new DataCallback<List<Review>>() {
            @Override
            public void onSuccess(List<Review> result) {
                myReview.postValue(findReviewBy(result, uid));
            }

            @Override
            public void onError(String message) {
                error.postValue(message);
            }
        });
    }

    public void markShipped(String carrier, String trackingCode, Uri photoUri) {
        error.setValue(DataCallback.NOT_READY);
    }

    public void markReceived(Uri photoUri) {
        error.setValue(DataCallback.NOT_READY);
    }

    public void checkAutoRelease() {
        dealRepository.releaseDueDeals(new DataCallback<Integer>() {
            @Override
            public void onSuccess(Integer result) {
                dealRepository.completeDueDeals(new DataCallback<Integer>() {
                    @Override
                    public void onSuccess(Integer res2) {
                        dealRepository.expirePendingDeals(new DataCallback<Integer>() {
                            @Override
                            public void onSuccess(Integer res3) {
                            }

                            @Override
                            public void onError(String message) {
                            }
                        });
                    }

                    @Override
                    public void onError(String message) {
                    }
                });
            }

            @Override
            public void onError(String message) {
            }
        });
    }

    public void cancel(String reason) {
        Deal current = deal.getValue();
        if (current == null) {
            error.setValue("Không tìm thấy giao dịch để hủy.");
            return;
        }
        loading.setValue(true);
        dealRepository.cancel(current.getId(), reason, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                loading.postValue(false);
                done.postValue(true);
                loadDeal(current.getId());
            }

            @Override
            public void onError(String message) {
                loading.postValue(false);
                error.postValue(message);
            }
        });
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

    static boolean canEditTerms(Deal deal, String userId, boolean isVerified, boolean isOnline) {
        if (deal == null || !isOnline || !isVerified) {
            return false;
        }
        return deal.getStatus() == com.nhom3.pubgapp.model.enums.DealStatus.PENDING_CONFIRM
                && deal.isParticipant(userId);
    }

    static boolean canConfirm(Deal deal, String userId, boolean isVerified, boolean isOnline) {
        if (deal == null || !isOnline || !isVerified) {
            return false;
        }
        if (deal.getStatus() != com.nhom3.pubgapp.model.enums.DealStatus.PENDING_CONFIRM
                || !deal.isParticipant(userId)) {
            return false;
        }
        boolean alreadyConfirmed = userId.equals(deal.getBuyerId())
                ? deal.isBuyerConfirmed() : deal.isSellerConfirmed();
        return !alreadyConfirmed;
    }

    static boolean canCancel(Deal deal, String userId, boolean isOnline) {
        if (deal == null || !isOnline) {
            return false;
        }
        if (!deal.isParticipant(userId)) {
            return false;
        }
        boolean allowedStatus = deal.getStatus() == com.nhom3.pubgapp.model.enums.DealStatus.PENDING_CONFIRM
                || deal.getStatus() == com.nhom3.pubgapp.model.enums.DealStatus.CONFIRMED;
        return allowedStatus && deal.canMoveTo(com.nhom3.pubgapp.model.enums.DealStatus.CANCELLED);
    }

    static boolean canPay(Deal deal, Payment payment, String userId,
                          boolean isVerified, boolean isOnline) {
        if (deal == null || !isOnline || !isVerified) {
            return false;
        }
        if (deal.getStatus() != com.nhom3.pubgapp.model.enums.DealStatus.CONFIRMED) {
            return false;
        }
        if (!deal.needsPayment()) {
            return false;
        }
        if (payment != null && payment.isHeld()) {
            return false;
        }
        return userId != null && userId.equals(deal.getBuyerId());
    }

    static boolean isPaymentInfoVisible(Deal deal) {
        return deal != null && deal.needsPayment();
    }

    static Review findReviewBy(List<Review> reviews, String reviewerId) {
        if (reviews == null || reviewerId == null) {
            return null;
        }
        for (Review review : reviews) {
            if (review != null && reviewerId.equals(review.getReviewerId())) {
                return review;
            }
        }
        return null;
    }
}
