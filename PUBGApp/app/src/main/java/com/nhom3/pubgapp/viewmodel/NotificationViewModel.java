package com.nhom3.pubgapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.nhom3.pubgapp.data.DataCallback;
import com.nhom3.pubgapp.data.NotificationRepository;
import com.nhom3.pubgapp.model.AppNotification;
import com.nhom3.pubgapp.util.SessionManager;

import java.util.List;

// State for the notification list and the unread badge. Action bodies report NOT_READY until the repositories are filled.
public class NotificationViewModel extends ViewModel {

    private final MutableLiveData<List<AppNotification>> notifications = new MutableLiveData<>();
    private final MutableLiveData<Integer> unreadCount = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> done = new MutableLiveData<>();

    private final NotificationRepository notificationRepository;

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
        loading.setValue(true);
        error.setValue(null);
        notificationRepository.getForUser(uid, new DataCallback<List<AppNotification>>() {
            @Override
            public void onSuccess(List<AppNotification> list) {
                loading.setValue(false);
                notifications.setValue(list);
                int count = 0;
                if (list != null) {
                    for (AppNotification item : list) {
                        if (!item.isRead()) {
                            count++;
                        }
                    }
                }
                unreadCount.setValue(count);
            }

            @Override
            public void onError(String message) {
                loading.setValue(false);
                error.setValue(message != null ? message : "Không thể tải danh sách thông báo.");
            }
        });
    }

    public void markRead(final String id) {
        if (id == null || id.isEmpty()) {
            return;
        }
        notificationRepository.markRead(id, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
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
                    notifications.setValue(current);
                    unreadCount.setValue(unread);
                }
            }

            @Override
            public void onError(String message) {
                error.setValue(message);
            }
        });
    }

    public void clearError() {
        error.setValue(null);
    }

    public void clearDone() {
        done.setValue(false);
    }
}
