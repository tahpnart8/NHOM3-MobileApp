package com.nhom3.pubgapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.nhom3.pubgapp.ui.main.MainActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Scenario 2: UI testing for post listing tab protection when unauthenticated (R12). */
@RunWith(AndroidJUnit4.class)
public class PostListingFlowEspressoTest {

    private ActivityScenario<MainActivity> scenario;

    @Before
    public void setUp() {
        EspressoTestHelper.ensureSignedOut();
        scenario = ActivityScenario.launch(MainActivity.class);
    }

    @After
    public void tearDown() {
        if (scenario != null) {
            scenario.close();
        }
        EspressoTestHelper.ensureSignedOut();
    }

    @Test
    public void postListingTab_unauthenticatedUser_redirectsToSignIn() {
        // Tapping Post Listing tab when unauthenticated redirects to Sign In screen.
        onView(withId(R.id.postListingFragment)).perform(click());

        onView(withId(R.id.buttonSignIn)).check(matches(isDisplayed()));
    }

    @Test
    public void postListingTab_unauthenticatedUser_returnsToMainOnBack() {
        onView(withId(R.id.postListingFragment)).perform(click());
        onView(withId(R.id.buttonSignIn)).check(matches(isDisplayed()));

        // Pressing back from Sign In returns to MainActivity.
        pressBack();

        onView(withId(R.id.bottomNavigation)).check(matches(isDisplayed()));
    }
}
