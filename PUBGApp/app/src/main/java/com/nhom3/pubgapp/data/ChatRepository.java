package com.nhom3.pubgapp.data;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.nhom3.pubgapp.data.local.ChatDao;
import com.nhom3.pubgapp.data.local.ConversationEntity;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatRepository extends BaseRepository {

    private static final String COLLECTION_CONVERSATIONS = "conversations";
    private static final String COLLECTION_MESSAGES = "messages";
    private static final String COLLECTION_OFFERS = "offers";
    private static final String COLLECTION_LISTINGS = "listings";
    private static final String COLLECTION_DEALS = "deals";
    private static final String COLLECTION_NOTIFICATIONS = "notifications";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private ChatDao getChatDao() {
        try {
            android.content.Context context = com.google.firebase.FirebaseApp.getInstance().getApplicationContext();
            if (context != null) {
                return com.nhom3.pubgapp.data.local.AppDatabase.getInstance(context).chatDao();
            }
        } catch (Exception e) {
            // Room is unavailable in unit tests or before app initialization.
        }
        return null;
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

    static boolean isValidMessageText(String text) {
        if (text == null) {
            return false;
        }
        String trimmed = text.trim();
        return !trimmed.isEmpty() && text.length() <= 2000;
    }

    static String validateMessageText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "Nội dung tin nhắn không được để trống.";
        }
        if (text.length() > 2000) {
            return "Nội dung tin nhắn không được vượt quá 2000 ký tự.";
        }
        return null;
    }

    static boolean isSenderAllowed(Conversation conv, String senderId) {
        if (conv == null || senderId == null || senderId.trim().isEmpty()) {
            return false;
        }
        return conv.isParticipant(senderId);
    }

    public void getOrCreateConversation(String listingId, String buyerId, DataCallback<Conversation> cb) {
        try {
            if (!com.nhom3.pubgapp.util.NetworkMonitor.getInstance().isOnline()) {
                cb.onError("Thiết bị đang ngoại tuyến. Vui lòng kiểm tra kết nối mạng.");
                return;
            }
        } catch (UnsupportedOperationException ignored) {
            // NetworkMonitor not ready yet in early phase.
        }

        if (buyerId == null || buyerId.trim().isEmpty()) {
            cb.onError("Vui lòng đăng nhập để nhắn tin.");
            return;
        }

        String convId = buildConversationId(listingId, buyerId);
        DocumentReference docRef = db.collection(COLLECTION_CONVERSATIONS).document(convId);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (!task.isSuccessful()) {
                    cb.onError(getErrorMessage(task.getException(), "Lỗi tải cuộc trò chuyện."));
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
                // Tạo mới nếu chưa có
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
                        if (!task.isSuccessful() || task.getResult() == null || !task.getResult().exists()) {
                            cb.onError("Không tìm thấy tin đăng.");
                            return;
                        }
                        DocumentSnapshot listingDoc = task.getResult();
                        String sellerId = listingDoc.getString("sellerId");
                        String statusStr = listingDoc.getString("status");
                        ListingStatus status = null;
                        if (statusStr != null) {
                            try {
                                status = ListingStatus.valueOf(statusStr);
                            } catch (IllegalArgumentException e) {
                                status = null;
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
                        conv.setCreatedAt(Timestamp.now());
                        docRef.set(conv).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> setTask) {
                                if (setTask.isSuccessful()) {
                                    cb.onSuccess(conv);
                                } else {
                                    cb.onError(getErrorMessage(setTask.getException(),
                                            "Lỗi tạo cuộc trò chuyện."));
                                }
                            }
                        });
                    }
                });
    }

    public void getConversations(String uid, DataCallback<List<Conversation>> cb) {
        // Đọc cache từ Room trước
        executor.execute(new Runnable() {
            @Override
            public void run() {
                ChatDao dao = getChatDao();
                if (dao != null) {
                    List<ConversationEntity> cached = dao.getConversations();
                    if (cached != null && !cached.isEmpty()) {
                        List<Conversation> list = new ArrayList<Conversation>();
                        for (int i = 0; i < cached.size(); i++) {
                            list.add(cached.get(i).toModel());
                        }
                        cb.onSuccess(list);
                    }
                }
            }
        });

        // Tải các cuộc trò chuyện người dùng tham gia với vai trò người mua
        db.collection(COLLECTION_CONVERSATIONS)
                .whereEqualTo("buyerId", uid)
                .orderBy("lastMessageAt", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> buyerTask) {
                        if (!buyerTask.isSuccessful()) {
                            cb.onError(getErrorMessage(buyerTask.getException(), "Lỗi tải cuộc trò chuyện."));
                            return;
                        }
                        // Tải các cuộc trò chuyện vai trò người bán
                        db.collection(COLLECTION_CONVERSATIONS)
                                .whereEqualTo("sellerId", uid)
                                .orderBy("lastMessageAt", Query.Direction.DESCENDING)
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> sellerTask) {
                                        if (!sellerTask.isSuccessful()) {
                                            cb.onError(getErrorMessage(sellerTask.getException(),
                                                    "Lỗi tải cuộc trò chuyện."));
                                            return;
                                        }
                                        Map<String, Conversation> map = new HashMap<>();
                                        addConversationsToMap(buyerTask.getResult(), map);
                                        addConversationsToMap(sellerTask.getResult(), map);

                                        List<Conversation> list = new ArrayList<>(map.values());
                                        sortConversationsByDate(list);

                                        // Lưu vào Room
                                        executor.execute(new Runnable() {
                                            @Override
                                            public void run() {
                                                ChatDao dao = getChatDao();
                                                if (dao != null && !list.isEmpty()) {
                                                    List<ConversationEntity> entities =
                                                            new ArrayList<ConversationEntity>();
                                                    for (int i = 0; i < list.size(); i++) {
                                                        ConversationEntity entity =
                                                                new ConversationEntity().fromModel(list.get(i));
                                                        entities.add(entity);
                                                    }
                                                    dao.upsertConversations(entities);
                                                }
                                            }
                                        });

                                        cb.onSuccess(list);
                                    }
                                });
                    }
                });
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
        Query query = db.collection(COLLECTION_CONVERSATIONS).document(id)
                .collection(COLLECTION_MESSAGES)
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .limitToLast(50);
        return query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshots,
                                @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    cb.onError(error.getMessage());
                    return;
                }
                List<ChatMessage> list = new ArrayList<>();
                if (snapshots != null) {
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        ChatMessage msg = doc.toObject(ChatMessage.class);
                        if (msg != null) {
                            msg.setId(doc.getId());
                            list.add(msg);
                        }
                    }
                }
                final List<ChatMessage> finalList = list;
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        ChatDao dao = getChatDao();
                        if (dao != null && !finalList.isEmpty()) {
                            List<com.nhom3.pubgapp.data.local.MessageEntity> entities =
                                    new ArrayList<com.nhom3.pubgapp.data.local.MessageEntity>();
                            for (int i = 0; i < finalList.size(); i++) {
                                entities.add(new com.nhom3.pubgapp.data.local.MessageEntity()
                                        .fromModel(finalList.get(i)));
                            }
                            dao.upsertMessages(entities);
                        }
                    }
                });
                cb.onSuccess(finalList);
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
                    cb.onError(getErrorMessage(task.getException(), "Lỗi tải tin nhắn cũ."));
                    return;
                }
                List<ChatMessage> list = new ArrayList<>();
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
                // Sắp xếp lại từ cũ đến mới để giao diện hiển thị nối tiếp
                Collections.reverse(list);
                final List<ChatMessage> finalList = list;
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        ChatDao dao = getChatDao();
                        if (dao != null && !finalList.isEmpty()) {
                            List<com.nhom3.pubgapp.data.local.MessageEntity> entities =
                                    new ArrayList<com.nhom3.pubgapp.data.local.MessageEntity>();
                            for (int i = 0; i < finalList.size(); i++) {
                                entities.add(new com.nhom3.pubgapp.data.local.MessageEntity()
                                        .fromModel(finalList.get(i)));
                            }
                            dao.upsertMessages(entities);
                        }
                    }
                });
                cb.onSuccess(finalList);
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
                    cb.onError(getErrorMessage(task.getException(), "Lỗi đọc tin nhắn."));
                    return;
                }
                List<ChatMessage> list = new ArrayList<>();
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
        try {
            if (!com.nhom3.pubgapp.util.NetworkMonitor.getInstance().isOnline()) {
                cb.onError("Thiết bị đang ngoại tuyến. Vui lòng kiểm tra kết nối mạng.");
                return;
            }
        } catch (UnsupportedOperationException ignored) {
        }

        String validationError = validateMessageText(text);
        if (validationError != null) {
            cb.onError(validationError);
            return;
        }

        String senderId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "";
        if (senderId.isEmpty()) {
            cb.onError("Vui lòng đăng nhập để gửi tin nhắn.");
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
                msg.setText(text);
                msg.setCreatedAt(Timestamp.now());

                WriteBatch batch = db.batch();
                DocumentReference msgRef = db.collection(COLLECTION_CONVERSATIONS).document(id)
                        .collection(COLLECTION_MESSAGES).document();
                msg.setId(msgRef.getId());
                batch.set(msgRef, msg);

                batch.update(convRef, "lastMessage", text, "lastMessageAt", msg.getCreatedAt());

                String recipientId = conv != null ? conv.otherParty(senderId) : null;
                if (recipientId != null) {
                    queueNotification(recipientId, NotificationType.NEW_MESSAGE,
                            "Tin nhắn mới", text, id, batch);
                }
                commitBatch(batch, cb, "Lỗi gửi tin nhắn.");
            }
        });
    }

    public void sendImage(String id, Uri uri, DataCallback<Void> cb) {
        try {
            if (!com.nhom3.pubgapp.util.NetworkMonitor.getInstance().isOnline()) {
                cb.onError("Thiết bị đang ngoại tuyến. Vui lòng kiểm tra kết nối mạng.");
                return;
            }
        } catch (UnsupportedOperationException ignored) {
        }

        String senderId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "";
        if (senderId.isEmpty()) {
            cb.onError("Vui lòng đăng nhập để gửi ảnh.");
            return;
        }

        MediaRepository mediaRepo = new MediaRepository();
        mediaRepo.saveImage(uri, ImagePurpose.CHAT, new DataCallback<String>() {
            @Override
            public void onSuccess(String imageId) {
                sendImageMessage(id, imageId, senderId, cb);
            }

            @Override
            public void onError(String message) {
                cb.onError(message);
            }
        });
    }

    private void sendImageMessage(String id, String imageId, String senderId, DataCallback<Void> cb) {
        DocumentReference convRef = db.collection(COLLECTION_CONVERSATIONS).document(id);
        convRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (!task.isSuccessful() || task.getResult() == null || !task.getResult().exists()) {
                    new MediaRepository().delete(imageId, new DataCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                        }
                        @Override
                        public void onError(String msg) {
                        }
                    });
                    cb.onError("Cuộc trò chuyện không tồn tại.");
                    return;
                }

                Conversation conv = task.getResult().toObject(Conversation.class);
                if (!isSenderAllowed(conv, senderId)) {
                    new MediaRepository().delete(imageId, new DataCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                        }
                        @Override
                        public void onError(String msg) {
                        }
                    });
                    cb.onError("Bạn không thuộc cuộc trò chuyện này.");
                    return;
                }

                ChatMessage msg = new ChatMessage();
                msg.setConversationId(id);
                msg.setSenderId(senderId);
                msg.setType(MessageType.IMAGE);
                msg.setImageId(imageId);
                msg.setText("[Hình ảnh]");
                msg.setCreatedAt(Timestamp.now());

                WriteBatch batch = db.batch();
                DocumentReference msgRef = db.collection(COLLECTION_CONVERSATIONS).document(id)
                        .collection(COLLECTION_MESSAGES).document();
                msg.setId(msgRef.getId());
                batch.set(msgRef, msg);

                batch.update(convRef, "lastMessage", "[Hình ảnh]", "lastMessageAt", msg.getCreatedAt());

                String recipientId = conv != null ? conv.otherParty(senderId) : null;
                if (recipientId != null) {
                    queueNotification(recipientId, NotificationType.NEW_MESSAGE,
                            "Tin nhắn hình ảnh mới", "[Hình ảnh]", id, batch);
                }
                commitBatchImage(batch, imageId, cb, "Lỗi gửi ảnh.");
            }
        });
    }

    private void commitBatchImage(WriteBatch batch, String imageId, DataCallback<Void> cb,
                                  String defaultErrorMessage) {
        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
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
                    cb.onError(getErrorMessage(task.getException(), defaultErrorMessage));
                }
            }
        });
    }

    public void sendOffer(Offer offer, DataCallback<String> cb) {
        try {
            if (!com.nhom3.pubgapp.util.NetworkMonitor.getInstance().isOnline()) {
                cb.onError("Thiết bị đang ngoại tuyến. Vui lòng kiểm tra kết nối mạng.");
                return;
            }
        } catch (UnsupportedOperationException ignored) {
        }

        String senderId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "";
        if (senderId.isEmpty()) {
            cb.onError("Vui lòng đăng nhập để gửi đề nghị.");
            return;
        }

        if (offer == null || offer.getListingId() == null || offer.getListingId().trim().isEmpty()) {
            cb.onError("Thông tin đề nghị không hợp lệ.");
            return;
        }

        offer.setBuyerId(senderId);

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

                        String validationError = validateOffer(offer, listing, senderId);
                        if (validationError != null) {
                            cb.onError(validationError);
                            return;
                        }

                        checkPendingOfferAndProceed(offer, listing, senderId, cb);
                    }
                });
    }

    private void checkPendingOfferAndProceed(Offer offer, Listing listing, String senderId,
                                             DataCallback<String> cb) {
        db.collection(COLLECTION_OFFERS)
                .whereEqualTo("listingId", offer.getListingId())
                .whereEqualTo("buyerId", senderId)
                .whereEqualTo("status", OfferStatus.PENDING.name())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                            cb.onError("Bạn đã có đề nghị đang chờ cho tin đăng này.");
                            return;
                        }
                        ensureConversationAndSaveOffer(offer, listing, senderId, cb);
                    }
                });
    }

    private void ensureConversationAndSaveOffer(Offer offer, Listing listing, String senderId,
                                                DataCallback<String> cb) {
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

    private void executeSendOfferBatch(Offer offer, Listing listing, String senderId,
                                       String convId, DocumentReference convRef,
                                       boolean convExists, DataCallback<String> cb) {
        offer.setStatus(OfferStatus.PENDING);
        offer.setCreatedAt(Timestamp.now());

        WriteBatch batch = db.batch();
        DocumentReference offerRef = db.collection(COLLECTION_OFFERS).document();
        String offerId = offerRef.getId();
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
        msg.setText(summary);
        msg.setCreatedAt(Timestamp.now());
        batch.set(msgRef, msg);

        if (!convExists) {
            Conversation conv = new Conversation();
            conv.setId(convId);
            conv.setListingId(listing.getId());
            conv.setBuyerId(senderId);
            conv.setSellerId(listing.getSellerId());
            conv.setLastMessage(summary);
            conv.setLastMessageAt(msg.getCreatedAt());
            conv.setCreatedAt(Timestamp.now());
            batch.set(convRef, conv);
        } else {
            batch.update(convRef, "lastMessage", summary, "lastMessageAt", msg.getCreatedAt());
        }

        queueNotification(listing.getSellerId(), NotificationType.NEW_OFFER,
                "Đề nghị giá mới", "Có người gửi đề nghị cho tin: " + listing.getTitle(),
                convId, batch);

        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> commitTask) {
                if (commitTask.isSuccessful()) {
                    cb.onSuccess(offerId);
                } else {
                    cb.onError(getErrorMessage(commitTask.getException(), "Lỗi gửi đề nghị."));
                }
            }
        });
    }

    public void respondToOffer(String offerId, boolean accept, DataCallback<String> cb) {
        try {
            if (!com.nhom3.pubgapp.util.NetworkMonitor.getInstance().isOnline()) {
                cb.onError("Thiết bị đang ngoại tuyến. Vui lòng kiểm tra kết nối mạng.");
                return;
            }
        } catch (UnsupportedOperationException ignored) {
        }

        String senderId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "";
        if (senderId.isEmpty()) {
            cb.onError("Vui lòng đăng nhập để thực hiện thao tác.");
            return;
        }

        DocumentReference offerRef = db.collection(COLLECTION_OFFERS).document(offerId);
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

                if (!senderId.equals(offer.getSellerId())) {
                    cb.onError("Chỉ người bán mới có quyền trả lời đề nghị.");
                    return;
                }

                if (offer.getStatus() != OfferStatus.PENDING) {
                    cb.onError("Đề nghị này đã được xử lý trước đó.");
                    return;
                }

                if (!accept) {
                    rejectOffer(offerRef, offer, cb);
                } else {
                    acceptOffer(offerRef, offer, cb);
                }
            }
        });
    }

    private void rejectOffer(DocumentReference offerRef, Offer offer, DataCallback<String> cb) {
        offer.reject();
        WriteBatch batch = db.batch();
        batch.update(offerRef, "status", OfferStatus.REJECTED.name());
        queueNotification(offer.getBuyerId(), NotificationType.OFFER_RESULT,
                "Đề nghị bị từ chối", "Người bán đã từ chối đề nghị của bạn.",
                offer.getConversationId(), batch);

        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> commitTask) {
                if (commitTask.isSuccessful()) {
                    cb.onSuccess(null);
                } else {
                    cb.onError(getErrorMessage(commitTask.getException(), "Lỗi từ chối đề nghị."));
                }
            }
        });
    }

    private void acceptOffer(DocumentReference offerRef, Offer offer, DataCallback<String> cb) {
        DocumentReference listingRef = db.collection(COLLECTION_LISTINGS).document(offer.getListingId());
        listingRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (!task.isSuccessful() || task.getResult() == null || !task.getResult().exists()) {
                    cb.onError("Không tìm thấy tin đăng.");
                    return;
                }
                Listing listing = task.getResult().toObject(Listing.class);
                if (listing == null || listing.getStatus() != ListingStatus.AVAILABLE) {
                    // Tin không còn khả dụng thì từ chối đề nghị và báo lỗi
                    offer.reject();
                    WriteBatch rejectBatch = db.batch();
                    rejectBatch.update(offerRef, "status", OfferStatus.REJECTED.name());
                    queueNotification(offer.getBuyerId(), NotificationType.OFFER_RESULT,
                            "Đề nghị bị từ chối",
                            "Tin không còn ở trạng thái khả dụng để chấp nhận.",
                            offer.getConversationId(), rejectBatch);
                    rejectBatch.commit();
                    cb.onError("Tin không còn khả dụng để chấp nhận đề nghị.");
                    return;
                }
                listing.setId(task.getResult().getId());
                executeAcceptOfferBatch(offerRef, offer, listingRef, listing, cb);
            }
        });
    }

    private void executeAcceptOfferBatch(DocumentReference offerRef, Offer offer,
                                         DocumentReference listingRef, Listing listing,
                                         DataCallback<String> cb) {
        db.collection(COLLECTION_OFFERS)
                .whereEqualTo("listingId", offer.getListingId())
                .whereEqualTo("status", OfferStatus.PENDING.name())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> queryTask) {
                        WriteBatch batch = db.batch();
                        offer.accept();
                        batch.update(offerRef, "status", OfferStatus.ACCEPTED.name());

                        DealRepository dealRepo = new DealRepository();
                        Deal deal = dealRepo.buildFromOffer(offer, listing);
                        DocumentReference dealRef = db.collection(COLLECTION_DEALS).document();
                        String dealId = dealRef.getId();
                        deal.setId(dealId);
                        batch.set(dealRef, deal);

                        batch.update(listingRef, "status", ListingStatus.RESERVED.name(),
                                "reservedDealId", dealId,
                                "updatedAt", Timestamp.now());

                        if (queryTask.isSuccessful() && queryTask.getResult() != null) {
                            for (DocumentSnapshot doc : queryTask.getResult().getDocuments()) {
                                if (!doc.getId().equals(offer.getId())) {
                                    batch.update(doc.getReference(), "status", OfferStatus.REJECTED.name());
                                    String otherBuyerId = doc.getString("buyerId");
                                    String otherConvId = doc.getString("conversationId");
                                    if (otherBuyerId != null) {
                                        queueNotification(otherBuyerId, NotificationType.OFFER_RESULT,
                                                "Đề nghị bị từ chối",
                                                "Tin đăng đã được giao dịch với người khác.",
                                                otherConvId, batch);
                                    }
                                }
                            }
                        }

                        String acceptBody = "Người bán đã chấp nhận đề nghị của bạn. "
                                + "Nhấn để xem chi tiết giao dịch.";
                        queueNotification(offer.getBuyerId(), NotificationType.OFFER_RESULT,
                                "Đề nghị được chấp nhận", acceptBody, dealId, batch);

                        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> commitTask) {
                                if (commitTask.isSuccessful()) {
                                    cb.onSuccess(dealId);
                                } else {
                                    cb.onError(getErrorMessage(commitTask.getException(),
                                            "Lỗi chấp nhận đề nghị."));
                                }
                            }
                        });
                    }
                });
    }

    public ListenerRegistration observeOffers(String conversationId, DataCallback<Void> cb) {
        return observeOffersData(conversationId, new DataCallback<List<Offer>>() {
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

    ListenerRegistration observeOffersData(String conversationId, DataCallback<List<Offer>> cb) {
        Query query = db.collection(COLLECTION_OFFERS).whereEqualTo("conversationId", conversationId);
        return query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value,
                                @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    cb.onError(error.getMessage());
                    return;
                }
                List<Offer> list = new ArrayList<>();
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

    static String validateOffer(Offer offer, Listing listing, String senderId) {
        if (offer == null || listing == null) {
            return "Thông tin đề nghị không hợp lệ.";
        }
        if (senderId != null && senderId.equals(listing.getSellerId())) {
            return "Không thể gửi đề nghị cho tin đăng của chính mình.";
        }
        if (listing.getStatus() != ListingStatus.AVAILABLE) {
            return "Tin đăng không còn ở trạng thái khả dụng để gửi đề nghị.";
        }
        if (offer.getOfferType() == null || listing.getAcceptedOfferTypes() == null
                || !listing.getAcceptedOfferTypes().contains(offer.getOfferType())) {
            return "Người bán không chấp nhận hình thức đề nghị này.";
        }
        if (offer.getOfferType() == OfferType.MONEY || offer.getOfferType() == OfferType.MONEY_AND_ITEM) {
            if (offer.getAmount() < listing.getMinOfferPrice()) {
                return "Giá đề nghị chưa phù hợp, hãy thử mức khác.";
            }
        }
        if (offer.getOfferType() == OfferType.ITEM) {
            if (offer.getAmount() != 0 || offer.getExchangeItem() == null
                    || offer.getExchangeItem().trim().isEmpty()) {
                return "Vui lòng nhập thông tin món đồ trao đổi.";
            }
        }
        if (offer.getOfferType() == OfferType.MONEY_AND_ITEM) {
            if (offer.getExchangeItem() == null || offer.getExchangeItem().trim().isEmpty()) {
                return "Vui lòng nhập thông tin món đồ trao đổi.";
            }
        }
        return null;
    }

    static String formatOfferSummary(Offer offer) {
        if (offer == null || offer.getOfferType() == null) {
            return "Đề nghị giá mới";
        }
        if (offer.getOfferType() == OfferType.MONEY) {
            return "Đề nghị: " + com.nhom3.pubgapp.util.FormatUtils.formatPrice(offer.getAmount());
        }
        if (offer.getOfferType() == OfferType.ITEM) {
            String item = offer.getExchangeItem() != null ? offer.getExchangeItem().trim() : "";
            return "Đề nghị đổi: " + item;
        }
        if (offer.getOfferType() == OfferType.MONEY_AND_ITEM) {
            String item = offer.getExchangeItem() != null ? offer.getExchangeItem().trim() : "";
            return "Đề nghị: " + com.nhom3.pubgapp.util.FormatUtils.formatPrice(offer.getAmount())
                    + " + " + item;
        }
        return "Đề nghị giá mới";
    }

    static List<Offer> filterOtherPendingOffers(List<Offer> allPending, String currentOfferId) {
        List<Offer> result = new ArrayList<>();
        if (allPending == null) {
            return result;
        }
        for (Offer o : allPending) {
            if (o != null && o.getId() != null && !o.getId().equals(currentOfferId)) {
                result.add(o);
            }
        }
        return result;
    }

    private void queueNotification(String recipientId, NotificationType type,
                                   String title, String body, String targetId, WriteBatch batch) {
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
        notif.setCreatedAt(Timestamp.now());
        DocumentReference notifRef = db.collection(COLLECTION_NOTIFICATIONS).document();
        notif.setId(notifRef.getId());
        batch.set(notifRef, notif);
    }

    private void commitBatch(WriteBatch batch, DataCallback<Void> cb, String defaultErrorMessage) {
        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    cb.onSuccess(null);
                } else {
                    cb.onError(getErrorMessage(task.getException(), defaultErrorMessage));
                }
            }
        });
    }

    private String getErrorMessage(Exception exception, String fallback) {
        if (exception != null && exception.getMessage() != null) {
            return exception.getMessage();
        }
        return fallback;
    }
}
