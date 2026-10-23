package com.nhom3.pubgapp.data;

import android.net.Uri;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ListenerRegistration;
import com.nhom3.pubgapp.model.ChatMessage;
import com.nhom3.pubgapp.model.Conversation;
import com.nhom3.pubgapp.model.Offer;
import java.util.List;

// Skeleton: the module owner fills each body as the issue assigns it.
public class ChatRepository extends BaseRepository {

    public void getOrCreateConversation(String listingId, String buyerId, DataCallback<Conversation> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void getConversations(String uid, DataCallback<List<Conversation>> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public ListenerRegistration observeMessages(String id, DataCallback<List<ChatMessage>> cb) {
        cb.onError(DataCallback.NOT_READY);
        return null;
    }

    public void loadOlder(String id, Timestamp before, DataCallback<List<ChatMessage>> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void getMessagesOnce(String id, DataCallback<List<ChatMessage>> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void sendText(String id, String text, DataCallback<Void> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void sendImage(String id, Uri uri, DataCallback<Void> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void sendOffer(Offer offer, DataCallback<String> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void respondToOffer(String offerId, boolean accept, DataCallback<String> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public ListenerRegistration observeOffers(String conversationId, DataCallback<Void> cb) {
        cb.onError(DataCallback.NOT_READY);
        return null;
    }
}
