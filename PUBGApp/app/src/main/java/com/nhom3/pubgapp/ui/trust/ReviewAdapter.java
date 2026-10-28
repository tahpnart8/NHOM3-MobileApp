package com.nhom3.pubgapp.ui.trust;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.data.DataCallback;
import com.nhom3.pubgapp.data.UserRepository;
import com.nhom3.pubgapp.databinding.ItemTrustReviewBinding;
import com.nhom3.pubgapp.model.Review;
import com.nhom3.pubgapp.model.User;
import com.nhom3.pubgapp.ui.common.AvatarBinder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Rows of reviews on D02 (received and given) and D03 (recent). */
public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private static final String TAG = "ReviewAdapter";

    private final List<Review> reviews = new ArrayList<Review>();
    // users/{reviewerId}, for the name and avatar, filled as rows are bound.
    private final Map<String, User> reviewers = new HashMap<String, User>();
    private final Set<String> requestedIds = new HashSet<String>();
    private final UserRepository userRepository = new UserRepository();

    public void submitList(List<Review> newReviews) {
        reviews.clear();
        if (newReviews != null) {
            reviews.addAll(newReviews);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemTrustReviewBinding binding = ItemTrustReviewBinding.inflate(inflater, parent, false);
        return new ReviewViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviews.get(position);
        Context context = holder.itemView.getContext();
        String fallback = context.getString(R.string.profile_review_item_reviewer);
        String reviewerId = review.getReviewerId();
        User reviewer = reviewers.get(reviewerId);
        String displayName = reviewer == null ? null : reviewer.getDisplayName();
        String avatarBase64 = reviewer == null ? null : reviewer.getAvatarBase64();
        holder.bind(review, resolveName(displayName, fallback), avatarBase64);
        loadReviewerName(reviewerId);
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    /** The reviewer's display name, or the neutral fallback while it is unknown or blank. */
    static String resolveName(@Nullable String displayName, String fallback) {
        if (displayName == null || displayName.trim().isEmpty()) {
            return fallback;
        }
        return displayName.trim();
    }

    // Each reviewer is read once per adapter; a failed read keeps the neutral name instead of retrying on every bind.
    private void loadReviewerName(@Nullable final String reviewerId) {
        if (reviewerId == null || reviewerId.isEmpty() || requestedIds.contains(reviewerId)) {
            return;
        }
        requestedIds.add(reviewerId);
        userRepository.getProfile(reviewerId, new DataCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (user == null) {
                    return;
                }
                reviewers.put(reviewerId, user);
                refreshRowsOf(reviewerId);
            }

            @Override
            public void onError(String message) {
                Log.w(TAG, "Reviewer name not loaded: " + message);
            }
        });
    }

    private void refreshRowsOf(String reviewerId) {
        for (int i = 0; i < reviews.size(); i++) {
            if (reviewerId.equals(reviews.get(i).getReviewerId())) {
                notifyItemChanged(i);
            }
        }
    }

    static final class ReviewViewHolder extends RecyclerView.ViewHolder {

        private final ItemTrustReviewBinding binding;

        ReviewViewHolder(ItemTrustReviewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Review review, String name, @Nullable String avatarBase64) {
            binding.textReviewerName.setText(name);
            AvatarBinder.bind(binding.includeAvatar, name, avatarBase64);
            binding.includeRating.ratingStars.setRating(review.getRating());
            binding.includeRating.textRatingValue.setText(String.valueOf(review.getRating()));
            binding.includeRating.textRatingCount.setVisibility(View.GONE);
            String comment = review.getComment();
            boolean hasComment = comment != null && !comment.trim().isEmpty();
            binding.textComment.setText(hasComment ? comment : "");
            binding.textComment.setVisibility(hasComment ? View.VISIBLE : View.GONE);
        }
    }
}
