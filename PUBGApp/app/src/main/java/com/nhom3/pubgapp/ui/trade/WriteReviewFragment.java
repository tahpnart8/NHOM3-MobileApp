package com.nhom3.pubgapp.ui.trade;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.databinding.FragmentWriteReviewBinding;
import com.nhom3.pubgapp.databinding.ViewRatingInputBinding;
import com.nhom3.pubgapp.model.Deal;
import com.nhom3.pubgapp.model.Review;
import com.nhom3.pubgapp.model.enums.DealStatus;
import com.nhom3.pubgapp.ui.common.ImageLoader;
import com.nhom3.pubgapp.util.NetworkMonitor;
import com.nhom3.pubgapp.util.SessionManager;
import com.nhom3.pubgapp.viewmodel.DealViewModel;
import com.nhom3.pubgapp.viewmodel.TrustViewModel;

/** Screen C05: rate the other side of a received or completed deal, once. */
public class WriteReviewFragment extends Fragment {

    private static final String ARG_DEAL_ID = "dealId";
    private static final String KEY_RATING_OVERALL = "key_rating_overall";
    private static final String KEY_RATING_ACCURACY = "key_rating_accuracy";
    private static final String KEY_RATING_ATTITUDE = "key_rating_attitude";
    private static final String KEY_RATING_COMMITMENT = "key_rating_commitment";
    private static final float STAR_STEP = 1f;

    private FragmentWriteReviewBinding binding;
    private DealViewModel dealViewModel;
    private TrustViewModel trustViewModel;
    private String dealId;
    private boolean alreadyReviewed;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentWriteReviewBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dealViewModel = new ViewModelProvider(this).get(DealViewModel.class);
        trustViewModel = new ViewModelProvider(this).get(TrustViewModel.class);
        if (getArguments() != null) {
            dealId = getArguments().getString(ARG_DEAL_ID);
        }

        setupToolbar();
        setupRatingRow(binding.ratingOverall);
        setupRatingRow(binding.ratingAccuracy);
        setupRatingRow(binding.ratingAttitude);
        setupRatingRow(binding.ratingCommitment);
        if (savedInstanceState != null) {
            restoreRatings(savedInstanceState);
        }
        setupActions();
        observeDeal();
        observeSubmit();

        if (dealViewModel.getDeal().getValue() == null) {
            loadDeal();
        }
    }

    private void setupToolbar() {
        binding.includeToolbar.textTitle.setText(R.string.trade_write_review_title);
        binding.includeToolbar.buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(WriteReviewFragment.this).navigateUp();
            }
        });
    }

    private void setupRatingRow(ViewRatingInputBinding row) {
        // view_rating is drawn as a read-only indicator; C05 turns it into a whole-star picker.
        row.ratingStars.setIsIndicator(false);
        row.ratingStars.setStepSize(STAR_STEP);
        // The four rows share view ids, so Android would restore them all to one value; the Fragment keeps them.
        row.ratingStars.setSaveEnabled(false);
        row.textRatingCount.setVisibility(View.GONE);
        showRatingValue(row);
        row.ratingStars.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                showRatingValue(row);
                refreshSubmitState();
            }
        });
    }

    private void setupActions() {
        binding.buttonSubmitReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitReview();
            }
        });
        binding.includeErrorState.buttonRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadDeal();
            }
        });
    }

    private void loadDeal() {
        boolean online = NetworkMonitor.getInstance().isOnline();
        binding.includeOfflineBanner.layoutOfflineBanner.setVisibility(online ? View.GONE : View.VISIBLE);
        if (dealId == null || dealId.trim().isEmpty()) {
            showErrorState(getString(R.string.trade_write_review_not_found));
            return;
        }
        binding.includeErrorState.layoutErrorState.setVisibility(View.GONE);
        dealViewModel.loadDeal(dealId);
    }

    private void observeDeal() {
        dealViewModel.getDeal().observe(getViewLifecycleOwner(), new Observer<Deal>() {
            @Override
            public void onChanged(Deal deal) {
                if (deal != null) {
                    renderDeal(deal);
                }
            }
        });
        dealViewModel.getMyReview().observe(getViewLifecycleOwner(), new Observer<Review>() {
            @Override
            public void onChanged(Review review) {
                if (review != null) {
                    renderSubmittedReview(review);
                }
            }
        });
        dealViewModel.getLoading().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isLoading) {
                refreshProgress();
            }
        });
        dealViewModel.getError().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String message) {
                showDealError(message);
            }
        });
    }

    private void observeSubmit() {
        trustViewModel.getLoading().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isLoading) {
                refreshProgress();
                refreshSubmitState();
            }
        });
        trustViewModel.getError().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String message) {
                if (message != null && !message.isEmpty()) {
                    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
                    trustViewModel.clearError();
                }
            }
        });
        trustViewModel.getDone().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isDone) {
                if (Boolean.TRUE.equals(isDone)) {
                    trustViewModel.clearDone();
                    Toast.makeText(requireContext(), R.string.trade_write_review_success,
                            Toast.LENGTH_SHORT).show();
                    NavHostFragment.findNavController(WriteReviewFragment.this).navigateUp();
                }
            }
        });
    }

    private void renderDeal(Deal deal) {
        String uid = currentUserId();
        if (!deal.isParticipant(uid)) {
            showErrorState(getString(R.string.trade_write_review_not_participant));
            return;
        }
        if (deal.getStatus() != DealStatus.RECEIVED && deal.getStatus() != DealStatus.COMPLETED) {
            showErrorState(getString(R.string.trade_write_review_not_allowed));
            return;
        }
        binding.includeErrorState.layoutErrorState.setVisibility(View.GONE);
        binding.scrollContent.setVisibility(View.VISIBLE);
        binding.textItemTitle.setText(deal.getItemTitle() != null ? deal.getItemTitle() : "");
        // No profile LiveData holds the partner's name, so the line names the role (draft P2-C7, question 5).
        boolean isBuyer = uid.equals(deal.getBuyerId());
        binding.textPartner.setText(isBuyer ? R.string.trade_write_review_partner_seller
                : R.string.trade_write_review_partner_buyer);
        ImageLoader.load(deal.getItemThumb(), binding.imageItemThumb, R.drawable.ic_inbox, R.drawable.ic_error);
        dealViewModel.loadMyReview();
        refreshSubmitState();
    }

    private void renderSubmittedReview(Review review) {
        alreadyReviewed = true;
        binding.cardAlreadyReviewed.setVisibility(View.VISIBLE);
        showReadOnlyRating(binding.ratingOverall, review.getRating());
        showReadOnlyRating(binding.ratingAccuracy, review.getAccuracyRating());
        showReadOnlyRating(binding.ratingAttitude, review.getAttitudeRating());
        showReadOnlyRating(binding.ratingCommitment, review.getCommitmentRating());
        binding.layoutComment.setVisibility(View.GONE);
        String comment = review.getComment();
        boolean hasComment = comment != null && !comment.trim().isEmpty();
        binding.textSubmittedComment.setVisibility(hasComment ? View.VISIBLE : View.GONE);
        binding.textSubmittedComment.setText(hasComment ? comment : "");
        binding.textSubmitHint.setVisibility(View.GONE);
        binding.buttonSubmitReview.setVisibility(View.GONE);
    }

    private void showReadOnlyRating(ViewRatingInputBinding row, int stars) {
        row.ratingStars.setIsIndicator(true);
        row.ratingStars.setRating(stars);
        showRatingValue(row);
    }

    private void showRatingValue(ViewRatingInputBinding row) {
        int stars = starsOf(row);
        row.textRatingValue.setVisibility(stars > 0 ? View.VISIBLE : View.INVISIBLE);
        row.textRatingValue.setText(getString(R.string.trade_write_review_rating_value, stars));
    }

    private void showDealError(String message) {
        if (message == null || message.isEmpty()) {
            return;
        }
        if (dealViewModel.getDeal().getValue() == null) {
            showErrorState(message);
        } else {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        }
        dealViewModel.clearError();
    }

    private void showErrorState(String message) {
        binding.scrollContent.setVisibility(View.GONE);
        binding.includeErrorState.layoutErrorState.setVisibility(View.VISIBLE);
        binding.includeErrorState.textErrorMessage.setText(message);
    }

    private void refreshProgress() {
        boolean dealLoading = Boolean.TRUE.equals(dealViewModel.getLoading().getValue());
        boolean submitting = Boolean.TRUE.equals(trustViewModel.getLoading().getValue());
        binding.progressLoading.setVisibility(dealLoading || submitting ? View.VISIBLE : View.GONE);
    }

    private void refreshSubmitState() {
        if (binding == null || alreadyReviewed) {
            return;
        }
        boolean online = NetworkMonitor.getInstance().isOnline();
        boolean submitting = Boolean.TRUE.equals(trustViewModel.getLoading().getValue());
        boolean complete = hasAllRatings();
        binding.buttonSubmitReview.setEnabled(online && complete && !submitting);
        binding.includeOfflineBanner.layoutOfflineBanner.setVisibility(online ? View.GONE : View.VISIBLE);
        if (!online) {
            binding.textSubmitHint.setText(R.string.trade_write_review_offline);
        } else {
            binding.textSubmitHint.setText(R.string.trade_write_review_submit_hint);
        }
        binding.textSubmitHint.setVisibility(!online || !complete ? View.VISIBLE : View.GONE);
    }

    private boolean hasAllRatings() {
        return starsOf(binding.ratingOverall) > 0
                && starsOf(binding.ratingAccuracy) > 0
                && starsOf(binding.ratingAttitude) > 0
                && starsOf(binding.ratingCommitment) > 0;
    }

    private void submitReview() {
        if (!NetworkMonitor.getInstance().isOnline()) {
            refreshSubmitState();
            return;
        }
        Review review = new Review();
        review.setDealId(dealId);
        review.setRating(starsOf(binding.ratingOverall));
        review.setAccuracyRating(starsOf(binding.ratingAccuracy));
        review.setAttitudeRating(starsOf(binding.ratingAttitude));
        review.setCommitmentRating(starsOf(binding.ratingCommitment));
        review.setComment(commentText());
        trustViewModel.submitReview(review);
    }

    private String commentText() {
        if (binding.editComment.getText() == null) {
            return "";
        }
        return binding.editComment.getText().toString().trim();
    }

    private int starsOf(ViewRatingInputBinding row) {
        return Math.round(row.ratingStars.getRating());
    }

    private String currentUserId() {
        String uid = SessionManager.getInstance().getCurrentUserId();
        return uid != null ? uid : "";
    }

    private void restoreRatings(@NonNull Bundle state) {
        binding.ratingOverall.ratingStars.setRating(state.getInt(KEY_RATING_OVERALL, 0));
        binding.ratingAccuracy.ratingStars.setRating(state.getInt(KEY_RATING_ACCURACY, 0));
        binding.ratingAttitude.ratingStars.setRating(state.getInt(KEY_RATING_ATTITUDE, 0));
        binding.ratingCommitment.ratingStars.setRating(state.getInt(KEY_RATING_COMMITMENT, 0));
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (binding == null) {
            return;
        }
        outState.putInt(KEY_RATING_OVERALL, starsOf(binding.ratingOverall));
        outState.putInt(KEY_RATING_ACCURACY, starsOf(binding.ratingAccuracy));
        outState.putInt(KEY_RATING_ATTITUDE, starsOf(binding.ratingAttitude));
        outState.putInt(KEY_RATING_COMMITMENT, starsOf(binding.ratingCommitment));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Views are gone after this point, so drop the reference to avoid a leak.
        binding = null;
    }
}
