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

public class ChatRepository extends BaseRepository {

    private static final String COLLECTION_CONVERSATIONS = "conversations";
    private static final String COLLECTION_MESSAGES = "messages";
    private static final String COLLECTION_OFFERS = "offers";
    private static final String COLLECTION_LISTINGS = "listings";
    private static final String COLLECTION_DEALS = "deals";
    private static final String COLLECTION_NOTIFICATIONS = "notifications";

    public void getOrCreateConversation(String listingId, String buyerId, DataCallback<Conversation> cb) {
        String convId = listingId + "_" + buyerId;
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
                        String sellerId = task.getResult().getString("sellerId");
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
        // Tải các cuộc trò chuyện người dùng tham gia với vai trò người mua
        db.collection(COLLECTION_CONVERSATIONS).whereEqualTo("buyerId", uid).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> buyerTask) {
                        if (!buyerTask.isSuccessful()) {
                            cb.onError(getErrorMessage(buyerTask.getException(), "Lỗi tải cuộc trò chuyện."));
                            return;
                        }
                        // Tải các cuộc trò chuyện vai trò người bán
                        db.collection(COLLECTION_CONVERSATIONS).whereEqualTo("sellerId", uid).get()
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
                cb.onSuccess(list);
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
        String senderId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "";
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

        DocumentReference convRef = db.collection(COLLECTION_CONVERSATIONS).document(id);
        batch.update(convRef, "lastMessage", text, "lastMessageAt", msg.getCreatedAt());

        convRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                    Conversation conv = task.getResult().toObject(Conversation.class);
                    String recipientId = conv != null ? conv.otherParty(senderId) : null;
                    if (recipientId != null) {
                        queueNotification(recipientId, NotificationType.NEW_MESSAGE,
                                "Tin nhắn mới", text, id, batch);
                    }
                }
                commitBatch(batch, cb, "Lỗi gửi tin nhắn.");
            }
        });
    }

    public void sendImage(String id, Uri uri, DataCallback<Void> cb) {
        MediaRepository mediaRepo = new MediaRepository();
        mediaRepo.saveImage(uri, ImagePurpose.CHAT, new DataCallback<String>() {
            @Override
            public void onSuccess(String imageId) {
                sendImageMessage(id, imageId, cb);
            }

            @Override
            public void onError(String message) {
                cb.onError(message);
            }
        });
    }

    private void sendImageMessage(String id, String imageId, DataCallback<Void> cb) {
        String senderId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "";
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

        DocumentReference convRef = db.collection(COLLECTION_CONVERSATIONS).document(id);
        batch.update(convRef, "lastMessage", "[Hình ảnh]", "lastMessageAt", msg.getCreatedAt());

        convRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                    Conversation conv = task.getResult().toObject(Conversation.class);
                    String recipientId = conv != null ? conv.otherParty(senderId) : null;
                    if (recipientId != null) {
                        queueNotification(recipientId, NotificationType.NEW_MESSAGE,
                                "Tin nhắn hình ảnh mới", "[Hình ảnh]", id, batch);
                    }
                }
                commitBatch(batch, cb, "Lỗi gửi ảnh.");
            }
        });
    }

    public void sendOffer(Offer offer, DataCallback<String> cb) {
        db.collection(COLLECTION_LISTINGS).document(offer.getListingId()).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (!task.isSuccessful() || task.getResult() == null || !task.getResult().exists()) {
                            cb.onError("Không tìm thấy tin đăng.");
                            return;
                        }
                        Listing listing = task.getResult().toObject(Listing.class);
                        if (listing == null || !listing.acceptsOffer(offer)) {
                            cb.onError("Tin đăng không chấp nhận đề nghị này.");
                            return;
                        }
                        saveOfferAndMessage(offer, cb);
                    }
                });
    }

    private void saveOfferAndMessage(Offer offer, DataCallback<String> cb) {
        offer.setStatus(OfferStatus.PENDING);
        offer.setCreatedAt(Timestamp.now());

        WriteBatch batch = db.batch();
        DocumentReference offerRef = db.collection(COLLECTION_OFFERS).document();
        String offerId = offerRef.getId();
        offer.setId(offerId);
        batch.set(offerRef, offer);

        DocumentReference msgRef = db.collection(COLLECTION_CONVERSATIONS).document(offer.getConversationId())
                .collection(COLLECTION_MESSAGES).document();
        ChatMessage msg = new ChatMessage();
        msg.setId(msgRef.getId());
        msg.setConversationId(offer.getConversationId());
        msg.setSenderId(offer.getBuyerId());
        msg.setType(MessageType.OFFER);
        msg.setOfferId(offerId);
        msg.setText("Đã gửi một đề nghị giá.");
        msg.setCreatedAt(Timestamp.now());
        batch.set(msgRef, msg);

        DocumentReference convRef = db.collection(COLLECTION_CONVERSATIONS).document(offer.getConversationId());
        batch.update(convRef, "lastMessage", "Đề nghị giá mới", "lastMessageAt", msg.getCreatedAt());

        queueNotification(offer.getSellerId(), NotificationType.NEW_OFFER,
                "Đề nghị giá mới", "Có người gửi đề nghị cho tin đăng của bạn.",
                offer.getConversationId(), batch);

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
                if (!accept) {
                    rejectOffer(offerRef, offer, cb);
                } else {
                    acceptOffer(offerRef, offer, cb);
                }
            }
        });
    }

    private void rejectOffer(DocumentReference offerRef, Offer offer, DataCallback<String> cb) {
        WriteBatch batch = db.batch();
        batch.update(offerRef, "status", OfferStatus.REJECTED.name());
        queueNotification(offer.getBuyerId(), NotificationType.OFFER_RESULT,
                "Đề nghị đã bị từ chối", "Người bán đã từ chối đề nghị của bạn.",
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
                    // Tin không còn khả dụng thì từ chối đề nghị
                    offerRef.update("status", OfferStatus.REJECTED.name());
                    cb.onError("Tin không còn khả dụng.");
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
        // Tìm các offer PENDING khác của cùng tin đăng để từ chối
        db.collection(COLLECTION_OFFERS)
                .whereEqualTo("listingId", offer.getListingId())
                .whereEqualTo("status", OfferStatus.PENDING.name())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> queryTask) {
                        WriteBatch batch = db.batch();
                        // Chấp nhận offer hiện tại
                        batch.update(offerRef, "status", OfferStatus.ACCEPTED.name());

                        // Dựng Deal từ offer và listing
                        DealRepository dealRepo = new DealRepository();
                        Deal deal = dealRepo.buildFromOffer(offer, listing);
                        DocumentReference dealRef = db.collection(COLLECTION_DEALS).document();
                        String dealId = dealRef.getId();
                        deal.setId(dealId);
                        batch.set(dealRef, deal);

                        // Cập nhật tin sang RESERVED
                        batch.update(listingRef, "status", ListingStatus.RESERVED.name(),
                                "reservedDealId", dealId);

                        // Từ chối các offer PENDING khác
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

                        // Thông báo kết quả cho người mua được chấp nhận
                        String acceptBody = "Người bán đã chấp nhận đề nghị của bạn. "
                                + "Nhấn để xem giao dịch.";
                        queueNotification(offer.getBuyerId(), NotificationType.OFFER_RESULT,
                                "Đề nghị đã được chấp nhận", acceptBody, dealId, batch);

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
        Query query = db.collection(COLLECTION_OFFERS).whereEqualTo("conversationId", conversationId);
        return query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value,
                                @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    cb.onError(error.getMessage());
                    return;
                }
                cb.onSuccess(null);
            }
        });
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
