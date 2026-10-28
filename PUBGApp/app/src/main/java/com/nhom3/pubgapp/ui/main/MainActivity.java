package com.nhom3.pubgapp.ui.main;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.DialogFragmentNavigator;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.snackbar.Snackbar;
import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.databinding.ActivityMainBinding;
import com.nhom3.pubgapp.model.User;
import com.nhom3.pubgapp.ui.common.AvatarBinder;
import com.nhom3.pubgapp.util.SessionManager;
import com.nhom3.pubgapp.viewmodel.AuthViewModel;
import com.nhom3.pubgapp.viewmodel.DealViewModel;
import com.nhom3.pubgapp.viewmodel.NotificationViewModel;
import com.nhom3.pubgapp.viewmodel.ProfileViewModel;

/** Hosts nav_main with the five-tab bottom bar. */
public class MainActivity extends AppCompatActivity {

    // The unread badge shows this many, then "99+".
    private static final int BADGE_MAX = 99;
    private static final String STATE_AUTO_RELEASE_CHECKED = "autoReleaseChecked";
    private static final String STATE_PENDING_TAB_ID = "pendingTabId";
    // A keyed instance keeps the restore result apart from the AuthViewModel the child screens share.
    public static final String RESTORE_VIEW_MODEL_KEY = "mainRestoreSession";

    private ActivityMainBinding binding;
    private NavController navController;
    private AuthViewModel restoreViewModel;
    private NotificationViewModel notificationViewModel;
    private DealViewModel dealViewModel;
    private ProfileViewModel profileViewModel;
    private boolean autoReleaseChecked;
    private int pendingTabId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        if (savedInstanceState != null) {
            autoReleaseChecked = savedInstanceState.getBoolean(STATE_AUTO_RELEASE_CHECKED, false);
            pendingTabId = savedInstanceState.getInt(STATE_PENDING_TAB_ID, 0);
        }
        applyWindowInsets();
        setUpViewModels();
        setUpNavigation();
        setUpTopBar();
        startSession();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_AUTO_RELEASE_CHECKED, autoReleaseChecked);
        outState.putInt(STATE_PENDING_TAB_ID, pendingTabId);
    }

    enum TabAction {
        NAVIGATE,
        WAIT_RESTORE,
        BLOCK_TO_SIGN_IN
    }

    /** Decides whether to allow navigation, wait for session restore, or block to sign-in. */
    static TabAction decideTabAction(int tabId, boolean loggedIn, boolean isRestoring) {
        if (!isTabBlockedForGuest(tabId, false)) {
            return TabAction.NAVIGATE;
        }
        if (loggedIn) {
            return TabAction.NAVIGATE;
        }
        if (isRestoring) {
            return TabAction.WAIT_RESTORE;
        }
        return TabAction.BLOCK_TO_SIGN_IN;
    }

    /** Guests may open Home and Search only; the other three tabs send them to A01. */
    static boolean isTabBlockedForGuest(int tabId, boolean loggedIn) {
        if (loggedIn) {
            return false;
        }
        return tabId == R.id.postListingFragment
                || tabId == R.id.conversationListFragment
                || tabId == R.id.myProfileFragment;
    }

    /** The auto release check runs once per fresh start, and only for a signed-in user. */
    static boolean shouldCheckAutoRelease(boolean alreadyChecked, boolean loggedIn) {
        return loggedIn && !alreadyChecked;
    }

    static boolean isBadgeVisible(@Nullable Integer unreadCount) {
        return unreadCount != null && unreadCount.intValue() > 0;
    }

    static boolean isBadgeOverflow(@Nullable Integer unreadCount) {
        return unreadCount != null && unreadCount.intValue() > BADGE_MAX;
    }

    private void setUpViewModels() {
        ViewModelProvider provider = new ViewModelProvider(this);
        restoreViewModel = provider.get(RESTORE_VIEW_MODEL_KEY, AuthViewModel.class);
        notificationViewModel = provider.get(NotificationViewModel.class);
        dealViewModel = provider.get(DealViewModel.class);
        // Activity scope: the same instance MyProfileFragment uses, so both read one profile listener.
        profileViewModel = provider.get(ProfileViewModel.class);
        restoreViewModel.getLoggedIn().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean loggedIn) {
                if (loggedIn != null && loggedIn.booleanValue()) {
                    onSessionReady();
                }
            }
        });
        restoreViewModel.getError().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String message) {
                showRestoreError(message);
            }
        });
        restoreViewModel.getLoading().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean loading) {
                if (loading != null && !loading.booleanValue()) {
                    onRestoreSessionFinished();
                }
            }
        });
        notificationViewModel.getUnreadCount().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer unreadCount) {
                showUnreadCount(unreadCount);
            }
        });
        // The profile is a live listener, so the Google photo saved just after sign in shows without a restart.
        profileViewModel.getProfile().observe(this, new Observer<User>() {
            @Override
            public void onChanged(User profile) {
                showTopBarAvatar();
            }
        });
    }

    // Firebase may still hold a session while SessionManager is empty (the process was killed).
    private void startSession() {
        if (SessionManager.getInstance().isLoggedIn()) {
            onSessionReady();
            return;
        }
        Boolean restoring = restoreViewModel.getLoading().getValue();
        if (restoring != null && restoring.booleanValue()) {
            return;
        }
        restoreViewModel.restoreSession();
    }

    private void onSessionReady() {
        updateTopBarForSession();
        refreshUnreadCount(getCurrentDestinationId());
        boolean loggedIn = SessionManager.getInstance().isLoggedIn();
        if (shouldCheckAutoRelease(autoReleaseChecked, loggedIn)) {
            autoReleaseChecked = true;
            dealViewModel.checkAutoRelease();
        }
    }

    // A locked or deleted account is signed out by restoreSession; the reason is shown once.
    private void showRestoreError(@Nullable String message) {
        if (message == null || message.isEmpty()) {
            return;
        }
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
        restoreViewModel.clearError();
    }

    private int getCurrentDestinationId() {
        NavDestination current = navController.getCurrentDestination();
        if (current == null) {
            return 0;
        }
        return current.getId();
    }

    // The bottom bar pads itself for the navigation bar; without it the root must, or a screen's own
    // bottom buttons sit on the gesture bar.
    private void applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), new OnApplyWindowInsetsListener() {
            @Override
            public WindowInsetsCompat onApplyWindowInsets(View view, WindowInsetsCompat insets) {
                Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                Insets keyboard = insets.getInsets(WindowInsetsCompat.Type.ime());
                int bottom = keyboard.bottom;
                boolean isBottomBarShown = binding.bottomNavigation.getVisibility() == View.VISIBLE;
                if (!isBottomBarShown) {
                    bottom = Math.max(bars.bottom, keyboard.bottom);
                }
                view.setPadding(bars.left, bars.top, bars.right, bottom);
                return insets;
            }
        });
    }

    private void setUpNavigation() {
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.navHostMain);
        navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(binding.bottomNavigation, navController);
        // Replaces the listener set above so a guest can be stopped before the tab changes.
        binding.bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                return handleTabSelected(item);
            }
        });
        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController controller,
                    @NonNull NavDestination destination, @Nullable Bundle arguments) {
                updateBars(destination);
            }
        });
    }

    private boolean handleTabSelected(MenuItem item) {
        boolean loggedIn = SessionManager.getInstance().isLoggedIn();
        Boolean restoring = restoreViewModel.getLoading().getValue();
        boolean isRestoring = restoring != null && restoring.booleanValue();
        TabAction action = decideTabAction(item.getItemId(), loggedIn, isRestoring);
        if (action == TabAction.WAIT_RESTORE) {
            pendingTabId = item.getItemId();
            Snackbar.make(binding.getRoot(), R.string.common_loading, Snackbar.LENGTH_SHORT).show();
            return false;
        }
        if (action == TabAction.BLOCK_TO_SIGN_IN) {
            navController.navigate(R.id.action_global_sign_in);
            return false;
        }
        return NavigationUI.onNavDestinationSelected(item, navController);
    }

    // Runs when session restore finishes, redirecting any tab tap that was deferred.
    private void onRestoreSessionFinished() {
        if (pendingTabId == 0) {
            return;
        }
        int targetTab = pendingTabId;
        pendingTabId = 0;
        boolean loggedIn = SessionManager.getInstance().isLoggedIn();
        if (loggedIn) {
            binding.bottomNavigation.setSelectedItemId(targetTab);
        } else {
            navController.navigate(R.id.action_global_sign_in);
        }
    }

    // Per ui/README.md: the bottom bar shows on the five tabs only, the top bar on Home only.
    private void updateBars(NavDestination destination) {
        // A bottom sheet floats over the current screen, so the bars stay as they are.
        if (destination instanceof DialogFragmentNavigator.Destination) {
            return;
        }
        int destinationId = destination.getId();
        boolean isTab = destinationId == R.id.homeFragment
                || destinationId == R.id.searchFragment
                || destinationId == R.id.postListingFragment
                || destinationId == R.id.conversationListFragment
                || destinationId == R.id.myProfileFragment;
        binding.bottomNavigation.setVisibility(isTab ? View.VISIBLE : View.GONE);
        // The bottom padding depends on whether the bottom bar shows, so ask for it again.
        ViewCompat.requestApplyInsets(binding.getRoot());
        binding.layoutTopBar.setVisibility(destinationId == R.id.homeFragment ? View.VISIBLE : View.GONE);
        updateTopBarForSession();
        refreshUnreadCount(destinationId);
    }

    // A guest sees neither the bell nor the avatar; Home draws the sign-in button itself.
    private void updateTopBarForSession() {
        boolean loggedIn = SessionManager.getInstance().isLoggedIn();
        int visibility = loggedIn ? View.VISIBLE : View.GONE;
        binding.frameNotifications.setVisibility(visibility);
        binding.frameTopBarAvatar.setVisibility(visibility);
        showTopBarAvatar();
        if (loggedIn) {
            // Does nothing when this account's profile is already loaded or loading.
            profileViewModel.loadProfile(SessionManager.getInstance().getCurrentUserId());
        }
    }

    // Until the first snapshot arrives, the session's copy of the user gives the initial; a guest's is cleared.
    private void showTopBarAvatar() {
        SessionManager session = SessionManager.getInstance();
        User user = session.getCurrentUser();
        if (user == null) {
            AvatarBinder.bind(binding.includeAvatar, null, null);
            return;
        }
        User profile = profileViewModel.getProfile().getValue();
        if (profile != null && session.getCurrentUserId().equals(profile.getId())) {
            user = profile;
        }
        AvatarBinder.bind(binding.includeAvatar, user.getDisplayName(), user.getAvatarBase64());
    }

    // The bell is on Home only, so the count is read each time Home comes forward.
    private void refreshUnreadCount(int destinationId) {
        if (destinationId != R.id.homeFragment || !SessionManager.getInstance().isLoggedIn()) {
            return;
        }
        Boolean loading = notificationViewModel.getLoading().getValue();
        if (loading != null && loading.booleanValue()) {
            return;
        }
        notificationViewModel.load();
    }

    private void showUnreadCount(@Nullable Integer unreadCount) {
        if (!isBadgeVisible(unreadCount)) {
            binding.textNotificationBadge.setVisibility(View.GONE);
            binding.buttonNotifications.setContentDescription(getString(R.string.common_top_bar_notifications));
            return;
        }
        int count = unreadCount.intValue();
        String text = String.valueOf(count);
        if (isBadgeOverflow(unreadCount)) {
            text = getString(R.string.common_top_bar_badge_overflow, BADGE_MAX);
        }
        binding.textNotificationBadge.setText(text);
        binding.textNotificationBadge.setVisibility(View.VISIBLE);
        binding.buttonNotifications.setContentDescription(
                getString(R.string.common_top_bar_notifications_unread, count));
    }

    private void setUpTopBar() {
        binding.buttonNotifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navController.navigate(R.id.action_global_notifications);
            }
        });
        binding.frameTopBarAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Selecting the tab lets NavigationUI switch the destination and the highlight together.
                binding.bottomNavigation.setSelectedItemId(R.id.myProfileFragment);
            }
        });
    }
}
