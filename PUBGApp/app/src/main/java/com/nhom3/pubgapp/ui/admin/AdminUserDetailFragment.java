package com.nhom3.pubgapp.ui.admin;

import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.databinding.DialogAdminVerificationRejectBinding;
import com.nhom3.pubgapp.databinding.FragmentAdminUserDetailBinding;
import com.nhom3.pubgapp.model.ProfileStats;
import com.nhom3.pubgapp.model.SellerVerification;
import com.nhom3.pubgapp.model.User;
import com.nhom3.pubgapp.model.UserIdentity;
import com.nhom3.pubgapp.model.enums.Gender;
import com.nhom3.pubgapp.model.enums.VerificationStatus;
import com.nhom3.pubgapp.ui.common.ImageLoader;
import com.nhom3.pubgapp.ui.common.StatusChips;
import com.nhom3.pubgapp.util.NetworkMonitor;
import com.nhom3.pubgapp.viewmodel.AdminUserViewModel;

import java.util.List;
import java.util.Locale;

/** E03: one user's profile, history tabs and, when a request is waiting, the account verification block. */
public class AdminUserDetailFragment extends Fragment {

    private static final String ARG_USER_ID = "userId";
    private static final String KEY_SELECTED_TAB = "selectedTab";
    private static final String KEY_REJECT_OPEN = "rejectOpen";
    private static final String KEY_REJECT_TEXT = "rejectText";
    private static final String MONTH_PATTERN = "MM/yyyy";
    private static final String MASK_CHAR = "*";
    private static final int VISIBLE_PHONE_DIGITS = 3;
    private static final int TAB_LISTINGS = 0;
    private static final int TAB_DEALS = 1;
    private static final int TAB_REVIEWS = 2;
    private static final int TAB_WARNINGS = 3;
    private static final int[] TAB_TITLES = {
            R.string.admin_user_detail_tab_listings,
            R.string.admin_user_detail_tab_deals,
            R.string.admin_user_detail_tab_reviews,
            R.string.admin_user_detail_tab_warnings,
            R.string.admin_user_detail_tab_logs};
    private static final int[] EMPTY_TITLES = {
            R.string.admin_user_detail_empty_listings_title,
            R.string.admin_user_detail_empty_deals_title,
            R.string.admin_user_detail_empty_reviews_title,
            R.string.admin_user_detail_empty_warnings_title,
            R.string.admin_user_detail_empty_logs_title};
    private static final int[] EMPTY_MESSAGES = {
            R.string.admin_user_detail_empty_listings_message,
            R.string.admin_user_detail_empty_deals_message,
            R.string.admin_user_detail_empty_reviews_message,
            R.string.admin_user_detail_empty_warnings_message,
            R.string.admin_user_detail_empty_logs_message};

    @Nullable
    private FragmentAdminUserDetailBinding binding;
    private AdminUserViewModel viewModel;
    private AdminHistoryAdapter adapter;
    private final Locale locale = Locale.forLanguageTag("vi-VN");
    private String userId;
    private int selectedTab = TAB_WARNINGS;
    // The error text while no profile could be loaded, so the full error state can show it.
    @Nullable
    private String screenError;
    @Nullable
    private AlertDialog rejectDialog;
    @Nullable
    private DialogAdminVerificationRejectBinding rejectBinding;
    private boolean shownImages;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminUserDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        userId = readUserId();
        viewModel = new ViewModelProvider(this).get(AdminUserViewModel.class);
        if (savedInstanceState != null) {
            selectedTab = savedInstanceState.getInt(KEY_SELECTED_TAB, TAB_WARNINGS);
        }
        setUpToolbar();
        setUpTabs();
        setUpButtons();
        binding.recyclerHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new AdminHistoryAdapter();
        binding.recyclerHistory.setAdapter(adapter);
        observeProfile();
        observeHistory();
        observeVerification();
        observeState();
        // Each load returns at once when the ViewModel already holds this user, as after a rotation.
        viewModel.loadProfile(userId);
        viewModel.loadActivityLog(userId);
        if (savedInstanceState != null && savedInstanceState.getBoolean(KEY_REJECT_OPEN, false)) {
            showRejectDialog(savedInstanceState.getString(KEY_REJECT_TEXT, ""));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (binding != null) {
            binding.includeOfflineBanner.getRoot().setVisibility(isOnline() ? View.GONE : View.VISIBLE);
            updateVerificationButtons();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_SELECTED_TAB, selectedTab);
        outState.putBoolean(KEY_REJECT_OPEN, rejectDialog != null);
        if (rejectBinding != null) {
            outState.putString(KEY_REJECT_TEXT, String.valueOf(rejectBinding.editRejectReason.getText()));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (rejectDialog != null) {
            rejectDialog.setOnDismissListener(null);
            rejectDialog.dismiss();
        }
        rejectDialog = null;
        rejectBinding = null;
        // Views are gone after this call, so the binding must not outlive them.
        binding = null;
    }

    private String readUserId() {
        Bundle arguments = getArguments();
        String argument = arguments == null ? null : arguments.getString(ARG_USER_ID);
        return argument == null ? "" : argument;
    }

    private void setUpToolbar() {
        binding.includeToolbar.textTitle.setText(R.string.admin_user_detail_title);
        binding.includeToolbar.buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View clicked) {
                NavHostFragment.findNavController(AdminUserDetailFragment.this).navigateUp();
            }
        });
        binding.includeErrorState.buttonRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View clicked) {
                retryAll();
            }
        });
    }

    private void setUpTabs() {
        binding.tabLayoutHistory.removeAllTabs();
        for (int title : TAB_TITLES) {
            binding.tabLayoutHistory.addTab(binding.tabLayoutHistory.newTab().setText(title));
        }
        TabLayout.Tab tab = binding.tabLayoutHistory.getTabAt(selectedTab);
        if (tab != null) {
            tab.select();
        }
        binding.tabLayoutHistory.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectedTab = tab.getPosition();
                renderHistory();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void setUpButtons() {
        binding.buttonApprove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View clicked) {
                viewModel.reviewVerification(userId, true, "");
            }
        });
        binding.buttonReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View clicked) {
                showRejectDialog("");
            }
        });
    }

    // Redraws one part of the screen when any LiveData it reads changes.
    private final class RedrawObserver<T> implements Observer<T> {

        private final boolean header;

        RedrawObserver(boolean header) {
            this.header = header;
        }

        @Override
        public void onChanged(T value) {
            if (header) {
                renderHeader();
            } else {
                renderHistory();
            }
        }
    }

    private void observeProfile() {
        viewModel.getSelectedUser().observe(getViewLifecycleOwner(), new Observer<User>() {
            @Override
            public void onChanged(User user) {
                onUserChanged(user);
            }
        });
        viewModel.getIdentity().observe(getViewLifecycleOwner(), new Observer<UserIdentity>() {
            @Override
            public void onChanged(UserIdentity identity) {
                renderIdentity(identity);
            }
        });
        viewModel.getStats().observe(getViewLifecycleOwner(), new RedrawObserver<ProfileStats>(true));
        viewModel.getReportCount().observe(getViewLifecycleOwner(), new RedrawObserver<Integer>(true));
    }

    private void observeHistory() {
        viewModel.getWarnings().observe(getViewLifecycleOwner(), new RedrawObserver<List<?>>(false));
        viewModel.getUserListings().observe(getViewLifecycleOwner(), new RedrawObserver<List<?>>(false));
        viewModel.getUserDeals().observe(getViewLifecycleOwner(), new RedrawObserver<List<?>>(false));
        viewModel.getUserReviews().observe(getViewLifecycleOwner(), new RedrawObserver<List<?>>(false));
        viewModel.getLogs().observe(getViewLifecycleOwner(), new RedrawObserver<List<?>>(false));
    }

    private void observeVerification() {
        viewModel.getVerifications().observe(getViewLifecycleOwner(), new Observer<List<SellerVerification>>() {
            @Override
            public void onChanged(List<SellerVerification> list) {
                renderVerification();
            }
        });
        viewModel.getVerificationImages().observe(getViewLifecycleOwner(), new Observer<List<byte[]>>() {
            @Override
            public void onChanged(List<byte[]> images) {
                showVerificationImages();
            }
        });
    }

    private void observeState() {
        viewModel.getLoading().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean loading) {
                updateScreenState();
                updateVerificationButtons();
            }
        });
        viewModel.getError().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String message) {
                onErrorChanged(message);
            }
        });
        viewModel.getDone().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean done) {
                if (Boolean.TRUE.equals(done)) {
                    closeRejectDialog();
                    viewModel.clearDone();
                    Snackbar.make(binding.getRoot(), R.string.admin_verification_done, Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void onUserChanged(@Nullable User user) {
        renderHeader();
        renderVerification();
        updateScreenState();
        if (user == null) {
            return;
        }
        if (user.getVerificationStatus() == VerificationStatus.PENDING) {
            viewModel.loadVerificationRequests();
        }
        if (screenError != null) {
            // The profile arrived after an earlier read failed, so the error moves from the full state to a banner.
            String message = screenError;
            screenError = null;
            updateScreenState();
            showRetrySnackbar(message);
            viewModel.clearError();
        }
    }

    private void onErrorChanged(@Nullable String message) {
        if (message == null || message.isEmpty()) {
            screenError = null;
            updateScreenState();
            return;
        }
        if (rejectBinding != null) {
            rejectBinding.inputRejectReason.setError(message);
            viewModel.clearError();
        } else if (viewModel.getSelectedUser().getValue() == null) {
            screenError = message;
            binding.includeErrorState.textErrorMessage.setText(message);
            updateScreenState();
        } else {
            showRetrySnackbar(message);
            viewModel.clearError();
        }
    }

    private void showRetrySnackbar(String message) {
        Snackbar snackbar = Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.common_retry, new View.OnClickListener() {
            @Override
            public void onClick(View clicked) {
                retryAll();
            }
        });
        snackbar.show();
    }

    // Every load skips what it already holds, and a failed read clears that memory, so this reads only what is missing.
    private void retryAll() {
        viewModel.clearError();
        viewModel.loadProfile(userId);
        viewModel.loadActivityLog(userId);
        User user = viewModel.getSelectedUser().getValue();
        if (user != null && user.getVerificationStatus() == VerificationStatus.PENDING) {
            viewModel.loadVerificationRequests();
        }
        if (currentRequest() != null) {
            viewModel.loadVerificationImages(userId);
        }
    }

    private void updateScreenState() {
        if (binding == null) {
            return;
        }
        boolean hasUser = viewModel.getSelectedUser().getValue() != null;
        boolean loading = Boolean.TRUE.equals(viewModel.getLoading().getValue());
        boolean failed = !hasUser && screenError != null;
        binding.scrollContent.setVisibility(hasUser ? View.VISIBLE : View.GONE);
        binding.includeErrorState.getRoot().setVisibility(failed ? View.VISIBLE : View.GONE);
        binding.progressLoading.setVisibility(loading || (!hasUser && !failed) ? View.VISIBLE : View.GONE);
    }

    private boolean isOnline() {
        try {
            return NetworkMonitor.getInstance().isOnline();
        } catch (UnsupportedOperationException e) {
            // NetworkMonitor still reports NOT_READY, so ask the system directly.
            ConnectivityManager manager = (ConnectivityManager)
                    requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            if (manager == null) {
                return true;
            }
            NetworkCapabilities capabilities = manager.getNetworkCapabilities(manager.getActiveNetwork());
            return capabilities != null
                    && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        }
    }

    private void renderHeader() {
        User user = viewModel.getSelectedUser().getValue();
        if (binding == null || user == null) {
            return;
        }
        String name = user.getDisplayName() == null ? "" : user.getDisplayName();
        binding.textUserName.setText(name);
        binding.includeAvatar.textInitial.setText(name.isEmpty() ? "" : name.substring(0, 1).toUpperCase(locale));
        String joined = AdminHistoryAdapter.formatDate(user.getCreatedAt(), MONTH_PATTERN);
        binding.textPhoneJoined.setText(
                getString(R.string.admin_user_detail_phone_joined, phoneText(user.getPhone()), joined));
        StatusChips.bind(binding.includeAccountChip.getRoot(), user.getStatus());
        StatusChips.bind(binding.includeVerificationChip.getRoot(), user.getVerificationStatus());
        ProfileStats stats = viewModel.getStats().getValue();
        binding.textStatDeals.setText(stats == null ? "" : String.valueOf(stats.getDealCount()));
        binding.includeRating.ratingStars.setRating((float) user.ratingAverage());
        binding.includeRating.textRatingValue.setText(String.format(locale, "%.1f", user.ratingAverage()));
        binding.includeRating.textRatingCount.setText(
                getString(R.string.admin_user_detail_rating_count, user.getRatingCount()));
        Integer reports = viewModel.getReportCount().getValue();
        binding.textStatReports.setText(reports == null ? "" : String.valueOf(reports));
    }

    // The national id is deliberately never read here; it only appears in the verification block.
    private void renderIdentity(@Nullable UserIdentity identity) {
        if (binding == null) {
            return;
        }
        int detailVisibility = identity == null ? View.GONE : View.VISIBLE;
        binding.textIdentityGender.setVisibility(detailVisibility);
        binding.textIdentityAddress.setVisibility(detailVisibility);
        if (identity == null) {
            binding.textIdentityBirth.setText(R.string.admin_user_detail_identity_empty);
            return;
        }
        String notProvided = getString(R.string.admin_user_detail_not_provided);
        String birth = notProvided;
        if (identity.getDateOfBirth() != null) {
            birth = AdminHistoryAdapter.formatDate(identity.getDateOfBirth(), AdminHistoryAdapter.DATE_PATTERN);
        }
        String address = identity.getPermanentAddress();
        if (address == null || address.trim().isEmpty()) {
            address = notProvided;
        }
        binding.textIdentityBirth.setText(getString(R.string.admin_user_detail_label_value,
                getString(R.string.admin_user_detail_identity_birth), birth));
        binding.textIdentityGender.setText(getString(R.string.admin_user_detail_label_value,
                getString(R.string.admin_user_detail_identity_gender), genderText(identity.getGender(), notProvided)));
        binding.textIdentityAddress.setText(getString(R.string.admin_user_detail_label_value,
                getString(R.string.admin_user_detail_identity_address), address));
    }

    private String genderText(@Nullable Gender gender, String notProvided) {
        if (gender == Gender.MALE) {
            return getString(R.string.admin_user_detail_gender_male);
        }
        if (gender == Gender.FEMALE) {
            return getString(R.string.admin_user_detail_gender_female);
        }
        if (gender == Gender.OTHER) {
            return getString(R.string.admin_user_detail_gender_other);
        }
        return notProvided;
    }

    private void renderHistory() {
        if (binding == null) {
            return;
        }
        List<AdminHistoryAdapter.Row> rows = rowsForTab();
        adapter.submitRows(rows);
        boolean empty = rows != null && rows.isEmpty();
        binding.includeEmptyState.getRoot().setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.includeEmptyState.textEmptyTitle.setText(EMPTY_TITLES[selectedTab]);
        binding.includeEmptyState.textEmptyMessage.setText(EMPTY_MESSAGES[selectedTab]);
    }

    @Nullable
    private List<AdminHistoryAdapter.Row> rowsForTab() {
        Context context = requireContext();
        if (selectedTab == TAB_LISTINGS) {
            return AdminHistoryAdapter.listingRows(viewModel.getUserListings().getValue());
        }
        if (selectedTab == TAB_DEALS) {
            return AdminHistoryAdapter.dealRows(viewModel.getUserDeals().getValue());
        }
        if (selectedTab == TAB_REVIEWS) {
            return AdminHistoryAdapter.reviewRows(context, viewModel.getUserReviews().getValue());
        }
        if (selectedTab == TAB_WARNINGS) {
            return AdminHistoryAdapter.warningRows(context, viewModel.getWarnings().getValue());
        }
        return AdminHistoryAdapter.logRows(context, viewModel.getLogs().getValue());
    }

    private void renderVerification() {
        if (binding == null) {
            return;
        }
        SellerVerification request = currentRequest();
        if (request == null) {
            binding.layoutVerification.setVisibility(View.GONE);
            shownImages = false;
            closeRejectDialog();
            return;
        }
        binding.layoutVerification.setVisibility(View.VISIBLE);
        binding.textNationalId.setText(request.getNationalId());
        int resubmitted = request.getResubmitCount();
        binding.textResubmit.setText(getString(R.string.admin_verification_resubmit, resubmitted,
                resubmitted + request.remainingResubmits()));
        updateVerificationButtons();
        if (!shownImages) {
            binding.imageDocument.setImageResource(R.drawable.ic_inbox);
            binding.imagePortrait.setImageResource(R.drawable.ic_inbox);
            viewModel.loadVerificationImages(userId);
            showVerificationImages();
        }
    }

    @Nullable
    private SellerVerification currentRequest() {
        return AdminVerificationRules.findRequest(viewModel.getVerifications().getValue(), userId);
    }

    private void showVerificationImages() {
        SellerVerification request = currentRequest();
        List<byte[]> images = viewModel.getVerificationImages().getValue();
        if (binding == null || request == null || images == null || request.getImageIds() == null) {
            return;
        }
        shownImages = true;
        bindImage(binding.imageDocument, images, request.getImageIds(), 0);
        bindImage(binding.imagePortrait, images, request.getImageIds(), 1);
    }

    private void bindImage(ImageView view, List<byte[]> images, List<String> imageIds, int index) {
        byte[] bytes = images.size() > index ? images.get(index) : null;
        if (bytes == null || bytes.length == 0 || imageIds.size() <= index) {
            view.setImageResource(R.drawable.ic_error);
            return;
        }
        ImageLoader.load(bytes, imageIds.get(index), view, R.drawable.ic_inbox, R.drawable.ic_error);
    }

    // Both buttons need an ACTIVE account, a PENDING request, a network and no read or write in flight.
    private void updateVerificationButtons() {
        SellerVerification request = currentRequest();
        User user = viewModel.getSelectedUser().getValue();
        if (binding == null || request == null || user == null) {
            return;
        }
        boolean reviewable = AdminVerificationRules.canReview(user.getStatus(), request.getStatus());
        boolean online = isOnline();
        boolean busy = Boolean.TRUE.equals(viewModel.getLoading().getValue());
        boolean enabled = reviewable && online && !busy;
        binding.buttonApprove.setEnabled(enabled);
        binding.buttonReject.setEnabled(enabled);
        if (rejectBinding != null) {
            rejectBinding.buttonRejectConfirm.setEnabled(enabled);
        }
        int blockedText = reviewable ? R.string.admin_verification_blocked_offline
                : R.string.admin_verification_blocked_locked;
        binding.textVerificationBlocked.setText(blockedText);
        binding.textVerificationBlocked.setVisibility(reviewable && online ? View.GONE : View.VISIBLE);
    }

    private void showRejectDialog(String initialText) {
        if (rejectDialog != null) {
            return;
        }
        rejectBinding = DialogAdminVerificationRejectBinding.inflate(getLayoutInflater());
        rejectBinding.editRejectReason.setText(initialText);
        rejectBinding.buttonRejectCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View clicked) {
                closeRejectDialog();
            }
        });
        rejectBinding.buttonRejectConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View clicked) {
                confirmReject();
            }
        });
        rejectDialog = new AlertDialog.Builder(requireContext()).setView(rejectBinding.getRoot()).create();
        rejectDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                rejectDialog = null;
                rejectBinding = null;
            }
        });
        rejectDialog.show();
        updateVerificationButtons();
    }

    private void confirmReject() {
        if (rejectBinding == null) {
            return;
        }
        String reason = String.valueOf(rejectBinding.editRejectReason.getText());
        if (!AdminVerificationRules.isRejectReasonValid(reason)) {
            rejectBinding.inputRejectReason.setError(getString(R.string.admin_verification_reject_required));
            return;
        }
        rejectBinding.inputRejectReason.setError(null);
        viewModel.reviewVerification(userId, false, reason.trim());
    }

    private void closeRejectDialog() {
        if (rejectDialog != null) {
            rejectDialog.dismiss();
        }
    }

    // Keeps the last three digits, like E02 shows a phone number.
    private String phoneText(@Nullable String phone) {
        if (phone == null || phone.isEmpty()) {
            return getString(R.string.admin_user_detail_no_phone);
        }
        if (phone.length() <= VISIBLE_PHONE_DIGITS) {
            return phone;
        }
        int hidden = phone.length() - VISIBLE_PHONE_DIGITS;
        StringBuilder masked = new StringBuilder();
        for (int index = 0; index < hidden; index++) {
            masked.append(MASK_CHAR);
        }
        masked.append(phone.substring(hidden));
        return masked.toString();
    }
}
