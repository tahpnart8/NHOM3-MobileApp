package com.nhom3.pubgapp.data;

import com.nhom3.pubgapp.model.Category;
import java.util.List;

// Skeleton: the module owner fills each body as the issue assigns it.
public class CategoryRepository extends BaseRepository {

    public void getAll(DataCallback<List<Category>> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void getAllForAdmin(DataCallback<List<Category>> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void getChildren(String parentId, DataCallback<List<Category>> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void add(Category category, DataCallback<String> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void update(Category category, DataCallback<Void> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void delete(String id, DataCallback<Void> cb) {
        cb.onError(DataCallback.NOT_READY);
    }
}
