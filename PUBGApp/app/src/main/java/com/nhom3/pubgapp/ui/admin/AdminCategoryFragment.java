package com.nhom3.pubgapp.ui.admin;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.databinding.DialogAdminCategoryBinding;
import com.nhom3.pubgapp.databinding.DialogAdminCategoryFieldsBinding;
import com.nhom3.pubgapp.databinding.FragmentAdminCategoryBinding;
import com.nhom3.pubgapp.model.Category;
import com.nhom3.pubgapp.util.NetworkMonitor;
import com.nhom3.pubgapp.viewmodel.AdminContentViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** Screen E05: Category management tree, active switches, and add/edit/delete dialogs. */
public class AdminCategoryFragment extends Fragment {

    private static final String STATE_DIALOG_OPEN = "state_dialog_open";
    private static final String STATE_CATEGORY_ID = "state_category_id";
    private static final String STATE_CATEGORY_NAME = "state_category_name";
    private static final String STATE_PARENT_ID = "state_parent_id";
    private static final String STATE_ACTIVE = "state_active";
    private static final String STATE_FIELDS_DIALOG_OPEN = "state_fields_dialog_open";
    private static final String STATE_FIELDS_CATEGORY_ID = "state_fields_category_id";
    private static final String STATE_FIELDS_LIST = "state_fields_list";
    private static final String STATE_FIELDS_INPUT = "state_fields_input";

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
    private AlertDialog activeFieldsDialog;
    @Nullable
    private DialogAdminCategoryFieldsBinding activeFieldsDialogBinding;
    @Nullable
    private String activeFieldsCategoryId;
    private final ArrayList<String> activeFieldsList = new ArrayList<String>();
    private boolean lastActionWasFields;

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

        if (savedInstanceState != null && savedInstanceState.getBoolean(STATE_FIELDS_DIALOG_OPEN, false)) {
            String catId = savedInstanceState.getString(STATE_FIELDS_CATEGORY_ID);
            ArrayList<String> list = savedInstanceState.getStringArrayList(STATE_FIELDS_LIST);
            String input = savedInstanceState.getString(STATE_FIELDS_INPUT, "");
            restoreFieldsDialog(catId, list, input);
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
        if (activeFieldsDialogBinding != null) {
            boolean canAdd = online && activeFieldsList.size() < 30;
            activeFieldsDialogBinding.buttonAddField.setEnabled(canAdd);
            activeFieldsDialogBinding.buttonSaveFields.setEnabled(online);
        }
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
        if (activeFieldsDialog != null && activeFieldsDialog.isShowing() && activeFieldsDialogBinding != null) {
            outState.putBoolean(STATE_FIELDS_DIALOG_OPEN, true);
            outState.putString(STATE_FIELDS_CATEGORY_ID, activeFieldsCategoryId);
            outState.putStringArrayList(STATE_FIELDS_LIST, activeFieldsList);
            Editable text = activeFieldsDialogBinding.editNewField.getText();
            outState.putString(STATE_FIELDS_INPUT, text != null ? text.toString() : "");
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
        if (activeFieldsDialog != null) {
            activeFieldsDialog.dismiss();
            activeFieldsDialog = null;
        }
        activeFieldsDialogBinding = null;
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
        binding.recyclerCategories.addOnChildAttachStateChangeListener(
                new RecyclerView.OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(@NonNull View childView) {
                childView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (binding == null) {
                            return;
                        }
                        int position = binding.recyclerCategories.getChildAdapterPosition(v);
                        if (position != RecyclerView.NO_POSITION) {
                            List<Category> all = viewModel.getCategories().getValue();
                            List<Category> tree = AdminCategoryRules.buildTreeList(all);
                            if (position >= 0 && position < tree.size()) {
                                showConditionFieldsDialog(tree.get(position), null, null);
                            }
                        }
                    }
                });
            }

            @Override
            public void onChildViewDetachedFromWindow(@NonNull View childView) {
                childView.setOnClickListener(null);
            }
        });

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
        if (activeFieldsDialog != null) {
            activeFieldsDialog.dismiss();
            activeFieldsDialog = null;
            activeFieldsDialogBinding = null;
            activeFieldsCategoryId = null;
        }
        int messageId;
        if (lastActionWasDelete) {
            messageId = R.string.admin_category_delete_success;
        } else if (lastActionWasFields) {
            messageId = R.string.admin_category_fields_save_success;
        } else {
            messageId = R.string.admin_category_save_success;
        }
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
                if (isNameTaken(name, existing)) {
                    dialogBinding.layoutCategoryName.setError(getString(R.string.admin_category_name_taken));
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

    // UC39 exception 2b: two categories with the same name confuse sellers, so the name must be new.
    private boolean isNameTaken(String name, @Nullable Category existing) {
        List<Category> all = viewModel.getCategories().getValue();
        if (all == null) {
            return false;
        }
        for (Category c : all) {
            boolean same = existing != null && c.getId() != null && c.getId().equals(existing.getId());
            if (!same && c.getName() != null && c.getName().trim().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
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

    private void restoreFieldsDialog(@Nullable String id, @Nullable ArrayList<String> list,
            @Nullable String input) {
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
        if (existing != null) {
            showConditionFieldsDialog(existing, list, input);
        }
    }

    private void showConditionFieldsDialog(@NonNull final Category category,
            @Nullable ArrayList<String> overrideList,
            @Nullable String overrideInput) {
        if (activeFieldsDialog != null) {
            activeFieldsDialog.dismiss();
            activeFieldsDialog = null;
        }
        activeFieldsCategoryId = category.getId();
        activeFieldsList.clear();
        if (overrideList != null) {
            activeFieldsList.addAll(overrideList);
        } else if (category.getConditionFields() != null) {
            activeFieldsList.addAll(category.getConditionFields());
        }

        final DialogAdminCategoryFieldsBinding dialogBinding =
                DialogAdminCategoryFieldsBinding.inflate(getLayoutInflater());
        activeFieldsDialogBinding = dialogBinding;

        String catName = category.getName() != null ? category.getName() : "";
        dialogBinding.textFieldsTitle.setText(getString(R.string.admin_category_fields_dialog_title, catName));

        if (overrideInput != null) {
            dialogBinding.editNewField.setText(overrideInput);
        }

        renderFieldsList(dialogBinding);

        dialogBinding.buttonAddField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Editable text = dialogBinding.editNewField.getText();
                String name = text != null ? text.toString().trim() : "";
                if (name.isEmpty()) {
                    dialogBinding.layoutNewField.setError(getString(R.string.admin_category_fields_name_empty));
                    return;
                }
                if (name.length() > 50) {
                    dialogBinding.layoutNewField.setError(getString(R.string.admin_category_fields_name_too_long));
                    return;
                }
                if (activeFieldsList.size() >= 30) {
                    dialogBinding.layoutNewField.setError(getString(R.string.admin_category_fields_limit_reached));
                    return;
                }
                for (String existing : activeFieldsList) {
                    if (name.equalsIgnoreCase(existing.trim())) {
                        dialogBinding.layoutNewField.setError(getString(R.string.admin_category_fields_name_duplicate));
                        return;
                    }
                }
                dialogBinding.layoutNewField.setError(null);
                activeFieldsList.add(name);
                dialogBinding.editNewField.setText("");
                renderFieldsList(dialogBinding);
            }
        });

        dialogBinding.buttonCancelFields.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activeFieldsDialog != null) {
                    activeFieldsDialog.dismiss();
                }
            }
        });

        dialogBinding.buttonSaveFields.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!NetworkMonitor.getInstance().isOnline()) {
                    Snackbar.make(binding != null ? binding.getRoot() : dialogBinding.getRoot(),
                            R.string.admin_category_fields_offline_warning, Snackbar.LENGTH_LONG).show();
                    return;
                }
                lastActionWasDelete = false;
                lastActionWasFields = true;
                viewModel.setConditionFields(category.getId(), new ArrayList<String>(activeFieldsList));
            }
        });

        activeFieldsDialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogBinding.getRoot())
                .create();
        activeFieldsDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                activeFieldsDialog = null;
                activeFieldsDialogBinding = null;
                activeFieldsCategoryId = null;
            }
        });
        activeFieldsDialog.show();
    }

    private void renderFieldsList(final DialogAdminCategoryFieldsBinding dialogBinding) {
        if (getContext() == null) {
            return;
        }
        dialogBinding.layoutFieldsContainer.removeAllViews();
        int count = activeFieldsList.size();
        dialogBinding.textFieldsCount.setText(getString(R.string.admin_category_fields_count, count));

        boolean online = NetworkMonitor.getInstance().isOnline();
        dialogBinding.buttonAddField.setEnabled(online && count < 30);
        dialogBinding.buttonSaveFields.setEnabled(online);

        if (count == 0) {
            dialogBinding.textFieldsEmpty.setVisibility(View.VISIBLE);
            dialogBinding.scrollFields.setVisibility(View.GONE);
            return;
        }

        dialogBinding.textFieldsEmpty.setVisibility(View.GONE);
        dialogBinding.scrollFields.setVisibility(View.VISIBLE);

        int padV = getResources().getDimensionPixelSize(R.dimen.space_xs);
        int padH = getResources().getDimensionPixelSize(R.dimen.space_xs);
        int touchMin = getResources().getDimensionPixelSize(R.dimen.touch_min);

        for (int i = 0; i < activeFieldsList.size(); i++) {
            final int index = i;
            final String fieldName = activeFieldsList.get(i);

            LinearLayout row = new LinearLayout(requireContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(padH, padV, padH, padV);

            TextView text = new TextView(requireContext());
            LinearLayout.LayoutParams textLp = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
            text.setLayoutParams(textLp);
            text.setText(fieldName);
            text.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyMedium);
            row.addView(text);

            MaterialButton btnUp = new MaterialButton(requireContext(), null,
                    android.R.attr.borderlessButtonStyle);
            btnUp.setText(R.string.admin_category_fields_item_up);
            btnUp.setMinimumHeight(touchMin);
            btnUp.setEnabled(index > 0);
            btnUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Collections.swap(activeFieldsList, index, index - 1);
                    renderFieldsList(dialogBinding);
                }
            });
            row.addView(btnUp);

            MaterialButton btnDown = new MaterialButton(requireContext(), null,
                    android.R.attr.borderlessButtonStyle);
            btnDown.setText(R.string.admin_category_fields_item_down);
            btnDown.setMinimumHeight(touchMin);
            btnDown.setEnabled(index < activeFieldsList.size() - 1);
            btnDown.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Collections.swap(activeFieldsList, index, index + 1);
                    renderFieldsList(dialogBinding);
                }
            });
            row.addView(btnDown);

            MaterialButton btnDelete = new MaterialButton(requireContext(), null,
                    android.R.attr.borderlessButtonStyle);
            btnDelete.setText(R.string.admin_category_fields_item_delete);
            btnDelete.setMinimumHeight(touchMin);
            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activeFieldsList.remove(index);
                    renderFieldsList(dialogBinding);
                }
            });
            row.addView(btnDelete);

            dialogBinding.layoutFieldsContainer.addView(row);
        }
    }
}
