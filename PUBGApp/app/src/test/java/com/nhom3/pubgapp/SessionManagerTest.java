package com.nhom3.pubgapp;

import com.nhom3.pubgapp.model.User;
import com.nhom3.pubgapp.util.SessionManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class SessionManagerTest {

    private SessionManager sessionManager;

    @Before
    public void setUp() {
        sessionManager = SessionManager.getInstance();
        sessionManager.clear();
    }

    @After
    public void tearDown() {
        sessionManager.clear();
    }

    @Test
    public void testInitialState() {
        assertNotNull(sessionManager);
        assertFalse(sessionManager.isLoggedIn());
        assertFalse(sessionManager.isAdmin());
        assertNull(sessionManager.getCurrentUser());
        assertEquals("", sessionManager.getCurrentUserId());
    }

    @Test
    public void testStartSession() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setDisplayName("Ngân");

        sessionManager.start(user, true);

        assertTrue(sessionManager.isLoggedIn());
        assertTrue(sessionManager.isAdmin());
        assertEquals(user, sessionManager.getCurrentUser());
    }

    @Test
    public void testClearSession() {
        User user = new User();
        sessionManager.start(user, false);
        assertTrue(sessionManager.isLoggedIn());

        sessionManager.clear();
        assertFalse(sessionManager.isLoggedIn());
        assertFalse(sessionManager.isAdmin());
        assertNull(sessionManager.getCurrentUser());
    }
}
