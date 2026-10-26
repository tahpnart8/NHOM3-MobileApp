package com.nhom3.pubgapp.ui.account;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.databinding.FragmentAccountSettingsBinding;
import com.nhom3.pubgapp.model.User;
import com.nhom3.pubgapp.model.enums.NotificationType;
import com.nhom3.pubgapp.util.NetworkMonitor;
import com.nhom3.pubgapp.util.SessionManager;
import com.nhom3.pubgapp.viewmodel.AuthViewModel;
import com.nhom3.pubgapp.viewmodel.ProfileViewModel;
import java.util.HashMap;
import java.util.Map;

/** Screen A05 Account Settings: contains password change, notifications, and account deletion. */
public class AccountSettingsFragment extends Fragment {

    private static final String KEY_WAITING_GOOGLE_REAUTH = "key_waiting_google_reauth";
    private static final String KEY_DEACTIVATING = "key_deactivating";

    @Nullable
    private FragmentAccountSettingsBinding binding;
    private AuthViewModel authViewModel;
    private ProfileViewModel profileViewModel;
    private boolean isBindingNotificationPrefs;
    private boolean isWaitingForGoogleReauthToDelete;
    private boolean isDeactivating;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentAccountSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState != null) {
            isWaitingForGoogleReauthToDelete = savedInstanceState.getBoolean(
                    KEY_WAITING_GOOGLE_REAUTH, false);
            isDeactivating = savedInstanceState.getBoolean(KEY_DEACTIVATING, false);
        }
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        setupTopBar();
        setupPasswordSection();
        setupNotificationSection();
        setupDeleteSection();
        observeViewModel();
        updateOfflineBanner();

        String currentUid = SessionManager.getInstance().getCurrentUserId();
        if (currentUid != null && !currentUid.isEmpty()) {
            profileViewModel.loadProfile(currentUid);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateOfflineBanner();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_WAITING_GOOGLE_REAUTH, isWaitingForGoogleReauthToDelete);
        outState.putBoolean(KEY_DEACTIVATING, isDeactivating);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Views are gone after this call, so the binding must not outlive them.
        binding = null;
    }

    private void setupTopBar() {
        if (binding == null) {
            return;
        }
        binding.buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigateUp();
            }
        });
    }

    private void setupPasswordSection() {
        if (binding == null) {
            return;
        }
        // Google accounts have no password in Firebase Auth, so hide the password section.
        if (authViewModel.isGoogleAccount()) {
            binding.includePassword.getRoot().setVisibility(View.GONE);
            return;
        }
        binding.includePassword.getRoot().setVisibility(View.VISIBLE);
        binding.includePassword.buttonChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performChangePassword();
            }
        });
    }

    private void performChangePassword() {
        if (binding == null) {
            return;
        }
        binding.includePassword.textLayoutCurrentPassword.setError(null);
        binding.includePassword.textLayoutNewPassword.setError(null);
        binding.includePassword.textLayoutConfirmPassword.setError(null);
        binding.includePassword.textPasswordError.setVisibility(View.GONE);

        String current = "";
        if (binding.includePassword.editCurrentPassword.getText() != null) {
            current = binding.includePassword.editCurrentPassword.getText().toString().trim();
        }
        String next = "";
        if (binding.includePassword.editNewPassword.getText() != null) {
            next = binding.includePassword.editNewPassword.getText().toString().trim();
        }
        String confirm = "";
        if (binding.includePassword.editConfirmPassword.getText() != null) {
            confirm = binding.includePassword.editConfirmPassword.getText().toString().trim();
        }

        if (current.isEmpty()) {
            binding.includePassword.textLayoutCurrentPassword.setError(
                    getString(R.string.account_settings_current_password_empty));
            return;
        }
        if (next.isEmpty()) {
            binding.includePassword.textLayoutNewPassword.setError(
                    getString(R.string.account_settings_new_password_empty));
            return;
        }
        if (!AuthViewModel.isPasswordValid(next)) {
            binding.includePassword.textLayoutNewPassword.setError(
                    getString(R.string.account_settings_new_password_invalid));
            return;
        }
        if (current.equals(next)) {
            binding.includePassword.textLayoutNewPassword.setError(
                    getString(R.string.account_settings_new_password_same));
            return;
        }
        if (confirm.isEmpty()) {
            binding.includePassword.textLayoutConfirmPassword.setError(
                    getString(R.string.account_settings_confirm_password_empty));
            return;
        }
        if (!AuthViewModel.isPasswordConfirmationMatching(next, confirm)) {
            binding.includePassword.textLayoutConfirmPassword.setError(
                    getString(R.string.account_settings_confirm_password_mismatch));
            return;
        }
        if (!NetworkMonitor.getInstance().isOnline()) {
            binding.includePassword.textPasswordError.setText(R.string.account_settings_offline);
            binding.includePassword.textPasswordError.setVisibility(View.VISIBLE);
            return;
        }

        authViewModel.changePassword(current, next);
    }

    private void setupNotificationSection() {
        if (binding == null) {
            return;
        }
        binding.includeNotifications.switchNotifyMessages.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        handleNotificationPrefChanged(NotificationType.NEW_MESSAGE.name(), isChecked, buttonView);
                    }
                });
        binding.includeNotifications.switchNotifyOffers.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        handleNotificationPrefChanged(NotificationType.NEW_OFFER.name(), isChecked, buttonView);
                    }
                });
        binding.includeNotifications.switchNotifyDeals.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        handleNotificationPrefChanged(NotificationType.DEAL_UPDATE.name(), isChecked, buttonView);
                    }
                });
    }

    private void handleNotificationPrefChanged(String typeKey, boolean isChecked, CompoundButton buttonView) {
        if (isBindingNotificationPrefs || binding == null) {
            return;
        }
        if (!NetworkMonitor.getInstance().isOnline()) {
            Toast.makeText(requireContext(), R.string.account_settings_offline, Toast.LENGTH_SHORT).show();
            isBindingNotificationPrefs = true;
            buttonView.setChecked(!isChecked);
            isBindingNotificationPrefs = false;
            return;
        }
        Map<String, Boolean> current = null;
        User user = profileViewModel.getProfile().getValue();
        if (user != null) {
            current = user.getNotificationPrefs();
        }
        Map<String, Boolean> updated = buildUpdatedNotificationPrefs(current, typeKey, isChecked);
        profileViewModel.updateNotificationPrefs(updated);
    }

    private void applyNotificationPrefsToSwitches(Map<String, Boolean> prefs) {
        if (binding == null) {
            return;
        }
        isBindingNotificationPrefs = true;
        boolean msgEnabled = isNotificationTypeEnabled(prefs, NotificationType.NEW_MESSAGE.name());
        boolean offerEnabled = isNotificationTypeEnabled(prefs, NotificationType.NEW_OFFER.name());
        boolean dealEnabled = isNotificationTypeEnabled(prefs, NotificationType.DEAL_UPDATE.name());
        binding.includeNotifications.switchNotifyMessages.setChecked(msgEnabled);
        binding.includeNotifications.switchNotifyOffers.setChecked(offerEnabled);
        binding.includeNotifications.switchNotifyDeals.setChecked(dealEnabled);
        isBindingNotificationPrefs = false;
    }

    private static boolean isNotificationTypeEnabled(Map<String, Boolean> prefs, String typeKey) {
        if (prefs == null || !prefs.containsKey(typeKey)) {
            return true;
        }
        Boolean val = prefs.get(typeKey);
        if (val == null) {
            return true;
        }
        return val.booleanValue();
    }

    private static Map<String, Boolean> buildUpdatedNotificationPrefs(Map<String, Boolean> currentPrefs,
            String typeKey, boolean enabled) {
        Map<String, Boolean> updated = new HashMap<String, Boolean>();
        if (currentPrefs != null) {
            updated.putAll(currentPrefs);
        }
        updated.put(typeKey, Boolean.valueOf(enabled));
        return updated;
    }

    private void setupDeleteSection() {
        if (binding == null) {
            return;
        }
        binding.includeDelete.buttonDeleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!NetworkMonitor.getInstance().isOnline()) {
                    Toast.makeText(requireContext(),
                            R.string.account_settings_offline, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (authViewModel.isGoogleAccount()) {
                    showGoogleDeleteConfirmDialog();
                } else {
                    showEmailDeleteConfirmDialog();
                }
            }
        });
    }

    private void showGoogleDeleteConfirmDialog() {
        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.account_settings_delete_dialog_title)
                .setMessage(R.string.account_settings_delete_google_dialog_message)
                .setNegativeButton(R.string.common_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int which) {
                        isWaitingForGoogleReauthToDelete = false;
                    }
                })
                .setPositiveButton(R.string.account_settings_delete_google_reauth_button,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface d, int which) {
                                isWaitingForGoogleReauthToDelete = true;
                                authViewModel.loginWithGoogleBrowser(requireActivity());
                            }
                        })
                .create();
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface d) {
                isWaitingForGoogleReauthToDelete = false;
            }
        });
        dialog.show();
    }

    private void showEmailDeleteConfirmDialog() {
        final TextInputLayout textLayout = new TextInputLayout(requireContext());
        textLayout.setHint(getString(R.string.account_settings_delete_password_hint));
        textLayout.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);

        final TextInputEditText editPassword = new TextInputEditText(requireContext());
        editPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        textLayout.addView(editPassword);

        FrameLayout container = new FrameLayout(requireContext());
        int padding = getResources().getDimensionPixelSize(R.dimen.space_md);
        container.setPadding(padding, padding / 2, padding, 0);
        container.addView(textLayout);

        final AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.account_settings_delete_dialog_title)
                .setMessage(R.string.account_settings_delete_dialog_message)
                .setView(container)
                .setNegativeButton(R.string.common_cancel, null)
                .setPositiveButton(R.string.account_settings_delete_confirm_button, null)
                .create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String password = "";
                        if (editPassword.getText() != null) {
                            password = editPassword.getText().toString();
                        }
                        if (isDeletePasswordEmpty(password)) {
                            textLayout.setError(
                                    getString(R.string.account_settings_delete_password_empty));
                            return;
                        }
                        textLayout.setError(null);
                        dialog.dismiss();
                        isDeactivating = true;
                        profileViewModel.deactivateAccount(password);
                    }
                });
            }
        });

        dialog.show();
    }

    private void goHomeAsGuest() {
        NavOptions options = new NavOptions.Builder()
                .setPopUpTo(R.id.homeFragment, false)
                .setLaunchSingleTop(true)
                .build();
        NavHostFragment.findNavController(this).navigate(R.id.homeFragment, null, options);
    }

    private void observeViewModel() {
        authViewModel.getLoading().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean loading) {
                if (binding == null) {
                    return;
                }
                boolean isLoading = loading != null && loading.booleanValue();
                binding.includePassword.progressChangePassword.setVisibility(
                        isLoading ? View.VISIBLE : View.GONE);
                boolean online = NetworkMonitor.getInstance().isOnline();
                binding.includePassword.buttonChangePassword.setEnabled(!isLoading && online);
                binding.includePassword.editCurrentPassword.setEnabled(!isLoading);
                binding.includePassword.editNewPassword.setEnabled(!isLoading);
                binding.includePassword.editConfirmPassword.setEnabled(!isLoading);
            }
        });

        authViewModel.getError().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String message) {
                if (binding == null || message == null || message.isEmpty()) {
                    return;
                }
                isWaitingForGoogleReauthToDelete = false;
                String displayMessage = message;
                if (message.contains("recent-login") || message.contains("CREDENTIAL_TOO_OLD")
                        || message.contains("xác thực lại")) {
                    displayMessage = getString(R.string.account_settings_change_password_reauth_required);
                }
                binding.includePassword.textPasswordError.setText(displayMessage);
                binding.includePassword.textPasswordError.setVisibility(View.VISIBLE);
                authViewModel.clearError();
            }
        });

        authViewModel.getDone().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean done) {
                if (binding == null || done == null || !done.booleanValue()) {
                    return;
                }
                authViewModel.clearDone();
                Toast.makeText(requireContext(),
                        R.string.account_settings_change_password_success, Toast.LENGTH_LONG).show();
                authViewModel.logout();
                Navigation.findNavController(requireView()).navigate(R.id.action_global_sign_in);
            }
        });

        profileViewModel.getProfile().observe(getViewLifecycleOwner(), new Observer<User>() {
            @Override
            public void onChanged(User user) {
                if (binding == null || user == null) {
                    return;
                }
                applyNotificationPrefsToSwitches(user.getNotificationPrefs());
            }
        });

        authViewModel.getLoggedIn().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean loggedIn) {
                if (Boolean.TRUE.equals(loggedIn) && isWaitingForGoogleReauthToDelete) {
                    isWaitingForGoogleReauthToDelete = false;
                    isDeactivating = true;
                    profileViewModel.deactivateWithGoogle("");
                }
            }
        });

        profileViewModel.getLoading().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean loading) {
                if (binding == null) {
                    return;
                }
                boolean isLoading = loading != null && loading.booleanValue();
                binding.includeNotifications.progressNotifications.setVisibility(
                        isLoading ? View.VISIBLE : View.GONE);
                boolean online = NetworkMonitor.getInstance().isOnline();
                binding.includeNotifications.switchNotifyMessages.setEnabled(!isLoading && online);
                binding.includeNotifications.switchNotifyOffers.setEnabled(!isLoading && online);
                binding.includeNotifications.switchNotifyDeals.setEnabled(!isLoading && online);
                binding.includeDelete.progressDeleteAccount.setVisibility(
                        isLoading ? View.VISIBLE : View.GONE);
                binding.includeDelete.buttonDeleteAccount.setEnabled(!isLoading && online);
            }
        });

        profileViewModel.getError().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String message) {
                if (binding == null || message == null || message.isEmpty()) {
                    return;
                }
                if (isDeactivating) {
                    isDeactivating = false;
                    binding.includeDelete.textDeleteError.setText(message);
                    binding.includeDelete.textDeleteError.setVisibility(View.VISIBLE);
                    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
                    profileViewModel.clearError();
                    if (!SessionManager.getInstance().isLoggedIn()) {
                        goHomeAsGuest();
                    }
                } else {
                    binding.includeNotifications.textNotificationsError.setText(message);
                    binding.includeNotifications.textNotificationsError.setVisibility(View.VISIBLE);
                    User current = profileViewModel.getProfile().getValue();
                    if (current != null) {
                        applyNotificationPrefsToSwitches(current.getNotificationPrefs());
                    }
                    profileViewModel.clearError();
                }
            }
        });

        profileViewModel.getDone().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean done) {
                if (binding == null || done == null || !done.booleanValue()) {
                    return;
                }
                if (isDeactivating) {
                    isDeactivating = false;
                    profileViewModel.clearDone();
                    Toast.makeText(requireContext(),
                            R.string.account_settings_delete_success, Toast.LENGTH_LONG).show();
                    authViewModel.logout();
                    goHomeAsGuest();
                } else {
                    profileViewModel.clearDone();
                    binding.includeNotifications.textNotificationsError.setVisibility(View.GONE);
                    Toast.makeText(requireContext(),
                            R.string.account_settings_notifications_update_success, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateOfflineBanner() {
        if (binding == null) {
            return;
        }
        boolean online = NetworkMonitor.getInstance().isOnline();
        binding.layoutOfflineBanner.setVisibility(online ? View.GONE : View.VISIBLE);
        Boolean loading = authViewModel.getLoading().getValue();
        boolean isLoading = loading != null && loading.booleanValue();
        binding.includePassword.buttonChangePassword.setEnabled(online && !isLoading);
        Boolean loadingProfile = profileViewModel.getLoading().getValue();
        boolean isProfileLoading = loadingProfile != null && loadingProfile.booleanValue();
        binding.includeNotifications.switchNotifyMessages.setEnabled(online && !isProfileLoading);
        binding.includeNotifications.switchNotifyOffers.setEnabled(online && !isProfileLoading);
        binding.includeNotifications.switchNotifyDeals.setEnabled(online && !isProfileLoading);
        binding.includeDelete.buttonDeleteAccount.setEnabled(online && !isProfileLoading);
    }

    private static boolean isDeletePasswordEmpty(String password) {
        return password == null || password.trim().isEmpty();
    }
}
