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
import androidx.navigation.fragment.NavHostFragment;

import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.databinding.FragmentAddressEditBinding;
import com.nhom3.pubgapp.model.UserAddress;
import com.nhom3.pubgapp.viewmodel.ProfileViewModel;

import java.util.List;

// Screen A06: Add or edit a user delivery address.
public class AddressEditFragment extends Fragment {

    private static final String KEY_ADDRESS_ID = "addressId";

    @Nullable
    private FragmentAddressEditBinding binding;
    private ProfileViewModel viewModel;
    private String addressId = "";
    private UserAddress currentAddress;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentAddressEditBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);

        if (savedInstanceState != null) {
            addressId = savedInstanceState.getString(KEY_ADDRESS_ID, "");
        } else if (getArguments() != null) {
            addressId = getArguments().getString(KEY_ADDRESS_ID, "");
        }

        bindAddressData();
        setupListeners();
        observeViewModel();
    }

    private void bindAddressData() {
        if (binding == null) {
            return;
        }
        if (addressId != null && !addressId.trim().isEmpty()) {
            binding.textTitle.setText(R.string.account_address_edit_title_edit);
            findAndFillAddress(addressId);
        } else {
            binding.textTitle.setText(R.string.account_address_edit_title_add);
            List<UserAddress> existing = viewModel.getAddresses().getValue();
            if (existing == null || existing.isEmpty()) {
                binding.switchDefault.setChecked(true);
                binding.switchDefault.setEnabled(false);
            }
        }
    }

    private void findAndFillAddress(String id) {
        if (binding == null) {
            return;
        }
        List<UserAddress> addresses = viewModel.getAddresses().getValue();
        if (addresses != null) {
            for (UserAddress addr : addresses) {
                if (id.equals(addr.getId())) {
                    currentAddress = addr;
                    break;
                }
            }
        }
        if (currentAddress != null) {
            binding.editLabel.setText(currentAddress.getLabel() != null ? currentAddress.getLabel() : "");
            binding.editReceiverName.setText(
                    currentAddress.getReceiverName() != null ? currentAddress.getReceiverName() : "");
            binding.editPhone.setText(currentAddress.getPhone() != null ? currentAddress.getPhone() : "");
            binding.editProvince.setText(
                    currentAddress.getProvince() != null ? currentAddress.getProvince() : "");
            binding.editDistrict.setText(
                    currentAddress.getDistrict() != null ? currentAddress.getDistrict() : "");
            binding.editDetail.setText(currentAddress.getDetail() != null ? currentAddress.getDetail() : "");
            binding.switchDefault.setChecked(currentAddress.isDefault());
            if (currentAddress.isDefault()) {
                binding.switchDefault.setEnabled(false);
            }
        }
    }

    private void setupListeners() {
        if (binding == null) {
            return;
        }
        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavHostFragment.findNavController(AddressEditFragment.this).navigateUp();
            }
        });

        binding.btnSaveAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSaveAddress();
            }
        });
    }

    private void handleSaveAddress() {
        if (binding == null) {
            return;
        }
        clearFieldErrors();

        String label = textOf(binding.editLabel);
        String receiverName = textOf(binding.editReceiverName);
        String phone = textOf(binding.editPhone);
        String province = textOf(binding.editProvince);
        String district = textOf(binding.editDistrict);
        String detail = textOf(binding.editDetail);
        boolean isDefault = binding.switchDefault.isChecked();

        if (receiverName.isEmpty()) {
            binding.layoutReceiverName.setError(getString(R.string.account_address_receiver_name_empty));
            return;
        }
        if (phone.isEmpty()) {
            binding.layoutPhone.setError(getString(R.string.account_address_phone_empty));
            return;
        }
        if (!isPhoneValid(phone)) {
            binding.layoutPhone.setError(getString(R.string.account_address_phone_invalid));
            return;
        }
        if (province.isEmpty()) {
            binding.layoutProvince.setError(getString(R.string.account_address_province_empty));
            return;
        }
        if (district.isEmpty()) {
            binding.layoutDistrict.setError(getString(R.string.account_address_district_empty));
            return;
        }
        if (detail.isEmpty()) {
            binding.layoutDetail.setError(getString(R.string.account_address_detail_empty));
            return;
        }

        UserAddress target;
        if (currentAddress != null) {
            target = currentAddress;
        } else {
            target = new UserAddress();
        }
        target.setLabel(label);
        target.setReceiverName(receiverName);
        target.setPhone(phone);
        target.setProvince(province);
        target.setDistrict(district);
        target.setDetail(detail);
        target.setDefault(isDefault);

        viewModel.saveAddress(target);
    }

    private String textOf(com.google.android.material.textfield.TextInputEditText editText) {
        if (editText.getText() == null) {
            return "";
        }
        return editText.getText().toString().trim();
    }

    // Phone numbers in Vietnam start with 0 and have exactly 10 digits.
    private boolean isPhoneValid(String phone) {
        if (phone == null) {
            return false;
        }
        String trimmed = phone.trim();
        if (trimmed.length() != 10) {
            return false;
        }
        if (!trimmed.startsWith("0")) {
            return false;
        }
        for (int i = 0; i < trimmed.length(); i++) {
            char digit = trimmed.charAt(i);
            if (digit < '0' || digit > '9') {
                return false;
            }
        }
        return true;
    }

    private void clearFieldErrors() {
        if (binding == null) {
            return;
        }
        binding.layoutReceiverName.setError(null);
        binding.layoutPhone.setError(null);
        binding.layoutProvince.setError(null);
        binding.layoutDistrict.setError(null);
        binding.layoutDetail.setError(null);
        binding.textFormError.setVisibility(View.GONE);
    }

    private void observeViewModel() {
        viewModel.getLoading().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isLoading) {
                if (binding == null) {
                    return;
                }
                if (Boolean.TRUE.equals(isLoading)) {
                    binding.progressSave.setVisibility(View.VISIBLE);
                    binding.btnSaveAddress.setEnabled(false);
                } else {
                    binding.progressSave.setVisibility(View.GONE);
                    binding.btnSaveAddress.setEnabled(true);
                }
            }
        });

        viewModel.getError().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String errorMessage) {
                if (binding == null || errorMessage == null || errorMessage.isEmpty()) {
                    return;
                }
                binding.textFormError.setVisibility(View.VISIBLE);
                binding.textFormError.setText(errorMessage);
                viewModel.clearError();
            }
        });

        viewModel.getDone().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isDone) {
                if (Boolean.TRUE.equals(isDone)) {
                    viewModel.clearDone();
                    Toast.makeText(requireContext(), R.string.account_address_save_success,
                            Toast.LENGTH_SHORT).show();
                    NavHostFragment.findNavController(AddressEditFragment.this).navigateUp();
                }
            }
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_ADDRESS_ID, addressId);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Views are gone after this call, so the binding must not outlive them.
        binding = null;
    }
}
