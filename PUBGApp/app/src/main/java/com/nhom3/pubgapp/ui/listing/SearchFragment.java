package com.nhom3.pubgapp.ui.listing;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.firebase.firestore.GeoPoint;
import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.data.DataCallback;
import com.nhom3.pubgapp.data.UserRepository;
import com.nhom3.pubgapp.databinding.FragmentSearchBinding;
import com.nhom3.pubgapp.databinding.LayoutListingFilterPanelBinding;
import com.nhom3.pubgapp.model.Category;
import com.nhom3.pubgapp.model.Listing;
import com.nhom3.pubgapp.model.SearchFilter;
import com.nhom3.pubgapp.model.UserAddress;
import com.nhom3.pubgapp.model.enums.ItemCondition;
import com.nhom3.pubgapp.model.enums.SortOrder;
import com.nhom3.pubgapp.model.enums.TradeType;
import com.nhom3.pubgapp.util.NetworkMonitor;
import com.nhom3.pubgapp.util.SessionManager;
import com.nhom3.pubgapp.viewmodel.SearchViewModel;

import java.util.ArrayList;
import java.util.List;

/** Screen B02 Search & Filter: 2-column grid with keyword search, history chips and filter panel. */
public class SearchFragment extends Fragment {

    private FragmentSearchBinding binding;
    private SearchViewModel viewModel;
    private ListingCardAdapter adapter;
    private final List<Category> loadedCategories = new ArrayList<Category>();
    private final List<String> provinceNames = new ArrayList<String>();
    private final UserRepository userRepo = new UserRepository();
    private GeoPoint userLocation = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SearchViewModel.class);

        setupRecyclerView();
        setupSearchInput();
        setupFilterPanel();
        setupStates();
        observeViewModel();

        updateOfflineBanner();
        loadUserLocation();

        // Load categories and recent search history once at start
        viewModel.loadCategories();
        viewModel.loadRecentSearches();

        // Perform initial search only if results are not already present (survives rotation)
        if (viewModel.getResults().getValue() == null) {
            viewModel.search();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserLocation();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Views die with the view hierarchy; holding the binding would leak them.
        binding = null;
    }

    private void loadUserLocation() {
        if (!SessionManager.getInstance().isLoggedIn()) {
            userLocation = null;
            return;
        }
        String uid = SessionManager.getInstance().getCurrentUserId();
        if (uid == null || uid.isEmpty()) {
            userLocation = null;
            return;
        }
        userRepo.getAddresses(uid, new DataCallback<List<UserAddress>>() {
            @Override
            public void onSuccess(List<UserAddress> addresses) {
                if (addresses == null || addresses.isEmpty()) {
                    userLocation = null;
                    return;
                }
                UserAddress defaultAddress = null;
                for (int i = 0; i < addresses.size(); i++) {
                    UserAddress addr = addresses.get(i);
                    if (addr != null && addr.isDefault()) {
                        defaultAddress = addr;
                        break;
                    }
                }
                if (defaultAddress != null && defaultAddress.getLocation() != null) {
                    userLocation = defaultAddress.getLocation();
                } else if (addresses.get(0).getLocation() != null) {
                    userLocation = addresses.get(0).getLocation();
                } else {
                    userLocation = null;
                }
            }

            @Override
            public void onError(String err) {
                userLocation = null;
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new ListingCardAdapter();
        final GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        binding.recyclerSearchResults.setLayoutManager(layoutManager);
        binding.recyclerSearchResults.setAdapter(adapter);

        // Endless scrolling: load next page when scrolled near the end
        binding.recyclerSearchResults.addOnScrollListener(new RecyclerView.OnScrollListener() {
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

    private void setupSearchInput() {
        // Restore keyword text if ViewModel filter already has one
        SearchFilter currentFilter = viewModel.getFilter();
        if (currentFilter != null && currentFilter.getKeyword() != null) {
            binding.editKeyword.setText(currentFilter.getKeyword());
            binding.buttonClearKeyword.setVisibility(View.VISIBLE);
        }

        binding.editKeyword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s != null && s.length() > 0) {
                    binding.buttonClearKeyword.setVisibility(View.VISIBLE);
                } else {
                    binding.buttonClearKeyword.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.editKeyword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                        && event.getAction() == KeyEvent.ACTION_DOWN)) {
                    triggerSearchFromInput();
                    return true;
                }
                return false;
            }
        });

        binding.buttonClearKeyword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.editKeyword.setText("");
                binding.buttonClearKeyword.setVisibility(View.GONE);
                viewModel.setKeyword("");
                viewModel.search();
            }
        });

        binding.buttonToggleFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isPanelVisible = binding.includeFilterPanel.getRoot().getVisibility() == View.VISIBLE;
                binding.includeFilterPanel.getRoot().setVisibility(isPanelVisible ? View.GONE : View.VISIBLE);
                if (!isPanelVisible) {
                    syncFilterPanelFromViewModel();
                }
            }
        });
    }

    private void triggerSearchFromInput() {
        String keyword = binding.editKeyword.getText().toString().trim();
        viewModel.setKeyword(keyword);
        viewModel.search();
        hideKeyboard();
    }

    private void setupFilterPanel() {
        final LayoutListingFilterPanelBinding panel = binding.includeFilterPanel;

        panel.buttonCloseFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                panel.getRoot().setVisibility(View.GONE);
            }
        });

        setupQuickPriceChips();
        setupProvinceSpinner();
        setupRadiusChips();

        panel.buttonResetFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetFilterInputs();
            }
        });

        panel.buttonApplyFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applyFilterInputs();
            }
        });
    }

    private void setupQuickPriceChips() {
        final LayoutListingFilterPanelBinding panel = binding.includeFilterPanel;

        panel.chipPriceUnder100k.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    panel.editMinPrice.setText("0");
                    panel.editMaxPrice.setText("100000");
                }
            }
        });

        panel.chipPrice100kTo500k.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    panel.editMinPrice.setText("100000");
                    panel.editMaxPrice.setText("500000");
                }
            }
        });

        panel.chipPrice500kTo2m.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    panel.editMinPrice.setText("500000");
                    panel.editMaxPrice.setText("2000000");
                }
            }
        });

        panel.chipPriceOver2m.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    panel.editMinPrice.setText("2000000");
                    panel.editMaxPrice.setText("");
                }
            }
        });
    }

    private void setupProvinceSpinner() {
        if (getContext() == null) {
            return;
        }
        provinceNames.clear();
        provinceNames.add(getString(R.string.listing_search_province_all));
        String[] provincesArray = getResources().getStringArray(R.array.listing_provinces);
        for (int i = 0; i < provincesArray.length; i++) {
            provinceNames.add(provincesArray[i]);
        }

        ArrayAdapter<String> provinceAdapter = new ArrayAdapter<String>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                provinceNames);
        provinceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.includeFilterPanel.spinnerProvince.setAdapter(provinceAdapter);
    }

    private void setupRadiusChips() {
        final LayoutListingFilterPanelBinding panel = binding.includeFilterPanel;
        CompoundButton.OnCheckedChangeListener radiusListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked && userLocation == null) {
                    buttonView.setChecked(false);
                    if (getContext() != null) {
                        Toast.makeText(getContext(),
                                R.string.listing_search_no_location,
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };
        panel.chipRadiusNearMe.setOnCheckedChangeListener(radiusListener);
        panel.chipRadius1km.setOnCheckedChangeListener(radiusListener);
        panel.chipRadius3km.setOnCheckedChangeListener(radiusListener);
        panel.chipRadius5km.setOnCheckedChangeListener(radiusListener);
    }

    private void setupStates() {
        binding.includeErrorState.buttonRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.includeErrorState.getRoot().setVisibility(View.GONE);
                updateOfflineBanner();
                viewModel.search();
            }
        });
    }

    private void observeViewModel() {
        viewModel.getResults().observe(getViewLifecycleOwner(), new Observer<List<Listing>>() {
            @Override
            public void onChanged(List<Listing> list) {
                updateOfflineBanner();
                adapter.submitList(list);

                if (list == null || list.isEmpty()) {
                    binding.recyclerSearchResults.setVisibility(View.GONE);
                    binding.textResultCount.setVisibility(View.GONE);
                    binding.includeEmptyState.getRoot().setVisibility(View.VISIBLE);
                    binding.includeErrorState.getRoot().setVisibility(View.GONE);

                    binding.includeEmptyState.textEmptyTitle.setText(R.string.listing_search_empty_title);
                    binding.includeEmptyState.textEmptyMessage.setText(R.string.listing_search_empty_message);
                    binding.includeEmptyState.buttonEmptyAction.setVisibility(View.GONE);
                } else {
                    binding.recyclerSearchResults.setVisibility(View.VISIBLE);
                    binding.includeEmptyState.getRoot().setVisibility(View.GONE);
                    binding.includeErrorState.getRoot().setVisibility(View.GONE);

                    binding.textResultCount.setVisibility(View.VISIBLE);
                    binding.textResultCount.setText(
                            getString(R.string.listing_search_result_count, list.size()));
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
                        binding.recyclerSearchResults.setVisibility(View.GONE);
                        binding.textResultCount.setVisibility(View.GONE);
                        binding.includeEmptyState.getRoot().setVisibility(View.GONE);
                    }
                    viewModel.clearError();
                }
            }
        });

        viewModel.getCategories().observe(getViewLifecycleOwner(), new Observer<List<Category>>() {
            @Override
            public void onChanged(List<Category> categories) {
                updateCategorySpinner(categories);
            }
        });

        viewModel.getRecentSearches().observe(getViewLifecycleOwner(), new Observer<List<String>>() {
            @Override
            public void onChanged(List<String> searches) {
                updateRecentSearchChips(searches);
            }
        });
    }

    private void updateCategorySpinner(List<Category> categories) {
        if (getContext() == null) {
            return;
        }
        loadedCategories.clear();
        List<String> names = new ArrayList<String>();
        names.add(getString(R.string.listing_search_category_all));

        if (categories != null) {
            for (int i = 0; i < categories.size(); i++) {
                Category cat = categories.get(i);
                loadedCategories.add(cat);
                names.add(cat.getName());
            }
        }

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<String>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                names);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.includeFilterPanel.spinnerCategory.setAdapter(categoryAdapter);

        // Pre-select category if existing in filter
        SearchFilter currentFilter = viewModel.getFilter();
        if (currentFilter != null && currentFilter.getCategoryId() != null) {
            for (int i = 0; i < loadedCategories.size(); i++) {
                if (currentFilter.getCategoryId().equals(loadedCategories.get(i).getId())) {
                    binding.includeFilterPanel.spinnerCategory.setSelection(i + 1);
                    break;
                }
            }
        }
    }

    private void updateRecentSearchChips(List<String> searches) {
        if (getContext() == null || binding == null) {
            return;
        }
        binding.chipGroupRecent.removeAllViews();

        if (searches == null || searches.isEmpty()) {
            binding.layoutRecentSearches.setVisibility(View.GONE);
            return;
        }

        binding.layoutRecentSearches.setVisibility(View.VISIBLE);
        LayoutInflater inflater = LayoutInflater.from(getContext());

        for (int i = 0; i < searches.size(); i++) {
            final String query = searches.get(i);
            Chip chip = new Chip(requireContext());
            chip.setText(query);
            chip.setCheckable(false);
            chip.setClickable(true);
            chip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    binding.editKeyword.setText(query);
                    viewModel.setKeyword(query);
                    viewModel.search();
                    hideKeyboard();
                }
            });
            binding.chipGroupRecent.addView(chip);
        }
    }

    private void syncFilterPanelFromViewModel() {
        SearchFilter f = viewModel.getFilter();
        if (f == null) {
            return;
        }
        LayoutListingFilterPanelBinding panel = binding.includeFilterPanel;

        // Sync prices
        if (f.getMinPrice() > 0) {
            panel.editMinPrice.setText(String.valueOf(f.getMinPrice()));
        } else {
            panel.editMinPrice.setText("");
        }
        if (f.getMaxPrice() > 0) {
            panel.editMaxPrice.setText(String.valueOf(f.getMaxPrice()));
        } else {
            panel.editMaxPrice.setText("");
        }
        panel.textPriceError.setVisibility(View.GONE);

        // Sync conditions
        List<ItemCondition> conditions = f.getConditions();
        panel.chipConditionNew.setChecked(conditions != null && conditions.contains(ItemCondition.NEW));
        panel.chipConditionLikeNew.setChecked(conditions != null && conditions.contains(ItemCondition.LIKE_NEW));
        panel.chipConditionUsed.setChecked(conditions != null && conditions.contains(ItemCondition.USED));
        panel.chipConditionRepaired.setChecked(conditions != null && conditions.contains(ItemCondition.REPAIRED));
        panel.chipConditionDefective.setChecked(conditions != null && conditions.contains(ItemCondition.DEFECTIVE));

        // Sync trade types
        List<TradeType> tradeTypes = f.getTradeTypes();
        panel.chipTradeSell.setChecked(tradeTypes != null && tradeTypes.contains(TradeType.SELL));
        panel.chipTradeExchange.setChecked(tradeTypes != null && tradeTypes.contains(TradeType.EXCHANGE));
        panel.chipTradeGiveaway.setChecked(tradeTypes != null && tradeTypes.contains(TradeType.GIVEAWAY));

        // Sync province
        if (f.getProvince() != null && !f.getProvince().isEmpty()) {
            for (int i = 1; i < provinceNames.size(); i++) {
                if (f.getProvince().equals(provinceNames.get(i))) {
                    panel.spinnerProvince.setSelection(i);
                    break;
                }
            }
        } else {
            panel.spinnerProvince.setSelection(0);
        }

        // Sync radius and distance (STT 27)
        if (f.getCenter() != null) {
            int radius = f.getRadiusKm();
            if (radius == 1) {
                panel.chipGroupRadius.check(R.id.chipRadius1km);
            } else if (radius == 3) {
                panel.chipGroupRadius.check(R.id.chipRadius3km);
            } else if (radius == 5) {
                panel.chipGroupRadius.check(R.id.chipRadius5km);
            } else {
                panel.chipGroupRadius.check(R.id.chipRadiusNearMe);
            }
        } else {
            panel.chipGroupRadius.clearCheck();
        }

        // Sync sort order
        SortOrder order = f.getSortOrder();
        if (order == SortOrder.PRICE_ASC) {
            panel.chipGroupSort.check(R.id.chipSortPriceAsc);
        } else if (order == SortOrder.PRICE_DESC) {
            panel.chipGroupSort.check(R.id.chipSortPriceDesc);
        } else if (order == SortOrder.TOP_RATED) {
            panel.chipGroupSort.check(R.id.chipSortTopRated);
        } else {
            panel.chipGroupSort.check(R.id.chipSortNewest);
        }
    }

    private void resetFilterInputs() {
        LayoutListingFilterPanelBinding panel = binding.includeFilterPanel;
        panel.spinnerCategory.setSelection(0);
        panel.chipGroupQuickPrice.clearCheck();
        panel.editMinPrice.setText("");
        panel.editMaxPrice.setText("");
        panel.textPriceError.setVisibility(View.GONE);
        panel.chipGroupCondition.clearCheck();
        panel.chipGroupTradeType.clearCheck();
        panel.spinnerProvince.setSelection(0);
        panel.chipGroupRadius.clearCheck();
        panel.chipGroupSort.check(R.id.chipSortNewest);
    }

    private void applyFilterInputs() {
        LayoutListingFilterPanelBinding panel = binding.includeFilterPanel;

        long minPrice = 0;
        String minText = panel.editMinPrice.getText() != null ? panel.editMinPrice.getText().toString().trim() : "";
        if (!minText.isEmpty()) {
            try {
                minPrice = Long.parseLong(minText);
            } catch (NumberFormatException e) {
                minPrice = 0;
            }
        }

        long maxPrice = 0;
        String maxText = panel.editMaxPrice.getText() != null ? panel.editMaxPrice.getText().toString().trim() : "";
        if (!maxText.isEmpty()) {
            try {
                maxPrice = Long.parseLong(maxText);
            } catch (NumberFormatException e) {
                maxPrice = 0;
            }
        }

        if (minPrice > 0 && maxPrice > 0 && maxPrice < minPrice) {
            panel.textPriceError.setVisibility(View.VISIBLE);
            return;
        }
        panel.textPriceError.setVisibility(View.GONE);

        SearchFilter newFilter = new SearchFilter();
        newFilter.setKeyword(binding.editKeyword.getText().toString().trim());

        // Category
        int categoryPos = panel.spinnerCategory.getSelectedItemPosition();
        if (categoryPos > 0 && (categoryPos - 1) < loadedCategories.size()) {
            newFilter.setCategoryId(loadedCategories.get(categoryPos - 1).getId());
        }

        newFilter.setMinPrice(minPrice);
        newFilter.setMaxPrice(maxPrice);

        // Conditions
        List<ItemCondition> conditions = new ArrayList<ItemCondition>();
        if (panel.chipConditionNew.isChecked()) {
            conditions.add(ItemCondition.NEW);
        }
        if (panel.chipConditionLikeNew.isChecked()) {
            conditions.add(ItemCondition.LIKE_NEW);
        }
        if (panel.chipConditionUsed.isChecked()) {
            conditions.add(ItemCondition.USED);
        }
        if (panel.chipConditionRepaired.isChecked()) {
            conditions.add(ItemCondition.REPAIRED);
        }
        if (panel.chipConditionDefective.isChecked()) {
            conditions.add(ItemCondition.DEFECTIVE);
        }
        if (!conditions.isEmpty()) {
            newFilter.setConditions(conditions);
        }

        // Trade types
        List<TradeType> tradeTypes = new ArrayList<TradeType>();
        if (panel.chipTradeSell.isChecked()) {
            tradeTypes.add(TradeType.SELL);
        }
        if (panel.chipTradeExchange.isChecked()) {
            tradeTypes.add(TradeType.EXCHANGE);
        }
        if (panel.chipTradeGiveaway.isChecked()) {
            tradeTypes.add(TradeType.GIVEAWAY);
        }
        if (!tradeTypes.isEmpty()) {
            newFilter.setTradeTypes(tradeTypes);
        }

        // Province
        int provincePos = panel.spinnerProvince.getSelectedItemPosition();
        if (provincePos > 0 && provincePos < provinceNames.size()) {
            newFilter.setProvince(provinceNames.get(provincePos));
        }

        // Radius and distance (STT 27)
        int checkedRadiusId = panel.chipGroupRadius.getCheckedChipId();
        if (checkedRadiusId != View.NO_ID && checkedRadiusId != -1) {
            if (userLocation != null) {
                newFilter.setCenter(userLocation);
                if (checkedRadiusId == R.id.chipRadius1km) {
                    newFilter.setRadiusKm(1);
                } else if (checkedRadiusId == R.id.chipRadius3km) {
                    newFilter.setRadiusKm(3);
                } else if (checkedRadiusId == R.id.chipRadius5km) {
                    newFilter.setRadiusKm(5);
                } else {
                    newFilter.setRadiusKm(0);
                }
            } else {
                newFilter.setCenter(null);
                newFilter.setRadiusKm(0);
            }
        } else {
            newFilter.setCenter(null);
            newFilter.setRadiusKm(0);
        }

        // Sort order
        int checkedSortId = panel.chipGroupSort.getCheckedChipId();
        if (checkedSortId == R.id.chipSortPriceAsc) {
            newFilter.setSortOrder(SortOrder.PRICE_ASC);
        } else if (checkedSortId == R.id.chipSortPriceDesc) {
            newFilter.setSortOrder(SortOrder.PRICE_DESC);
        } else if (checkedSortId == R.id.chipSortTopRated) {
            newFilter.setSortOrder(SortOrder.TOP_RATED);
        } else {
            newFilter.setSortOrder(SortOrder.NEWEST);
        }

        panel.getRoot().setVisibility(View.GONE);
        hideKeyboard();
        viewModel.applyFilter(newFilter);
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

    private void hideKeyboard() {
        if (getActivity() == null || getView() == null) {
            return;
        }
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        }
    }
}
