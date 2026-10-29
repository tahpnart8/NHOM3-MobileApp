package com.nhom3.pubgapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.nhom3.pubgapp.data.DataCallback;
import com.nhom3.pubgapp.data.NotificationRepository;
import com.nhom3.pubgapp.model.AppNotification;
import com.nhom3.pubgapp.util.SessionManager;

import java.util.List;

// State for the notification list and the unread badge.
public class NotificationViewModel extends ViewModel {

    private final MutableLiveData<List<AppNotification>> notifications = new MutableLiveData<>();
    private final MutableLiveData<Integer> unreadCount = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> done = new MutableLiveData<>();

    private final java.util.Set<String> pendingMarks = new java.util.HashSet<String>();
    private final NotificationRepository notificationRepository;
    // One live list per signed-in user; MainActivity keeps it, so the badge follows new notices.
    private final ListenerSet listeners = new ListenerSet();
    private String liveUid;
    private static final String KEY_NOTIFICATIONS = "notifications";

    public NotificationViewModel() {
        this.notificationRepository = new NotificationRepository();
    }

    public LiveData<List<AppNotification>> getNotifications() {
        return notifications;
    }

    public LiveData<Integer> getUnreadCount() {
        return unreadCount;
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

    public void load() {
        String uid = SessionManager.getInstance().getCurrentUserId();
        if (uid == null || uid.isEmpty()) {
            error.setValue("Chưa đăng nhập.");
            return;
        }
        if (uid.equals(liveUid) && listeners.has(KEY_NOTIFICATIONS)) {
            return;
        }
        // Another account signed in: drop the previous user's list and badge before the new snapshot arrives.
        if (liveUid != null && !uid.equals(liveUid)) {
            notifications.setValue(null);
            unreadCount.setValue(Integer.valueOf(0));
        }
        liveUid = uid;
        loading.setValue(true);
        error.setValue(null);
        listeners.put(KEY_NOTIFICATIONS, notificationRepository.observeForUser(uid,
                new DataCallback<List<AppNotification>>() {
                    @Override
                    public void onSuccess(List<AppNotification> list) {
                        loading.postValue(false);
                        notifications.postValue(list);
                        int count = 0;
                        if (list != null) {
                            for (AppNotification item : list) {
                                if (!item.isRead()) {
                                    count++;
                                }
                            }
                        }
                        unreadCount.postValue(Integer.valueOf(count));
                    }

                    @Override
                    public void onError(String message) {
                        listeners.remove(KEY_NOTIFICATIONS);
                        loading.postValue(false);
                        error.postValue(message != null ? message : "Không thể tải danh sách thông báo.");
                    }
                }));
    }

    public void markRead(final String id) {
        if (id == null || id.isEmpty()) {
            return;
        }
        if (pendingMarks.contains(id)) {
            return;
        }
        pendingMarks.add(id);
        notificationRepository.markRead(id, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                pendingMarks.remove(id);
                List<AppNotification> current = notifications.getValue();
                if (current != null) {
                    int unread = 0;
                    for (AppNotification item : current) {
                        if (id.equals(item.getId())) {
                            item.setRead(true);
                        }
                        if (!item.isRead()) {
                            unread++;
                        }
                    }
                    notifications.postValue(current);
                    unreadCount.postValue(Integer.valueOf(unread));
                }
            }

            @Override
            public void onError(String message) {
                pendingMarks.remove(id);
                if (notifications.getValue() == null) {
                    error.postValue(message);
                }
            }
        });
    }

    public void clearError() {
        error.setValue(null);
    }

    public void clearDone() {
        done.setValue(false);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        listeners.clear();
    }
}
