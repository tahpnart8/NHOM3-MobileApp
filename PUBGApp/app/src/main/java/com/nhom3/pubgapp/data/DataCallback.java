package com.nhom3.pubgapp.data;

public interface DataCallback<T> {

    // Shared message while a repository method is still a skeleton.
    String NOT_READY = "Chức năng chưa sẵn sàng.";

    void onSuccess(T result);

    void onError(String message);
}
