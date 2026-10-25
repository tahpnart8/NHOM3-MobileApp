package com.nhom3.pubgapp.ui.admin;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.databinding.DialogAdminCategoryBinding;
import com.nhom3.pubgapp.databinding.FragmentAdminCategoryBinding;
import com.nhom3.pubgapp.model.Category;
import com.nhom3.pubgapp.util.NetworkMonitor;
import com.nhom3.pubgapp.viewmodel.AdminContentViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Screen E05: Category management tree, active switches, and add/edit/delete dialogs. */
public class AdminCategoryFragment extends Fragment {

    private static final String STATE_DIALOG_OPEN = "state_dialog_open";
    private static final String STATE_CATEGORY_ID = "state_category_id";
    private static final String STATE_CATEGORY_NAME = "state_category_name";
    private static final String STATE_PARENT_ID = "state_parent_id";
    private static final String STATE_ACTIVE = "state_active";

    @Nullable
    private FragmentAdminCategoryBinding binding;
    private AdminContentViewModel viewModel;
    private AdminCategoryAdapter adapter;

    @Nullable
    private AlertDialog activeDialog;
    @Nullable
    private DialogAdminCategoryBinding activeDialogBinding;
    @Nullable
    private String activeEditingId;
    private boolean lastActionWasDelete;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminCategoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AdminContentViewModel.class);
        setUpToolbar();
        setUpList();
        setUpAddButton();
        observeViewModel();

        viewModel.loadCategories();

        if (savedInstanceState != null && savedInstanceState.getBoolean(STATE_DIALOG_OPEN, false)) {
            String id = savedInstanceState.getString(STATE_CATEGORY_ID);
            String name = savedInstanceState.getString(STATE_CATEGORY_NAME, "");
            String parentId = savedInstanceState.getString(STATE_PARENT_ID, "");
            boolean active = savedInstanceState.getBoolean(STATE_ACTIVE, true);
            restoreCategoryDialog(id, name, parentId, active);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (binding == null) {
            return;
        }
        boolean online = NetworkMonitor.getInstance().isOnline();
        binding.viewOfflineBanner.getRoot().setVisibility(online ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (activeDialog != null && activeDialog.isShowing() && activeDialogBinding != null) {
            outState.putBoolean(STATE_DIALOG_OPEN, true);
            outState.putString(STATE_CATEGORY_ID, activeEditingId);
            Editable text = activeDialogBinding.editCategoryName.getText();
            outState.putString(STATE_CATEGORY_NAME, text != null ? text.toString() : "");
            outState.putBoolean(STATE_ACTIVE, activeDialogBinding.switchDialogActive.isChecked());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (activeDialog != null) {
            activeDialog.dismiss();
            activeDialog = null;
        }
        activeDialogBinding = null;
        binding = null;
    }

    private void setUpToolbar() {
        if (binding == null) {
            return;
        }
        binding.includeToolbar.textTitle.setText(R.string.admin_category_title);
        binding.includeToolbar.buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View clicked) {
                NavHostFragment.findNavController(AdminCategoryFragment.this).navigateUp();
            }
        });
    }

    private void setUpList() {
        if (binding == null) {
            return;
        }
        adapter = new AdminCategoryAdapter(new AdminCategoryAdapter.OnCategoryActionListener() {
            @Override
            public void onEdit(Category category) {
                showCategoryDialog(category, null, null, null);
            }

            @Override
            public void onDelete(Category category) {
                showDeleteConfirmDialog(category);
            }

            @Override
            public void onToggleActive(Category category, boolean active) {
                lastActionWasDelete = false;
                Category updated = new Category();
                updated.setId(category.getId());
                updated.setName(category.getName());
                updated.setParentId(category.getParentId());
                updated.setConditionFields(category.getConditionFields());
                updated.setActive(active);
                viewModel.saveCategory(updated);
            }
        });
        binding.recyclerCategories.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerCategories.setAdapter(adapter);

        binding.viewEmptyState.textEmptyTitle.setText(R.string.admin_category_empty_title);
        binding.viewEmptyState.textEmptyMessage.setText(R.string.admin_category_empty_subtitle);
        binding.viewEmptyState.buttonEmptyAction.setVisibility(View.GONE);

        binding.viewErrorState.buttonRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View clicked) {
                viewModel.loadCategories();
            }
        });
    }

    private void setUpAddButton() {
        if (binding == null) {
            return;
        }
        binding.buttonAddCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View clicked) {
                showCategoryDialog(null, null, null, null);
            }
        });
    }

    private void observeViewModel() {
        viewModel.getCategories().observe(getViewLifecycleOwner(), new Observer<List<Category>>() {
            @Override
            public void onChanged(List<Category> categories) {
                adapter.setData(categories);
                updateState();
            }
        });
        viewModel.getCategoryCounts().observe(getViewLifecycleOwner(), new Observer<Map<String, Integer>>() {
            @Override
            public void onChanged(Map<String, Integer> counts) {
                adapter.setCategoryCounts(counts);
            }
        });
        viewModel.getLoading().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean loading) {
                updateState();
            }
        });
        viewModel.getError().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String message) {
                showError(message);
            }
        });
        viewModel.getDone().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean done) {
                handleDone(done);
            }
        });
    }

    private void updateState() {
        if (binding == null) {
            return;
        }
        boolean loading = Boolean.TRUE.equals(viewModel.getLoading().getValue());
        List<Category> categories = viewModel.getCategories().getValue();
        boolean hasList = categories != null;
        boolean empty = hasList && categories.isEmpty();
        boolean failed = !hasList && !loading;

        binding.progressCategories.setVisibility(loading && !hasList ? View.VISIBLE : View.GONE);
        binding.recyclerCategories.setVisibility(hasList && !empty ? View.VISIBLE : View.GONE);
        binding.viewEmptyState.getRoot().setVisibility(empty && !loading ? View.VISIBLE : View.GONE);
        binding.viewErrorState.getRoot().setVisibility(failed ? View.VISIBLE : View.GONE);

        adapter.setActionsEnabled(!loading);
        binding.buttonAddCategory.setEnabled(!loading);
    }

    private void showError(String message) {
        if (message == null || message.trim().isEmpty() || binding == null) {
            return;
        }
        if (viewModel.getCategories().getValue() == null) {
            binding.viewErrorState.textErrorMessage.setText(message);
        } else {
            Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
        }
        viewModel.clearError();
    }

    private void handleDone(Boolean done) {
        if (!Boolean.TRUE.equals(done) || binding == null) {
            return;
        }
        if (activeDialog != null) {
            activeDialog.dismiss();
            activeDialog = null;
            activeDialogBinding = null;
        }
        int messageId = lastActionWasDelete ? R.string.admin_category_delete_success
                : R.string.admin_category_save_success;
        Snackbar.make(binding.getRoot(), messageId, Snackbar.LENGTH_SHORT).show();
        viewModel.clearDone();
    }

    private void restoreCategoryDialog(@Nullable String id, String name, String parentId, boolean active) {
        List<Category> all = viewModel.getCategories().getValue();
        Category existing = null;
        if (id != null && all != null) {
            for (Category c : all) {
                if (id.equals(c.getId())) {
                    existing = c;
                    break;
                }
            }
        }
        showCategoryDialog(existing, name, parentId, Boolean.valueOf(active));
    }

    private void showCategoryDialog(@Nullable final Category existing,
            @Nullable String overrideName,
            @Nullable String overrideParentId,
            @Nullable Boolean overrideActive) {
        if (activeDialog != null) {
            activeDialog.dismiss();
            activeDialog = null;
        }
        activeEditingId = existing != null ? existing.getId() : null;

        final DialogAdminCategoryBinding dialogBinding = DialogAdminCategoryBinding.inflate(getLayoutInflater());
        activeDialogBinding = dialogBinding;

        dialogBinding.textDialogTitle.setText(existing == null ? R.string.admin_category_add
                : R.string.admin_category_edit);

        String initialName = overrideName != null ? overrideName
                : (existing != null && existing.getName() != null ? existing.getName() : "");
        dialogBinding.editCategoryName.setText(initialName);

        boolean initialActive = overrideActive != null ? overrideActive.booleanValue()
                : (existing == null || existing.isActive());
        dialogBinding.switchDialogActive.setChecked(initialActive);

        final List<String> parentNames = new ArrayList<String>();
        final List<String> parentIds = new ArrayList<String>();
        parentNames.add(getString(R.string.admin_category_parent_root));
        parentIds.add("");

        List<Category> all = viewModel.getCategories().getValue();
        boolean hasChildren = false;
        if (all != null) {
            for (Category c : all) {
                if (c.isRoot()) {
                    if (existing != null && c.getId() != null && c.getId().equals(existing.getId())) {
                        continue;
                    }
                    parentNames.add(c.getName() != null ? c.getName() : "");
                    parentIds.add(c.getId() != null ? c.getId() : "");
                }
            }
            if (existing != null) {
                hasChildren = AdminCategoryRules.hasChildren(existing, all);
            }
        }

        ArrayAdapter<String> parentAdapter = new ArrayAdapter<String>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                parentNames
        );
        dialogBinding.spinnerParentCategory.setAdapter(parentAdapter);

        if (hasChildren) {
            dialogBinding.layoutParentCategory.setEnabled(false);
            dialogBinding.layoutParentCategory.setHelperText(getString(R.string.admin_category_parent_invalid));
        }

        String targetParentId = overrideParentId != null ? overrideParentId
                : (existing != null && existing.getParentId() != null ? existing.getParentId() : "");
        int selectedIndex = 0;
        int idx = parentIds.indexOf(targetParentId);
        if (idx >= 0) {
            selectedIndex = idx;
        }
        dialogBinding.spinnerParentCategory.setText(parentNames.get(selectedIndex), false);

        dialogBinding.buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activeDialog != null) {
                    activeDialog.dismiss();
                }
            }
        });

        dialogBinding.buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Editable text = dialogBinding.editCategoryName.getText();
                String name = text != null ? text.toString().trim() : "";
                if (!AdminCategoryRules.isValidName(name)) {
                    if (name.isEmpty()) {
                        dialogBinding.layoutCategoryName.setError(getString(R.string.admin_category_name_required));
                    } else {
                        dialogBinding.layoutCategoryName.setError(getString(R.string.admin_category_name_too_long));
                    }
                    return;
                }
                dialogBinding.layoutCategoryName.setError(null);

                String chosenText = dialogBinding.spinnerParentCategory.getText().toString();
                int chosenIndex = parentNames.indexOf(chosenText);
                String parentId = (chosenIndex >= 0 && chosenIndex < parentIds.size())
                        ? parentIds.get(chosenIndex) : "";

                boolean active = dialogBinding.switchDialogActive.isChecked();

                Category toSave = new Category();
                if (existing != null) {
                    toSave.setId(existing.getId());
                    toSave.setConditionFields(existing.getConditionFields());
                }
                toSave.setName(name);
                toSave.setParentId(parentId);
                toSave.setActive(active);

                lastActionWasDelete = false;
                viewModel.saveCategory(toSave);
            }
        });

        activeDialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogBinding.getRoot())
                .create();
        activeDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                activeDialog = null;
                activeDialogBinding = null;
                activeEditingId = null;
            }
        });
        activeDialog.show();
    }

    private void showDeleteConfirmDialog(final Category category) {
        if (activeDialog != null) {
            activeDialog.dismiss();
            activeDialog = null;
        }
        String catName = category.getName() != null ? category.getName() : "";
        activeDialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.admin_category_delete_confirm_title)
                .setMessage(getString(R.string.admin_category_delete_confirm_message, catName))
                .setNegativeButton(R.string.common_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(R.string.admin_category_delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        lastActionWasDelete = true;
                        viewModel.deleteCategory(category.getId());
                    }
                })
                .create();
        activeDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                activeDialog = null;
            }
        });
        activeDialog.show();
    }
}
