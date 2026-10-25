package com.nhom3.pubgapp;

import static androidx.test.espresso.Espresso.closeSoftKeyboard;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.nhom3.pubgapp.ui.account.AuthActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Scenario 1: UI testing for the Authentication screen (A01 Login, navigation to A03). */
@RunWith(AndroidJUnit4.class)
public class LoginFlowEspressoTest {

    private ActivityScenario<AuthActivity> scenario;

    @Before
    public void setUp() {
        EspressoTestHelper.ensureSignedOut();
        scenario = ActivityScenario.launch(AuthActivity.class);
    }

    @After
    public void tearDown() {
        if (scenario != null) {
            scenario.close();
        }
        EspressoTestHelper.ensureSignedOut();
    }

    @Test
    public void loginScreen_rendersAllInputViews() {
        onView(withId(R.id.editEmail)).check(matches(isDisplayed()));
        onView(withId(R.id.editPassword)).check(matches(isDisplayed()));
        onView(withId(R.id.buttonSignIn)).check(matches(isDisplayed()));
        onView(withId(R.id.buttonGoogleSignIn)).check(matches(isDisplayed()));
        onView(withId(R.id.buttonRegister)).check(matches(isDisplayed()));
        onView(withId(R.id.textForgotPassword)).check(matches(isDisplayed()));
    }

    @Test
    public void loginScreen_emptyPassword_showsError() {
        onView(withId(R.id.editEmail)).perform(typeText("tester@pubgapp.test"));
        closeSoftKeyboard();
        onView(withId(R.id.buttonSignIn)).perform(click());

        onView(withId(R.id.layoutPassword))
                .check(matches(hasDescendant(withText(R.string.account_login_password_empty))));
    }

    @Test
    public void loginScreen_invalidEmail_showsError() {
        onView(withId(R.id.editEmail)).perform(typeText("invalid-email-address"));
        closeSoftKeyboard();
        onView(withId(R.id.editPassword)).perform(typeText("ValidPass123"));
        closeSoftKeyboard();
        onView(withId(R.id.buttonSignIn)).perform(click());

        onView(withId(R.id.layoutEmail))
                .check(matches(hasDescendant(withText(R.string.account_login_email_invalid))));
    }

    @Test
    public void loginScreen_navigateToForgotPasswordAndBack() {
        onView(withId(R.id.textForgotPassword)).perform(click());
        onView(withId(R.id.buttonSend)).check(matches(isDisplayed()));

        onView(withId(R.id.buttonBack)).perform(click());
        onView(withId(R.id.buttonSignIn)).check(matches(isDisplayed()));
    }
}
