package com.nhom3.pubgapp.ui.account;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.databinding.FragmentAddressListBinding;
import com.nhom3.pubgapp.model.UserAddress;
import com.nhom3.pubgapp.viewmodel.ProfileViewModel;

import java.util.List;

// Screen A06: Manage user shipping and pickup addresses.
public class AddressListFragment extends Fragment implements AddressAdapter.AddressActionListener {

    @Nullable
    private FragmentAddressListBinding binding;
    private ProfileViewModel viewModel;
    private AddressAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentAddressListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);

        setupRecyclerView();
        setupListeners();
        observeViewModel();

        viewModel.loadAddresses();
    }

    private void setupRecyclerView() {
        if (binding == null) {
            return;
        }
        adapter = new AddressAdapter();
        adapter.setListener(this);
        binding.recyclerAddresses.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerAddresses.setAdapter(adapter);
    }

    private void setupListeners() {
        if (binding == null) {
            return;
        }
        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavHostFragment.findNavController(AddressListFragment.this).navigateUp();
            }
        });

        View.OnClickListener addListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToAddAddress();
            }
        };
        binding.btnAddNewAddress.setOnClickListener(addListener);
        binding.btnEmptyAdd.setOnClickListener(addListener);

        binding.btnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.loadAddresses();
            }
        });
    }

    private void navigateToAddAddress() {
        Bundle args = new Bundle();
        args.putString("addressId", "");
        NavHostFragment.findNavController(AddressListFragment.this)
                .navigate(R.id.action_global_address_edit, args);
    }

    private void observeViewModel() {
        viewModel.getAddresses().observe(getViewLifecycleOwner(), new Observer<List<UserAddress>>() {
            @Override
            public void onChanged(List<UserAddress> addresses) {
                if (binding == null || addresses == null) {
                    return;
                }
                if (addresses.isEmpty()) {
                    showEmpty();
                } else {
                    showData(addresses);
                }
            }
        });

        viewModel.getLoading().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isLoading) {
                if (binding == null) {
                    return;
                }
                if (Boolean.TRUE.equals(isLoading)) {
                    if (adapter.getItemCount() == 0) {
                        showLoading();
                    }
                } else {
                    binding.layoutLoading.setVisibility(View.GONE);
                }
            }
        });

        viewModel.getError().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String errorMessage) {
                if (binding == null || errorMessage == null || errorMessage.isEmpty()) {
                    return;
                }
                if (adapter.getItemCount() == 0) {
                    showError(errorMessage);
                } else {
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
                }
                viewModel.clearError();
            }
        });
    }

    private void showLoading() {
        if (binding == null) {
            return;
        }
        binding.layoutLoading.setVisibility(View.VISIBLE);
        binding.recyclerAddresses.setVisibility(View.GONE);
        binding.layoutEmpty.setVisibility(View.GONE);
        binding.layoutError.setVisibility(View.GONE);
        binding.bottomBar.setVisibility(View.GONE);
    }

    private void showData(List<UserAddress> addresses) {
        if (binding == null) {
            return;
        }
        binding.layoutLoading.setVisibility(View.GONE);
        binding.recyclerAddresses.setVisibility(View.VISIBLE);
        binding.layoutEmpty.setVisibility(View.GONE);
        binding.layoutError.setVisibility(View.GONE);
        binding.bottomBar.setVisibility(View.VISIBLE);
        adapter.setItems(addresses);
    }

    private void showEmpty() {
        if (binding == null) {
            return;
        }
        binding.layoutLoading.setVisibility(View.GONE);
        binding.recyclerAddresses.setVisibility(View.GONE);
        binding.layoutEmpty.setVisibility(View.VISIBLE);
        binding.layoutError.setVisibility(View.GONE);
        binding.bottomBar.setVisibility(View.GONE);
        adapter.setItems(null);
    }

    private void showError(String message) {
        if (binding == null) {
            return;
        }
        binding.layoutLoading.setVisibility(View.GONE);
        binding.recyclerAddresses.setVisibility(View.GONE);
        binding.layoutEmpty.setVisibility(View.GONE);
        binding.layoutError.setVisibility(View.VISIBLE);
        binding.bottomBar.setVisibility(View.GONE);
        binding.textError.setText(message);
    }

    @Override
    public void onEditAddress(UserAddress address) {
        if (address == null) {
            return;
        }
        Bundle args = new Bundle();
        args.putString("addressId", address.getId());
        NavHostFragment.findNavController(AddressListFragment.this)
                .navigate(R.id.action_global_address_edit, args);
    }

    @Override
    public void onDeleteAddress(final UserAddress address) {
        if (address == null) {
            return;
        }
        if (adapter.getItemCount() <= 1) {
            Toast.makeText(requireContext(), R.string.account_address_delete_cannot_delete_only_one,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.account_address_delete_confirm_title)
                .setMessage(R.string.account_address_delete_confirm_message)
                .setPositiveButton(R.string.account_address_delete,
                        new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialogInterface, int which) {
                        viewModel.deleteAddress(address.getId());
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        dialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Views are gone after this call, so the binding must not outlive them.
        binding = null;
    }
}
