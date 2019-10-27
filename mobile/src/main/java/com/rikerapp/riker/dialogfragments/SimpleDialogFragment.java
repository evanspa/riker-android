package com.rikerapp.riker.dialogfragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.TextView;

import com.rikerapp.riker.HtmlTagHandler;
import com.rikerapp.riker.R;
import com.rikerapp.riker.Utils;

import java.util.List;

public final class SimpleDialogFragment extends DialogFragment {

    private static final String ARG_REQUEST_CODE = "ARG_REQUEST_CODE";
    private static final String ARG_HEADER_TEXT = "ARG_HEADER_TEXT";
    private static final String ARG_HEADER_COLOR = "ARG_HEADER_COLOR";
    private static final String ARG_MESSAGE_TEXT = "ARG_MESSAGE_TEXT";
    private static final String ARG_POSITIVE_BUTTON_TEXT = "ARG_POSITIVE_BUTTON_TEXT";
    private static final String ARG_NEGATIVE_BUTTON_TEXT = "ARG_NEGATIVE_BUTTON_TEXT";
    private static final String ARG_NEUTRAL_BUTTON_TEXT = "ARG_NEUTRAL_BUTTON_TEXT";

    private Integer requestCode;
    private Callbacks listener;
    private String negativeButtonText;

    private static SimpleDialogFragment newInstance(final Integer requestCode,
                                                    final String headerText,
                                                    final @ColorRes int headerColorRes,
                                                    final String messageText,
                                                    final String positiveButtonText,
                                                    final String negativeButtonText,
                                                    final String neutralButtonText) {
        final SimpleDialogFragment simpleDialogFragment = new SimpleDialogFragment();
        final Bundle args = new Bundle();
        if (requestCode != null) {
            args.putString(ARG_REQUEST_CODE, requestCode.toString());
        }
        args.putString(ARG_HEADER_TEXT, headerText);
        args.putInt(ARG_HEADER_COLOR, headerColorRes);
        args.putString(ARG_MESSAGE_TEXT, messageText);
        if (positiveButtonText != null) {
            args.putString(ARG_POSITIVE_BUTTON_TEXT, positiveButtonText);
        }
        if (negativeButtonText != null) {
            args.putString(ARG_NEGATIVE_BUTTON_TEXT, negativeButtonText);
        }
        if (neutralButtonText != null) {
            args.putString(ARG_NEUTRAL_BUTTON_TEXT, neutralButtonText);
        }
        simpleDialogFragment.setArguments(args);
        return simpleDialogFragment;
    }

    public static SimpleDialogFragment infoInstance(final Integer requestCode,
                                                    final String headerText,
                                                    final String messageText) {
        return newInstance(requestCode, headerText, R.color.infoDialogHeader, messageText, null, null, null);
    }

    public static SimpleDialogFragment warningInstance(final Integer requestCode,
                                                       final String headerText,
                                                       final String messageText) {
        return newInstance(requestCode, headerText, R.color.sunflower, messageText, null, null, null);
    }

    public static SimpleDialogFragment errorInstance(final Integer requestCode,
                                                     final String headerText,
                                                     final String messageText) {
        return newInstance(requestCode, headerText, R.color.rikerRed, messageText, null, null, null);
    }

    public static SimpleDialogFragment successInstance(final Integer requestCode,
                                                       final String headerText,
                                                       final String messageText) {
        return newInstance(requestCode, headerText, R.color.emerlandGreen, messageText, null, null, null);
    }

    public static SimpleDialogFragment validationErrorsInstance(final Integer requestCode,
                                                                final List<String> errorMessagesList) {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<ul>");
        for (final String errorMessage : errorMessagesList) {
            stringBuilder.append(String.format("<li>&nbsp;&nbsp;%s</li>", errorMessage));
        }
        stringBuilder.append("</ul>");
        return warningInstance(requestCode, "Oops", stringBuilder.toString());
    }

    public static SimpleDialogFragment confirmInstance(final Integer requestCode,
                                                       final String headerText,
                                                       final String messageText,
                                                       final String confirmButtonText,
                                                       final String cancelButtonText) {
        return confirmInstance(requestCode,
                headerText,
                messageText,
                confirmButtonText,
                null,
                cancelButtonText);
    }

    public static SimpleDialogFragment confirmInstance(final Integer requestCode,
                                                       final String headerText,
                                                       final String messageText,
                                                       final String confirmButtonText,
                                                       final String neutralButtonText,
                                                       final String cancelButtonText) {
        return newInstance(requestCode,
                headerText,
                R.color.infoDialogHeader,
                messageText,
                confirmButtonText,
                cancelButtonText,
                neutralButtonText);
    }

    @Override
    public final Dialog onCreateDialog(final Bundle savedInstanceState) {
        final Bundle args = getArguments();
        final String requestCodeStr = args.getString(ARG_REQUEST_CODE);
        if (requestCodeStr != null) {
            this.requestCode = new Integer(requestCodeStr);
        }
        final String headerText = args.getString(ARG_HEADER_TEXT);
        final @ColorRes int headerColorRes = args.getInt(ARG_HEADER_COLOR);
        final String messageText = args.getString(ARG_MESSAGE_TEXT);
        final Activity activity = getActivity();
        final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(activity);
        final View customTitleView = activity.getLayoutInflater().inflate(R.layout.dialog_title_header, null);
        customTitleView.setBackgroundColor(ContextCompat.getColor(activity, headerColorRes));
        final TextView titleView = customTitleView.findViewById(R.id.titleTextView);
        titleView.setText(headerText);
        alertBuilder.setCustomTitle(customTitleView);
        if (messageText != null) {
            alertBuilder.setMessage(Utils.fromHtml(messageText, new HtmlTagHandler()));
        }
        final String positiveButtonText = args.getString(ARG_POSITIVE_BUTTON_TEXT, "Ok");
        alertBuilder.setPositiveButton(positiveButtonText,
                ((dialog, which) -> {
                    if (requestCode != null && listener != null) {
                        listener.dialogPositiveClicked(requestCode);
                    }
                }));
        final String neutralButtonText = args.getString(ARG_NEUTRAL_BUTTON_TEXT);
        if (neutralButtonText != null) {
            alertBuilder.setNeutralButton(neutralButtonText,
                    ((dialog, which) -> {
                        if (requestCode != null && listener != null) {
                            listener.dialogNeutralClicked(requestCode);
                        }
                    }));
        }
        this.negativeButtonText = args.getString(ARG_NEGATIVE_BUTTON_TEXT);
        if (negativeButtonText != null) {
            alertBuilder.setNegativeButton(negativeButtonText, ((dialog, which) -> {
                if (requestCode != null && listener != null) {
                    listener.dialogNegativeClicked(requestCode);
                }
            }));
        }
        final Dialog dialog = alertBuilder.create();
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    @Override
    public final void onCancel(final DialogInterface dialog) {
        if (requestCode != null) {
            if (this.negativeButtonText != null) {
                if (listener != null) {
                    listener.dialogNegativeClicked(requestCode);
                }
            } else {
                if (listener != null) {
                    listener.dialogPositiveClicked(requestCode);
                }
            }
        }
        super.onCancel(dialog);
    }

    @Override
    public final void onAttach(final Context context) {
        super.onAttach(context);
        if (context instanceof Callbacks) {
            this.listener = (Callbacks)context;
        }
    }

    @Override
    public final void onDetach() {
        this.listener = null;
        super.onDetach();
    }

    public interface Callbacks {
        void dialogPositiveClicked(final int requestCode);
        void dialogNegativeClicked(final int requestCode);
        void dialogNeutralClicked(final int requestCode);
    }
}
