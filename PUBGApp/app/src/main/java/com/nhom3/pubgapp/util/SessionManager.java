package com.nhom3.pubgapp.util;

import com.nhom3.pubgapp.model.User;

/** Holds the signed-in user and the admin flag for the whole app. */
public class SessionManager {

    private static SessionManager instance;

    private User currentUser;
    // Only drives the UI; the real permission is decided by the Security Rules.
    private boolean admin;

    public static SessionManager getInstance() {
        throw new UnsupportedOperationException("NOT_READY");
    }

    public void start(User user, boolean isAdmin) {
        this.currentUser = user;
        this.admin = isAdmin;
    }

    public void clear() {
        this.currentUser = null;
        this.admin = false;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public String getCurrentUserId() {
        if (currentUser == null) {
            return null;
        }
        return currentUser.getId();
    }

    public boolean isAdmin() {
        return admin;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }
}
