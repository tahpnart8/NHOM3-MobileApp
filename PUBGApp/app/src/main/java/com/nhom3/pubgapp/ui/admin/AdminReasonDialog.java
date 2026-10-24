package com.nhom3.pubgapp.ui.admin;

import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.databinding.DialogAdminReasonBinding;

// The confirm dialog of E04 and its detail screen. The Fragment owns one and calls release() in onDestroyView.
final class AdminReasonDialog {

    interface OnConfirmListener {
        void onConfirm(String reason);
    }

    private final Context context;
    private final LayoutInflater inflater;
    private final OnConfirmListener listener;
    @Nullable
    private AlertDialog dialog;
    @Nullable
    private DialogAdminReasonBinding content;
    private boolean needsReason;

    AdminReasonDialog(Context context, LayoutInflater inflater, OnConfirmListener listener) {
        this.context = context;
        this.inflater = inflater;
        this.listener = listener;
    }

    boolean isOpen() {
        return dialog != null;
    }

    void show(@StringRes int title, @StringRes int message, boolean needsReason, String text) {
        close();
        this.needsReason = needsReason;
        final DialogAdminReasonBinding views = DialogAdminReasonBinding.inflate(inflater);
        content = views;
        views.textDialogTitle.setText(title);
        views.textDialogMessage.setText(message);
        views.inputReason.setVisibility(needsReason ? View.VISIBLE : View.GONE);
        views.editReason.setText(text);
        views.buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                close();
            }
        });
        views.buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirm();
            }
        });
        dialog = new MaterialAlertDialogBuilder(context).setView(views.getRoot()).create();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface closed) {
                dialog = null;
                content = null;
            }
        });
        dialog.show();
    }

    String readText() {
        if (content == null) {
            return "";
        }
        Editable typed = content.editReason.getText();
        return typed == null ? "" : typed.toString().trim();
    }

    void showError(String message) {
        if (content == null) {
            return;
        }
        if (needsReason) {
            content.inputReason.setError(message);
        } else {
            content.textDialogMessage.setText(message);
        }
    }

    void setConfirmEnabled(boolean enabled) {
        if (content != null) {
            content.buttonConfirm.setEnabled(enabled);
        }
    }

    void close() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    // Used when the view is destroyed: closes the window without forgetting that the dialog was open.
    void release() {
        if (dialog != null) {
            dialog.setOnDismissListener(null);
            dialog.dismiss();
        }
        dialog = null;
        content = null;
    }

    private void confirm() {
        String reason = readText();
        if (needsReason && reason.isEmpty()) {
            showError(context.getString(R.string.admin_reason_required));
            return;
        }
        if (content != null) {
            content.inputReason.setError(null);
        }
        listener.onConfirm(reason);
    }
}
