package com.nhom3.pubgapp.ui.account;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.databinding.FragmentAccountSettingsBinding;
import com.nhom3.pubgapp.util.NetworkMonitor;
import com.nhom3.pubgapp.viewmodel.AuthViewModel;

/** Screen A05 Account Settings: contains password change, notifications, and account deletion. */
public class AccountSettingsFragment extends Fragment {

    @Nullable
    private FragmentAccountSettingsBinding binding;
    private AuthViewModel authViewModel;

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
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        setupTopBar();
        setupPasswordSection();
        observeViewModel();
        updateOfflineBanner();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateOfflineBanner();
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
    }
}
