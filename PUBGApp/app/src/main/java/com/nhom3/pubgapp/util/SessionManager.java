package com.nhom3.pubgapp.util;

import com.nhom3.pubgapp.model.User;

/** Holds the signed-in user and the admin flag for the whole app. */
public class SessionManager {

    private static SessionManager instance;

    private User currentUser;
    // Only drives the UI; the real permission is decided by the Security Rules.
    private boolean admin;

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
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
            return "";
        }
        String id = currentUser.getId();
        if (id == null) {
            return "";
        }
        return id;
    }

    public boolean isAdmin() {
        return admin;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }
}

