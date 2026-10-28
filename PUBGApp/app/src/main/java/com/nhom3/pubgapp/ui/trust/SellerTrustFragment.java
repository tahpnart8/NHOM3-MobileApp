package com.nhom3.pubgapp.ui.trust;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;
import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.databinding.FragmentSellerTrustBinding;
import com.nhom3.pubgapp.model.Listing;
import com.nhom3.pubgapp.model.ProfileStats;
import com.nhom3.pubgapp.model.Review;
import com.nhom3.pubgapp.model.User;
import com.nhom3.pubgapp.model.enums.ReportTarget;
import com.nhom3.pubgapp.model.enums.VerificationStatus;
import com.nhom3.pubgapp.ui.common.AvatarBinder;
import com.nhom3.pubgapp.ui.common.StatusChips;
import com.nhom3.pubgapp.ui.profile.ProfileListingAdapter;
import com.nhom3.pubgapp.util.NetworkMonitor;
import com.nhom3.pubgapp.util.SessionManager;
import com.nhom3.pubgapp.viewmodel.ProfileViewModel;
import com.nhom3.pubgapp.viewmodel.TrustViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** D03: a seller's trust page, with rating, the three criteria, recent reviews and listings on sale. */
public class SellerTrustFragment extends Fragment {

    private static final String ARG_USER_ID = "userId";
    private static final String ARG_TARGET_TYPE = "targetType";
    private static final String ARG_TARGET_ID = "targetId";
    private static final String JOINED_PATTERN = "MM/yyyy";
    private static final int GRID_COLUMNS = 2;
    private static final int RECENT_REVIEW_LIMIT = 10;
    private static final int PERCENT = 100;

    private FragmentSellerTrustBinding binding;
    private ProfileViewModel profileViewModel;
    private TrustViewModel trustViewModel;
    private ProfileListingAdapter listingAdapter;
    private ReviewAdapter reviewAdapter;
    private String userId;
    private boolean ownPage;
    private boolean leaving;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentSellerTrustBinding.inflate(inflater, container, false);
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
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        trustViewModel = new ViewModelProvider(this).get(TrustViewModel.class);
        setUpToolbar();
        setUpLists();
        setUpButtons();
        observeProfile();
        observeReviews();
        // Each load returns at once when the ViewModel already holds this user's data, as after a rotation.
        profileViewModel.loadProfile(userId);
        profileViewModel.loadUserListings(userId);
        trustViewModel.loadReviews(userId, true);
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
        binding.includeToolbar.textTitle.setText(R.string.profile_seller_trust_title);
        binding.includeToolbar.buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View clicked) {
                NavHostFragment.findNavController(SellerTrustFragment.this).navigateUp();
            }
        });
    }

    private void setUpLists() {
        listingAdapter = new ProfileListingAdapter();
        binding.recyclerListings.setLayoutManager(new GridLayoutManager(requireContext(), GRID_COLUMNS));
        binding.recyclerListings.setAdapter(listingAdapter);
        binding.textSellingTitle.setText(R.string.profile_seller_trust_selling_loading);
        reviewAdapter = new ReviewAdapter();
        binding.recyclerReviews.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerReviews.setAdapter(reviewAdapter);
        binding.includeReviewsEmpty.buttonEmptyAction.setText(R.string.common_retry);
        binding.includeReviewsEmpty.buttonEmptyAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View clicked) {
                trustViewModel.loadReviews(userId, true);
            }
        });
        binding.includeListingsEmpty.buttonEmptyAction.setText(R.string.common_retry);
        binding.includeListingsEmpty.buttonEmptyAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View clicked) {
                profileViewModel.loadUserListings(userId);
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
        View.OnClickListener openProfile = new View.OnClickListener() {
            @Override
            public void onClick(View clicked) {
                openUserProfile();
            }
        };
        binding.includeAvatar.getRoot().setOnClickListener(openProfile);
        binding.layoutNameBlock.setOnClickListener(openProfile);
        binding.buttonFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View clicked) {
                toggleFollow();
            }
        });
        binding.buttonReportUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View clicked) {
                openReport();
            }
        });
        binding.buttonFollow.setVisibility(ownPage ? View.GONE : View.VISIBLE);
        binding.buttonReportUser.setVisibility(ownPage ? View.GONE : View.VISIBLE);
        showOwnStatsOnly();
    }

    // Deal counts are readable only on the own page (Rules on deals), so another user's page hides them.
    private void showOwnStatsOnly() {
        int visibility = ownPage ? View.VISIBLE : View.GONE;
        binding.layoutCompletionStat.setVisibility(visibility);
        binding.layoutDealStat.setVisibility(visibility);
    }

    private void openUserProfile() {
        Bundle arguments = new Bundle();
        arguments.putString(ARG_USER_ID, userId);
        NavHostFragment.findNavController(this).navigate(R.id.action_global_user_profile, arguments);
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
                showListings(listings);
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
                showProfileError(message);
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

    private void observeReviews() {
        trustViewModel.getReviews().observe(getViewLifecycleOwner(), new Observer<List<Review>>() {
            @Override
            public void onChanged(List<Review> reviews) {
                showReviews();
            }
        });
        trustViewModel.getLoading().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean loading) {
                showReviews();
            }
        });
        trustViewModel.getError().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String message) {
                if (message == null || message.isEmpty()) {
                    return;
                }
                Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
                trustViewModel.clearError();
            }
        });
    }

    private void updateScreenState() {
        boolean loading = Boolean.TRUE.equals(profileViewModel.getLoading().getValue());
        boolean hasProfile = profileViewModel.getProfile().getValue() != null;
        binding.progressLoading.setVisibility(loading && !hasProfile ? View.VISIBLE : View.GONE);
        binding.scrollContent.setVisibility(hasProfile ? View.VISIBLE : View.GONE);
        boolean failed = !hasProfile && !loading;
        binding.includeErrorState.getRoot().setVisibility(failed ? View.VISIBLE : View.GONE);
        boolean statsFailed = ownPage && hasProfile && !loading && profileViewModel.getStats().getValue() == null;
        binding.buttonRetryStats.setVisibility(statsFailed ? View.VISIBLE : View.GONE);
        if (statsFailed) {
            binding.textCompletionRate.setText(R.string.profile_user_profile_stat_unknown);
            binding.textDealCount.setText(R.string.profile_user_profile_stat_unknown);
        }
        boolean statsLoading = ownPage && loading && profileViewModel.getStats().getValue() == null;
        if (statsLoading) {
            binding.textCompletionRate.setText(R.string.profile_user_profile_stat_loading);
            binding.textDealCount.setText(R.string.profile_user_profile_stat_loading);
        }
    }

    private void showProfileError(String message) {
        if (message == null || message.isEmpty()) {
            return;
        }
        if (profileViewModel.getUserListings().getValue() == null) {
            showListingsFailed();
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
        showVerifiedBadge(user.getVerificationStatus());
        showRating(user);
        showAvatar(user);
    }

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
        String monthYear = formatJoinedMonth(user.getCreatedAt().toDate());
        binding.textJoined.setText(getString(R.string.profile_seller_trust_joined, monthYear));
        binding.textJoined.setVisibility(View.VISIBLE);
    }

    // "10/2026" for the line "tham gia tháng 10/2026"; the year alone did not say when.
    static String formatJoinedMonth(Date joined) {
        SimpleDateFormat format = new SimpleDateFormat(JOINED_PATTERN, Locale.ROOT);
        return format.format(joined);
    }

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
        AvatarBinder.bind(binding.includeAvatar, user.getDisplayName(), user.getAvatarBase64());
    }

    private void showStats(ProfileStats stats) {
        if (stats == null) {
            return;
        }
        long percent = Math.round(stats.completionRate() * PERCENT);
        binding.textCompletionRate.setText(getString(R.string.profile_user_profile_completion_value, (int) percent));
        binding.textDealCount.setText(String.valueOf(stats.getDealCount()));
    }

    private void updateFollowButton() {
        if (ownPage) {
            return;
        }
        Boolean followingValue = profileViewModel.getFollowing().getValue();
        boolean following = Boolean.TRUE.equals(followingValue);
        boolean loading = Boolean.TRUE.equals(profileViewModel.getLoading().getValue());
        boolean online = NetworkMonitor.getInstance().isOnline();
        binding.buttonFollow.setText(following ? R.string.profile_user_profile_following
                : R.string.profile_user_profile_follow);
        // Until the state is known a tap could write follows again, which the Rules refuse.
        binding.buttonFollow.setEnabled(online && !loading && followingValue != null);
    }

    private void showListings(List<Listing> all) {
        List<Listing> selling = ProfileListingAdapter.filterSelling(all);
        if (all == null) {
            binding.textSellingTitle.setText(R.string.profile_seller_trust_selling_loading);
        } else {
            binding.textSellingTitle.setText(getString(R.string.profile_seller_trust_selling, selling.size()));
        }
        binding.progressListings.setVisibility(all == null ? View.VISIBLE : View.GONE);
        listingAdapter.submitList(selling);
        boolean empty = all != null && selling.isEmpty();
        binding.recyclerListings.setVisibility(selling.isEmpty() ? View.GONE : View.VISIBLE);
        binding.includeListingsEmpty.buttonEmptyAction.setVisibility(View.GONE);
        binding.includeListingsEmpty.textEmptyTitle.setText(R.string.profile_seller_trust_selling_empty_title);
        binding.includeListingsEmpty.textEmptyMessage.setText(R.string.profile_seller_trust_selling_empty_message);
        binding.includeListingsEmpty.getRoot().setVisibility(empty ? View.VISIBLE : View.GONE);
    }

    private void showListingsFailed() {
        binding.progressListings.setVisibility(View.GONE);
        binding.includeListingsEmpty.textEmptyTitle.setText(R.string.profile_user_profile_listings_failed_title);
        binding.includeListingsEmpty.textEmptyMessage.setText(R.string.common_error_generic);
        binding.includeListingsEmpty.buttonEmptyAction.setVisibility(View.VISIBLE);
        binding.includeListingsEmpty.getRoot().setVisibility(View.VISIBLE);
    }

    private void showReviews() {
        List<Review> reviews = trustViewModel.getReviews().getValue();
        boolean loading = Boolean.TRUE.equals(trustViewModel.getLoading().getValue());
        binding.progressReviews.setVisibility(loading ? View.VISIBLE : View.GONE);
        showCriteria(reviews);
        List<Review> recent = firstReviews(reviews);
        reviewAdapter.submitList(recent);
        binding.recyclerReviews.setVisibility(recent.isEmpty() ? View.GONE : View.VISIBLE);
        if (loading) {
            binding.includeReviewsEmpty.getRoot().setVisibility(View.GONE);
            return;
        }
        // Nothing loading and no list means the read failed.
        boolean failed = reviews == null;
        binding.includeReviewsEmpty.buttonEmptyAction.setVisibility(failed ? View.VISIBLE : View.GONE);
        if (failed) {
            binding.includeReviewsEmpty.textEmptyTitle.setText(R.string.profile_user_profile_reviews_failed_title);
            binding.includeReviewsEmpty.textEmptyMessage.setText(R.string.common_error_generic);
        } else {
            binding.includeReviewsEmpty.textEmptyTitle.setText(R.string.profile_seller_trust_reviews_empty_title);
            binding.includeReviewsEmpty.textEmptyMessage.setText(R.string.profile_seller_trust_reviews_empty_message);
        }
        binding.includeReviewsEmpty.getRoot().setVisibility(recent.isEmpty() ? View.VISIBLE : View.GONE);
    }

    // The repository sorts newest first, so the first rows are the most recent ones.
    private static List<Review> firstReviews(List<Review> reviews) {
        List<Review> recent = new ArrayList<Review>();
        if (reviews == null) {
            return recent;
        }
        int count = Math.min(reviews.size(), RECENT_REVIEW_LIMIT);
        for (int i = 0; i < count; i++) {
            recent.add(reviews.get(i));
        }
        return recent;
    }

    private void showCriteria(List<Review> reviews) {
        boolean hasReviews = reviews != null && !reviews.isEmpty();
        double[] averages = TrustViewModel.averageCriteria(reviews);
        showCriterion(binding.textAccuracyAverage, hasReviews, averages[TrustViewModel.CRITERIA_ACCURACY]);
        showCriterion(binding.textAttitudeAverage, hasReviews, averages[TrustViewModel.CRITERIA_ATTITUDE]);
        showCriterion(binding.textCommitmentAverage, hasReviews, averages[TrustViewModel.CRITERIA_COMMITMENT]);
    }

    private void showCriterion(TextView target, boolean hasReviews, double average) {
        if (!hasReviews) {
            target.setText(R.string.profile_user_profile_stat_unknown);
            return;
        }
        target.setText(getString(R.string.profile_seller_trust_criteria_value, average));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // The view is gone; keeping the binding would leak it.
        binding = null;
    }
}
