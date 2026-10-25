package com.nhom3.pubgapp.ui.trust;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.tabs.TabLayout;
import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.databinding.FragmentMyReportsBinding;
import com.nhom3.pubgapp.util.NetworkMonitor;

/** D07: the reports and complaints the signed-in user has sent. */
public class MyReportsFragment extends Fragment {

    private static final String STATE_SELECTED_TAB = "selected_tab";
    private static final int TAB_REPORTS = 0;

    @Nullable
    private FragmentMyReportsBinding binding;
    private int selectedTab = TAB_REPORTS;

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
        setUpToolbar();
        setUpTabs();
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
