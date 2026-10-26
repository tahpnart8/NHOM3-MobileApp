package com.nhom3.pubgapp.viewmodel;

import android.net.Uri;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.nhom3.pubgapp.data.AuthRepository;
import com.nhom3.pubgapp.data.DataCallback;
import com.nhom3.pubgapp.data.ListingRepository;
import com.nhom3.pubgapp.data.MediaRepository;
import com.nhom3.pubgapp.data.UserRepository;
import com.nhom3.pubgapp.model.Listing;
import com.nhom3.pubgapp.model.ProfileStats;
import com.nhom3.pubgapp.model.SellerVerification;
import com.nhom3.pubgapp.model.User;
import com.nhom3.pubgapp.model.UserAddress;
import com.nhom3.pubgapp.model.UserIdentity;
import com.nhom3.pubgapp.model.enums.ImagePurpose;
import com.nhom3.pubgapp.model.enums.ListingStatus;
import com.nhom3.pubgapp.model.enums.VerificationStatus;
import com.google.firebase.firestore.GeoPoint;
import com.nhom3.pubgapp.util.NetworkMonitor;
import com.nhom3.pubgapp.util.SessionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// State for the profile, address, saved-listing and verification screens. Action bodies report NOT_READY until the repositories are filled.
public class ProfileViewModel extends ViewModel {

    private final MutableLiveData<User> profile = new MutableLiveData<>();
    private final MutableLiveData<ProfileStats> stats = new MutableLiveData<>();
    private final MutableLiveData<List<UserAddress>> addresses = new MutableLiveData<>();
    private final MutableLiveData<List<Listing>> savedListings = new MutableLiveData<>();
    private final MutableLiveData<List<Listing>> userListings = new MutableLiveData<>();
    private final MutableLiveData<UserIdentity> identity = new MutableLiveData<>();
    private final MutableLiveData<SellerVerification> verification = new MutableLiveData<>();
    private final MutableLiveData<Boolean> following = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> done = new MutableLiveData<>();

    static final int NATIONAL_ID_LENGTH = 12;
    static final int FORM_OK = 0;
    static final int FORM_MISSING_NATIONAL_ID = 1;
    static final int FORM_BAD_NATIONAL_ID = 2;
    static final int FORM_MISSING_ID_IMAGE = 3;
    static final int FORM_MISSING_PORTRAIT_IMAGE = 4;

    static final int ADDRESS_FORM_OK = 0;
    static final int ADDRESS_FORM_MISSING_RECEIVER = 1;
    static final int ADDRESS_FORM_MISSING_PHONE = 2;
    static final int ADDRESS_FORM_BAD_PHONE = 3;
    static final int ADDRESS_FORM_MISSING_PROVINCE = 4;
    static final int ADDRESS_FORM_MISSING_DISTRICT = 5;
    static final int ADDRESS_FORM_MISSING_DETAIL = 6;

    // ViewModels have no Context, so messages are Vietnamese constants, like the repositories.
    private static final String MESSAGE_NOT_SIGNED_IN = "Bạn cần đăng nhập để xem hồ sơ.";
    private static final String MESSAGE_OFFLINE = "Không có kết nối.";
    private static final String MESSAGE_MISSING_RECEIVER = "Vui lòng nhập họ tên người nhận.";
    private static final String MESSAGE_MISSING_PHONE = "Vui lòng nhập số điện thoại.";
    private static final String MESSAGE_BAD_PHONE =
            "Số điện thoại không hợp lệ (10 chữ số bắt đầu bằng 0).";
    private static final String MESSAGE_MISSING_PROVINCE = "Vui lòng nhập Tỉnh / Thành phố.";
    private static final String MESSAGE_MISSING_DISTRICT = "Vui lòng nhập Quận / Huyện.";
    private static final String MESSAGE_MISSING_DETAIL = "Vui lòng nhập địa chỉ chi tiết.";
    private static final String MESSAGE_MISSING_NATIONAL_ID = "Hãy nhập số CCCD.";
    private static final String MESSAGE_BAD_NATIONAL_ID = "Số CCCD phải gồm đúng 12 chữ số.";
    private static final String MESSAGE_MISSING_ID_IMAGE = "Hãy chọn ảnh giấy tờ.";
    private static final String MESSAGE_MISSING_PORTRAIT_IMAGE = "Hãy chọn ảnh chân dung cầm giấy tờ.";
    private static final String MESSAGE_EMAIL_NOT_VERIFIED =
            "Hãy xác thực email trước khi gửi yêu cầu xác thực tài khoản.";
    private static final String MESSAGE_ALREADY_PENDING = "Yêu cầu xác thực tài khoản đang chờ duyệt.";
    private static final String MESSAGE_ALREADY_APPROVED = "Tài khoản đã xác thực.";
    private static final String MESSAGE_NO_RESUBMIT_LEFT = "Đã hết số lần gửi lại, hãy liên hệ admin.";
    private static final String MESSAGE_CANNOT_FOLLOW_SELF = "Bạn không thể tự theo dõi chính mình.";
    private static final String MESSAGE_STATS_FAILED = "Không tải được thống kê, bấm Thử lại.";

    private final UserRepository userRepository = new UserRepository();
    private final AuthRepository authRepository = new AuthRepository();
    private final MediaRepository mediaRepository = new MediaRepository();
    private final ListingRepository listingRepository = new ListingRepository();

    // The uid whose profile is in profile, so a rotation does not read users/{uid} again.
    private String loadedUid;
    // The uid of the last read started, so a late answer for a previous account is dropped.
    private String requestedUid;
    // The seller whose listings are in userListings, so a rotation does not query again.
    private String listingsUid;

    public LiveData<User> getProfile() {
        return profile;
    }

    public LiveData<ProfileStats> getStats() {
        return stats;
    }

    public LiveData<List<UserAddress>> getAddresses() {
        return addresses;
    }

    public LiveData<List<Listing>> getSavedListings() {
        return savedListings;
    }

    public LiveData<List<Listing>> getUserListings() {
        return userListings;
    }

    public LiveData<UserIdentity> getIdentity() {
        return identity;
    }

    public LiveData<SellerVerification> getVerification() {
        return verification;
    }

    public LiveData<Boolean> getFollowing() {
        return following;
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

    public void loadProfile(String uid) {
        if (uid == null || uid.isEmpty()) {
            error.setValue(MESSAGE_NOT_SIGNED_IN);
            return;
        }
        if (Boolean.TRUE.equals(loading.getValue()) && uid.equals(requestedUid)) {
            return;
        }
        if (!uid.equals(loadedUid)) {
            clearPreviousUser();
            fetchProfile(uid);
            return;
        }
        if (profile.getValue() == null) {
            fetchProfile(uid);
            return;
        }
        // A failed count leaves stats empty, so opening the screen or Retry reads it again.
        if (needsStatsRead(isOwnPage(uid), stats.getValue() != null)) {
            loading.setValue(true);
            fetchStats(uid);
        }
        // A failed read of the follow state keeps the button locked, so opening the screen again retries it.
        if (following.getValue() == null && !isOwnPage(uid)) {
            fetchFollowing(uid);
        }
    }

    public void updateProfile(User user) {
        if (!NetworkMonitor.getInstance().isOnline()) {
            error.setValue(MESSAGE_OFFLINE);
            return;
        }
        loading.setValue(true);
        userRepository.updateProfile(user, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                profile.postValue(user);
                loading.postValue(false);
                done.postValue(true);
            }

            @Override
            public void onError(String message) {
                loading.postValue(false);
                error.postValue(message);
            }
        });
    }

    public void changeAvatar(Uri uri) {
        User current = profile.getValue();
        if (current == null) {
            error.setValue(MESSAGE_NOT_SIGNED_IN);
            return;
        }
        if (!NetworkMonitor.getInstance().isOnline()) {
            error.setValue(MESSAGE_OFFLINE);
            return;
        }
        loading.setValue(true);
        mediaRepository.makeThumb(uri, new DataCallback<String>() {
            @Override
            public void onSuccess(String base64) {
                saveAvatar(current, base64);
            }

            @Override
            public void onError(String message) {
                loading.postValue(false);
                error.postValue(message);
            }
        });
    }

    public void loadAddresses() {
        String uid = SessionManager.getInstance().getCurrentUserId();
        if (uid == null) {
            error.setValue(MESSAGE_NOT_SIGNED_IN);
            return;
        }
        if (!NetworkMonitor.getInstance().isOnline()) {
            error.setValue(MESSAGE_OFFLINE);
            return;
        }
        loading.setValue(true);
        fetchAddresses(uid);
    }

    private void fetchAddresses(String uid) {
        userRepository.getAddresses(uid, new DataCallback<List<UserAddress>>() {
            @Override
            public void onSuccess(List<UserAddress> result) {
                loading.postValue(false);
                addresses.postValue(result);
            }

            @Override
            public void onError(String message) {
                loading.postValue(false);
                error.postValue(message);
            }
        });
    }

    public void saveAddress(final UserAddress address) {
        final String uid = SessionManager.getInstance().getCurrentUserId();
        if (uid == null) {
            error.setValue(MESSAGE_NOT_SIGNED_IN);
            return;
        }
        if (!NetworkMonitor.getInstance().isOnline()) {
            error.setValue(MESSAGE_OFFLINE);
            return;
        }
        if (address == null) {
            return;
        }
        int check = checkAddressForm(address.getReceiverName(), address.getPhone(),
                address.getProvince(), address.getDistrict(), address.getDetail());
        if (check != ADDRESS_FORM_OK) {
            error.setValue(addressFormErrorMessage(check));
            return;
        }
        if (address.getLocation() == null) {
            GeoPoint fallback = findCoordinatesByProvince(address.getProvince());
            if (fallback != null) {
                address.setLocation(fallback);
            }
        }
        loading.setValue(true);
        userRepository.saveAddress(uid, address, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                fetchAddresses(uid);
                loading.postValue(false);
                done.postValue(true);
            }

            @Override
            public void onError(String message) {
                loading.postValue(false);
                error.postValue(message);
            }
        });
    }

    // Coordinates of Vietnamese provinces used when Geocoder lookup fails or runs offline.
    static GeoPoint findCoordinatesByProvince(String province) {
        if (province == null) {
            return null;
        }
        String p = normalizeProvinceName(province);
        if (p.isEmpty()) {
            return null;
        }
        if (p.contains("ha noi")) {
            return new GeoPoint(21.0285, 105.8542);
        }
        if (p.contains("ho chi minh") || p.contains("hcm") || p.contains("sai gon")) {
            return new GeoPoint(10.7769, 106.7009);
        }
        if (p.contains("da nang")) {
            return new GeoPoint(16.0544, 108.2022);
        }
        if (p.contains("hai phong")) {
            return new GeoPoint(20.8449, 106.6881);
        }
        if (p.contains("can tho")) {
            return new GeoPoint(10.0452, 105.7469);
        }
        if (p.contains("binh duong")) {
            return new GeoPoint(10.9805, 106.6519);
        }
        if (p.contains("dong nai")) {
            return new GeoPoint(10.9574, 106.8427);
        }
        if (p.contains("ba ria") || p.contains("vung tau")) {
            return new GeoPoint(10.5425, 107.2429);
        }
        if (p.contains("an giang")) {
            return new GeoPoint(10.5216, 105.1259);
        }
        if (p.contains("bac giang")) {
            return new GeoPoint(21.2731, 106.1946);
        }
        if (p.contains("bac kan") || p.contains("bac can")) {
            return new GeoPoint(22.1470, 105.8348);
        }
        if (p.contains("bac lieu")) {
            return new GeoPoint(9.2941, 105.7278);
        }
        if (p.contains("bac ninh")) {
            return new GeoPoint(21.1861, 106.0763);
        }
        if (p.contains("ben tre")) {
            return new GeoPoint(10.2434, 106.3756);
        }
        if (p.contains("binh dinh") || p.contains("quy nhon")) {
            return new GeoPoint(13.7820, 109.2197);
        }
        if (p.contains("binh phuoc") || p.contains("dong xoai")) {
            return new GeoPoint(11.5333, 106.9000);
        }
        if (p.contains("binh thuan") || p.contains("phan thiet")) {
            return new GeoPoint(10.9333, 108.1000);
        }
        if (p.contains("ca mau")) {
            return new GeoPoint(9.1769, 105.1500);
        }
        if (p.contains("cao bang")) {
            return new GeoPoint(22.6667, 106.2500);
        }
        if (p.contains("dak lak") || p.contains("dac lac") || p.contains("buon ma thuot")) {
            return new GeoPoint(12.6667, 108.0500);
        }
        if (p.contains("dak nong") || p.contains("dac nong")) {
            return new GeoPoint(12.0000, 107.6833);
        }
        if (p.contains("dien bien")) {
            return new GeoPoint(21.3833, 103.0167);
        }
        if (p.contains("dong thap") || p.contains("cao lanh")) {
            return new GeoPoint(10.4592, 105.6328);
        }
        if (p.contains("gia lai") || p.contains("pleiku")) {
            return new GeoPoint(13.9833, 108.0000);
        }
        if (p.contains("ha giang")) {
            return new GeoPoint(22.8233, 104.9836);
        }
        if (p.contains("ha nam") || p.contains("phu ly")) {
            return new GeoPoint(20.5411, 105.9139);
        }
        if (p.contains("ha tinh")) {
            return new GeoPoint(18.3333, 105.9000);
        }
        if (p.contains("hai duong")) {
            return new GeoPoint(20.9333, 106.3167);
        }
        if (p.contains("hau giang") || p.contains("vi thanh")) {
            return new GeoPoint(9.7844, 105.4703);
        }
        if (p.contains("hoa binh")) {
            return new GeoPoint(20.8167, 105.3333);
        }
        if (p.contains("hung yen")) {
            return new GeoPoint(20.6500, 106.0500);
        }
        if (p.contains("khanh hoa") || p.contains("nha trang")) {
            return new GeoPoint(12.2388, 109.1967);
        }
        if (p.contains("kien giang") || p.contains("rach gia")) {
            return new GeoPoint(10.0125, 105.0809);
        }
        if (p.contains("kon tum")) {
            return new GeoPoint(14.3500, 108.0000);
        }
        if (p.contains("lai chau")) {
            return new GeoPoint(22.4000, 103.4667);
        }
        if (p.contains("lam dong") || p.contains("da lat")) {
            return new GeoPoint(11.9404, 108.4583);
        }
        if (p.contains("lang son")) {
            return new GeoPoint(21.8500, 106.7500);
        }
        if (p.contains("lao cai")) {
            return new GeoPoint(22.4833, 103.9667);
        }
        if (p.contains("long an") || p.contains("tan an")) {
            return new GeoPoint(10.5364, 106.4131);
        }
        if (p.contains("nam dinh")) {
            return new GeoPoint(20.4333, 106.1667);
        }
        if (p.contains("nghe an") || p.contains("vinh")) {
            return new GeoPoint(18.6734, 105.6811);
        }
        if (p.contains("ninh binh")) {
            return new GeoPoint(20.2500, 105.9750);
        }
        if (p.contains("ninh thuan") || p.contains("phan rang")) {
            return new GeoPoint(11.5667, 108.9833);
        }
        if (p.contains("phu tho") || p.contains("viet tri")) {
            return new GeoPoint(21.3228, 105.4019);
        }
        if (p.contains("phu yen") || p.contains("tuy hoa")) {
            return new GeoPoint(13.0955, 109.3210);
        }
        if (p.contains("quang binh") || p.contains("dong hoi")) {
            return new GeoPoint(17.4833, 106.6000);
        }
        if (p.contains("quang nam") || p.contains("tam ky") || p.contains("hoi an")) {
            return new GeoPoint(15.5667, 108.4833);
        }
        if (p.contains("quang ngai")) {
            return new GeoPoint(15.1167, 108.8000);
        }
        if (p.contains("quang ninh") || p.contains("ha long")) {
            return new GeoPoint(20.9505, 107.0734);
        }
        if (p.contains("quang tri") || p.contains("dong ha")) {
            return new GeoPoint(16.8167, 107.1000);
        }
        if (p.contains("soc trang")) {
            return new GeoPoint(9.6000, 105.9667);
        }
        if (p.contains("son la")) {
            return new GeoPoint(21.3167, 103.9000);
        }
        if (p.contains("tay ninh")) {
            return new GeoPoint(11.3000, 106.1000);
        }
        if (p.contains("thai binh")) {
            return new GeoPoint(20.4500, 106.3333);
        }
        if (p.contains("thai nguyen")) {
            return new GeoPoint(21.5833, 105.8333);
        }
        if (p.contains("thanh hoa")) {
            return new GeoPoint(19.8000, 105.7667);
        }
        if (p.contains("thua thien") || p.contains("hue")) {
            return new GeoPoint(16.4667, 107.6000);
        }
        if (p.contains("tien giang") || p.contains("my tho")) {
            return new GeoPoint(10.3600, 106.3600);
        }
        if (p.contains("tra vinh")) {
            return new GeoPoint(9.9333, 106.3333);
        }
        if (p.contains("tuyen quang")) {
            return new GeoPoint(21.8167, 105.2167);
        }
        if (p.contains("vinh long")) {
            return new GeoPoint(10.2500, 105.9667);
        }
        if (p.contains("vinh phuc") || p.contains("vinh yen")) {
            return new GeoPoint(21.3093, 105.5972);
        }
        if (p.contains("yen bai")) {
            return new GeoPoint(21.7000, 104.8667);
        }
        return null;
    }

    private static String normalizeProvinceName(String text) {
        if (text == null) {
            return "";
        }
        String normalized = java.text.Normalizer.normalize(
                text.toLowerCase(java.util.Locale.ROOT),
                java.text.Normalizer.Form.NFD);
        String withoutAccents = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return withoutAccents.replace('đ', 'd').replace('Đ', 'd').trim();
    }

    public void deleteAddress(String id) {
        final String uid = SessionManager.getInstance().getCurrentUserId();
        if (uid == null) {
            error.setValue(MESSAGE_NOT_SIGNED_IN);
            return;
        }
        if (!NetworkMonitor.getInstance().isOnline()) {
            error.setValue(MESSAGE_OFFLINE);
            return;
        }
        if (id == null || id.trim().isEmpty()) {
            return;
        }
        loading.setValue(true);
        userRepository.deleteAddress(uid, id, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                fetchAddresses(uid);
            }

            @Override
            public void onError(String message) {
                loading.postValue(false);
                error.postValue(message);
            }
        });
    }

    // uid is the person being followed; the follower is always the signed-in user.
    public void follow(String uid) {
        String me = followerIdOrReport(uid);
        if (me == null) {
            return;
        }
        loading.setValue(true);
        userRepository.follow(me, uid, new FollowCallback(true));
    }

    public void unfollow(String uid) {
        String me = followerIdOrReport(uid);
        if (me == null) {
            return;
        }
        loading.setValue(true);
        userRepository.unfollow(me, uid, new FollowCallback(false));
    }

    public void loadSaved() {
        String uid = SessionManager.getInstance().getCurrentUserId();
        if (uid == null) {
            error.setValue(MESSAGE_NOT_SIGNED_IN);
            return;
        }
        if (Boolean.TRUE.equals(loading.getValue())) {
            return;
        }
        loading.setValue(true);
        userRepository.getSavedIds(uid, new DataCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> savedIds) {
                fetchSavedListings(savedIds);
            }

            @Override
            public void onError(String message) {
                loading.postValue(false);
                error.postValue(message);
            }
        });
    }

    // The card leaves the list at once and comes back if the write fails.
    public void unsaveListing(String listingId) {
        String uid = SessionManager.getInstance().getCurrentUserId();
        if (uid == null) {
            error.setValue(MESSAGE_NOT_SIGNED_IN);
            return;
        }
        if (!NetworkMonitor.getInstance().isOnline()) {
            error.setValue(MESSAGE_OFFLINE);
            return;
        }
        List<Listing> before = savedListings.getValue();
        savedListings.setValue(withoutListing(before, listingId));
        userRepository.unsaveListing(uid, listingId, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                done.postValue(true);
            }

            @Override
            public void onError(String message) {
                savedListings.postValue(before);
                error.postValue(message);
            }
        });
    }

    // Keeps AVAILABLE, RESERVED and SOLD as the repository returns them; each screen filters by tab.
    public void loadUserListings(String uid) {
        if (uid == null || uid.isEmpty()) {
            error.setValue(MESSAGE_NOT_SIGNED_IN);
            return;
        }
        if (uid.equals(listingsUid) && userListings.getValue() != null) {
            return;
        }
        listingsUid = uid;
        userListings.setValue(null);
        listingRepository.getBySeller(uid, new DataCallback<List<Listing>>() {
            @Override
            public void onSuccess(List<Listing> result) {
                if (!uid.equals(listingsUid)) {
                    return;
                }
                userListings.postValue(result);
            }

            @Override
            public void onError(String message) {
                error.postValue(message);
            }
        });
    }

    public void requestVerification(String nationalId, List<Uri> imageUris) {
        if (Boolean.TRUE.equals(loading.getValue())) {
            return;
        }
        int formResult = checkVerificationForm(nationalId, hasImageAt(imageUris, 0), hasImageAt(imageUris, 1));
        String formMessage = formErrorMessage(formResult);
        if (formMessage != null) {
            error.setValue(formMessage);
            return;
        }
        String statusMessage = verificationBlockMessage();
        if (statusMessage != null) {
            error.setValue(statusMessage);
            return;
        }
        if (!NetworkMonitor.getInstance().isOnline()) {
            error.setValue(MESSAGE_OFFLINE);
            return;
        }
        List<Uri> twoImages = new ArrayList<Uri>();
        twoImages.add(imageUris.get(0));
        twoImages.add(imageUris.get(1));
        loading.setValue(true);
        checkEmailThenSubmit(nationalId.trim(), twoImages);
    }

    public void loadVerification() {
        String uid = SessionManager.getInstance().getCurrentUserId();
        if (uid == null) {
            error.setValue(MESSAGE_NOT_SIGNED_IN);
            return;
        }
        userRepository.getVerification(uid, new DataCallback<SellerVerification>() {
            @Override
            public void onSuccess(SellerVerification result) {
                verification.postValue(result);
            }

            @Override
            public void onError(String message) {
                error.postValue(message);
            }
        });
    }

    public void loadIdentity() {
        error.setValue(DataCallback.NOT_READY);
    }

    public void saveIdentity(UserIdentity identity) {
        error.setValue(DataCallback.NOT_READY);
    }

    public void updateNotificationPrefs(Map<String, Boolean> prefs) {
        if (!NetworkMonitor.getInstance().isOnline()) {
            error.setValue(MESSAGE_OFFLINE);
            return;
        }
        String uid = SessionManager.getInstance().getCurrentUserId();
        if (uid == null || uid.isEmpty()) {
            error.setValue(MESSAGE_NOT_SIGNED_IN);
            return;
        }
        loading.setValue(true);
        userRepository.updateNotificationPrefs(uid, prefs, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                loading.postValue(false);
                User current = profile.getValue();
                if (current != null) {
                    Map<String, Boolean> copy = new HashMap<String, Boolean>();
                    if (prefs != null) {
                        copy.putAll(prefs);
                    }
                    current.setNotificationPrefs(copy);
                    profile.postValue(current);
                }
                done.postValue(true);
            }

            @Override
            public void onError(String message) {
                loading.postValue(false);
                error.postValue(message);
            }
        });
    }

    public void deactivateAccount(String password) {
        if (!NetworkMonitor.getInstance().isOnline()) {
            error.setValue(MESSAGE_OFFLINE);
            return;
        }
        if (isDeletePasswordEmpty(password)) {
            error.setValue("Vui lòng nhập mật khẩu để xóa tài khoản.");
            return;
        }
        final String uid = SessionManager.getInstance().getCurrentUserId();
        if (uid == null || uid.isEmpty()) {
            error.setValue(MESSAGE_NOT_SIGNED_IN);
            return;
        }
        loading.setValue(true);
        authRepository.reauthenticate(password, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                deactivateFirestoreAndAuth(uid);
            }

            @Override
            public void onError(String message) {
                loading.postValue(false);
                error.postValue(message != null ? message : "Mật khẩu không đúng.");
            }
        });
    }

    public void deactivateWithGoogle(String idToken) {
        if (!NetworkMonitor.getInstance().isOnline()) {
            error.setValue(MESSAGE_OFFLINE);
            return;
        }
        final String uid = SessionManager.getInstance().getCurrentUserId();
        if (uid == null || uid.isEmpty()) {
            error.setValue(MESSAGE_NOT_SIGNED_IN);
            return;
        }
        loading.setValue(true);
        if (requiresGoogleReauth(idToken)) {
            authRepository.reauthenticateWithGoogle(idToken, new DataCallback<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    deactivateFirestoreAndAuth(uid);
                }

                @Override
                public void onError(String message) {
                    loading.postValue(false);
                    String fallback = "Không thể xác thực lại bằng tài khoản Google.";
                    error.postValue(message != null ? message : fallback);
                }
            });
        } else {
            deactivateFirestoreAndAuth(uid);
        }
    }

    // The ViewModel outlives a sign out, so another account must never see the previous one's data.
    private void clearPreviousUser() {
        loadedUid = null;
        profile.setValue(null);
        stats.setValue(null);
        verification.setValue(null);
        following.setValue(null);
    }

    private void fetchProfile(String uid) {
        requestedUid = uid;
        loading.setValue(true);
        userRepository.getProfile(uid, new DataCallback<User>() {
            @Override
            public void onSuccess(User result) {
                if (!uid.equals(requestedUid)) {
                    return;
                }
                loadedUid = uid;
                profile.postValue(result);
                fetchFollowing(uid);
                if (isOwnPage(uid)) {
                    fetchStats(uid);
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

    // Counted on every open (trang-thai.md, common rules), never read from a stored field.
    private void fetchStats(String uid) {
        userRepository.getStats(uid, new DataCallback<ProfileStats>() {
            @Override
            public void onSuccess(ProfileStats result) {
                if (!uid.equals(requestedUid)) {
                    return;
                }
                stats.postValue(result);
                loading.postValue(false);
            }

            // The raw Firestore text is in the log; the user gets one short sentence.
            @Override
            public void onError(String message) {
                loading.postValue(false);
                error.postValue(MESSAGE_STATS_FAILED);
            }
        });
    }

    // The Rules let a user read only deals they are part of, so the counts exist only on the own page.
    private boolean isOwnPage(String uid) {
        String me = SessionManager.getInstance().getCurrentUserId();
        return me != null && !me.isEmpty() && me.equals(uid);
    }

    // Reads every saved listing at once and joins the answers in the saved order.
    private void fetchSavedListings(List<String> savedIds) {
        List<String> ids = new ArrayList<String>();
        if (savedIds != null) {
            ids.addAll(savedIds);
        }
        if (ids.isEmpty()) {
            savedListings.postValue(new ArrayList<Listing>());
            loading.postValue(false);
            return;
        }
        SavedCollector collector = new SavedCollector(ids);
        for (String id : ids) {
            listingRepository.getById(id, new SavedListingCallback(collector, id));
        }
    }

    private static List<Listing> withoutListing(List<Listing> listings, String listingId) {
        List<Listing> result = new ArrayList<Listing>();
        if (listings == null) {
            return result;
        }
        for (Listing listing : listings) {
            if (listingId == null || !listingId.equals(listing.getId())) {
                result.add(listing);
            }
        }
        return result;
    }

    // A listing that could not be read stays as a card holding only its id, so it can still be unsaved.
    static List<Listing> buildSavedList(List<String> savedIds, Map<String, Listing> found) {
        List<Listing> result = new ArrayList<Listing>();
        if (savedIds == null) {
            return result;
        }
        for (String id : savedIds) {
            Listing listing = null;
            if (found != null) {
                listing = found.get(id);
            }
            if (listing == null) {
                listing = new Listing();
                listing.setId(id);
            }
            result.add(listing);
        }
        return result;
    }

    static boolean isUnavailable(Listing listing) {
        if (listing == null || listing.getStatus() == null) {
            return true;
        }
        ListingStatus status = listing.getStatus();
        return status == ListingStatus.HIDDEN || status == ListingStatus.REMOVED || status == ListingStatus.DRAFT;
    }

    // Callbacks may come back on different threads, so the shared map and counter are synchronized.
    private final class SavedCollector {
        private final List<String> savedIds;
        private final Map<String, Listing> found = new HashMap<String, Listing>();
        private int answered;

        SavedCollector(List<String> savedIds) {
            this.savedIds = savedIds;
        }

        synchronized void answer(String id, Listing listing) {
            if (listing != null) {
                found.put(id, listing);
            }
            answered = answered + 1;
            if (answered < savedIds.size()) {
                return;
            }
            savedListings.postValue(buildSavedList(savedIds, found));
            loading.postValue(false);
        }
    }

    private static final class SavedListingCallback implements DataCallback<Listing> {
        private final SavedCollector collector;
        private final String id;

        SavedListingCallback(SavedCollector collector, String id) {
            this.collector = collector;
            this.id = id;
        }

        @Override
        public void onSuccess(Listing result) {
            collector.answer(id, result);
        }

        // A hidden or removed listing is refused by the Rules; it becomes a "no longer available" card.
        @Override
        public void onError(String message) {
            collector.answer(id, null);
        }
    }

    // Only someone else's page has a Follow button, so the own profile skips this read.
    private void fetchFollowing(String uid) {
        String me = SessionManager.getInstance().getCurrentUserId();
        if (me == null || me.isEmpty() || me.equals(uid)) {
            return;
        }
        userRepository.getFollowingIds(me, new DataCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> result) {
                if (!uid.equals(requestedUid)) {
                    return;
                }
                following.postValue(result != null && result.contains(uid));
            }

            @Override
            public void onError(String message) {
                error.postValue(message);
            }
        });
    }

    // Returns the signed-in uid, or null after putting the reason into error.
    private String followerIdOrReport(String uid) {
        if (Boolean.TRUE.equals(loading.getValue())) {
            return null;
        }
        String me = SessionManager.getInstance().getCurrentUserId();
        if (me == null || me.isEmpty()) {
            error.setValue(MESSAGE_NOT_SIGNED_IN);
            return null;
        }
        if (uid == null || uid.isEmpty() || me.equals(uid)) {
            error.setValue(MESSAGE_CANNOT_FOLLOW_SELF);
            return null;
        }
        if (!NetworkMonitor.getInstance().isOnline()) {
            error.setValue(MESSAGE_OFFLINE);
            return null;
        }
        return me;
    }

    // On success the flag flips; the follower count is not read again because it is only shown on the own page.
    private final class FollowCallback implements DataCallback<Void> {
        private final boolean nowFollowing;

        FollowCallback(boolean nowFollowing) {
            this.nowFollowing = nowFollowing;
        }

        @Override
        public void onSuccess(Void result) {
            following.postValue(nowFollowing);
            loading.postValue(false);
            done.postValue(true);
        }

        @Override
        public void onError(String message) {
            loading.postValue(false);
            error.postValue(message);
        }
    }

    private void saveAvatar(User user, String base64) {
        String previous = user.getAvatarBase64();
        user.setAvatarBase64(base64);
        userRepository.updateProfile(user, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                profile.postValue(user);
                loading.postValue(false);
            }

            @Override
            public void onError(String message) {
                user.setAvatarBase64(previous);
                loading.postValue(false);
                error.postValue(message);
            }
        });
    }

    private VerificationStatus currentVerificationStatus() {
        User current = profile.getValue();
        if (current == null || current.getVerificationStatus() == null) {
            return VerificationStatus.NONE;
        }
        return current.getVerificationStatus();
    }

    // Returns why the current state forbids a request, or null when it may be sent.
    private String verificationBlockMessage() {
        VerificationStatus status = currentVerificationStatus();
        if (status == VerificationStatus.PENDING) {
            return MESSAGE_ALREADY_PENDING;
        }
        if (status == VerificationStatus.APPROVED) {
            return MESSAGE_ALREADY_APPROVED;
        }
        SellerVerification previous = verification.getValue();
        if (status == VerificationStatus.REJECTED && previous != null && !previous.canResubmit()) {
            return MESSAGE_NO_RESUBMIT_LEFT;
        }
        return null;
    }

    // The email flag is reloaded first, so an unverified account is told why instead of the Rules refusing.
    private void checkEmailThenSubmit(String nationalId, List<Uri> imageUris) {
        authRepository.reloadUser(new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                boolean emailVerified = authRepository.isEmailVerified();
                if (!canSubmitVerification(currentVerificationStatus(), emailVerified)) {
                    loading.postValue(false);
                    error.postValue(MESSAGE_EMAIL_NOT_VERIFIED);
                    return;
                }
                saveVerificationImages(nationalId, imageUris, new ArrayList<String>());
            }

            @Override
            public void onError(String message) {
                loading.postValue(false);
                error.postValue(message);
            }
        });
    }

    // One image at a time keeps imageIds in order: identity paper first, portrait second.
    private void saveVerificationImages(String nationalId, List<Uri> imageUris, List<String> savedIds) {
        if (savedIds.size() == imageUris.size()) {
            sendVerification(nationalId, savedIds);
            return;
        }
        Uri next = imageUris.get(savedIds.size());
        mediaRepository.saveImage(next, ImagePurpose.VERIFICATION, new DataCallback<String>() {
            @Override
            public void onSuccess(String imageId) {
                savedIds.add(imageId);
                saveVerificationImages(nationalId, imageUris, savedIds);
            }

            @Override
            public void onError(String message) {
                failVerification(savedIds, message);
            }
        });
    }

    private void sendVerification(String nationalId, List<String> imageIds) {
        String uid = SessionManager.getInstance().getCurrentUserId();
        SellerVerification request = new SellerVerification();
        request.setUserId(uid);
        request.setNationalId(nationalId);
        request.setImageIds(imageIds);
        request.setStatus(VerificationStatus.PENDING);
        userRepository.requestVerification(uid, request, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                loading.postValue(false);
                done.postValue(true);
                refreshProfile(uid);
            }

            @Override
            public void onError(String message) {
                failVerification(imageIds, message);
            }
        });
    }

    // Reads users/{uid} again so the block on D01 switches to PENDING.
    private void refreshProfile(String uid) {
        userRepository.getProfile(uid, new DataCallback<User>() {
            @Override
            public void onSuccess(User result) {
                profile.postValue(result);
            }

            @Override
            public void onError(String message) {
                error.postValue(message);
            }
        });
    }

    // Images saved by a failed attempt are deleted so they do not stay in Firestore unused.
    private void failVerification(List<String> savedIds, String message) {
        for (String imageId : savedIds) {
            mediaRepository.delete(imageId, new DataCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                }

                @Override
                public void onError(String deleteMessage) {
                    error.postValue(deleteMessage);
                }
            });
        }
        loading.postValue(false);
        error.postValue(message);
    }

    // Takes flags instead of the Uri list so the JVM unit test needs no Android Uri.
    static int checkVerificationForm(String nationalId, boolean hasIdImage, boolean hasPortraitImage) {
        if (nationalId == null || nationalId.trim().isEmpty()) {
            return FORM_MISSING_NATIONAL_ID;
        }
        if (!isNationalIdValid(nationalId.trim())) {
            return FORM_BAD_NATIONAL_ID;
        }
        if (!hasIdImage) {
            return FORM_MISSING_ID_IMAGE;
        }
        if (!hasPortraitImage) {
            return FORM_MISSING_PORTRAIT_IMAGE;
        }
        return FORM_OK;
    }

    private static boolean hasImageAt(List<Uri> imageUris, int index) {
        return imageUris != null && imageUris.size() > index && imageUris.get(index) != null;
    }

    static boolean isNationalIdValid(String nationalId) {
        if (nationalId.length() != NATIONAL_ID_LENGTH) {
            return false;
        }
        for (int i = 0; i < nationalId.length(); i++) {
            char digit = nationalId.charAt(i);
            if (digit < '0' || digit > '9') {
                return false;
            }
        }
        return true;
    }

    // Same uid is skipped only when the counts are there or the page is not the own one.
    static boolean needsStatsRead(boolean ownPage, boolean hasStats) {
        return ownPage && !hasStats;
    }

    static boolean canSubmitVerification(VerificationStatus status, boolean emailVerified) {
        if (!emailVerified) {
            return false;
        }
        return status == VerificationStatus.NONE || status == VerificationStatus.REJECTED;
    }

    private static String formErrorMessage(int formResult) {
        if (formResult == FORM_MISSING_NATIONAL_ID) {
            return MESSAGE_MISSING_NATIONAL_ID;
        }
        if (formResult == FORM_BAD_NATIONAL_ID) {
            return MESSAGE_BAD_NATIONAL_ID;
        }
        if (formResult == FORM_MISSING_ID_IMAGE) {
            return MESSAGE_MISSING_ID_IMAGE;
        }
        if (formResult == FORM_MISSING_PORTRAIT_IMAGE) {
            return MESSAGE_MISSING_PORTRAIT_IMAGE;
        }
        return null;
    }

<<<<<<< HEAD
    static boolean isPhoneValid(String phone) {
        if (phone == null) {
            return false;
        }
        String trimmed = phone.trim();
        if (trimmed.length() != 10) {
            return false;
        }
        if (!trimmed.startsWith("0")) {
            return false;
        }
        for (int i = 0; i < trimmed.length(); i++) {
            char digit = trimmed.charAt(i);
            if (digit < '0' || digit > '9') {
                return false;
            }
        }
        return true;
    }

    static int checkAddressForm(String receiverName, String phone, String province,
            String district, String detail) {
        if (receiverName == null || receiverName.trim().isEmpty()) {
            return ADDRESS_FORM_MISSING_RECEIVER;
        }
        if (phone == null || phone.trim().isEmpty()) {
            return ADDRESS_FORM_MISSING_PHONE;
        }
        if (!isPhoneValid(phone)) {
            return ADDRESS_FORM_BAD_PHONE;
        }
        if (province == null || province.trim().isEmpty()) {
            return ADDRESS_FORM_MISSING_PROVINCE;
        }
        if (district == null || district.trim().isEmpty()) {
            return ADDRESS_FORM_MISSING_DISTRICT;
        }
        if (detail == null || detail.trim().isEmpty()) {
            return ADDRESS_FORM_MISSING_DETAIL;
        }
        return ADDRESS_FORM_OK;
    }

    static String addressFormErrorMessage(int formResult) {
        if (formResult == ADDRESS_FORM_MISSING_RECEIVER) {
            return MESSAGE_MISSING_RECEIVER;
        }
        if (formResult == ADDRESS_FORM_MISSING_PHONE) {
            return MESSAGE_MISSING_PHONE;
        }
        if (formResult == ADDRESS_FORM_BAD_PHONE) {
            return MESSAGE_BAD_PHONE;
        }
        if (formResult == ADDRESS_FORM_MISSING_PROVINCE) {
            return MESSAGE_MISSING_PROVINCE;
        }
        if (formResult == ADDRESS_FORM_MISSING_DISTRICT) {
            return MESSAGE_MISSING_DISTRICT;
        }
        if (formResult == ADDRESS_FORM_MISSING_DETAIL) {
            return MESSAGE_MISSING_DETAIL;
        }
        return null;
    }

    static boolean isNotificationTypeEnabled(Map<String, Boolean> prefs, String typeKey) {
        if (prefs == null || !prefs.containsKey(typeKey)) {
            return true;
        }
        Boolean val = prefs.get(typeKey);
        if (val == null) {
            return true;
        }
        return val.booleanValue();
    }

    static Map<String, Boolean> buildUpdatedNotificationPrefs(Map<String, Boolean> currentPrefs,
            String typeKey, boolean enabled) {
        Map<String, Boolean> updated = new HashMap<String, Boolean>();
        if (currentPrefs != null) {
            updated.putAll(currentPrefs);
        }
        updated.put(typeKey, Boolean.valueOf(enabled));
        return updated;
    }

    private void deactivateFirestoreAndAuth(final String uid) {
        userRepository.deactivate(uid, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                authRepository.deleteLogin(new DataCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        completeDeactivation();
                    }

                    @Override
                    public void onError(String message) {
                        loading.postValue(false);
                        String display = message;
                        if (message != null && (message.contains("recent-login")
                                || message.contains("CREDENTIAL_TOO_OLD")
                                || message.contains("xác thực lại"))) {
                            display = "Cần đăng nhập lại bằng Google trước khi xóa tài khoản.";
                        }
                        error.postValue(display != null ? display : "Không thể xóa thông tin đăng nhập.");
                    }
                });
            }

            @Override
            public void onError(String message) {
                loading.postValue(false);
                error.postValue(message != null ? message : "Không thể vô hiệu hóa tài khoản.");
            }
        });
    }

    private void completeDeactivation() {
        authRepository.signOut();
        SessionManager.getInstance().clear();
        clearRoomDatabase();
        clearPreviousUser();
        loading.postValue(false);
        done.postValue(true);
    }

    private void clearRoomDatabase() {
        try {
            com.google.firebase.FirebaseApp app = com.google.firebase.FirebaseApp.getInstance();
            if (app != null) {
                android.content.Context context = app.getApplicationContext();
                if (context != null) {
                    final com.nhom3.pubgapp.data.local.AppDatabase db =
                            com.nhom3.pubgapp.data.local.AppDatabase.getInstance(context);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                db.listingDao().clearAll();
                                db.chatDao().clearAll();
                                db.dealDao().clearAll();
                                db.historyDao().clearAll();
                            } catch (Exception ignored) {
                            }
                        }
                    }).start();
                }
            }
        } catch (Exception ignored) {
        }
    }

    public static boolean isDeletePasswordEmpty(String password) {
        return password == null || password.trim().isEmpty();
    }

    public static boolean requiresGoogleReauth(String idToken) {
        return idToken != null && !idToken.trim().isEmpty();
    }

    public void clearError() {
        error.setValue(null);
    }

    public void clearDone() {
        done.setValue(false);
    }
}
