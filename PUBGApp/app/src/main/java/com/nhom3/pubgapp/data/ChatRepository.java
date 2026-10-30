package com.nhom3.pubgapp.data;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.nhom3.pubgapp.data.local.AppDatabase;
import com.nhom3.pubgapp.data.local.ChatDao;
import com.nhom3.pubgapp.data.local.ConversationEntity;
import com.nhom3.pubgapp.data.local.MessageEntity;
import com.nhom3.pubgapp.model.AppNotification;
import com.nhom3.pubgapp.model.ChatMessage;
import com.nhom3.pubgapp.model.Conversation;
import com.nhom3.pubgapp.model.Deal;
import com.nhom3.pubgapp.model.Listing;
import com.nhom3.pubgapp.model.Offer;
import com.nhom3.pubgapp.model.enums.ImagePurpose;
import com.nhom3.pubgapp.model.enums.ListingStatus;
import com.nhom3.pubgapp.model.enums.MessageType;
import com.nhom3.pubgapp.model.enums.NotificationType;
import com.nhom3.pubgapp.model.enums.OfferStatus;
import com.nhom3.pubgapp.model.enums.OfferType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class ChatRepository extends BaseRepository {

    private static final String TAG = "ChatRepository";
    private static final String COLLECTION_CONVERSATIONS = "conversations";
    private static final String COLLECTION_MESSAGES = "messages";
    private static final String COLLECTION_OFFERS = "offers";
    private static final String COLLECTION_LISTINGS = "listings";
    private static final String COLLECTION_DEALS = "deals";

    private final NotificationRepository notificationRepository = new NotificationRepository();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private ChatDao getChatDao() {
        try {
            Context context = FirebaseApp.getInstance().getApplicationContext();
            if (context != null) {
                return AppDatabase.getInstance(context).chatDao();
            }
        } catch (Exception e) {
            Log.w(TAG, "Cannot access Room database", e);
        }
        return null;
    }

    @Override
    public String currentUserId() {
        return super.currentUserId();
    }

    static String buildConversationId(String listingId, String buyerId) {
        return listingId + "_" + buyerId;
    }

    static boolean isSameUser(String buyerId, String sellerId) {
        return buyerId != null && buyerId.equals(sellerId);
    }

    static boolean isListingStatusAllowed(ListingStatus status) {
        return status == ListingStatus.AVAILABLE || status == ListingStatus.RESERVED;
    }

    static String validateNewConversation(String buyerId, String sellerId, ListingStatus status) {
        if (buyerId == null || buyerId.trim().isEmpty()) {
            return "Vui lòng đăng nhập để nhắn tin.";
        }
        if (isSameUser(buyerId, sellerId)) {
            return "Không thể nhắn tin cho chính mình.";
        }
        if (status != null && !isListingStatusAllowed(status)) {
            return "Tin đăng không còn hoạt động.";
        }
        return null;
    }

    static String validateMessageText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "Nội dung tin nhắn không được để trống.";
        }
        if (text.length() > 2000) {
            return "Tin nhắn không được vượt quá 2000 ký tự.";
        }
        return null;
    }

    static boolean isValidMessageText(String text) {
        return text != null && !text.trim().isEmpty() && text.length() <= 2000;
    }

    static boolean isSenderAllowed(Conversation conv, String senderId) {
        if (conv == null || senderId == null) {
            return false;
        }
        return conv.isParticipant(senderId);
    }

    public void getOrCreateConversation(String listingId, String buyerId, DataCallback<Conversation> cb) {
        String currentUid = currentUserId();
        if (currentUid.isEmpty()) {
            cb.onError("Chưa đăng nhập.");
            return;
        }
        if (!currentUid.equals(buyerId)) {
            cb.onError("Người mua không khớp với tài khoản hiện tại.");
            return;
        }

        String convId = buildConversationId(listingId, buyerId);
        DocumentReference docRef = db.collection(COLLECTION_CONVERSATIONS).document(convId);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (!task.isSuccessful()) {
                    fail(cb, task.getException());
                    return;
                }
                DocumentSnapshot doc = task.getResult();
                if (doc != null && doc.exists()) {
                    Conversation conv = doc.toObject(Conversation.class);
                    if (conv != null) {
                        conv.setId(doc.getId());
                    }
                    cb.onSuccess(conv);
                    return;
                }
                createConversation(listingId, buyerId, convId, docRef, cb);
            }
        });
    }

    private void createConversation(String listingId, String buyerId, String convId,
                                    DocumentReference docRef, DataCallback<Conversation> cb) {
        db.collection(COLLECTION_LISTINGS).document(listingId).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        DocumentSnapshot doc = task.getResult();
                        if (!task.isSuccessful() || doc == null || !doc.exists()) {
                            cb.onError("Không tìm thấy tin đăng.");
                            return;
                        }
                        String sellerId = doc.getString("sellerId");
                        String statusStr = doc.getString("status");
                        ListingStatus status = null;
                        if (statusStr != null) {
                            try {
                                status = ListingStatus.valueOf(statusStr);
                            } catch (IllegalArgumentException ignored) {
                            }
                        }
                        String validationError = validateNewConversation(buyerId, sellerId, status);
                        if (validationError != null) {
                            cb.onError(validationError);
                            return;
                        }
                        Conversation conv = new Conversation();
                        conv.setId(convId);
                        conv.setListingId(listingId);
                        conv.setBuyerId(buyerId);
                        conv.setSellerId(sellerId);
                        conv.setLastMessage("");

                        docRef.set(conv).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> setTask) {
                                if (setTask.isSuccessful()) {
                                    cb.onSuccess(conv);
                                } else {
                                    fail(cb, setTask.getException());
                                }
                            }
                        });
                    }
                });
    }

    public void getConversation(String id, DataCallback<Conversation> cb) {
        if (id == null || id.isEmpty()) {
            cb.onError("Mã cuộc trò chuyện không hợp lệ.");
            return;
        }
        db.collection(COLLECTION_CONVERSATIONS).document(id).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (!task.isSuccessful()) {
                            fail(cb, task.getException());
                            return;
                        }
                        DocumentSnapshot doc = task.getResult();
                        if (doc == null || !doc.exists()) {
                            cb.onError("Không tìm thấy cuộc trò chuyện.");
                            return;
                        }
                        Conversation conv = doc.toObject(Conversation.class);
                        if (conv != null) {
                            conv.setId(doc.getId());
                        }
                        cb.onSuccess(conv);
                    }
                });
    }

    public void getConversations(String uid, DataCallback<List<Conversation>> cb) {
        final AtomicBoolean liveArrived = new AtomicBoolean(false);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                ChatDao dao = getChatDao();
                if (dao != null) {
                    List<ConversationEntity> cached = dao.getConversations();
                    if (cached != null && !cached.isEmpty()) {
                        final List<Conversation> cachedList = new ArrayList<Conversation>();
                        for (int i = 0; i < cached.size(); i++) {
                            Conversation conv = cached.get(i).toModel();
                            if (conv != null && conv.isParticipant(uid)) {
                                cachedList.add(conv);
                            }
                        }
                        if (!cachedList.isEmpty()) {
                            postToMain(new Runnable() {
                                @Override
                                public void run() {
                                    // Once live data is on screen, a late Room copy would replace it with older data.
                                    if (!liveArrived.get()) {
                                        cb.onSuccess(cachedList);
                                    }
                                }
                            });
                        }
                    }
                }
            }
        });

        db.collection(COLLECTION_CONVERSATIONS).whereEqualTo("buyerId", uid).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> buyerTask) {
                        if (!buyerTask.isSuccessful()) {
                            fail(cb, buyerTask.getException());
                            return;
                        }
                        db.collection(COLLECTION_CONVERSATIONS).whereEqualTo("sellerId", uid).get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> sellerTask) {
                                        if (!sellerTask.isSuccessful()) {
                                            fail(cb, sellerTask.getException());
                                            return;
                                        }
                                        Map<String, Conversation> map = new HashMap<String, Conversation>();
                                        addConversationsToMap(buyerTask.getResult(), map);
                                        addConversationsToMap(sellerTask.getResult(), map);

                                        List<Conversation> list = new ArrayList<Conversation>(map.values());
                                        sortConversationsByDate(list);
                                        // An empty answer may be Firestore's offline cache only,
                                        // so the Room copy may still fill in.
                                        if (!list.isEmpty()) {
                                            liveArrived.set(true);
                                        }
                                        cb.onSuccess(list);

                                        executor.execute(new Runnable() {
                                            @Override
                                            public void run() {
                                                ChatDao dao = getChatDao();
                                                if (dao != null && !list.isEmpty()) {
                                                    List<ConversationEntity> entities =
                                                            new ArrayList<ConversationEntity>();
                                                    for (int i = 0; i < list.size(); i++) {
                                                        entities.add(new ConversationEntity().fromModel(list.get(i)));
                                                    }
                                                    dao.upsertConversations(entities);
                                                }
                                            }
                                        });
                                    }
                                });
                    }
                });
    }

    // Live version of getConversations for D01: a new message moves its conversation to the top at once.
    // Like getConversations it answers from Room first, so the list opens offline.
    public ListenerRegistration observeConversations(final String uid, final DataCallback<List<Conversation>> cb) {
        final AtomicBoolean liveArrived = new AtomicBoolean(false);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                final List<Conversation> cachedList = cachedConversations(uid);
                if (!cachedList.isEmpty()) {
                    postToMain(new Runnable() {
                        @Override
                        public void run() {
                            // Once live data is on screen, a late Room copy would replace it with older data.
                            if (!liveArrived.get()) {
                                cb.onSuccess(cachedList);
                            }
                        }
                    });
                }
            }
        });
        Query asBuyer = db.collection(COLLECTION_CONVERSATIONS).whereEqualTo("buyerId", uid);
        Query asSeller = db.collection(COLLECTION_CONVERSATIONS).whereEqualTo("sellerId", uid);
        return listenEitherQuery(asBuyer, asSeller, Conversation.class, new DataCallback<List<Conversation>>() {
            @Override
            public void onSuccess(final List<Conversation> list) {
                sortConversationsByDate(list);
                // An empty answer may be Firestore's offline cache only,
                // so the Room copy may still fill in.
                if (!list.isEmpty()) {
                    liveArrived.set(true);
                }
                cb.onSuccess(list);
                if (list.isEmpty()) {
                    return;
                }
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        ChatDao dao = getChatDao();
                        if (dao == null) {
                            return;
                        }
                        List<ConversationEntity> entities = new ArrayList<ConversationEntity>();
                        for (int i = 0; i < list.size(); i++) {
                            entities.add(new ConversationEntity().fromModel(list.get(i)));
                        }
                        dao.upsertConversations(entities);
                    }
                });
            }

            @Override
            public void onError(String message) {
                cb.onError(message);
            }
        });
    }

    private List<Conversation> cachedConversations(String uid) {
        List<Conversation> cachedList = new ArrayList<Conversation>();
        ChatDao dao = getChatDao();
        if (dao == null) {
            return cachedList;
        }
        List<ConversationEntity> cached = dao.getConversations();
        if (cached == null) {
            return cachedList;
        }
        for (int i = 0; i < cached.size(); i++) {
            Conversation conv = cached.get(i).toModel();
            if (conv != null && conv.isParticipant(uid)) {
                cachedList.add(conv);
            }
        }
        return cachedList;
    }

    private void addConversationsToMap(QuerySnapshot snapshot, Map<String, Conversation> map) {
        if (snapshot == null) {
            return;
        }
        for (QueryDocumentSnapshot doc : snapshot) {
            Conversation conv = doc.toObject(Conversation.class);
            conv.setId(doc.getId());
            map.put(conv.getId(), conv);
        }
    }

    private void sortConversationsByDate(List<Conversation> list) {
        Collections.sort(list, new Comparator<Conversation>() {
            @Override
            public int compare(Conversation a, Conversation b) {
                Timestamp tA = a.getLastMessageAt();
                Timestamp tB = b.getLastMessageAt();
                if (tA == null && tB == null) {
                    return 0;
                }
                if (tA == null) {
                    return 1;
                }
                if (tB == null) {
                    return -1;
                }
                return tB.compareTo(tA);
            }
        });
    }

    public ListenerRegistration observeMessages(String id, DataCallback<List<ChatMessage>> cb) {
        final AtomicBoolean liveArrived = new AtomicBoolean(false);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                ChatDao dao = getChatDao();
                if (dao != null) {
                    List<MessageEntity> cached = dao.getMessages(id);
                    if (cached != null && !cached.isEmpty()) {
                        final List<ChatMessage> cachedList = new ArrayList<ChatMessage>();
                        for (int i = 0; i < cached.size(); i++) {
                            cachedList.add(cached.get(i).toModel());
                        }
                        postToMain(new Runnable() {
                            @Override
                            public void run() {
                                // Once live data is on screen, a late Room copy would replace it with older data.
                                if (!liveArrived.get()) {
                                    cb.onSuccess(cachedList);
                                }
                            }
                        });
                    }
                }
            }
        });

        Query query = db.collection(COLLECTION_CONVERSATIONS).document(id)
                .collection(COLLECTION_MESSAGES)
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .limitToLast(50);
        return query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshots,
                                @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    fail(cb, error);
                    return;
                }
                List<ChatMessage> list = new ArrayList<ChatMessage>();
                if (snapshots != null) {
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        ChatMessage msg = doc.toObject(ChatMessage.class);
                        if (msg != null) {
                            msg.setId(doc.getId());
                            list.add(msg);
                        }
                    }
                }
                // An empty answer may be Firestore's offline cache only,
                // so the Room copy may still fill in.
                if (!list.isEmpty()) {
                    liveArrived.set(true);
                }
                cb.onSuccess(list);

                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        ChatDao dao = getChatDao();
                        if (dao != null && !list.isEmpty()) {
                            List<MessageEntity> entities = new ArrayList<MessageEntity>();
                            for (int i = 0; i < list.size(); i++) {
                                entities.add(new MessageEntity().fromModel(list.get(i)));
                            }
                            dao.upsertMessages(entities);
                        }
                    }
                });
            }
        });
    }

    public void loadOlder(String id, Timestamp before, DataCallback<List<ChatMessage>> cb) {
        Query query = db.collection(COLLECTION_CONVERSATIONS).document(id)
                .collection(COLLECTION_MESSAGES)
                .whereLessThan("createdAt", before)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(50);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (!task.isSuccessful()) {
                    fail(cb, task.getException());
                    return;
                }
                List<ChatMessage> list = new ArrayList<ChatMessage>();
                QuerySnapshot snapshot = task.getResult();
                if (snapshot != null) {
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        ChatMessage msg = doc.toObject(ChatMessage.class);
                        if (msg != null) {
                            msg.setId(doc.getId());
                            list.add(msg);
                        }
                    }
                }
                Collections.reverse(list);
                cb.onSuccess(list);
            }
        });
    }

    public void getMessagesOnce(String id, DataCallback<List<ChatMessage>> cb) {
        Query query = db.collection(COLLECTION_CONVERSATIONS).document(id)
                .collection(COLLECTION_MESSAGES)
                .orderBy("createdAt", Query.Direction.ASCENDING);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (!task.isSuccessful()) {
                    fail(cb, task.getException());
                    return;
                }
                List<ChatMessage> list = new ArrayList<ChatMessage>();
                QuerySnapshot snapshot = task.getResult();
                if (snapshot != null) {
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        ChatMessage msg = doc.toObject(ChatMessage.class);
                        if (msg != null) {
                            msg.setId(doc.getId());
                            list.add(msg);
                        }
                    }
                }
                cb.onSuccess(list);
            }
        });
    }

    public void sendText(String id, String text, DataCallback<Void> cb) {
        String validationError = validateMessageText(text);
        if (validationError != null) {
            cb.onError(validationError);
            return;
        }

        String senderId = currentUserId();
        if (senderId.isEmpty()) {
            cb.onError("Chưa đăng nhập.");
            return;
        }

        DocumentReference convRef = db.collection(COLLECTION_CONVERSATIONS).document(id);
        convRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (!task.isSuccessful() || task.getResult() == null || !task.getResult().exists()) {
                    cb.onError("Cuộc trò chuyện không tồn tại.");
                    return;
                }
                Conversation conv = task.getResult().toObject(Conversation.class);
                if (!isSenderAllowed(conv, senderId)) {
                    cb.onError("Bạn không thuộc cuộc trò chuyện này.");
                    return;
                }

                ChatMessage msg = new ChatMessage();
                msg.setConversationId(id);
                msg.setSenderId(senderId);
                msg.setType(MessageType.TEXT);
                msg.setText(text != null ? text.trim() : "");
                msg.setImageId("");
                msg.setOfferId("");

                WriteBatch batch = db.batch();
                DocumentReference msgRef = db.collection(COLLECTION_CONVERSATIONS).document(id)
                        .collection(COLLECTION_MESSAGES).document();
                msg.setId(msgRef.getId());
                batch.set(msgRef, msg);

                Map<String, Object> convUpdates = new HashMap<String, Object>();
                convUpdates.put("lastMessage", msg.getText());
                convUpdates.put("lastMessageAt", FieldValue.serverTimestamp());
                convUpdates.put("updatedAt", FieldValue.serverTimestamp());
                batch.update(convRef, convUpdates);

                String recipientId = conv != null ? conv.otherParty(senderId) : null;
                if (recipientId != null) {
                    sendNotification(recipientId, NotificationType.NEW_MESSAGE,
                            "Tin nhắn mới", msg.getText(), id, batch);
                }
                commitBatch(batch, cb);
            }
        });
    }

    public void sendImage(String id, Uri uri, DataCallback<Void> cb) {
        String senderId = currentUserId();
        if (senderId.isEmpty()) {
            cb.onError("Chưa đăng nhập.");
            return;
        }

        DocumentReference convRef = db.collection(COLLECTION_CONVERSATIONS).document(id);
        convRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (!task.isSuccessful() || task.getResult() == null || !task.getResult().exists()) {
                    cb.onError("Cuộc trò chuyện không tồn tại.");
                    return;
                }
                Conversation conv = task.getResult().toObject(Conversation.class);
                if (!isSenderAllowed(conv, senderId)) {
                    cb.onError("Bạn không thuộc cuộc trò chuyện này.");
                    return;
                }

                MediaRepository mediaRepo = new MediaRepository();
                mediaRepo.saveImage(uri, ImagePurpose.CHAT, new DataCallback<String>() {
                    @Override
                    public void onSuccess(String imageId) {
                        sendImageMessage(id, imageId, senderId, conv, cb);
                    }

                    @Override
                    public void onError(String message) {
                        cb.onError(message);
                    }
                });
            }
        });
    }

    private void sendImageMessage(String id, String imageId, String senderId, Conversation conv,
                                  DataCallback<Void> cb) {
        ChatMessage msg = new ChatMessage();
        msg.setConversationId(id);
        msg.setSenderId(senderId);
        msg.setType(MessageType.IMAGE);
        msg.setImageId(imageId != null ? imageId : "");
        msg.setOfferId("");
        msg.setText("[Hình ảnh]");

        WriteBatch batch = db.batch();
        DocumentReference msgRef = db.collection(COLLECTION_CONVERSATIONS).document(id)
                .collection(COLLECTION_MESSAGES).document();
        msg.setId(msgRef.getId());
        batch.set(msgRef, msg);

        DocumentReference convRef = db.collection(COLLECTION_CONVERSATIONS).document(id);
        Map<String, Object> convUpdates = new HashMap<String, Object>();
        convUpdates.put("lastMessage", "[Hình ảnh]");
        convUpdates.put("lastMessageAt", FieldValue.serverTimestamp());
        convUpdates.put("updatedAt", FieldValue.serverTimestamp());
        batch.update(convRef, convUpdates);

        String recipientId = conv != null ? conv.otherParty(senderId) : null;
        if (recipientId != null) {
            sendNotification(recipientId, NotificationType.NEW_MESSAGE,
                    "Tin nhắn hình ảnh mới", "[Hình ảnh]", id, batch);
        }

        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> commitTask) {
                if (commitTask.isSuccessful()) {
                    cb.onSuccess(null);
                } else {
                    new MediaRepository().delete(imageId, new DataCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                        }

                        @Override
                        public void onError(String msg) {
                        }
                    });
                    fail(cb, commitTask.getException());
                }
            }
        });
    }

    public void sendOffer(final Offer offer, final DataCallback<String> cb) {
        final String currentUid = currentUserId();
        if (currentUid.isEmpty()) {
            cb.onError("Chưa đăng nhập.");
            return;
        }
        if (offer == null || offer.getListingId() == null || offer.getListingId().trim().isEmpty()) {
            cb.onError("Thông tin đề nghị không hợp lệ.");
            return;
        }

        offer.setBuyerId(currentUid);

        db.collection(COLLECTION_LISTINGS).document(offer.getListingId()).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (!task.isSuccessful() || task.getResult() == null || !task.getResult().exists()) {
                            cb.onError("Không tìm thấy tin đăng.");
                            return;
                        }
                        Listing listing = task.getResult().toObject(Listing.class);
                        if (listing == null) {
                            cb.onError("Dữ liệu tin đăng không hợp lệ.");
                            return;
                        }
                        listing.setId(task.getResult().getId());
                        offer.setSellerId(listing.getSellerId());

                        String validationError = validateOffer(offer, listing, currentUid);
                        if (validationError != null) {
                            cb.onError(validationError);
                            return;
                        }

                        checkPendingOfferAndProceed(offer, listing, currentUid, cb);
                    }
                });
    }

    private void checkPendingOfferAndProceed(final Offer offer, final Listing listing,
                                             final String senderId, final DataCallback<String> cb) {
        db.collection(COLLECTION_OFFERS)
                .whereEqualTo("buyerId", senderId)
                .whereEqualTo("listingId", offer.getListingId())
                .whereEqualTo("status", OfferStatus.PENDING.name())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (!task.isSuccessful()) {
                            fail(cb, task.getException());
                            return;
                        }
                        if (task.getResult() != null && !task.getResult().isEmpty()) {
                            cb.onError("Bạn đã có đề nghị đang chờ cho tin đăng này.");
                            return;
                        }
                        ensureConversationAndSaveOffer(offer, listing, senderId, cb);
                    }
                });
    }

    private void ensureConversationAndSaveOffer(final Offer offer, final Listing listing,
                                                final String senderId, final DataCallback<String> cb) {
        String convId = offer.getConversationId();
        if (convId == null || convId.trim().isEmpty()) {
            convId = buildConversationId(listing.getId(), senderId);
            offer.setConversationId(convId);
        }

        final String finalConvId = convId;
        final DocumentReference convRef = db.collection(COLLECTION_CONVERSATIONS).document(finalConvId);

        convRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                boolean convExists = task.isSuccessful() && task.getResult() != null && task.getResult().exists();
                executeSendOfferBatch(offer, listing, senderId, finalConvId, convRef, convExists, cb);
            }
        });
    }

    private void executeSendOfferBatch(final Offer offer, final Listing listing, final String senderId,
                                       final String convId, final DocumentReference convRef,
                                       final boolean convExists, final DataCallback<String> cb) {
        offer.setStatus(OfferStatus.PENDING);
        offer.setCreatedAt(null);
        offer.setUpdatedAt(null);

        WriteBatch batch = db.batch();
        DocumentReference offerRef = db.collection(COLLECTION_OFFERS).document();
        final String offerId = offerRef.getId();
        offer.setId(offerId);
        batch.set(offerRef, offer);

        String summary = formatOfferSummary(offer);
        ChatMessage msg = new ChatMessage();
        DocumentReference msgRef = db.collection(COLLECTION_CONVERSATIONS).document(convId)
                .collection(COLLECTION_MESSAGES).document();
        msg.setId(msgRef.getId());
        msg.setConversationId(convId);
        msg.setSenderId(senderId);
        msg.setType(MessageType.OFFER);
        msg.setOfferId(offerId);
        msg.setImageId("");
        msg.setText(summary);
        msg.setCreatedAt(null);
        batch.set(msgRef, msg);

        if (!convExists) {
            Conversation conv = new Conversation();
            conv.setId(convId);
            conv.setListingId(listing.getId());
            conv.setBuyerId(senderId);
            conv.setSellerId(listing.getSellerId());
            conv.setLastMessage(summary);
            conv.setLastMessageAt(null);
            conv.setCreatedAt(null);
            conv.setUpdatedAt(null);
            batch.set(convRef, conv);
        } else {
            Map<String, Object> convUpdates = new HashMap<String, Object>();
            convUpdates.put("lastMessage", summary);
            convUpdates.put("lastMessageAt", FieldValue.serverTimestamp());
            convUpdates.put("updatedAt", FieldValue.serverTimestamp());
            batch.update(convRef, convUpdates);
        }

        sendNotification(listing.getSellerId(), NotificationType.NEW_OFFER,
                "Đề nghị giá mới", "Có người gửi đề nghị cho tin: " + listing.getTitle(),
                convId, batch);

        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> commitTask) {
                if (commitTask.isSuccessful()) {
                    cb.onSuccess(offerId);
                } else {
                    fail(cb, commitTask.getException());
                }
            }
        });
    }

    public void respondToOffer(final String offerId, final boolean accept, final DataCallback<String> cb) {
        respondToOffer(offerId, accept, null, cb);
    }

    // Accepting needs the seller's terms, so the deal is born with them (STT 50, 51).
    public void respondToOffer(final String offerId, final boolean accept, final Deal terms,
                               final DataCallback<String> cb) {
        final String currentUid = currentUserId();
        if (currentUid.isEmpty()) {
            cb.onError("Chưa đăng nhập.");
            return;
        }
        if (accept) {
            String termsError = checkAcceptTerms(terms);
            if (termsError != null) {
                cb.onError(termsError);
                return;
            }
        }

        final DocumentReference offerRef = db.collection(COLLECTION_OFFERS).document(offerId);
        offerRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (!task.isSuccessful() || task.getResult() == null || !task.getResult().exists()) {
                    cb.onError("Không tìm thấy đề nghị.");
                    return;
                }
                Offer offer = task.getResult().toObject(Offer.class);
                if (offer == null) {
                    cb.onError("Dữ liệu đề nghị không hợp lệ.");
                    return;
                }
                offer.setId(offerId);

                if (!currentUid.equals(offer.getSellerId())) {
                    cb.onError("Chỉ người bán mới có quyền trả lời đề nghị.");
                    return;
                }
                if (offer.getStatus() != OfferStatus.PENDING) {
                    cb.onError("Đề nghị này không còn ở trạng thái chờ.");
                    return;
                }

                if (!accept) {
                    rejectOffer(offerRef, offer, cb);
                } else {
                    acceptOffer(offerRef, offer, terms, cb);
                }
            }
        });
    }

    // Null when the terms are complete; otherwise the message for the seller.
    static String checkAcceptTerms(Deal terms) {
        if (terms == null || !terms.hasRequiredTerms()) {
            return "Cần chọn cách giao nhận, nơi hẹn và giờ hẹn trước khi chấp nhận.";
        }
        if (Deal.isScheduleInPast(terms.getScheduledAt(), Timestamp.now())) {
            return "Thời gian hẹn không được ở trong quá khứ.";
        }
        return null;
    }

    private void rejectOffer(DocumentReference offerRef, Offer offer, final DataCallback<String> cb) {
        offer.reject();

        WriteBatch batch = db.batch();
        Map<String, Object> offerUpdates = new HashMap<String, Object>();
        offerUpdates.put("status", offer.getStatus().name());
        offerUpdates.put("updatedAt", FieldValue.serverTimestamp());
        batch.update(offerRef, offerUpdates);

        sendNotification(offer.getBuyerId(), NotificationType.OFFER_RESULT,
                "Đề nghị bị từ chối", "Người bán đã từ chối đề nghị của bạn.",
                offer.getConversationId(), batch);

        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> commitTask) {
                if (commitTask.isSuccessful()) {
                    cb.onSuccess(null);
                } else {
                    fail(cb, commitTask.getException());
                }
            }
        });
    }

    private void acceptOffer(final DocumentReference offerRef, final Offer offer, final Deal terms,
                             final DataCallback<String> cb) {
        final DocumentReference listingRef = db.collection(COLLECTION_LISTINGS).document(offer.getListingId());
        listingRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (!task.isSuccessful() || task.getResult() == null || !task.getResult().exists()) {
                    cb.onError("Không tìm thấy tin đăng.");
                    return;
                }
                Listing listing = task.getResult().toObject(Listing.class);
                if (listing == null || listing.getStatus() != ListingStatus.AVAILABLE) {
                    offer.reject();
                    WriteBatch rejectBatch = db.batch();
                    Map<String, Object> offerUpdates = new HashMap<String, Object>();
                    offerUpdates.put("status", offer.getStatus().name());
                    offerUpdates.put("updatedAt", FieldValue.serverTimestamp());
                    rejectBatch.update(offerRef, offerUpdates);
                    sendNotification(offer.getBuyerId(), NotificationType.OFFER_RESULT,
                            "Đề nghị bị từ chối",
                            "Tin không còn ở trạng thái khả dụng để chấp nhận.",
                            offer.getConversationId(), rejectBatch);
                    rejectBatch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> t) {
                            cb.onError("Tin không còn khả dụng để chấp nhận đề nghị.");
                        }
                    });
                    return;
                }
                listing.setId(task.getResult().getId());
                executeAcceptOfferBatch(offerRef, offer, listingRef, listing, terms, cb);
            }
        });
    }

    private void executeAcceptOfferBatch(final DocumentReference offerRef, final Offer offer,
                                         final DocumentReference listingRef, final Listing listing,
                                         final Deal terms, final DataCallback<String> cb) {
        String currentUid = currentUserId();
        db.collection(COLLECTION_OFFERS)
                .whereEqualTo("sellerId", currentUid)
                .whereEqualTo("listingId", offer.getListingId())
                .whereEqualTo("status", OfferStatus.PENDING.name())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> queryTask) {
                        if (!queryTask.isSuccessful()) {
                            fail(cb, queryTask.getException());
                            return;
                        }
                        WriteBatch batch = db.batch();

                        offer.accept();
                        Map<String, Object> offerUpdates = new HashMap<String, Object>();
                        offerUpdates.put("status", offer.getStatus().name());
                        offerUpdates.put("updatedAt", FieldValue.serverTimestamp());
                        batch.update(offerRef, offerUpdates);

                        Deal deal = DealRepository.buildFromOffer(offer, listing, terms);
                        DocumentReference dealRef = db.collection(COLLECTION_DEALS).document();
                        final String dealId = dealRef.getId();
                        deal.setId(dealId);
                        batch.set(dealRef, deal);

                        listing.reserve(dealId);
                        Map<String, Object> listingUpdates = new HashMap<String, Object>();
                        listingUpdates.put("status", listing.getStatus().name());
                        listingUpdates.put("reservedDealId", dealId);
                        listingUpdates.put("updatedAt", FieldValue.serverTimestamp());
                        batch.update(listingRef, listingUpdates);

                        if (queryTask.getResult() != null) {
                            for (DocumentSnapshot doc : queryTask.getResult().getDocuments()) {
                                if (!doc.getId().equals(offer.getId())) {
                                    Map<String, Object> rejUpdates = new HashMap<String, Object>();
                                    rejUpdates.put("status", OfferStatus.REJECTED.name());
                                    rejUpdates.put("updatedAt", FieldValue.serverTimestamp());
                                    batch.update(doc.getReference(), rejUpdates);

                                    String otherBuyerId = doc.getString("buyerId");
                                    String otherConvId = doc.getString("conversationId");
                                    if (otherBuyerId != null) {
                                        sendNotification(otherBuyerId, NotificationType.OFFER_RESULT,
                                                "Đề nghị bị từ chối",
                                                "Tin đăng đã được giao dịch với người khác.",
                                                otherConvId, batch);
                                    }
                                }
                            }
                        }

                        String acceptBody = "Người bán đã chấp nhận đề nghị của bạn. "
                                + "Nhấn để xem giao dịch.";
                        sendNotification(offer.getBuyerId(), NotificationType.OFFER_RESULT,
                                "Đề nghị đã được chấp nhận", acceptBody, dealId, batch);

                        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> commitTask) {
                                if (commitTask.isSuccessful()) {
                                    cb.onSuccess(dealId);
                                } else {
                                    fail(cb, commitTask.getException());
                                }
                            }
                        });
                    }
                });
    }

    public ListenerRegistration observeOffers(String conversationId, final DataCallback<List<Offer>> cb) {
        String currentUid = currentUserId();
        Query query = db.collection(COLLECTION_OFFERS)
                .where(Filter.and(
                        Filter.equalTo("conversationId", conversationId),
                        Filter.or(Filter.equalTo("buyerId", currentUid), Filter.equalTo("sellerId", currentUid))
                ));
        return query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value,
                                @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    fail(cb, error);
                    return;
                }
                List<Offer> list = new ArrayList<Offer>();
                if (value != null) {
                    for (DocumentSnapshot doc : value.getDocuments()) {
                        Offer offer = doc.toObject(Offer.class);
                        if (offer != null) {
                            offer.setId(doc.getId());
                            list.add(offer);
                        }
                    }
                }
                cb.onSuccess(list);
            }
        });
    }

    public ListenerRegistration observeOffers(String conversationId, final DataCallback<Void> cb, boolean dummy) {
        return observeOffers(conversationId, new DataCallback<List<Offer>>() {
            @Override
            public void onSuccess(List<Offer> result) {
                cb.onSuccess(null);
            }

            @Override
            public void onError(String message) {
                cb.onError(message);
            }
        });
    }

    static String validateOffer(Offer offer, Listing listing, String senderId) {
        if (offer == null) {
            return "Thông tin đề nghị không hợp lệ.";
        }
        if (listing == null) {
            return "Không tìm thấy tin đăng.";
        }
        if (listing.getStatus() != ListingStatus.AVAILABLE) {
            return "Tin đăng không còn ở trạng thái khả dụng để gửi đề nghị.";
        }
        if (senderId != null && senderId.equals(listing.getSellerId())) {
            return "Không thể gửi đề nghị cho tin đăng của chính mình.";
        }
        List<OfferType> accepted = listing.getAcceptedOfferTypes();
        if (accepted == null || !accepted.contains(offer.getOfferType())) {
            return "Người bán không chấp nhận hình thức đề nghị này.";
        }
        if (offer.getOfferType() == OfferType.MONEY) {
            if (offer.getAmount() < listing.getMinOfferPrice()) {
                return "Giá đề nghị chưa phù hợp, hãy thử mức khác.";
            }
        } else if (offer.getOfferType() == OfferType.ITEM) {
            if (offer.getAmount() != 0) {
                return "Đề nghị chỉ có vật không được có số tiền.";
            }
            if (offer.getExchangeItem() == null || offer.getExchangeItem().trim().isEmpty()) {
                return "Vui lòng nhập thông tin món đồ trao đổi.";
            }
        } else if (offer.getOfferType() == OfferType.MONEY_AND_ITEM) {
            if (offer.getAmount() < listing.getMinOfferPrice()) {
                return "Giá đề nghị chưa phù hợp, hãy thử mức khác.";
            }
            if (offer.getExchangeItem() == null || offer.getExchangeItem().trim().isEmpty()) {
                return "Vui lòng nhập thông tin món đồ trao đổi.";
            }
        }
        return null;
    }

    static String formatOfferSummary(Offer offer) {
        com.nhom3.pubgapp.util.FormatUtils formatUtils = new com.nhom3.pubgapp.util.FormatUtils();
        if (offer == null) {
            return "Đề nghị giá mới";
        }
        if (offer.getOfferType() == OfferType.MONEY) {
            return "Đề nghị: " + formatUtils.formatPrice(offer.getAmount());
        }
        if (offer.getOfferType() == OfferType.ITEM) {
            return "Đề nghị đổi: " + (offer.getExchangeItem() != null ? offer.getExchangeItem().trim() : "");
        }
        if (offer.getOfferType() == OfferType.MONEY_AND_ITEM) {
            return "Đề nghị: " + formatUtils.formatPrice(offer.getAmount()) + " + "
                    + (offer.getExchangeItem() != null ? offer.getExchangeItem().trim() : "");
        }
        return "Đề nghị giá mới";
    }

    static List<Offer> filterOtherPendingOffers(List<Offer> offers, String currentOfferId) {
        List<Offer> result = new ArrayList<Offer>();
        if (offers == null) {
            return result;
        }
        for (int i = 0; i < offers.size(); i++) {
            Offer o = offers.get(i);
            if (o != null && o.getId() != null && !o.getId().equals(currentOfferId)) {
                result.add(o);
            }
        }
        return result;
    }

    private void sendNotification(String recipientId, NotificationType type,
                                  String title, String body, String targetId,
                                  WriteBatch batch) {
        if (recipientId == null || recipientId.trim().isEmpty()) {
            return;
        }
        AppNotification notif = new AppNotification();
        notif.setRecipientId(recipientId);
        notif.setType(type);
        notif.setTitle(title);
        notif.setBody(body);
        notif.setTargetId(targetId);
        notif.setRead(false);
        notificationRepository.create(notif, batch, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
            }

            @Override
            public void onError(String message) {
            }
        });
    }

    private void commitBatch(WriteBatch batch, DataCallback<Void> cb) {
        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    cb.onSuccess(null);
                } else {
                    fail(cb, task.getException());
                }
            }
        });
    }
}
