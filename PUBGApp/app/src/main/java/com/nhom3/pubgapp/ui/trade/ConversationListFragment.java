package com.nhom3.pubgapp.ui.trade;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.databinding.FragmentConversationListBinding;
import com.nhom3.pubgapp.model.Conversation;
import com.nhom3.pubgapp.util.NetworkMonitor;
import com.nhom3.pubgapp.viewmodel.ChatViewModel;

import java.util.ArrayList;
import java.util.List;

// Screen C01: displays user conversations and opens C02 chat.
public class ConversationListFragment extends Fragment implements ConversationAdapter.OnConversationClickListener {

    private FragmentConversationListBinding binding;
    private ChatViewModel viewModel;
    private ConversationAdapter adapter;
    private final List<Conversation> fullList = new ArrayList<Conversation>();
    private String currentSearchQuery = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentConversationListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(ChatViewModel.class);

        setupRecyclerView();
        setupSearch();
        setupRetryButton();
        observeViewModel();

        checkNetworkAndShowBanner();

        if (viewModel.getConversations().getValue() == null) {
            viewModel.loadConversations();
        }
    }

    private void setupRecyclerView() {
        adapter = new ConversationAdapter(this);
        binding.recyclerConversations.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerConversations.setAdapter(adapter);
    }

    private void setupSearch() {
        binding.editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s != null ? s.toString().trim() : "";
                applyFilter();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setupRetryButton() {
        binding.includeErrorState.buttonRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.includeErrorState.layoutErrorState.setVisibility(View.GONE);
                viewModel.loadConversations();
            }
        });
    }

    private void observeViewModel() {
        viewModel.getLoading().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isLoading) {
                if (isLoading != null && isLoading) {
                    binding.progressLoading.setVisibility(View.VISIBLE);
                    binding.includeErrorState.layoutErrorState.setVisibility(View.GONE);
                } else {
                    binding.progressLoading.setVisibility(View.GONE);
                }
            }
        });

        viewModel.getConversations().observe(getViewLifecycleOwner(), new Observer<List<Conversation>>() {
            @Override
            public void onChanged(List<Conversation> conversations) {
                fullList.clear();
                if (conversations != null) {
                    fullList.addAll(conversations);
                }
                applyFilter();
            }
        });

        viewModel.getError().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String errorMessage) {
                if (errorMessage != null && !errorMessage.isEmpty()) {
                    if (fullList.isEmpty()) {
                        // Four states: Error state
                        binding.includeErrorState.layoutErrorState.setVisibility(View.VISIBLE);
                        binding.includeErrorState.textErrorMessage.setText(errorMessage);
                        binding.recyclerConversations.setVisibility(View.GONE);
                        binding.includeEmptyState.layoutEmptyState.setVisibility(View.GONE);
                    } else {
                        // Have cached data, show offline banner
                        binding.includeOfflineBanner.layoutOfflineBanner.setVisibility(View.VISIBLE);
                    }
                    viewModel.clearError();
                }
            }
        });
    }

    private void checkNetworkAndShowBanner() {
        try {
            boolean isOnline = NetworkMonitor.getInstance().isOnline();
            binding.includeOfflineBanner.layoutOfflineBanner.setVisibility(
                    isOnline ? View.GONE : View.VISIBLE
            );
        } catch (UnsupportedOperationException e) {
            binding.includeOfflineBanner.layoutOfflineBanner.setVisibility(View.GONE);
        }
    }

    private void applyFilter() {
        List<Conversation> filtered = new ArrayList<Conversation>();
        String query = currentSearchQuery.toLowerCase();

        for (int i = 0; i < fullList.size(); i++) {
            Conversation conv = fullList.get(i);
            if (query.isEmpty()) {
                filtered.add(conv);
            } else {
                String msg = conv.getLastMessage() != null ? conv.getLastMessage().toLowerCase() : "";
                String fallback = getString(R.string.trade_conversation_list_partner_fallback).toLowerCase();
                if (msg.contains(query) || fallback.contains(query)) {
                    filtered.add(conv);
                }
            }
        }

        adapter.setConversations(filtered);

        if (fullList.isEmpty()) {
            binding.includeEmptyState.layoutEmptyState.setVisibility(View.VISIBLE);
            binding.includeEmptyState.textEmptyTitle.setText(R.string.trade_conversation_list_empty_title);
            binding.includeEmptyState.textEmptyMessage.setText(R.string.trade_conversation_list_empty_message);
            binding.recyclerConversations.setVisibility(View.GONE);
        } else if (filtered.isEmpty()) {
            binding.includeEmptyState.layoutEmptyState.setVisibility(View.VISIBLE);
            binding.includeEmptyState.textEmptyTitle.setText(R.string.trade_conversation_list_empty_search);
            binding.includeEmptyState.textEmptyMessage.setText("");
            binding.recyclerConversations.setVisibility(View.GONE);
        } else {
            binding.includeEmptyState.layoutEmptyState.setVisibility(View.GONE);
            binding.recyclerConversations.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onConversationClick(Conversation conversation) {
        if (conversation == null) {
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putString("listingId", conversation.getListingId());
        bundle.putString("conversationId", conversation.getId());
        Navigation.findNavController(requireView()).navigate(R.id.action_global_chat, bundle);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
