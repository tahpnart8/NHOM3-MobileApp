package com.nhom3.pubgapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertEquals;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.nhom3.pubgapp.ui.admin.AdminActivity;
import com.nhom3.pubgapp.ui.main.MainActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Scenario 3: UI testing for admin access control and guest navigation without fake sessions (R12). */
@RunWith(AndroidJUnit4.class)
public class AdminVerificationFlowEspressoTest {

    @Before
    public void setUp() {
        EspressoTestHelper.ensureSignedOut();
    }

    @After
    public void tearDown() {
        EspressoTestHelper.ensureSignedOut();
    }

    @Test
    public void adminActivity_unauthenticatedUser_accessDeniedAndFinished() {
        ActivityScenario<AdminActivity> scenario = ActivityScenario.launch(AdminActivity.class);

        // AdminActivity finishes immediately if user does not possess admin permission.
        assertEquals(Lifecycle.State.DESTROYED, scenario.getState());
        scenario.close();
    }

    @Test
    public void guestNavigation_allowedTabs_showsPublicScreens() {
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);

        // Home screen is displayed by default for guests.
        onView(withId(R.id.cardSearch)).check(matches(isDisplayed()));

        // Guest navigates to Search tab.
        onView(withId(R.id.searchFragment)).perform(click());
        onView(withId(R.id.editKeyword)).check(matches(isDisplayed()));

        // Guest navigates back to Home tab.
        onView(withId(R.id.homeFragment)).perform(click());
        onView(withId(R.id.cardSearch)).check(matches(isDisplayed()));

        scenario.close();
    }

    @Test
    public void guestNavigation_protectedTab_redirectsToSignIn() {
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);

        // Conversation list tab is protected; guest clicking it redirects to Sign In.
        onView(withId(R.id.conversationListFragment)).perform(click());
        onView(withId(R.id.buttonSignIn)).check(matches(isDisplayed()));

        scenario.close();
    }
}
