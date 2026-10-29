package com.nhom3.pubgapp.ui.listing;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.databinding.FragmentHomeBinding;
import com.nhom3.pubgapp.model.Listing;
import com.nhom3.pubgapp.model.enums.SortOrder;
import com.nhom3.pubgapp.ui.main.MainActivity;
import com.nhom3.pubgapp.util.NetworkMonitor;
import com.nhom3.pubgapp.util.SessionManager;
import com.nhom3.pubgapp.viewmodel.AuthViewModel;
import com.nhom3.pubgapp.viewmodel.FeedViewModel;

import java.util.List;

/** Screen B01 Home: two-column listing grid with general and following channels. */
public class HomeFragment extends Fragment {

    private static final String KEY_FOLLOWING_CHANNEL = "isFollowingChannel";

    private FragmentHomeBinding binding;
    private FeedViewModel viewModel;
    private AuthViewModel authViewModel;
    private ListingCardAdapter adapter;
    private ListingCardAdapter forYouAdapter;
    private boolean isFollowingChannel = false;
    private boolean isInitialResume = true;

    /** Only displays guest sign-in header when definitely not logged in and not restoring session. */
    public static boolean shouldShowGuestSignIn(boolean loggedIn, boolean isRestoring) {
        if (loggedIn) {
            return false;
        }
        return !isRestoring;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(FeedViewModel.class);
        authViewModel = new ViewModelProvider(requireActivity())
                .get(MainActivity.RESTORE_VIEW_MODEL_KEY, AuthViewModel.class);

        if (savedInstanceState != null) {
            isFollowingChannel = savedInstanceState.getBoolean(KEY_FOLLOWING_CHANNEL, false);
            isInitialResume = false;
        } else {
            isInitialResume = true;
        }

        setupRecyclerView();
        setupForYouRecyclerView();
        setupTopHeader();
        setupSearchBar();
        setupChannelSelector();
        setupErrorState();
        observeViewModel();

        updateOfflineBanner();
        updateForYouVisibility();

        // Only load feed on initial creation so screen rotation preserves state
        if (viewModel.getFeed().getValue() == null) {
            viewModel.loadFeed(isFollowingChannel, SortOrder.NEWEST);
        }
        if (viewModel.getForYou().getValue() == null) {
            viewModel.loadForYou();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setupTopHeader();
        updateOfflineBanner();
        if (isInitialResume) {
            isInitialResume = false;
        } else {
            // The feed is read again so new listings show; For You only reloads once its kept copy is old.
            viewModel.loadFeed(isFollowingChannel, SortOrder.NEWEST);
            viewModel.loadForYou();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_FOLLOWING_CHANNEL, isFollowingChannel);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setupRecyclerView() {
        adapter = new ListingCardAdapter();
        final GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        binding.recyclerListings.setLayoutManager(layoutManager);
        binding.recyclerListings.setAdapter(adapter);

        // Endless scrolling: load next page when scrolled near bottom
        binding.recyclerListings.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy <= 0) {
                    return;
                }
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 4
                        && firstVisibleItemPosition >= 0) {
                    viewModel.loadNext();
                }
            }
        });
    }

    private void setupForYouRecyclerView() {
        forYouAdapter = new ListingCardAdapter() {
            @NonNull
            @Override
            public ListingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                ListingViewHolder holder = super.onCreateViewHolder(parent, viewType);
                ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
                if (lp != null) {
                    float density = parent.getResources().getDisplayMetrics().density;
                    lp.width = (int) (160f * density);
                    holder.itemView.setLayoutParams(lp);
                }
                return holder;
            }
        };
        LinearLayoutManager layoutManager = new LinearLayoutManager(
                getContext(), LinearLayoutManager.HORIZONTAL, false);
        binding.recyclerForYou.setLayoutManager(layoutManager);
        binding.recyclerForYou.setAdapter(forYouAdapter);
    }

    private void updateForYouVisibility() {
        if (binding == null) {
            return;
        }
        if (isFollowingChannel) {
            binding.layoutForYou.setVisibility(View.GONE);
            return;
        }
        List<Listing> forYou = viewModel.getForYou().getValue();
        if (forYou != null && !forYou.isEmpty()) {
            binding.layoutForYou.setVisibility(View.VISIBLE);
        } else {
            binding.layoutForYou.setVisibility(View.GONE);
        }
    }

    private void setupTopHeader() {
        if (binding == null) {
            return;
        }
        boolean loggedIn = SessionManager.getInstance().isLoggedIn();
        boolean isRestoring = false;
        if (authViewModel != null) {
            Boolean loading = authViewModel.getLoading().getValue();
            isRestoring = loading != null && loading.booleanValue();
        }
        if (shouldShowGuestSignIn(loggedIn, isRestoring)) {
            binding.layoutTopHeader.setVisibility(View.VISIBLE);
            binding.buttonSignIn.setVisibility(View.VISIBLE);
            binding.buttonSignIn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Navigation.findNavController(v).navigate(R.id.action_global_sign_in);
                }
            });
        } else {
            binding.layoutTopHeader.setVisibility(View.GONE);
            binding.buttonSignIn.setVisibility(View.GONE);
        }
    }

    private void setupSearchBar() {
        binding.cardSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(R.id.searchFragment);
            }
        });

        binding.buttonRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateOfflineBanner();
                viewModel.refresh();
            }
        });
    }

    private void setupChannelSelector() {
        if (isFollowingChannel) {
            binding.toggleChannels.check(R.id.buttonChannelFollowing);
        } else {
            binding.toggleChannels.check(R.id.buttonChannelGeneral);
        }

        binding.toggleChannels.addOnButtonCheckedListener(
                new MaterialButtonToggleGroup.OnButtonCheckedListener() {
                    @Override
                    public void onButtonChecked(MaterialButtonToggleGroup group,
                                                int checkedId, boolean isChecked) {
                        if (!isChecked) {
                            return;
                        }
                        if (checkedId == R.id.buttonChannelFollowing) {
                            if (!SessionManager.getInstance().isLoggedIn()) {
                                binding.toggleChannels.check(R.id.buttonChannelGeneral);
                                showGuestSignInDialog();
                                return;
                            }
                            if (!isFollowingChannel) {
                                isFollowingChannel = true;
                                updateForYouVisibility();
                                viewModel.loadFeed(true, SortOrder.NEWEST);
                            }
                        } else if (checkedId == R.id.buttonChannelGeneral) {
                            if (isFollowingChannel) {
                                isFollowingChannel = false;
                                updateForYouVisibility();
                                viewModel.loadFeed(false, SortOrder.NEWEST);
                            }
                        }
                    }
                });
    }

    private void setupErrorState() {
        binding.includeErrorState.buttonRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.includeErrorState.getRoot().setVisibility(View.GONE);
                updateOfflineBanner();
                viewModel.refresh();
            }
        });

        binding.includeEmptyState.buttonEmptyAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.toggleChannels.check(R.id.buttonChannelGeneral);
            }
        });
    }

    private void observeViewModel() {
        if (authViewModel != null) {
            authViewModel.getLoading().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
                @Override
                public void onChanged(Boolean loading) {
                    setupTopHeader();
                }
            });
            authViewModel.getLoggedIn().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
                @Override
                public void onChanged(Boolean loggedIn) {
                    setupTopHeader();
                }
            });
        }
        viewModel.getForYou().observe(getViewLifecycleOwner(), new Observer<List<Listing>>() {
            @Override
            public void onChanged(List<Listing> list) {
                if (forYouAdapter != null) {
                    forYouAdapter.submitList(list);
                }
                updateForYouVisibility();
            }
        });
        viewModel.getFeed().observe(getViewLifecycleOwner(), new Observer<List<Listing>>() {
            @Override
            public void onChanged(List<Listing> list) {
                updateOfflineBanner();
                adapter.submitList(list);

                if (list == null || list.isEmpty()) {
                    binding.recyclerListings.setVisibility(View.GONE);
                    binding.scrollEmptyState.setVisibility(View.VISIBLE);
                    binding.includeEmptyState.getRoot().setVisibility(View.VISIBLE);
                    binding.includeErrorState.getRoot().setVisibility(View.GONE);

                    if (isFollowingChannel) {
                        binding.includeEmptyState.textEmptyTitle.setText(
                                R.string.listing_home_following_empty_title);
                        binding.includeEmptyState.textEmptyMessage.setText(
                                R.string.listing_home_following_empty_message);
                        binding.includeEmptyState.buttonEmptyAction.setText(
                                R.string.listing_home_following_empty_action);
                        binding.includeEmptyState.buttonEmptyAction.setVisibility(View.VISIBLE);
                    } else {
                        binding.includeEmptyState.textEmptyTitle.setText(
                                R.string.listing_home_empty_title);
                        binding.includeEmptyState.textEmptyMessage.setText(
                                R.string.listing_home_empty_message);
                        binding.includeEmptyState.buttonEmptyAction.setVisibility(View.GONE);
                    }
                } else {
                    binding.recyclerListings.setVisibility(View.VISIBLE);
                    binding.scrollEmptyState.setVisibility(View.GONE);
                    binding.includeEmptyState.getRoot().setVisibility(View.GONE);
                    binding.includeErrorState.getRoot().setVisibility(View.GONE);
                }
            }
        });

        viewModel.getLoading().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean loading) {
                boolean isLoading = loading != null && loading;
                if (isLoading && adapter.getItemCount() == 0) {
                    binding.progressBar.setVisibility(View.VISIBLE);
                    binding.includeErrorState.getRoot().setVisibility(View.GONE);
                    binding.scrollEmptyState.setVisibility(View.GONE);
                    binding.includeEmptyState.getRoot().setVisibility(View.GONE);
                } else {
                    binding.progressBar.setVisibility(View.GONE);
                }
            }
        });

        viewModel.getError().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String err) {
                if (err != null && !err.isEmpty()) {
                    if (adapter.getItemCount() == 0) {
                        binding.includeErrorState.getRoot().setVisibility(View.VISIBLE);
                        binding.includeErrorState.textErrorMessage.setText(err);
                        binding.recyclerListings.setVisibility(View.GONE);
                        binding.scrollEmptyState.setVisibility(View.GONE);
                        binding.includeEmptyState.getRoot().setVisibility(View.GONE);
                    }
                    viewModel.clearError();
                }
            }
        });
    }

    private void updateOfflineBanner() {
        boolean online = true;
        try {
            online = NetworkMonitor.getInstance().isOnline();
        } catch (UnsupportedOperationException e) {
            online = true;
        }
        if (binding != null) {
            binding.includeOfflineBanner.getRoot().setVisibility(
                    !online ? View.VISIBLE : View.GONE);
        }
    }

    private void showGuestSignInDialog() {
        if (getContext() == null) {
            return;
        }
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.listing_home_guest_dialog_title)
                .setMessage(R.string.listing_home_guest_dialog_message)
                .setNegativeButton(R.string.listing_home_guest_dialog_cancel, null)
                .setPositiveButton(R.string.listing_home_guest_dialog_sign_in,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Navigation.findNavController(binding.getRoot())
                                        .navigate(R.id.action_global_sign_in);
                            }
                        })
                .show();
    }
}
