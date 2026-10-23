package com.nhom3.pubgapp.viewmodel;

import android.net.Uri;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.nhom3.pubgapp.data.ChatRepository;
import com.nhom3.pubgapp.data.DataCallback;
import com.nhom3.pubgapp.model.ChatMessage;
import com.nhom3.pubgapp.model.Conversation;
import com.nhom3.pubgapp.model.Listing;
import com.nhom3.pubgapp.model.Offer;
import com.nhom3.pubgapp.model.User;

import java.util.List;

// State for the conversation list and the chat screen. Action bodies report NOT_READY until the repositories are filled.
public class ChatViewModel extends ViewModel {

    private final MutableLiveData<List<Conversation>> conversations = new MutableLiveData<>();
    private final MutableLiveData<List<ChatMessage>> messages = new MutableLiveData<>();
    private final MutableLiveData<User> partner = new MutableLiveData<>();
    private final MutableLiveData<Listing> listing = new MutableLiveData<>();
    private final MutableLiveData<List<Offer>> offers = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> done = new MutableLiveData<>();

    private final ChatRepository chatRepository = new ChatRepository();
    private String currentConversationId;
    private Conversation currentConversation;
    private com.google.firebase.firestore.ListenerRegistration messageListener;
    private com.google.firebase.firestore.ListenerRegistration offersListener;

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

    public void loadConversations() {
        loading.setValue(true);
        String uid = null;
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        if (uid == null || uid.isEmpty()) {
            loading.setValue(false);
            error.setValue("Vui lòng đăng nhập để xem tin nhắn.");
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
        loading.setValue(true);
        String uid = null;
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        if (uid == null || uid.isEmpty()) {
            loading.setValue(false);
            error.setValue("Vui lòng đăng nhập để nhắn tin.");
            return;
        }
        chatRepository.getOrCreateConversation(listingId, uid, new DataCallback<Conversation>() {
            @Override
            public void onSuccess(Conversation result) {
                loading.postValue(false);
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

    public void openConversationById(String id) {
        if (id == null || id.isEmpty()) {
            error.setValue("Mã cuộc trò chuyện không hợp lệ.");
            return;
        }
        currentConversationId = id;
        startObservingMessages(id);
        observeOffers();
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("conversations").document(id).get()
                .addOnCompleteListener(
                        new com.google.android.gms.tasks.OnCompleteListener<
                                com.google.firebase.firestore.DocumentSnapshot>() {
                            @Override
                            public void onComplete(
                                    @androidx.annotation.NonNull
                                    com.google.android.gms.tasks.Task<
                                            com.google.firebase.firestore.DocumentSnapshot> task) {
                                if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                                    Conversation conv = task.getResult().toObject(Conversation.class);
                                    if (conv != null) {
                                        conv.setId(id);
                                        currentConversation = conv;
                                        loadPartnerInternal(conv);
                                        loadListingInternal(conv.getListingId());
                                    }
                                }
                            }
                        });
        done.setValue(true);
    }

    private void startObservingMessages(final String conversationId) {
        if (messageListener != null) {
            messageListener.remove();
            messageListener = null;
        }
        try {
            final android.content.Context ctx =
                    com.google.firebase.FirebaseApp.getInstance().getApplicationContext();
            if (ctx != null) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        com.nhom3.pubgapp.data.local.ChatDao dao =
                                com.nhom3.pubgapp.data.local.AppDatabase.getInstance(ctx).chatDao();
                        if (dao != null) {
                            List<com.nhom3.pubgapp.data.local.MessageEntity> cached =
                                    dao.getMessages(conversationId);
                            if (cached != null && !cached.isEmpty()) {
                                List<ChatMessage> list = new java.util.ArrayList<ChatMessage>();
                                for (int i = 0; i < cached.size(); i++) {
                                    list.add(cached.get(i).toModel());
                                }
                                messages.postValue(list);
                            }
                        }
                    }
                }).start();
            }
        } catch (Exception ignored) {
        }

        messageListener = chatRepository.observeMessages(conversationId, new DataCallback<List<ChatMessage>>() {
            @Override
            public void onSuccess(List<ChatMessage> result) {
                messages.postValue(result);
            }

            @Override
            public void onError(String message) {
                error.postValue(message);
            }
        });
    }

    public void loadOlder() {
        if (currentConversationId == null || currentConversationId.isEmpty()) {
            return;
        }
        List<ChatMessage> currentList = messages.getValue();
        if (currentList == null || currentList.isEmpty()) {
            return;
        }
        ChatMessage oldest = currentList.get(0);
        if (oldest.getCreatedAt() == null) {
            return;
        }
        loading.setValue(true);
        chatRepository.loadOlder(currentConversationId, oldest.getCreatedAt(),
                new DataCallback<List<ChatMessage>>() {
                    @Override
                    public void onSuccess(List<ChatMessage> olderMessages) {
                        loading.postValue(false);
                        if (olderMessages != null && !olderMessages.isEmpty()) {
                            List<ChatMessage> combined = new java.util.ArrayList<ChatMessage>(olderMessages);
                            List<ChatMessage> existing = messages.getValue();
                            if (existing != null) {
                                combined.addAll(existing);
                            }
                            messages.postValue(combined);
                        }
                    }

                    @Override
                    public void onError(String message) {
                        loading.postValue(false);
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
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "";
        String partnerId = conv.otherParty(uid);
        if (partnerId != null && !partnerId.isEmpty()) {
            new com.nhom3.pubgapp.data.UserRepository().getProfile(partnerId, new DataCallback<User>() {
                @Override
                public void onSuccess(User result) {
                    partner.postValue(result);
                }

                @Override
                public void onError(String message) {
                    error.postValue(message);
                }
            });
        }
    }

    public void loadListing() {
        if (currentConversation != null) {
            loadListingInternal(currentConversation.getListingId());
        }
    }

    private void loadListingInternal(String listingId) {
        if (listingId != null && !listingId.isEmpty()) {
            new com.nhom3.pubgapp.data.ListingRepository().getById(listingId, new DataCallback<Listing>() {
                @Override
                public void onSuccess(Listing result) {
                    listing.postValue(result);
                }

                @Override
                public void onError(String message) {
                    error.postValue(message);
                }
            });
        }
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
                offers.postValue(result);
            }

            @Override
            public void onError(String message) {
                error.postValue(message);
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

    public void makeOffer(Offer offer) {
        loading.setValue(true);
        String uid = null;
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        if (uid == null || uid.isEmpty()) {
            loading.setValue(false);
            error.setValue("Vui lòng đăng nhập để gửi đề nghị.");
            return;
        }

        new com.nhom3.pubgapp.data.UserRepository().getProfile(uid, new DataCallback<User>() {
            @Override
            public void onSuccess(User user) {
                boolean emailVerified = new com.nhom3.pubgapp.data.AuthRepository().isEmailVerified();
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

    public void respondToOffer(String offerId, boolean accept) {
        loading.setValue(true);
        String uid = null;
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        if (uid == null || uid.isEmpty()) {
            loading.setValue(false);
            error.setValue("Vui lòng đăng nhập để thực hiện thao tác.");
            return;
        }

        new com.nhom3.pubgapp.data.UserRepository().getProfile(uid, new DataCallback<User>() {
            @Override
            public void onSuccess(User user) {
                boolean emailVerified = new com.nhom3.pubgapp.data.AuthRepository().isEmailVerified();
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

    static boolean canUserBuy(User user, boolean emailVerified) {
        if (user == null) {
            return false;
        }
        try {
            return user.canBuy(emailVerified);
        } catch (UnsupportedOperationException e) {
            return user.getVerificationStatus()
                    == com.nhom3.pubgapp.model.enums.VerificationStatus.APPROVED && emailVerified;
        }
    }

    static boolean canUserSell(User user, boolean emailVerified) {
        if (user == null) {
            return false;
        }
        try {
            return user.canSell(emailVerified);
        } catch (UnsupportedOperationException e) {
            return user.getVerificationStatus()
                    == com.nhom3.pubgapp.model.enums.VerificationStatus.APPROVED && emailVerified;
        }
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
        if (offersListener != null) {
            offersListener.remove();
            offersListener = null;
        }
    }
}
