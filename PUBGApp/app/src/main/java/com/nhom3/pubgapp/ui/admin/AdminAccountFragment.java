package com.nhom3.pubgapp.ui.admin;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.databinding.FragmentAdminAccountBinding;
import com.nhom3.pubgapp.model.User;
import com.nhom3.pubgapp.ui.common.AvatarBinder;
import com.nhom3.pubgapp.util.NetworkMonitor;
import com.nhom3.pubgapp.util.SessionManager;
import com.nhom3.pubgapp.viewmodel.AdminUserViewModel;

import java.util.List;

/** Screen E07 Admin Account Management: view admins, search candidate by email, grant/revoke admin rights. */
public class AdminAccountFragment extends Fragment {

    private static final String KEY_TYPED_EMAIL = "key_typed_email";
    private static final String KEY_CANDIDATE_NOT_FOUND = "key_candidate_not_found";

    @Nullable
    private FragmentAdminAccountBinding binding;
    private AdminUserViewModel viewModel;
    private AdminAccountAdapter adapter;
    private boolean searchingCandidate;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminAccountBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AdminUserViewModel.class);

        setupToolbar();
        setupRecyclerView();
        setupSearchInput();
        setupObservers();

        if (savedInstanceState != null) {
            String savedEmail = savedInstanceState.getString(KEY_TYPED_EMAIL, "");
            if (binding != null && !savedEmail.isEmpty()) {
                binding.editEmailCandidate.setText(savedEmail);
            }
            boolean notFound = savedInstanceState.getBoolean(KEY_CANDIDATE_NOT_FOUND, false);
            if (binding != null && notFound) {
                binding.textCandidateNotFound.setVisibility(View.VISIBLE);
            }
        }

        updateOfflineBanner();
        viewModel.loadAdmins();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (binding != null) {
            if (binding.editEmailCandidate.getText() != null) {
                outState.putString(KEY_TYPED_EMAIL,
                        binding.editEmailCandidate.getText().toString());
            }
            outState.putBoolean(KEY_CANDIDATE_NOT_FOUND,
                    binding.textCandidateNotFound.getVisibility() == View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setupToolbar() {
        if (binding == null) {
            return;
        }
        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavHostFragment.findNavController(AdminAccountFragment.this).navigateUp();
            }
        });
    }

    private void setupRecyclerView() {
        if (binding == null) {
            return;
        }
        adapter = new AdminAccountAdapter();
        String currentId = SessionManager.getInstance().getCurrentUserId();
        adapter.setCurrentAdminId(currentId);
        adapter.setListener(new AdminAccountAdapter.OnRevokeAdminClickListener() {
            @Override
            public void onRevokeClick(User user) {
                showRevokeConfirmDialog(user);
            }
        });
        binding.recyclerAdmins.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerAdmins.setAdapter(adapter);
    }

    private void setupSearchInput() {
        if (binding == null) {
            return;
        }
        binding.buttonFindCandidate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                triggerFindCandidate();
            }
        });

        binding.editEmailCandidate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (binding != null) {
                    binding.inputEmailLayout.setError(null);
                    binding.textCandidateNotFound.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.editEmailCandidate.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    triggerFindCandidate();
                    return true;
                }
                return false;
            }
        });
    }

    private void triggerFindCandidate() {
        if (binding == null) {
            return;
        }
        if (!isOnline()) {
            Toast.makeText(getContext(), R.string.admin_account_offline_warning, Toast.LENGTH_SHORT).show();
            return;
        }
        String email = binding.editEmailCandidate.getText() != null
                ? binding.editEmailCandidate.getText().toString().trim() : "";
        if (email.isEmpty() || !email.contains("@")) {
            binding.inputEmailLayout.setError(getString(R.string.admin_account_email_invalid));
            return;
        }
        binding.inputEmailLayout.setError(null);
        binding.layoutCandidateResult.setVisibility(View.GONE);
        binding.textCandidateNotFound.setVisibility(View.GONE);
        hideKeyboard();
        searchingCandidate = true;
        viewModel.findAdminCandidate(email);
    }

    private void setupObservers() {
        viewModel.getAdmins().observe(getViewLifecycleOwner(), new Observer<List<User>>() {
            @Override
            public void onChanged(List<User> list) {
                adapter.setItems(list);
                updateAdminsVisibility(list);
            }
        });

        viewModel.getPendingAdmin().observe(getViewLifecycleOwner(), new Observer<User>() {
            @Override
            public void onChanged(User user) {
                bindCandidateResult(user);
            }
        });

        viewModel.getLoading().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean loading) {
                boolean isLoading = loading != null && loading;
                if (binding != null) {
                    binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                }
            }
        });

        viewModel.getError().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String err) {
                if (err != null && !err.isEmpty()) {
                    if (searchingCandidate) {
                        searchingCandidate = false;
                        if (binding != null) {
                            binding.layoutCandidateResult.setVisibility(View.GONE);
                            binding.textCandidateNotFound.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Toast.makeText(getContext(), err, Toast.LENGTH_LONG).show();
                    }
                    viewModel.clearError();
                }
            }
        });

        viewModel.getDone().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isDone) {
                if (Boolean.TRUE.equals(isDone)) {
                    if (binding != null) {
                        binding.editEmailCandidate.setText("");
                        binding.inputEmailLayout.setError(null);
                        binding.textCandidateNotFound.setVisibility(View.GONE);
                        binding.layoutCandidateResult.setVisibility(View.GONE);
                    }
                    viewModel.clearDone();
                }
            }
        });
    }

    private void bindCandidateResult(final User user) {
        if (binding == null) {
            return;
        }
        if (user == null) {
            binding.layoutCandidateResult.setVisibility(View.GONE);
            if (searchingCandidate) {
                searchingCandidate = false;
                binding.textCandidateNotFound.setVisibility(View.VISIBLE);
            }
            return;
        }
        searchingCandidate = false;
        binding.textCandidateNotFound.setVisibility(View.GONE);
        binding.layoutCandidateResult.setVisibility(View.VISIBLE);

        String name = user.getDisplayName();
        if (name == null || name.trim().isEmpty()) {
            name = user.getEmail();
        }
        binding.textCandidateName.setText(name != null ? name : "");
        binding.textCandidateEmail.setText(user.getEmail() != null ? user.getEmail() : "");

        AvatarBinder.bind(binding.includeCandidateAvatar, name, user.getAvatarBase64());

        binding.buttonGrantAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGrantConfirmDialog(user);
            }
        });
    }

    private void showGrantConfirmDialog(final User user) {
        if (getContext() == null || user == null) {
            return;
        }
        if (!isOnline()) {
            Toast.makeText(getContext(), R.string.admin_account_offline_warning, Toast.LENGTH_SHORT).show();
            return;
        }
        String currentId = SessionManager.getInstance().getCurrentUserId();
        if (isSelfAdminAction(user.getId(), currentId)) {
            Toast.makeText(getContext(), R.string.admin_account_self_grant_denied, Toast.LENGTH_SHORT).show();
            return;
        }

        String displayName = user.getDisplayName();
        if (displayName == null || displayName.trim().isEmpty()) {
            displayName = user.getEmail();
        }
        String msg = getString(R.string.admin_account_grant_dialog_message, displayName, user.getEmail());

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.admin_account_grant_dialog_title)
                .setMessage(msg)
                .setPositiveButton(R.string.admin_account_dialog_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        viewModel.grantAdmin(user.getId());
                    }
                })
                .setNegativeButton(R.string.admin_account_dialog_cancel, null)
                .show();
    }

    // An admin cannot grant or revoke their own admin status (R12).
    static boolean isSelfAdminAction(String targetUserId, String currentUserId) {
        if (targetUserId == null || currentUserId == null) {
            return false;
        }
        return targetUserId.equals(currentUserId);
    }

    private void showRevokeConfirmDialog(final User user) {
        if (getContext() == null || user == null) {
            return;
        }
        if (!isOnline()) {
            Toast.makeText(getContext(), R.string.admin_account_offline_warning, Toast.LENGTH_SHORT).show();
            return;
        }
        String currentId = SessionManager.getInstance().getCurrentUserId();
        if (isSelfAdminAction(user.getId(), currentId)) {
            Toast.makeText(getContext(), R.string.admin_account_self_revoke_denied, Toast.LENGTH_SHORT).show();
            return;
        }

        String displayName = user.getDisplayName();
        if (displayName == null || displayName.trim().isEmpty()) {
            displayName = user.getEmail();
        }
        String msg = getString(R.string.admin_account_revoke_dialog_message, displayName, user.getEmail());

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.admin_account_revoke_dialog_title)
                .setMessage(msg)
                .setPositiveButton(R.string.admin_account_dialog_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        viewModel.revokeAdmin(user.getId());
                    }
                })
                .setNegativeButton(R.string.admin_account_dialog_cancel, null)
                .show();
    }

    private void updateAdminsVisibility(List<User> list) {
        if (binding == null) {
            return;
        }
        boolean hasData = list != null && !list.isEmpty();
        binding.recyclerAdmins.setVisibility(hasData ? View.VISIBLE : View.GONE);
        binding.includeEmptyState.getRoot().setVisibility(hasData ? View.GONE : View.VISIBLE);
        if (!hasData) {
            binding.includeEmptyState.textEmptyTitle.setText(R.string.admin_account_empty_title);
            binding.includeEmptyState.textEmptyMessage.setText(R.string.admin_account_empty_message);
            binding.includeEmptyState.buttonEmptyAction.setVisibility(View.GONE);
        }
    }

    private void updateOfflineBanner() {
        if (binding == null) {
            return;
        }
        binding.includeOfflineBanner.getRoot().setVisibility(
                !isOnline() ? View.VISIBLE : View.GONE);
    }

    private boolean isOnline() {
        try {
            return NetworkMonitor.getInstance().isOnline();
        } catch (UnsupportedOperationException e) {
            return true;
        }
    }

    private void hideKeyboard() {
        if (getActivity() == null || getView() == null) {
            return;
        }
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        }
    }
}
