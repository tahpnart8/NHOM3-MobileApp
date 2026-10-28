package com.nhom3.pubgapp.viewmodel;

import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Holds the live Firestore listeners of one ViewModel, one per key.
// Putting a new listener under a key removes the old one, so reopening a screen never stacks listeners.
public class ListenerSet {

    private final Map<String, ListenerRegistration> registrations =
            new HashMap<String, ListenerRegistration>();

    public synchronized void put(String key, ListenerRegistration registration) {
        remove(key);
        if (registration != null) {
            registrations.put(key, registration);
        }
    }

    public synchronized boolean has(String key) {
        return registrations.containsKey(key);
    }

    public synchronized void remove(String key) {
        ListenerRegistration old = registrations.remove(key);
        if (old != null) {
            old.remove();
        }
    }

    // Called from onCleared: a listener left behind keeps reading and costs quota after the screen is gone.
    public synchronized void clear() {
        List<ListenerRegistration> all = new ArrayList<ListenerRegistration>(registrations.values());
        registrations.clear();
        for (ListenerRegistration registration : all) {
            registration.remove();
        }
    }
}
