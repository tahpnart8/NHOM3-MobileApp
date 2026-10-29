package com.nhom3.pubgapp.ui.admin;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
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
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.Timestamp;
import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.databinding.DialogAdminReportEvidenceBinding;
import com.nhom3.pubgapp.databinding.FragmentAdminModerationBinding;
import com.nhom3.pubgapp.model.BannedKeyword;
import com.nhom3.pubgapp.model.ChatMessage;
import com.nhom3.pubgapp.model.Listing;
import com.nhom3.pubgapp.model.Report;
import com.nhom3.pubgapp.model.enums.MessageType;
import com.nhom3.pubgapp.model.enums.ReportStatus;
import com.nhom3.pubgapp.model.enums.ReportTarget;
import com.nhom3.pubgapp.ui.common.ImageLoader;
import com.nhom3.pubgapp.util.NetworkMonitor;
import com.nhom3.pubgapp.viewmodel.AdminContentViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
    private static final String STATE_EVIDENCE_TYPE = "state_evidence_type";
    private static final String STATE_EVIDENCE_TARGET = "state_evidence_target";
    // Chat lines kept on each side of the reported message: enough context, and no more of the private chat.
    static final int MESSAGES_AROUND = 5;

    @Nullable
    private FragmentAdminModerationBinding binding;
    private AdminContentViewModel viewModel;
    private AdminReportAdapter adapter;
    private AdminFlaggedAdapter flaggedAdapter;
    private AdminKeywordAdapter keywordAdapter;
    private AdminReasonDialog reasonDialog;
    private ReportEvidenceDialog evidenceDialog;
    // The target whose evidence dialog is open, kept so a rotation shows it again.
    @Nullable
    private ReportTarget evidenceType;
    @Nullable
    private String evidenceTargetId;
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
    // True after the admin left this screen (another screen or the home button), so the flagged tab reloads.
    private boolean leftScreen;
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
            String evidenceTypeName = savedInstanceState.getString(STATE_EVIDENCE_TYPE);
            evidenceType = evidenceTypeName == null ? null : ReportTarget.valueOf(evidenceTypeName);
            evidenceTargetId = savedInstanceState.getString(STATE_EVIDENCE_TARGET);
        }
        evidenceDialog = new ReportEvidenceDialog();
        setupToolbar();
        setupFilters();
        setupList();
        setupFlaggedList();
        setupKeywordList();
        setupKeywordInput();
        setupTabs();
        observeViewModel();
        observeEvidence();
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
        if (evidenceTargetId != null) {
            showEvidenceDialog();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        renderConnection();
        refreshFlaggedAfterLeaving();
    }

    // A rotation is not leaving the screen; the ViewModel still holds the lists then.
    @Override
    public void onPause() {
        super.onPause();
        if (!requireActivity().isChangingConfigurations()) {
            leftScreen = true;
        }
    }

    // getFlaggedListings has no live version, so the flagged tab is read again when the admin comes back.
    private void refreshFlaggedAfterLeaving() {
        if (!leftScreen) {
            return;
        }
        leftScreen = false;
        if (viewModel.getFlagged().getValue() != null) {
            viewModel.loadFlagged();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_TAB, selectedTab);
        outState.putString(STATE_REMOVE_ID, removeDialog == null ? null : removeKeywordId);
        outState.putString(STATE_REMOVE_WORD, removeDialog == null ? null : removeKeywordWord);
        outState.putString(STATE_STATUS, selectedStatus.name());
        outState.putString(STATE_TYPE, selectedType == null ? null : selectedType.name());
        outState.putString(STATE_EVIDENCE_TYPE, evidenceType == null ? null : evidenceType.name());
        outState.putString(STATE_EVIDENCE_TARGET, evidenceTargetId);
        if (reasonDialog != null && reasonDialog.isOpen()) {
            outState.putString(STATE_DIALOG_TARGET, dialogTargetId);
            outState.putString(STATE_DIALOG_TEXT, reasonDialog.readText());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        reasonDialog.release();
        evidenceDialog.release();
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

            @Override
            public void onViewEvidence(AdminReportGrouping.Group group) {
                openEvidence(group);
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

    private void observeEvidence() {
        viewModel.getReportEvidence().observe(getViewLifecycleOwner(), new Observer<List<byte[]>>() {
            @Override
            public void onChanged(List<byte[]> images) {
                evidenceDialog.showImages(images, evidenceTargetId);
            }
        });
        viewModel.getReportedMessages().observe(getViewLifecycleOwner(), new Observer<List<ChatMessage>>() {
            @Override
            public void onChanged(List<ChatMessage> messages) {
                showEvidenceMessages(messages);
            }
        });
    }

    // Every report of the group counts: their images together, and the first conversation one of them names.
    private void openEvidence(AdminReportGrouping.Group group) {
        evidenceType = group.getTargetType();
        evidenceTargetId = group.getTargetId();
        Report combined = evidenceOf(viewModel.getReports().getValue(), evidenceType, evidenceTargetId);
        viewModel.loadReportEvidence(combined);
        viewModel.loadReportedConversation(combined.getConversationId());
        showEvidenceDialog();
    }

    private void showEvidenceDialog() {
        evidenceDialog.show(requireContext(), getLayoutInflater(), evidenceType == ReportTarget.MESSAGE,
                new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        evidenceType = null;
                        evidenceTargetId = null;
                    }
                });
        evidenceDialog.showImages(viewModel.getReportEvidence().getValue(), evidenceTargetId);
        showEvidenceMessages(viewModel.getReportedMessages().getValue());
    }

    private void showEvidenceMessages(@Nullable List<ChatMessage> messages) {
        if (messages == null) {
            evidenceDialog.showMessages(null, evidenceTargetId);
            return;
        }
        evidenceDialog.showMessages(messagesAround(messages, evidenceTargetId, MESSAGES_AROUND), evidenceTargetId);
    }

    /** One report holding the evidence of every report on this target, for loadReportEvidence. */
    @NonNull
    static Report evidenceOf(@Nullable List<Report> reports, @Nullable ReportTarget type, @Nullable String targetId) {
        Report combined = new Report();
        combined.setTargetType(type);
        combined.setTargetId(targetId);
        List<String> imageIds = new ArrayList<String>();
        combined.setEvidenceImageIds(imageIds);
        if (reports == null || targetId == null) {
            return combined;
        }
        for (Report report : reports) {
            if (report.getTargetType() != type || !targetId.equals(report.getTargetId())) {
                continue;
            }
            addMissing(imageIds, report.getEvidenceImageIds());
            String conversationId = report.getConversationId();
            boolean hasConversation = conversationId != null && !conversationId.isEmpty();
            if (hasConversation && combined.getConversationId() == null) {
                combined.setConversationId(conversationId);
            }
        }
        return combined;
    }

    private static void addMissing(List<String> target, @Nullable List<String> source) {
        if (source == null) {
            return;
        }
        for (String id : source) {
            if (id != null && !target.contains(id)) {
                target.add(id);
            }
        }
    }

    /** The reported message with up to {@code around} messages on each side; the last ones if it is not found. */
    @NonNull
    static List<ChatMessage> messagesAround(@Nullable List<ChatMessage> messages, @Nullable String messageId,
            int around) {
        List<ChatMessage> result = new ArrayList<ChatMessage>();
        if (messages == null || messages.isEmpty()) {
            return result;
        }
        int center = messages.size() - 1;
        for (int index = 0; index < messages.size(); index++) {
            String id = messages.get(index).getId();
            if (id != null && id.equals(messageId)) {
                center = index;
                break;
            }
        }
        int from = Math.max(0, center - around);
        int to = Math.min(messages.size() - 1, center + around);
        for (int index = from; index <= to; index++) {
            result.add(messages.get(index));
        }
        return result;
    }

    /** The E04 evidence dialog: images of the reports and, for a message report, the chat around that message. */
    private static final class ReportEvidenceDialog {

        private final EvidenceImageAdapter imageAdapter = new EvidenceImageAdapter();
        private final ReportedMessageAdapter messageAdapter = new ReportedMessageAdapter();
        @Nullable
        private AlertDialog dialog;
        @Nullable
        private DialogAdminReportEvidenceBinding binding;

        void show(Context context, LayoutInflater inflater, boolean withChat,
                final DialogInterface.OnDismissListener onDismiss) {
            release();
            binding = DialogAdminReportEvidenceBinding.inflate(inflater);
            binding.recyclerEvidenceImages.setLayoutManager(
                    new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
            binding.recyclerEvidenceImages.setAdapter(imageAdapter);
            binding.recyclerMessages.setLayoutManager(new LinearLayoutManager(context));
            binding.recyclerMessages.setAdapter(messageAdapter);
            binding.layoutConversation.setVisibility(withChat ? View.VISIBLE : View.GONE);
            dialog = new MaterialAlertDialogBuilder(context)
                    .setTitle(R.string.admin_report_evidence_title)
                    .setView(binding.getRoot())
                    .setPositiveButton(R.string.admin_report_evidence_close, null)
                    .create();
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface closed) {
                    dialog = null;
                    binding = null;
                    onDismiss.onDismiss(closed);
                }
            });
            dialog.show();
        }

        // Null means still reading, so the spinner stays; an empty list says the report has no image.
        void showImages(@Nullable List<byte[]> images, @Nullable String targetId) {
            if (binding == null) {
                return;
            }
            boolean reading = images == null;
            boolean hasImages = !reading && !images.isEmpty();
            binding.progressEvidenceImages.setVisibility(reading ? View.VISIBLE : View.GONE);
            binding.recyclerEvidenceImages.setVisibility(hasImages ? View.VISIBLE : View.GONE);
            binding.textNoEvidenceImages.setVisibility(!reading && !hasImages ? View.VISIBLE : View.GONE);
            imageAdapter.submit(images, targetId);
        }

        void showMessages(@Nullable List<ChatMessage> messages, @Nullable String reportedId) {
            if (binding == null) {
                return;
            }
            boolean reading = messages == null;
            boolean hasMessages = !reading && !messages.isEmpty();
            binding.progressConversation.setVisibility(reading ? View.VISIBLE : View.GONE);
            binding.recyclerMessages.setVisibility(hasMessages ? View.VISIBLE : View.GONE);
            binding.textNoMessages.setVisibility(!reading && !hasMessages ? View.VISIBLE : View.GONE);
            messageAdapter.submit(messages, reportedId);
        }

        // Closing without the listener keeps the open target, so a rotation shows the dialog again.
        void release() {
            if (dialog != null) {
                dialog.setOnDismissListener(null);
                dialog.dismiss();
            }
            dialog = null;
            binding = null;
        }
    }

    /** Decoded evidence images; an image that could not be read shows the error icon. */
    private static final class EvidenceImageAdapter extends RecyclerView.Adapter<EvidenceImageAdapter.ImageHolder> {

        private final List<byte[]> images = new ArrayList<byte[]>();
        private String tagPrefix = "";

        void submit(@Nullable List<byte[]> newImages, @Nullable String targetId) {
            images.clear();
            if (newImages != null) {
                images.addAll(newImages);
            }
            tagPrefix = targetId == null ? "" : targetId;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ImageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ImageView image = new ImageView(parent.getContext());
            int size = parent.getResources().getDimensionPixelSize(R.dimen.avatar_size_lg);
            int gap = parent.getResources().getDimensionPixelSize(R.dimen.space_sm);
            RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(size, size);
            params.setMarginEnd(gap);
            image.setLayoutParams(params);
            image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            image.setContentDescription(parent.getContext().getString(R.string.admin_report_evidence_image));
            return new ImageHolder(image);
        }

        // The loader needs a tag per image; the bytes carry no id, so the target id and position name it.
        @Override
        public void onBindViewHolder(@NonNull ImageHolder holder, int position) {
            byte[] bytes = images.get(position);
            if (bytes == null || bytes.length == 0) {
                holder.image.setTag(null);
                holder.image.setImageResource(R.drawable.ic_error);
                return;
            }
            String tag = tagPrefix + ":" + position;
            ImageLoader.load(bytes, tag, holder.image, R.drawable.ic_inbox, R.drawable.ic_error);
        }

        @Override
        public int getItemCount() {
            return images.size();
        }

        static final class ImageHolder extends RecyclerView.ViewHolder {
            final ImageView image;

            ImageHolder(ImageView image) {
                super(image);
                this.image = image;
            }
        }
    }

    /** Lines of the reported chat: time and content only, so no more about the users is shown than needed. */
    private static final class ReportedMessageAdapter
            extends RecyclerView.Adapter<ReportedMessageAdapter.MessageHolder> {

        private static final String TIME_PATTERN = "dd/MM HH:mm";

        private final List<ChatMessage> messages = new ArrayList<ChatMessage>();
        @Nullable
        private String reportedId;

        void submit(@Nullable List<ChatMessage> newMessages, @Nullable String newReportedId) {
            messages.clear();
            if (newMessages != null) {
                messages.addAll(newMessages);
            }
            reportedId = newReportedId;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public MessageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            TextView text = new TextView(parent.getContext());
            text.setLayoutParams(new RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            int gap = parent.getResources().getDimensionPixelSize(R.dimen.space_xs);
            text.setPadding(0, gap, 0, gap);
            text.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyMedium);
            text.setTextColor(MaterialColors.getColor(parent, com.google.android.material.R.attr.colorOnSurface));
            return new MessageHolder(text);
        }

        @Override
        public void onBindViewHolder(@NonNull MessageHolder holder, int position) {
            ChatMessage message = messages.get(position);
            Context context = holder.text.getContext();
            String line = context.getString(R.string.admin_report_evidence_message_line,
                    timeOf(message.getCreatedAt()), contentOf(context, message));
            boolean reported = message.getId() != null && message.getId().equals(reportedId);
            if (reported) {
                line = context.getString(R.string.admin_report_evidence_message_reported, line);
            }
            holder.text.setText(line);
            holder.text.setTypeface(null, reported ? Typeface.BOLD : Typeface.NORMAL);
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }

        private static String timeOf(@Nullable Timestamp createdAt) {
            if (createdAt == null) {
                return "";
            }
            SimpleDateFormat format = new SimpleDateFormat(TIME_PATTERN, Locale.ROOT);
            return format.format(createdAt.toDate());
        }

        private static String contentOf(Context context, ChatMessage message) {
            if (message.getType() == MessageType.IMAGE) {
                return context.getString(R.string.admin_report_evidence_message_image);
            }
            if (message.getType() == MessageType.OFFER) {
                return context.getString(R.string.admin_report_evidence_message_offer);
            }
            return message.getText();
        }

        static final class MessageHolder extends RecyclerView.ViewHolder {
            final TextView text;

            MessageHolder(TextView text) {
                super(text);
                this.text = text;
            }
        }
    }
}
