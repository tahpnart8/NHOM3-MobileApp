package com.nhom3.pubgapp.ui.admin;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.ChipGroup;
import com.google.firebase.Timestamp;
import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.databinding.FragmentAdminUserListBinding;
import com.nhom3.pubgapp.model.User;
import com.nhom3.pubgapp.model.enums.UserStatus;
import com.nhom3.pubgapp.util.NetworkMonitor;
import com.nhom3.pubgapp.viewmodel.AdminUserViewModel;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

// Screen E02: search and filter the users. Read only; a row opens E03.
public class AdminUserListFragment extends Fragment {

    private static final long NO_DATE = -1L;
    private static final String ARG_PENDING_ONLY = "pendingOnly";
    private static final String ARG_USER_ID = "userId";
    private static final String STATE_LOADED = "loaded";
    private static final String STATE_LEFT_SCREEN = "leftScreen";
    private static final String STATE_FROM = "fromMillis";
    private static final String STATE_TO = "toMillis";
    private static final String STATE_ERROR = "errorMessage";
    private static final String STATE_KEYWORD = "submittedKeyword";
    private static final String STATE_PROVINCE = "submittedProvince";

    @Nullable
    private FragmentAdminUserListBinding binding;
    private AdminUserViewModel viewModel;
    private AdminUserAdapter adapter;

    private boolean hasLoaded;
    // True after the admin opened E03, so coming back reloads the list with the new status.
    private boolean leftScreen;
    // Rotation keeps these in the bundle; the ViewModel has no field for the filters (R20).
    private long fromMillis = NO_DATE;
    private long toMillis = NO_DATE;
    private String submittedKeyword = "";
    private String submittedProvince = "";
    @Nullable
    private String errorMessage;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            hasLoaded = savedInstanceState.getBoolean(STATE_LOADED);
            leftScreen = savedInstanceState.getBoolean(STATE_LEFT_SCREEN);
            fromMillis = savedInstanceState.getLong(STATE_FROM, NO_DATE);
            toMillis = savedInstanceState.getLong(STATE_TO, NO_DATE);
            errorMessage = savedInstanceState.getString(STATE_ERROR);
            submittedKeyword = savedInstanceState.getString(STATE_KEYWORD, "");
            submittedProvince = savedInstanceState.getString(STATE_PROVINCE, "");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminUserListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AdminUserViewModel.class);

        setupToolbar();
        setupList();
        setupDateButtons();
        observeViewModel();
        Bundle arguments = getArguments();
        if (!hasLoaded && arguments != null && arguments.getBoolean(ARG_PENDING_ONLY, false)) {
            binding.switchPendingOnly.setChecked(true);
        }
        updateDateButtons();
    }

    // Listeners that change filters are attached after the view state is restored, so a
    // rotation does not look like the admin touched a filter and does not reload.
    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        setupFilterListeners();
        if (!hasLoaded || leftScreen) {
            hasLoaded = true;
            leftScreen = false;
            applyFilters();
        }
        render();
    }

    @Override
    public void onStart() {
        super.onStart();
        updateOfflineBanner();
    }

    // listUsers has no live version, so the list is read again whenever the admin comes back to it.
    @Override
    public void onResume() {
        super.onResume();
        if (hasLoaded && leftScreen) {
            leftScreen = false;
            applyFilters();
            render();
        }
    }

    // A rotation is not leaving the screen; the ViewModel still holds the list then.
    @Override
    public void onPause() {
        super.onPause();
        if (!requireActivity().isChangingConfigurations()) {
            leftScreen = true;
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_LOADED, hasLoaded);
        outState.putBoolean(STATE_LEFT_SCREEN, leftScreen);
        outState.putLong(STATE_FROM, fromMillis);
        outState.putLong(STATE_TO, toMillis);
        outState.putString(STATE_ERROR, errorMessage);
        outState.putString(STATE_KEYWORD, submittedKeyword);
        outState.putString(STATE_PROVINCE, submittedProvince);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Views are gone after this call, so the binding must not outlive them.
        binding = null;
    }

    private void setupToolbar() {
        binding.includeToolbar.textTitle.setText(R.string.admin_user_list_title);
        binding.includeToolbar.buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(AdminUserListFragment.this).popBackStack();
            }
        });
    }

    private void setupList() {
        adapter = new AdminUserAdapter(new AdminUserAdapter.OnUserClickListener() {
            @Override
            public void onUserClick(User user) {
                openUser(user);
            }
        });
        // Keeps the scroll position through rotation even though the rows arrive after the restore.
        adapter.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY);
        binding.recyclerUsers.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerUsers.setAdapter(adapter);

        binding.viewEmptyState.textEmptyTitle.setText(R.string.admin_user_list_empty_title);
        binding.viewEmptyState.textEmptyMessage.setText(R.string.admin_user_list_empty_message);
        binding.viewErrorState.buttonRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                applyFilters();
            }
        });
    }

    private void setupDateButtons() {
        binding.buttonFromDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickDate(true);
            }
        });
        binding.buttonToDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickDate(false);
            }
        });
        binding.buttonClearDates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fromMillis = NO_DATE;
                toMillis = NO_DATE;
                updateDateButtons();
                applyFilters();
            }
        });
    }

    private void setupFilterListeners() {
        binding.chipGroupStatus.setOnCheckedStateChangeListener(new ChipGroup.OnCheckedStateChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull ChipGroup group, @NonNull List<Integer> checkedIds) {
                applyFilters();
            }
        });
        binding.switchPendingOnly.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton button, boolean checked) {
                render();
            }
        });
        TextView.OnEditorActionListener searchListener = new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
                if (actionId != EditorInfo.IME_ACTION_SEARCH) {
                    return false;
                }
                hideKeyboard(view);
                applyFilters();
                return true;
            }
        };
        binding.editSearch.setOnEditorActionListener(searchListener);
        binding.editProvince.setOnEditorActionListener(searchListener);
        binding.editSearch.addTextChangedListener(new ClearedTextWatcher(true));
        binding.editProvince.addTextChangedListener(new ClearedTextWatcher(false));
    }

    // Reloads only when a box that was submitted is emptied; typing does not hit Firestore.
    private class ClearedTextWatcher implements TextWatcher {

        private final boolean isKeyword;

        ClearedTextWatcher(boolean isKeyword) {
            this.isKeyword = isKeyword;
        }

        @Override
        public void beforeTextChanged(CharSequence text, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence text, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable text) {
            String submitted = isKeyword ? submittedKeyword : submittedProvince;
            if (text.toString().trim().isEmpty() && !submitted.isEmpty()) {
                applyFilters();
            }
        }
    }

    private void observeViewModel() {
        viewModel.getUsers().observe(getViewLifecycleOwner(), new Observer<List<User>>() {
            @Override
            public void onChanged(List<User> users) {
                render();
            }
        });
        viewModel.getLoading().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean loading) {
                render();
            }
        });
        viewModel.getError().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String message) {
                if (message != null && !message.isEmpty()) {
                    showError(message);
                }
            }
        });
    }

    private void showError(String message) {
        viewModel.clearError();
        if (hasShownRows()) {
            // The old rows stay on screen, so a toast is enough and nothing is kept for rotation.
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
        } else {
            errorMessage = message;
        }
        render();
    }

    private void applyFilters() {
        errorMessage = null;
        String keyword = binding.editSearch.getText().toString().trim();
        String province = binding.editProvince.getText().toString().trim();
        submittedKeyword = keyword;
        submittedProvince = province;
        viewModel.filterUsers(selectedStatus(), toTimestamp(fromMillis), toTimestamp(toMillis),
                province, keyword);
    }

    @Nullable
    private UserStatus selectedStatus() {
        int checkedId = binding.chipGroupStatus.getCheckedChipId();
        if (checkedId == R.id.chipStatusActive) {
            return UserStatus.ACTIVE;
        }
        if (checkedId == R.id.chipStatusTempLocked) {
            return UserStatus.TEMP_LOCKED;
        }
        if (checkedId == R.id.chipStatusPermLocked) {
            return UserStatus.PERM_LOCKED;
        }
        if (checkedId == R.id.chipStatusDeleted) {
            return UserStatus.DELETED;
        }
        return null;
    }

    @Nullable
    private Timestamp toTimestamp(long millis) {
        if (millis == NO_DATE) {
            return null;
        }
        return new Timestamp(new Date(millis));
    }

    private void pickDate(final boolean isFrom) {
        Calendar calendar = Calendar.getInstance();
        long current = isFrom ? fromMillis : toMillis;
        if (current != NO_DATE) {
            calendar.setTimeInMillis(current);
        }
        DatePickerDialog dialog = new DatePickerDialog(requireContext(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker picker, int year, int month, int day) {
                        onDatePicked(isFrom, year, month, day);
                    }
                },
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void onDatePicked(boolean isFrom, int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(year, month, day);
        Date picked = calendar.getTime();
        if (isFrom) {
            fromMillis = AdminUserFilterRules.startOfDay(picked).getTime();
            if (toMillis != NO_DATE && toMillis < fromMillis) {
                toMillis = NO_DATE;
            }
        } else {
            toMillis = AdminUserFilterRules.endOfDay(picked).getTime();
            if (fromMillis != NO_DATE && fromMillis > toMillis) {
                fromMillis = NO_DATE;
            }
        }
        updateDateButtons();
        applyFilters();
    }

    private void updateDateButtons() {
        if (binding == null) {
            return;
        }
        if (fromMillis == NO_DATE) {
            binding.buttonFromDate.setText(R.string.admin_user_list_from_date);
        } else {
            binding.buttonFromDate.setText(AdminUserFilterRules.formatDate(new Date(fromMillis)));
        }
        if (toMillis == NO_DATE) {
            binding.buttonToDate.setText(R.string.admin_user_list_to_date);
        } else {
            binding.buttonToDate.setText(AdminUserFilterRules.formatDate(new Date(toMillis)));
        }
    }

    private void openUser(User user) {
        Bundle args = new Bundle();
        args.putString(ARG_USER_ID, user.getId());
        leftScreen = true;
        NavHostFragment.findNavController(this).navigate(R.id.action_admin_user_list_to_user_detail, args);
    }

    private List<User> visibleUsers() {
        List<User> loaded = viewModel.getUsers().getValue();
        if (binding != null && binding.switchPendingOnly.isChecked()) {
            return AdminUserFilterRules.keepPendingVerification(loaded);
        }
        return loaded;
    }

    private boolean hasShownRows() {
        List<User> visible = visibleUsers();
        return visible != null && !visible.isEmpty();
    }

    // Shows one of: the rows, the empty note, the error, the spinner (the spinner only when no rows).
    private void render() {
        if (binding == null) {
            return;
        }
        List<User> visible = visibleUsers();
        boolean hasRows = visible != null && !visible.isEmpty();
        Boolean loadingValue = viewModel.getLoading().getValue();
        boolean loading = loadingValue != null && loadingValue.booleanValue();
        boolean failed = errorMessage != null;

        adapter.submitUsers(visible);
        binding.recyclerUsers.setVisibility(hasRows ? View.VISIBLE : View.GONE);
        binding.progressUsers.setVisibility(loading && !hasRows ? View.VISIBLE : View.GONE);
        binding.viewErrorState.layoutErrorState.setVisibility(
                failed && !loading && !hasRows ? View.VISIBLE : View.GONE);
        binding.viewEmptyState.layoutEmptyState.setVisibility(
                visible != null && !hasRows && !failed && !loading ? View.VISIBLE : View.GONE);
        if (failed) {
            binding.viewErrorState.textErrorMessage.setText(errorMessage);
        }
        updateOfflineBanner();
    }

    private void hideKeyboard(View view) {
        WindowCompat.getInsetsController(requireActivity().getWindow(), view)
                .hide(WindowInsetsCompat.Type.ime());
        view.clearFocus();
    }

    // NetworkMonitor still throws until its body is filled; then the banner stays hidden.
    private void updateOfflineBanner() {
        if (binding == null) {
            return;
        }
        boolean online = true;
        try {
            online = NetworkMonitor.getInstance().isOnline();
        } catch (UnsupportedOperationException e) {
            online = true;
        }
        binding.viewOfflineBanner.layoutOfflineBanner.setVisibility(online ? View.GONE : View.VISIBLE);
    }
}
