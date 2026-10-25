package com.nhom3.pubgapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.nhom3.pubgapp.data.DataCallback;
import com.nhom3.pubgapp.data.ListingRepository;
import com.nhom3.pubgapp.data.MediaRepository;
import com.nhom3.pubgapp.data.UserRepository;
import com.nhom3.pubgapp.model.Listing;
import com.nhom3.pubgapp.model.ProfileStats;
import com.nhom3.pubgapp.model.User;
import com.nhom3.pubgapp.model.enums.ListingStatus;
import com.nhom3.pubgapp.util.RecommendationEngine;
import com.nhom3.pubgapp.util.SessionManager;

import java.util.ArrayList;
import java.util.List;

// State for the listing detail screen. Action bodies report NOT_READY until the repositories are filled.
public class ListingDetailViewModel extends ViewModel {

    public static boolean canShowListing(Listing listing, String viewerId) {
        if (listing == null) {
            return false;
        }
        ListingStatus status = listing.getStatus();
        if (status == ListingStatus.AVAILABLE || status == ListingStatus.RESERVED || status == ListingStatus.SOLD) {
            return true;
        }
        if (status == ListingStatus.DRAFT || status == ListingStatus.HIDDEN || status == ListingStatus.REMOVED) {
            return viewerId != null && viewerId.equals(listing.getSellerId());
        }
        return false;
    }

    public static boolean canStartChat(Listing listing, String viewerId) {
        if (listing == null) {
            return false;
        }
        if (viewerId != null && viewerId.equals(listing.getSellerId())) {
            return false;
        }
        ListingStatus status = listing.getStatus();
        return status == ListingStatus.AVAILABLE || status == ListingStatus.RESERVED;
    }

    public static boolean canSendOffer(Listing listing, String viewerId) {
        if (listing == null) {
            return false;
        }
        if (viewerId != null && viewerId.equals(listing.getSellerId())) {
            return false;
        }
        return listing.getStatus() == ListingStatus.AVAILABLE;
    }

    private static final String MESSAGE_LOGIN_TO_FOLLOW = "Vui lòng đăng nhập để theo dõi người bán";
    private static final String MESSAGE_CANNOT_FOLLOW_SELF = "Không thể theo dõi chính mình";

    static boolean canFollowSeller(String sellerId, String viewerId) {
        if (sellerId == null || sellerId.trim().isEmpty()) {
            return false;
        }
        if (viewerId != null && viewerId.equals(sellerId)) {
            return false;
        }
        return true;
    }

    private final ListingRepository listingRepository = new ListingRepository();
    private final UserRepository userRepository = new UserRepository();
    private final MediaRepository mediaRepository = new MediaRepository();
    private final RecommendationEngine recommendationEngine = new RecommendationEngine();

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
        if (listingId == null || listingId.trim().isEmpty()) {
            error.setValue("Mã tin không hợp lệ");
            return;
        }
        loading.setValue(true);
        listingRepository.getById(listingId, new DataCallback<Listing>() {
            @Override
            public void onSuccess(final Listing result) {
                loading.postValue(false);
                listing.postValue(result);
                if (result != null) {
                    listingRepository.recordView(result);
                    if (SessionManager.getInstance().isLoggedIn()) {
                        String currentUid = SessionManager.getInstance().getCurrentUserId();
                        if (result.getSellerId() != null) {
                            userRepository.getProfile(result.getSellerId(), new DataCallback<User>() {
                                @Override
                                public void onSuccess(User sellerUser) {
                                    seller.postValue(sellerUser);
                                }

                                @Override
                                public void onError(String errorMessage) {
                                    // B03 ignores seller load error if unverified or missing
                                }
                            });
                        }
                        if (currentUid != null) {
                            userRepository.getSavedIds(currentUid, new DataCallback<List<String>>() {
                                @Override
                                public void onSuccess(List<String> savedIds) {
                                    if (savedIds != null && savedIds.contains(result.getId())) {
                                        saved.postValue(true);
                                    } else {
                                        saved.postValue(false);
                                    }
                                }

                                @Override
                                public void onError(String errorMessage) {
                                    saved.postValue(false);
                                }
                            });
                            userRepository.getFollowingIds(currentUid, new DataCallback<List<String>>() {
                                @Override
                                public void onSuccess(List<String> followingIds) {
                                    if (result.getSellerId() != null && followingIds != null
                                            && followingIds.contains(result.getSellerId())) {
                                        followingSeller.postValue(true);
                                    } else {
                                        followingSeller.postValue(false);
                                    }
                                }

                                @Override
                                public void onError(String errorMessage) {
                                    followingSeller.postValue(false);
                                }
                            });
                        }
                    } else {
                        seller.postValue(null);
                        saved.postValue(false);
                        followingSeller.postValue(false);
                    }
                }
            }

            @Override
            public void onError(String errorMessage) {
                loading.postValue(false);
                listing.postValue(null);
                error.postValue(errorMessage);
            }
        });
    }

    public void loadImages() {
        Listing current = listing.getValue();
        if (current == null || current.getImageIds() == null || current.getImageIds().isEmpty()) {
            return;
        }
        final List<String> ids = current.getImageIds();
        final int total = ids.size();
        final byte[][] loaded = new byte[total][];
        final int[] completedCount = new int[1];

        for (int i = 0; i < total; i++) {
            final int index = i;
            String imageId = ids.get(i);
            mediaRepository.getImage(imageId, new DataCallback<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    loaded[index] = bytes;
                    completedCount[0]++;
                    if (completedCount[0] == total) {
                        List<byte[]> result = new ArrayList<byte[]>();
                        for (int j = 0; j < total; j++) {
                            if (loaded[j] != null) {
                                result.add(loaded[j]);
                            }
                        }
                        images.postValue(result);
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    completedCount[0]++;
                    if (completedCount[0] == total) {
                        List<byte[]> result = new ArrayList<byte[]>();
                        for (int j = 0; j < total; j++) {
                            if (loaded[j] != null) {
                                result.add(loaded[j]);
                            }
                        }
                        images.postValue(result);
                    }
                }
            });
        }
    }

    public void toggleSave() {
        final Listing current = listing.getValue();
        if (current == null || current.getId() == null) {
            return;
        }
        if (!SessionManager.getInstance().isLoggedIn()) {
            error.setValue("Vui lòng đăng nhập để lưu tin");
            return;
        }
        String uid = SessionManager.getInstance().getCurrentUserId();
        if (uid == null) {
            return;
        }
        final boolean currentSaved = Boolean.TRUE.equals(saved.getValue());
        final boolean newSaved = !currentSaved;
        saved.setValue(newSaved);

        if (newSaved) {
            userRepository.saveListing(uid, current.getId(), new DataCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    done.postValue(true);
                }

                @Override
                public void onError(String errorMessage) {
                    saved.postValue(currentSaved);
                    error.postValue(errorMessage);
                }
            });
        } else {
            userRepository.unsaveListing(uid, current.getId(), new DataCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    done.postValue(true);
                }

                @Override
                public void onError(String errorMessage) {
                    saved.postValue(currentSaved);
                    error.postValue(errorMessage);
                }
            });
        }
    }

    public void toggleFollowSeller() {
        final Listing current = listing.getValue();
        if (current == null || current.getSellerId() == null || current.getSellerId().isEmpty()) {
            return;
        }
        final String sellerId = current.getSellerId();
        if (!SessionManager.getInstance().isLoggedIn()) {
            error.setValue(MESSAGE_LOGIN_TO_FOLLOW);
            return;
        }
        final String uid = SessionManager.getInstance().getCurrentUserId();
        if (uid == null || uid.isEmpty()) {
            error.setValue(MESSAGE_LOGIN_TO_FOLLOW);
            return;
        }
        if (uid.equals(sellerId)) {
            error.setValue(MESSAGE_CANNOT_FOLLOW_SELF);
            return;
        }
        final boolean isCurrentlyFollowing = Boolean.TRUE.equals(followingSeller.getValue());
        final boolean newFollowing = !isCurrentlyFollowing;
        loading.setValue(true);
        if (newFollowing) {
            userRepository.follow(uid, sellerId, new DataCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    loading.postValue(false);
                    followingSeller.postValue(true);
                }

                @Override
                public void onError(String errorMessage) {
                    loading.postValue(false);
                    error.postValue(errorMessage);
                }
            });
        } else {
            userRepository.unfollow(uid, sellerId, new DataCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    loading.postValue(false);
                    followingSeller.postValue(false);
                }

                @Override
                public void onError(String errorMessage) {
                    loading.postValue(false);
                    error.postValue(errorMessage);
                }
            });
        }
    }

    public void loadSimilar() {
        final Listing current = listing.getValue();
        if (current == null) {
            return;
        }
        listingRepository.getSimilar(current, new DataCallback<List<Listing>>() {
            @Override
            public void onSuccess(List<Listing> candidates) {
                List<Listing> scored = recommendationEngine.similarTo(current, candidates);
                similar.postValue(scored);
            }

            @Override
            public void onError(String errorMessage) {
                similar.postValue(new ArrayList<Listing>());
            }
        });
    }

    public void loadPriceRange() {
        final Listing current = listing.getValue();
        if (current == null) {
            priceRange.setValue(new long[0]);
            return;
        }
        List<Listing> candidates = similar.getValue();
        if (candidates != null && !candidates.isEmpty()) {
            long[] range = recommendationEngine.priceRange(current, candidates);
            priceRange.setValue(range);
            return;
        }
        listingRepository.getSimilar(current, new DataCallback<List<Listing>>() {
            @Override
            public void onSuccess(List<Listing> fetchedCandidates) {
                long[] range = recommendationEngine.priceRange(current, fetchedCandidates);
                priceRange.postValue(range);
            }

            @Override
            public void onError(String errorMessage) {
                priceRange.postValue(new long[0]);
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
