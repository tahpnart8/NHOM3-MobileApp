package com.nhom3.pubgapp.viewmodel;

import android.net.Uri;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.nhom3.pubgapp.data.DataCallback;
import com.google.firebase.Timestamp;
import com.nhom3.pubgapp.data.DealRepository;
import com.nhom3.pubgapp.data.MediaRepository;
import com.nhom3.pubgapp.data.ReviewRepository;
import com.nhom3.pubgapp.model.enums.DealStatus;
import com.nhom3.pubgapp.model.enums.DeliveryMethod;
import com.nhom3.pubgapp.model.enums.ImagePurpose;
import com.nhom3.pubgapp.model.Deal;
import com.nhom3.pubgapp.model.Payment;
import com.nhom3.pubgapp.model.Review;
import com.nhom3.pubgapp.model.enums.PaymentMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

// State for the deal screens, from creation to completion. Action bodies report NOT_READY until the repositories are filled.
public class DealViewModel extends ViewModel {

    private final DealRepository dealRepository;
    private final MediaRepository mediaRepository;
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
    // The open deal, its payment and the history lists are live, so the other party's step shows at once.
    private final ListenerSet listeners = new ListenerSet();
    private String liveDealId;

    private static final String KEY_DEAL = "deal";
    private static final String KEY_PAYMENT = "payment";
    private static final String KEY_HISTORY = "history";
    private static final String KEY_PAYMENTS = "payments";

    public DealViewModel() {
        this(new DealRepository(), new MediaRepository());
    }

    DealViewModel(DealRepository dealRepository) {
        this(dealRepository, new MediaRepository());
    }

    DealViewModel(DealRepository dealRepository, MediaRepository mediaRepository) {
        this.dealRepository = dealRepository;
        this.mediaRepository = mediaRepository;
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

    // Starts listening once per deal; the reloads after confirm, pay and the other actions then cost nothing.
    public void loadDeal(String id) {
        if (id == null || id.trim().isEmpty()) {
            return;
        }
        if (id.equals(liveDealId) && listeners.has(KEY_DEAL)) {
            return;
        }
        liveDealId = id;
        loading.setValue(true);
        // Loading ends when both the deal and its payment have answered once.
        final AtomicInteger firstAnswers = new AtomicInteger(2);
        listeners.put(KEY_DEAL, dealRepository.observeDeal(id, new FirstAnswerCallback<Deal>(firstAnswers) {
            @Override
            void onValue(Deal result) {
                deal.postValue(result);
            }

            @Override
            void onFailure(String message) {
                listeners.remove(KEY_DEAL);
                error.postValue(message);
            }
        }));
        // A payment that cannot be read is shown as no payment, as before.
        listeners.put(KEY_PAYMENT, dealRepository.observePayment(id,
                new FirstAnswerCallback<Payment>(firstAnswers) {
                    @Override
                    void onValue(Payment result) {
                        payment.postValue(result);
                    }

                    @Override
                    void onFailure(String message) {
                        listeners.remove(KEY_PAYMENT);
                        payment.postValue(null);
                    }
                }));
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

    static String formatRemainingTime(Timestamp autoReleaseAt, Timestamp now) {
        if (autoReleaseAt == null || now == null) {
            return "";
        }
        long diffMillis = autoReleaseAt.toDate().getTime() - now.toDate().getTime();
        if (diffMillis <= 0) {
            return "Đã hết đếm ngược, hệ thống sẽ tự chuyển sang Đã nhận";
        }
        if (diffMillis < 24L * 60 * 60 * 1000) {
            long hours = diffMillis / (60L * 60 * 1000);
            long minutes = (diffMillis % (60L * 60 * 1000)) / (60L * 1000);
            return "Còn " + hours + " giờ " + minutes + " phút để xác nhận";
        }
        long days = diffMillis / (24L * 60 * 60 * 1000);
        return "Còn " + days + " ngày để xác nhận";
    }

    static boolean canMarkShipped(Deal deal, Payment payment, String currentUid,
                                  boolean isSellerVerified) {
        if (deal == null || currentUid == null || !currentUid.equals(deal.getSellerId())) {
            return false;
        }
        if (deal.getStatus() != DealStatus.CONFIRMED) {
            return false;
        }
        if (deal.getDeliveryMethod() != DeliveryMethod.SHIPPING) {
            return false;
        }
        if (!isSellerVerified) {
            return false;
        }
        if (deal.needsPayment()) {
            return payment != null && payment.isHeld();
        }
        return true;
    }

    static boolean canMarkReceived(Deal deal, Payment payment, String currentUid,
                                   boolean isBuyerVerified) {
        if (deal == null || currentUid == null || !currentUid.equals(deal.getBuyerId())) {
            return false;
        }
        if (!isBuyerVerified) {
            return false;
        }
        if (deal.getDeliveryMethod() == DeliveryMethod.SHIPPING) {
            if (deal.getStatus() != DealStatus.SHIPPED) {
                return false;
            }
        } else if (deal.getDeliveryMethod() == DeliveryMethod.MEET_UP) {
            if (deal.getStatus() != DealStatus.CONFIRMED && deal.getStatus() != DealStatus.SHIPPED) {
                return false;
            }
        } else {
            return false;
        }
        if (deal.needsPayment()) {
            return payment != null && payment.isHeld();
        }
        return true;
    }

    static boolean canComplain(Deal deal, String currentUid, Timestamp now) {
        if (deal == null || currentUid == null) {
            return false;
        }
        boolean isBuyer = currentUid.equals(deal.getBuyerId());
        boolean isSeller = currentUid.equals(deal.getSellerId());
        if (!isBuyer && !isSeller) {
            return false;
        }
        if (deal.getStatus() == DealStatus.SHIPPED) {
            return deal.getAutoReleaseAt() != null && now != null
                    && now.compareTo(deal.getAutoReleaseAt()) < 0;
        }
        if (deal.getStatus() == DealStatus.CONFIRMED
                && deal.getDeliveryMethod() == DeliveryMethod.MEET_UP) {
            return isBuyer;
        }
        return false;
    }

    public void markShipped(final String carrier, final String trackingCode, Uri photoUri) {
        final Deal currentDeal = deal.getValue();
        if (currentDeal == null) {
            error.setValue("Không tìm thấy thông tin giao dịch.");
            return;
        }
        if (carrier == null || carrier.trim().isEmpty()
                || trackingCode == null || trackingCode.trim().isEmpty()
                || photoUri == null) {
            error.setValue("Vui lòng nhập đầy đủ thông tin vận đơn và chọn ảnh.");
            return;
        }
        if (Boolean.TRUE.equals(loading.getValue())) {
            return;
        }

        loading.setValue(true);
        mediaRepository.saveImage(photoUri, ImagePurpose.WAYBILL, new DataCallback<String>() {
            @Override
            public void onSuccess(final String imageId) {
                dealRepository.markShipped(currentDeal.getId(), carrier.trim(),
                        trackingCode.trim(), imageId, new DataCallback<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                loadDeal(currentDeal.getId());
                                done.postValue(true);
                                loading.postValue(false);
                            }

                            @Override
                            public void onError(String message) {
                                mediaRepository.delete(imageId, new DataCallback<Void>() {
                                    @Override
                                    public void onSuccess(Void ignored) {
                                    }

                                    @Override
                                    public void onError(String ignored) {
                                    }
                                });
                                loading.postValue(false);
                                error.postValue(message);
                            }
                        });
            }

            @Override
            public void onError(String message) {
                loading.postValue(false);
                error.postValue("Lỗi tải ảnh vận đơn: " + message);
            }
        });
    }

    public void markReceived(Uri photoUri) {
        final Deal currentDeal = deal.getValue();
        if (currentDeal == null) {
            error.setValue("Không tìm thấy thông tin giao dịch.");
            return;
        }
        if (photoUri == null) {
            error.setValue("Vui lòng chụp hoặc chọn ảnh món đồ đã nhận.");
            return;
        }
        if (Boolean.TRUE.equals(loading.getValue())) {
            return;
        }

        loading.setValue(true);
        mediaRepository.saveImage(photoUri, ImagePurpose.RECEIVED, new DataCallback<String>() {
            @Override
            public void onSuccess(final String imageId) {
                dealRepository.markReceived(currentDeal.getId(), imageId, new DataCallback<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        loadDeal(currentDeal.getId());
                        done.postValue(true);
                        loading.postValue(false);
                    }

                    @Override
                    public void onError(String message) {
                        mediaRepository.delete(imageId, new DataCallback<Void>() {
                            @Override
                            public void onSuccess(Void ignored) {
                            }

                            @Override
                            public void onError(String ignored) {
                            }
                        });
                        loading.postValue(false);
                        error.postValue(message);
                    }
                });
            }

            @Override
            public void onError(String message) {
                loading.postValue(false);
                error.postValue("Lỗi tải ảnh nhận hàng: " + message);
            }
        });
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
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            error.setValue("Chưa đăng nhập");
            return;
        }
        if (listeners.has(KEY_HISTORY)) {
            return;
        }
        loading.setValue(true);
        listeners.put(KEY_HISTORY, dealRepository.observeMyDeals(user.getUid(), new DataCallback<List<Deal>>() {
            @Override
            public void onSuccess(List<Deal> result) {
                loading.postValue(false);
                deals.postValue(result != null ? result : new ArrayList<Deal>());
            }

            @Override
            public void onError(String message) {
                listeners.remove(KEY_HISTORY);
                loading.postValue(false);
                error.postValue(message);
            }
        }));
    }

    public void loadPayments() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            error.setValue("Chưa đăng nhập");
            return;
        }
        if (listeners.has(KEY_PAYMENTS)) {
            return;
        }
        loading.setValue(true);
        listeners.put(KEY_PAYMENTS, dealRepository.observePayments(user.getUid(),
                new DataCallback<List<Payment>>() {
                    @Override
                    public void onSuccess(List<Payment> result) {
                        loading.postValue(false);
                        payments.postValue(result != null ? result : new ArrayList<Payment>());
                    }

                    @Override
                    public void onError(String message) {
                        listeners.remove(KEY_PAYMENTS);
                        loading.postValue(false);
                        error.postValue(message);
                    }
                }));
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

    @Override
    protected void onCleared() {
        super.onCleared();
        listeners.clear();
    }

    // Ends the shared loading state on the first answer of each listener; later answers only update data.
    private abstract class FirstAnswerCallback<T> implements DataCallback<T> {

        private final AtomicInteger firstAnswers;
        private final AtomicBoolean answered = new AtomicBoolean(false);

        FirstAnswerCallback(AtomicInteger firstAnswers) {
            this.firstAnswers = firstAnswers;
        }

        abstract void onValue(T result);

        abstract void onFailure(String message);

        @Override
        public void onSuccess(T result) {
            onValue(result);
            countAnswer();
        }

        @Override
        public void onError(String message) {
            onFailure(message);
            countAnswer();
        }

        private void countAnswer() {
            if (answered.compareAndSet(false, true) && firstAnswers.decrementAndGet() <= 0) {
                loading.postValue(false);
            }
        }
    }
}
