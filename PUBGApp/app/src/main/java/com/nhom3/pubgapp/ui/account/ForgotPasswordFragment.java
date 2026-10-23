package com.nhom3.pubgapp.ui.account;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.databinding.FragmentForgotPasswordBinding;
import com.nhom3.pubgapp.util.NetworkMonitor;
import com.nhom3.pubgapp.viewmodel.AuthViewModel;

public class ForgotPasswordFragment extends Fragment {

    private FragmentForgotPasswordBinding binding;
    private AuthViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentForgotPasswordBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        setupObservers();
        setupListeners();
    }

    private void setupObservers() {
        viewModel.getLoading().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean loading) {
                boolean isLoading = loading != null && loading;
                binding.progressBarLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                binding.buttonSend.setEnabled(!isLoading);
            }
        });

        viewModel.getError().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String errorMsg) {
                if (errorMsg != null && !errorMsg.isEmpty()) {
                    binding.textError.setText(errorMsg);
                    binding.textError.setVisibility(View.VISIBLE);
                } else {
                    binding.textError.setVisibility(View.GONE);
                }
            }
        });

        viewModel.getDone().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isDone) {
                if (isDone != null && isDone) {
                    binding.textSuccess.setVisibility(View.VISIBLE);
                    binding.textError.setVisibility(View.GONE);
                    binding.layoutEmail.setError(null);
                    if (binding.editEmail.getText() != null) {
                        binding.editEmail.getText().clear();
                    }
                    viewModel.clearDone();
                }
            }
        });
    }

    private void setupListeners() {
        binding.buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavHostFragment.findNavController(ForgotPasswordFragment.this).navigateUp();
            }
        });

        binding.buttonBackToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavHostFragment.findNavController(ForgotPasswordFragment.this).navigateUp();
            }
        });

        binding.buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSendResetLink();
            }
        });
    }

    private void handleSendResetLink() {
        if (!isNetworkAvailable()) {
            binding.viewOfflineBanner.layoutOfflineBanner.setVisibility(View.VISIBLE);
            binding.textError.setText(R.string.account_forgot_password_offline);
            binding.textError.setVisibility(View.VISIBLE);
            return;
        }
        binding.viewOfflineBanner.layoutOfflineBanner.setVisibility(View.GONE);

        String email = "";
        if (binding.editEmail.getText() != null) {
            email = binding.editEmail.getText().toString().trim();
        }

        if (email.isEmpty()) {
            binding.layoutEmail.setError(getString(R.string.account_login_email_empty));
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.layoutEmail.setError(getString(R.string.account_login_email_invalid));
            return;
        }

        binding.layoutEmail.setError(null);
        binding.textError.setVisibility(View.GONE);
        binding.textSuccess.setVisibility(View.GONE);
        viewModel.clearError();
        viewModel.resetPassword(email);
    }

    private boolean isNetworkAvailable() {
        try {
            return NetworkMonitor.getInstance().isOnline();
        } catch (Exception e) {
            ConnectivityManager cm = (ConnectivityManager)
                    requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
                return capabilities != null
                        && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
            }
            return true;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
