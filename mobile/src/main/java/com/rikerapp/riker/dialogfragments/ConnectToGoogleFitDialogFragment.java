package com.rikerapp.riker.dialogfragments;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.rikerapp.riker.R;
import com.rikerapp.riker.Utils;
import com.rikerapp.riker.eventbus.AppEvent;

import org.greenrobot.eventbus.EventBus;

public final class ConnectToGoogleFitDialogFragment extends DialogFragment {

    @Override
    public final View onCreateView(final LayoutInflater inflater,
                                   final ViewGroup container,
                                   final Bundle savedInstanceState) {
        final Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.setCanceledOnTouchOutside(true);
        }
        final View fragmentView = inflater.inflate(R.layout.dialogfragment_prompt_connect_to_google_fit, container, false);
        final TextView googleFitDialogPromptTextView = fragmentView.findViewById(R.id.googleFitDialogPromptTextView);
        googleFitDialogPromptTextView.setText(Utils.fromHtml(getResources().getString(R.string.google_fit_prompt)));
        final Button googleFitConnectButton = fragmentView.findViewById(R.id.googleFitConnectButton);
        googleFitConnectButton.setOnClickListener(view -> {
            dismiss();
            EventBus.getDefault().post(new AppEvent.ConnectToGoogleFit());
        });
        final Button googleFitCancelButton = fragmentView.findViewById(R.id.googleFitCancelButton);
        googleFitCancelButton.setOnClickListener(view -> {
            dismiss();
            EventBus.getDefault().post(new AppEvent.CancelConnectToGoogleFit());
        });
        return fragmentView;
    }
}
