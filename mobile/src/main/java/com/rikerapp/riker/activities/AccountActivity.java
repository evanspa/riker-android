package com.rikerapp.riker.activities;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.rikerapp.riker.R;
import com.rikerapp.riker.dialogfragments.SimpleDialogFragment;

public final class AccountActivity extends BaseActivity implements SimpleDialogFragment.Callbacks {

    // dialog request codes
    private static final int DIALOG_REQUESTCODE_RIKER_ACCOUNT_BENEFITS = 1;
    private static final int DIALOG_REQUESTCODE_RIKER_WITHOUT_AN_ACCOUNT = 2;

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EventBus.getDefault().register(this);
        setContentView(R.layout.activity_account);
        configureAppBar();
        logScreen(getTitle());

        //final Button loginButton = findViewById(R.id.loginButton);
        final Button freeTrialButton = findViewById(R.id.freeTrialButton);
        freeTrialButton.setOnClickListener(view -> startActivityForResult(SignUpActivity.makeIntent(this), REQUEST_CODE_SIGNUP));
        final TextView rikerAccountBenefitsTextView = findViewById(R.id.rikerAccountBenefitsTextView);
        final Resources resources = getResources();
        rikerAccountBenefitsTextView.setOnClickListener(view -> {
            final SimpleDialogFragment simpleDialogFragment = SimpleDialogFragment.infoInstance(
                    DIALOG_REQUESTCODE_RIKER_ACCOUNT_BENEFITS,
                    resources.getString(R.string.riker_account_benefits_title),
                    resources.getString(R.string.riker_account_benefits_msg));
            showDialog(simpleDialogFragment, "dialog_fragment_riker_account_benefits_info");
        });
        final TextView afterTheTrialTextView = findViewById(R.id.afterTheTrialTextView);
        afterTheTrialTextView.setOnClickListener(view -> startActivity(new Intent(this, AfterTheTrialInfoActivity.class)));
        final TextView useWithoutAccountTextView = findViewById(R.id.useWithoutAccountTextView);
        useWithoutAccountTextView.setOnClickListener(view -> {
            final SimpleDialogFragment simpleDialogFragment = SimpleDialogFragment.infoInstance(
                    DIALOG_REQUESTCODE_RIKER_WITHOUT_AN_ACCOUNT,
                    resources.getString(R.string.use_without_account_title),
                    resources.getString(R.string.use_without_account_msg));
            showDialog(simpleDialogFragment, "dialog_fragment_riker_without_an_account_info");
        });
    }

    private final void handleSignUpSuccessful() {

    }

    private final void handleSignUpResult(final int resultCode, final Intent data) {
        switch (resultCode) {
            case SignUpActivity.RESULT_CODE_SIGN_UP_SUCCESSFUL:
                handleSignUpSuccessful();
                break;
        }
    }

    private final void handleLoginResult(final int resultCode, final Intent data) {

    }

    @Override
    protected final void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_SIGNUP:
                handleSignUpResult(resultCode, data);
                break;
            case REQUEST_CODE_LOGIN:
                handleLoginResult(resultCode, data);
                break;
        }
    }

    @Override
    public final void dialogPositiveClicked(final int requestCode) {
        switch (requestCode) {
            case DIALOG_REQUESTCODE_RIKER_ACCOUNT_BENEFITS:
                logHelpInfoPopupContentViewed("riker_account_benefits");
                break;
        }
    }
}
