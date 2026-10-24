package com.nhom3.pubgapp.ui.listing;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.databinding.FragmentPostListingBinding;
import com.nhom3.pubgapp.model.Category;
import com.nhom3.pubgapp.model.Listing;
import com.nhom3.pubgapp.model.User;
import com.nhom3.pubgapp.model.enums.ItemCondition;
import com.nhom3.pubgapp.model.enums.ListingStatus;
import com.nhom3.pubgapp.model.enums.OfferType;
import com.nhom3.pubgapp.model.enums.SortOrder;
import com.nhom3.pubgapp.model.enums.TradeType;
import com.nhom3.pubgapp.model.enums.VerificationStatus;
import com.nhom3.pubgapp.util.NetworkMonitor;
import com.nhom3.pubgapp.util.SessionManager;
import com.nhom3.pubgapp.viewmodel.ProfileViewModel;
import com.nhom3.pubgapp.viewmodel.ShopViewModel;

import java.util.ArrayList;
import java.util.List;

/** Screen B04: post a new listing with images, details, and seller verification check. */
public class PostListingFragment extends Fragment {

    private static final String KEY_IMAGES = "key_post_images";
    private static final String KEY_EXISTING_IMAGE_IDS = "key_existing_image_ids";
    private static final String KEY_EDITING_ID = "key_editing_id";
    private static final String KEY_EDITING_SELLER_ID = "key_editing_seller_id";
    private static final String KEY_EDITING_STATUS = "key_editing_status";
    private static final String KEY_EDITING_COVER_THUMB = "key_editing_cover_thumb";
    private static final String KEY_EDITING_FLAGGED = "key_editing_flagged";
    private static final String KEY_EDITING_CREATED_AT = "key_editing_created_at";
    private static final String ARG_LISTING_ID = "listingId";

    private FragmentPostListingBinding binding;
    private ShopViewModel viewModel;
    private ProfileViewModel profileViewModel;
    private PostImageAdapter imageAdapter;
    private ActivityResultLauncher<PickVisualMediaRequest> photoPickerLauncher;

    private final List<Category> loadedCategories = new ArrayList<Category>();
    private VerificationStatus currentVerification = VerificationStatus.NONE;
    private boolean isSavingDraft = false;
    private String editingListingId;
    private Listing editingListing;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        photoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.PickMultipleVisualMedia(5),
                new ActivityResultCallback<List<Uri>>() {
                    @Override
                    public void onActivityResult(List<Uri> uris) {
                        handlePickedUris(uris);
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPostListingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(ShopViewModel.class);
        profileViewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);

        setupImageRecycler();
        setupProvincesSpinner();
        setupFormListeners();
        observeViewModel();
        checkSellerVerification();
        updateOfflineBanner();

        binding.layoutErrorState.buttonRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.layoutErrorState.getRoot().setVisibility(View.GONE);
                viewModel.clearError();
                if (loadedCategories.isEmpty()) {
                    viewModel.loadCategories();
                } else if (isSavingDraft) {
                    attemptSaveDraft();
                } else {
                    attemptPublish();
                }
            }
        });

        if (getArguments() != null) {
            editingListingId = getArguments().getString(ARG_LISTING_ID);
        }
        if (isEditMode()) {
            binding.buttonPost.setText(R.string.listing_post_edit_button_save);
            binding.buttonSaveDraft.setVisibility(View.GONE);
            if (savedInstanceState == null) {
                loadEditingListing();
            }
        }

        if (savedInstanceState != null) {
            restoreInstanceState(savedInstanceState);
        }

        viewModel.loadCategories();
    }

    private void setupImageRecycler() {
        imageAdapter = new PostImageAdapter(new PostImageAdapter.OnRemoveImageListener() {
            @Override
            public void onRemoveImage(int position) {
                if (position >= 0 && position < imageAdapter.getAllImageCount()) {
                    imageAdapter.removeImage(position);
                    binding.buttonAddImage.setVisibility(View.VISIBLE);
                }
            }
        });
        binding.recyclerImages.setAdapter(imageAdapter);

        binding.buttonAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageAdapter.getAllImageCount() >= 5) {
                    Toast.makeText(requireContext(),
                            R.string.listing_post_max_images_reached, Toast.LENGTH_SHORT).show();
                    return;
                }
                PickVisualMediaRequest request = new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build();
                photoPickerLauncher.launch(request);
            }
        });
    }

    private void handlePickedUris(List<Uri> uris) {
        if (uris == null || uris.isEmpty()) {
            return;
        }
        int availableSlots = 5 - imageAdapter.getAllImageCount();
        if (uris.size() > availableSlots) {
            Toast.makeText(requireContext(),
                    R.string.listing_post_max_images_reached, Toast.LENGTH_SHORT).show();
        }
        for (Uri uri : uris) {
            if (imageAdapter.getAllImageCount() < 5) {
                imageAdapter.addImageUri(uri);
            }
        }
        if (imageAdapter.getAllImageCount() >= 5) {
            binding.buttonAddImage.setVisibility(View.GONE);
        }
    }

    private void setupProvincesSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.listing_provinces,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerProvince.setAdapter(adapter);
    }

    private void setupFormListeners() {
        binding.checkTradeSell.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked && binding.checkTradeGiveaway.isChecked()) {
                    binding.checkTradeGiveaway.setChecked(false);
                }
                updateTradeVisibility();
            }
        });

        binding.checkTradeExchange.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked && binding.checkTradeGiveaway.isChecked()) {
                    binding.checkTradeGiveaway.setChecked(false);
                }
                updateTradeVisibility();
            }
        });

        binding.checkTradeGiveaway.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    binding.checkTradeSell.setChecked(false);
                    binding.checkTradeExchange.setChecked(false);
                    binding.editPrice.setText("0");
                }
                updateTradeVisibility();
            }
        });

        binding.buttonPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptPublish();
            }
        });

        binding.buttonSaveDraft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptSaveDraft();
            }
        });
    }

    private void checkSellerVerification() {
        String uid = SessionManager.getInstance().getCurrentUserId();
        if (uid != null && !uid.isEmpty()) {
            profileViewModel.loadProfile(uid);
        }
        profileViewModel.getProfile().observe(getViewLifecycleOwner(), new Observer<User>() {
            @Override
            public void onChanged(User user) {
                if (user != null && user.getVerificationStatus() != null) {
                    currentVerification = user.getVerificationStatus();
                    boolean approved = currentVerification == VerificationStatus.APPROVED;
                    binding.textVerificationWarning.setVisibility(approved ? View.GONE : View.VISIBLE);
                }
            }
        });
    }

    private void observeViewModel() {
        viewModel.getCategories().observe(getViewLifecycleOwner(), new Observer<List<Category>>() {
            @Override
            public void onChanged(List<Category> categories) {
                loadedCategories.clear();
                List<String> names = new ArrayList<>();
                if (categories != null) {
                    for (Category cat : categories) {
                        if (cat != null && cat.isActive()) {
                            loadedCategories.add(cat);
                            names.add(cat.getName());
                        }
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        names
                );
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                binding.spinnerCategory.setAdapter(adapter);
                if (isEditMode() && editingListing != null) {
                    selectCategory(editingListing.getCategoryId());
                }
            }
        });

        viewModel.getLoading().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isLoading) {
                boolean loading = isLoading != null && isLoading;
                binding.progressLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
                boolean canInteract = !loading && isNetworkAvailable();
                binding.buttonPost.setEnabled(canInteract);
                binding.buttonSaveDraft.setEnabled(canInteract);
            }
        });

        viewModel.getError().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String err) {
                if (err != null && !err.isEmpty()) {
                    binding.layoutErrorState.getRoot().setVisibility(View.VISIBLE);
                    binding.layoutErrorState.textErrorMessage.setText(err);
                    Snackbar.make(binding.getRoot(), err, Snackbar.LENGTH_LONG).show();
                    binding.nestedScrollView.smoothScrollTo(0, binding.layoutErrorState.getRoot().getTop());
                }
            }
        });

        viewModel.getKeywordFlag().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean flagged) {
                if (flagged != null && flagged) {
                    Toast.makeText(requireContext(),
                            R.string.listing_post_banned_keyword_warning,
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        viewModel.getCreatedListingId().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String listingId) {
                if (listingId != null && !listingId.isEmpty()) {
                    viewModel.clearDone();
                    if (isSavingDraft) {
                        Snackbar.make(binding.getRoot(),
                                R.string.listing_post_draft_success,
                                Snackbar.LENGTH_SHORT).show();
                        clearForm();
                        NavHostFragment.findNavController(PostListingFragment.this)
                                .navigate(R.id.action_global_my_shop);
                    } else {
                        Snackbar.make(binding.getRoot(),
                                R.string.listing_post_success,
                                Snackbar.LENGTH_SHORT).show();
                        clearForm();
                        Bundle args = new Bundle();
                        args.putString("listingId", listingId);
                        NavHostFragment.findNavController(PostListingFragment.this)
                                .navigate(R.id.action_global_listing_detail, args);
                    }
                }
            }
        });

        viewModel.getDone().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isDone) {
                if (isDone != null && isDone && isEditMode()) {
                    Toast.makeText(requireContext(), R.string.listing_post_edit_success,
                            Toast.LENGTH_SHORT).show();
                    viewModel.clearDone();
                    NavHostFragment.findNavController(PostListingFragment.this).popBackStack();
                }
            }
        });
    }

    private void attemptPublish() {
        if (!isNetworkAvailable()) {
            updateOfflineBanner();
            return;
        }
        binding.layoutOfflineBanner.getRoot().setVisibility(View.GONE);
        binding.layoutErrorState.getRoot().setVisibility(View.GONE);
        viewModel.clearError();

        binding.layoutTitle.setError(null);
        binding.layoutPrice.setError(null);

        String title = binding.editTitle.getText() != null
                ? binding.editTitle.getText().toString().trim() : "";
        if (title.isEmpty()) {
            binding.layoutTitle.setError(getString(R.string.listing_post_missing_fields));
            binding.nestedScrollView.smoothScrollTo(0, binding.layoutTitle.getTop());
            return;
        }

        if (imageAdapter.getAllImageCount() < 1) {
            Toast.makeText(requireContext(),
                    R.string.listing_post_missing_fields, Toast.LENGTH_SHORT).show();
            binding.nestedScrollView.smoothScrollTo(0, binding.textImagesLabel.getTop());
            return;
        }

        if (loadedCategories.isEmpty() || binding.spinnerCategory.getSelectedItemPosition() < 0) {
            Toast.makeText(requireContext(),
                    R.string.listing_post_missing_fields, Toast.LENGTH_SHORT).show();
            binding.nestedScrollView.smoothScrollTo(0, binding.textCategoryLabel.getTop());
            return;
        }

        if (getSelectedCondition() == null) {
            Toast.makeText(requireContext(),
                    R.string.listing_post_missing_fields, Toast.LENGTH_SHORT).show();
            binding.nestedScrollView.smoothScrollTo(0, binding.textConditionLabel.getTop());
            return;
        }

        if (!binding.checkTradeSell.isChecked()
                && !binding.checkTradeExchange.isChecked()
                && !binding.checkTradeGiveaway.isChecked()) {
            Toast.makeText(requireContext(),
                    R.string.listing_post_missing_fields, Toast.LENGTH_SHORT).show();
            binding.nestedScrollView.smoothScrollTo(0, binding.textTradeTypeLabel.getTop());
            return;
        }

        long typedPrice = 0L;
        String priceStr = binding.editPrice.getText() != null
                ? binding.editPrice.getText().toString().trim() : "";
        if (!priceStr.isEmpty()) {
            try {
                typedPrice = Long.parseLong(priceStr);
            } catch (NumberFormatException ignored) {
                typedPrice = 0L;
            }
        }
        if (binding.checkTradeSell.isChecked() && typedPrice <= 0L) {
            binding.layoutPrice.setError(getString(R.string.listing_post_price_error));
            binding.nestedScrollView.smoothScrollTo(0, binding.layoutPrice.getTop());
            return;
        }

        if (!isEditMode() && currentVerification != VerificationStatus.APPROVED) {
            showUnverifiedDialog();
            return;
        }

        isSavingDraft = false;
        Listing listing = buildListingFromInputs();
        if (isEditMode()) {
            listing.setId(editingListingId);
            if (editingListing != null) {
                listing.setSellerId(editingListing.getSellerId());
                listing.setStatus(editingListing.getStatus());
                listing.setCreatedAt(editingListing.getCreatedAt());
                listing.setCoverThumb(editingListing.getCoverThumb());
                listing.setFlagged(editingListing.isFlagged());
            }
            listing.setImageIds(imageAdapter.getExistingImageIds());
            viewModel.updateListing(listing, imageAdapter.getNewImageUris());
        } else {
            viewModel.createListing(listing, imageAdapter.getNewImageUris());
        }
    }

    private void attemptSaveDraft() {
        if (!isNetworkAvailable()) {
            updateOfflineBanner();
            return;
        }
        binding.layoutOfflineBanner.getRoot().setVisibility(View.GONE);
        binding.layoutErrorState.getRoot().setVisibility(View.GONE);
        viewModel.clearError();

        binding.layoutTitle.setError(null);
        binding.layoutPrice.setError(null);

        isSavingDraft = true;
        Listing listing = buildListingFromInputs();
        viewModel.saveDraft(listing, imageAdapter.getNewImageUris());
    }

    private void clearForm() {
        binding.editTitle.setText("");
        binding.editDescription.setText("");
        binding.editPrice.setText("");
        binding.editMinOfferPrice.setText("");
        binding.layoutTitle.setError(null);
        binding.layoutPrice.setError(null);
        binding.radioGroupCondition.clearCheck();
        binding.radioGroupOffer.clearCheck();
        binding.checkTradeSell.setChecked(false);
        binding.checkTradeExchange.setChecked(false);
        binding.checkTradeGiveaway.setChecked(false);
        imageAdapter.setInitialImages(new ArrayList<String>(), new ArrayList<Uri>());
        binding.buttonAddImage.setVisibility(View.VISIBLE);
        updateTradeVisibility();
    }

    private void updateTradeVisibility() {
        boolean isSell = binding.checkTradeSell.isChecked();
        boolean isGiveaway = binding.checkTradeGiveaway.isChecked();

        binding.layoutPrice.setVisibility(isSell ? View.VISIBLE : View.GONE);

        int offerVisibility = isGiveaway ? View.GONE : View.VISIBLE;
        binding.textOfferLabel.setVisibility(offerVisibility);
        binding.radioGroupOffer.setVisibility(offerVisibility);
        binding.layoutMinOfferPrice.setVisibility(offerVisibility);
    }

    private boolean isNetworkAvailable() {
        try {
            return NetworkMonitor.getInstance().isOnline();
        } catch (UnsupportedOperationException e) {
            return true;
        }
    }

    private void updateOfflineBanner() {
        boolean online = isNetworkAvailable();
        binding.layoutOfflineBanner.getRoot().setVisibility(online ? View.GONE : View.VISIBLE);
        binding.buttonPost.setEnabled(online);
        binding.buttonSaveDraft.setEnabled(online);
    }

    private Listing buildListingFromInputs() {
        Listing listing = new Listing();
        listing.setTitle(binding.editTitle.getText() != null
                ? binding.editTitle.getText().toString().trim() : "");
        listing.setDescription(binding.editDescription.getText() != null
                ? binding.editDescription.getText().toString().trim() : "");

        int catPos = binding.spinnerCategory.getSelectedItemPosition();
        if (catPos >= 0 && catPos < loadedCategories.size()) {
            listing.setCategoryId(loadedCategories.get(catPos).getId());
        }

        listing.setItemCondition(getSelectedCondition());

        List<TradeType> tradeTypes = new ArrayList<TradeType>();
        if (binding.checkTradeSell.isChecked()) {
            tradeTypes.add(TradeType.SELL);
        }
        if (binding.checkTradeExchange.isChecked()) {
            tradeTypes.add(TradeType.EXCHANGE);
        }
        if (binding.checkTradeGiveaway.isChecked()) {
            tradeTypes.add(TradeType.GIVEAWAY);
        }
        listing.setTradeTypes(tradeTypes);

        long typedPrice = 0L;
        String priceStr = binding.editPrice.getText() != null
                ? binding.editPrice.getText().toString().trim() : "";
        if (!priceStr.isEmpty()) {
            try {
                typedPrice = Long.parseLong(priceStr);
            } catch (NumberFormatException ignored) {
                typedPrice = 0L;
            }
        }
        listing.setPrice(typedPrice);

        List<OfferType> offerTypes = new ArrayList<>();
        int offerId = binding.radioGroupOffer.getCheckedRadioButtonId();
        if (offerId == R.id.radioOfferMoney) {
            offerTypes.add(OfferType.MONEY);
        } else if (offerId == R.id.radioOfferItem) {
            offerTypes.add(OfferType.ITEM);
        } else if (offerId == R.id.radioOfferBoth) {
            offerTypes.add(OfferType.MONEY_AND_ITEM);
        }
        listing.setAcceptedOfferTypes(offerTypes);

        long minOffer = 0L;
        String minOfferStr = binding.editMinOfferPrice.getText() != null
                ? binding.editMinOfferPrice.getText().toString().trim() : "";
        if (!minOfferStr.isEmpty()) {
            try {
                minOffer = Long.parseLong(minOfferStr);
            } catch (NumberFormatException ignored) {
                minOffer = 0L;
            }
        }
        listing.setMinOfferPrice(minOffer);

        Object selectedProvince = binding.spinnerProvince.getSelectedItem();
        if (selectedProvince != null) {
            listing.setProvince(selectedProvince.toString());
        }

        return listing;
    }

    private ItemCondition getSelectedCondition() {
        int checkedId = binding.radioGroupCondition.getCheckedRadioButtonId();
        if (checkedId == R.id.radioConditionNew) {
            return ItemCondition.NEW;
        } else if (checkedId == R.id.radioConditionLikeNew) {
            return ItemCondition.LIKE_NEW;
        } else if (checkedId == R.id.radioConditionUsed) {
            return ItemCondition.USED;
        } else if (checkedId == R.id.radioConditionRepaired) {
            return ItemCondition.REPAIRED;
        } else if (checkedId == R.id.radioConditionDefective) {
            return ItemCondition.DEFECTIVE;
        }
        return null;
    }

    private void showUnverifiedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(R.string.listing_post_unverified_dialog_title);

        if (currentVerification == VerificationStatus.PENDING) {
            builder.setMessage(R.string.listing_post_pending_dialog_msg);
            builder.setPositiveButton(R.string.listing_post_btn_close, null);
        } else {
            builder.setMessage(R.string.listing_post_unverified_dialog_msg);
            builder.setPositiveButton(R.string.listing_post_btn_verify, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    NavHostFragment.findNavController(PostListingFragment.this)
                            .navigate(R.id.action_global_my_profile);
                }
            });
            builder.setNegativeButton(R.string.listing_post_btn_close, null);
        }
        builder.create().show();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        List<Uri> newUris = imageAdapter.getNewImageUris();
        ArrayList<Parcelable> parcelables = new ArrayList<Parcelable>(newUris);
        outState.putParcelableArrayList(KEY_IMAGES, parcelables);
        outState.putStringArrayList(KEY_EXISTING_IMAGE_IDS,
                new ArrayList<String>(imageAdapter.getExistingImageIds()));
        outState.putString(KEY_EDITING_ID, editingListingId);
        if (editingListing != null) {
            outState.putString(KEY_EDITING_SELLER_ID, editingListing.getSellerId());
            if (editingListing.getStatus() != null) {
                outState.putString(KEY_EDITING_STATUS, editingListing.getStatus().name());
            }
            outState.putString(KEY_EDITING_COVER_THUMB, editingListing.getCoverThumb());
            outState.putBoolean(KEY_EDITING_FLAGGED, editingListing.isFlagged());
            if (editingListing.getCreatedAt() != null) {
                outState.putLong(KEY_EDITING_CREATED_AT, editingListing.getCreatedAt().getSeconds());
            }
        }
    }

    private void restoreInstanceState(Bundle state) {
        editingListingId = state.getString(KEY_EDITING_ID);
        ArrayList<String> existingIds = state.getStringArrayList(KEY_EXISTING_IMAGE_IDS);
        ArrayList<Parcelable> parcelables = state.getParcelableArrayList(KEY_IMAGES);
        List<Uri> restoredUris = new ArrayList<Uri>();
        if (parcelables != null) {
            for (Parcelable p : parcelables) {
                if (p instanceof Uri) {
                    restoredUris.add((Uri) p);
                }
            }
        }
        imageAdapter.setInitialImages(existingIds, restoredUris);
        if (imageAdapter.getAllImageCount() >= 5) {
            binding.buttonAddImage.setVisibility(View.GONE);
        }
        if (editingListingId != null && !editingListingId.isEmpty()) {
            if (state.containsKey(KEY_EDITING_SELLER_ID)) {
                editingListing = new Listing();
                editingListing.setId(editingListingId);
                editingListing.setSellerId(state.getString(KEY_EDITING_SELLER_ID));
                String statusStr = state.getString(KEY_EDITING_STATUS);
                if (statusStr != null) {
                    try {
                        editingListing.setStatus(ListingStatus.valueOf(statusStr));
                    } catch (IllegalArgumentException ignored) {
                    }
                }
                editingListing.setCoverThumb(state.getString(KEY_EDITING_COVER_THUMB));
                editingListing.setFlagged(state.getBoolean(KEY_EDITING_FLAGGED, false));
                if (state.containsKey(KEY_EDITING_CREATED_AT)) {
                    long seconds = state.getLong(KEY_EDITING_CREATED_AT);
                    editingListing.setCreatedAt(new Timestamp(seconds, 0));
                }
            }
        }
    }

    private boolean isEditMode() {
        return editingListingId != null && !editingListingId.isEmpty();
    }

    private void loadEditingListing() {
        if (editingListingId == null || editingListingId.isEmpty()) {
            return;
        }
        viewModel.getMyListings().observe(getViewLifecycleOwner(), new Observer<List<Listing>>() {
            @Override
            public void onChanged(List<Listing> listings) {
                if (listings == null || listings.isEmpty() || editingListing != null) {
                    return;
                }
                for (Listing item : listings) {
                    if (item != null && editingListingId.equals(item.getId())) {
                        editingListing = item;
                        populateForm(item);
                        break;
                    }
                }
            }
        });
        List<Listing> current = viewModel.getMyListings().getValue();
        if (current == null || current.isEmpty()) {
            viewModel.loadMyListings(SortOrder.NEWEST, "");
        } else {
            for (Listing item : current) {
                if (item != null && editingListingId.equals(item.getId())) {
                    editingListing = item;
                    populateForm(item);
                    break;
                }
            }
        }
    }

    private void populateForm(Listing listing) {
        if (listing == null) {
            return;
        }
        binding.editTitle.setText(listing.getTitle());
        binding.editDescription.setText(listing.getDescription());
        if (listing.getPrice() > 0) {
            binding.editPrice.setText(String.valueOf(listing.getPrice()));
        }
        if (listing.getMinOfferPrice() > 0) {
            binding.editMinOfferPrice.setText(String.valueOf(listing.getMinOfferPrice()));
        }

        List<TradeType> tradeTypes = listing.getTradeTypes();
        if (tradeTypes != null) {
            binding.checkTradeSell.setChecked(tradeTypes.contains(TradeType.SELL));
            binding.checkTradeExchange.setChecked(tradeTypes.contains(TradeType.EXCHANGE));
            binding.checkTradeGiveaway.setChecked(tradeTypes.contains(TradeType.GIVEAWAY));
        }

        ItemCondition cond = listing.getItemCondition();
        if (cond == ItemCondition.NEW) {
            binding.radioGroupCondition.check(R.id.radioConditionNew);
        } else if (cond == ItemCondition.LIKE_NEW) {
            binding.radioGroupCondition.check(R.id.radioConditionLikeNew);
        } else if (cond == ItemCondition.USED) {
            binding.radioGroupCondition.check(R.id.radioConditionUsed);
        } else if (cond == ItemCondition.REPAIRED) {
            binding.radioGroupCondition.check(R.id.radioConditionRepaired);
        } else if (cond == ItemCondition.DEFECTIVE) {
            binding.radioGroupCondition.check(R.id.radioConditionDefective);
        }

        List<OfferType> offers = listing.getAcceptedOfferTypes();
        if (offers != null) {
            if (offers.contains(OfferType.MONEY_AND_ITEM)) {
                binding.radioGroupOffer.check(R.id.radioOfferBoth);
            } else if (offers.contains(OfferType.ITEM)) {
                binding.radioGroupOffer.check(R.id.radioOfferItem);
            } else if (offers.contains(OfferType.MONEY)) {
                binding.radioGroupOffer.check(R.id.radioOfferMoney);
            }
        }

        selectCategory(listing.getCategoryId());
        selectProvince(listing.getProvince());

        imageAdapter.setInitialImages(listing.getImageIds(), null);
        if (imageAdapter.getAllImageCount() >= 5) {
            binding.buttonAddImage.setVisibility(View.GONE);
        }
    }

    private void selectCategory(String categoryId) {
        if (categoryId == null || loadedCategories.isEmpty()) {
            return;
        }
        for (int i = 0; i < loadedCategories.size(); i++) {
            Category cat = loadedCategories.get(i);
            if (cat != null && categoryId.equals(cat.getId())) {
                binding.spinnerCategory.setSelection(i);
                break;
            }
        }
    }

    private void selectProvince(String province) {
        if (province == null || province.isEmpty()) {
            return;
        }
        String[] provinces = getResources().getStringArray(R.array.listing_provinces);
        for (int i = 0; i < provinces.length; i++) {
            if (province.equals(provinces[i])) {
                binding.spinnerProvince.setSelection(i);
                break;
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
