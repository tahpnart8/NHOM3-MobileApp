package com.nhom3.pubgapp.viewmodel;

import android.content.Context;
import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.ListenerRegistration;
import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.data.AuthRepository;
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
    private final MutableLiveData<String> createdDealId = new MutableLiveData<String>();

    private final ChatRepository chatRepository = new ChatRepository();
    private final UserRepository userRepository = new UserRepository();
    private final ListingRepository listingRepository = new ListingRepository();
    private final AuthRepository authRepository = new AuthRepository();

    private String currentConversationId;
    private Conversation currentConversation;
    private ListenerRegistration messageListener;
    private ListenerRegistration offersListener;
    private boolean isLoadingOlder = false;
    // The conversation list is live too, so a new message or offer reorders it at once.
    private final ListenerSet listeners = new ListenerSet();
    private String liveConversationsUid;
    private static final String KEY_CONVERSATIONS = "conversations";
    private static final String KEY_LISTING = "listing";
    private String liveListingId;
    // The merged messages live here, not in messages.getValue(), which lags while a postValue is pending.
    private final Map<String, ChatMessage> messageById = new LinkedHashMap<String, ChatMessage>();
    // Bumped when a conversation is closed, so a late answer for the old one is dropped.
    private int openGeneration = 0;
    // The listing id or conversation id the chat screen opened with; closeConversation matches against it.
    private String openedWith;

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

    public LiveData<String> getCreatedDealId() {
        return createdDealId;
    }

    public void clearCreatedDealId() {
        createdDealId.setValue(null);
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
        if (uid.equals(liveConversationsUid) && listeners.has(KEY_CONVERSATIONS)) {
            loading.setValue(false);
            return;
        }
        liveConversationsUid = uid;
        listeners.put(KEY_CONVERSATIONS, chatRepository.observeConversations(uid,
                new DataCallback<List<Conversation>>() {
                    @Override
                    public void onSuccess(List<Conversation> result) {
                        loading.postValue(false);
                        conversations.postValue(result);
                    }

                    @Override
                    public void onError(String message) {
                        listeners.remove(KEY_CONVERSATIONS);
                        loading.postValue(false);
                        error.postValue(message);
                    }
                }));
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
        openedWith = listingId;
        final int generation = openGeneration;
        loading.setValue(true);
        chatRepository.getOrCreateConversation(listingId, uid, new DataCallback<Conversation>() {
            @Override
            public void onSuccess(Conversation result) {
                loading.postValue(false);
                if (generation != openGeneration) {
                    return;
                }
                if (result != null) {
                    currentConversation = result;
                    currentConversationId = result.getId();
                    startObservingMessages(result.getId());
                    observeOffers();
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
        openedWith = id;
        final int generation = openGeneration;
        currentConversationId = id;
        loading.setValue(true);
        startObservingMessages(id);
        observeOffers();
        chatRepository.getConversation(id, new DataCallback<Conversation>() {
            @Override
            public void onSuccess(Conversation result) {
                loading.postValue(false);
                if (generation != openGeneration) {
                    return;
                }
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
        if (offersListener != null) {
            offersListener.remove();
            offersListener = null;
        }
        listeners.remove(KEY_LISTING);
        liveListingId = null;
        openedWith = null;
        openGeneration++;
        currentConversation = null;
        currentConversationId = null;
        isLoadingOlder = false;
        synchronized (messageById) {
            messageById.clear();
        }
        messages.setValue(new ArrayList<ChatMessage>());
        partner.setValue(null);
        listing.setValue(null);
        offers.setValue(new ArrayList<Offer>());
        createdDealId.setValue(null);
    }

    // Called when a chat screen is really closed. Another chat screen may already have opened a
    // different conversation, so only the one this screen opened is stopped.
    public void closeConversation(String screenOpenedWith) {
        if (screenOpenedWith == null || screenOpenedWith.isEmpty()) {
            return;
        }
        boolean sameOpen = screenOpenedWith.equals(openedWith);
        boolean sameId = screenOpenedWith.equals(currentConversationId);
        boolean sameListing = currentConversation != null
                && screenOpenedWith.equals(currentConversation.getListingId());
        if (sameOpen || sameId || sameListing) {
            clearPreviousConversation();
        }
    }

    private void startObservingMessages(final String conversationId) {
        if (messageListener != null) {
            messageListener.remove();
            messageListener = null;
        }
        messageListener = chatRepository.observeMessages(conversationId, new DataCallback<List<ChatMessage>>() {
            @Override
            public void onSuccess(List<ChatMessage> result) {
                if (result == null || !conversationId.equals(currentConversationId)) {
                    return;
                }
                List<ChatMessage> merged;
                synchronized (messageById) {
                    merged = mergeNewer(messageById, result);
                }
                messages.postValue(merged);
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
        ChatMessage oldest = oldestLoadedMessage();
        if (oldest == null || oldest.getCreatedAt() == null) {
            return;
        }
        isLoadingOlder = true;
        final String conversationId = currentConversationId;
        chatRepository.loadOlder(conversationId, oldest.getCreatedAt(),
                new DataCallback<List<ChatMessage>>() {
                    @Override
                    public void onSuccess(List<ChatMessage> olderMessages) {
                        isLoadingOlder = false;
                        if (!conversationId.equals(currentConversationId)) {
                            return;
                        }
                        if (olderMessages != null && !olderMessages.isEmpty()) {
                            List<ChatMessage> merged;
                            synchronized (messageById) {
                                merged = mergeOlder(messageById, olderMessages);
                            }
                            messages.postValue(merged);
                        }
                    }

                    @Override
                    public void onError(String message) {
                        isLoadingOlder = false;
                        error.postValue(message);
                    }
                });
    }

    private ChatMessage oldestLoadedMessage() {
        synchronized (messageById) {
            if (messageById.isEmpty()) {
                return null;
            }
            return messageById.values().iterator().next();
        }
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
                    error.postValue(getString(R.string.trade_error_load_partner,
                            "Không tải được thông tin người trò chuyện."));
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

    // A live listener, so the banner shows the new status and price as soon as an offer is accepted.
    private void loadListingInternal(final String listingId) {
        if (listingId == null || listingId.isEmpty()) {
            return;
        }
        if (listingId.equals(liveListingId) && listeners.has(KEY_LISTING)) {
            return;
        }
        liveListingId = listingId;
        listeners.put(KEY_LISTING, listingRepository.observeListing(listingId,
                new DataCallback<Listing>() {
                    @Override
                    public void onSuccess(Listing result) {
                        if (listingId.equals(liveListingId)) {
                            listing.postValue(result);
                        }
                    }

                    @Override
                    public void onError(String message) {
                        if (!listingId.equals(liveListingId)) {
                            return;
                        }
                        listeners.remove(KEY_LISTING);
                        error.postValue(getString(R.string.trade_error_load_listing,
                                "Không tải được tin đăng của cuộc trò chuyện."));
                    }
                }));
    }

    public void observeOffers() {
        if (currentConversationId == null || currentConversationId.isEmpty()) {
            return;
        }
        if (offersListener != null) {
            offersListener.remove();
            offersListener = null;
        }
        offersListener = chatRepository.observeOffers(currentConversationId, new DataCallback<List<Offer>>() {
            @Override
            public void onSuccess(List<Offer> result) {
                offers.postValue(result != null ? result : new ArrayList<Offer>());
            }

            @Override
            public void onError(String message) {
                error.postValue(getString(R.string.trade_error_load_offers,
                        "Không tải được các đề nghị của cuộc trò chuyện."));
            }
        });
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

    public void makeOffer(final Offer offer) {
        loading.setValue(true);
        String uid = SessionManager.getInstance().getCurrentUserId();
        if (uid.isEmpty()) {
            loading.setValue(false);
            error.setValue("Vui lòng đăng nhập để gửi đề nghị.");
            return;
        }
        userRepository.getProfile(uid, new DataCallback<User>() {
            @Override
            public void onSuccess(User user) {
                boolean emailVerified = authRepository.isEmailVerified();
                if (!canUserBuy(user, emailVerified)) {
                    loading.postValue(false);
                    error.postValue("Tài khoản cần được admin duyệt "
                            + "xác thực trước khi gửi đề nghị mua bán.");
                    return;
                }
                chatRepository.sendOffer(offer, new DataCallback<String>() {
                    @Override
                    public void onSuccess(String offerId) {
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

            @Override
            public void onError(String message) {
                loading.postValue(false);
                error.postValue(message);
            }
        });
    }

    public void respondToOffer(final String offerId, final boolean accept) {
        loading.setValue(true);
        String uid = SessionManager.getInstance().getCurrentUserId();
        if (uid.isEmpty()) {
            loading.setValue(false);
            error.setValue("Vui lòng đăng nhập để thực hiện thao tác.");
            return;
        }
        userRepository.getProfile(uid, new DataCallback<User>() {
            @Override
            public void onSuccess(User user) {
                boolean emailVerified = authRepository.isEmailVerified();
                if (!canUserSell(user, emailVerified)) {
                    loading.postValue(false);
                    error.postValue("Tài khoản cần được admin duyệt "
                            + "xác thực trước khi phản hồi đề nghị.");
                    return;
                }
                chatRepository.respondToOffer(offerId, accept, new DataCallback<String>() {
                    @Override
                    public void onSuccess(String dealId) {
                        loading.postValue(false);
                        loadListing();
                        if (dealId != null && !dealId.isEmpty()) {
                            createdDealId.postValue(dealId);
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

            @Override
            public void onError(String message) {
                loading.postValue(false);
                error.postValue(message);
            }
        });
    }

    // Newer snapshot: a known id is replaced in place, a new id goes to the end.
    static List<ChatMessage> mergeNewer(Map<String, ChatMessage> byId, List<ChatMessage> incoming) {
        for (int i = 0; i < incoming.size(); i++) {
            ChatMessage message = incoming.get(i);
            if (message != null && message.getId() != null) {
                byId.put(message.getId(), message);
            }
        }
        return new ArrayList<ChatMessage>(byId.values());
    }

    // Older page: those messages go before everything already loaded.
    static List<ChatMessage> mergeOlder(Map<String, ChatMessage> byId, List<ChatMessage> older) {
        Map<String, ChatMessage> rebuilt = new LinkedHashMap<String, ChatMessage>();
        for (int i = 0; i < older.size(); i++) {
            ChatMessage message = older.get(i);
            if (message != null && message.getId() != null && !byId.containsKey(message.getId())) {
                rebuilt.put(message.getId(), message);
            }
        }
        rebuilt.putAll(byId);
        byId.clear();
        byId.putAll(rebuilt);
        return new ArrayList<ChatMessage>(byId.values());
    }

    static boolean canUserBuy(User user, boolean emailVerified) {
        if (user == null) {
            return false;
        }
        return user.canBuy(emailVerified);
    }

    static boolean canUserSell(User user, boolean emailVerified) {
        if (user == null) {
            return false;
        }
        return user.canSell(emailVerified);
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
        listeners.clear();
        if (messageListener != null) {
            messageListener.remove();
            messageListener = null;
        }
        if (offersListener != null) {
            offersListener.remove();
            offersListener = null;
        }
    }
}
