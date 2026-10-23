package com.nhom3.pubgapp.viewmodel;

import android.net.Uri;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

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
        error.setValue(DataCallback.NOT_READY);
    }

    public void openConversation(String listingId) {
        error.setValue(DataCallback.NOT_READY);
    }

    public void openConversationById(String id) {
        error.setValue(DataCallback.NOT_READY);
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
