package com.nhom3.pubgapp.ui.trade;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ListenerRegistration;
import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.data.DataCallback;
import com.nhom3.pubgapp.data.DealRepository;
import com.nhom3.pubgapp.databinding.FragmentChatBinding;
import com.nhom3.pubgapp.model.ChatMessage;
import com.nhom3.pubgapp.model.Deal;
import com.nhom3.pubgapp.model.Listing;
import com.nhom3.pubgapp.model.Offer;
import com.nhom3.pubgapp.model.User;
import com.nhom3.pubgapp.model.enums.DealStatus;
import com.nhom3.pubgapp.ui.common.AvatarBinder;
import com.nhom3.pubgapp.ui.common.ImageLoader;
import com.nhom3.pubgapp.util.FormatUtils;
import com.nhom3.pubgapp.util.NetworkMonitor;
import com.nhom3.pubgapp.util.SessionManager;
import com.nhom3.pubgapp.viewmodel.ChatViewModel;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Screen C02: Real-time chat with text, image, partner status, and listing banner.
public class ChatFragment extends Fragment implements MessageAdapter.OnMessageLongClickListener {

    private static final String STATE_DRAFT_MESSAGE = "state_draft_message";

    private FragmentChatBinding binding;
    private ChatViewModel viewModel;
    private MessageAdapter adapter;
    private ActivityResultLauncher<String> imagePickerLauncher;
    private final FormatUtils formatUtils = new FormatUtils();
    private String currentListingId;
    private String currentConversationId;
    private String currentPartnerId;
    private boolean isOnline = true;
    private final DealRepository dealRepository = new DealRepository();
    private ListenerRegistration dealsListener;
    // The cached first answer of observeMyDeals can come from a background thread; the adapter needs the main one.
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri result) {
                        if (result != null) {
                            viewModel.sendImage(result);
                        }
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentChatBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(ChatViewModel.class);

        if (getArguments() != null) {
            currentListingId = getArguments().getString("listingId");
            currentConversationId = getArguments().getString("conversationId");
        }

        setupRecyclerView();
        setupTermsResult();
        setupInputBar();
        setupHeader();
        setupProductBanner();
        observeViewModel();

        if (savedInstanceState != null) {
            String draft = savedInstanceState.getString(STATE_DRAFT_MESSAGE, "");
            if (draft != null && !draft.isEmpty() && binding != null) {
                binding.editMessage.setText(draft);
            }
        }

        checkNetwork();

        String myUid = SessionManager.getInstance().getCurrentUserId();
        if (!myUid.isEmpty()) {
            observeDeals(myUid);
        }

        if (currentConversationId != null && !currentConversationId.isEmpty()) {
            viewModel.openConversationById(currentConversationId);
        } else if (currentListingId != null && !currentListingId.isEmpty()) {
            viewModel.openConversation(currentListingId);
        }
    }

    private void setupRecyclerView() {
        adapter = new MessageAdapter(this, new MessageAdapter.OnOfferActionListener() {
            @Override
            public void onOfferAccept(Offer offer) {
                if (offer != null && offer.getId() != null) {
                    showTermsDialog(offer.getId());
                }
            }

            @Override
            public void onOfferClick(Offer offer, String dealId) {
                openDealOfOffer(dealId);
            }

            @Override
            public void onOfferReject(Offer offer) {
                if (offer != null && offer.getId() != null) {
                    viewModel.respondToOffer(offer.getId(), false);
                }
            }
        });
        String myUid = SessionManager.getInstance().getCurrentUserId();
        adapter.setCurrentUserId(myUid);

        final LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        layoutManager.setStackFromEnd(true);
        binding.recyclerMessages.setLayoutManager(layoutManager);
        binding.recyclerMessages.setAdapter(adapter);

        binding.recyclerMessages.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (!recyclerView.canScrollVertically(-1) && dy < 0) {
                    viewModel.loadOlder();
                }
            }
        });
    }

    // The seller must give the terms before an offer is accepted; the dialog hands them back here.
    private void showTermsDialog(String offerId) {
        DealTermsDialogFragment dialog = DealTermsDialogFragment.newInstance(offerId, null, false);
        dialog.show(getParentFragmentManager(), "deal_terms");
    }

    private void setupTermsResult() {
        getParentFragmentManager().setFragmentResultListener(DealTermsDialogFragment.RESULT_KEY,
                getViewLifecycleOwner(), new FragmentResultListener() {
                    @Override
                    public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                        String offerId = result.getString(DealTermsDialogFragment.RESULT_OFFER_ID, "");
                        if (offerId.isEmpty()) {
                            return;
                        }
                        Deal terms = DealTermsDialogFragment.termsFromResult(result);
                        viewModel.respondToOffer(offerId, true, terms);
                    }
                });
    }

    private void openDealOfOffer(String dealId) {
        if (dealId == null || dealId.isEmpty()) {
            Snackbar.make(binding.getRoot(), R.string.trade_chat_offer_no_deal_hint, Snackbar.LENGTH_SHORT).show();
            return;
        }
        Bundle args = new Bundle();
        args.putString("dealId", dealId);
        Navigation.findNavController(requireView()).navigate(R.id.action_global_deal_detail, args);
    }

    private void setupInputBar() {
        binding.buttonAttachImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imagePickerLauncher.launch("image/*");
            }
        });

        binding.editMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean hasText = s != null && !s.toString().trim().isEmpty();
                binding.buttonSend.setEnabled(hasText && isOnline);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = binding.editMessage.getText() != null
                        ? binding.editMessage.getText().toString().trim() : "";
                if (!text.isEmpty()) {
                    viewModel.sendText(text);
                }
            }
        });
    }

    private void setupHeader() {
        binding.buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(requireView()).navigateUp();
            }
        });

        binding.buttonMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMenu(v);
            }
        });
    }

    private void setupProductBanner() {
        binding.cardProductBanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentListingId != null && !currentListingId.isEmpty()) {
                    Bundle args = new Bundle();
                    args.putString("listingId", currentListingId);
                    try {
                        Navigation.findNavController(requireView()).navigate(R.id.action_global_listing_detail, args);
                    } catch (Exception ignored) {
                    }
                }
            }
        });
    }

    private void showMenu(View anchor) {
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        popup.getMenu().add(0, 1, 0, R.string.trade_chat_menu_profile);
        popup.getMenu().add(0, 2, 1, R.string.trade_chat_menu_report_user);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == 1) {
                    if (currentPartnerId != null && !currentPartnerId.isEmpty()) {
                        Bundle bundle = new Bundle();
                        bundle.putString("userId", currentPartnerId);
                        Navigation.findNavController(requireView()).navigate(R.id.action_global_user_profile, bundle);
                        return true;
                    }
                } else if (id == 2) {
                    if (currentPartnerId != null && !currentPartnerId.isEmpty()) {
                        Bundle bundle = new Bundle();
                        bundle.putString("targetType", "USER");
                        bundle.putString("targetId", currentPartnerId);
                        Navigation.findNavController(requireView()).navigate(R.id.action_global_report, bundle);
                        return true;
                    }
                }
                return false;
            }
        });
        popup.show();
    }

    private void checkNetwork() {
        try {
            isOnline = NetworkMonitor.getInstance().isOnline();
        } catch (UnsupportedOperationException e) {
            isOnline = true;
        }
        binding.includeOfflineBanner.layoutOfflineBanner.setVisibility(isOnline ? View.GONE : View.VISIBLE);
        String text = binding.editMessage.getText() != null
                ? binding.editMessage.getText().toString().trim() : "";
        binding.buttonSend.setEnabled(!text.isEmpty() && isOnline);
        binding.buttonAttachImage.setEnabled(isOnline);
    }

    private void observeViewModel() {
        viewModel.getMessages().observe(getViewLifecycleOwner(), new Observer<List<ChatMessage>>() {
            @Override
            public void onChanged(List<ChatMessage> chatMessages) {
                int previousCount = adapter.getItemCount();
                adapter.setMessages(chatMessages);
                if (chatMessages != null && !chatMessages.isEmpty()) {
                    if (previousCount == 0) {
                        binding.recyclerMessages.scrollToPosition(chatMessages.size() - 1);
                    }
                }
            }
        });

        viewModel.getOffers().observe(getViewLifecycleOwner(), new Observer<List<Offer>>() {
            @Override
            public void onChanged(List<Offer> offers) {
                if (adapter != null) {
                    adapter.setOffers(offers);
                }
            }
        });

        viewModel.getCreatedDealId().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String dealId) {
                if (dealId != null && !dealId.isEmpty()) {
                    viewModel.clearCreatedDealId();
                    Bundle args = new Bundle();
                    args.putString("dealId", dealId);
                    try {
                        Navigation.findNavController(requireView())
                                .navigate(R.id.action_global_deal_detail, args);
                    } catch (Exception ignored) {
                    }
                }
            }
        });

        viewModel.getDone().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isDone) {
                if (isDone != null && isDone) {
                    binding.editMessage.setText("");
                    viewModel.clearDone();
                }
            }
        });

        viewModel.getPartner().observe(getViewLifecycleOwner(), new Observer<User>() {
            @Override
            public void onChanged(User partnerUser) {
                if (partnerUser != null) {
                    currentPartnerId = partnerUser.getId();
                    String name = partnerUser.getDisplayName();
                    if (name == null || name.trim().isEmpty()) {
                        name = getString(R.string.trade_conversation_list_partner_fallback);
                    }
                    binding.textPartnerName.setText(name);
                    AvatarBinder.bind(binding.includeAvatar, name, partnerUser.getAvatarBase64());

                    Timestamp lastActive = partnerUser.getLastActiveAt();
                    if (lastActive != null) {
                        long fiveMinutesAgo = System.currentTimeMillis() - 5 * 60 * 1000L;
                        if (partnerUser.isActiveSince(new Timestamp(new Date(fiveMinutesAgo)))) {
                            binding.textPartnerStatus.setText(R.string.trade_chat_active_now);
                        } else {
                            String timeStr = formatUtils.formatTime(lastActive);
                            binding.textPartnerStatus.setText(getString(R.string.trade_chat_active_recent, timeStr));
                        }
                    } else {
                        binding.textPartnerStatus.setText("");
                    }

                    if (partnerUser.isLocked() || partnerUser.isDeleted()) {
                        binding.editMessage.setEnabled(false);
                        binding.buttonSend.setEnabled(false);
                        binding.buttonAttachImage.setEnabled(false);
                        binding.textLockedWarning.setVisibility(View.VISIBLE);
                    } else {
                        binding.editMessage.setEnabled(true);
                        String text = binding.editMessage.getText() != null
                                ? binding.editMessage.getText().toString().trim() : "";
                        binding.buttonSend.setEnabled(!text.isEmpty() && isOnline);
                        binding.buttonAttachImage.setEnabled(isOnline);
                        binding.textLockedWarning.setVisibility(View.GONE);
                    }
                }
            }
        });

        viewModel.getListing().observe(getViewLifecycleOwner(), new Observer<Listing>() {
            @Override
            public void onChanged(Listing listing) {
                if (listing != null) {
                    binding.textProductTitle.setText(listing.getTitle());
                    binding.textProductPrice.setText(formatUtils.formatPrice(listing.getPrice()));
                    if (listing.getCoverThumb() != null && !listing.getCoverThumb().isEmpty()) {
                        ImageLoader.load(
                                listing.getCoverThumb(),
                                binding.imageProductThumb,
                                R.drawable.bg_status_chip,
                                R.drawable.bg_status_chip
                        );
                    }
                } else {
                    binding.textProductTitle.setText(R.string.trade_chat_listing_unavailable);
                    binding.textProductPrice.setText("");
                }
            }
        });

        viewModel.getLoading().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isLoading) {
                if (isLoading != null && isLoading) {
                    binding.progressLoading.setVisibility(View.VISIBLE);
                } else {
                    binding.progressLoading.setVisibility(View.GONE);
                }
            }
        });

        viewModel.getError().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String err) {
                if (err != null && !err.isEmpty()) {
                    if (err.contains("xác thực")) {
                        Snackbar.make(binding.getRoot(), err, Snackbar.LENGTH_LONG)
                                .setAction(R.string.trade_chat_offer_register_verification,
                                        new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                try {
                                                    Navigation.findNavController(requireView())
                                                            .navigate(R.id.action_global_my_profile);
                                                } catch (Exception ignored) {
                                                }
                                            }
                                        }).show();
                    } else {
                        Snackbar.make(binding.getRoot(), err, Snackbar.LENGTH_LONG).show();
                    }
                    viewModel.clearError();
                }
            }
        });
    }

    @Override
    public void onMessageLongClick(final ChatMessage message) {
        if (message == null || getContext() == null) {
            return;
        }
        PopupMenu popup = new PopupMenu(requireContext(), binding.recyclerMessages);
        popup.getMenu().add(0, 1, 0, R.string.trade_chat_menu_report_message);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == 1) {
                    Bundle bundle = new Bundle();
                    bundle.putString("targetType", "MESSAGE");
                    bundle.putString("targetId", message.getId());
                    bundle.putString("conversationId", currentConversationId);
                    Navigation.findNavController(requireView()).navigate(R.id.action_global_report, bundle);
                    return true;
                }
                return false;
            }
        });
        popup.show();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (binding != null && binding.editMessage.getText() != null) {
            outState.putString(STATE_DRAFT_MESSAGE, binding.editMessage.getText().toString());
        }
    }

    private void observeDeals(String uid) {
        if (dealsListener != null) {
            dealsListener.remove();
            dealsListener = null;
        }
        dealsListener = dealRepository.observeMyDeals(uid, new DataCallback<List<Deal>>() {
            @Override
            public void onSuccess(List<Deal> deals) {
                if (deals == null) {
                    return;
                }
                final Map<String, DealStatus> dealStatusByOffer = dealStatusByOffer(deals);
                final Map<String, String> dealIdByOffer = dealIdByOffer(deals);
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (binding != null && adapter != null) {
                            adapter.setDealIdByOffer(dealIdByOffer);
                            adapter.setDealStatusByOffer(dealStatusByOffer);
                        }
                    }
                });
            }

            @Override
            public void onError(String message) {
            }
        });
    }

    // Live deal status per offer: the offer cards show "deal cancelled" as soon as the other party cancels.
    private Map<String, DealStatus> dealStatusByOffer(List<Deal> deals) {
        Map<String, DealStatus> result = new HashMap<String, DealStatus>();
        for (int i = 0; i < deals.size(); i++) {
            Deal deal = deals.get(i);
            if (deal != null && deal.getOfferId() != null) {
                result.put(deal.getOfferId(), deal.getStatus());
            }
        }
        return result;
    }

    private Map<String, String> dealIdByOffer(List<Deal> deals) {
        Map<String, String> result = new HashMap<String, String>();
        for (int i = 0; i < deals.size(); i++) {
            Deal deal = deals.get(i);
            if (deal != null && deal.getOfferId() != null && deal.getId() != null) {
                result.put(deal.getOfferId(), deal.getId());
            }
        }
        return result;
    }

    // The ViewModel belongs to the activity, so it outlives this screen; stop the chat's listeners
    // when the screen is really closed, but not on rotation, which recreates it at once.
    @Override
    public void onDestroy() {
        super.onDestroy();
        boolean rotating = getActivity() != null && getActivity().isChangingConfigurations();
        if (viewModel != null && !rotating) {
            viewModel.closeConversation(openedWith());
        }
    }

    // The same key the screen passed to openConversationById or openConversation.
    private String openedWith() {
        if (currentConversationId != null && !currentConversationId.isEmpty()) {
            return currentConversationId;
        }
        return currentListingId;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mainHandler.removeCallbacksAndMessages(null);
        if (dealsListener != null) {
            dealsListener.remove();
            dealsListener = null;
        }
        binding = null;
    }
}
