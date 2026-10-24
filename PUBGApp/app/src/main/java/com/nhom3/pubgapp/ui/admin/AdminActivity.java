package com.nhom3.pubgapp.ui.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.databinding.ActivityAdminBinding;
import com.nhom3.pubgapp.util.SessionManager;

/** Hosts nav_admin; it has no bottom bar. */
public class AdminActivity extends AppCompatActivity {

    private ActivityAdminBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        if (!SessionManager.getInstance().isAdmin()) {
            // The flag only hides the UI; the Security Rules are what protect admin data (R12).
            Toast.makeText(this, R.string.admin_no_permission, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        binding = ActivityAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        applyWindowInsets();
    }

    // Keeps content clear of the system bars and above the keyboard.
    private void applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), new OnApplyWindowInsetsListener() {
            @Override
            public WindowInsetsCompat onApplyWindowInsets(View view, WindowInsetsCompat insets) {
                int types = WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime();
                Insets bars = insets.getInsets(types);
                view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
                return insets;
            }
        });
    }
}
