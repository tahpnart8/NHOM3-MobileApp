package com.nhom3.pubgapp.ui.admin;

import android.content.pm.ApplicationInfo;
import android.view.View;

import androidx.fragment.app.Fragment;

import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.data.DataCallback;
import com.nhom3.pubgapp.data.DemoDataSeeder;
import com.nhom3.pubgapp.databinding.ViewAdminDemoDataBinding;

// The debug-only demo data block of E01. Hidden in a release build. A run goes on through a rotation but only
// the screen that started it shows the result; the new screen shows a busy note and keeps its buttons usable.
class DemoDataPanel {

    private final Fragment host;
    private final ViewAdminDemoDataBinding views;
    private boolean detached = false;

    DemoDataPanel(Fragment host, ViewAdminDemoDataBinding views) {
        this.host = host;
        this.views = views;
    }

    void attach() {
        if (!isDebuggable()) {
            views.getRoot().setVisibility(View.GONE);
            return;
        }
        views.getRoot().setVisibility(View.VISIBLE);
        if (DemoDataSeeder.isRunning()) {
            showStatus(host.getString(R.string.admin_demo_busy));
        }
        views.buttonSeedDemo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSeed();
            }
        });
        views.buttonClearDemo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startClear();
            }
        });
    }

    void detach() {
        detached = true;
    }

    private boolean isDebuggable() {
        int flags = host.requireContext().getApplicationInfo().flags;
        return (flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
    }

    private void startSeed() {
        showStatus(host.getString(R.string.admin_demo_starting));
        showButtons(false);
        new DemoDataSeeder().seed(newProgressListener(), newResultCallback(R.string.admin_demo_seed_done));
    }

    private void startClear() {
        showStatus(host.getString(R.string.admin_demo_starting));
        showButtons(false);
        new DemoDataSeeder().clear(newProgressListener(), newResultCallback(R.string.admin_demo_clear_done));
    }

    private DemoDataSeeder.ProgressListener newProgressListener() {
        return new DemoDataSeeder.ProgressListener() {
            @Override
            public void onProgress(int done, int total) {
                showOnScreen(R.string.admin_demo_running, false, Integer.valueOf(done), Integer.valueOf(total));
            }
        };
    }

    private DataCallback<Integer> newResultCallback(final int doneMessage) {
        return new DataCallback<Integer>() {
            @Override
            public void onSuccess(Integer count) {
                int value = count != null ? count.intValue() : 0;
                if (value == 0) {
                    showOnScreen(R.string.admin_demo_nothing_to_do, true);
                    return;
                }
                showOnScreen(doneMessage, true, Integer.valueOf(value));
            }

            @Override
            public void onError(String message) {
                showOnScreen(R.string.admin_demo_error, true, message);
            }
        };
    }

    private void showStatus(String text) {
        views.textDemoStatus.setText(text);
        views.textDemoStatus.setVisibility(View.VISIBLE);
    }

    private void showButtons(boolean enabled) {
        views.buttonSeedDemo.setEnabled(enabled);
        views.buttonClearDemo.setEnabled(enabled);
    }

    // The repositories answer on any thread and the screen may be gone after a rotation, so check before touching views.
    private void showOnScreen(final int messageId, final boolean finished, final Object... arguments) {
        if (detached || !host.isAdded()) {
            return;
        }
        host.requireActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (detached || !host.isAdded()) {
                    return;
                }
                showStatus(host.getString(messageId, arguments));
                if (finished) {
                    showButtons(true);
                }
            }
        });
    }
}
