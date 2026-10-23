package com.nhom3.pubgapp.ui.trade;

import android.net.Uri;
import android.os.Bundle;
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
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.databinding.FragmentChatBinding;
import com.nhom3.pubgapp.model.ChatMessage;
import com.nhom3.pubgapp.model.Listing;
import com.nhom3.pubgapp.model.User;
import com.nhom3.pubgapp.ui.common.ImageLoader;
import com.nhom3.pubgapp.util.FormatUtils;
import com.nhom3.pubgapp.util.NetworkMonitor;
import com.nhom3.pubgapp.util.SessionManager;
import com.nhom3.pubgapp.viewmodel.ChatViewModel;

import java.util.Date;
import java.util.List;

// Screen C02: Real-time chat with text, image, partner status, and listing banner.
public class ChatFragment extends Fragment implements MessageAdapter.OnMessageLongClickListener {

    private FragmentChatBinding binding;
    private ChatViewModel viewModel;
    private MessageAdapter adapter;
    private ActivityResultLauncher<String> imagePickerLauncher;
    private final FormatUtils formatUtils = new FormatUtils();
    private String currentListingId;
    private String currentConversationId;
    private String currentPartnerId;
    private boolean isOnline = true;

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
        setupInputBar();
        setupHeader();
        setupProductBanner();
        observeViewModel();

        checkNetwork();

        if (currentConversationId != null && !currentConversationId.isEmpty()) {
            viewModel.openConversationById(currentConversationId);
        } else if (currentListingId != null && !currentListingId.isEmpty()) {
            viewModel.openConversation(currentListingId);
        }
    }

    private void setupRecyclerView() {
        adapter = new MessageAdapter(this);
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
                Navigation.findNavController(v).navigateUp();
            }
        });

        binding.layoutPartnerInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToPartnerProfile();
            }
        });

        binding.buttonMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMenuPopup(v);
            }
        });
    }

    private void setupProductBanner() {
        binding.cardProductBanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Listing listing = viewModel.getListing().getValue();
                if (listing != null && listing.getId() != null) {
                    Bundle bundle = new Bundle();
                    bundle.putString("listingId", listing.getId());
                    Navigation.findNavController(v).navigate(R.id.action_global_listing_detail, bundle);
                }
            }
        });
    }

    private void showMenuPopup(View anchor) {
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        popup.getMenu().add(0, 1, 0, R.string.trade_chat_menu_profile);
        popup.getMenu().add(0, 2, 1, R.string.trade_chat_menu_report_user);

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == 1) {
                    navigateToPartnerProfile();
                    return true;
                } else if (item.getItemId() == 2) {
                    navigateToReportUser();
                    return true;
                }
                return false;
            }
        });
        popup.show();
    }

    private void navigateToPartnerProfile() {
        if (currentPartnerId != null && !currentPartnerId.isEmpty()) {
            Bundle bundle = new Bundle();
            bundle.putString("userId", currentPartnerId);
            Navigation.findNavController(requireView()).navigate(R.id.action_global_user_profile, bundle);
        }
    }

    private void navigateToReportUser() {
        if (currentPartnerId != null && !currentPartnerId.isEmpty()) {
            Bundle bundle = new Bundle();
            bundle.putString("targetType", "USER");
            bundle.putString("targetId", currentPartnerId);
            Navigation.findNavController(requireView()).navigate(R.id.action_global_report, bundle);
        }
    }

    private void checkNetwork() {
        try {
            isOnline = NetworkMonitor.getInstance().isOnline();
            binding.includeOfflineBanner.layoutOfflineBanner.setVisibility(
                    isOnline ? View.GONE : View.VISIBLE
            );
            binding.buttonAttachImage.setEnabled(isOnline);
            String text = binding.editMessage.getText() != null
                    ? binding.editMessage.getText().toString().trim() : "";
            binding.buttonSend.setEnabled(!text.isEmpty() && isOnline);
        } catch (UnsupportedOperationException e) {
            isOnline = true;
            binding.includeOfflineBanner.layoutOfflineBanner.setVisibility(View.GONE);
        }
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
                    binding.textPartnerName.setText(name != null && !name.isEmpty() ? name : "Người dùng");

                    if (partnerUser.getAvatarBase64() != null && !partnerUser.getAvatarBase64().isEmpty()) {
                        ImageLoader.load(
                                partnerUser.getAvatarBase64(),
                                binding.includeAvatar.imageAvatar,
                                R.drawable.bg_avatar,
                                R.drawable.bg_avatar
                        );
                        binding.includeAvatar.imageAvatar.setVisibility(View.VISIBLE);
                        binding.includeAvatar.textInitial.setVisibility(View.GONE);
                    } else {
                        binding.includeAvatar.imageAvatar.setVisibility(View.GONE);
                        binding.includeAvatar.textInitial.setVisibility(View.VISIBLE);
                        binding.includeAvatar.textInitial.setText(
                                name != null && !name.isEmpty() ? name.substring(0, 1).toUpperCase() : "U"
                        );
                    }

                    // Trạng thái hoạt động trong vòng 5 phút
                    long fiveMinutesAgo = System.currentTimeMillis() - (5 * 60 * 1000);
                    Timestamp fiveMinTimestamp = new Timestamp(new Date(fiveMinutesAgo));
                    if (partnerUser.isActiveSince(fiveMinTimestamp)) {
                        binding.textPartnerStatus.setText(R.string.trade_chat_active_now);
                    } else if (partnerUser.getLastActiveAt() != null) {
                        String timeStr = formatUtils.formatTime(partnerUser.getLastActiveAt());
                        binding.textPartnerStatus.setText(
                                String.format(getString(R.string.trade_chat_active_recent), timeStr)
                        );
                    } else {
                        binding.textPartnerStatus.setText("");
                    }

                    // Kiểm tra tài khoản bị khóa hoặc xóa
                    if (partnerUser.isLocked() || partnerUser.isDeleted()) {
                        binding.editMessage.setEnabled(false);
                        binding.buttonSend.setEnabled(false);
                        binding.buttonAttachImage.setEnabled(false);
                        binding.textLockedWarning.setVisibility(View.VISIBLE);
                    } else {
                        binding.editMessage.setEnabled(true);
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
                    Snackbar.make(binding.getRoot(), err, Snackbar.LENGTH_LONG).show();
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
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
