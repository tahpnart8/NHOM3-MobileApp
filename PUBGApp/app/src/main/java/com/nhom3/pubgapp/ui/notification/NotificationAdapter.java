package com.nhom3.pubgapp.ui.notification;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.databinding.ItemNotificationBinding;
import com.nhom3.pubgapp.model.AppNotification;
import com.nhom3.pubgapp.model.enums.NotificationType;
import com.nhom3.pubgapp.util.FormatUtils;

import java.util.ArrayList;
import java.util.List;

// Adapter for the notifications list screen C06.
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    public interface OnNotificationClickListener {
        void onNotificationClick(AppNotification notification);
    }

    private final List<AppNotification> items = new ArrayList<AppNotification>();
    private final OnNotificationClickListener clickListener;
    private final FormatUtils formatUtils = new FormatUtils();

    public NotificationAdapter(OnNotificationClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void submitList(List<AppNotification> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemNotificationBinding binding = ItemNotificationBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppNotification notification = items.get(position);
        holder.bind(notification);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemNotificationBinding binding;

        ViewHolder(ItemNotificationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(final AppNotification notification) {
            binding.textNotificationTitle.setText(notification.getTitle());
            binding.textNotificationBody.setText(notification.getBody());

            String timeText = formatUtils.formatTime(notification.getCreatedAt());
            binding.textNotificationTime.setText(timeText);

            boolean isUnread = !notification.isRead();
            binding.viewUnreadDot.setVisibility(isUnread ? View.VISIBLE : View.GONE);

            Context context = binding.getRoot().getContext();
            int iconRes = getIconResource(notification.getType());
            int descRes = getDescriptionResource(notification.getType());

            binding.imageNotificationType.setImageResource(iconRes);
            binding.imageNotificationType.setContentDescription(context.getString(descRes));

            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (clickListener != null) {
                        clickListener.onNotificationClick(notification);
                    }
                }
            });
        }

        private int getIconResource(NotificationType type) {
            if (type == null) {
                return R.drawable.ic_bell;
            }
            switch (type) {
                case DEAL_UPDATE:
                case PAYMENT_UPDATE:
                    return R.drawable.ic_notification_deal;
                case NEW_MESSAGE:
                case NEW_OFFER:
                case OFFER_RESULT:
                    return R.drawable.ic_notification_chat;
                case NEW_REVIEW:
                    return R.drawable.ic_notification_review;
                case WARNING:
                case COMPLAINT_RESULT:
                    return R.drawable.ic_notification_alert;
                case LISTING_MODERATED:
                    return R.drawable.ic_notification_listing;
                case VERIFICATION_RESULT:
                    return R.drawable.ic_notification_verification;
                default:
                    return R.drawable.ic_bell;
            }
        }

        private int getDescriptionResource(NotificationType type) {
            if (type == null) {
                return R.string.account_notification_title;
            }
            switch (type) {
                case NEW_MESSAGE:
                    return R.string.notification_type_new_message;
                case NEW_OFFER:
                    return R.string.notification_type_new_offer;
                case OFFER_RESULT:
                    return R.string.notification_type_offer_result;
                case DEAL_UPDATE:
                    return R.string.notification_type_deal_update;
                case PAYMENT_UPDATE:
                    return R.string.notification_type_payment_update;
                case NEW_REVIEW:
                    return R.string.notification_type_new_review;
                case COMPLAINT_RESULT:
                    return R.string.notification_type_complaint_result;
                case WARNING:
                    return R.string.notification_type_warning;
                case LISTING_MODERATED:
                    return R.string.notification_type_listing_moderated;
                case VERIFICATION_RESULT:
                    return R.string.notification_type_verification_result;
                default:
                    return R.string.account_notification_title;
            }
        }
    }
}
