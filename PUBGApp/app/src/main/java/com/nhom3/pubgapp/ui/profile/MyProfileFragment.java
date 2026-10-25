package com.nhom3.pubgapp.ui.profile;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.os.BundleCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.databinding.FragmentMyProfileBinding;
import com.nhom3.pubgapp.model.ProfileStats;
import com.nhom3.pubgapp.model.SellerVerification;
import com.nhom3.pubgapp.model.User;
import com.nhom3.pubgapp.model.enums.VerificationStatus;
import com.nhom3.pubgapp.ui.admin.AdminActivity;
import com.nhom3.pubgapp.util.NetworkMonitor;
import com.nhom3.pubgapp.util.SessionManager;
import com.nhom3.pubgapp.viewmodel.AuthViewModel;
import com.nhom3.pubgapp.viewmodel.ProfileViewModel;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** D01: the signed-in user's own profile, shortcuts, account verification and sign out. */
public class MyProfileFragment extends Fragment {

    private static final String ARG_USER_ID = "userId";
    private static final String STATE_FORM_OPEN = "verificationFormOpen";
    private static final String STATE_ID_IMAGE = "verificationIdImage";
    private static final String STATE_PORTRAIT_IMAGE = "verificationPortraitImage";
    private static final String IMAGE_TYPE_PREFIX = "image/";
    private static final int PREVIEW_SAMPLE_SIZE = 8;

    private FragmentMyProfileBinding binding;
    private ProfileViewModel profileViewModel;
    private AuthViewModel authViewModel;

    private ActivityResultLauncher<PickVisualMediaRequest> pickIdImage;
    private ActivityResultLauncher<PickVisualMediaRequest> pickPortraitImage;
    // The chosen images and the open form survive rotation through onSaveInstanceState (UC31 NFR3).
    private Uri idImageUri;
    private Uri portraitImageUri;
    private boolean formOpen;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pickIdImage = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri uri) {
                        onIdImagePicked(uri);
                    }
                });
        pickPortraitImage = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri uri) {
                        onPortraitImagePicked(uri);
                    }
                });
        if (savedInstanceState != null) {
            formOpen = savedInstanceState.getBoolean(STATE_FORM_OPEN);
            idImageUri = BundleCompat.getParcelable(savedInstanceState, STATE_ID_IMAGE, Uri.class);
            portraitImageUri = BundleCompat.getParcelable(savedInstanceState, STATE_PORTRAIT_IMAGE, Uri.class);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_FORM_OPEN, formOpen);
        outState.putParcelable(STATE_ID_IMAGE, idImageUri);
        outState.putParcelable(STATE_PORTRAIT_IMAGE, portraitImageUri);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentMyProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (!SessionManager.getInstance().isLoggedIn()) {
            NavHostFragment.findNavController(this).navigate(R.id.action_global_sign_in);
            return;
        }
        // Activity scope, so A05 edits the same profile this screen shows.
        profileViewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        setUpClicks();
        observeProfile();
        observeLogout();
        profileViewModel.loadProfile(SessionManager.getInstance().getCurrentUserId());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (binding == null) {
            return;
        }
        boolean online = NetworkMonitor.getInstance().isOnline();
        binding.includeOfflineBanner.getRoot().setVisibility(online ? View.GONE : View.VISIBLE);
        if (profileViewModel != null) {
            updateSubmitEnabled();
        }
    }

    private void setUpClicks() {
        binding.includeErrorState.buttonRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View clicked) {
                profileViewModel.loadProfile(SessionManager.getInstance().getCurrentUserId());
            }
        });
        binding.buttonRetryStats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View clicked) {
                profileViewModel.loadProfile(SessionManager.getInstance().getCurrentUserId());
            }
        });
        View.OnClickListener openOwnProfile = new View.OnClickListener() {
            @Override
            public void onClick(View clicked) {
                navigateWithOwnId(R.id.action_global_user_profile);
            }
        };
        binding.includeAvatar.getRoot().setOnClickListener(openOwnProfile);
        binding.layoutNameBlock.setOnClickListener(openOwnProfile);
        View.OnClickListener openEditProfile = new View.OnClickListener() {
            @Override
            public void onClick(View clicked) {
                NavHostFragment.findNavController(MyProfileFragment.this).navigate(R.id.action_global_edit_profile);
            }
        };
        binding.buttonEditProfile.setOnClickListener(openEditProfile);
        binding.textRowEditProfile.setOnClickListener(openEditProfile);
        setUpShortcutRows();
        setUpAccountRows();
        setUpAccountButtons();
        setUpVerificationForm();
    }

    private void setUpVerificationForm() {
        binding.buttonStartVerification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View clicked) {
                formOpen = true;
                binding.layoutVerificationForm.setVisibility(View.VISIBLE);
                binding.buttonStartVerification.setVisibility(View.GONE);
            }
        });
        binding.buttonPickIdImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View clicked) {
                pickIdImage.launch(imageOnlyRequest());
            }
        });
        binding.buttonPickPortraitImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View clicked) {
                pickPortraitImage.launch(imageOnlyRequest());
            }
        });
        binding.buttonSubmitVerification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View clicked) {
                submitVerification();
            }
        });
        showPreview(binding.imageIdPreview, idImageUri);
        showPreview(binding.imagePortraitPreview, portraitImageUri);
    }

    private PickVisualMediaRequest imageOnlyRequest() {
        return new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build();
    }

    private void onIdImagePicked(Uri uri) {
        if (!isImage(uri)) {
            return;
        }
        idImageUri = uri;
        showPreview(binding.imageIdPreview, uri);
    }

    private void onPortraitImagePicked(Uri uri) {
        if (!isImage(uri)) {
            return;
        }
        portraitImageUri = uri;
        showPreview(binding.imagePortraitPreview, uri);
    }

    // The picker can still hand back a non-image from some providers, so the type is checked here.
    private boolean isImage(Uri uri) {
        if (uri == null || binding == null) {
            return false;
        }
        String type = requireContext().getContentResolver().getType(uri);
        if (type == null || !type.startsWith(IMAGE_TYPE_PREFIX)) {
            Snackbar.make(binding.getRoot(), R.string.profile_my_profile_verify_not_image, Snackbar.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private void submitVerification() {
        String nationalId = "";
        if (binding.editNationalId.getText() != null) {
            nationalId = binding.editNationalId.getText().toString();
        }
        List<Uri> imageUris = new ArrayList<Uri>();
        imageUris.add(idImageUri);
        imageUris.add(portraitImageUri);
        profileViewModel.requestVerification(nationalId, imageUris);
    }

    private void updateSubmitEnabled() {
        boolean loading = Boolean.TRUE.equals(profileViewModel.getLoading().getValue());
        boolean online = NetworkMonitor.getInstance().isOnline();
        binding.buttonSubmitVerification.setEnabled(online && !loading);
        // The form only stays open while a request can be sent, so loading here means sending.
        boolean sending = loading && formOpen;
        binding.progressSubmitVerification.setVisibility(sending ? View.VISIBLE : View.GONE);
    }

    private void clearVerificationForm() {
        formOpen = false;
        idImageUri = null;
        portraitImageUri = null;
        binding.editNationalId.setText("");
        binding.imageIdPreview.setVisibility(View.GONE);
        binding.imagePortraitPreview.setVisibility(View.GONE);
        binding.layoutVerificationForm.setVisibility(View.GONE);
    }

    private void showPreview(ImageView target, Uri uri) {
        if (uri == null) {
            target.setVisibility(View.GONE);
            return;
        }
        Context appContext = requireContext().getApplicationContext();
        // Taken on the main thread: onDestroyView may null the binding while the worker runs.
        View root = binding.getRoot();
        Thread worker = new Thread(new Runnable() {
            @Override
            public void run() {
                postPreview(root, target, decodePreview(appContext, uri));
            }
        });
        worker.start();
    }

    // A small sample is enough for a thumbnail and keeps a large photo from filling memory.
    private static Bitmap decodePreview(Context context, Uri uri) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = PREVIEW_SAMPLE_SIZE;
        try (InputStream input = context.getContentResolver().openInputStream(uri)) {
            return BitmapFactory.decodeStream(input, null, options);
        } catch (IOException | SecurityException readError) {
            return null;
        }
    }

    private void postPreview(View root, ImageView target, Bitmap bitmap) {
        if (bitmap == null) {
            return;
        }
        root.post(new Runnable() {
            @Override
            public void run() {
                if (binding == null) {
                    return;
                }
                target.setImageBitmap(bitmap);
                target.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setUpShortcutRows() {
        binding.textRowMyShop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View clicked) {
                NavHostFragment.findNavController(MyProfileFragment.this).navigate(R.id.action_global_my_shop);
            }
        });
        binding.textRowSaved.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View clicked) {
                NavHostFragment.findNavController(MyProfileFragment.this).navigate(R.id.action_global_saved_listings);
            }
        });
        binding.textRowTrust.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View clicked) {
                navigateWithOwnId(R.id.action_global_seller_trust);
            }
        });
    }

    // A04, A06, C04 and D07 are reached only through their global actions in nav_main.xml.
    private void setUpAccountRows() {
        openOnClick(binding.textRowDealHistory, R.id.action_global_deal_history);
        openOnClick(binding.textRowMyReports, R.id.action_global_my_reports);
        openOnClick(binding.textRowAccountSettings, R.id.action_global_account_settings);
        openOnClick(binding.textRowAddresses, R.id.action_global_address_list);
    }

    private void openOnClick(View row, final int actionId) {
        row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View clicked) {
                NavHostFragment.findNavController(MyProfileFragment.this).navigate(actionId);
            }
        });
    }

    private void setUpAccountButtons() {
        // The flag only shows the button; the Security Rules decide what an admin may do.
        boolean isAdmin = SessionManager.getInstance().isAdmin();
        binding.buttonAdminArea.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        binding.buttonAdminArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View clicked) {
                startActivity(new Intent(requireContext(), AdminActivity.class));
            }
        });
        binding.textLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View clicked) {
                confirmLogout();
            }
        });
    }

    private void navigateWithOwnId(int actionId) {
        Bundle arguments = new Bundle();
        arguments.putString(ARG_USER_ID, SessionManager.getInstance().getCurrentUserId());
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
        profileViewModel.getLoading().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean loading) {
                updateScreenState();
                updateSubmitEnabled();
            }
        });
        profileViewModel.getError().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String message) {
                showError(message);
            }
        });
        profileViewModel.getVerification().observe(getViewLifecycleOwner(), new Observer<SellerVerification>() {
            @Override
            public void onChanged(SellerVerification request) {
                showRejectedRequest(request);
            }
        });
        profileViewModel.getDone().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean finished) {
                if (Boolean.TRUE.equals(finished)) {
                    profileViewModel.clearDone();
                    clearVerificationForm();
                    Snackbar.make(binding.getRoot(), R.string.profile_my_profile_verify_sent,
                            Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    // Loading, content and error come from the ViewModel, so a rotation shows the same state.
    private void updateScreenState() {
        boolean loading = Boolean.TRUE.equals(profileViewModel.getLoading().getValue());
        boolean hasProfile = profileViewModel.getProfile().getValue() != null;
        binding.progressLoading.setVisibility(loading && !hasProfile ? View.VISIBLE : View.GONE);
        binding.scrollContent.setVisibility(hasProfile ? View.VISIBLE : View.GONE);
        // Nothing loaded and nothing loading means the last read failed, also after a rotation.
        boolean failed = !hasProfile && !loading;
        binding.includeErrorState.getRoot().setVisibility(failed ? View.VISIBLE : View.GONE);
        updateStatsState(hasProfile && !loading);
    }

    // Profile shown, nothing loading and no stats means the count failed: show "-" and Retry.
    private void updateStatsState(boolean settled) {
        boolean statsFailed = settled && profileViewModel.getStats().getValue() == null;
        binding.buttonRetryStats.setVisibility(statsFailed ? View.VISIBLE : View.GONE);
        if (!statsFailed) {
            return;
        }
        binding.textSoldCount.setText(R.string.profile_my_profile_stat_unknown);
        binding.textFollowerCount.setText(R.string.profile_my_profile_stat_unknown);
        binding.textMyShopHint.setVisibility(View.GONE);
    }

    private void showError(String message) {
        if (message == null || message.isEmpty()) {
            return;
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
        binding.textDisplayName.setText(user.getDisplayName());
        binding.textLocation.setText(formatLocation(user));
        showRatingAverage(user);
        showAvatar(user);
        showVerification(user);
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

    private void showStats(ProfileStats stats) {
        if (stats == null) {
            return;
        }
        binding.textSoldCount.setText(String.valueOf(stats.getSoldCount()));
        binding.textFollowerCount.setText(String.valueOf(stats.getFollowerCount()));
        binding.textMyShopHint.setVisibility(stats.getSoldCount() == 0 ? View.VISIBLE : View.GONE);
    }

    // The average comes from the users document, so it does not wait for the counts.
    private void showRatingAverage(User user) {
        if (user.getRatingCount() == 0) {
            binding.textRatingAverage.setText(R.string.profile_my_profile_no_rating);
        } else {
            String average = getString(R.string.profile_my_profile_rating_value, user.ratingAverage());
            binding.textRatingAverage.setText(average);
        }
    }

    private void showAvatar(User user) {
        String name = user.getDisplayName();
        String initial = "";
        if (name != null && !name.trim().isEmpty()) {
            initial = name.trim().substring(0, 1).toUpperCase(Locale.ROOT);
        }
        binding.includeAvatar.textInitial.setText(initial);
        binding.includeAvatar.textInitial.setVisibility(View.VISIBLE);
        binding.includeAvatar.imageAvatar.setVisibility(View.GONE);
        String avatarBase64 = user.getAvatarBase64();
        if (avatarBase64 != null && !avatarBase64.isEmpty()) {
            decodeAvatar(avatarBase64);
        }
    }

    // Decoding is disk-like work, so it runs off the main thread; a failure keeps the initial letter.
    private void decodeAvatar(String avatarBase64) {
        View root = binding.getRoot();
        Thread worker = new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = null;
                try {
                    byte[] bytes = Base64.decode(avatarBase64, Base64.DEFAULT);
                    bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                } catch (IllegalArgumentException badBase64) {
                    bitmap = null;
                }
                postAvatar(root, bitmap);
            }
        });
        worker.start();
    }

    private void postAvatar(View root, Bitmap bitmap) {
        if (bitmap == null) {
            return;
        }
        root.post(new Runnable() {
            @Override
            public void run() {
                if (binding == null) {
                    return;
                }
                binding.includeAvatar.imageAvatar.setImageBitmap(bitmap);
                binding.includeAvatar.imageAvatar.setVisibility(View.VISIBLE);
                binding.includeAvatar.textInitial.setVisibility(View.GONE);
            }
        });
    }

    private void showVerification(User user) {
        VerificationStatus status = user.getVerificationStatus();
        if (status == null) {
            status = VerificationStatus.NONE;
        }
        binding.textVerificationNote.setVisibility(View.GONE);
        binding.textResubmitLeft.setVisibility(View.GONE);
        binding.buttonStartVerification.setVisibility(View.GONE);
        if (status == VerificationStatus.PENDING) {
            showChip(R.string.profile_my_profile_verify_chip_pending, R.color.warning, R.color.chip_background_beige);
            binding.textVerificationMessage.setText(R.string.profile_my_profile_verify_pending_message);
        } else if (status == VerificationStatus.APPROVED) {
            showChip(R.string.profile_my_profile_verify_chip_approved, R.color.success, R.color.chip_background_green);
            binding.textVerificationMessage.setText(R.string.profile_my_profile_verify_approved_message);
        } else if (status == VerificationStatus.REJECTED) {
            showChip(R.string.profile_my_profile_verify_chip_rejected, R.color.on_primary, R.color.error);
            binding.textVerificationMessage.setText(R.string.profile_my_profile_verify_rejected_message);
            showRejectedRequest(profileViewModel.getVerification().getValue());
            if (profileViewModel.getVerification().getValue() == null) {
                profileViewModel.loadVerification();
            }
        } else {
            // giao-dien.md section 4 gives NONE no chip, only the explanation.
            binding.includeVerificationChip.getRoot().setVisibility(View.GONE);
            binding.textVerificationMessage.setText(R.string.profile_my_profile_verify_none_message);
            showStartButton(R.string.profile_my_profile_verify_start);
        }
        if (status == VerificationStatus.PENDING || status == VerificationStatus.APPROVED) {
            formOpen = false;
            binding.layoutVerificationForm.setVisibility(View.GONE);
        }
    }

    // The reason and the resend button come from the user's own sellerVerifications document.
    private void showRejectedRequest(SellerVerification request) {
        User user = profileViewModel.getProfile().getValue();
        if (request == null || user == null || user.getVerificationStatus() != VerificationStatus.REJECTED) {
            return;
        }
        String note = request.getNote();
        if (note != null && !note.isEmpty()) {
            binding.textVerificationNote.setText(getString(R.string.profile_my_profile_verify_note, note));
            binding.textVerificationNote.setVisibility(View.VISIBLE);
        }
        binding.textResubmitLeft.setVisibility(View.VISIBLE);
        if (request.canResubmit()) {
            binding.textResubmitLeft.setText(getString(R.string.profile_my_profile_verify_resubmit_left,
                    request.remainingResubmits()));
            showStartButton(R.string.profile_my_profile_verify_resend);
        } else {
            binding.textResubmitLeft.setText(R.string.profile_my_profile_verify_no_resubmit);
            binding.buttonStartVerification.setVisibility(View.GONE);
            formOpen = false;
            binding.layoutVerificationForm.setVisibility(View.GONE);
        }
    }

    // While the form is open the button that opens it is hidden.
    private void showStartButton(int labelId) {
        binding.buttonStartVerification.setText(labelId);
        binding.buttonStartVerification.setVisibility(formOpen ? View.GONE : View.VISIBLE);
        binding.layoutVerificationForm.setVisibility(formOpen ? View.VISIBLE : View.GONE);
    }

    private void showChip(int labelId, int textColorId, int backgroundColorId) {
        binding.includeVerificationChip.getRoot().setVisibility(View.VISIBLE);
        binding.includeVerificationChip.getRoot().setText(labelId);
        binding.includeVerificationChip.getRoot().setTextColor(ContextCompat.getColor(requireContext(), textColorId));
        int background = ContextCompat.getColor(requireContext(), backgroundColorId);
        binding.includeVerificationChip.getRoot().setBackgroundTintList(ColorStateList.valueOf(background));
    }

    private void confirmLogout() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.profile_my_profile_logout_title)
                .setMessage(R.string.profile_my_profile_logout_message)
                .setNegativeButton(R.string.common_cancel, null)
                .setPositiveButton(R.string.profile_my_profile_logout, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        authViewModel.logout();
                    }
                })
                .show();
    }

    private void observeLogout() {
        authViewModel.getDone().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean finished) {
                if (Boolean.TRUE.equals(finished)) {
                    authViewModel.clearDone();
                    goHomeAsGuest();
                }
            }
        });
        authViewModel.getError().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String message) {
                if (message == null || message.isEmpty()) {
                    return;
                }
                Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
                authViewModel.clearError();
            }
        });
    }

    // Signing out lands on Home in guest mode, not on A01, and Back must not return here.
    private void goHomeAsGuest() {
        NavOptions options = new NavOptions.Builder()
                .setPopUpTo(R.id.homeFragment, false)
                .setLaunchSingleTop(true)
                .build();
        NavHostFragment.findNavController(this).navigate(R.id.homeFragment, null, options);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // The view is gone; keeping the binding would leak it.
        binding = null;
    }
}
