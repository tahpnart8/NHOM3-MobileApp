package com.nhom3.pubgapp.ui.trust;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.databinding.FragmentMyReportsBinding;
import com.nhom3.pubgapp.model.Report;
import com.nhom3.pubgapp.util.NetworkMonitor;
import com.nhom3.pubgapp.viewmodel.TrustViewModel;

import java.util.List;

/** D07: the reports and complaints the signed-in user has sent. */
public class MyReportsFragment extends Fragment {

    private static final String STATE_SELECTED_TAB = "selected_tab";
    private static final int TAB_REPORTS = 0;

    @Nullable
    private FragmentMyReportsBinding binding;
    private int selectedTab = TAB_REPORTS;
    private TrustViewModel trustViewModel;
    private MyReportAdapter reportAdapter;

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
        }
        trustViewModel = new ViewModelProvider(this).get(TrustViewModel.class);
        setUpToolbar();
        setUpTabs();
        setUpReportList();
        observeReports();
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
        reportAdapter = new MyReportAdapter();
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
        binding.includeReports.includeReportsError.getRoot().setVisibility(!hasList && !loading ? View.VISIBLE : View.GONE);
    }

    private void showError(String message) {
        if (message == null || message.isEmpty()) {
            return;
        }
        if (trustViewModel.getReports().getValue() == null) {
            binding.includeReports.includeReportsError.textErrorMessage.setText(message);
        } else {
            Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
        }
        trustViewModel.clearError();
    }

    private void showTab(int position) {
        boolean reports = position == TAB_REPORTS;
        binding.includeReports.getRoot().setVisibility(reports ? View.VISIBLE : View.GONE);
        binding.includeComplaints.getRoot().setVisibility(reports ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Views are gone after this call, so the binding must not outlive them.
        binding = null;
    }
}
