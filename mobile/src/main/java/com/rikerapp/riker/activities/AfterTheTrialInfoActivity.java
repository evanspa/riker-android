package com.rikerapp.riker.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.rikerapp.riker.R;
import com.rikerapp.riker.Utils;
import com.rikerapp.riker.dialogfragments.SimpleDialogFragment;

import org.apache.commons.lang3.StringUtils;

public final class AfterTheTrialInfoActivity extends BaseActivity implements /*PurchasesUpdatedListener,*/ SimpleDialogFragment.Callbacks {

    // dialog request codes
    private static final int DIALOG_REQUESTCODE_UNABLE_TO_LOAD_SKU_DETAILS = 1;
    private static final int DIALOG_REQUESTCODE_GOOGLE_PLAY_PROBLEM = 2;

    // local state keys
    private static final String LSTATE_SUBSCRIPTION_PRICE = "LSTATE_SUBSCRIPTION_PRICE";

    private String subscriptionPrice;

    private ProgressBar progressBar;
    private TextView headingTextView;
    private Button enrollInSubscriptionInfoButton;
    private TextView enrollInSubscriptionInfoTextView;
    private Button useRikerAppExclusivelyInfoButton;
    private TextView useRikerAppExclusivelyInfoTextView;

    //private BillingClient billingClient;

    @Override
    public final void onSaveInstanceState(final Bundle outState) {
        if (this.subscriptionPrice != null) {
            outState.putString(LSTATE_SUBSCRIPTION_PRICE, subscriptionPrice);
        }
        super.onSaveInstanceState(outState);
    }

    private final void handleBillingSetupOk() {
        /*final List skuList = new ArrayList<>();
        skuList.add(BuildConfig.rikerBasicSubscriptionName);
        final SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(BillingClient.SkuType.SUBS);
        billingClient.querySkuDetailsAsync(params.build(),
                new SkuDetailsResponseListener() {
                    @Override
                    public final void onSkuDetailsResponse(@BillingClient.BillingResponse int billingResponseCode, final List<SkuDetails> skuDetailsList) {
                        switch (billingResponseCode) {
                            case BillingClient.BillingResponse.OK:
                                handleSkuDetailsOk(skuDetailsList);
                                break;
                            case BillingClient.BillingResponse.SERVICE_UNAVAILABLE:
                                indicateNetworkProblem();
                                break;
                            case BillingClient.BillingResponse.FEATURE_NOT_SUPPORTED:
                            case BillingClient.BillingResponse.BILLING_UNAVAILABLE:
                                indicateProblemWithGooglePlayInstalled();
                                break;
                            case BillingClient.BillingResponse.ERROR:
                                indicateProblemLoadingSubscriptionInfo();
                                break;
                        }
                    }
                });*/
    }

    /*private final void handleSkuDetailsOk(final List<SkuDetails> skuDetailsList) {
        progressBar.setVisibility(View.GONE);
        if (skuDetailsList != null && skuDetailsList.size() > 0) {
            final SkuDetails skuDetails = skuDetailsList.get(0);
            subscriptionPrice = skuDetails.getPrice();
            setSubscriptionInfo();
            useRikerAppExclusivelyInfoButton.setEnabled(true);
            headingTextView.setText(getResources().getString(R.string.after_the_trial_heading));
            enrollInSubscriptionInfoButton.setEnabled(true);
        } else {
            indicateProblemLoadingSubscriptionInfo();
        }
    }*/

    private final void indicateNetworkProblem() {
        progressBar.setVisibility(View.GONE);
        final SimpleDialogFragment simpleDialogFragment = SimpleDialogFragment.warningInstance(
                DIALOG_REQUESTCODE_NETWORK_PROBLEM,
                "Oops",
                "<p>There was a problem trying to load Riker's subscription information from Google Play at this time.</p><p>Please check your internet connection and try again.</p>");
        showDialog(simpleDialogFragment, "dialog_fragment_network_problem");
    }

    private final void indicateProblemWithGooglePlayInstalled() {
        progressBar.setVisibility(View.GONE);
        final SimpleDialogFragment simpleDialogFragment = SimpleDialogFragment.warningInstance(
                DIALOG_REQUESTCODE_GOOGLE_PLAY_PROBLEM,
                "Oops",
                "<p>There was a problem trying to load Riker's subscription information from Google Play.</p><p>Looks like there's a problem with the version of Google Play installed on your device.</p><p>Try upgrading the Google Play app and try again.</p>");
        showDialog(simpleDialogFragment, "dialog_fragment_google_play_problem");
    }

    private final void indicateProblemLoadingSubscriptionInfo() {
        progressBar.setVisibility(View.GONE);
        final SimpleDialogFragment simpleDialogFragment = SimpleDialogFragment.warningInstance(
                DIALOG_REQUESTCODE_UNABLE_TO_LOAD_SKU_DETAILS,
                "Oops",
                "<p>There was a problem trying to load Riker's subscription information from Google Play at this time.</p><p>Please try again a bit later.</p>");
        showDialog(simpleDialogFragment, "dialog_fragment_unable_to_load_subscription_details");
    }

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_after_the_trial);
        configureAppBar();
        logScreen(getTitle());
        this.progressBar = findViewById(R.id.progressBar);
        this.headingTextView = findViewById(R.id.headingTextView);
        this.enrollInSubscriptionInfoButton = findViewById(R.id.enrollInSubscriptionInfoButton);
        this.enrollInSubscriptionInfoTextView = findViewById(R.id.enrollInSubscriptionInfoTextView);
        this.useRikerAppExclusivelyInfoButton = findViewById(R.id.useRikerAppExclusivelyInfoButton);
        this.useRikerAppExclusivelyInfoTextView = findViewById(R.id.useRikerAppExclusivelyInfoTextView);
        this.useRikerAppExclusivelyInfoTextView.setText(Utils.fromHtml(getResources().getString(R.string.after_the_trial_use_riker_exclusively_msg)));
        this.enrollInSubscriptionInfoButton.setOnClickListener(view -> Utils.toggleVisibility(enrollInSubscriptionInfoTextView));
        this.useRikerAppExclusivelyInfoButton.setOnClickListener(view -> Utils.toggleVisibility(useRikerAppExclusivelyInfoTextView));
        if (savedInstanceState != null) {
            subscriptionPrice = savedInstanceState.getString(LSTATE_SUBSCRIPTION_PRICE);
        }
        if (subscriptionPrice == null) {
            progressBar.setVisibility(View.VISIBLE);
            enrollInSubscriptionInfoButton.setEnabled(false);
            useRikerAppExclusivelyInfoButton.setEnabled(false);
            /*billingClient = BillingClient.newBuilder(this).setListener(this).build();
            billingClient.startConnection(new BillingClientStateListener() {
                @Override
                public void onBillingSetupFinished(@BillingClient.BillingResponse int billingResponseCode) {
                    switch (billingResponseCode) {
                        case BillingClient.BillingResponse.OK:
                            handleBillingSetupOk();
                            break;
                        case BillingClient.BillingResponse.FEATURE_NOT_SUPPORTED:
                        case BillingClient.BillingResponse.BILLING_UNAVAILABLE:
                            indicateProblemWithGooglePlayInstalled();
                            break;
                        case BillingClient.BillingResponse.SERVICE_UNAVAILABLE:
                            indicateNetworkProblem();
                            break;
                        case BillingClient.BillingResponse.ERROR:
                            indicateProblemLoadingSubscriptionInfo();
                            break;
                    }
                }
                @Override
                public final void onBillingServiceDisconnected() {
                    indicateProblemLoadingSubscriptionInfo();
                }
            });*/
        } else {
            progressBar.setVisibility(View.GONE);
            headingTextView.setText(getResources().getString(R.string.after_the_trial_heading));
            enrollInSubscriptionInfoButton.setEnabled(true);
            setSubscriptionInfo();
            useRikerAppExclusivelyInfoButton.setEnabled(true);
        }
     }

    private final void setSubscriptionInfo() {
        enrollInSubscriptionInfoTextView.setText(Utils.fromHtml(StringUtils.replace(getResources().getString(R.string.after_the_trial_enroll_msg), "__PRICE__", subscriptionPrice)));
    }

    /*@Override
    public final void onPurchasesUpdated(@BillingClient.BillingResponse final int responseCode, @Nullable final List<Purchase> purchases) {

    }*/

    @Override
    public final void dialogPositiveClicked(final int requestCode) {
        switch (requestCode) {
            case DIALOG_REQUESTCODE_GOOGLE_PLAY_PROBLEM:
            case DIALOG_REQUESTCODE_UNABLE_TO_LOAD_SKU_DETAILS:
                finish();
                break;
        }
    }
}
