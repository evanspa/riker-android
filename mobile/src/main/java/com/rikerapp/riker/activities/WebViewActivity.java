package com.rikerapp.riker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.rikerapp.riker.BuildConfig;
import com.rikerapp.riker.R;
import com.rikerapp.riker.Utils;

public final class WebViewActivity extends BaseActivity {

    public static final String INTENTDATA_TITLE = "INTENTDATA_TITLE";
    public static final String INTENTDATA_RIKER_URI = "INTENTDATA_RIKER_URI";

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        configureAppBar();
        Utils.displayProgressDialog(this, "Loading...");
        final Intent intent = getIntent();
        final String title = intent.getStringExtra(INTENTDATA_TITLE);
        setTitle(title);
        logScreen(title);
        final WebView webView = (WebView)findViewById(R.id.webView);
        final WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webView.setWebChromeClient(new WebChromeClient() {
            public final void onProgressChanged(final WebView view, final int progress) {
                if (progress == 100) {
                    Utils.dismissProgressDialog(WebViewActivity.this);
                }
            }
        });
        webView.loadUrl(String.format("%s/%s", BuildConfig.rikerUriPrefix, intent.getStringExtra(INTENTDATA_RIKER_URI)));
    }
}
