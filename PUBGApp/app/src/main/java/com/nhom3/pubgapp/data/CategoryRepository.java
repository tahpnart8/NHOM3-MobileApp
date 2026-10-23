package com.nhom3.pubgapp.data;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.AggregateQuerySnapshot;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.FieldValue;
import com.nhom3.pubgapp.model.Category;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryRepository extends BaseRepository {

    public void getAll(final DataCallback<List<Category>> cb) {
        db.collection("categories")
                .whereEqualTo("isActive", true)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot snapshots) {
                        List<Category> list = new ArrayList<Category>();
                        if (snapshots != null) {
                            for (DocumentSnapshot doc : snapshots.getDocuments()) {
                                Category item = doc.toObject(Category.class);
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
                        cb.onError(e.getMessage() != null ? e.getMessage() : "Error loading categories");
                    }
                });
    }

    public void getAllForAdmin(final DataCallback<List<Category>> cb) {
        db.collection("categories")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot snapshots) {
                        List<Category> list = new ArrayList<Category>();
                        if (snapshots != null) {
                            for (DocumentSnapshot doc : snapshots.getDocuments()) {
                                Category item = doc.toObject(Category.class);
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
                        cb.onError(e.getMessage() != null ? e.getMessage() : "Error loading categories");
                    }
                });
    }

    public void getChildren(String parentId, final DataCallback<List<Category>> cb) {
        db.collection("categories")
                .whereEqualTo("parentId", parentId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot snapshots) {
                        List<Category> list = new ArrayList<Category>();
                        if (snapshots != null) {
                            for (DocumentSnapshot doc : snapshots.getDocuments()) {
                                Category item = doc.toObject(Category.class);
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
                        cb.onError(e.getMessage() != null ? e.getMessage() : "Error loading child categories");
                    }
                });
    }

    public void add(final Category category, final DataCallback<String> cb) {
        final DocumentReference ref = db.collection("categories").document();
        category.setId(ref.getId());
        ref.set(category)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        cb.onSuccess(ref.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        cb.onError(e.getMessage() != null ? e.getMessage() : "Error adding category");
                    }
                });
    }

    public void update(final Category category, final DataCallback<Void> cb) {
        if (category == null || category.getId() == null || category.getId().trim().isEmpty()) {
            cb.onError("Category ID is required");
            return;
        }
        if (category.getParentId() != null && !category.getParentId().trim().isEmpty()) {
            db.collection("categories").whereEqualTo("parentId", category.getId()).count()
                    .get(AggregateSource.SERVER)
                    .addOnSuccessListener(new OnSuccessListener<AggregateQuerySnapshot>() {
                        @Override
                        public void onSuccess(AggregateQuerySnapshot snapshot) {
                            if (snapshot.getCount() > 0) {
                                String msg = "Không thể chuyển danh mục đang có danh mục con "
                                        + "thành danh mục con";
                                cb.onError(msg);
                                return;
                            }
                            performUpdate(category, cb);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            cb.onError(e.getMessage() != null ? e.getMessage() : "Error checking child categories");
                        }
                    });
        } else {
            performUpdate(category, cb);
        }
    }

    private void performUpdate(Category category, final DataCallback<Void> cb) {
        Map<String, Object> updates = new HashMap<String, Object>();
        updates.put("name", category.getName() != null ? category.getName() : "");
        updates.put("parentId", category.getParentId() != null ? category.getParentId() : "");
        List<String> conditions = category.getConditionFields();
        updates.put("conditionFields", conditions != null ? conditions : new ArrayList<String>());
        updates.put("isActive", category.isActive());
        updates.put("updatedAt", FieldValue.serverTimestamp());

        db.collection("categories").document(category.getId())
                .update(updates)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        cb.onSuccess(null);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        cb.onError(e.getMessage() != null ? e.getMessage() : "Error updating category");
                    }
                });
    }

    public void delete(final String id, final DataCallback<Void> cb) {
        if (id == null || id.trim().isEmpty()) {
            cb.onError("Category ID is required");
            return;
        }
        db.collection("categories").whereEqualTo("parentId", id).count().get(AggregateSource.SERVER)
                .addOnSuccessListener(new OnSuccessListener<AggregateQuerySnapshot>() {
                    @Override
                    public void onSuccess(AggregateQuerySnapshot catCountSnapshot) {
                        if (catCountSnapshot.getCount() > 0) {
                            cb.onError("Không thể xóa danh mục còn danh mục con; hãy ẩn danh mục");
                            return;
                        }
                        db.collection("listings").whereEqualTo("categoryId", id).count().get(AggregateSource.SERVER)
                                .addOnSuccessListener(new OnSuccessListener<AggregateQuerySnapshot>() {
                                    @Override
                                    public void onSuccess(AggregateQuerySnapshot listCountSnapshot) {
                                        if (listCountSnapshot.getCount() > 0) {
                                            String msg = "Không thể xóa danh mục còn tin đăng; "
                                                    + "hãy ẩn danh mục";
                                            cb.onError(msg);
                                            return;
                                        }
                                        db.collection("categories").document(id).delete()
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
                                                                ? e.getMessage() : "Error deleting category";
                                                        cb.onError(err);
                                                    }
                                                });
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(Exception e) {
                                        cb.onError(e.getMessage() != null ? e.getMessage() : "Error checking listings");
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        cb.onError(e.getMessage() != null ? e.getMessage() : "Error checking child categories");
                    }
                });
    }
}
