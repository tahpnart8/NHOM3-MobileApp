package com.nhom3.pubgapp.ui.trade;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.nhom3.pubgapp.R;

// Confirmation for steps that cannot be undone; the agree button works only after a short countdown.
public class CountdownConfirmDialogFragment extends DialogFragment {

    public static final String RESULT_KEY = "countdown_confirm_result";
    public static final String RESULT_ACTION = "action";
    public static final String ACTION_CANCEL_DEAL = "cancel_deal";
    public static final String ACTION_ACCEPT_TERMS = "accept_terms";

    static final int COUNTDOWN_SECONDS = 5;

    private static final String ARG_ACTION = "action";
    private static final String ARG_TITLE = "title";
    private static final String ARG_MESSAGE = "message";
    private static final long TICK_MILLIS = 1000L;

    private CountDownTimer timer;

    public static CountdownConfirmDialogFragment newInstance(String action, int titleRes, int messageRes) {
        Bundle args = new Bundle();
        args.putString(ARG_ACTION, action);
        args.putInt(ARG_TITLE, titleRes);
        args.putInt(ARG_MESSAGE, messageRes);
        CountdownConfirmDialogFragment fragment = new CountdownConfirmDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /** Whole seconds still to wait; a tick at 4.2 s left shows 5, so the label never jumps to 0 early. */
    static int secondsLeft(long millisUntilFinished) {
        if (millisUntilFinished <= 0L) {
            return 0;
        }
        return (int) ((millisUntilFinished + TICK_MILLIS - 1L) / TICK_MILLIS);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final Bundle args = requireArguments();
        return new MaterialAlertDialogBuilder(requireContext())
                .setTitle(args.getInt(ARG_TITLE))
                .setMessage(args.getInt(ARG_MESSAGE))
                .setNegativeButton(R.string.trade_countdown_back, null)
                .setPositiveButton(getString(R.string.trade_countdown_agree_wait, COUNTDOWN_SECONDS),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Bundle result = new Bundle();
                                result.putString(RESULT_ACTION, args.getString(ARG_ACTION, ""));
                                getParentFragmentManager().setFragmentResult(RESULT_KEY, result);
                            }
                        })
                .create();
    }

    @Override
    public void onStart() {
        super.onStart();
        AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog == null) {
            return;
        }
        startCountdown(dialog);
    }

    // The timer restarts on every start, so a rotation does not let the user skip the wait.
    private void startCountdown(final AlertDialog dialog) {
        final Button agree = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        agree.setEnabled(false);
        stopCountdown();
        timer = new CountDownTimer(COUNTDOWN_SECONDS * TICK_MILLIS, TICK_MILLIS) {
            @Override
            public void onTick(long millisUntilFinished) {
                agree.setText(getString(R.string.trade_countdown_agree_wait, secondsLeft(millisUntilFinished)));
            }

            @Override
            public void onFinish() {
                agree.setText(R.string.trade_countdown_agree);
                agree.setEnabled(true);
            }
        };
        timer.start();
    }

    private void stopCountdown() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        stopCountdown();
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        stopCountdown();
        super.onDismiss(dialog);
    }
}
