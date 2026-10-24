package com.nhom3.pubgapp.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.snackbar.Snackbar;
import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.databinding.FragmentAdminListingDetailBinding;
import com.nhom3.pubgapp.model.Listing;
import com.nhom3.pubgapp.model.Report;
import com.nhom3.pubgapp.model.enums.ItemCondition;
import com.nhom3.pubgapp.model.enums.ListingStatus;
import com.nhom3.pubgapp.model.enums.ReportStatus;
import com.nhom3.pubgapp.model.enums.ReportTarget;
import com.nhom3.pubgapp.model.enums.TradeType;
import com.nhom3.pubgapp.ui.common.ImageLoader;
import com.nhom3.pubgapp.ui.common.StatusChips;
import com.nhom3.pubgapp.util.FormatUtils;
import com.nhom3.pubgapp.util.NetworkMonitor;
import com.nhom3.pubgapp.viewmodel.AdminContentViewModel;

import java.util.List;

// Admin view of one listing: read everything, then hide, restore or remove it.
public class AdminListingDetailFragment extends Fragment {

    private static final int ACTION_NONE = 0;
    private static final int ACTION_HIDE = 1;
    private static final int ACTION_REMOVE = 2;
    private static final int ACTION_RESTORE = 3;

    private static final String STATE_ACTION = "state_action";
    private static final String STATE_DIALOG_TEXT = "state_dialog_text";

    @Nullable
    private FragmentAdminListingDetailBinding binding;
    private AdminContentViewModel viewModel;
    private AdminReasonDialog reasonDialog;
    private final FormatUtils formatUtils = new FormatUtils();
    private String listingId = "";
    private int openAction = ACTION_NONE;
    @Nullable
    private String errorText;
    @StringRes
    private int successMessage = R.string.admin_listing_detail_done_hidden;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminListingDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle args = getArguments();
        String argId = args == null ? null : args.getString(AdminModerationFragment.ARG_LISTING_ID);
        listingId = argId == null ? "" : argId;
        viewModel = new ViewModelProvider(this).get(AdminContentViewModel.class);
        reasonDialog = new AdminReasonDialog(requireContext(), getLayoutInflater(),
                new AdminReasonDialog.OnConfirmListener() {
                    @Override
                    public void onConfirm(String reason) {
                        confirmAction(reason);
                    }
                });
        setupButtons();
        observeViewModel();
        if (viewModel.getSelectedListing().getValue() == null) {
            loadAll();
        }
        if (savedInstanceState != null) {
            int action = savedInstanceState.getInt(STATE_ACTION, ACTION_NONE);
            if (action != ACTION_NONE) {
                showActionDialog(action, savedInstanceState.getString(STATE_DIALOG_TEXT, ""));
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        render();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (reasonDialog != null && reasonDialog.isOpen()) {
            outState.putInt(STATE_ACTION, openAction);
            outState.putString(STATE_DIALOG_TEXT, reasonDialog.readText());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        reasonDialog.release();
        binding = null;
    }

    private void setupButtons() {
        binding.layoutToolbar.textTitle.setText(R.string.admin_listing_detail_title);
        binding.layoutToolbar.buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(AdminListingDetailFragment.this).navigateUp();
            }
        });
        binding.layoutErrorState.buttonRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadAll();
            }
        });
        binding.buttonViewSeller.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openSeller();
            }
        });
        binding.buttonHide.setOnClickListener(actionClick(ACTION_HIDE));
        binding.buttonRestore.setOnClickListener(actionClick(ACTION_RESTORE));
        binding.buttonRemove.setOnClickListener(actionClick(ACTION_REMOVE));
    }

    private View.OnClickListener actionClick(final int action) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showActionDialog(action, "");
            }
        };
    }

    private void observeViewModel() {
        viewModel.getSelectedListing().observe(getViewLifecycleOwner(), new Observer<Listing>() {
            @Override
            public void onChanged(Listing listing) {
                errorText = null;
                render();
            }
        });
        viewModel.getReports().observe(getViewLifecycleOwner(), new Observer<List<Report>>() {
            @Override
            public void onChanged(List<Report> reports) {
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

    private void loadAll() {
        errorText = null;
        viewModel.loadListing(listingId);
        viewModel.loadReports(ReportStatus.OPEN, ReportTarget.LISTING);
    }

    private void render() {
        if (binding == null) {
            return;
        }
        Listing listing = viewModel.getSelectedListing().getValue();
        boolean busy = Boolean.TRUE.equals(viewModel.getLoading().getValue());
        boolean online = isOnline();
        binding.layoutOfflineBanner.getRoot().setVisibility(online ? View.GONE : View.VISIBLE);
        binding.scrollContent.setVisibility(listing != null ? View.VISIBLE : View.GONE);
        boolean showError = listing == null && errorText != null;
        binding.layoutErrorState.getRoot().setVisibility(showError ? View.VISIBLE : View.GONE);
        binding.progressBar.setVisibility(listing == null && errorText == null ? View.VISIBLE : View.GONE);
        if (errorText != null) {
            binding.layoutErrorState.textErrorMessage.setText(errorText);
        }
        reasonDialog.setConfirmEnabled(online && !busy);
        if (listing != null) {
            bindListing(listing, online && !busy);
        }
    }

    private void bindListing(Listing listing, boolean canAct) {
        binding.textListingTitle.setText(listing.getTitle());
        binding.textPrice.setText(formatUtils.formatPrice(listing.getPrice()));
        binding.textDescription.setText(listing.getDescription());
        binding.textDetails.setText(detailsText(listing));
        ImageLoader.load(listing.getCoverThumb(), binding.imageCover, R.drawable.ic_inbox, R.drawable.ic_error);
        StatusChips.bind(binding.layoutStatusChip.textStatusChip, listing.getStatus());
        boolean hasReason = listing.getStatusReason() != null && !listing.getStatusReason().isEmpty();
        binding.textStatusReason.setVisibility(hasReason ? View.VISIBLE : View.GONE);
        if (hasReason) {
            binding.textStatusReason.setText(
                    getString(R.string.admin_listing_detail_status_reason, listing.getStatusReason()));
        }
        binding.textFlagged.setVisibility(listing.isFlagged() ? View.VISIBLE : View.GONE);
        binding.textReports.setText(reportsText(listing.getId()));
        binding.textSeller.setText(getString(R.string.admin_listing_detail_seller_id, listing.getSellerId()));
        bindActions(listing, canAct);
    }

    // Each button follows AdminListingActionRules; a sold or reserved listing locks all three with a hint.
    private void bindActions(Listing listing, boolean canAct) {
        boolean canRestore = AdminListingActionRules.canRestore(listing.getStatus(), listing.getStatusReason());
        boolean canHide = AdminListingActionRules.canHide(listing.getStatus());
        boolean canRemove = AdminListingActionRules.canRemove(listing.getStatus());
        boolean removed = listing.getStatus() == ListingStatus.REMOVED;
        binding.buttonHide.setVisibility(!removed && !canRestore ? View.VISIBLE : View.GONE);
        binding.buttonRestore.setVisibility(canRestore ? View.VISIBLE : View.GONE);
        binding.buttonRemove.setVisibility(removed ? View.GONE : View.VISIBLE);
        binding.buttonHide.setEnabled(canAct && canHide);
        binding.buttonRestore.setEnabled(canAct && canRestore);
        binding.buttonRemove.setEnabled(canAct && canRemove);
        boolean anyAllowed = canHide || canRestore || canRemove;
        binding.textActionHint.setVisibility(anyAllowed ? View.GONE : View.VISIBLE);
        binding.textActionHint.setText(removed ? R.string.admin_listing_detail_removed_hint
                : R.string.admin_listing_detail_locked_hint);
    }

    private String detailsText(Listing listing) {
        StringBuilder text = new StringBuilder();
        text.append(getString(R.string.admin_listing_detail_condition, conditionLabel(listing.getItemCondition())));
        text.append('\n').append(getString(R.string.admin_listing_detail_trade, tradeLabels(listing.getTradeTypes())));
        text.append('\n').append(getString(R.string.admin_listing_detail_province, orEmpty(listing.getProvince())));
        return text.toString();
    }

    private String orEmpty(@Nullable String text) {
        return text == null ? "" : text;
    }

    private String conditionLabel(@Nullable ItemCondition condition) {
        if (condition == null) {
            return "";
        }
        switch (condition) {
            case NEW:
                return getString(R.string.admin_listing_detail_condition_new);
            case LIKE_NEW:
                return getString(R.string.admin_listing_detail_condition_like_new);
            case USED:
                return getString(R.string.admin_listing_detail_condition_used);
            case REPAIRED:
                return getString(R.string.admin_listing_detail_condition_repaired);
            default:
                return getString(R.string.admin_listing_detail_condition_defective);
        }
    }

    private String tradeLabels(@Nullable List<TradeType> types) {
        StringBuilder text = new StringBuilder();
        if (types == null) {
            return "";
        }
        for (TradeType type : types) {
            if (text.length() > 0) {
                text.append(", ");
            }
            text.append(tradeLabel(type));
        }
        return text.toString();
    }

    private String tradeLabel(TradeType type) {
        if (type == TradeType.EXCHANGE) {
            return getString(R.string.admin_listing_detail_trade_exchange);
        }
        return getString(type == TradeType.GIVEAWAY ? R.string.admin_listing_detail_trade_giveaway
                : R.string.admin_listing_detail_trade_sell);
    }

    // The ViewModel holds every open listing report; only the ones for this listing are shown.
    private String reportsText(String id) {
        List<Report> reports = viewModel.getReports().getValue();
        StringBuilder text = new StringBuilder();
        if (reports != null) {
            for (Report report : reports) {
                if (id != null && id.equals(report.getTargetId()) && report.getReason() != null) {
                    appendReport(text, report);
                }
            }
        }
        return text.length() == 0 ? getString(R.string.admin_listing_detail_no_reports) : text.toString();
    }

    private void appendReport(StringBuilder text, Report report) {
        if (text.length() > 0) {
            text.append('\n');
        }
        text.append(getString(AdminReportAdapter.reasonLabel(report.getReason())));
        String description = report.getDescription();
        if (description != null && !description.trim().isEmpty()) {
            text.append(": ").append(description.trim());
        }
    }

    private void openSeller() {
        Listing listing = viewModel.getSelectedListing().getValue();
        if (listing == null || listing.getSellerId() == null) {
            return;
        }
        Bundle args = new Bundle();
        args.putString(AdminModerationFragment.ARG_USER_ID, listing.getSellerId());
        NavHostFragment.findNavController(this).navigate(R.id.adminUserDetailFragment, args);
    }

    private boolean isOnline() {
        try {
            return NetworkMonitor.getInstance().isOnline();
        } catch (UnsupportedOperationException notReady) {
            // NetworkMonitor is still a skeleton; assume online and let the server answer decide.
            return true;
        }
    }

    private void showActionDialog(int action, String text) {
        openAction = action;
        if (action == ACTION_HIDE) {
            reasonDialog.show(R.string.admin_listing_detail_hide_title,
                    R.string.admin_listing_detail_hide_message, true, text);
        } else if (action == ACTION_REMOVE) {
            reasonDialog.show(R.string.admin_listing_detail_remove_title,
                    R.string.admin_listing_detail_remove_message, true, text);
        } else {
            reasonDialog.show(R.string.admin_listing_detail_restore_title,
                    R.string.admin_listing_detail_restore_message, false, text);
        }
        render();
    }

    private void confirmAction(String reason) {
        if (openAction == ACTION_HIDE) {
            successMessage = R.string.admin_listing_detail_done_hidden;
            viewModel.hide(listingId, reason);
        } else if (openAction == ACTION_REMOVE) {
            successMessage = R.string.admin_listing_detail_done_removed;
            viewModel.remove(listingId, reason);
        } else if (openAction == ACTION_RESTORE) {
            successMessage = R.string.admin_listing_detail_done_restored;
            viewModel.restore(listingId);
        }
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
        if (viewModel.getSelectedListing().getValue() == null) {
            errorText = message;
            render();
            return;
        }
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
    }

    private void onActionDone() {
        viewModel.clearDone();
        reasonDialog.close();
        openAction = ACTION_NONE;
        getParentFragmentManager().setFragmentResult(AdminModerationFragment.RESULT_LISTING_CHANGED, new Bundle());
        if (binding != null) {
            Snackbar.make(binding.getRoot(), successMessage, Snackbar.LENGTH_SHORT).show();
        }
    }
}
