package com.nhom3.pubgapp.ui.admin;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.databinding.FragmentAdminModerationBinding;
import com.nhom3.pubgapp.model.BannedKeyword;
import com.nhom3.pubgapp.model.Listing;
import com.nhom3.pubgapp.model.Report;
import com.nhom3.pubgapp.model.enums.ReportStatus;
import com.nhom3.pubgapp.model.enums.ReportTarget;
import com.nhom3.pubgapp.util.NetworkMonitor;
import com.nhom3.pubgapp.viewmodel.AdminContentViewModel;

import java.util.ArrayList;
import java.util.List;

// Screen E04: three tabs, reports grouped by target, listings flagged by a banned keyword, and the keywords.
public class AdminModerationFragment extends Fragment {

    // The detail screen sends this when it changed a listing, so this list reloads when shown again.
    static final String RESULT_LISTING_CHANGED = "admin_listing_changed";
    static final String ARG_LISTING_ID = "listingId";
    static final String ARG_USER_ID = "userId";

    private static final int TAB_REPORTS = 0;
    private static final int TAB_FLAGGED = 1;
    private static final int TAB_KEYWORDS = 2;
    private static final String STATE_TAB = "state_tab";
    private static final String STATE_REMOVE_ID = "state_remove_id";
    private static final String STATE_REMOVE_WORD = "state_remove_word";
    private static final String STATE_STATUS = "state_status";
    private static final String STATE_TYPE = "state_type";
    private static final String STATE_DIALOG_TARGET = "state_dialog_target";
    private static final String STATE_DIALOG_TEXT = "state_dialog_text";

    @Nullable
    private FragmentAdminModerationBinding binding;
    private AdminContentViewModel viewModel;
    private AdminReportAdapter adapter;
    private AdminFlaggedAdapter flaggedAdapter;
    private AdminKeywordAdapter keywordAdapter;
    private AdminReasonDialog reasonDialog;
    private ReportStatus selectedStatus = ReportStatus.OPEN;
    @Nullable
    private ReportTarget selectedType;
    private boolean awaitingResult;
    @Nullable
    private String errorText;
    @StringRes
    private int successMessage = R.string.admin_moderation_done_dismissed;
    // Set while the hide dialog is open, so a rotation can open it again for the same listing.
    @Nullable
    private String dialogTargetId;
    private int selectedTab = TAB_REPORTS;
    // The error of a tab that has no data yet; once data exists an error is a snackbar instead.
    @Nullable
    private String flaggedError;
    @Nullable
    private String keywordsError;
    private boolean addingKeyword;
    @Nullable
    private AlertDialog removeDialog;
    @Nullable
    private String removeKeywordId;
    @Nullable
    private String removeKeywordWord;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminModerationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AdminContentViewModel.class);
        reasonDialog = new AdminReasonDialog(requireContext(), getLayoutInflater(),
                new AdminReasonDialog.OnConfirmListener() {
                    @Override
                    public void onConfirm(String reason) {
                        confirmHide(reason);
                    }
                });
        String dialogText = "";
        if (savedInstanceState != null) {
            selectedStatus = ReportStatus.valueOf(
                    savedInstanceState.getString(STATE_STATUS, ReportStatus.OPEN.name()));
            String typeName = savedInstanceState.getString(STATE_TYPE);
            selectedType = typeName == null ? null : ReportTarget.valueOf(typeName);
            dialogTargetId = savedInstanceState.getString(STATE_DIALOG_TARGET);
            dialogText = savedInstanceState.getString(STATE_DIALOG_TEXT, "");
            selectedTab = savedInstanceState.getInt(STATE_TAB, TAB_REPORTS);
            removeKeywordId = savedInstanceState.getString(STATE_REMOVE_ID);
            removeKeywordWord = savedInstanceState.getString(STATE_REMOVE_WORD);
        }
        setupToolbar();
        setupFilters();
        setupList();
        setupFlaggedList();
        setupKeywordList();
        setupKeywordInput();
        setupTabs();
        observeViewModel();
        listenForListingChanges();
        if (viewModel.getReports().getValue() == null) {
            loadReports();
        }
        if (dialogTargetId != null) {
            showHideDialog(dialogTargetId, dialogText);
        }
        if (removeKeywordId != null) {
            showRemoveKeywordDialog(removeKeywordId, removeKeywordWord == null ? "" : removeKeywordWord);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        renderConnection();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_TAB, selectedTab);
        outState.putString(STATE_REMOVE_ID, removeDialog == null ? null : removeKeywordId);
        outState.putString(STATE_REMOVE_WORD, removeDialog == null ? null : removeKeywordWord);
        outState.putString(STATE_STATUS, selectedStatus.name());
        outState.putString(STATE_TYPE, selectedType == null ? null : selectedType.name());
        if (reasonDialog != null && reasonDialog.isOpen()) {
            outState.putString(STATE_DIALOG_TARGET, dialogTargetId);
            outState.putString(STATE_DIALOG_TEXT, reasonDialog.readText());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        reasonDialog.release();
        if (removeDialog != null) {
            removeDialog.setOnDismissListener(null);
            removeDialog.dismiss();
        }
        removeDialog = null;
        binding = null;
    }

    private void setupToolbar() {
        binding.layoutToolbar.textTitle.setText(R.string.admin_moderation_title);
        binding.layoutToolbar.buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(AdminModerationFragment.this).navigateUp();
            }
        });
        binding.layoutErrorState.buttonRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadReports();
            }
        });
        binding.layoutFlaggedError.buttonRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flaggedError = null;
                renderFlagged();
                viewModel.loadFlagged();
            }
        });
        binding.layoutKeywordsError.buttonRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                keywordsError = null;
                renderKeywords();
                viewModel.loadKeywords();
            }
        });
    }

    private void setupTabs() {
        TabLayout tabs = binding.tabLayoutModeration;
        TabLayout.Tab tab = tabs.getTabAt(selectedTab);
        if (tab != null) {
            tab.select();
        }
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab selected) {
                showTab(selected.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab unselected) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab reselected) {
            }
        });
        showTab(selectedTab);
    }

    // A tab reads its list the first time it is shown; a rotation finds the list already in the ViewModel.
    private void showTab(int tab) {
        selectedTab = tab;
        binding.layoutReportsTab.setVisibility(tab == TAB_REPORTS ? View.VISIBLE : View.GONE);
        binding.layoutFlaggedTab.setVisibility(tab == TAB_FLAGGED ? View.VISIBLE : View.GONE);
        binding.layoutKeywordsTab.setVisibility(tab == TAB_KEYWORDS ? View.VISIBLE : View.GONE);
        if (tab == TAB_FLAGGED && viewModel.getFlagged().getValue() == null && flaggedError == null) {
            viewModel.loadFlagged();
        }
        if (tab == TAB_KEYWORDS && viewModel.getKeywords().getValue() == null && keywordsError == null) {
            viewModel.loadKeywords();
        }
        renderFlagged();
        renderKeywords();
    }

    private void setupFlaggedList() {
        flaggedAdapter = new AdminFlaggedAdapter(new AdminFlaggedAdapter.OnFlaggedActionListener() {
            @Override
            public void onOpen(Listing listing) {
                Bundle args = new Bundle();
                args.putString(ARG_LISTING_ID, listing.getId());
                NavHostFragment.findNavController(AdminModerationFragment.this)
                        .navigate(R.id.action_admin_moderation_to_listing_detail, args);
            }

            @Override
            public void onClearFlag(Listing listing) {
                successMessage = R.string.admin_moderation_flagged_cleared;
                viewModel.clearFlag(listing.getId());
            }

            @Override
            public void onHide(Listing listing) {
                showHideDialog(listing.getId(), "");
            }
        });
        binding.recyclerFlagged.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerFlagged.setAdapter(flaggedAdapter);
    }

    private void setupKeywordList() {
        keywordAdapter = new AdminKeywordAdapter(new AdminKeywordAdapter.OnKeywordRemoveListener() {
            @Override
            public void onRemove(BannedKeyword keyword) {
                showRemoveKeywordDialog(keyword.getId(), keyword.getKeyword());
            }
        });
        binding.recyclerKeywords.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerKeywords.setAdapter(keywordAdapter);
    }

    private void setupKeywordInput() {
        binding.buttonAddKeyword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addKeyword();
            }
        });
        binding.editKeyword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    addKeyword();
                    return true;
                }
                return false;
            }
        });
        binding.editKeyword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence text, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {
                binding.inputKeyword.setError(null);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    private void addKeyword() {
        String word = String.valueOf(binding.editKeyword.getText());
        if (!AdminKeywordRules.isValidKeyword(word)) {
            binding.inputKeyword.setError(getString(R.string.admin_moderation_keyword_invalid));
            return;
        }
        binding.inputKeyword.setError(null);
        addingKeyword = true;
        successMessage = R.string.admin_moderation_keyword_done_added;
        viewModel.addKeyword(word.trim());
    }

    private void showRemoveKeywordDialog(String id, String word) {
        if (removeDialog != null) {
            return;
        }
        removeKeywordId = id;
        removeKeywordWord = word;
        removeDialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.admin_moderation_keyword_remove_title)
                .setMessage(getString(R.string.admin_moderation_keyword_remove_message, word))
                .setNegativeButton(R.string.common_cancel, null)
                .setPositiveButton(R.string.common_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        confirmRemoveKeyword();
                    }
                })
                .create();
        removeDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                removeDialog = null;
            }
        });
        removeDialog.show();
    }

    private void confirmRemoveKeyword() {
        if (removeKeywordId == null) {
            return;
        }
        successMessage = R.string.admin_moderation_keyword_done_removed;
        viewModel.removeKeyword(removeKeywordId);
    }

    private void setupFilters() {
        binding.chipGroupStatus.check(chipIdOf(selectedStatus));
        if (selectedType == null) {
            binding.chipGroupType.clearCheck();
        } else {
            binding.chipGroupType.check(chipIdOf(selectedType));
        }
        ChipGroup.OnCheckedStateChangeListener listener = new ChipGroup.OnCheckedStateChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull ChipGroup group, @NonNull List<Integer> checkedIds) {
                readFiltersFromChips();
                loadReports();
            }
        };
        binding.chipGroupStatus.setOnCheckedStateChangeListener(listener);
        binding.chipGroupType.setOnCheckedStateChangeListener(listener);
    }

    private void readFiltersFromChips() {
        int statusId = binding.chipGroupStatus.getCheckedChipId();
        if (statusId == R.id.chipStatusResolved) {
            selectedStatus = ReportStatus.RESOLVED;
        } else if (statusId == R.id.chipStatusDismissed) {
            selectedStatus = ReportStatus.DISMISSED;
        } else {
            selectedStatus = ReportStatus.OPEN;
        }
        int typeId = binding.chipGroupType.getCheckedChipId();
        if (typeId == R.id.chipTypeListing) {
            selectedType = ReportTarget.LISTING;
        } else if (typeId == R.id.chipTypeUser) {
            selectedType = ReportTarget.USER;
        } else if (typeId == R.id.chipTypeMessage) {
            selectedType = ReportTarget.MESSAGE;
        } else {
            selectedType = null;
        }
    }

    private int chipIdOf(ReportStatus status) {
        if (status == ReportStatus.RESOLVED) {
            return R.id.chipStatusResolved;
        }
        return status == ReportStatus.DISMISSED ? R.id.chipStatusDismissed : R.id.chipStatusOpen;
    }

    private int chipIdOf(ReportTarget type) {
        if (type == ReportTarget.USER) {
            return R.id.chipTypeUser;
        }
        return type == ReportTarget.MESSAGE ? R.id.chipTypeMessage : R.id.chipTypeListing;
    }

    private void setupList() {
        adapter = new AdminReportAdapter(new AdminReportAdapter.OnReportActionListener() {
            @Override
            public void onOpenListing(AdminReportGrouping.Group group) {
                Bundle args = new Bundle();
                args.putString(ARG_LISTING_ID, group.getTargetId());
                NavHostFragment.findNavController(AdminModerationFragment.this)
                        .navigate(R.id.action_admin_moderation_to_listing_detail, args);
            }

            @Override
            public void onDismiss(AdminReportGrouping.Group group) {
                successMessage = R.string.admin_moderation_done_dismissed;
                viewModel.closeReports(group.getTargetId(), true);
            }

            @Override
            public void onHide(AdminReportGrouping.Group group) {
                showHideDialog(group.getTargetId(), "");
            }

            @Override
            public void onViewUser(AdminReportGrouping.Group group) {
                Bundle args = new Bundle();
                args.putString(ARG_USER_ID, group.getTargetId());
                NavHostFragment.findNavController(AdminModerationFragment.this)
                        .navigate(R.id.adminUserDetailFragment, args);
            }
        });
        binding.recyclerReports.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerReports.setAdapter(adapter);
    }

    private void observeViewModel() {
        viewModel.getFlagged().observe(getViewLifecycleOwner(), new Observer<List<Listing>>() {
            @Override
            public void onChanged(List<Listing> listings) {
                flaggedError = null;
                renderFlagged();
            }
        });
        viewModel.getKeywords().observe(getViewLifecycleOwner(), new Observer<List<BannedKeyword>>() {
            @Override
            public void onChanged(List<BannedKeyword> list) {
                keywordsError = null;
                renderKeywords();
            }
        });
        viewModel.getReports().observe(getViewLifecycleOwner(), new Observer<List<Report>>() {
            @Override
            public void onChanged(List<Report> reports) {
                awaitingResult = false;
                errorText = null;
                render();
            }
        });
        viewModel.getReportedListings().observe(getViewLifecycleOwner(), new Observer<List<Listing>>() {
            @Override
            public void onChanged(List<Listing> listings) {
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
                showError(message);
            }
        });
        viewModel.getDone().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean done) {
                if (Boolean.TRUE.equals(done)) {
                    onActionDone();
                }
            }
        });
    }

    private void listenForListingChanges() {
        getParentFragmentManager().setFragmentResultListener(RESULT_LISTING_CHANGED, getViewLifecycleOwner(),
                new FragmentResultListener() {
                    @Override
                    public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                        loadReports();
                        if (viewModel.getFlagged().getValue() != null) {
                            viewModel.loadFlagged();
                        }
                    }
                });
    }

    private void loadReports() {
        awaitingResult = true;
        errorText = null;
        render();
        viewModel.loadReports(selectedStatus, selectedType);
    }

    // The list shows only when its data matches the chips; otherwise the screen shows loading or the error.
    private void render() {
        if (binding == null) {
            return;
        }
        List<Report> reports = viewModel.getReports().getValue();
        boolean hasData = reports != null && !awaitingResult && errorText == null;
        List<AdminReportGrouping.Group> groups = AdminReportGrouping.group(hasData ? reports : null);
        adapter.setData(groups, viewModel.getReportedListings().getValue());
        renderConnection();
        boolean isEmpty = hasData && groups.isEmpty();
        binding.recyclerReports.setVisibility(hasData && !isEmpty ? View.VISIBLE : View.GONE);
        binding.layoutEmptyState.getRoot().setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.layoutErrorState.getRoot().setVisibility(errorText != null ? View.VISIBLE : View.GONE);
        binding.progressBar.setVisibility(!hasData && errorText == null ? View.VISIBLE : View.GONE);
        if (isEmpty) {
            binding.layoutEmptyState.textEmptyTitle.setText(R.string.admin_moderation_empty_title);
            binding.layoutEmptyState.textEmptyMessage.setText(selectedStatus == ReportStatus.OPEN
                    ? R.string.admin_moderation_empty_open : R.string.admin_moderation_empty_closed);
        }
        if (errorText != null) {
            binding.layoutErrorState.textErrorMessage.setText(errorText);
        }
    }

    private void renderFlagged() {
        if (binding == null) {
            return;
        }
        List<Listing> data = viewModel.getFlagged().getValue();
        flaggedAdapter.setData(data == null ? new ArrayList<Listing>() : data);
        boolean isEmpty = data != null && data.isEmpty();
        boolean failed = data == null && flaggedError != null;
        binding.recyclerFlagged.setVisibility(data != null && !isEmpty ? View.VISIBLE : View.GONE);
        binding.layoutFlaggedEmpty.getRoot().setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.layoutFlaggedError.getRoot().setVisibility(failed ? View.VISIBLE : View.GONE);
        binding.progressFlagged.setVisibility(data == null && !failed ? View.VISIBLE : View.GONE);
        binding.layoutFlaggedEmpty.textEmptyTitle.setText(R.string.admin_moderation_flagged_empty_title);
        binding.layoutFlaggedEmpty.textEmptyMessage.setText(R.string.admin_moderation_flagged_empty_message);
        if (failed) {
            binding.layoutFlaggedError.textErrorMessage.setText(flaggedError);
        }
        renderConnection();
    }

    private void renderKeywords() {
        if (binding == null) {
            return;
        }
        List<BannedKeyword> data = viewModel.getKeywords().getValue();
        keywordAdapter.setData(data == null ? new ArrayList<BannedKeyword>() : data);
        boolean isEmpty = data != null && data.isEmpty();
        boolean failed = data == null && keywordsError != null;
        binding.recyclerKeywords.setVisibility(data != null && !isEmpty ? View.VISIBLE : View.GONE);
        binding.layoutKeywordsEmpty.getRoot().setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.layoutKeywordsError.getRoot().setVisibility(failed ? View.VISIBLE : View.GONE);
        binding.progressKeywords.setVisibility(data == null && !failed ? View.VISIBLE : View.GONE);
        binding.layoutKeywordsEmpty.textEmptyTitle.setText(R.string.admin_moderation_keyword_empty_title);
        binding.layoutKeywordsEmpty.textEmptyMessage.setText(R.string.admin_moderation_keyword_empty_message);
        if (failed) {
            binding.layoutKeywordsError.textErrorMessage.setText(keywordsError);
        }
        renderConnection();
    }

    private boolean isOnline() {
        try {
            return NetworkMonitor.getInstance().isOnline();
        } catch (UnsupportedOperationException notReady) {
            // NetworkMonitor is still a skeleton; assume online and let the server answer decide.
            return true;
        }
    }

    // Buttons stay locked while offline or while a request runs.
    private void renderConnection() {
        if (binding == null) {
            return;
        }
        boolean online = isOnline();
        boolean busy = Boolean.TRUE.equals(viewModel.getLoading().getValue());
        binding.layoutOfflineBanner.getRoot().setVisibility(online ? View.GONE : View.VISIBLE);
        adapter.setActionsEnabled(online && !busy);
        flaggedAdapter.setActionsEnabled(online && !busy);
        keywordAdapter.setActionsEnabled(online && !busy);
        binding.buttonAddKeyword.setEnabled(online && !busy);
        reasonDialog.setConfirmEnabled(online && !busy);
    }

    private void showError(@Nullable String message) {
        if (message == null || message.isEmpty() || binding == null) {
            return;
        }
        viewModel.clearError();
        if (reasonDialog.isOpen()) {
            reasonDialog.showError(message);
            return;
        }
        if (addingKeyword) {
            addingKeyword = false;
            binding.inputKeyword.setError(message);
            return;
        }
        if (selectedTab == TAB_FLAGGED && viewModel.getFlagged().getValue() == null) {
            flaggedError = message;
            renderFlagged();
            return;
        }
        if (selectedTab == TAB_KEYWORDS && viewModel.getKeywords().getValue() == null) {
            keywordsError = message;
            renderKeywords();
            return;
        }
        if (viewModel.getReports().getValue() == null || awaitingResult) {
            awaitingResult = false;
            errorText = message;
            render();
            return;
        }
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
    }

    private void onActionDone() {
        viewModel.clearDone();
        reasonDialog.close();
        if (binding != null) {
            if (addingKeyword) {
                addingKeyword = false;
                binding.editKeyword.setText("");
            }
            Snackbar.make(binding.getRoot(), successMessage, Snackbar.LENGTH_SHORT).show();
        }
    }

    private void showHideDialog(String listingId, String text) {
        dialogTargetId = listingId;
        reasonDialog.show(R.string.admin_moderation_hide_title, R.string.admin_moderation_hide_message, true, text);
        renderConnection();
    }

    private void confirmHide(String reason) {
        if (dialogTargetId == null) {
            return;
        }
        successMessage = R.string.admin_moderation_done_hidden;
        viewModel.hide(dialogTargetId, reason);
    }
}
