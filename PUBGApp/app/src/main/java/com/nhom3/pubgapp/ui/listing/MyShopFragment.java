package com.nhom3.pubgapp.ui.listing;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

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
import com.nhom3.pubgapp.databinding.FragmentMyShopBinding;
import com.nhom3.pubgapp.model.Category;
import com.nhom3.pubgapp.model.Listing;
import com.nhom3.pubgapp.model.User;
import com.nhom3.pubgapp.model.enums.ListingStatus;
import com.nhom3.pubgapp.model.enums.SortOrder;
import com.nhom3.pubgapp.model.enums.VerificationStatus;
import com.nhom3.pubgapp.util.NetworkMonitor;
import com.nhom3.pubgapp.util.SessionManager;
import com.nhom3.pubgapp.viewmodel.ProfileViewModel;
import com.nhom3.pubgapp.viewmodel.ShopViewModel;

import java.util.ArrayList;
import java.util.List;

// Screen B05: My shop shelf with tabs, sort, category filter, and card actions.
public class MyShopFragment extends Fragment {

    private static final String KEY_SELECTED_TAB = "key_shop_tab";
    private static final String KEY_SORT_ORDER = "key_shop_sort";
    private static final String KEY_CATEGORY_ID = "key_shop_category";

    private FragmentMyShopBinding binding;
    private ShopViewModel viewModel;
    private ProfileViewModel profileViewModel;
    private ShopListingAdapter adapter;

    private int selectedTab = ShopViewModel.TAB_ACTIVE;
    private SortOrder currentSortOrder = SortOrder.NEWEST;
    private String selectedCategoryId = "";
    private VerificationStatus userVerification = VerificationStatus.NONE;
    private List<Listing> currentFullList = new ArrayList<Listing>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMyShopBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(ShopViewModel.class);
        profileViewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);

        if (savedInstanceState != null) {
            selectedTab = savedInstanceState.getInt(KEY_SELECTED_TAB, ShopViewModel.TAB_ACTIVE);
            String sortName = savedInstanceState.getString(KEY_SORT_ORDER, SortOrder.NEWEST.name());
            currentSortOrder = SortOrder.valueOf(sortName);
            selectedCategoryId = savedInstanceState.getString(KEY_CATEGORY_ID, "");
        }

        setupToolbar();
        setupTabs();
        setupRecycler();
        setupObservers();

        checkNetworkAndVerification();

        viewModel.loadCategories();
        viewModel.loadMyListings(currentSortOrder, selectedCategoryId);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_SELECTED_TAB, selectedTab);
        outState.putString(KEY_SORT_ORDER, currentSortOrder.name());
        outState.putString(KEY_CATEGORY_ID, selectedCategoryId);
    }

    private void setupToolbar() {
        binding.layoutToolbar.textTitle.setText(R.string.listing_shop_title);
        binding.layoutToolbar.buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavHostFragment.findNavController(MyShopFragment.this).popBackStack();
            }
        });

        binding.buttonOverflowMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOverflowMenu(v);
            }
        });

        binding.buttonVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavHostFragment.findNavController(MyShopFragment.this)
                        .navigate(R.id.action_global_my_profile);
            }
        });

        binding.layoutErrorState.buttonRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.loadMyListings(currentSortOrder, selectedCategoryId);
            }
        });
    }

    private void setupTabs() {
        binding.tabLayoutStatus.removeAllTabs();
        binding.tabLayoutStatus.addTab(binding.tabLayoutStatus.newTab()
                .setText(R.string.listing_shop_tab_active));
        binding.tabLayoutStatus.addTab(binding.tabLayoutStatus.newTab()
                .setText(R.string.listing_shop_tab_sold));
        binding.tabLayoutStatus.addTab(binding.tabLayoutStatus.newTab()
                .setText(R.string.listing_shop_tab_hidden));
        binding.tabLayoutStatus.addTab(binding.tabLayoutStatus.newTab()
                .setText(getString(R.string.listing_shop_tab_draft, 0)));

        TabLayout.Tab tab = binding.tabLayoutStatus.getTabAt(selectedTab);
        if (tab != null) {
            tab.select();
        }

        binding.tabLayoutStatus.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectedTab = tab.getPosition();
                updateDisplayedList();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void setupRecycler() {
        adapter = new ShopListingAdapter(
                new ShopListingAdapter.OnListingClickListener() {
                    @Override
                    public void onListingClick(Listing listing) {
                        openListingDetail(listing);
                    }
                },
                new ShopListingAdapter.OnListingMenuClickListener() {
                    @Override
                    public void onListingMenuClick(View anchorView, Listing listing) {
                        showCardActionMenu(anchorView, listing);
                    }
                }
        );
        binding.recyclerListings.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerListings.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getMyListings().observe(getViewLifecycleOwner(), new Observer<List<Listing>>() {
            @Override
            public void onChanged(List<Listing> listings) {
                currentFullList = listings != null ? listings : new ArrayList<Listing>();
                updateDraftCountTab();
                updateDisplayedList();
            }
        });

        viewModel.getLoading().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isLoading) {
                boolean loading = isLoading != null && isLoading;
                binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
            }
        });

        viewModel.getError().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String errorMsg) {
                if (errorMsg != null && !errorMsg.isEmpty()) {
                    binding.layoutErrorState.getRoot().setVisibility(View.VISIBLE);
                    binding.layoutErrorState.textErrorMessage.setText(errorMsg);
                    binding.recyclerListings.setVisibility(View.GONE);
                    binding.layoutEmptyState.getRoot().setVisibility(View.GONE);
                    viewModel.clearError();
                } else {
                    binding.layoutErrorState.getRoot().setVisibility(View.GONE);
                }
            }
        });

        viewModel.getDone().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isDone) {
                if (isDone != null && isDone) {
                    Snackbar.make(binding.getRoot(), R.string.listing_shop_publish_success,
                            Snackbar.LENGTH_SHORT).show();
                    viewModel.clearDone();
                }
            }
        });

        profileViewModel.getProfile().observe(getViewLifecycleOwner(), new Observer<User>() {
            @Override
            public void onChanged(User user) {
                if (user != null && user.getVerificationStatus() != null) {
                    userVerification = user.getVerificationStatus();
                } else {
                    userVerification = VerificationStatus.NONE;
                }
                updateVerificationBanner();
            }
        });
    }

    private void checkNetworkAndVerification() {
        boolean online = NetworkMonitor.getInstance().isOnline();
        binding.layoutOfflineBanner.getRoot().setVisibility(online ? View.GONE : View.VISIBLE);

        String uid = SessionManager.getInstance().getCurrentUserId();
        if (uid != null && !uid.isEmpty()) {
            profileViewModel.loadProfile(uid);
        }
    }

    private void updateVerificationBanner() {
        boolean showBanner = userVerification != VerificationStatus.APPROVED;
        binding.layoutUnverifiedBanner.setVisibility(showBanner ? View.VISIBLE : View.GONE);
    }

    private void updateDraftCountTab() {
        List<Listing> drafts = ShopViewModel.filterByTab(currentFullList, ShopViewModel.TAB_DRAFT);
        int draftCount = drafts != null ? drafts.size() : 0;
        TabLayout.Tab draftTab = binding.tabLayoutStatus.getTabAt(ShopViewModel.TAB_DRAFT);
        if (draftTab != null) {
            draftTab.setText(getString(R.string.listing_shop_tab_draft, draftCount));
        }
    }

    private void updateDisplayedList() {
        List<Listing> filtered = ShopViewModel.filterByTab(currentFullList, selectedTab);
        adapter.setListings(filtered);

        if (filtered == null || filtered.isEmpty()) {
            binding.recyclerListings.setVisibility(View.GONE);
            binding.layoutEmptyState.getRoot().setVisibility(View.VISIBLE);
            if (selectedTab == ShopViewModel.TAB_DRAFT) {
                binding.layoutEmptyState.textEmptyMessage.setText(R.string.listing_shop_empty_draft);
            } else {
                binding.layoutEmptyState.textEmptyMessage.setText(R.string.listing_shop_empty_general);
            }
        } else {
            binding.recyclerListings.setVisibility(View.VISIBLE);
            binding.layoutEmptyState.getRoot().setVisibility(View.GONE);
        }
    }

    private void showOverflowMenu(View anchor) {
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        popup.inflate(R.menu.menu_listing_shop);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.action_sort_newest) {
                    currentSortOrder = SortOrder.NEWEST;
                    viewModel.loadMyListings(currentSortOrder, selectedCategoryId);
                    return true;
                } else if (id == R.id.action_filter_category) {
                    showCategoryFilterDialog();
                    return true;
                }
                return false;
            }
        });
        popup.show();
    }

    private void showCategoryFilterDialog() {
        List<Category> categories = viewModel.getCategories().getValue();
        final List<String> names = new ArrayList<String>();
        final List<String> ids = new ArrayList<String>();

        names.add(getString(R.string.listing_shop_filter_all));
        ids.add("");

        if (categories != null) {
            for (Category c : categories) {
                if (c != null) {
                    names.add(c.getName() != null ? c.getName() : "");
                    ids.add(c.getId() != null ? c.getId() : "");
                }
            }
        }

        int checkedIndex = 0;
        for (int i = 0; i < ids.size(); i++) {
            if (ids.get(i).equals(selectedCategoryId)) {
                checkedIndex = i;
                break;
            }
        }

        String[] nameArray = names.toArray(new String[0]);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(R.string.listing_shop_menu_filter);
        builder.setSingleChoiceItems(nameArray, checkedIndex, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selectedCategoryId = ids.get(which);
                dialog.dismiss();
                viewModel.loadMyListings(currentSortOrder, selectedCategoryId);
            }
        });
        builder.setNegativeButton(R.string.listing_post_btn_close, null);
        builder.show();
    }

    private void showCardActionMenu(View anchor, final Listing listing) {
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        popup.inflate(R.menu.menu_listing_shop_item);
        List<String> allowed = ShopViewModel.allowedActions(listing);

        popup.getMenu().findItem(R.id.action_edit)
                .setVisible(allowed.contains(ShopViewModel.ACTION_EDIT));
        popup.getMenu().findItem(R.id.action_hide)
                .setVisible(allowed.contains(ShopViewModel.ACTION_HIDE));
        popup.getMenu().findItem(R.id.action_show)
                .setVisible(allowed.contains(ShopViewModel.ACTION_SHOW));
        popup.getMenu().findItem(R.id.action_mark_sold)
                .setVisible(allowed.contains(ShopViewModel.ACTION_MARK_SOLD));
        popup.getMenu().findItem(R.id.action_reopen)
                .setVisible(allowed.contains(ShopViewModel.ACTION_REOPEN));
        popup.getMenu().findItem(R.id.action_publish)
                .setVisible(allowed.contains(ShopViewModel.ACTION_PUBLISH));
        popup.getMenu().findItem(R.id.action_delete)
                .setVisible(allowed.contains(ShopViewModel.ACTION_DELETE));
        popup.getMenu().findItem(R.id.action_view_reason)
                .setVisible(allowed.contains(ShopViewModel.ACTION_VIEW_REASON));

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return handleCardAction(item.getItemId(), listing);
            }
        });
        popup.show();
    }

    private boolean handleCardAction(int itemId, Listing listing) {
        if (itemId == R.id.action_view_reason) {
            showAdminReasonDialog(listing);
            return true;
        }

        if (!NetworkMonitor.getInstance().isOnline()) {
            Snackbar.make(binding.getRoot(), R.string.listing_shop_offline_warning,
                    Snackbar.LENGTH_LONG).show();
            return true;
        }

        if (itemId == R.id.action_edit) {
            openListingEdit(listing);
            return true;
        } else if (itemId == R.id.action_hide) {
            confirmHideListing(listing);
            return true;
        } else if (itemId == R.id.action_show) {
            viewModel.setHidden(listing.getId(), false);
            return true;
        } else if (itemId == R.id.action_mark_sold) {
            confirmMarkSold(listing);
            return true;
        } else if (itemId == R.id.action_reopen) {
            viewModel.changeStatus(listing.getId(), ListingStatus.AVAILABLE);
            return true;
        } else if (itemId == R.id.action_publish) {
            publishDraftListing(listing);
            return true;
        } else if (itemId == R.id.action_delete) {
            confirmDeleteDraft(listing);
            return true;
        }
        return false;
    }

    private void openListingDetail(Listing listing) {
        if (listing == null || listing.getId() == null) {
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putString("listingId", listing.getId());
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_global_listing_detail, bundle);
    }

    private void openListingEdit(Listing listing) {
        if (listing == null || listing.getId() == null) {
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putString("listingId", listing.getId());
        NavHostFragment.findNavController(this)
                .navigate(R.id.postListingFragment, bundle);
    }

    private void confirmHideListing(final Listing listing) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setMessage(R.string.listing_shop_hide_confirm);
        builder.setPositiveButton(R.string.listing_shop_action_hide, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                viewModel.setHidden(listing.getId(), true);
            }
        });
        builder.setNegativeButton(R.string.listing_post_btn_close, null);
        builder.show();
    }

    private void confirmMarkSold(final Listing listing) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setMessage(R.string.listing_shop_mark_sold_confirm);
        builder.setPositiveButton(R.string.listing_shop_action_mark_sold,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        viewModel.changeStatus(listing.getId(), ListingStatus.SOLD);
                    }
                });
        builder.setNegativeButton(R.string.listing_post_btn_close, null);
        builder.show();
    }

    private void confirmDeleteDraft(final Listing listing) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(R.string.listing_shop_dialog_delete_title);
        builder.setMessage(R.string.listing_shop_dialog_delete_msg);
        builder.setPositiveButton(R.string.listing_shop_action_delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                viewModel.deleteListing(listing.getId());
            }
        });
        builder.setNegativeButton(R.string.listing_post_btn_close, null);
        builder.show();
    }

    private void publishDraftListing(Listing listing) {
        if (userVerification != VerificationStatus.APPROVED) {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle(R.string.listing_post_unverified_dialog_title);
            if (userVerification == VerificationStatus.PENDING) {
                builder.setMessage(R.string.listing_post_pending_dialog_msg);
                builder.setPositiveButton(R.string.listing_post_btn_close, null);
            } else {
                builder.setMessage(R.string.listing_post_unverified_dialog_msg);
                builder.setPositiveButton(R.string.listing_post_btn_verify,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                NavHostFragment.findNavController(MyShopFragment.this)
                                        .navigate(R.id.action_global_my_profile);
                            }
                        });
                builder.setNegativeButton(R.string.listing_post_btn_close, null);
            }
            builder.show();
            return;
        }

        viewModel.publishDraft(listing.getId());
    }

    private void showAdminReasonDialog(Listing listing) {
        String reason = listing != null ? listing.getStatusReason() : null;
        if (reason == null || reason.trim().isEmpty()) {
            reason = getString(R.string.listing_shop_reason_empty);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(R.string.listing_shop_reason_title);
        builder.setMessage(reason);
        builder.setPositiveButton(R.string.listing_post_btn_close, null);
        builder.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
