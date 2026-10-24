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
import com.nhom3.pubgapp.util.SessionManager;
import com.nhom3.pubgapp.viewmodel.AuthViewModel;
import com.nhom3.pubgapp.viewmodel.DealViewModel;
import com.nhom3.pubgapp.viewmodel.NotificationViewModel;

/** Hosts nav_main with the five-tab bottom bar. */
public class MainActivity extends AppCompatActivity {

    // The unread badge shows this many, then "99+".
    private static final int BADGE_MAX = 99;
    private static final String STATE_AUTO_RELEASE_CHECKED = "autoReleaseChecked";
    // A keyed instance keeps the restore result apart from the AuthViewModel the child screens share.
    private static final String RESTORE_VIEW_MODEL_KEY = "mainRestoreSession";

    private ActivityMainBinding binding;
    private NavController navController;
    private AuthViewModel restoreViewModel;
    private NotificationViewModel notificationViewModel;
    private DealViewModel dealViewModel;
    private boolean autoReleaseChecked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        if (savedInstanceState != null) {
            autoReleaseChecked = savedInstanceState.getBoolean(STATE_AUTO_RELEASE_CHECKED, false);
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
        notificationViewModel.getUnreadCount().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer unreadCount) {
                showUnreadCount(unreadCount);
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

    // The bottom bar pads itself for the navigation bar, so only the keyboard is handled at the bottom.
    private void applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), new OnApplyWindowInsetsListener() {
            @Override
            public WindowInsetsCompat onApplyWindowInsets(View view, WindowInsetsCompat insets) {
                Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                Insets keyboard = insets.getInsets(WindowInsetsCompat.Type.ime());
                view.setPadding(bars.left, bars.top, bars.right, keyboard.bottom);
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
        if (isTabBlockedForGuest(item.getItemId(), loggedIn)) {
            navController.navigate(R.id.action_global_sign_in);
            return false;
        }
        return NavigationUI.onNavDestinationSelected(item, navController);
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
