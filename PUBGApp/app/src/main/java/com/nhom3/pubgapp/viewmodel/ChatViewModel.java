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
                    currentConversationId = result.getId();
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
        done.setValue(true);
    }

    public void loadOlder() {
        error.setValue(DataCallback.NOT_READY);
    }

    public void loadPartner() {
        error.setValue(DataCallback.NOT_READY);
    }

    public void loadListing() {
        error.setValue(DataCallback.NOT_READY);
    }

    public void observeOffers() {
        error.setValue(DataCallback.NOT_READY);
    }

    public void sendText(String text) {
        error.setValue(DataCallback.NOT_READY);
    }

    public void sendImage(Uri uri) {
        error.setValue(DataCallback.NOT_READY);
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
}
