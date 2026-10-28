package com.nhom3.pubgapp.ui.profile;

import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
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

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.databinding.FragmentEditProfileBinding;
import com.nhom3.pubgapp.model.User;
import com.nhom3.pubgapp.ui.common.AvatarBinder;
import com.nhom3.pubgapp.util.SessionManager;
import com.nhom3.pubgapp.viewmodel.ProfileViewModel;


/** A05: edit the public profile and change the avatar. */
public class EditProfileFragment extends Fragment {

    // No limit is written in the documents yet; these match the counters in the layout.
    private static final int DISPLAY_NAME_MAX = 50;
    private static final int BIO_MAX = 200;
    private static final String PHONE_PATTERN = "0[0-9]{9}";
    private static final String STATE_FIELDS_FILLED = "editProfileFieldsFilled";

    private FragmentEditProfileBinding binding;
    private ProfileViewModel profileViewModel;
    private ActivityResultLauncher<PickVisualMediaRequest> pickAvatar;
    // The text fields restore themselves after rotation, so they are filled from the profile only once.
    private boolean fieldsFilled;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pickAvatar = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri uri) {
                        if (uri != null) {
                            profileViewModel.changeAvatar(uri);
                        }
                    }
                });
        if (savedInstanceState != null) {
            fieldsFilled = savedInstanceState.getBoolean(STATE_FIELDS_FILLED);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Activity scope, so D01 shows the saved profile without reading it again.
        profileViewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);
        binding.includeToolbar.textTitle.setText(R.string.profile_edit_profile_title);
        setUpClicks();
        observeViewModel();
        profileViewModel.loadProfile(SessionManager.getInstance().getCurrentUserId());
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_FIELDS_FILLED, fieldsFilled);
    }

    private void setUpClicks() {
        binding.includeToolbar.buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View clicked) {
                leaveScreen();
            }
        });
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        leaveScreen();
                    }
                });
        binding.buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View clicked) {
                saveProfile();
            }
        });
        binding.buttonChangeAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View clicked) {
                PickVisualMediaRequest request = new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build();
                pickAvatar.launch(request);
            }
        });
        binding.includeErrorState.buttonRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View clicked) {
                profileViewModel.loadProfile(SessionManager.getInstance().getCurrentUserId());
            }
        });
    }

    private void observeViewModel() {
        profileViewModel.getProfile().observe(getViewLifecycleOwner(), new Observer<User>() {
            @Override
            public void onChanged(User user) {
                showProfile(user);
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
        profileViewModel.getDone().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean finished) {
                if (Boolean.TRUE.equals(finished)) {
                    profileViewModel.clearDone();
                    Snackbar.make(binding.getRoot(), R.string.profile_edit_profile_saved, Snackbar.LENGTH_SHORT).show();
                    NavHostFragment.findNavController(EditProfileFragment.this).navigateUp();
                }
            }
        });
    }

    private void updateScreenState() {
        boolean loading = Boolean.TRUE.equals(profileViewModel.getLoading().getValue());
        boolean hasProfile = profileViewModel.getProfile().getValue() != null;
        binding.progressLoading.setVisibility(loading && !hasProfile ? View.VISIBLE : View.GONE);
        binding.scrollContent.setVisibility(hasProfile ? View.VISIBLE : View.GONE);
        binding.includeErrorState.getRoot().setVisibility(!hasProfile && !loading ? View.VISIBLE : View.GONE);
        // While a photo or a save is in flight, the spinner covers the avatar and both buttons wait.
        binding.progressAvatar.setVisibility(loading && hasProfile ? View.VISIBLE : View.GONE);
        binding.buttonSave.setEnabled(hasProfile && !loading);
        binding.buttonChangeAvatar.setEnabled(hasProfile && !loading);
    }

    private void showError(String message) {
        if (message == null || message.isEmpty()) {
            return;
        }
        if (profileViewModel.getProfile().getValue() == null) {
            binding.includeErrorState.textErrorMessage.setText(message);
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
        showAvatar(user);
        if (fieldsFilled) {
            return;
        }
        binding.editDisplayName.setText(user.getDisplayName());
        binding.editBio.setText(user.getBio());
        binding.editPhone.setText(user.getPhone());
        binding.editProvince.setText(user.getProvince());
        binding.editDistrict.setText(user.getDistrict());
        fieldsFilled = true;
    }

    private void showAvatar(User user) {
        AvatarBinder.bind(binding.includeAvatar, user.getDisplayName(), user.getAvatarBase64());
    }

    private void saveProfile() {
        User current = profileViewModel.getProfile().getValue();
        if (current == null || !isFormValid()) {
            return;
        }
        // A copy, so the profile D01 shows changes only after the write succeeds.
        User edited = copyProfile(current);
        edited.setDisplayName(textOf(binding.editDisplayName.getText()));
        edited.setBio(textOf(binding.editBio.getText()));
        edited.setPhone(textOf(binding.editPhone.getText()));
        edited.setProvince(textOf(binding.editProvince.getText()));
        edited.setDistrict(textOf(binding.editDistrict.getText()));
        profileViewModel.updateProfile(edited);
    }

    private boolean isFormValid() {
        String name = textOf(binding.editDisplayName.getText());
        String bio = textOf(binding.editBio.getText());
        String phone = textOf(binding.editPhone.getText());
        boolean valid = true;
        binding.inputDisplayName.setError(null);
        binding.inputBio.setError(null);
        binding.inputPhone.setError(null);
        if (name.isEmpty()) {
            binding.inputDisplayName.setError(getString(R.string.profile_edit_profile_name_empty));
            valid = false;
        } else if (name.length() > DISPLAY_NAME_MAX) {
            binding.inputDisplayName.setError(getString(R.string.profile_edit_profile_name_too_long));
            valid = false;
        }
        if (bio.length() > BIO_MAX) {
            binding.inputBio.setError(getString(R.string.profile_edit_profile_bio_too_long));
            valid = false;
        }
        if (!phone.matches(PHONE_PATTERN)) {
            binding.inputPhone.setError(getString(R.string.profile_edit_profile_phone_invalid));
            valid = false;
        }
        return valid;
    }

    private boolean hasUnsavedChanges() {
        User current = profileViewModel.getProfile().getValue();
        if (current == null || binding == null) {
            return false;
        }
        return !sameText(current.getDisplayName(), binding.editDisplayName.getText())
                || !sameText(current.getBio(), binding.editBio.getText())
                || !sameText(current.getPhone(), binding.editPhone.getText())
                || !sameText(current.getProvince(), binding.editProvince.getText())
                || !sameText(current.getDistrict(), binding.editDistrict.getText());
    }

    private void leaveScreen() {
        if (!hasUnsavedChanges()) {
            NavHostFragment.findNavController(this).navigateUp();
            return;
        }
        new MaterialAlertDialogBuilder(requireContext())
                .setMessage(R.string.profile_edit_profile_unsaved_message)
                .setNegativeButton(R.string.profile_edit_profile_unsaved_stay, null)
                .setPositiveButton(R.string.profile_edit_profile_unsaved_leave, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        NavHostFragment.findNavController(EditProfileFragment.this).navigateUp();
                    }
                })
                .show();
    }

    private static boolean sameText(String saved, Editable typed) {
        String savedText = saved == null ? "" : saved.trim();
        return savedText.equals(textOf(typed));
    }

    private static String textOf(Editable text) {
        if (text == null) {
            return "";
        }
        return text.toString().trim();
    }

    // Every field is copied, because repositories and D01 read the whole object back.
    private static User copyProfile(User source) {
        User copy = new User();
        copy.setId(source.getId());
        copy.setCreatedAt(source.getCreatedAt());
        copy.setUpdatedAt(source.getUpdatedAt());
        copy.setEmail(source.getEmail());
        copy.setDisplayName(source.getDisplayName());
        copy.setPhone(source.getPhone());
        copy.setAvatarBase64(source.getAvatarBase64());
        copy.setBio(source.getBio());
        copy.setStatus(source.getStatus());
        copy.setLockedUntil(source.getLockedUntil());
        copy.setVerificationStatus(source.getVerificationStatus());
        copy.setRatingSum(source.getRatingSum());
        copy.setRatingCount(source.getRatingCount());
        copy.setWarningCount(source.getWarningCount());
        copy.setInterestedCategoryIds(source.getInterestedCategoryIds());
        copy.setProvince(source.getProvince());
        copy.setDistrict(source.getDistrict());
        copy.setTermsAcceptedAt(source.getTermsAcceptedAt());
        copy.setNotificationPrefs(source.getNotificationPrefs());
        copy.setLastActiveAt(source.getLastActiveAt());
        copy.setLockReason(source.getLockReason());
        copy.setSearchKeywords(source.getSearchKeywords());
        return copy;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // The view is gone; keeping the binding would leak it.
        binding = null;
    }
}
