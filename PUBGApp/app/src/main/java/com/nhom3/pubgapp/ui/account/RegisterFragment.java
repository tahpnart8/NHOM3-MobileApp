package com.nhom3.pubgapp.ui.account;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialCancellationException;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.Timestamp;
import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.databinding.FragmentRegisterBinding;
import com.nhom3.pubgapp.model.Category;
import com.nhom3.pubgapp.model.User;
import com.nhom3.pubgapp.model.UserIdentity;
import com.nhom3.pubgapp.model.enums.Gender;
import com.nhom3.pubgapp.ui.main.MainActivity;
import com.nhom3.pubgapp.util.NetworkMonitor;
import com.nhom3.pubgapp.viewmodel.AuthViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// Handles user registration, email verification waiting, and profile completion.
public class RegisterFragment extends Fragment {

    private static final String KEY_DOB_MILLIS = "key_dob_millis";
    private static final String KEY_SELECTED_CATEGORIES = "key_selected_categories";

    private FragmentRegisterBinding binding;
    private AuthViewModel viewModel;
    private CategoryChoiceAdapter categoryChoiceAdapter;
    private Timestamp selectedDob;
    private ArrayList<String> savedCategoryIds = new ArrayList<String>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        categoryChoiceAdapter = new CategoryChoiceAdapter();
        binding.recyclerCategories.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerCategories.setAdapter(categoryChoiceAdapter);

        if (savedInstanceState != null) {
            long dobMillis = savedInstanceState.getLong(KEY_DOB_MILLIS, -1);
            if (dobMillis > 0) {
                selectedDob = new Timestamp(new Date(dobMillis));
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                binding.editDob.setText(sdf.format(selectedDob.toDate()));
            }
            ArrayList<String> restoredCategories = savedInstanceState.getStringArrayList(KEY_SELECTED_CATEGORIES);
            if (restoredCategories != null) {
                savedCategoryIds = restoredCategories;
                categoryChoiceAdapter.setSelectedCategoryIds(restoredCategories);
            }
        }

        setupObservers();
        setupListeners();
        setupBackNavigation();
    }

    @Override
    public void onResume() {
        super.onResume();
        Integer step = viewModel.getSignUpStep().getValue();
        if (step != null && step == 2) {
            viewModel.checkEmailVerified();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (selectedDob != null) {
            outState.putLong(KEY_DOB_MILLIS, selectedDob.toDate().getTime());
        }
        if (categoryChoiceAdapter != null) {
            List<String> selected = categoryChoiceAdapter.getSelectedCategoryIds();
            outState.putStringArrayList(KEY_SELECTED_CATEGORIES, new ArrayList<String>(selected));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setupObservers() {
        viewModel.getSignUpStep().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer step) {
                updateStepVisibility(step);
            }
        });

        viewModel.getNeedsProfile().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean needsProfile) {
                if (needsProfile != null && needsProfile) {
                    showStep3();
                }
            }
        });

        viewModel.getCategories().observe(getViewLifecycleOwner(), new Observer<List<Category>>() {
            @Override
            public void onChanged(List<Category> categories) {
                if (categories != null) {
                    categoryChoiceAdapter.submitList(categories);
                    if (!savedCategoryIds.isEmpty()) {
                        categoryChoiceAdapter.setSelectedCategoryIds(savedCategoryIds);
                    }
                }
            }
        });

        viewModel.getLoading().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean loading) {
                boolean isLoading = loading != null && loading;
                binding.progressLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                binding.buttonContinue.setEnabled(!isLoading);
                binding.buttonGoogle.setEnabled(!isLoading);
                binding.buttonResendLink.setEnabled(!isLoading);
                binding.buttonCompleteProfile.setEnabled(!isLoading);
            }
        });

        viewModel.getError().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String err) {
                if (err != null && !err.isEmpty()) {
                    binding.textError.setText(err);
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
                    Toast.makeText(requireContext(), R.string.account_register_welcome, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(requireContext(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    requireActivity().finish();
                }
            }
        });
    }

    private void updateStepVisibility(Integer step) {
        Boolean needsProfile = viewModel.getNeedsProfile().getValue();
        if (needsProfile != null && needsProfile) {
            showStep3();
            return;
        }

        if (step != null && step == 2) {
            binding.layoutStep1.setVisibility(View.GONE);
            binding.layoutStep2.setVisibility(View.VISIBLE);
            binding.layoutStep3.setVisibility(View.GONE);
            if (binding.editEmail.getText() != null) {
                binding.textTargetEmail.setText(binding.editEmail.getText().toString().trim());
            }
        } else if (step != null && step == 3) {
            showStep3();
        } else {
            binding.layoutStep1.setVisibility(View.VISIBLE);
            binding.layoutStep2.setVisibility(View.GONE);
            binding.layoutStep3.setVisibility(View.GONE);
        }
    }

    private void showStep3() {
        binding.layoutStep1.setVisibility(View.GONE);
        binding.layoutStep2.setVisibility(View.GONE);
        binding.layoutStep3.setVisibility(View.VISIBLE);
        viewModel.loadCategories();
    }

    private void setupListeners() {
        binding.buttonContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleStep1Submit();
            }
        });

        binding.buttonGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGoogleSignIn();
            }
        });

        binding.textLoginPrompt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavHostFragment.findNavController(RegisterFragment.this)
                        .navigate(R.id.action_register_to_login);
            }
        });

        binding.buttonResendLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isNetworkAvailable()) {
                    showOfflineError();
                    return;
                }
                viewModel.resendVerificationLink();
                Toast.makeText(requireContext(), R.string.account_register_resend_success, Toast.LENGTH_SHORT).show();
            }
        });

        binding.buttonBackToStep1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.layoutStep2.setVisibility(View.GONE);
                binding.layoutStep1.setVisibility(View.VISIBLE);
            }
        });

        binding.editDob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        binding.textTermsLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTextDialog(R.string.account_register_terms_title, R.string.account_register_terms_content);
            }
        });

        binding.textPrivacyLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTextDialog(R.string.account_register_privacy_title, R.string.account_register_privacy_content);
            }
        });

        binding.buttonCompleteProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleProfileSubmit();
            }
        });
    }

    private void setupBackNavigation() {
        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        Boolean needsProfile = viewModel.getNeedsProfile().getValue();
                        if (needsProfile != null && needsProfile) {
                            viewModel.logout();
                            NavHostFragment.findNavController(RegisterFragment.this)
                                    .navigate(R.id.action_register_to_login);
                            return;
                        }
                        Integer step = viewModel.getSignUpStep().getValue();
                        if (step != null && step == 2) {
                            binding.layoutStep2.setVisibility(View.GONE);
                            binding.layoutStep1.setVisibility(View.VISIBLE);
                            return;
                        }
                        setEnabled(false);
                        requireActivity().getOnBackPressedDispatcher().onBackPressed();
                    }
                }
        );
    }

    private void handleStep1Submit() {
        if (!isNetworkAvailable()) {
            showOfflineError();
            return;
        }

        binding.layoutDisplayName.setError(null);
        binding.layoutEmail.setError(null);
        binding.layoutPassword.setError(null);
        binding.layoutConfirmPassword.setError(null);
        binding.textError.setVisibility(View.GONE);

        String name = getFieldText(binding.editDisplayName);
        String email = getFieldText(binding.editEmail);
        String password = getFieldText(binding.editPassword);
        String confirm = getFieldText(binding.editConfirmPassword);

        boolean hasError = false;
        if (name.isEmpty()) {
            binding.layoutDisplayName.setError(getString(R.string.account_register_display_name_empty));
            hasError = true;
        }
        if (email.isEmpty()) {
            binding.layoutEmail.setError(getString(R.string.account_register_email_empty));
            hasError = true;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.layoutEmail.setError(getString(R.string.account_register_email_invalid));
            hasError = true;
        }
        if (password.isEmpty()) {
            binding.layoutPassword.setError(getString(R.string.account_register_password_empty));
            hasError = true;
        } else if (!isPasswordValid(password)) {
            binding.layoutPassword.setError(getString(R.string.account_register_password_invalid));
            hasError = true;
        }
        if (confirm.isEmpty()) {
            binding.layoutConfirmPassword.setError(getString(R.string.account_register_confirm_password_empty));
            hasError = true;
        } else if (!isPasswordConfirmationMatching(password, confirm)) {
            binding.layoutConfirmPassword.setError(getString(R.string.account_register_password_mismatch));
            hasError = true;
        }

        if (hasError) {
            return;
        }

        viewModel.startSignUp(email, password);
    }

    private void handleProfileSubmit() {
        if (!isNetworkAvailable()) {
            showOfflineError();
            return;
        }

        binding.layoutPhone.setError(null);
        binding.layoutProvince.setError(null);
        binding.layoutDistrict.setError(null);
        binding.layoutNationalId.setError(null);
        binding.layoutDob.setError(null);
        binding.layoutAddress.setError(null);
        binding.textError.setVisibility(View.GONE);

        String phone = getFieldText(binding.editPhone);
        String province = getFieldText(binding.editProvince);
        String district = getFieldText(binding.editDistrict);
        String nationalId = getFieldText(binding.editNationalId);
        String address = getFieldText(binding.editAddress);

        boolean hasError = false;
        if (phone.isEmpty()) {
            binding.layoutPhone.setError(getString(R.string.account_register_phone_empty));
            hasError = true;
        } else if (!isPhoneValid(phone)) {
            binding.layoutPhone.setError(getString(R.string.account_register_phone_invalid));
            hasError = true;
        }

        if (province.isEmpty()) {
            binding.layoutProvince.setError(getString(R.string.account_register_province_empty));
            hasError = true;
        }
        if (district.isEmpty()) {
            binding.layoutDistrict.setError(getString(R.string.account_register_district_empty));
            hasError = true;
        }

        if (nationalId.length() != 12) {
            binding.layoutNationalId.setError(getString(R.string.account_register_national_id_invalid));
            hasError = true;
        }

        if (selectedDob == null) {
            binding.layoutDob.setError(getString(R.string.account_register_dob_empty));
            hasError = true;
        }

        Gender gender = getSelectedGender();
        if (gender == null) {
            binding.textError.setText(R.string.account_register_gender_empty);
            binding.textError.setVisibility(View.VISIBLE);
            hasError = true;
        }

        if (address.isEmpty()) {
            binding.layoutAddress.setError(getString(R.string.account_register_address_empty));
            hasError = true;
        }

        if (!binding.checkTerms.isChecked()) {
            binding.textError.setText(R.string.account_register_terms_required);
            binding.textError.setVisibility(View.VISIBLE);
            hasError = true;
        }

        if (hasError) {
            return;
        }

        String displayName = getFieldText(binding.editDisplayName);
        User user = new User();
        user.setDisplayName(displayName);
        user.setPhone(phone);
        user.setProvince(province);
        user.setDistrict(district);

        UserIdentity identity = new UserIdentity();
        identity.setNationalId(nationalId);
        identity.setDateOfBirth(selectedDob);
        identity.setGender(gender);
        identity.setPermanentAddress(address);

        List<String> categoryIds = categoryChoiceAdapter.getSelectedCategoryIds();
        viewModel.completeProfile(user, identity, categoryIds);
    }

    private Gender getSelectedGender() {
        int checkedId = binding.groupGender.getCheckedRadioButtonId();
        if (checkedId == R.id.radioMale) {
            return Gender.MALE;
        }
        if (checkedId == R.id.radioFemale) {
            return Gender.FEMALE;
        }
        if (checkedId == R.id.radioOther) {
            return Gender.OTHER;
        }
        return null;
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        if (selectedDob != null) {
            calendar.setTime(selectedDob.toDate());
        } else {
            calendar.set(2000, Calendar.JANUARY, 1);
        }
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int sYear, int sMonth, int sDay) {
                        Calendar chosen = Calendar.getInstance();
                        chosen.set(sYear, sMonth, sDay, 0, 0, 0);
                        selectedDob = new Timestamp(chosen.getTime());
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        binding.editDob.setText(sdf.format(chosen.getTime()));
                        binding.layoutDob.setError(null);
                    }
                },
                year,
                month,
                day
        );
        dialog.show();
    }

    private void showTextDialog(int titleRes, int contentRes) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(titleRes);
        builder.setMessage(contentRes);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void startGoogleSignIn() {
        if (!isNetworkAvailable()) {
            showOfflineError();
            return;
        }

        try {
            CredentialManager credentialManager = CredentialManager.create(requireContext());
            GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(getString(R.string.default_web_client_id))
                    .setAutoSelectEnabled(false)
                    .build();

            GetCredentialRequest request = new GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build();

            credentialManager.getCredentialAsync(
                    requireContext(),
                    request,
                    null,
                    requireActivity().getMainExecutor(),
                    new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                        @Override
                        public void onResult(GetCredentialResponse response) {
                            handleGoogleSignInResponse(response);
                        }

                        @Override
                        public void onError(GetCredentialException e) {
                            if (e instanceof GetCredentialCancellationException) {
                                return;
                            }
                            binding.textError.setText("Không thể đăng nhập bằng Google: " + e.getMessage());
                            binding.textError.setVisibility(View.VISIBLE);
                        }
                    }
            );
        } catch (Exception e) {
            binding.textError.setText("Không thể khởi động đăng nhập Google.");
            binding.textError.setVisibility(View.VISIBLE);
        }
    }

    private void handleGoogleSignInResponse(GetCredentialResponse response) {
        Credential credential = response.getCredential();
        if (credential instanceof CustomCredential) {
            CustomCredential customCredential = (CustomCredential) credential;
            if (GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(customCredential.getType())) {
                try {
                    GoogleIdTokenCredential googleIdTokenCredential =
                            GoogleIdTokenCredential.createFrom(customCredential.getData());
                    String idToken = googleIdTokenCredential.getIdToken();
                    viewModel.loginWithGoogle(idToken);
                } catch (Exception e) {
                    binding.textError.setText("Lỗi xử lý tài khoản Google.");
                    binding.textError.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private boolean isNetworkAvailable() {
        try {
            return NetworkMonitor.getInstance().isOnline();
        } catch (Exception e) {
            Context context = getContext();
            if (context == null) {
                return true;
            }
            ConnectivityManager cm = (ConnectivityManager)
                    context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
                return capabilities != null
                        && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
            }
            return true;
        }
    }

    private boolean isPasswordValid(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        boolean hasLetter = false;
        boolean hasDigit = false;
        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);
            if (Character.isLetter(c)) {
                hasLetter = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            }
        }
        return hasLetter && hasDigit;
    }

    private boolean isPasswordConfirmationMatching(String password, String confirmation) {
        if (password == null || confirmation == null) {
            return false;
        }
        return password.equals(confirmation);
    }

    private boolean isPhoneValid(String phone) {
        if (phone == null || phone.length() != 10) {
            return false;
        }
        if (phone.charAt(0) != '0') {
            return false;
        }
        for (int i = 0; i < phone.length(); i++) {
            if (!Character.isDigit(phone.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private void showOfflineError() {
        binding.viewOfflineBanner.layoutOfflineBanner.setVisibility(View.VISIBLE);
        binding.textError.setText(R.string.account_register_offline);
        binding.textError.setVisibility(View.VISIBLE);
    }

    private String getFieldText(com.google.android.material.textfield.TextInputEditText editText) {
        if (editText != null && editText.getText() != null) {
            return editText.getText().toString().trim();
        }
        return "";
    }
}
