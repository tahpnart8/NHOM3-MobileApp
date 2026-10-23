package com.nhom3.pubgapp.ui.trade;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.snackbar.Snackbar;
import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.data.DataCallback;
import com.nhom3.pubgapp.data.ListingRepository;
import com.nhom3.pubgapp.databinding.FragmentMakeOfferBinding;
import com.nhom3.pubgapp.model.Listing;
import com.nhom3.pubgapp.model.Offer;
import com.nhom3.pubgapp.model.enums.OfferType;
import com.nhom3.pubgapp.util.FormatUtils;
import com.nhom3.pubgapp.viewmodel.ChatViewModel;

import java.util.List;

// Bottom sheet dialog for sending a price offer from screen B03.
public class MakeOfferFragment extends BottomSheetDialogFragment {

    private FragmentMakeOfferBinding binding;
    private ChatViewModel viewModel;
    private String listingId;
    private Listing currentListing;
    private final FormatUtils formatUtils = new FormatUtils();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentMakeOfferBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(ChatViewModel.class);

        if (getArguments() != null) {
            listingId = getArguments().getString("listingId");
        }

        setupRadioGroup();
        setupSubmitButton();
        observeViewModel();

        if (listingId != null && !listingId.isEmpty()) {
            loadListingDetails(listingId);
        }
    }

    private void setupRadioGroup() {
        binding.radioGroupOfferType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radioOfferMoney) {
                    binding.inputLayoutOfferAmount.setVisibility(View.VISIBLE);
                    binding.inputLayoutOfferItem.setVisibility(View.GONE);
                } else if (checkedId == R.id.radioOfferItem) {
                    binding.inputLayoutOfferAmount.setVisibility(View.GONE);
                    binding.inputLayoutOfferItem.setVisibility(View.VISIBLE);
                } else if (checkedId == R.id.radioOfferBoth) {
                    binding.inputLayoutOfferAmount.setVisibility(View.VISIBLE);
                    binding.inputLayoutOfferItem.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void loadListingDetails(String id) {
        binding.progressLoading.setVisibility(View.VISIBLE);
        new ListingRepository().getById(id, new DataCallback<Listing>() {
            @Override
            public void onSuccess(Listing result) {
                binding.progressLoading.setVisibility(View.GONE);
                currentListing = result;
                if (result != null && binding != null) {
                    binding.textListingTitle.setText(result.getTitle());
                    binding.textListingPrice.setText(formatUtils.formatPrice(result.getPrice()));

                    List<OfferType> accepted = result.getAcceptedOfferTypes();
                    if (accepted != null) {
                        boolean hasMoney = accepted.contains(OfferType.MONEY);
                        boolean hasItem = accepted.contains(OfferType.ITEM);
                        boolean hasBoth = accepted.contains(OfferType.MONEY_AND_ITEM);

                        binding.radioOfferMoney.setVisibility(hasMoney ? View.VISIBLE : View.GONE);
                        binding.radioOfferItem.setVisibility(hasItem ? View.VISIBLE : View.GONE);
                        binding.radioOfferBoth.setVisibility(hasBoth ? View.VISIBLE : View.GONE);

                        if (hasMoney) {
                            binding.radioOfferMoney.setChecked(true);
                        } else if (hasItem) {
                            binding.radioOfferItem.setChecked(true);
                        } else if (hasBoth) {
                            binding.radioOfferBoth.setChecked(true);
                        }
                    }
                }
            }

            @Override
            public void onError(String message) {
                if (binding != null) {
                    binding.progressLoading.setVisibility(View.GONE);
                    Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setupSubmitButton() {
        binding.buttonSendOffer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitOffer();
            }
        });
    }

    private void submitOffer() {
        if (listingId == null || listingId.isEmpty()) {
            return;
        }

        int checkedId = binding.radioGroupOfferType.getCheckedRadioButtonId();
        OfferType offerType = OfferType.MONEY;
        long amount = 0;
        String exchangeItem = "";

        if (checkedId == R.id.radioOfferMoney) {
            offerType = OfferType.MONEY;
            amount = parseAmount();
            if (amount <= 0) {
                binding.inputLayoutOfferAmount.setError("Vui lòng nhập số tiền đề nghị hợp lệ");
                return;
            }
            binding.inputLayoutOfferAmount.setError(null);
        } else if (checkedId == R.id.radioOfferItem) {
            offerType = OfferType.ITEM;
            exchangeItem = binding.editOfferItem.getText() != null
                    ? binding.editOfferItem.getText().toString().trim() : "";
            if (exchangeItem.isEmpty()) {
                binding.inputLayoutOfferItem.setError("Vui lòng nhập thông tin món đồ trao đổi");
                return;
            }
            binding.inputLayoutOfferItem.setError(null);
        } else if (checkedId == R.id.radioOfferBoth) {
            offerType = OfferType.MONEY_AND_ITEM;
            amount = parseAmount();
            exchangeItem = binding.editOfferItem.getText() != null
                    ? binding.editOfferItem.getText().toString().trim() : "";
            if (amount <= 0) {
                binding.inputLayoutOfferAmount.setError("Vui lòng nhập số tiền đề nghị hợp lệ");
                return;
            }
            binding.inputLayoutOfferAmount.setError(null);
            if (exchangeItem.isEmpty()) {
                binding.inputLayoutOfferItem.setError("Vui lòng nhập thông tin món đồ trao đổi");
                return;
            }
            binding.inputLayoutOfferItem.setError(null);
        }

        Offer offer = new Offer();
        offer.setListingId(listingId);
        offer.setOfferType(offerType);
        offer.setAmount(amount);
        offer.setExchangeItem(exchangeItem);

        viewModel.makeOffer(offer);
    }

    private long parseAmount() {
        String amountStr = binding.editOfferAmount.getText() != null
                ? binding.editOfferAmount.getText().toString().trim() : "";
        if (amountStr.isEmpty()) {
            return 0;
        }
        try {
            return Long.parseLong(amountStr);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void observeViewModel() {
        viewModel.getLoading().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isLoading) {
                boolean loading = Boolean.TRUE.equals(isLoading);
                binding.progressLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
                binding.buttonSendOffer.setEnabled(!loading);
            }
        });

        viewModel.getError().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String err) {
                if (err != null && !err.isEmpty() && binding != null) {
                    if (err.contains("xác thực")) {
                        Snackbar.make(binding.getRoot(), err, Snackbar.LENGTH_LONG)
                                .setAction(R.string.trade_chat_offer_register_verification,
                                        new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                dismiss();
                                                try {
                                                    Navigation.findNavController(
                                                            requireActivity(),
                                                            R.id.navHostMain)
                                                            .navigate(R.id.action_global_my_profile);
                                                } catch (Exception ignored) {
                                                }
                                            }
                                        }).show();
                    } else {
                        Snackbar.make(binding.getRoot(), err, Snackbar.LENGTH_SHORT).show();
                    }
                    viewModel.clearError();
                }
            }
        });

        viewModel.getDone().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isDone) {
                if (Boolean.TRUE.equals(isDone)) {
                    viewModel.clearDone();
                    dismiss();
                    Bundle args = new Bundle();
                    args.putString("listingId", listingId);
                    try {
                        Navigation.findNavController(requireActivity(),
                                R.id.navHostMain)
                                .navigate(R.id.action_global_chat, args);
                    } catch (Exception ignored) {
                    }
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
