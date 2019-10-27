package com.rikerapp.riker.activities;

import android.os.Bundle;
import android.widget.Button;

import com.rikerapp.riker.Constants;
import com.rikerapp.riker.R;
import com.rikerapp.riker.Utils;

public final class LegalActivity extends BaseActivity {

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_legal);
        configureAppBar();
        logScreen(getTitle());
        final Button termsOfServiceButton = (Button)findViewById(R.id.termsOfServiceButton);
        termsOfServiceButton.setOnClickListener(view -> Utils.startWebViewActivity(this,
                getResources().getString(R.string.title_activity_tos),
                Constants.RIKER_URI_BARENAV_TERMS_OF_SERVICE));
        final Button privacyPolicyButton = (Button)findViewById(R.id.privacyPolicyButton);
        privacyPolicyButton.setOnClickListener(view -> Utils.startWebViewActivity(this,
                getResources().getString(R.string.title_activity_privacy_policy),
                Constants.RIKER_URI_BARENAV_PRIVACY_POLICY));
        final Button securityPolicyButton = (Button)findViewById(R.id.securityPolicyButton);
        securityPolicyButton.setOnClickListener(view -> Utils.startWebViewActivity(this,
                getResources().getString(R.string.title_activity_security_policy),
                Constants.RIKER_URI_BARENAV_SECURITY_POLICY));
    }
}
