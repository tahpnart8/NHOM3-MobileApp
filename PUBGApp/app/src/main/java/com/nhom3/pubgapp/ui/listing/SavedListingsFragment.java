package com.nhom3.pubgapp.ui.listing;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.android.material.snackbar.Snackbar;
import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.databinding.FragmentSavedListingsBinding;
import com.nhom3.pubgapp.model.Listing;
import com.nhom3.pubgapp.util.NetworkMonitor;
import com.nhom3.pubgapp.util.SessionManager;
import com.nhom3.pubgapp.viewmodel.ProfileViewModel;

import java.util.List;

/** B06: the signed-in user's saved listings in a two-column grid, with unsave. */
public class SavedListingsFragment extends Fragment {

    private static final int GRID_COLUMNS = 2;

    private FragmentSavedListingsBinding binding;
    private ProfileViewModel profileViewModel;
    private SavedListingAdapter savedAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentSavedListingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (!SessionManager.getInstance().isLoggedIn()) {
            NavHostFragment.findNavController(this).navigate(R.id.action_global_sign_in);
            return;
        }
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        setUpToolbar();
        setUpGrid();
        setUpStates();
        observeViewModel();
        // Read again every time the screen is shown, so a save or unsave made on the listing detail appears here.
        // The old list stays visible while the new one loads.
        profileViewModel.loadSaved();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (binding == null || profileViewModel == null) {
            return;
        }
        boolean online = NetworkMonitor.getInstance().isOnline();
        binding.includeOfflineBanner.getRoot().setVisibility(online ? View.GONE : View.VISIBLE);
        binding.textOfflineHint.setVisibility(online ? View.GONE : View.VISIBLE);
        savedAdapter.notifyDataSetChanged();
    }

    private void setUpToolbar() {
        binding.includeToolbar.textTitle.setText(R.string.listing_saved_title);
        binding.includeToolbar.buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View clicked) {
                NavHostFragment.findNavController(SavedListingsFragment.this).navigateUp();
            }
        });
    }

    private void setUpGrid() {
        savedAdapter = new SavedListingAdapter(profileViewModel);
        binding.recyclerSaved.setLayoutManager(new GridLayoutManager(requireContext(), GRID_COLUMNS));
        binding.recyclerSaved.setAdapter(savedAdapter);
    }

    private void setUpStates() {
        binding.includeEmptyState.textEmptyTitle.setText(R.string.listing_saved_empty_title);
        binding.includeEmptyState.textEmptyMessage.setText(R.string.listing_saved_empty_message);
        binding.includeEmptyState.buttonEmptyAction.setText(R.string.listing_saved_go_home);
        binding.includeEmptyState.buttonEmptyAction.setVisibility(View.VISIBLE);
        binding.includeEmptyState.buttonEmptyAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View clicked) {
                goHome();
            }
        });
        binding.includeErrorState.buttonRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View clicked) {
                profileViewModel.loadSaved();
            }
        });
    }

    // nav_main.xml has no action_global_tab; a tab is reached by its destination id.
    private void goHome() {
        NavOptions options = new NavOptions.Builder()
                .setPopUpTo(R.id.homeFragment, false)
                .setLaunchSingleTop(true)
                .build();
        NavHostFragment.findNavController(this).navigate(R.id.homeFragment, null, options);
    }

    private void observeViewModel() {
        profileViewModel.getSavedListings().observe(getViewLifecycleOwner(), new Observer<List<Listing>>() {
            @Override
            public void onChanged(List<Listing> listings) {
                savedAdapter.submitList(listings);
                updateScreenState();
            }
        });
        profileViewModel.getLoading().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean loading) {
                updateScreenState();
            }
        });
        profileViewModel.getError().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String message) {
                showError(message);
            }
        });
        profileViewModel.getDone().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean finished) {
                if (!Boolean.TRUE.equals(finished)) {
                    return;
                }
                profileViewModel.clearDone();
                Snackbar.make(binding.getRoot(), R.string.listing_saved_unsaved, Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void updateScreenState() {
        List<Listing> listings = profileViewModel.getSavedListings().getValue();
        boolean loading = Boolean.TRUE.equals(profileViewModel.getLoading().getValue());
        boolean loaded = listings != null;
        boolean empty = loaded && listings.isEmpty();
        binding.progressLoading.setVisibility(loading && !loaded ? View.VISIBLE : View.GONE);
        binding.recyclerSaved.setVisibility(loaded && !empty ? View.VISIBLE : View.GONE);
        binding.includeEmptyState.getRoot().setVisibility(empty && !loading ? View.VISIBLE : View.GONE);
        // Nothing loaded and nothing loading means the last read failed, also after a rotation.
        binding.includeErrorState.getRoot().setVisibility(!loaded && !loading ? View.VISIBLE : View.GONE);
        binding.textSavedCount.setVisibility(loaded && !empty ? View.VISIBLE : View.GONE);
        if (loaded) {
            binding.textSavedCount.setText(getString(R.string.listing_saved_count_text, listings.size()));
        }
    }

    private void showError(String message) {
        if (message == null || message.isEmpty()) {
            return;
        }
        if (profileViewModel.getSavedListings().getValue() == null) {
            binding.includeErrorState.textErrorMessage.setText(message);
            binding.includeErrorState.getRoot().setVisibility(View.VISIBLE);
        } else {
            Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
        }
        profileViewModel.clearError();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // The view is gone; keeping the binding would leak it.
        binding = null;
    }
}
