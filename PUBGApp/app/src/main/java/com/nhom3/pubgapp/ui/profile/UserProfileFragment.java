package com.nhom3.pubgapp.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.databinding.FragmentUserProfileBinding;
import com.nhom3.pubgapp.model.Listing;
import com.nhom3.pubgapp.model.ProfileStats;
import com.nhom3.pubgapp.model.User;
import com.nhom3.pubgapp.model.enums.ListingStatus;
import com.nhom3.pubgapp.model.enums.ReportTarget;
import com.nhom3.pubgapp.model.enums.VerificationStatus;
import com.nhom3.pubgapp.ui.common.ImageLoader;
import com.nhom3.pubgapp.ui.common.StatusChips;
import com.nhom3.pubgapp.util.NetworkMonitor;
import com.nhom3.pubgapp.util.SessionManager;
import com.nhom3.pubgapp.viewmodel.ProfileViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** D02: a user's public page, with follow, listings by tab and the received and given reviews. */
public class UserProfileFragment extends Fragment {

    static final String ARG_USER_ID = "userId";
    private static final String ARG_TARGET_TYPE = "targetType";
    private static final String ARG_TARGET_ID = "targetId";
    private static final String STATE_TAB = "selectedTab";
    private static final String STATE_LISTINGS_FAILED = "listingsFailed";
    private static final String JOINED_PATTERN = "MM/yyyy";
    private static final int GRID_COLUMNS = 2;
    private static final int TAB_SELLING = 0;
    private static final int TAB_SOLD = 1;
    private static final int TAB_REVIEWS = 2;
    private static final int PERCENT = 100;

    private FragmentUserProfileBinding binding;
    private ProfileViewModel profileViewModel;
    private ProfileListingAdapter listingAdapter;
    private String userId;
    private boolean ownPage;
    // Tab and the listing failure flag survive rotation through onSaveInstanceState.
    private int selectedTab = TAB_SELLING;
    private boolean listingsFailed;
    private boolean leaving;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            selectedTab = savedInstanceState.getInt(STATE_TAB, TAB_SELLING);
            listingsFailed = savedInstanceState.getBoolean(STATE_LISTINGS_FAILED);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_TAB, selectedTab);
        outState.putBoolean(STATE_LISTINGS_FAILED, listingsFailed);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentUserProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (!SessionManager.getInstance().isLoggedIn()) {
            NavHostFragment.findNavController(this).navigate(R.id.action_global_sign_in);
            return;
        }
        String currentUid = SessionManager.getInstance().getCurrentUserId();
        userId = readUserId(currentUid);
        ownPage = userId != null && userId.equals(currentUid);
        // Fragment scope: D01 keeps its own profile in the Activity-scoped ViewModel.
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        setUpToolbar();
        setUpListings();
        setUpTabs();
        setUpButtons();
        observeProfile();
        profileViewModel.loadProfile(userId);
        profileViewModel.loadUserListings(userId);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (binding == null || profileViewModel == null) {
            return;
        }
        boolean online = NetworkMonitor.getInstance().isOnline();
        binding.includeOfflineBanner.getRoot().setVisibility(online ? View.GONE : View.VISIBLE);
        updateFollowButton();
    }

    private String readUserId(String currentUid) {
        Bundle arguments = getArguments();
        if (arguments == null) {
            return currentUid;
        }
        String argument = arguments.getString(ARG_USER_ID);
        if (argument == null || argument.isEmpty()) {
            return currentUid;
        }
        return argument;
    }

    private void setUpToolbar() {
        binding.includeToolbar.textTitle.setText(R.string.profile_user_profile_title);
        binding.includeToolbar.buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View clicked) {
                NavHostFragment.findNavController(UserProfileFragment.this).navigateUp();
            }
        });
    }

    private void setUpListings() {
        listingAdapter = new ProfileListingAdapter();
        binding.recyclerListings.setLayoutManager(new GridLayoutManager(requireContext(), GRID_COLUMNS));
        binding.recyclerListings.setAdapter(listingAdapter);
        binding.includeEmptyState.buttonEmptyAction.setText(R.string.common_retry);
        binding.includeEmptyState.buttonEmptyAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View clicked) {
                listingsFailed = false;
                profileViewModel.loadUserListings(userId);
                showTabContent();
            }
        });
    }

    private void setUpTabs() {
        TabLayout tabs = binding.tabsContent;
        tabs.addTab(tabs.newTab());
        tabs.addTab(tabs.newTab());
        tabs.addTab(tabs.newTab().setText(R.string.profile_user_profile_tab_reviews));
        updateTabCounts(profileViewModel.getUserListings().getValue());
        TabLayout.Tab restored = tabs.getTabAt(selectedTab);
        if (restored != null) {
            restored.select();
        }
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectedTab = tab.getPosition();
                showTabContent();
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
        binding.includeErrorState.buttonRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View clicked) {
                profileViewModel.loadProfile(userId);
            }
        });
        binding.buttonRetryStats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View clicked) {
                profileViewModel.loadProfile(userId);
            }
        });
        binding.layoutRatingStat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View clicked) {
                navigateWithUserId(R.id.action_global_seller_trust);
            }
        });
        binding.buttonFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View clicked) {
                toggleFollow();
            }
        });
        binding.buttonEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View clicked) {
                NavHostFragment.findNavController(UserProfileFragment.this).navigate(R.id.action_global_edit_profile);
            }
        });
        binding.buttonReportUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View clicked) {
                openReport();
            }
        });
        binding.buttonEditProfile.setVisibility(ownPage ? View.VISIBLE : View.GONE);
        binding.buttonFollow.setVisibility(ownPage ? View.GONE : View.VISIBLE);
        binding.buttonReportUser.setVisibility(ownPage ? View.GONE : View.VISIBLE);
    }

    private void toggleFollow() {
        if (Boolean.TRUE.equals(profileViewModel.getFollowing().getValue())) {
            profileViewModel.unfollow(userId);
        } else {
            profileViewModel.follow(userId);
        }
    }

    // A USER report has no conversation, so conversationId keeps its null default.
    private void openReport() {
        Bundle arguments = new Bundle();
        arguments.putString(ARG_TARGET_TYPE, ReportTarget.USER.name());
        arguments.putString(ARG_TARGET_ID, userId);
        NavHostFragment.findNavController(this).navigate(R.id.action_global_report, arguments);
    }

    private void navigateWithUserId(int actionId) {
        Bundle arguments = new Bundle();
        arguments.putString(ARG_USER_ID, userId);
        NavHostFragment.findNavController(this).navigate(actionId, arguments);
    }

    private void observeProfile() {
        profileViewModel.getProfile().observe(getViewLifecycleOwner(), new Observer<User>() {
            @Override
            public void onChanged(User user) {
                showProfile(user);
            }
        });
        profileViewModel.getStats().observe(getViewLifecycleOwner(), new Observer<ProfileStats>() {
            @Override
            public void onChanged(ProfileStats stats) {
                showStats(stats);
            }
        });
        profileViewModel.getUserListings().observe(getViewLifecycleOwner(), new Observer<List<Listing>>() {
            @Override
            public void onChanged(List<Listing> listings) {
                if (listings != null) {
                    listingsFailed = false;
                }
                updateTabCounts(listings);
                showTabContent();
            }
        });
        profileViewModel.getFollowing().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean following) {
                updateFollowButton();
            }
        });
        profileViewModel.getLoading().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean loading) {
                updateScreenState();
                updateFollowButton();
            }
        });
        profileViewModel.getError().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String message) {
                showError(message);
            }
        });
        profileViewModel.getDone().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean finished) {
                if (Boolean.TRUE.equals(finished)) {
                    profileViewModel.clearDone();
                }
            }
        });
    }

    private void updateScreenState() {
        boolean loading = Boolean.TRUE.equals(profileViewModel.getLoading().getValue());
        boolean hasProfile = profileViewModel.getProfile().getValue() != null;
        binding.progressLoading.setVisibility(loading && !hasProfile ? View.VISIBLE : View.GONE);
        binding.scrollContent.setVisibility(hasProfile ? View.VISIBLE : View.GONE);
        // Nothing loaded and nothing loading means the last read failed, also after a rotation.
        boolean failed = !hasProfile && !loading;
        binding.includeErrorState.getRoot().setVisibility(failed ? View.VISIBLE : View.GONE);
        boolean statsFailed = hasProfile && !loading && profileViewModel.getStats().getValue() == null;
        binding.buttonRetryStats.setVisibility(statsFailed ? View.VISIBLE : View.GONE);
        if (statsFailed) {
            binding.textSoldCount.setText(R.string.profile_user_profile_stat_unknown);
            binding.textCompletionRate.setText(R.string.profile_user_profile_stat_unknown);
            binding.textFollowerCount.setText(R.string.profile_user_profile_stat_unknown);
        }
    }

    private void showError(String message) {
        if (message == null || message.isEmpty()) {
            return;
        }
        if (profileViewModel.getUserListings().getValue() == null) {
            listingsFailed = true;
            showTabContent();
        }
        if (profileViewModel.getProfile().getValue() == null) {
            binding.includeErrorState.textErrorMessage.setText(message);
            binding.includeErrorState.getRoot().setVisibility(View.VISIBLE);
        } else {
            Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
        }
        profileViewModel.clearError();
    }

    private void showProfile(User user) {
        updateScreenState();
        if (user == null) {
            return;
        }
        if (user.isLocked() || user.isDeleted()) {
            leaveUnavailable();
            return;
        }
        binding.textDisplayName.setText(user.getDisplayName());
        binding.textLocation.setText(formatLocation(user));
        showJoined(user);
        showBio(user.getBio());
        showVerifiedBadge(user.getVerificationStatus());
        showRating(user);
        showAvatar(user);
    }

    // UC29 exception 1c: a locked or deleted account is not shown; say so and go back once.
    private void leaveUnavailable() {
        if (leaving) {
            return;
        }
        leaving = true;
        Toast.makeText(requireContext(), R.string.profile_user_profile_unavailable, Toast.LENGTH_LONG).show();
        NavHostFragment.findNavController(this).navigateUp();
    }

    private String formatLocation(User user) {
        String district = user.getDistrict();
        String province = user.getProvince();
        boolean hasDistrict = district != null && !district.isEmpty();
        boolean hasProvince = province != null && !province.isEmpty();
        if (hasDistrict && hasProvince) {
            return getString(R.string.profile_my_profile_location, district, province);
        }
        if (hasProvince) {
            return province;
        }
        if (hasDistrict) {
            return district;
        }
        return getString(R.string.profile_my_profile_no_location);
    }

    private void showJoined(User user) {
        if (user.getCreatedAt() == null) {
            binding.textJoined.setVisibility(View.GONE);
            return;
        }
        SimpleDateFormat format = new SimpleDateFormat(JOINED_PATTERN, Locale.ROOT);
        String month = format.format(user.getCreatedAt().toDate());
        binding.textJoined.setText(getString(R.string.profile_user_profile_joined, month));
        binding.textJoined.setVisibility(View.VISIBLE);
    }

    private void showBio(String bio) {
        boolean hasBio = bio != null && !bio.trim().isEmpty();
        binding.textBio.setText(hasBio ? bio : "");
        binding.textBio.setVisibility(hasBio ? View.VISIBLE : View.GONE);
    }

    // UC29 BR3: only APPROVED shows the badge; the other states show nothing on a public page.
    private void showVerifiedBadge(VerificationStatus status) {
        if (status != VerificationStatus.APPROVED) {
            binding.includeVerifiedChip.getRoot().setVisibility(View.GONE);
            return;
        }
        StatusChips.bind(binding.includeVerifiedChip.getRoot(), status);
        binding.includeVerifiedChip.getRoot().setText(R.string.profile_user_profile_verified);
    }

    private void showRating(User user) {
        int count = user.getRatingCount();
        if (count == 0) {
            binding.includeRating.ratingStars.setRating(0f);
            binding.includeRating.textRatingValue.setText(R.string.profile_user_profile_stat_unknown);
            binding.includeRating.textRatingCount.setVisibility(View.GONE);
            return;
        }
        double average = user.ratingAverage();
        binding.includeRating.ratingStars.setRating((float) average);
        binding.includeRating.textRatingValue.setText(getString(R.string.profile_user_profile_rating_value, average));
        binding.includeRating.textRatingCount.setText(getString(R.string.profile_user_profile_rating_count, count));
        binding.includeRating.textRatingCount.setVisibility(View.VISIBLE);
    }

    private void showAvatar(User user) {
        String name = user.getDisplayName();
        String initial = "";
        if (name != null && !name.trim().isEmpty()) {
            initial = name.trim().substring(0, 1).toUpperCase(Locale.ROOT);
        }
        binding.includeAvatar.textInitial.setText(initial);
        String avatarBase64 = user.getAvatarBase64();
        boolean hasPhoto = avatarBase64 != null && !avatarBase64.isEmpty();
        binding.includeAvatar.imageAvatar.setVisibility(hasPhoto ? View.VISIBLE : View.GONE);
        binding.includeAvatar.textInitial.setVisibility(hasPhoto ? View.GONE : View.VISIBLE);
        if (hasPhoto) {
            ImageLoader.load(avatarBase64, binding.includeAvatar.imageAvatar,
                    R.drawable.bg_avatar, R.drawable.bg_avatar);
        }
    }

    private void showStats(ProfileStats stats) {
        if (stats == null) {
            return;
        }
        long percent = Math.round(stats.completionRate() * PERCENT);
        binding.textSoldCount.setText(String.valueOf(stats.getSoldCount()));
        binding.textCompletionRate.setText(getString(R.string.profile_user_profile_completion_value, (int) percent));
        binding.textFollowerCount.setText(String.valueOf(stats.getFollowerCount()));
    }

    private void updateFollowButton() {
        if (ownPage) {
            return;
        }
        boolean following = Boolean.TRUE.equals(profileViewModel.getFollowing().getValue());
        boolean loading = Boolean.TRUE.equals(profileViewModel.getLoading().getValue());
        boolean online = NetworkMonitor.getInstance().isOnline();
        binding.buttonFollow.setText(following ? R.string.profile_user_profile_following
                : R.string.profile_user_profile_follow);
        binding.buttonFollow.setEnabled(online && !loading);
    }

    private void updateTabCounts(List<Listing> listings) {
        TabLayout.Tab sellingTab = binding.tabsContent.getTabAt(TAB_SELLING);
        TabLayout.Tab soldTab = binding.tabsContent.getTabAt(TAB_SOLD);
        if (sellingTab == null || soldTab == null) {
            return;
        }
        int sellingCount = filterSelling(listings).size();
        int soldCount = filterSold(listings).size();
        sellingTab.setText(getString(R.string.profile_user_profile_tab_selling, sellingCount));
        soldTab.setText(getString(R.string.profile_user_profile_tab_sold, soldCount));
    }

    private void showTabContent() {
        boolean reviewsTab = selectedTab == TAB_REVIEWS;
        binding.layoutReviews.setVisibility(reviewsTab ? View.VISIBLE : View.GONE);
        if (reviewsTab) {
            binding.recyclerListings.setVisibility(View.GONE);
            binding.progressListings.setVisibility(View.GONE);
            binding.includeEmptyState.getRoot().setVisibility(View.GONE);
            return;
        }
        showListingTab();
    }

    private void showListingTab() {
        List<Listing> all = profileViewModel.getUserListings().getValue();
        boolean waiting = all == null && !listingsFailed;
        binding.progressListings.setVisibility(waiting ? View.VISIBLE : View.GONE);
        if (all == null) {
            listingAdapter.submitList(null);
            binding.recyclerListings.setVisibility(View.GONE);
            showListingsEmpty(listingsFailed);
            return;
        }
        List<Listing> shown = selectedTab == TAB_SOLD ? filterSold(all) : filterSelling(all);
        listingAdapter.submitList(shown);
        binding.recyclerListings.setVisibility(shown.isEmpty() ? View.GONE : View.VISIBLE);
        binding.includeEmptyState.getRoot().setVisibility(View.GONE);
        if (shown.isEmpty()) {
            showListingsEmpty(false);
        }
    }

    private void showListingsEmpty(boolean failed) {
        if (!failed && profileViewModel.getUserListings().getValue() == null) {
            binding.includeEmptyState.getRoot().setVisibility(View.GONE);
            return;
        }
        binding.includeEmptyState.buttonEmptyAction.setVisibility(failed ? View.VISIBLE : View.GONE);
        if (failed) {
            binding.includeEmptyState.textEmptyTitle.setText(R.string.profile_user_profile_listings_failed_title);
            binding.includeEmptyState.textEmptyMessage.setText(R.string.common_error_generic);
        } else if (selectedTab == TAB_SOLD) {
            binding.includeEmptyState.textEmptyTitle.setText(R.string.profile_user_profile_empty_sold_title);
            binding.includeEmptyState.textEmptyMessage.setText(R.string.profile_user_profile_empty_sold_message);
        } else {
            binding.includeEmptyState.textEmptyTitle.setText(R.string.profile_user_profile_empty_selling_title);
            binding.includeEmptyState.textEmptyMessage.setText(R.string.profile_user_profile_empty_selling_message);
        }
        binding.includeEmptyState.getRoot().setVisibility(View.VISIBLE);
    }

    // The owner's own query also returns DRAFT, HIDDEN and REMOVED; a public page never shows them.
    static List<Listing> filterSelling(List<Listing> all) {
        List<Listing> result = new ArrayList<Listing>();
        if (all == null) {
            return result;
        }
        for (Listing listing : all) {
            ListingStatus status = listing.getStatus();
            if (status == ListingStatus.AVAILABLE || status == ListingStatus.RESERVED) {
                result.add(listing);
            }
        }
        return result;
    }

    static List<Listing> filterSold(List<Listing> all) {
        List<Listing> result = new ArrayList<Listing>();
        if (all == null) {
            return result;
        }
        for (Listing listing : all) {
            if (listing.getStatus() == ListingStatus.SOLD) {
                result.add(listing);
            }
        }
        return result;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // The view is gone; keeping the binding would leak it.
        binding = null;
    }
}
