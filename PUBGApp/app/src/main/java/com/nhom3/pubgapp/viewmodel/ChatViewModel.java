package com.nhom3.pubgapp.viewmodel;

import android.content.Context;
import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.ListenerRegistration;
import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.data.ChatRepository;
import com.nhom3.pubgapp.data.DataCallback;
import com.nhom3.pubgapp.data.ListingRepository;
import com.nhom3.pubgapp.data.UserRepository;
import com.nhom3.pubgapp.model.ChatMessage;
import com.nhom3.pubgapp.model.Conversation;
import com.nhom3.pubgapp.model.Listing;
import com.nhom3.pubgapp.model.Offer;
import com.nhom3.pubgapp.model.User;
import com.nhom3.pubgapp.util.SessionManager;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// State for the conversation list and the chat screen.
public class ChatViewModel extends ViewModel {

    private final MutableLiveData<List<Conversation>> conversations = new MutableLiveData<List<Conversation>>();
    private final MutableLiveData<List<ChatMessage>> messages = new MutableLiveData<List<ChatMessage>>();
    private final MutableLiveData<User> partner = new MutableLiveData<User>();
    private final MutableLiveData<Listing> listing = new MutableLiveData<Listing>();
    private final MutableLiveData<List<Offer>> offers = new MutableLiveData<List<Offer>>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<Boolean>();
    private final MutableLiveData<String> error = new MutableLiveData<String>();
    private final MutableLiveData<Boolean> done = new MutableLiveData<Boolean>();

    private final ChatRepository chatRepository = new ChatRepository();
    private final UserRepository userRepository = new UserRepository();
    private final ListingRepository listingRepository = new ListingRepository();

    private String currentConversationId;
    private Conversation currentConversation;
    private ListenerRegistration messageListener;
    private boolean isLoadingOlder = false;

    public LiveData<List<Conversation>> getConversations() {
        return conversations;
    }

    public LiveData<List<ChatMessage>> getMessages() {
        return messages;
    }

    public LiveData<User> getPartner() {
        return partner;
    }

    public LiveData<Listing> getListing() {
        return listing;
    }

    public LiveData<List<Offer>> getOffers() {
        return offers;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<Boolean> getDone() {
        return done;
    }

    private String getString(int resId, String fallback) {
        try {
            Context context = FirebaseApp.getInstance().getApplicationContext();
            if (context != null) {
                return context.getString(resId);
            }
        } catch (Exception ignored) {
        }
        return fallback;
    }

    public void loadConversations() {
        loading.setValue(true);
        String uid = chatRepository.currentUserId();
        if (uid.isEmpty()) {
            uid = SessionManager.getInstance().getCurrentUserId();
        }
        if (uid.isEmpty()) {
            loading.setValue(false);
            error.setValue(getString(
                    R.string.trade_error_not_signed_in_messages,
                    "Vui lòng đăng nhập để xem tin nhắn."));
            return;
        }
        chatRepository.getConversations(uid, new DataCallback<List<Conversation>>() {
            @Override
            public void onSuccess(List<Conversation> result) {
                loading.postValue(false);
                conversations.postValue(result);
            }

            @Override
            public void onError(String message) {
                loading.postValue(false);
                error.postValue(message);
            }
        });
    }

    public void openConversation(String listingId) {
        String uid = chatRepository.currentUserId();
        if (uid.isEmpty()) {
            uid = SessionManager.getInstance().getCurrentUserId();
        }
        if (uid.isEmpty()) {
            error.setValue(getString(
                    R.string.trade_error_not_signed_in_chat,
                    "Vui lòng đăng nhập để nhắn tin."));
            return;
        }
        String expectedConvId = listingId + "_" + uid;
        if (expectedConvId.equals(currentConversationId) && currentConversation != null) {
            return;
        }
        clearPreviousConversation();
        loading.setValue(true);
        chatRepository.getOrCreateConversation(listingId, uid, new DataCallback<Conversation>() {
            @Override
            public void onSuccess(Conversation result) {
                loading.postValue(false);
                if (result != null) {
                    currentConversation = result;
                    currentConversationId = result.getId();
                    startObservingMessages(result.getId());
                    loadPartnerInternal(result);
                    loadListingInternal(result.getListingId());
                }
                done.postValue(true);
            }

            @Override
            public void onError(String message) {
                loading.postValue(false);
                error.postValue(message);
            }
        });
    }

    public void openConversationById(final String id) {
        if (id == null || id.isEmpty()) {
            error.setValue(getString(
                    R.string.trade_error_invalid_conversation_id,
                    "Mã cuộc trò chuyện không hợp lệ."));
            return;
        }
        if (id.equals(currentConversationId) && currentConversation != null) {
            return;
        }
        clearPreviousConversation();
        currentConversationId = id;
        loading.setValue(true);
        startObservingMessages(id);
        chatRepository.getConversation(id, new DataCallback<Conversation>() {
            @Override
            public void onSuccess(Conversation result) {
                loading.postValue(false);
                if (result != null) {
                    currentConversation = result;
                    loadPartnerInternal(result);
                    loadListingInternal(result.getListingId());
                }
                done.postValue(true);
            }

            @Override
            public void onError(String message) {
                loading.postValue(false);
                error.postValue(message);
            }
        });
    }

    public void clearPreviousConversation() {
        if (messageListener != null) {
            messageListener.remove();
            messageListener = null;
        }
        currentConversation = null;
        currentConversationId = null;
        isLoadingOlder = false;
        messages.setValue(new ArrayList<ChatMessage>());
        partner.setValue(null);
        listing.setValue(null);
    }

    private void startObservingMessages(final String conversationId) {
        if (messageListener != null) {
            messageListener.remove();
            messageListener = null;
        }
        messageListener = chatRepository.observeMessages(conversationId, new DataCallback<List<ChatMessage>>() {
            @Override
            public void onSuccess(List<ChatMessage> result) {
                if (result == null) {
                    return;
                }
                List<ChatMessage> currentList = messages.getValue();
                if (currentList == null || currentList.isEmpty()) {
                    messages.postValue(new ArrayList<ChatMessage>(result));
                } else {
                    Map<String, ChatMessage> map = new LinkedHashMap<String, ChatMessage>();
                    for (int i = 0; i < currentList.size(); i++) {
                        ChatMessage m = currentList.get(i);
                        if (m != null && m.getId() != null) {
                            map.put(m.getId(), m);
                        }
                    }
                    for (int i = 0; i < result.size(); i++) {
                        ChatMessage m = result.get(i);
                        if (m != null && m.getId() != null) {
                            map.put(m.getId(), m);
                        }
                    }
                    messages.postValue(new ArrayList<ChatMessage>(map.values()));
                }
            }

            @Override
            public void onError(String message) {
                error.postValue(message);
            }
        });
    }

    public void loadOlder() {
        if (isLoadingOlder || currentConversationId == null || currentConversationId.isEmpty()) {
            return;
        }
        List<ChatMessage> currentList = messages.getValue();
        if (currentList == null || currentList.isEmpty()) {
            return;
        }
        ChatMessage oldest = currentList.get(0);
        if (oldest == null || oldest.getCreatedAt() == null) {
            return;
        }
        isLoadingOlder = true;
        chatRepository.loadOlder(currentConversationId, oldest.getCreatedAt(),
                new DataCallback<List<ChatMessage>>() {
                    @Override
                    public void onSuccess(List<ChatMessage> olderMessages) {
                        isLoadingOlder = false;
                        if (olderMessages != null && !olderMessages.isEmpty()) {
                            Map<String, ChatMessage> map = new LinkedHashMap<String, ChatMessage>();
                            for (int i = 0; i < olderMessages.size(); i++) {
                                ChatMessage m = olderMessages.get(i);
                                if (m != null && m.getId() != null) {
                                    map.put(m.getId(), m);
                                }
                            }
                            List<ChatMessage> existing = messages.getValue();
                            if (existing != null) {
                                for (int i = 0; i < existing.size(); i++) {
                                    ChatMessage m = existing.get(i);
                                    if (m != null && m.getId() != null) {
                                        map.put(m.getId(), m);
                                    }
                                }
                            }
                            messages.postValue(new ArrayList<ChatMessage>(map.values()));
                        }
                    }

                    @Override
                    public void onError(String message) {
                        isLoadingOlder = false;
                        error.postValue(message);
                    }
                });
    }

    public void loadPartner() {
        if (currentConversation != null) {
            loadPartnerInternal(currentConversation);
        }
    }

    private void loadPartnerInternal(Conversation conv) {
        String uid = chatRepository.currentUserId();
        if (uid.isEmpty()) {
            uid = SessionManager.getInstance().getCurrentUserId();
        }
        String partnerId = conv.otherParty(uid);
        if (partnerId != null && !partnerId.isEmpty()) {
            userRepository.getProfile(partnerId, new DataCallback<User>() {
                @Override
                public void onSuccess(User result) {
                    partner.postValue(result);
                }

                @Override
                public void onError(String message) {
                }
            });
        }
        if (!uid.isEmpty()) {
            userRepository.touchLastActive(uid);
        }
    }

    public void loadListing() {
        if (currentConversation != null) {
            loadListingInternal(currentConversation.getListingId());
        }
    }

    private void loadListingInternal(String listingId) {
        if (listingId != null && !listingId.isEmpty()) {
            listingRepository.getById(listingId, new DataCallback<Listing>() {
                @Override
                public void onSuccess(Listing result) {
                    listing.postValue(result);
                }

                @Override
                public void onError(String message) {
                }
            });
        }
    }

    public void observeOffers() {
        error.setValue(DataCallback.NOT_READY);
    }

    public void sendText(String text) {
        if (currentConversationId == null || currentConversationId.isEmpty()) {
            error.setValue("Chưa mở cuộc trò chuyện.");
            return;
        }
        loading.setValue(true);
        chatRepository.sendText(currentConversationId, text, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                loading.postValue(false);
                done.postValue(true);
            }

            @Override
            public void onError(String message) {
                loading.postValue(false);
                error.postValue(message);
            }
        });
    }

    public void sendImage(Uri uri) {
        if (currentConversationId == null || currentConversationId.isEmpty()) {
            error.setValue("Chưa mở cuộc trò chuyện.");
            return;
        }
        loading.setValue(true);
        chatRepository.sendImage(currentConversationId, uri, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                loading.postValue(false);
                done.postValue(true);
            }

            @Override
            public void onError(String message) {
                loading.postValue(false);
                error.postValue(message);
            }
        });
    }

    public void makeOffer(Offer offer) {
        error.setValue(DataCallback.NOT_READY);
    }

    public void respondToOffer(String offerId, boolean accept) {
        error.setValue(DataCallback.NOT_READY);
    }

    public void clearError() {
        error.setValue(null);
    }

    public void clearDone() {
        done.setValue(false);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (messageListener != null) {
            messageListener.remove();
            messageListener = null;
        }
    }
}
