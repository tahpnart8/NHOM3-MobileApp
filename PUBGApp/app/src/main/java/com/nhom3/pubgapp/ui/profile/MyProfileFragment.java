package com.nhom3.pubgapp.ui.profile;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
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
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.databinding.FragmentMyProfileBinding;
import com.nhom3.pubgapp.model.ProfileStats;
import com.nhom3.pubgapp.model.User;
import com.nhom3.pubgapp.model.enums.VerificationStatus;
import com.nhom3.pubgapp.ui.admin.AdminActivity;
import com.nhom3.pubgapp.util.NetworkMonitor;
import com.nhom3.pubgapp.util.SessionManager;
import com.nhom3.pubgapp.viewmodel.AuthViewModel;
import com.nhom3.pubgapp.viewmodel.ProfileViewModel;

import java.util.Locale;

/** D01: the signed-in user's own profile, shortcuts, account verification and sign out. */
public class MyProfileFragment extends Fragment {

    private static final String ARG_USER_ID = "userId";

    private FragmentMyProfileBinding binding;
    private ProfileViewModel profileViewModel;
    private AuthViewModel authViewModel;

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
    }

    private void setUpClicks() {
        binding.includeErrorState.buttonRetry.setOnClickListener(new View.OnClickListener() {
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
        binding.buttonSettings.setOnClickListener(openEditProfile);
        binding.textRowEditProfile.setOnClickListener(openEditProfile);
        setUpShortcutRows();
        setUpAccountButtons();
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
            }
        });
        profileViewModel.getError().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String message) {
                showError(message);
            }
        });
    }

    // Loading, content and error come from the ViewModel, so a rotation shows the same state.
    private void updateScreenState() {
        boolean loading = Boolean.TRUE.equals(profileViewModel.getLoading().getValue());
        boolean hasProfile = profileViewModel.getProfile().getValue() != null;
        binding.progressLoading.setVisibility(loading && !hasProfile ? View.VISIBLE : View.GONE);
        binding.scrollContent.setVisibility(hasProfile ? View.VISIBLE : View.GONE);
        if (hasProfile || loading) {
            binding.includeErrorState.getRoot().setVisibility(View.GONE);
        }
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
        User user = profileViewModel.getProfile().getValue();
        if (user == null || user.getRatingCount() == 0) {
            binding.textRatingAverage.setText(R.string.profile_my_profile_no_rating);
        } else {
            binding.textRatingAverage.setText(getString(R.string.profile_my_profile_rating_value, user.ratingAverage()));
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
                postAvatar(bitmap);
            }
        });
        worker.start();
    }

    private void postAvatar(Bitmap bitmap) {
        if (bitmap == null || binding == null) {
            return;
        }
        binding.getRoot().post(new Runnable() {
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
        } else {
            // giao-dien.md section 4 gives NONE no chip, only the explanation.
            binding.includeVerificationChip.getRoot().setVisibility(View.GONE);
            binding.textVerificationMessage.setText(R.string.profile_my_profile_verify_none_message);
        }
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
