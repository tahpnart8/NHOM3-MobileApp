package com.nhom3.pubgapp.ui.trust;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.databinding.ItemTrustReviewBinding;
import com.nhom3.pubgapp.model.Review;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Rows of reviews on D02 (received and given) and D03 (recent). */
public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private final List<Review> reviews = new ArrayList<Review>();

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
        String name = context.getString(R.string.profile_review_item_reviewer);
        holder.bind(review, name);
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    static final class ReviewViewHolder extends RecyclerView.ViewHolder {

        private final ItemTrustReviewBinding binding;

        ReviewViewHolder(ItemTrustReviewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Review review, String name) {
            binding.textReviewerName.setText(name);
            binding.includeAvatar.textInitial.setText(name.substring(0, 1).toUpperCase(Locale.ROOT));
            binding.includeAvatar.textInitial.setVisibility(View.VISIBLE);
            binding.includeAvatar.imageAvatar.setVisibility(View.GONE);
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
