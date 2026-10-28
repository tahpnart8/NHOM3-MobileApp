package com.nhom3.pubgapp.ui.notification;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.databinding.FragmentNotificationListBinding;
import com.nhom3.pubgapp.model.AppNotification;
import com.nhom3.pubgapp.model.User;
import com.nhom3.pubgapp.model.enums.NotificationType;
import com.nhom3.pubgapp.util.SessionManager;
import com.nhom3.pubgapp.viewmodel.NotificationViewModel;

import java.util.List;

// Screen C06: Notification list, marking notifications read, and navigation by type.
public class NotificationListFragment extends Fragment {

    static final int ACTION_UNKNOWN = -1;

    private FragmentNotificationListBinding binding;
    private NotificationViewModel viewModel;
    private NotificationAdapter adapter;

    static int getDestinationAction(NotificationType type) {
        if (type == null) {
            return ACTION_UNKNOWN;
        }
        switch (type) {
            case NEW_MESSAGE:
            case NEW_OFFER:
            case OFFER_RESULT:
                return R.id.action_global_chat;
            case DEAL_UPDATE:
            case PAYMENT_UPDATE:
                return R.id.action_global_deal_detail;
            case NEW_REVIEW:
                return R.id.action_global_seller_trust;
            case LISTING_MODERATED:
                return R.id.action_global_my_shop;
            case WARNING:
            case VERIFICATION_RESULT:
            case COMPLAINT_RESULT:
                return R.id.action_global_my_profile;
            default:
                return ACTION_UNKNOWN;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentNotificationListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(NotificationViewModel.class);

        setupToolbar();
        setupRecyclerView();
        setupEmptyAndErrorStates();
        setupObservers();

        if (viewModel.getNotifications().getValue() == null) {
            viewModel.load();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setupToolbar() {
        binding.includeToolbar.textTitle.setText(R.string.account_notification_title);
        binding.includeToolbar.buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavHostFragment.findNavController(NotificationListFragment.this).popBackStack();
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new NotificationAdapter(new NotificationAdapter.OnNotificationClickListener() {
            @Override
            public void onNotificationClick(AppNotification notification) {
                handleNotificationClick(notification);
            }
        });
        binding.recyclerNotifications.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerNotifications.setAdapter(adapter);
    }

    private void setupEmptyAndErrorStates() {
        binding.viewEmptyState.textEmptyTitle.setText(R.string.notification_list_empty);
        binding.viewEmptyState.textEmptyMessage.setText(R.string.notification_list_empty_message);
        binding.viewEmptyState.textEmptyMessage.setVisibility(View.VISIBLE);
        binding.viewErrorState.buttonRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.load();
            }
        });
    }

    private void setupObservers() {
        viewModel.getNotifications().observe(getViewLifecycleOwner(), new Observer<List<AppNotification>>() {
            @Override
            public void onChanged(List<AppNotification> items) {
                if (items == null || items.isEmpty()) {
                    binding.recyclerNotifications.setVisibility(View.GONE);
                    binding.viewEmptyState.layoutEmptyState.setVisibility(View.VISIBLE);
                    binding.viewErrorState.layoutErrorState.setVisibility(View.GONE);
                } else {
                    binding.recyclerNotifications.setVisibility(View.VISIBLE);
                    binding.viewEmptyState.layoutEmptyState.setVisibility(View.GONE);
                    binding.viewErrorState.layoutErrorState.setVisibility(View.GONE);
                    adapter.submitList(items);
                }
            }
        });

        viewModel.getLoading().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean loading) {
                boolean isLoading = loading != null && loading;
                binding.progressLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                if (isLoading) {
                    binding.viewErrorState.layoutErrorState.setVisibility(View.GONE);
                }
            }
        });

        viewModel.getError().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String err) {
                if (err != null && !err.isEmpty()) {
                    List<AppNotification> currentList = viewModel.getNotifications().getValue();
                    if (currentList == null || currentList.isEmpty()) {
                        binding.recyclerNotifications.setVisibility(View.GONE);
                        binding.viewEmptyState.layoutEmptyState.setVisibility(View.GONE);
                        binding.viewErrorState.layoutErrorState.setVisibility(View.VISIBLE);
                        binding.viewErrorState.textErrorMessage.setText(err);
                    } else {
                        Toast.makeText(requireContext(), err, Toast.LENGTH_SHORT).show();
                    }
                    viewModel.clearError();
                }
            }
        });
    }

    private void handleNotificationClick(AppNotification notification) {
        if (!notification.isRead()) {
            viewModel.markRead(notification.getId());
        }

        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null && (currentUser.isLocked() || currentUser.isDeleted())) {
            Toast.makeText(requireContext(), R.string.notification_list_account_inactive, Toast.LENGTH_SHORT).show();
            return;
        }

        int actionId = getDestinationAction(notification.getType());
        if (actionId == ACTION_UNKNOWN) {
            return;
        }

        Bundle args = new Bundle();
        if (actionId == R.id.action_global_chat) {
            String targetId = notification.getTargetId();
            if (targetId == null || targetId.isEmpty()) {
                return;
            }
            args.putString("conversationId", targetId);
            args.putString("listingId", "");
        } else if (actionId == R.id.action_global_deal_detail) {
            String targetId = notification.getTargetId();
            if (targetId == null || targetId.isEmpty()) {
                return;
            }
            args.putString("dealId", targetId);
        } else if (actionId == R.id.action_global_seller_trust) {
            String uid = SessionManager.getInstance().getCurrentUserId();
            args.putString("userId", uid != null ? uid : "");
        }

        NavHostFragment.findNavController(NotificationListFragment.this).navigate(actionId, args);
    }
}
