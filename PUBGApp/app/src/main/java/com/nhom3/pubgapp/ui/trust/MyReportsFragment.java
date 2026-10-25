package com.nhom3.pubgapp.ui.trust;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.databinding.DialogComplaintDetailBinding;
import com.nhom3.pubgapp.databinding.FragmentMyReportsBinding;
import com.nhom3.pubgapp.databinding.ViewEmptyStateBinding;
import com.nhom3.pubgapp.model.Complaint;
import com.nhom3.pubgapp.model.Report;
import com.nhom3.pubgapp.util.NetworkMonitor;
import com.nhom3.pubgapp.viewmodel.TrustViewModel;

import java.io.File;
import java.util.List;

/** D07: the reports and complaints the signed-in user has sent. */
public class MyReportsFragment extends Fragment {

    private static final String STATE_SELECTED_TAB = "selected_tab";
    private static final int TAB_REPORTS = 0;
    private static final String STATE_OPEN_COMPLAINT = "open_complaint";
    // Argument name of the deal detail destination in nav_main.xml.
    private static final String ARG_DEAL_ID = "dealId";
    private static final String STATE_SHARED_EXPORT = "shared_export";
    // Matches android:authorities="${applicationId}.fileprovider" in the manifest.
    private static final String FILE_PROVIDER_SUFFIX = ".fileprovider";
    private static final String CSV_MIME_TYPE = "text/csv";

    @Nullable
    private FragmentMyReportsBinding binding;
    private int selectedTab = TAB_REPORTS;
    @Nullable
    private String sharedExportPath;
    private TrustViewModel trustViewModel;
    private MyReportAdapter reportAdapter;
    private MyComplaintAdapter complaintAdapter;
    // The complaint whose detail is open, kept so a rotation opens it again.
    @Nullable
    private String openComplaintId;
    // Set when a read fails before any complaint arrived, so the tab shows the error instead of the spinner.
    private boolean complaintsFailed;
    @Nullable
    private AlertDialog complaintDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentMyReportsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState != null) {
            selectedTab = savedInstanceState.getInt(STATE_SELECTED_TAB, TAB_REPORTS);
            sharedExportPath = savedInstanceState.getString(STATE_SHARED_EXPORT);
            openComplaintId = savedInstanceState.getString(STATE_OPEN_COMPLAINT);
        }
        trustViewModel = new ViewModelProvider(this).get(TrustViewModel.class);
        setUpToolbar();
        setUpTabs();
        setUpReportList();
        setUpComplaintList();
        observeReports();
        observeComplaints();
        observeExport();
        trustViewModel.loadMyReports();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (binding == null) {
            return;
        }
        boolean online = NetworkMonitor.getInstance().isOnline();
        binding.includeOfflineBanner.getRoot().setVisibility(online ? View.GONE : View.VISIBLE);
    }

    // The selected tab is a view choice, so it is kept in the saved state rather than the ViewModel.
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_TAB, selectedTab);
        outState.putString(STATE_SHARED_EXPORT, sharedExportPath);
        outState.putString(STATE_OPEN_COMPLAINT, openComplaintId);
    }

    private void setUpToolbar() {
        binding.includeToolbar.textTitle.setText(R.string.profile_my_reports_title);
        binding.includeToolbar.buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View clicked) {
                NavHostFragment.findNavController(MyReportsFragment.this).navigateUp();
            }
        });
    }

    private void setUpTabs() {
        TabLayout.Tab tab = binding.tabLayoutMyReports.getTabAt(selectedTab);
        if (tab != null) {
            tab.select();
        }
        showTab(selectedTab);
        binding.tabLayoutMyReports.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab selected) {
                selectedTab = selected.getPosition();
                showTab(selectedTab);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab unselected) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab reselected) {
            }
        });
    }

    private void setUpReportList() {
        reportAdapter = new MyReportAdapter(new MyReportAdapter.CancelListener() {
            @Override
            public void onCancel(Report report) {
                confirmCancel(report);
            }
        });
        binding.includeReports.recyclerReports.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.includeReports.recyclerReports.setAdapter(reportAdapter);
        binding.includeReports.includeReportsEmpty.textEmptyTitle.setText(R.string.profile_my_reports_empty_title);
        binding.includeReports.includeReportsEmpty.textEmptyMessage.setText(R.string.profile_my_reports_empty_message);
        binding.includeReports.includeReportsEmpty.buttonEmptyAction.setVisibility(View.GONE);
        binding.includeReports.includeReportsError.buttonRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View clicked) {
                trustViewModel.loadMyReports();
            }
        });
        binding.includeReports.buttonExportReports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View clicked) {
                trustViewModel.exportReports();
            }
        });
    }

    private void setUpComplaintList() {
        complaintAdapter = new MyComplaintAdapter(new MyComplaintAdapter.ComplaintListener() {
            @Override
            public void onOpen(Complaint complaint) {
                openComplaint(complaint);
            }

            @Override
            public void onWithdraw(Complaint complaint) {
                confirmWithdraw(complaint);
            }
        });
        binding.includeComplaints.recyclerComplaints.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.includeComplaints.recyclerComplaints.setAdapter(complaintAdapter);
        ViewEmptyStateBinding empty = binding.includeComplaints.includeComplaintsEmpty;
        empty.textEmptyTitle.setText(R.string.profile_my_complaints_empty_title);
        empty.textEmptyMessage.setText(R.string.profile_my_complaints_empty_message);
        empty.buttonEmptyAction.setVisibility(View.GONE);
        binding.includeComplaints.includeComplaintsError.buttonRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View clicked) {
                complaintsFailed = false;
                trustViewModel.loadMyComplaints();
                updateComplaintsState();
            }
        });
    }

    // Withdrawing gives the deal back its earlier status and cannot be undone (D07), so the user confirms it.
    private void confirmWithdraw(final Complaint complaint) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.profile_my_complaints_withdraw_title)
                .setMessage(R.string.profile_my_complaints_withdraw_message)
                .setNegativeButton(R.string.profile_my_reports_cancel_keep, null)
                .setPositiveButton(R.string.profile_my_complaints_withdraw, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        trustViewModel.withdrawComplaint(complaint.getId());
                    }
                })
                .show();
    }

    private void openComplaint(Complaint complaint) {
        openComplaintId = complaint.getId();
        trustViewModel.loadComplaint(complaint.getId());
    }

    // The ViewModel keeps the last file, so a file already shared is not shared again after a rotation.
    private void observeExport() {
        trustViewModel.getExportedFile().observe(getViewLifecycleOwner(), new Observer<File>() {
            @Override
            public void onChanged(File file) {
                if (file == null || file.getAbsolutePath().equals(sharedExportPath)) {
                    return;
                }
                sharedExportPath = file.getAbsolutePath();
                shareCsv(file);
            }
        });
    }

    private void shareCsv(File file) {
        Uri uri = FileProvider.getUriForFile(requireContext(),
                requireContext().getPackageName() + FILE_PROVIDER_SUFFIX, file);
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType(CSV_MIME_TYPE);
        send.putExtra(Intent.EXTRA_STREAM, uri);
        send.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            startActivity(Intent.createChooser(send, getString(R.string.profile_my_reports_export_chooser)));
        } catch (ActivityNotFoundException noApp) {
            Snackbar.make(binding.getRoot(), R.string.profile_my_reports_export_no_app, Snackbar.LENGTH_LONG).show();
        }
    }

    // Cancelling cannot be undone, so the user confirms it first (D07).
    private void confirmCancel(final Report report) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.profile_my_reports_cancel_title)
                .setMessage(R.string.profile_my_reports_cancel_message)
                .setNegativeButton(R.string.profile_my_reports_cancel_keep, null)
                .setPositiveButton(R.string.profile_my_reports_cancel_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        trustViewModel.cancelReport(report.getId());
                    }
                })
                .show();
    }

    private void observeReports() {
        trustViewModel.getReports().observe(getViewLifecycleOwner(), new Observer<List<Report>>() {
            @Override
            public void onChanged(List<Report> reports) {
                reportAdapter.submitList(reports);
                updateReportsState();
            }
        });
        trustViewModel.getLoading().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean loading) {
                updateReportsState();
                updateComplaintsState();
            }
        });
        trustViewModel.getError().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String message) {
                showError(message);
            }
        });
    }

    // Loading, list, empty and error all come from the ViewModel, so a rotation shows the same one.
    private void updateReportsState() {
        boolean loading = Boolean.TRUE.equals(trustViewModel.getLoading().getValue());
        List<Report> reports = trustViewModel.getReports().getValue();
        boolean hasList = reports != null;
        boolean empty = hasList && reports.isEmpty();
        binding.includeReports.progressReports.setVisibility(loading && !hasList ? View.VISIBLE : View.GONE);
        binding.includeReports.recyclerReports.setVisibility(hasList && !empty ? View.VISIBLE : View.GONE);
        binding.includeReports.includeReportsEmpty.getRoot().setVisibility(empty ? View.VISIBLE : View.GONE);
        boolean failed = !hasList && !loading;
        binding.includeReports.includeReportsError.getRoot().setVisibility(failed ? View.VISIBLE : View.GONE);
        binding.includeReports.buttonExportReports.setVisibility(hasList && !empty ? View.VISIBLE : View.GONE);
        binding.includeReports.buttonExportReports.setEnabled(!loading);
    }

    private void observeComplaints() {
        trustViewModel.getComplaints().observe(getViewLifecycleOwner(), new Observer<List<Complaint>>() {
            @Override
            public void onChanged(List<Complaint> complaints) {
                complaintAdapter.submitList(complaints);
                updateComplaintsState();
            }
        });
        trustViewModel.getSelectedComplaint().observe(getViewLifecycleOwner(), new Observer<Complaint>() {
            @Override
            public void onChanged(Complaint complaint) {
                showComplaintDetail(complaint);
            }
        });
    }

    // Shown only for the row the user opened; after a rotation openComplaintId brings the same one back.
    private void showComplaintDetail(@Nullable Complaint complaint) {
        if (complaint == null || openComplaintId == null || !openComplaintId.equals(complaint.getId())) {
            return;
        }
        dismissComplaintDialog();
        DialogComplaintDetailBinding detail = DialogComplaintDetailBinding.inflate(getLayoutInflater());
        MyComplaintAdapter.bindDetail(detail, complaint);
        final String dealId = complaint.getDealId();
        complaintDialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(detail.getRoot())
                .setPositiveButton(R.string.profile_my_complaints_close, null)
                .setNeutralButton(R.string.profile_my_complaints_view_deal, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        openDeal(dealId);
                    }
                })
                .create();
        complaintDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                openComplaintId = null;
                complaintDialog = null;
            }
        });
        complaintDialog.show();
    }

    // Closing without the listener keeps openComplaintId, so a rotation reopens the dialog.
    private void dismissComplaintDialog() {
        if (complaintDialog == null) {
            return;
        }
        complaintDialog.setOnDismissListener(null);
        complaintDialog.dismiss();
        complaintDialog = null;
    }

    // The complaint id is the deal id, and C03 is reached through its global action (R22).
    private void openDeal(@Nullable String dealId) {
        if (dealId == null || dealId.isEmpty()) {
            return;
        }
        Bundle args = new Bundle();
        args.putString(ARG_DEAL_ID, dealId);
        NavHostFragment.findNavController(this).navigate(R.id.action_global_deal_detail, args);
    }

    // Reports and complaints share one loading flag, so the complaints tab waits for its own list or failure.
    private void updateComplaintsState() {
        List<Complaint> complaints = trustViewModel.getComplaints().getValue();
        boolean hasList = complaints != null;
        boolean empty = hasList && complaints.isEmpty();
        boolean failed = !hasList && complaintsFailed;
        binding.includeComplaints.progressComplaints.setVisibility(!hasList && !failed ? View.VISIBLE : View.GONE);
        binding.includeComplaints.recyclerComplaints.setVisibility(hasList && !empty ? View.VISIBLE : View.GONE);
        binding.includeComplaints.includeComplaintsEmpty.getRoot().setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.includeComplaints.includeComplaintsError.getRoot().setVisibility(failed ? View.VISIBLE : View.GONE);
    }

    private void showError(String message) {
        if (message == null || message.isEmpty()) {
            return;
        }
        boolean shownInPlace = false;
        if (trustViewModel.getReports().getValue() == null) {
            binding.includeReports.includeReportsError.textErrorMessage.setText(message);
            shownInPlace = true;
        }
        if (selectedTab != TAB_REPORTS && trustViewModel.getComplaints().getValue() == null) {
            binding.includeComplaints.includeComplaintsError.textErrorMessage.setText(message);
            complaintsFailed = true;
            updateComplaintsState();
            shownInPlace = true;
        }
        if (!shownInPlace) {
            Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
        }
        trustViewModel.clearError();
    }

    private void showTab(int position) {
        boolean reports = position == TAB_REPORTS;
        binding.includeReports.getRoot().setVisibility(reports ? View.VISIBLE : View.GONE);
        binding.includeComplaints.getRoot().setVisibility(reports ? View.GONE : View.VISIBLE);
        // Complaints are read the first time their tab shows, so the two lists do not race for one flag.
        if (!reports) {
            trustViewModel.loadMyComplaints();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        dismissComplaintDialog();
        // Views are gone after this call, so the binding must not outlive them.
        binding = null;
    }
}
