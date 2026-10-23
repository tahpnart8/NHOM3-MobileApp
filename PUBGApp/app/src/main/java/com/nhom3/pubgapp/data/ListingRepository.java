package com.nhom3.pubgapp.data;

import android.content.Context;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
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
import com.nhom3.pubgapp.model.enums.ListingStatus;
import com.nhom3.pubgapp.util.FormatUtils;
import com.nhom3.pubgapp.util.GeoUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ListingRepository extends BaseRepository {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public void create(final Listing listing, final DataCallback<String> cb) {
        final String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (uid == null) {
            cb.onError("Chưa đăng nhập");
            return;
        }
        db.collection("users").document(uid).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot doc) {
                        User user = doc != null ? doc.toObject(User.class) : null;
                        if (user == null || !user.canSell()) {
                            cb.onError("Tài khoản chưa đủ điều kiện bán hàng");
                            return;
                        }
                        checkBannedKeywords(listing.getTitle(), listing.getDescription(), new DataCallback<Boolean>() {
                            @Override
                            public void onSuccess(Boolean flagged) {
                                listing.setSellerId(uid);
                                listing.setFlagged(flagged != null && flagged);
                                listing.setStatus(ListingStatus.AVAILABLE);
                                listing.buildSearchKeywords();
                                listing.setCreatedAt(Timestamp.now());
                                listing.setUpdatedAt(Timestamp.now());

                                final DocumentReference ref;
                                if (listing.getId() != null && !listing.getId().isEmpty()) {
                                    ref = db.collection("listings").document(listing.getId());
                                } else {
                                    ref = db.collection("listings").document();
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
                                                String err = e.getMessage() != null
                                                        ? e.getMessage() : "Error saving listing";
                                                cb.onError(err);
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
                        cb.onError(e.getMessage() != null ? e.getMessage() : "Error checking user");
                    }
                });
    }

    public void saveDraft(final Listing listing, final DataCallback<String> cb) {
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (uid == null) {
            cb.onError("Chưa đăng nhập");
            return;
        }
        listing.setSellerId(uid);
        listing.setStatus(ListingStatus.DRAFT);
        listing.buildSearchKeywords();
        listing.setCreatedAt(Timestamp.now());
        listing.setUpdatedAt(Timestamp.now());

        final DocumentReference ref;
        if (listing.getId() != null && !listing.getId().isEmpty()) {
            ref = db.collection("listings").document(listing.getId());
        } else {
            ref = db.collection("listings").document();
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
                        cb.onError(e.getMessage() != null ? e.getMessage() : "Error saving draft");
                    }
                });
    }

    public void publish(final Listing listing, final DataCallback<String> cb) {
        final String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (uid == null) {
            cb.onError("Chưa đăng nhập");
            return;
        }
        db.collection("users").document(uid).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot doc) {
                        User user = doc != null ? doc.toObject(User.class) : null;
                        if (user == null || !user.canSell()) {
                            cb.onError("Tài khoản chưa đủ điều kiện bán hàng");
                            return;
                        }
                        try {
                            listing.publish();
                        } catch (IllegalStateException e) {
                            cb.onError(e.getMessage());
                            return;
                        }
                        checkBannedKeywords(listing.getTitle(), listing.getDescription(), new DataCallback<Boolean>() {
                            @Override
                            public void onSuccess(Boolean flagged) {
                                listing.setSellerId(uid);
                                listing.setFlagged(flagged != null && flagged);
                                listing.buildSearchKeywords();
                                listing.setUpdatedAt(Timestamp.now());

                                db.collection("listings").document(listing.getId()).set(listing)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                cb.onSuccess(listing.getId());
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(Exception e) {
                                                String err = e.getMessage() != null
                                                        ? e.getMessage() : "Error publishing listing";
                                                cb.onError(err);
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
                        cb.onError(e.getMessage() != null ? e.getMessage() : "Error checking user");
                    }
                });
    }

    public void update(final Listing listing, final DataCallback<Void> cb) {
        if (listing == null || listing.getId() == null) {
            cb.onError("Listing ID is required");
            return;
        }
        checkBannedKeywords(listing.getTitle(), listing.getDescription(), new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean flagged) {
                if (flagged != null && flagged) {
                    listing.setFlagged(true);
                }
                listing.buildSearchKeywords();
                listing.setUpdatedAt(Timestamp.now());

                db.collection("listings").document(listing.getId()).set(listing)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                cb.onSuccess(null);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(Exception e) {
                                cb.onError(e.getMessage() != null ? e.getMessage() : "Error updating listing");
                            }
                        });
            }

            @Override
            public void onError(String error) {
                cb.onError(error);
            }
        });
    }

    public void setHidden(final String id, final boolean hidden, final DataCallback<Void> cb) {
        final DocumentReference listingRef = db.collection("listings").document(id);
        listingRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot doc) {
                if (doc == null || !doc.exists()) {
                    cb.onError("Listing not found");
                    return;
                }
                Listing listing = doc.toObject(Listing.class);
                String statusReason = listing != null ? listing.getStatusReason() : null;
                if (!hidden) {
                    if (statusReason != null && !statusReason.trim().isEmpty()) {
                        cb.onError("Không thể hiện lại tin do quản trị viên đã ẩn hoặc gỡ");
                        return;
                    }
                    listingRef.update("status", ListingStatus.AVAILABLE.name(), "updatedAt", Timestamp.now())
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    cb.onSuccess(null);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(Exception e) {
                                    cb.onError(e.getMessage() != null ? e.getMessage() : "Error unhiding listing");
                                }
                            });
                } else {
                    db.collection("offers")
                            .whereEqualTo("listingId", id)
                            .whereEqualTo("status", "PENDING")
                            .get()
                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot offerDocs) {
                                    WriteBatch batch = db.batch();
                                    batch.update(listingRef,
                                            "status", ListingStatus.HIDDEN.name(),
                                            "statusReason", "",
                                            "updatedAt", Timestamp.now());
                                    if (offerDocs != null) {
                                        for (DocumentSnapshot offerDoc : offerDocs.getDocuments()) {
                                            batch.update(offerDoc.getReference(), "status", "REJECTED");
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
                                                    String err = e.getMessage() != null
                                                            ? e.getMessage() : "Error hiding listing";
                                                    cb.onError(err);
                                                }
                                            });
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(Exception e) {
                                    cb.onError(e.getMessage() != null ? e.getMessage() : "Error querying offers");
                                }
                            });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                cb.onError(e.getMessage() != null ? e.getMessage() : "Error loading listing");
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

        final DocumentReference listingRef = db.collection("listings").document(id);

        if (isAvailableToSold) {
            db.collection("offers")
                    .whereEqualTo("listingId", id)
                    .whereEqualTo("status", "PENDING")
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot offerDocs) {
                            WriteBatch batch = db.batch();
                            batch.update(listingRef, "status", ListingStatus.SOLD.name(), "updatedAt", Timestamp.now());
                            if (offerDocs != null) {
                                for (DocumentSnapshot offerDoc : offerDocs.getDocuments()) {
                                    batch.update(offerDoc.getReference(), "status", "REJECTED");
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
                                            cb.onError(e.getMessage() != null ? e.getMessage() : "Error marking sold");
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            cb.onError(e.getMessage() != null ? e.getMessage() : "Error checking offers");
                        }
                    });
        } else {
            db.collection("deals")
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
                            listingRef.update("status", ListingStatus.AVAILABLE.name(), "updatedAt", Timestamp.now())
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            cb.onSuccess(null);
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(Exception e) {
                                            String err = e.getMessage() != null
                                                    ? e.getMessage() : "Error reopening listing";
                                            cb.onError(err);
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            cb.onError(e.getMessage() != null ? e.getMessage() : "Error checking deals");
                        }
                    });
        }
    }

    public void delete(final String id, final DataCallback<Void> cb) {
        db.collection("listings").document(id).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot doc) {
                        if (doc == null || !doc.exists()) {
                            cb.onError("Listing not found");
                            return;
                        }
                        Listing listing = doc.toObject(Listing.class);
                        if (listing == null || listing.getStatus() != ListingStatus.DRAFT) {
                            cb.onError("Chỉ xóa được tin ở trạng thái bản nháp");
                            return;
                        }
                        db.collection("listings").document(id).delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        cb.onSuccess(null);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(Exception e) {
                                        cb.onError(e.getMessage() != null ? e.getMessage() : "Error deleting draft");
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        cb.onError(e.getMessage() != null ? e.getMessage() : "Error reading listing");
                    }
                });
    }

    public void getById(final String id, final DataCallback<Listing> cb) {
        db.collection("listings").document(id).get()
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
        String currentUid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        boolean isOwner = sellerId != null && sellerId.equals(currentUid);
        Query query = db.collection("listings").whereEqualTo("sellerId", sellerId);
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
                        cb.onSuccess(list);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        cb.onError(e.getMessage() != null ? e.getMessage() : "Error loading listings");
                    }
                });
    }

    public void getFeed(final SearchFilter filter, final Timestamp after, final DataCallback<List<Listing>> cb) {
        Query query = db.collection("listings").whereEqualTo("status", ListingStatus.AVAILABLE.name());
        if (filter != null && filter.getCategoryId() != null && !filter.getCategoryId().isEmpty()) {
            query = query.whereEqualTo("categoryId", filter.getCategoryId());
        } else if (filter != null && filter.getProvince() != null && !filter.getProvince().isEmpty()) {
            query = query.whereEqualTo("province", filter.getProvince());
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
                        List<Listing> list = new ArrayList<Listing>();
                        if (snapshots != null) {
                            for (DocumentSnapshot doc : snapshots.getDocuments()) {
                                Listing item = doc.toObject(Listing.class);
                                if (item != null) {
                                    item.setId(doc.getId());
                                    if (matchesMemoryFilter(item, filter)) {
                                        list.add(item);
                                    }
                                }
                            }
                        }
                        cacheListings(list);
                        cb.onSuccess(list);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        if (after == null) {
                            loadFeedFromCache(filter, cb);
                        } else {
                            cb.onError(e.getMessage() != null ? e.getMessage() : "Error loading feed");
                        }
                    }
                });
    }

    public void search(final SearchFilter filter, final Timestamp after, final DataCallback<List<Listing>> cb) {
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

        Query query = db.collection("listings").whereEqualTo("status", ListingStatus.AVAILABLE.name());
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
                        List<Listing> list = new ArrayList<Listing>();
                        if (snapshots != null) {
                            for (DocumentSnapshot doc : snapshots.getDocuments()) {
                                Listing item = doc.toObject(Listing.class);
                                if (item != null) {
                                    item.setId(doc.getId());
                                    if (matchesMemoryFilter(item, filter)) {
                                        list.add(item);
                                    }
                                }
                            }
                        }
                        cb.onSuccess(list);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        cb.onError(e.getMessage() != null ? e.getMessage() : "Error searching listings");
                    }
                });
    }

    public void getSimilar(final Listing listing, final DataCallback<List<Listing>> cb) {
        if (listing == null || listing.getCategoryId() == null) {
            cb.onSuccess(new ArrayList<Listing>());
            return;
        }
        db.collection("listings")
                .whereEqualTo("status", ListingStatus.AVAILABLE.name())
                .whereEqualTo("categoryId", listing.getCategoryId())
                .limit(20)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot snapshots) {
                        List<Listing> list = new ArrayList<Listing>();
                        if (snapshots != null) {
                            for (DocumentSnapshot doc : snapshots.getDocuments()) {
                                if (!doc.getId().equals(listing.getId())) {
                                    Listing item = doc.toObject(Listing.class);
                                    if (item != null) {
                                        item.setId(doc.getId());
                                        list.add(item);
                                    }
                                }
                            }
                        }
                        cb.onSuccess(list);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        cb.onError(e.getMessage() != null ? e.getMessage() : "Error loading similar listings");
                    }
                });
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
        db.collection("bannedKeywords")
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
                        cb.onError(e.getMessage() != null ? e.getMessage() : "Error loading banned keywords");
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

    private void checkBannedKeywords(final String title, final String description, final DataCallback<Boolean> cb) {
        getBannedKeywords(new DataCallback<List<BannedKeyword>>() {
            @Override
            public void onSuccess(List<BannedKeyword> list) {
                String combined = (title != null ? title : "") + " " + (description != null ? description : "");
                FormatUtils fu = new FormatUtils();
                String cleaned = fu.removeAccents(combined);
                boolean flagged = false;
                if (list != null) {
                    for (int i = 0; i < list.size(); i++) {
                        BannedKeyword bk = list.get(i);
                        if (bk != null && bk.getKeyword() != null && !bk.getKeyword().isEmpty()) {
                            if (cleaned.contains(bk.getKeyword())) {
                                flagged = true;
                                break;
                            }
                        }
                    }
                }
                cb.onSuccess(flagged);
            }

            @Override
            public void onError(String error) {
                cb.onSuccess(false);
            }
        });
    }

    private boolean matchesMemoryFilter(Listing item, SearchFilter filter) {
        if (filter == null) {
            return true;
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
        if (filter.hasRadius() && item.getLocation() != null) {
            GeoUtils geoUtils = new GeoUtils();
            if (!geoUtils.isWithinRadius(item.getLocation(), filter.getCenter(), (int) filter.getRadiusKm())) {
                return false;
            }
        }
        return true;
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
            // Ignored when running outside Android environment
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
            // Ignored when running outside Android environment
        }
        return null;
    }
}
