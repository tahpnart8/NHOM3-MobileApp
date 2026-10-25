package com.nhom3.pubgapp.ui.account;

import android.content.Context;
import android.content.Intent;
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
import com.nhom3.pubgapp.databinding.FragmentLoginBinding;
import com.nhom3.pubgapp.ui.main.MainActivity;
import com.nhom3.pubgapp.util.NetworkMonitor;
import com.nhom3.pubgapp.viewmodel.AuthViewModel;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private AuthViewModel viewModel;
    private boolean navigatedToRegister = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
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
                binding.buttonSignIn.setEnabled(!isLoading);
                binding.buttonGoogleSignIn.setEnabled(!isLoading);
                if (isLoading) {
                    navigatedToRegister = false;
                }
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

        viewModel.getLoggedIn().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean loggedIn) {
                if (loggedIn != null && loggedIn) {
                    Intent intent = new Intent(requireContext(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    requireActivity().finish();
                }
            }
        });

        viewModel.getSignUpStep().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer step) {
                if (step != null && step == 2) {
                    if (!navigatedToRegister) {
                        navigatedToRegister = true;
                        NavHostFragment.findNavController(LoginFragment.this)
                                .navigate(R.id.action_login_to_register);
                    }
                }
            }
        });

        viewModel.getNeedsProfile().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean needsProfile) {
                if (needsProfile != null && needsProfile) {
                    if (!navigatedToRegister) {
                        navigatedToRegister = true;
                        NavHostFragment.findNavController(LoginFragment.this)
                                .navigate(R.id.action_login_to_register);
                    }
                }
            }
        });
    }

    private void setupListeners() {
        binding.buttonSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleEmailSignIn();
            }
        });

        binding.buttonGoogleSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGoogleSignIn();
            }
        });

        binding.textForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.clearError();
                NavHostFragment.findNavController(LoginFragment.this)
                        .navigate(R.id.action_login_to_forgot_password);
            }
        });

        binding.buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.clearError();
                navigatedToRegister = true;
                NavHostFragment.findNavController(LoginFragment.this)
                        .navigate(R.id.action_login_to_register);
            }
        });
    }

    private void handleEmailSignIn() {
        if (!isNetworkAvailable()) {
            binding.viewOfflineBanner.layoutOfflineBanner.setVisibility(View.VISIBLE);
            binding.textError.setText(R.string.account_login_offline);
            binding.textError.setVisibility(View.VISIBLE);
            return;
        }
        binding.viewOfflineBanner.layoutOfflineBanner.setVisibility(View.GONE);

        String email = "";
        if (binding.editEmail.getText() != null) {
            email = binding.editEmail.getText().toString().trim();
        }
        String password = "";
        if (binding.editPassword.getText() != null) {
            password = binding.editPassword.getText().toString();
        }

        boolean hasError = false;
        if (email.isEmpty()) {
            binding.layoutEmail.setError(getString(R.string.account_login_email_empty));
            hasError = true;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.layoutEmail.setError(getString(R.string.account_login_email_invalid));
            hasError = true;
        } else {
            binding.layoutEmail.setError(null);
        }

        if (password.isEmpty()) {
            binding.layoutPassword.setError(getString(R.string.account_login_password_empty));
            hasError = true;
        } else {
            binding.layoutPassword.setError(null);
        }

        if (hasError) {
            return;
        }

        binding.textError.setVisibility(View.GONE);
        viewModel.clearError();
        viewModel.login(email, password);
    }

    private void startGoogleSignIn() {
        if (!isNetworkAvailable()) {
            binding.viewOfflineBanner.layoutOfflineBanner.setVisibility(View.VISIBLE);
            binding.textError.setText(R.string.account_login_offline);
            binding.textError.setVisibility(View.VISIBLE);
            return;
        }
        binding.viewOfflineBanner.layoutOfflineBanner.setVisibility(View.GONE);
        // Opens Google's own sign-in page in the browser; the loading state disables the button meanwhile.
        viewModel.loginWithGoogleBrowser(requireActivity());
    }

    private boolean isNetworkAvailable() {
        try {
            return NetworkMonitor.getInstance().isOnline();
        } catch (Exception e) {
            // NetworkMonitor throws NOT_READY until fully implemented; falls back to ConnectivityManager.
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
