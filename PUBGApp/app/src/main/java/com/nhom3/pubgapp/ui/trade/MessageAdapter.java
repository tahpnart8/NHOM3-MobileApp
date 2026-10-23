package com.nhom3.pubgapp.ui.trade;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.data.DataCallback;
import com.nhom3.pubgapp.data.MediaRepository;
import com.nhom3.pubgapp.databinding.ItemTradeMessageImageBinding;
import com.nhom3.pubgapp.databinding.ItemTradeMessageTextBinding;
import com.nhom3.pubgapp.model.ChatMessage;
import com.nhom3.pubgapp.model.enums.MessageType;
import com.nhom3.pubgapp.util.FormatUtils;

import java.util.ArrayList;
import java.util.List;

// Adapter for chat messages displaying text and image bubbles.
public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnMessageLongClickListener {
        void onMessageLongClick(ChatMessage message);
    }

    private static final int VIEW_TYPE_TEXT = 1;
    private static final int VIEW_TYPE_IMAGE = 2;

    private final List<ChatMessage> messages = new ArrayList<ChatMessage>();
    private final FormatUtils formatUtils = new FormatUtils();
    private final MediaRepository mediaRepository = new MediaRepository();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private String currentUserId = "";
    private final OnMessageLongClickListener longClickListener;

    public MessageAdapter(OnMessageLongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }

    public void setCurrentUserId(String currentUserId) {
        this.currentUserId = currentUserId != null ? currentUserId : "";
        notifyDataSetChanged();
    }

    public void setMessages(List<ChatMessage> items) {
        messages.clear();
        if (items != null) {
            messages.addAll(items);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage msg = messages.get(position);
        if (msg.getType() == MessageType.IMAGE) {
            return VIEW_TYPE_IMAGE;
        }
        return VIEW_TYPE_TEXT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_IMAGE) {
            ItemTradeMessageImageBinding binding = ItemTradeMessageImageBinding.inflate(inflater, parent, false);
            return new ImageMessageViewHolder(binding);
        }
        ItemTradeMessageTextBinding binding = ItemTradeMessageTextBinding.inflate(inflater, parent, false);
        return new TextMessageViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage msg = messages.get(position);
        if (holder instanceof TextMessageViewHolder) {
            ((TextMessageViewHolder) holder).bind(msg);
        } else if (holder instanceof ImageMessageViewHolder) {
            ((ImageMessageViewHolder) holder).bind(msg);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    class TextMessageViewHolder extends RecyclerView.ViewHolder {

        private final ItemTradeMessageTextBinding binding;

        TextMessageViewHolder(@NonNull ItemTradeMessageTextBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(final ChatMessage message) {
            boolean isSent = currentUserId.equals(message.getSenderId());
            String time = formatUtils.formatTime(message.getCreatedAt());

            String displayText;
            if (message.getType() == MessageType.OFFER) {
                displayText = itemView.getContext().getString(R.string.trade_chat_offer_placeholder);
            } else {
                displayText = message.getText() != null ? message.getText() : "";
            }

            if (isSent) {
                binding.cardSent.setVisibility(View.VISIBLE);
                binding.cardReceived.setVisibility(View.GONE);
                binding.textMessageSent.setText(displayText);
                binding.textTimeSent.setText(time);
            } else {
                binding.cardSent.setVisibility(View.GONE);
                binding.cardReceived.setVisibility(View.VISIBLE);
                binding.textMessageReceived.setText(displayText);
                binding.textTimeReceived.setText(time);

                binding.cardReceived.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if (longClickListener != null) {
                            longClickListener.onMessageLongClick(message);
                            return true;
                        }
                        return false;
                    }
                });
            }
        }
    }

    class ImageMessageViewHolder extends RecyclerView.ViewHolder {

        private final ItemTradeMessageImageBinding binding;

        ImageMessageViewHolder(@NonNull ItemTradeMessageImageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(final ChatMessage message) {
            boolean isSent = currentUserId.equals(message.getSenderId());
            String time = formatUtils.formatTime(message.getCreatedAt());

            if (isSent) {
                binding.cardImageSent.setVisibility(View.VISIBLE);
                binding.cardImageReceived.setVisibility(View.GONE);
                binding.textTimeImageSent.setText(time);
                loadImage(message.getImageId(), binding.imageSent);
            } else {
                binding.cardImageSent.setVisibility(View.GONE);
                binding.cardImageReceived.setVisibility(View.VISIBLE);
                binding.textTimeImageReceived.setText(time);
                loadImage(message.getImageId(), binding.imageReceived);

                binding.cardImageReceived.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if (longClickListener != null) {
                            longClickListener.onMessageLongClick(message);
                            return true;
                        }
                        return false;
                    }
                });
            }
        }

        private void loadImage(String imageId, final android.widget.ImageView imageView) {
            if (imageId == null || imageId.isEmpty()) {
                imageView.setImageDrawable(null);
                return;
            }
            mediaRepository.getImage(imageId, new DataCallback<byte[]>() {
                @Override
                public void onSuccess(final byte[] bytes) {
                    if (bytes != null && bytes.length > 0) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                final Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                mainHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        imageView.setImageBitmap(bmp);
                                    }
                                });
                            }
                        }).start();
                    }
                }

                @Override
                public void onError(String message) {
                    imageView.setImageDrawable(null);
                }
            });
        }
    }
}
