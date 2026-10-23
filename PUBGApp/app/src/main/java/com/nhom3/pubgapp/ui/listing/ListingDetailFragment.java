package com.nhom3.pubgapp.ui.listing;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.databinding.FragmentListingDetailBinding;
import com.nhom3.pubgapp.model.Listing;
import com.nhom3.pubgapp.model.User;
import com.nhom3.pubgapp.model.enums.ItemCondition;
import com.nhom3.pubgapp.model.enums.ListingStatus;
import com.nhom3.pubgapp.model.enums.TradeType;
import com.nhom3.pubgapp.model.enums.VerificationStatus;
import com.nhom3.pubgapp.util.FormatUtils;
import com.nhom3.pubgapp.util.NetworkMonitor;
import com.nhom3.pubgapp.util.SessionManager;
import com.nhom3.pubgapp.viewmodel.ListingDetailViewModel;
import com.nhom3.pubgapp.viewmodel.ShopViewModel;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Screen B03: Displays full listing details, seller info, and interaction buttons. */
public class ListingDetailFragment extends Fragment {

    private static final String ARG_LISTING_ID = "listingId";

    private FragmentListingDetailBinding binding;
    private ListingDetailViewModel viewModel;
    private ShopViewModel shopViewModel;
    private ListingImagePagerAdapter imageAdapter;
    private String listingId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentListingDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ListingDetailViewModel.class);
        shopViewModel = new ViewModelProvider(requireActivity()).get(ShopViewModel.class);

        if (getArguments() != null) {
            listingId = getArguments().getString(ARG_LISTING_ID);
        }

        setupToolbar();
        setupCarousel();
        setupErrorAndEmptyStates();
        observeViewModel();

        checkNetworkBanner();

        // Load only on initial creation; ViewModel retains data across rotation
        if (viewModel.getListing().getValue() == null && listingId != null) {
            viewModel.load(listingId);
        }
    }

    private void setupToolbar() {
        binding.toolbarDetail.textTitle.setText(R.string.listing_detail_title);
        binding.toolbarDetail.buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigateUp();
            }
        });

        binding.buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!SessionManager.getInstance().isLoggedIn()) {
                    Navigation.findNavController(v).navigate(R.id.action_global_sign_in);
                    return;
                }
                if (!NetworkMonitor.getInstance().isOnline()) {
                    Snackbar.make(binding.getRoot(),
                            R.string.listing_detail_offline_save_warning,
                            Snackbar.LENGTH_SHORT).show();
                    return;
                }
                viewModel.toggleSave();
            }
        });

        binding.buttonGuestSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(R.id.action_global_sign_in);
            }
        });
    }

    private void setupCarousel() {
        imageAdapter = new ListingImagePagerAdapter();
        binding.recyclerImages.setAdapter(imageAdapter);
        PagerSnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(binding.recyclerImages);

        binding.recyclerImages.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    updateImageCounter();
                }
            }
        });
    }

    private void updateImageCounter() {
        LinearLayoutManager layoutManager = (LinearLayoutManager) binding.recyclerImages.getLayoutManager();
        if (layoutManager != null) {
            int position = layoutManager.findFirstVisibleItemPosition();
            int total = imageAdapter.getItemCount();
            if (position >= 0 && total > 0) {
                String counter = getString(R.string.listing_detail_image_count_format, position + 1, total);
                binding.textImageCount.setText(counter);
            }
        }
    }

    private void setupErrorAndEmptyStates() {
        binding.errorState.buttonRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.clearError();
                if (listingId != null) {
                    viewModel.load(listingId);
                }
            }
        });

        binding.emptyState.textEmptyTitle.setText(R.string.listing_detail_empty_title);
        binding.emptyState.textEmptyMessage.setText(R.string.listing_detail_empty_message);
    }

    private void checkNetworkBanner() {
        if (!NetworkMonitor.getInstance().isOnline()) {
            binding.offlineBanner.layoutOfflineBanner.setVisibility(View.VISIBLE);
        } else {
            binding.offlineBanner.layoutOfflineBanner.setVisibility(View.GONE);
        }
    }

    private void observeViewModel() {
        viewModel.getLoading().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isLoading) {
                if (Boolean.TRUE.equals(isLoading)) {
                    binding.progressLoading.setVisibility(View.VISIBLE);
                } else {
                    binding.progressLoading.setVisibility(View.GONE);
                }
            }
        });

        viewModel.getError().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String err) {
                if (err != null && !err.isEmpty()) {
                    binding.errorState.layoutErrorState.setVisibility(View.VISIBLE);
                    binding.errorState.textErrorMessage.setText(err);
                    binding.scrollContent.setVisibility(View.GONE);
                    binding.emptyState.layoutEmptyState.setVisibility(View.GONE);
                } else {
                    binding.errorState.layoutErrorState.setVisibility(View.GONE);
                }
            }
        });

        viewModel.getListing().observe(getViewLifecycleOwner(), new Observer<Listing>() {
            @Override
            public void onChanged(Listing listing) {
                String currentUid = SessionManager.getInstance().getCurrentUserId();
                if (!ListingDetailViewModel.canShowListing(listing, currentUid)) {
                    binding.emptyState.layoutEmptyState.setVisibility(View.VISIBLE);
                    binding.scrollContent.setVisibility(View.GONE);
                    binding.layoutBottomBar.setVisibility(View.GONE);
                    return;
                }

                binding.emptyState.layoutEmptyState.setVisibility(View.GONE);
                binding.scrollContent.setVisibility(View.VISIBLE);
                binding.layoutBottomBar.setVisibility(View.VISIBLE);

                displayListingDetails(listing, currentUid);
                viewModel.loadImages();
            }
        });

        viewModel.getImages().observe(getViewLifecycleOwner(), new Observer<List<byte[]>>() {
            @Override
            public void onChanged(List<byte[]> imageBytes) {
                if (imageBytes != null && !imageBytes.isEmpty()) {
                    imageAdapter.setImages(imageBytes);
                    updateImageCounter();
                }
            }
        });

        viewModel.getSeller().observe(getViewLifecycleOwner(), new Observer<User>() {
            @Override
            public void onChanged(User seller) {
                displaySeller(seller);
            }
        });

        viewModel.getSaved().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isSaved) {
                if (Boolean.TRUE.equals(isSaved)) {
                    binding.buttonSave.setImageResource(android.R.drawable.btn_star_big_on);
                } else {
                    binding.buttonSave.setImageResource(android.R.drawable.btn_star_big_off);
                }
            }
        });

        viewModel.getDone().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isDone) {
                if (Boolean.TRUE.equals(isDone)) {
                    boolean isSaved = Boolean.TRUE.equals(viewModel.getSaved().getValue());
                    int msgRes = isSaved
                            ? R.string.listing_detail_save_success
                            : R.string.listing_detail_unsave_success;
                    Snackbar.make(binding.getRoot(), msgRes, Snackbar.LENGTH_SHORT).show();
                    viewModel.clearDone();
                }
            }
        });

        shopViewModel.getError().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String err) {
                if (err != null && !err.isEmpty()) {
                    Snackbar.make(binding.getRoot(), err, Snackbar.LENGTH_SHORT).show();
                    shopViewModel.clearError();
                }
            }
        });
    }

    private void displayListingDetails(final Listing listing, String currentUid) {
        if (listing == null) {
            return;
        }

        imageAdapter.setCoverThumb(listing.getCoverThumb());
        updateImageCounter();

        bindStatusChip(listing.getStatus());

        if (listing.getStatus() == ListingStatus.SOLD) {
            binding.textSoldOverlay.setVisibility(View.VISIBLE);
        } else {
            binding.textSoldOverlay.setVisibility(View.GONE);
        }

        StringBuilder tradeBuilder = new StringBuilder();
        List<TradeType> types = listing.getTradeTypes();
        if (types != null) {
            for (int i = 0; i < types.size(); i++) {
                TradeType t = types.get(i);
                if (tradeBuilder.length() > 0) {
                    tradeBuilder.append(" • ");
                }
                if (t == TradeType.SELL) {
                    tradeBuilder.append(getString(R.string.listing_detail_trade_sell));
                } else if (t == TradeType.EXCHANGE) {
                    tradeBuilder.append(getString(R.string.listing_detail_trade_exchange));
                } else if (t == TradeType.GIVEAWAY) {
                    tradeBuilder.append(getString(R.string.listing_detail_trade_giveaway));
                }
            }
        }
        binding.textTradeTypes.setText(tradeBuilder.toString());

        if (listing.getItemCondition() != null) {
            binding.textCondition.setText(formatCondition(listing.getItemCondition()));
            binding.textCondition.setVisibility(View.VISIBLE);
        } else {
            binding.textCondition.setVisibility(View.GONE);
        }

        binding.textListingTitle.setText(listing.getTitle());

        FormatUtils formatUtils = new FormatUtils();
        if (listing.getPrice() <= 0) {
            binding.textPrice.setText(R.string.listing_detail_price_free);
        } else {
            binding.textPrice.setText(formatUtils.formatPrice(listing.getPrice()));
        }

        if (listing.getProvince() != null && !listing.getProvince().isEmpty()) {
            binding.textProvince.setText(listing.getProvince());
            binding.textProvince.setVisibility(View.VISIBLE);
        } else {
            binding.textProvince.setVisibility(View.GONE);
        }

        Map<String, String> conditionVals = listing.getConditionValues();
        if (conditionVals != null && !conditionVals.isEmpty()) {
            StringBuilder condBuilder = new StringBuilder();
            for (Map.Entry<String, String> entry : conditionVals.entrySet()) {
                if (condBuilder.length() > 0) {
                    condBuilder.append("\n");
                }
                condBuilder.append(entry.getKey()).append(": ").append(entry.getValue());
            }
            binding.textConditionDetails.setText(condBuilder.toString());
            binding.layoutConditionDetails.setVisibility(View.VISIBLE);
        } else {
            binding.layoutConditionDetails.setVisibility(View.GONE);
        }

        binding.textDescription.setText(listing.getDescription());

        setupActionButtons(listing, currentUid);
    }

    private void bindStatusChip(ListingStatus status) {
        if (status == null) {
            binding.chipStatus.textStatusChip.setVisibility(View.GONE);
            return;
        }
        binding.chipStatus.textStatusChip.setVisibility(View.VISIBLE);
        int variantColor = ContextCompat.getColor(requireContext(), R.color.on_surface_variant);
        if (status == ListingStatus.AVAILABLE) {
            binding.chipStatus.textStatusChip.setText(R.string.listing_detail_status_available);
            binding.chipStatus.textStatusChip.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.success));
        } else if (status == ListingStatus.RESERVED) {
            binding.chipStatus.textStatusChip.setText(R.string.listing_detail_status_reserved);
            binding.chipStatus.textStatusChip.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.warning));
        } else if (status == ListingStatus.SOLD) {
            binding.chipStatus.textStatusChip.setText(R.string.listing_detail_status_sold);
            binding.chipStatus.textStatusChip.setTextColor(variantColor);
        } else if (status == ListingStatus.HIDDEN) {
            binding.chipStatus.textStatusChip.setText(R.string.listing_detail_status_hidden);
            binding.chipStatus.textStatusChip.setTextColor(variantColor);
        } else if (status == ListingStatus.REMOVED) {
            binding.chipStatus.textStatusChip.setText(R.string.listing_detail_status_removed);
            binding.chipStatus.textStatusChip.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.error));
        } else if (status == ListingStatus.DRAFT) {
            binding.chipStatus.textStatusChip.setText(R.string.listing_detail_status_draft);
            binding.chipStatus.textStatusChip.setTextColor(variantColor);
        }
    }

    private String formatCondition(ItemCondition condition) {
        if (condition == ItemCondition.NEW) {
            return getString(R.string.listing_post_condition_new);
        } else if (condition == ItemCondition.LIKE_NEW) {
            return getString(R.string.listing_post_condition_like_new);
        } else if (condition == ItemCondition.USED) {
            return getString(R.string.listing_post_condition_used);
        } else if (condition == ItemCondition.REPAIRED) {
            return getString(R.string.listing_post_condition_repaired);
        } else if (condition == ItemCondition.DEFECTIVE) {
            return getString(R.string.listing_post_condition_defective);
        }
        return "";
    }

    private void displaySeller(User seller) {
        if (!SessionManager.getInstance().isLoggedIn()) {
            binding.layoutSellerBlock.setVisibility(View.GONE);
            binding.layoutGuestBlock.setVisibility(View.VISIBLE);
            return;
        }

        binding.layoutGuestBlock.setVisibility(View.GONE);
        binding.layoutSellerBlock.setVisibility(View.VISIBLE);

        if (seller == null) {
            return;
        }

        binding.textSellerName.setText(seller.getDisplayName());

        String initial = "";
        if (seller.getDisplayName() != null && !seller.getDisplayName().isEmpty()) {
            initial = seller.getDisplayName().substring(0, 1).toUpperCase(Locale.getDefault());
        }
        binding.avatarSeller.textInitial.setText(initial);

        if (seller.getAvatarBase64() != null && !seller.getAvatarBase64().isEmpty()) {
            try {
                byte[] bytes = Base64.decode(seller.getAvatarBase64(), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                binding.avatarSeller.imageAvatar.setImageBitmap(bitmap);
                binding.avatarSeller.imageAvatar.setVisibility(View.VISIBLE);
                binding.avatarSeller.textInitial.setVisibility(View.GONE);
            } catch (IllegalArgumentException e) {
                binding.avatarSeller.imageAvatar.setVisibility(View.GONE);
                binding.avatarSeller.textInitial.setVisibility(View.VISIBLE);
            }
        } else {
            binding.avatarSeller.imageAvatar.setVisibility(View.GONE);
            binding.avatarSeller.textInitial.setVisibility(View.VISIBLE);
        }

        float rating = (float) seller.ratingAverage();
        binding.ratingSeller.ratingStars.setRating(rating);
        binding.ratingSeller.textRatingValue.setText(String.format(Locale.getDefault(), "%.1f", rating));
        binding.ratingSeller.textRatingCount.setText("(" + seller.getRatingCount() + ")");

        if (seller.getVerificationStatus() == VerificationStatus.APPROVED) {
            binding.textVerifiedBadge.setVisibility(View.VISIBLE);
        } else {
            binding.textVerifiedBadge.setVisibility(View.GONE);
        }
    }

    private void setupActionButtons(final Listing listing, String currentUid) {
        boolean isOwner = currentUid != null && currentUid.equals(listing.getSellerId());

        if (isOwner) {
            binding.layoutBuyerActions.setVisibility(View.GONE);
            binding.layoutSellerActions.setVisibility(View.VISIBLE);

            binding.buttonEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putString("listingId", listing.getId());
                    Navigation.findNavController(v).navigate(R.id.postListingFragment, bundle);
                }
            });

            boolean canMarkSold = (listing.getStatus() == ListingStatus.AVAILABLE);
            binding.buttonMarkSold.setEnabled(canMarkSold);
            binding.buttonMarkSold.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    shopViewModel.changeStatus(listing.getId(), ListingStatus.SOLD);
                }
            });
        } else {
            binding.layoutSellerActions.setVisibility(View.GONE);
            binding.layoutBuyerActions.setVisibility(View.VISIBLE);

            boolean canChat = ListingDetailViewModel.canStartChat(listing, currentUid);
            boolean canOffer = ListingDetailViewModel.canSendOffer(listing, currentUid);

            binding.buttonChat.setEnabled(canChat);
            binding.buttonOffer.setEnabled(canOffer);

            binding.buttonChat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!SessionManager.getInstance().isLoggedIn()) {
                        Navigation.findNavController(v).navigate(R.id.action_global_sign_in);
                    }
                }
            });

            binding.buttonOffer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!SessionManager.getInstance().isLoggedIn()) {
                        Navigation.findNavController(v).navigate(R.id.action_global_sign_in);
                    }
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
