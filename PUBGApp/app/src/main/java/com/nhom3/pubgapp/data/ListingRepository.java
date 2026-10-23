package com.nhom3.pubgapp.data;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.nhom3.pubgapp.data.local.AppDatabase;
import com.nhom3.pubgapp.data.local.HistoryDao;
import com.nhom3.pubgapp.data.local.ListingDao;
import com.nhom3.pubgapp.data.local.ListingEntity;
import com.nhom3.pubgapp.data.local.RecentSearchEntity;
import com.nhom3.pubgapp.data.local.RecentViewEntity;
import com.nhom3.pubgapp.model.BannedKeyword;
import com.nhom3.pubgapp.model.Listing;
import com.nhom3.pubgapp.model.SearchFilter;
import com.nhom3.pubgapp.model.User;
import com.nhom3.pubgapp.model.enums.ItemCondition;
import com.nhom3.pubgapp.model.enums.ListingStatus;
import com.nhom3.pubgapp.model.enums.OfferStatus;
import com.nhom3.pubgapp.model.enums.OfferType;
import com.nhom3.pubgapp.model.enums.SortOrder;
import com.nhom3.pubgapp.model.enums.TradeType;
import com.nhom3.pubgapp.util.FormatUtils;
import com.nhom3.pubgapp.util.KeywordFilter;
import com.nhom3.pubgapp.util.NetworkMonitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ListingRepository extends BaseRepository {

    private static final String TAG = "ListingRepository";
    private static final String COLLECTION_LISTINGS = "listings";
    private static final String COLLECTION_USERS = "users";
    private static final String COLLECTION_OFFERS = "offers";
    private static final String COLLECTION_DEALS = "deals";
    private static final String COLLECTION_BANNED_KEYWORDS = "bannedKeywords";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public void create(final Listing listing, final DataCallback<String> cb) {
        final String uid = currentUserId();
        if (uid.isEmpty()) {
            cb.onError("Chưa đăng nhập");
            return;
        }
        db.collection(COLLECTION_USERS).document(uid).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot doc) {
                        User user = doc != null ? doc.toObject(User.class) : null;
                        boolean emailVerified = auth.getCurrentUser() != null
                                && auth.getCurrentUser().isEmailVerified();
                        if (user == null || !user.canSell(emailVerified)) {
                            cb.onError("Tài khoản chưa đủ điều kiện bán hàng");
                            return;
                        }
                        checkBannedKeywords(listing.getTitle(), listing.getDescription(),
                                new DataCallback<Boolean>() {
                                    @Override
                                    public void onSuccess(Boolean flagged) {
                                        listing.setSellerId(uid);
                                        listing.setFlagged(flagged != null && flagged);
                                        listing.setStatus(ListingStatus.AVAILABLE);
                                        listing.buildSearchKeywords();

                                        final DocumentReference ref;
                                        if (listing.getId() != null && !listing.getId().isEmpty()) {
                                            ref = db.collection(COLLECTION_LISTINGS).document(listing.getId());
                                        } else {
                                            ref = db.collection(COLLECTION_LISTINGS).document();
                                            listing.setId(ref.getId());
                                        }
                                        ref.set(listing)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        cb.onSuccess(ref.getId());
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(Exception e) {
                                                        fail(cb, e);
                                                    }
                                                });
                                    }

                                    @Override
                                    public void onError(String error) {
                                        cb.onError(error);
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        fail(cb, e);
                    }
                });
    }

    public void saveDraft(final Listing listing, final DataCallback<String> cb) {
        String uid = currentUserId();
        if (uid.isEmpty()) {
            cb.onError("Chưa đăng nhập");
            return;
        }
        listing.setSellerId(uid);
        listing.setStatus(ListingStatus.DRAFT);
        listing.buildSearchKeywords();
        listing.touch();

        final DocumentReference ref;
        if (listing.getId() != null && !listing.getId().isEmpty()) {
            ref = db.collection(COLLECTION_LISTINGS).document(listing.getId());
        } else {
            ref = db.collection(COLLECTION_LISTINGS).document();
            listing.setId(ref.getId());
        }
        ref.set(listing)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        cb.onSuccess(ref.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        fail(cb, e);
                    }
                });
    }

    public void publish(final Listing listing, final DataCallback<String> cb) {
        final String uid = currentUserId();
        if (uid.isEmpty()) {
            cb.onError("Chưa đăng nhập");
            return;
        }
        db.collection(COLLECTION_USERS).document(uid).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot doc) {
                        User user = doc != null ? doc.toObject(User.class) : null;
                        boolean emailVerified = auth.getCurrentUser() != null
                                && auth.getCurrentUser().isEmailVerified();
                        if (user == null || !user.canSell(emailVerified)) {
                            cb.onError("Tài khoản chưa đủ điều kiện bán hàng");
                            return;
                        }
                        try {
                            listing.publish();
                        } catch (IllegalStateException e) {
                            cb.onError(e.getMessage());
                            return;
                        }
                        checkBannedKeywords(listing.getTitle(), listing.getDescription(),
                                new DataCallback<Boolean>() {
                                    @Override
                                    public void onSuccess(Boolean flagged) {
                                        listing.setSellerId(uid);
                                        listing.setFlagged(flagged != null && flagged);
                                        listing.buildSearchKeywords();

                                        db.collection(COLLECTION_LISTINGS).document(listing.getId()).set(listing)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        cb.onSuccess(listing.getId());
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(Exception e) {
                                                        fail(cb, e);
                                                    }
                                                });
                                    }

                                    @Override
                                    public void onError(String error) {
                                        cb.onError(error);
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        fail(cb, e);
                    }
                });
    }

    public void update(final Listing listing, final DataCallback<Void> cb) {
        if (listing == null || listing.getId() == null) {
            cb.onError("Mã tin đăng không được để trống");
            return;
        }
        final DocumentReference ref = db.collection(COLLECTION_LISTINGS).document(listing.getId());
        ref.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot doc) {
                if (doc == null || !doc.exists()) {
                    cb.onError("Không tìm thấy tin đăng");
                    return;
                }
                Listing existing = doc.toObject(Listing.class);
                ListingStatus status = existing != null ? existing.getStatus() : null;
                if (status != ListingStatus.DRAFT && status != ListingStatus.AVAILABLE
                        && status != ListingStatus.HIDDEN) {
                    cb.onError("Không thể chỉnh sửa tin ở trạng thái này");
                    return;
                }
                String reason = existing != null ? existing.getStatusReason() : null;
                if (status == ListingStatus.HIDDEN && reason != null && !reason.trim().isEmpty()) {
                    cb.onError("Không thể chỉnh sửa tin do quản trị viên đã ẩn");
                    return;
                }

                checkBannedKeywords(listing.getTitle(), listing.getDescription(),
                        new DataCallback<Boolean>() {
                            @Override
                            public void onSuccess(Boolean flagged) {
                                if (flagged != null && flagged) {
                                    listing.setFlagged(true);
                                }
                                listing.buildSearchKeywords();

                                Map<String, Object> updates = new HashMap<String, Object>();
                                updates.put("title", listing.getTitle());
                                updates.put("description", listing.getDescription());
                                updates.put("price", listing.getPrice());
                                updates.put("minOfferPrice", listing.getMinOfferPrice());
                                updates.put("categoryId", listing.getCategoryId());
                                if (listing.getItemCondition() != null) {
                                    updates.put("itemCondition", listing.getItemCondition().name());
                                }
                                updates.put("conditionValues", listing.getConditionValues());
                                if (listing.getTradeTypes() != null) {
                                    List<String> types = new ArrayList<String>();
                                    for (TradeType tt : listing.getTradeTypes()) {
                                        types.add(tt.name());
                                    }
                                    updates.put("tradeTypes", types);
                                }
                                updates.put("province", listing.getProvince());
                                updates.put("location", listing.getLocation());
                                updates.put("imageIds", listing.getImageIds());
                                updates.put("coverThumb", listing.getCoverThumb());
                                if (listing.getAcceptedOfferTypes() != null) {
                                    List<String> offerTypes = new ArrayList<String>();
                                    for (OfferType ot : listing.getAcceptedOfferTypes()) {
                                        offerTypes.add(ot.name());
                                    }
                                    updates.put("acceptedOfferTypes", offerTypes);
                                }
                                updates.put("searchKeywords", listing.getSearchKeywords());
                                updates.put("flagged", listing.isFlagged());
                                updates.put("updatedAt", FieldValue.serverTimestamp());

                                ref.update(updates)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                cb.onSuccess(null);
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(Exception e) {
                                                fail(cb, e);
                                            }
                                        });
                            }

                            @Override
                            public void onError(String error) {
                                cb.onError(error);
                            }
                        });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                fail(cb, e);
            }
        });
    }

    public void setHidden(final String id, final boolean hidden, final DataCallback<Void> cb) {
        final DocumentReference listingRef = db.collection(COLLECTION_LISTINGS).document(id);
        listingRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot doc) {
                if (doc == null || !doc.exists()) {
                    cb.onError("Không tìm thấy tin đăng");
                    return;
                }
                Listing listing = doc.toObject(Listing.class);
                if (listing == null) {
                    cb.onError("Không thể đọc thông tin tin đăng");
                    return;
                }
                String statusReason = listing.getStatusReason();
                if (!hidden) {
                    if (statusReason != null && !statusReason.trim().isEmpty()) {
                        cb.onError("Không thể hiện lại tin do quản trị viên đã ẩn hoặc gỡ");
                        return;
                    }
                    try {
                        listing.restore();
                    } catch (IllegalStateException e) {
                        cb.onError(e.getMessage());
                        return;
                    }
                    listingRef.update("status", ListingStatus.AVAILABLE.name(),
                            "statusReason", null,
                            "updatedAt", FieldValue.serverTimestamp())
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    cb.onSuccess(null);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(Exception e) {
                                    fail(cb, e);
                                }
                            });
                } else {
                    try {
                        listing.hide("");
                    } catch (IllegalStateException e) {
                        cb.onError(e.getMessage());
                        return;
                    }
                    db.collection(COLLECTION_OFFERS)
                            .whereEqualTo("listingId", id)
                            .whereEqualTo("status", OfferStatus.PENDING.name())
                            .get()
                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot offerDocs) {
                                    WriteBatch batch = db.batch();
                                    batch.update(listingRef,
                                            "status", ListingStatus.HIDDEN.name(),
                                            "statusReason", "",
                                            "updatedAt", FieldValue.serverTimestamp());
                                    if (offerDocs != null) {
                                        for (DocumentSnapshot offerDoc : offerDocs.getDocuments()) {
                                            batch.update(offerDoc.getReference(), "status",
                                                    OfferStatus.REJECTED.name());
                                        }
                                    }
                                    batch.commit()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    cb.onSuccess(null);
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(Exception e) {
                                                    fail(cb, e);
                                                }
                                            });
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(Exception e) {
                                    fail(cb, e);
                                }
                            });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                fail(cb, e);
            }
        });
    }

    public void setStatus(final String id,
                          final ListingStatus from,
                          final ListingStatus to,
                          final DataCallback<Void> cb) {
        boolean isAvailableToSold = from == ListingStatus.AVAILABLE && to == ListingStatus.SOLD;
        boolean isSoldToAvailable = from == ListingStatus.SOLD && to == ListingStatus.AVAILABLE;
        if (!isAvailableToSold && !isSoldToAvailable) {
            cb.onError("Chuyển đổi trạng thái không hợp lệ");
            return;
        }

        final DocumentReference listingRef = db.collection(COLLECTION_LISTINGS).document(id);
        listingRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot doc) {
                if (doc == null || !doc.exists()) {
                    cb.onError("Không tìm thấy tin đăng");
                    return;
                }
                Listing listing = doc.toObject(Listing.class);
                if (listing == null || listing.getStatus() != from) {
                    cb.onError("Trạng thái hiện tại không khớp");
                    return;
                }

                if (to == ListingStatus.SOLD) {
                    try {
                        listing.markSold();
                    } catch (IllegalStateException e) {
                        cb.onError(e.getMessage());
                        return;
                    }
                    db.collection(COLLECTION_OFFERS)
                            .whereEqualTo("listingId", id)
                            .whereEqualTo("status", OfferStatus.PENDING.name())
                            .get()
                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot offerDocs) {
                                    WriteBatch batch = db.batch();
                                    batch.update(listingRef, "status", ListingStatus.SOLD.name(),
                                            "updatedAt", FieldValue.serverTimestamp());
                                    if (offerDocs != null) {
                                        for (DocumentSnapshot offerDoc : offerDocs.getDocuments()) {
                                            batch.update(offerDoc.getReference(), "status",
                                                    OfferStatus.REJECTED.name());
                                        }
                                    }
                                    batch.commit()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    cb.onSuccess(null);
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(Exception e) {
                                                    fail(cb, e);
                                                }
                                            });
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(Exception e) {
                                    fail(cb, e);
                                }
                            });
                } else {
                    db.collection(COLLECTION_DEALS)
                            .whereEqualTo("listingId", id)
                            .whereEqualTo("status", "COMPLETED")
                            .get()
                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot deals) {
                                    if (deals != null && !deals.isEmpty()) {
                                        cb.onError("Tin đã có giao dịch hoàn tất, không thể mở lại");
                                        return;
                                    }
                                    listingRef.update("status", ListingStatus.AVAILABLE.name(),
                                            "updatedAt", FieldValue.serverTimestamp())
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    cb.onSuccess(null);
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(Exception e) {
                                                    fail(cb, e);
                                                }
                                            });
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(Exception e) {
                                    fail(cb, e);
                                }
                            });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                fail(cb, e);
            }
        });
    }

    public void delete(final String id, final DataCallback<Void> cb) {
        db.collection(COLLECTION_LISTINGS).document(id).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot doc) {
                        if (doc == null || !doc.exists()) {
                            cb.onError("Không tìm thấy tin đăng");
                            return;
                        }
                        Listing listing = doc.toObject(Listing.class);
                        if (listing == null || listing.getStatus() != ListingStatus.DRAFT) {
                            cb.onError("Chỉ xóa được tin ở trạng thái bản nháp");
                            return;
                        }
                        db.collection(COLLECTION_LISTINGS).document(id).delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        cb.onSuccess(null);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(Exception e) {
                                        fail(cb, e);
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        fail(cb, e);
                    }
                });
    }

    public void getById(final String id, final DataCallback<Listing> cb) {
        db.collection(COLLECTION_LISTINGS).document(id).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot doc) {
                        if (doc != null && doc.exists()) {
                            Listing listing = doc.toObject(Listing.class);
                            if (listing != null) {
                                listing.setId(doc.getId());
                                cacheListing(listing);
                                cb.onSuccess(listing);
                                return;
                            }
                        }
                        loadFromCache(id, cb);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        loadFromCache(id, cb);
                    }
                });
    }

    public void getBySeller(final String sellerId, final DataCallback<List<Listing>> cb) {
        String currentUid = currentUserId();
        boolean isOwner = sellerId != null && sellerId.equals(currentUid);
        Query query = db.collection(COLLECTION_LISTINGS).whereEqualTo("sellerId", sellerId);
        if (!isOwner) {
            List<String> allowed = new ArrayList<String>();
            allowed.add(ListingStatus.AVAILABLE.name());
            allowed.add(ListingStatus.RESERVED.name());
            allowed.add(ListingStatus.SOLD.name());
            query = query.whereIn("status", allowed);
        }
        query.orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot snapshots) {
                        cb.onSuccess(toListingList(snapshots));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        fail(cb, e);
                    }
                });
    }

    public void getFeed(final SearchFilter filter, final Timestamp after,
                        final DataCallback<List<Listing>> cb) {
        boolean online = true;
        try {
            online = NetworkMonitor.getInstance().isOnline();
        } catch (UnsupportedOperationException e) {
            online = true;
        }

        if (!online) {
            if (after == null) {
                loadFeedFromCache(filter, cb);
            } else {
                cb.onError("Không có kết nối mạng");
            }
            return;
        }

        if (after == null) {
            loadFeedFromCache(filter, new DataCallback<List<Listing>>() {
                @Override
                public void onSuccess(List<Listing> cached) {
                    if (cached != null && !cached.isEmpty()) {
                        cb.onSuccess(cached);
                    }
                }

                @Override
                public void onError(String error) {
                    // Ignore cache read error, Firestore will be queried next
                }
            });
        }

        if (filter != null && filter.isFollowingOnly()) {
            String uid = currentUserId();
            if (uid.isEmpty()) {
                cb.onError("Chưa đăng nhập");
                return;
            }
            UserRepository userRepo = new UserRepository();
            userRepo.getFollowingIds(uid, new DataCallback<List<String>>() {
                @Override
                public void onSuccess(final List<String> followingIds) {
                    if (followingIds == null || followingIds.isEmpty()) {
                        cb.onSuccess(new ArrayList<Listing>());
                        return;
                    }
                    List<String> top30 = followingIds;
                    if (top30.size() > 30) {
                        top30 = top30.subList(0, 30);
                    }
                    executeFeedQuery(filter, after, top30, cb);
                }

                @Override
                public void onError(String error) {
                    cb.onError(error);
                }
            });
            return;
        }
        executeFeedQuery(filter, after, null, cb);
    }

    private void executeFeedQuery(final SearchFilter filter, final Timestamp after,
                                  final List<String> followingIds,
                                  final DataCallback<List<Listing>> cb) {
        Query query = db.collection(COLLECTION_LISTINGS).whereEqualTo("status", ListingStatus.AVAILABLE.name());
        if (followingIds != null && !followingIds.isEmpty()) {
            query = query.whereIn("sellerId", followingIds);
        }
        if (filter != null && filter.getCategoryId() != null && !filter.getCategoryId().isEmpty()) {
            query = query.whereEqualTo("categoryId", filter.getCategoryId());
        } else if (filter != null && filter.getProvince() != null && !filter.getProvince().isEmpty()) {
            query = query.whereEqualTo("province", filter.getProvince());
        }

        SortOrder sortOrder = filter != null ? filter.getSortOrder() : null;
        if (sortOrder == SortOrder.PRICE_ASC) {
            query = query.orderBy("price", Query.Direction.ASCENDING);
        } else if (sortOrder == SortOrder.PRICE_DESC) {
            query = query.orderBy("price", Query.Direction.DESCENDING);
        } else {
            query = query.orderBy("createdAt", Query.Direction.DESCENDING);
        }

        if (after != null) {
            query = query.startAfter(after);
        }
        query = query.limit(20);

        query.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot snapshots) {
                        List<Listing> rawList = toListingList(snapshots);
                        List<Listing> filteredList = new ArrayList<Listing>();
                        for (int i = 0; i < rawList.size(); i++) {
                            Listing item = rawList.get(i);
                            if (matchesMemoryFilter(item, filter)) {
                                filteredList.add(item);
                            }
                        }
                        cacheListings(filteredList);
                        cb.onSuccess(filteredList);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        if (after == null) {
                            loadFeedFromCache(filter, cb);
                        } else {
                            fail(cb, e);
                        }
                    }
                });
    }

    public void search(final SearchFilter filter, final Timestamp after,
                       final DataCallback<List<Listing>> cb) {
        String longestWord = "";
        if (filter != null && filter.getKeyword() != null) {
            FormatUtils fu = new FormatUtils();
            List<String> words = fu.toKeywords(filter.getKeyword());
            for (int i = 0; i < words.size(); i++) {
                String w = words.get(i);
                if (w.length() > longestWord.length()) {
                    longestWord = w;
                }
            }
        }

        Query query = db.collection(COLLECTION_LISTINGS).whereEqualTo("status", ListingStatus.AVAILABLE.name());
        if (!longestWord.isEmpty()) {
            query = query.whereArrayContains("searchKeywords", longestWord);
        }
        query = query.orderBy("createdAt", Query.Direction.DESCENDING);
        if (after != null) {
            query = query.startAfter(after);
        }
        query = query.limit(20);

        query.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot snapshots) {
                        List<Listing> rawList = toListingList(snapshots);
                        List<Listing> list = new ArrayList<Listing>();
                        for (int i = 0; i < rawList.size(); i++) {
                            Listing item = rawList.get(i);
                            if (matchesMemoryFilter(item, filter)) {
                                list.add(item);
                            }
                        }
                        cb.onSuccess(list);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        fail(cb, e);
                    }
                });
    }

    public void getSimilar(final Listing listing, final DataCallback<List<Listing>> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void recordView(final Listing listing) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                HistoryDao dao = getHistoryDao();
                if (dao != null && listing != null && listing.getId() != null) {
                    RecentViewEntity entity = new RecentViewEntity();
                    entity.setListingId(listing.getId());
                    entity.setCategoryId(listing.getCategoryId());
                    entity.setViewedAtMillis(System.currentTimeMillis());
                    dao.insertView(entity);
                    dao.trimViews(50);
                }
            }
        });
    }

    public void recordSearch(final String keyword) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                HistoryDao dao = getHistoryDao();
                if (dao != null && keyword != null && !keyword.trim().isEmpty()) {
                    RecentSearchEntity entity = new RecentSearchEntity();
                    entity.setKeyword(keyword.trim());
                    entity.setSearchedAtMillis(System.currentTimeMillis());
                    dao.insertSearch(entity);
                    dao.trimSearches(20);
                }
            }
        });
    }

    public void getBannedKeywords(final DataCallback<List<BannedKeyword>> cb) {
        db.collection(COLLECTION_BANNED_KEYWORDS)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot snapshots) {
                        List<BannedKeyword> list = new ArrayList<BannedKeyword>();
                        if (snapshots != null) {
                            for (DocumentSnapshot doc : snapshots.getDocuments()) {
                                BannedKeyword bk = doc.toObject(BannedKeyword.class);
                                if (bk != null) {
                                    bk.setId(doc.getId());
                                    list.add(bk);
                                }
                            }
                        }
                        cb.onSuccess(list);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        fail(cb, e);
                    }
                });
    }

    public void getRecentViews(final int limit, final DataCallback<List<String>> cb) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                HistoryDao dao = getHistoryDao();
                List<String> result = new ArrayList<String>();
                if (dao != null) {
                    List<RecentViewEntity> views = dao.getRecentViews(limit);
                    if (views != null) {
                        for (int i = 0; i < views.size(); i++) {
                            result.add(views.get(i).getListingId());
                        }
                    }
                }
                cb.onSuccess(result);
            }
        });
    }

    public void getRecentSearches(final int limit, final DataCallback<List<String>> cb) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                HistoryDao dao = getHistoryDao();
                List<String> result = new ArrayList<String>();
                if (dao != null) {
                    List<RecentSearchEntity> searches = dao.getRecentSearches(limit);
                    if (searches != null) {
                        for (int i = 0; i < searches.size(); i++) {
                            result.add(searches.get(i).getKeyword());
                        }
                    }
                }
                cb.onSuccess(result);
            }
        });
    }

    private void checkBannedKeywords(final String title, final String description,
                                     final DataCallback<Boolean> cb) {
        getBannedKeywords(new DataCallback<List<BannedKeyword>>() {
            @Override
            public void onSuccess(List<BannedKeyword> list) {
                String combined = (title != null ? title : "") + " " + (description != null ? description : "");
                KeywordFilter filter = new KeywordFilter();
                filter.setKeywords(list);
                boolean flagged = filter.containsBanned(combined);
                cb.onSuccess(flagged);
            }

            @Override
            public void onError(String error) {
                cb.onError(error);
            }
        });
    }

    private boolean matchesMemoryFilter(Listing item, SearchFilter filter) {
        if (filter == null || item == null) {
            return true;
        }
        if (filter.getCategoryId() != null && !filter.getCategoryId().isEmpty()) {
            if (!filter.getCategoryId().equals(item.getCategoryId())) {
                return false;
            }
        }
        if (filter.getProvince() != null && !filter.getProvince().isEmpty()) {
            if (!filter.getProvince().equals(item.getProvince())) {
                return false;
            }
        }
        if (filter.getMinPrice() > 0 && item.getPrice() < filter.getMinPrice()) {
            return false;
        }
        if (filter.getMaxPrice() > 0 && item.getPrice() > filter.getMaxPrice()) {
            return false;
        }
        if (filter.getConditions() != null && !filter.getConditions().isEmpty()) {
            if (!filter.getConditions().contains(item.getItemCondition())) {
                return false;
            }
        }
        if (filter.getTradeTypes() != null && !filter.getTradeTypes().isEmpty()) {
            boolean matchTrade = false;
            if (item.getTradeTypes() != null) {
                for (TradeType filterType : filter.getTradeTypes()) {
                    if (item.getTradeTypes().contains(filterType)) {
                        matchTrade = true;
                        break;
                    }
                }
            }
            if (!matchTrade) {
                return false;
            }
        }
        return true;
    }

    private List<Listing> toListingList(QuerySnapshot snapshots) {
        List<Listing> list = new ArrayList<Listing>();
        if (snapshots != null) {
            for (DocumentSnapshot doc : snapshots.getDocuments()) {
                Listing item = doc.toObject(Listing.class);
                if (item != null) {
                    item.setId(doc.getId());
                    list.add(item);
                }
            }
        }
        return list;
    }

    private void cacheListing(final Listing listing) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                ListingDao dao = getListingDao();
                if (dao != null && listing != null) {
                    ListingEntity entity = new ListingEntity().fromModel(listing);
                    dao.upsertAll(Collections.singletonList(entity));
                    dao.trim(200);
                }
            }
        });
    }

    private void cacheListings(final List<Listing> listings) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                ListingDao dao = getListingDao();
                if (dao != null && listings != null && !listings.isEmpty()) {
                    List<ListingEntity> entities = new ArrayList<ListingEntity>();
                    for (int i = 0; i < listings.size(); i++) {
                        entities.add(new ListingEntity().fromModel(listings.get(i)));
                    }
                    dao.upsertAll(entities);
                    dao.trim(200);
                }
            }
        });
    }

    private void loadFromCache(final String id, final DataCallback<Listing> cb) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                ListingDao dao = getListingDao();
                if (dao != null) {
                    ListingEntity entity = dao.getById(id);
                    if (entity != null) {
                        cb.onSuccess(entity.toModel());
                        return;
                    }
                }
                cb.onError("Không tìm thấy tin đăng");
            }
        });
    }

    private void loadFeedFromCache(final SearchFilter filter, final DataCallback<List<Listing>> cb) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                ListingDao dao = getListingDao();
                List<Listing> list = new ArrayList<Listing>();
                if (dao != null) {
                    List<ListingEntity> cached = dao.getFeed();
                    if (cached != null) {
                        for (int i = 0; i < cached.size(); i++) {
                            Listing item = cached.get(i).toModel();
                            if (matchesMemoryFilter(item, filter)) {
                                list.add(item);
                            }
                        }
                    }
                }
                cb.onSuccess(list);
            }
        });
    }

    private ListingDao getListingDao() {
        try {
            Context context = FirebaseApp.getInstance().getApplicationContext();
            if (context != null) {
                return AppDatabase.getInstance(context).listingDao();
            }
        } catch (Exception e) {
            Log.w(TAG, "Cannot access Room database", e);
        }
        return null;
    }

    private HistoryDao getHistoryDao() {
        try {
            Context context = FirebaseApp.getInstance().getApplicationContext();
            if (context != null) {
                return AppDatabase.getInstance(context).historyDao();
            }
        } catch (Exception e) {
            Log.w(TAG, "Cannot access Room database", e);
        }
        return null;
    }
}
