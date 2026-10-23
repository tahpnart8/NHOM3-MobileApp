package com.nhom3.pubgapp.ui.main;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.DialogFragmentNavigator;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.databinding.ActivityMainBinding;

/** Hosts nav_main with the five-tab bottom bar. */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        applyWindowInsets();
        setUpNavigation();
        setUpTopBar();
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
        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController controller,
                    @NonNull NavDestination destination, @Nullable Bundle arguments) {
                updateBars(destination);
            }
        });
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
