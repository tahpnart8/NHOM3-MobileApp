package com.nhom3.pubgapp.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.databinding.FragmentAdminDashboardBinding;
import com.nhom3.pubgapp.model.Complaint;
import com.nhom3.pubgapp.model.Report;
import com.nhom3.pubgapp.model.SellerVerification;
import com.nhom3.pubgapp.model.enums.ComplaintStatus;
import com.nhom3.pubgapp.model.enums.ReportStatus;
import com.nhom3.pubgapp.util.NetworkMonitor;
import com.nhom3.pubgapp.viewmodel.AdminComplaintViewModel;
import com.nhom3.pubgapp.viewmodel.AdminContentViewModel;
import com.nhom3.pubgapp.viewmodel.AdminUserViewModel;

import java.util.List;

// Screen E01: the "Cần xử lý" list and the shortcuts. Read only, except the debug demo data block (DemoDataPanel).
public class AdminDashboardFragment extends Fragment {

    private static final String STATE_LOADED = "loaded";
    private static final String STATE_LEFT_SCREEN = "leftScreen";
    private static final String STATE_REPORTS_ERROR = "reportsError";
    private static final String STATE_VERIFICATIONS_ERROR = "verificationsError";
    private static final String STATE_COMPLAINTS_ERROR = "complaintsError";
    private static final String ARG_PENDING_ONLY = "pendingOnly";
    private static final String ARG_COMPLAINT_ID = "complaintId";

    @Nullable
    private FragmentAdminDashboardBinding binding;
    private AdminContentViewModel contentViewModel;
    private AdminUserViewModel userViewModel;
    private AdminComplaintViewModel complaintViewModel;
    private AdminTaskAdapter adapter;
    private DemoDataPanel demoDataPanel;

    private boolean hasLoaded;
    // True after the admin went to another screen, so coming back reloads the tasks.
    private boolean leftScreen;
    // The ViewModel error is cleared once shown, so the fragment keeps the text for rotation.
    @Nullable
    private String reportsError;
    @Nullable
    private String verificationsError;
    @Nullable
    private String complaintsError;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            hasLoaded = savedInstanceState.getBoolean(STATE_LOADED);
            leftScreen = savedInstanceState.getBoolean(STATE_LEFT_SCREEN);
            reportsError = savedInstanceState.getString(STATE_REPORTS_ERROR);
            verificationsError = savedInstanceState.getString(STATE_VERIFICATIONS_ERROR);
            complaintsError = savedInstanceState.getString(STATE_COMPLAINTS_ERROR);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        contentViewModel = new ViewModelProvider(this).get(AdminContentViewModel.class);
        userViewModel = new ViewModelProvider(this).get(AdminUserViewModel.class);
        complaintViewModel = new ViewModelProvider(this).get(AdminComplaintViewModel.class);

        setupTaskList();
        setupButtons();
        demoDataPanel = new DemoDataPanel(this, binding.includeDemoData);
        demoDataPanel.attach();
        observeViewModels();

        if (!hasLoaded || leftScreen) {
            hasLoaded = true;
            leftScreen = false;
            loadComplaints();
            loadReports();
            loadVerifications();
        }
        render();
    }

    @Override
    public void onStart() {
        super.onStart();
        updateOfflineBanner();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_LOADED, hasLoaded);
        outState.putBoolean(STATE_LEFT_SCREEN, leftScreen);
        outState.putString(STATE_REPORTS_ERROR, reportsError);
        outState.putString(STATE_VERIFICATIONS_ERROR, verificationsError);
        outState.putString(STATE_COMPLAINTS_ERROR, complaintsError);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (demoDataPanel != null) {
            demoDataPanel.detach();
            demoDataPanel = null;
        }
        // Views are gone after this call, so the binding must not outlive them.
        binding = null;
    }

    private void setupTaskList() {
        adapter = new AdminTaskAdapter(new AdminTaskAdapter.OnTaskClickListener() {
            @Override
            public void onTaskClick(AdminDashboardRules.TaskRow row) {
                openTask(row);
            }
        });
        binding.recyclerTasks.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerTasks.setAdapter(adapter);

        binding.viewEmptyState.textEmptyTitle.setText(R.string.admin_dashboard_tasks_empty_title);
        binding.viewEmptyState.textEmptyMessage.setText(R.string.admin_dashboard_tasks_empty_message);
        binding.viewErrorState.buttonRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                retryFailedGroups();
            }
        });
    }

    private void setupButtons() {
        binding.buttonShortcutUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigate(R.id.action_admin_dashboard_to_user_list, null);
            }
        });
        binding.buttonShortcutReports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigate(R.id.action_admin_dashboard_to_moderation, null);
            }
        });
        binding.buttonShortcutComplaints.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigate(R.id.action_admin_dashboard_to_complaint_list, null);
            }
        });
        binding.buttonShortcutDeals.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigate(R.id.action_admin_dashboard_to_deal_monitor, null);
            }
        });
        binding.buttonShortcutCategories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigate(R.id.action_admin_dashboard_to_category, null);
            }
        });
        binding.buttonShortcutAdminAccounts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigate(R.id.action_admin_dashboard_to_admin_account, null);
            }
        });
        binding.buttonBackToApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requireActivity().finish();
            }
        });
    }

    private void observeViewModels() {
        Observer<Object> renderObserver = new Observer<Object>() {
            @Override
            public void onChanged(Object value) {
                render();
            }
        };
        contentViewModel.getReports().observe(getViewLifecycleOwner(), renderObserver);
        contentViewModel.getLoading().observe(getViewLifecycleOwner(), renderObserver);
        userViewModel.getVerifications().observe(getViewLifecycleOwner(), renderObserver);
        userViewModel.getLoading().observe(getViewLifecycleOwner(), renderObserver);
        complaintViewModel.getComplaints().observe(getViewLifecycleOwner(), renderObserver);
        complaintViewModel.getLoading().observe(getViewLifecycleOwner(), renderObserver);

        contentViewModel.getError().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String message) {
                if (message != null && !message.isEmpty()) {
                    reportsError = message;
                    contentViewModel.clearError();
                    render();
                }
            }
        });
        userViewModel.getError().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String message) {
                if (message != null && !message.isEmpty()) {
                    verificationsError = message;
                    userViewModel.clearError();
                    render();
                }
            }
        });
        complaintViewModel.getError().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String message) {
                if (message != null && !message.isEmpty()) {
                    complaintsError = message;
                    complaintViewModel.clearError();
                    render();
                }
            }
        });
    }

    // The complaint queue is a live listener, so a new complaint appears here without a reload.
    private void loadComplaints() {
        complaintsError = null;
        complaintViewModel.loadComplaints(ComplaintStatus.OPEN, null);
    }

    private void loadReports() {
        reportsError = null;
        // A null target type means reports about every kind of object.
        contentViewModel.loadReports(ReportStatus.OPEN, null);
    }

    private void loadVerifications() {
        verificationsError = null;
        userViewModel.loadVerificationRequests();
    }

    private void retryFailedGroups() {
        if (complaintsError != null) {
            loadComplaints();
        }
        if (reportsError != null) {
            loadReports();
        }
        if (verificationsError != null) {
            loadVerifications();
        }
        render();
    }

    // E01 README: a complaint row opens D06. The complaint id is the deal id, as in the complaint list.
    private void openTask(AdminDashboardRules.TaskRow row) {
        if (row.getKind() == AdminDashboardRules.KIND_COMPLAINT) {
            Bundle args = new Bundle();
            args.putString(ARG_COMPLAINT_ID, row.getTargetId());
            navigate(R.id.adminComplaintDetailFragment, args);
            return;
        }
        if (row.getKind() == AdminDashboardRules.KIND_VERIFICATION) {
            Bundle args = new Bundle();
            args.putBoolean(ARG_PENDING_ONLY, true);
            navigate(R.id.action_admin_dashboard_to_user_list, args);
            return;
        }
        navigate(R.id.action_admin_dashboard_to_moderation, null);
    }

    private void navigate(int actionId, @Nullable Bundle args) {
        leftScreen = true;
        NavHostFragment.findNavController(this).navigate(actionId, args);
    }

    // Shows exactly one of: the list, the empty note, the error, the spinner; the error may sit under the list.
    private void render() {
        if (binding == null) {
            return;
        }
        List<Report> reports = contentViewModel.getReports().getValue();
        List<SellerVerification> verifications = userViewModel.getVerifications().getValue();
        List<Complaint> complaints = complaintViewModel.getComplaints().getValue();
        List<AdminDashboardRules.TaskRow> rows =
                AdminDashboardRules.buildRows(complaints, reports, verifications);
        adapter.submitRows(rows);

        boolean loading = isTrue(contentViewModel.getLoading().getValue())
                || isTrue(userViewModel.getLoading().getValue())
                || isTrue(complaintViewModel.getLoading().getValue());
        boolean failed = reportsError != null || verificationsError != null || complaintsError != null;
        boolean hasRows = !rows.isEmpty();
        boolean loadedAll = reports != null && verifications != null && complaints != null;

        binding.recyclerTasks.setVisibility(hasRows ? View.VISIBLE : View.GONE);
        binding.progressTasks.setVisibility(loading && !hasRows ? View.VISIBLE : View.GONE);
        binding.viewErrorState.layoutErrorState.setVisibility(failed && !loading ? View.VISIBLE : View.GONE);
        binding.viewEmptyState.layoutEmptyState.setVisibility(
                !hasRows && !failed && !loading && loadedAll ? View.VISIBLE : View.GONE);
        if (failed) {
            binding.viewErrorState.textErrorMessage.setText(joinErrors());
        }
        updateOfflineBanner();
    }

    // Each different message once, one per line; the groups often fail with the same offline text.
    private String joinErrors() {
        StringBuilder text = new StringBuilder();
        appendError(text, complaintsError);
        appendError(text, reportsError);
        appendError(text, verificationsError);
        return text.toString();
    }

    private void appendError(StringBuilder text, @Nullable String message) {
        if (message == null || text.indexOf(message) >= 0) {
            return;
        }
        if (text.length() > 0) {
            text.append("\n");
        }
        text.append(message);
    }

    private boolean isTrue(@Nullable Boolean value) {
        return value != null && value.booleanValue();
    }

    // NetworkMonitor still throws until its body is filled; then the banner stays hidden.
    private void updateOfflineBanner() {
        if (binding == null) {
            return;
        }
        boolean online = true;
        try {
            online = NetworkMonitor.getInstance().isOnline();
        } catch (UnsupportedOperationException e) {
            online = true;
        }
        binding.viewOfflineBanner.layoutOfflineBanner.setVisibility(online ? View.GONE : View.VISIBLE);
    }
}
