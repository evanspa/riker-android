package com.rikerapp.riker.dialogfragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public final class ProgressDialogFragment extends DialogFragment {

    private static final String ARG_MESSAGE = "ARG_MESSAGE";

    public static ProgressDialogFragment newInstance(final String messageText) {
        final ProgressDialogFragment progressDialogFragment = new ProgressDialogFragment();
        final Bundle args = new Bundle();
        args.putString(ARG_MESSAGE, messageText);
        progressDialogFragment.setArguments(args);
        return progressDialogFragment;
    }

    @Override
    public final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
    }

    @Override
    public final Dialog onCreateDialog(final Bundle savedInstanceState) {
        final ProgressDialog dialog = new ProgressDialog(getActivity());
        final Bundle args = getArguments();
        dialog.setMessage(args.getString(ARG_MESSAGE));
        dialog.setIndeterminate(true);
        dialog.setCancelable(true);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        return dialog;
    }
}
