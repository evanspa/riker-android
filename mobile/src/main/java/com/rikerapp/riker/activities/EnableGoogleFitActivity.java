package com.rikerapp.riker.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataType;
import com.rikerapp.riker.AnalyticsEvent;
import com.rikerapp.riker.R;
import com.rikerapp.riker.RikerApp;
import com.rikerapp.riker.Utils;
import com.rikerapp.riker.dialogfragments.ConnectToGoogleFitDialogFragment;
import com.rikerapp.riker.dialogfragments.SimpleDialogFragment;
import com.rikerapp.riker.eventbus.AppEvent;
import com.rikerapp.riker.model.Set;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.text.NumberFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;

import timber.log.Timber;

public class EnableGoogleFitActivity extends BaseActivity implements SimpleDialogFragment.Callbacks {

    // activity request codes
    private static final int REQUEST_CODE_FITNESS_PERMISSION = 9082;
    private static final int REQUEST_CODE_CREATE_BODY_WEIGHT_BML = 3;

    // dialog request codes
    private static final int DIALOG_REQUESTCODE_GOOGLE_FIT_CONNECT_FAILED_ACK = 1;
    private static final int DIALOG_REQUESTCODE_GOOGLE_FIT_CONNECT_CREATE_BML_CONFIRM = 2;
    private static final int DIALOG_REQUESTCODE_GOOGLE_FIT_CONNECTED_SYNCED_BMLS_ACK = 3;
    private static final int DIALOG_REQUESTCODE_GOOGLE_FIT_CONNECT_NO_BMLS_TO_SYNC_ACK = 4;
    private static final int DIALOG_REQUESTCODE_GOOGLE_FIT_CONNECT_NO_SETS_TO_SYNC_ACK = 5;
    private static final int DIALOG_REQUESTCODE_GOOGLE_FIT_CONNECT_SYNC_NOW_BMLS_AND_SETS_CONFIRM = 6;
    private static final int DIALOG_REQUESTCODE_GOOGLE_FIT_CONNECT_SYNC_NOW_SETS_CONFIRM = 7;
    private static final int DIALOG_REQUESTCODE_GOOGLE_FIT_CONNECT_NOTHING_TO_SYNC_ACK = 8;
    private static final int DIALOG_REQUESTCODE_GOOGLE_FIT_CONNECTED_SYNCED_SETS_ACK = 9;
    private static final int DIALOG_REQUESTCODE_GOOGLE_FIT_CONNECTED_SYNCED_SETS_AND_BMLS_ACK = 10;
    private static final int DIALOG_REQUESTCODE_GOOGLE_FIT_CONNECTED_FAILURE_SYNCING_SETS_ACK = 11;
    private static final int DIALOG_REQUESTCODE_GOOGLE_FIT_CONNECTED_FAILURE_SYNCING_SETS_AND_BMLS_ACK = 12;

    // intent data
    public static final String INTENTDATA_SHOULD_PROMPT_BEFORE_CONNECTING = "INTENTDATA_SHOULD_PROMPT_BEFORE_CONNECTING";
    public static final String INTENTDATA_SHOULD_SYNC_BODY_WEIGHTS = "INTENTDATA_SHOULD_SYNC_BODY_WEIGHTS";
    public static final String INTENTDATA_SHOULD_SYNC_WORKOUTS = "INTENTDATA_SHOULD_SYNC_WORKOUTS";

    // local state keys
    private static final String LSTATE_HAS_PROMPTED_GOOGLE_FIT_CONNECT = "LSTATE_HAS_PROMPTED_GOOGLE_FIT_CONNECT";
    private static final String LSTATE_HAVE_PROCESSED_GOOGLE_FIT_SUCCESS = "LSTATE_HAVE_PROCESSED_GOOGLE_FIT_SUCCESS";
    private static final String LSTATE_NUM_BMLS_TO_SYNC = "LSTATE_NUM_BMLS_TO_SYNC";
    private static final String LSTATE_NUM_SETS_TO_SYNC = "LSTATE_NUM_SETS_TO_SYNC";
    private static final String LSTATE_IS_SYNCING_TO_GOOGLE_FIT = "LSTATE_IS_SYNCING_TO_GOOGLE_FIT";

    private boolean hasPromptedGoogleFitConnect;
    private boolean haveProcessedGoogleFitSuccess;
    private int numBmlsToSync;
    private int numSetsToSync;
    private Button retryButton;
    private boolean isSyncingToGoogleFit;

    @Subscribe
    public final void onMessageEvent(final AppEvent.CancelConnectToGoogleFit cancelConnectToGoogleFit) {
        finish();
    }

    @Subscribe
    public final void onMessageEvent(final AppEvent.ConnectToGoogleFit connectToGoogleFit) {
        connectToGoogleFit();
    }

    @Subscribe
    public final void onMessageEvent(final AppEvent.SyncSetsToGfSuccessResult successResult) {
        this.isSyncingToGoogleFit = false;
        Utils.dismissProgressDialog(this);
        final StringBuilder successMessageText = new StringBuilder(
                String.format("Your <strong>%s set%s</strong> %s been organized into <strong>%s workout%s</strong> and synced to Google Fit as calorie expenditures and strength training activity sessions.",
                        NumberFormat.getInstance().format(successResult.numSets),
                        successResult.numSets > 1 ? "s" : "",
                        successResult.numSets > 1 ? "have" : "has",
                        NumberFormat.getInstance().format(successResult.numWorkouts),
                        successResult.numWorkouts > 1 ? "s" : ""));
        final SimpleDialogFragment simpleDialogFragment = SimpleDialogFragment.successInstance(
                DIALOG_REQUESTCODE_GOOGLE_FIT_CONNECTED_SYNCED_SETS_ACK,
                "Synced to Google Fit",
                successMessageText.toString());
        showDialog(simpleDialogFragment, "dialog_fragment_google_fit_connected_synced");
    }

    @Subscribe
    public final void onMessageEvent(final AppEvent.SyncBmlsToGfSuccessResult syncBmlsResult) {
        this.isSyncingToGoogleFit = false;
        Utils.dismissProgressDialog(this);
        final StringBuilder successMessageText = new StringBuilder();
        successMessageText.append(String.format("Your <strong>%s body weight log%s</strong> %s been synced to Google Fit.",
                NumberFormat.getInstance().format(numBmlsToSync),
                numBmlsToSync > 1 ? "s" : "",
                numBmlsToSync > 1 ? "have" : "has"));
        final SimpleDialogFragment simpleDialogFragment = SimpleDialogFragment.successInstance(
                DIALOG_REQUESTCODE_GOOGLE_FIT_CONNECTED_SYNCED_BMLS_ACK,
                "Synced to Google Fit",
                successMessageText.toString());
        showDialog(simpleDialogFragment, "dialog_fragment_google_fit_connected_synced");
    }

    @Subscribe
    public final void onMessageEvent(final AppEvent.SyncSetsAndBmlsToGfSuccessResult successResult) {
        this.isSyncingToGoogleFit = false;
        Utils.dismissProgressDialog(this);
        final StringBuilder successMessageText = new StringBuilder();
        if (successResult.numSets > 0) {
            successMessageText.append(
                    String.format("Your <strong>%s set%s</strong> %s been organized into <strong>%s workout%s</strong> and synced to Google Fit as calorie expenditures and strength training activity sessions.",
                            NumberFormat.getInstance().format(successResult.numSets),
                            successResult.numSets > 1 ? "s" : "",
                            successResult.numSets > 1 ? "have" : "has",
                            NumberFormat.getInstance().format(successResult.numWorkouts),
                            successResult.numWorkouts > 1 ? "s" : ""));
        }
        if (successResult.numBmls > 0) {
            successMessageText.append(String.format("<p>Your <strong>%s body weight log%s</strong> %s%s been synced to Google Fit.</p>",
                    NumberFormat.getInstance().format(successResult.numBmls),
                    successResult.numBmls > 1 ? "s" : "",
                    successResult.numBmls > 1 ? "have" : "has",
                    successResult.numSets > 0 ? " also" : ""));
        }
        final SimpleDialogFragment simpleDialogFragment = SimpleDialogFragment.successInstance(
                DIALOG_REQUESTCODE_GOOGLE_FIT_CONNECTED_SYNCED_SETS_AND_BMLS_ACK,
                "Synced to Google Fit",
                successMessageText.toString());
        showDialog(simpleDialogFragment, "dialog_fragment_google_fit_connected_synced");
    }

    private final void connectToGoogleFit() {
        final FitnessOptions fitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_WRITE)
                .addDataType(DataType.TYPE_WEIGHT, FitnessOptions.ACCESS_WRITE)
                .build();
        final boolean hasPermissions = GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this), fitnessOptions);
        Timber.d("inside connectToGoogleFit, hasPermissions: [%b]", hasPermissions);
        if (hasPermissions) {
            handleGoogleFitConnectSuccess();
        } else {
            GoogleSignIn.requestPermissions(this, REQUEST_CODE_FITNESS_PERMISSION, GoogleSignIn.getLastSignedInAccount(this), fitnessOptions);
        }
    }

    private final int numBmlsToSync() {
        final RikerApp rikerApp = (RikerApp)getApplication();
        final Date lastBodyWeightEndDate = rikerApp.googleFitLastBodyWeightEndDate();
        if (lastBodyWeightEndDate != null) {
            return rikerApp.dao.numBmlsWithNonNilBodyWeight(rikerApp.dao.user(), lastBodyWeightEndDate);
        } else {
            return rikerApp.dao.numBmlsWithNonNilBodyWeight(rikerApp.dao.user());
        }
    }

    private final int numSetsToSync() {
        final RikerApp rikerApp = (RikerApp)getApplication();
        final Date lastWorkoutEndDate = rikerApp.googleFitLastWorkoutEndDate();
        if (lastWorkoutEndDate != null) {
            return rikerApp.dao.numSets(rikerApp.dao.user(), lastWorkoutEndDate);
        } else {
            return rikerApp.dao.numSets(rikerApp.dao.user());
        }
    }

    private final void promptSyncBmlsAndSets() {
        final StringBuilder promptMessageText = new StringBuilder();
        promptMessageText.append(String.format("You have <strong>%s body weight log%s</strong> and <strong>%s set%s</strong> that can be synced.",
                NumberFormat.getInstance().format(numBmlsToSync),
                numBmlsToSync > 1 ? "s" : "",
                NumberFormat.getInstance().format(numSetsToSync),
                numSetsToSync > 1 ? "s" : ""));
        promptMessageText.append("<p>If you are currently in the middle of a workout, it is recommended you do not sync your workouts right now.</p>");
        final SimpleDialogFragment simpleDialogFragment = SimpleDialogFragment.confirmInstance(
                DIALOG_REQUESTCODE_GOOGLE_FIT_CONNECT_SYNC_NOW_BMLS_AND_SETS_CONFIRM,
                "Sync now to Google Fit?",
                promptMessageText.toString(),
                "Sync Now",
                String.format("Sync Body Weight%s Only", numBmlsToSync > 1 ? "s" : ""),
                "Cancel");
        showDialog(simpleDialogFragment, "dialog_fragment_google_fit_sync_bmls_and_sets_now_confirm");
    }

    private final void syncBmlsOnly() {
        this.isSyncingToGoogleFit = true;
        displaySyncingToGoogleFitProgressDialog();
        Executors.newSingleThreadExecutor().execute(() ->
            Utils.syncBmlsToGoogleFit((RikerApp)getApplication(),
                    this,
                    numBmls -> runOnUiThread(() -> EventBus.getDefault().post(new AppEvent.SyncBmlsToGfSuccessResult(numBmls))),
                    null));
    }

    private final void syncSetsOnly() {
        this.isSyncingToGoogleFit = true;
        displaySyncingToGoogleFitProgressDialog();
        Executors.newSingleThreadExecutor().execute(() ->
                Utils.syncWorkoutsToGoogleFit((RikerApp)getApplication(),
                        this,
                        (numWorkouts, numSets) -> runOnUiThread(() -> EventBus.getDefault().post(new AppEvent.SyncSetsToGfSuccessResult(numWorkouts, numSets))),
                        null));
    }

    private final void skipSetsAndFinish() {
        final RikerApp rikerApp = (RikerApp)getApplication();
        final List sets = rikerApp.dao.descendingSets(rikerApp.dao.user());
        if (sets.size() > 0) {
            final Set latestSet = (Set)sets.get(0);
            rikerApp.setGoogleFitLastWorkoutEndDate(latestSet.loggedAt);
        }
        Toast.makeText(this, "Sets skipped.", Toast.LENGTH_SHORT).show();
        finish();
    }

    private final void syncBmlsAndSets() {
        this.isSyncingToGoogleFit = true;
        displaySyncingToGoogleFitProgressDialog();
        Executors.newSingleThreadExecutor().execute(() -> {
            final RikerApp rikerApp = (RikerApp)getApplication();
            Utils.syncBmlsToGoogleFit(rikerApp,
                    this,
                    numBmls -> Utils.syncWorkoutsToGoogleFit(rikerApp,
                            this,
                            (numWorkouts, numSets) -> runOnUiThread(() -> EventBus.getDefault().post(new AppEvent.SyncSetsAndBmlsToGfSuccessResult(numWorkouts, numSets, numBmls))),
                            throwable -> {}),
                    throwable -> {});
        });
    }

    private final void promptSyncSets() {
        final StringBuilder promptMessageText = new StringBuilder();
        promptMessageText.append(String.format("You have <strong>%s set%s</strong> that can be synced.",
                NumberFormat.getInstance().format(numSetsToSync),
                numSetsToSync > 1 ? "s" : ""));
        promptMessageText.append("<p>If you are currently in the middle of a workout, it is recommended you do not sync your workouts right now.</p>");
        final SimpleDialogFragment simpleDialogFragment = SimpleDialogFragment.confirmInstance(
                DIALOG_REQUESTCODE_GOOGLE_FIT_CONNECT_SYNC_NOW_SETS_CONFIRM,
                "Sync now to Google Fit?",
                promptMessageText.toString(),
                "Sync Now",
                "Don't Sync.  Skip them",
                "Not now");
        showDialog(simpleDialogFragment, "dialog_fragment_google_fit_sync_sets_now_confirm");
    }

    private final void indicateNothingToSync() {
        final SimpleDialogFragment simpleDialogFragment = SimpleDialogFragment.infoInstance(
                DIALOG_REQUESTCODE_GOOGLE_FIT_CONNECT_NOTHING_TO_SYNC_ACK,
                "Nothing to Sync",
                "You don't currently have any body weight logs or sets to sync to Google Fit.<p>Future body weight logs will be synced automatically as you create them.</p>");
        showDialog(simpleDialogFragment, "dialog_fragment_nothing_to_sync");
    }

    private final void promptCreateBml() {
        final SimpleDialogFragment simpleDialogFragment = SimpleDialogFragment.confirmInstance(
                DIALOG_REQUESTCODE_GOOGLE_FIT_CONNECT_CREATE_BML_CONFIRM,
                "No Body Weight Logs",
                "In order to calculate the calories burned in your workouts, we need to know your body weight.<p>Once you have at least 1 body weight log, Riker will be able to sync your workouts to Google Fit.</p><p>Create body weight log?</p>",
                "Okay",
                "Not Now");
        showDialog(simpleDialogFragment, "dialog_fragment_confirm_create_bml");
    }

    private final void indicateNoBmlsToSync() {
        final SimpleDialogFragment simpleDialogFragment = SimpleDialogFragment.infoInstance(
                DIALOG_REQUESTCODE_GOOGLE_FIT_CONNECT_NO_BMLS_TO_SYNC_ACK,
                "No Body Weight Logs to Sync",
                "You don't currently have any body weight logs to sync to Google Fit.<p>Future body weight logs will be synced as you create them automatically.</p>");
        showDialog(simpleDialogFragment, "dialog_fragment_no_bmls_to_sync");
    }

    private final void indicateNoSetsToSync() {
        final SimpleDialogFragment simpleDialogFragment = SimpleDialogFragment.infoInstance(
                DIALOG_REQUESTCODE_GOOGLE_FIT_CONNECT_NO_SETS_TO_SYNC_ACK,
                "No Workouts to Sync",
                "You don't currently have any workouts to sync.");
        showDialog(simpleDialogFragment, "dialog_fragment_no_sets_to_sync");
    }

    private final void computeSync() {
        final boolean shouldSyncBodyWeights = getIntent().getBooleanExtra(INTENTDATA_SHOULD_SYNC_BODY_WEIGHTS, false);
        final boolean shouldSyncWorkouts = getIntent().getBooleanExtra(INTENTDATA_SHOULD_SYNC_WORKOUTS, false);
        final RikerApp rikerApp = (RikerApp)getApplication();
        if (shouldSyncBodyWeights) {
            rikerApp.setGoogleFitBodyWeightsDisabledAt(null);
            numBmlsToSync = numBmlsToSync();
            if (shouldSyncWorkouts) {
                rikerApp.setGoogleFitWorkoutsDisabledAt(null);
                final boolean hasNonNilBodyWeightBml = rikerApp.dao.mostRecentBmlWithNonNilWeight(rikerApp.dao.user()) != null;
                if (hasNonNilBodyWeightBml) {
                    if (numBmlsToSync > 0) {
                        numSetsToSync = numSetsToSync();
                        if (numSetsToSync > 0) {
                            promptSyncBmlsAndSets();
                        } else {
                            syncBmlsOnly();
                        }
                    } else {
                        numSetsToSync = numSetsToSync();
                        if (numSetsToSync > 0) {
                            promptSyncSets();
                        } else {
                            indicateNothingToSync(); // absolutely nothing to sync
                        }
                    }
                } else {
                    promptCreateBml();
                }
            } else {
                if (numBmlsToSync > 0) {
                    syncBmlsOnly();
                } else {
                    indicateNoBmlsToSync();
                }
            }
        } else if (shouldSyncWorkouts) {
            rikerApp.setGoogleFitWorkoutsDisabledAt(null);
            final boolean hasNonNilBodyWeightBml = rikerApp.dao.mostRecentBmlWithNonNilWeight(rikerApp.dao.user()) != null;
            if (hasNonNilBodyWeightBml) {
                numSetsToSync = numSetsToSync();
                if (numSetsToSync > 0) {
                    promptSyncSets();
                } else {
                    indicateNoSetsToSync();
                }
            } else {
                promptCreateBml();
            }
        } else {
            // should never get here
        }
    }

    private final void handleGoogleFitConnectSuccess() {
        final RikerApp rikerApp = (RikerApp)getApplication();
        rikerApp.setGoogleFitEnabledAt(new Date());
        computeSync();
        haveProcessedGoogleFitSuccess = true;
    }

    @Override
    public final void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public final void onSaveInstanceState(final Bundle outState) {
        outState.putBoolean(LSTATE_HAS_PROMPTED_GOOGLE_FIT_CONNECT, this.hasPromptedGoogleFitConnect);
        outState.putBoolean(LSTATE_HAVE_PROCESSED_GOOGLE_FIT_SUCCESS, this.haveProcessedGoogleFitSuccess);
        outState.putInt(LSTATE_NUM_BMLS_TO_SYNC, this.numBmlsToSync);
        outState.putInt(LSTATE_NUM_SETS_TO_SYNC, this.numSetsToSync);
        outState.putBoolean(LSTATE_IS_SYNCING_TO_GOOGLE_FIT, this.isSyncingToGoogleFit);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.attemptGoogleFitSyncingOnActivityResume = false;
        EventBus.getDefault().register(this);
        setContentView(R.layout.activity_enable_google_fit);
        configureAppBar();
        if (savedInstanceState != null) {
            this.hasPromptedGoogleFitConnect = savedInstanceState.getBoolean(LSTATE_HAS_PROMPTED_GOOGLE_FIT_CONNECT);
            this.haveProcessedGoogleFitSuccess = savedInstanceState.getBoolean(LSTATE_HAVE_PROCESSED_GOOGLE_FIT_SUCCESS);
            this.numBmlsToSync = savedInstanceState.getInt(LSTATE_NUM_BMLS_TO_SYNC, 0);
            this.numSetsToSync = savedInstanceState.getInt(LSTATE_NUM_SETS_TO_SYNC, 0);
            this.isSyncingToGoogleFit = savedInstanceState.getBoolean(LSTATE_IS_SYNCING_TO_GOOGLE_FIT, false);
        }
        final RikerApp rikerApp = (RikerApp)getApplication();
        final boolean shouldPromptBeforeConnecting = getIntent().getBooleanExtra(INTENTDATA_SHOULD_PROMPT_BEFORE_CONNECTING, true);
        retryButton = findViewById(R.id.retryButton);
        retryButton.setEnabled(false);
        retryButton.setOnClickListener(view -> {
            logEvent(AnalyticsEvent.RETRY_GOOGLE_FIT_CONNECT_ATTEMPT);
            connectToGoogleFit();
        });
        final Button cancelButton = findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(view -> {
            logEvent(AnalyticsEvent.CANCEL_GOOGLE_FIT_CONNECT_ATTEMPT);
            finish();
        });
        if (isSyncingToGoogleFit) {
            displaySyncingToGoogleFitProgressDialog();
        } else {
            if (rikerApp.googleFitEnabledAt() == null) {
                if (!this.hasPromptedGoogleFitConnect) {
                    if (shouldPromptBeforeConnecting) {
                        final ConnectToGoogleFitDialogFragment googleFitDialogFragment = new ConnectToGoogleFitDialogFragment();
                        getSupportFragmentManager().beginTransaction().add(googleFitDialogFragment, "google_fit_dialog_prompt").commitAllowingStateLoss();
                        this.hasPromptedGoogleFitConnect = true;
                    } else {
                        connectToGoogleFit();
                    }
                }
            } else {
                connectToGoogleFit();
            }
        }
    }

    private final void displaySyncingToGoogleFitProgressDialog() {
        Utils.displayProgressDialog(this, "Syncing to Google Fit...");
    }

    @Override
    protected final void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        Timber.d("inside onActivityResult, requestCode: [%d], resultCode: [%d]", requestCode, resultCode);
        switch (requestCode) {
            case REQUEST_CODE_FITNESS_PERMISSION:
                if (resultCode == Activity.RESULT_OK) {
                    handleGoogleFitConnectSuccess();
                } else {
                    // permission denied
                    final RikerApp rikerApp = (RikerApp)getApplication();
                    rikerApp.setGoogleFitEnabledAt(null);
                    finish();
                }
                break;
            case REQUEST_CODE_CREATE_BODY_WEIGHT_BML:
                final RikerApp rikerApp = (RikerApp)getApplication();
                final boolean hasNonNilBodyWeightBml = rikerApp.dao.mostRecentBmlWithNonNilWeight(rikerApp.dao.user()) != null;
                if (hasNonNilBodyWeightBml) {
                    new Handler().postDelayed(() -> computeSync(), 100);
                } else {
                    finish();
                }
                break;
            default:
                if (resultCode == 0) {
                    // onActivityResult will be called if, when attempting to connect to Google Fit, and user
                    // gets the system dialog prompt to pick their account, and they dismiss it, the result
                    // code will be 0...and so, we should just finish
                    finish();
                }
        }
    }

    @Override
    public final void dialogPositiveClicked(final int requestCode) {
        switch (requestCode) {
            case DIALOG_REQUESTCODE_GOOGLE_FIT_CONNECT_CREATE_BML_CONFIRM:
                startActivityForResult(new Intent(this, EnterBodyWeightActivity.class), REQUEST_CODE_CREATE_BODY_WEIGHT_BML);
                break;
            case DIALOG_REQUESTCODE_GOOGLE_FIT_CONNECTED_SYNCED_BMLS_ACK:
            case DIALOG_REQUESTCODE_GOOGLE_FIT_CONNECTED_SYNCED_SETS_ACK:
            case DIALOG_REQUESTCODE_GOOGLE_FIT_CONNECTED_SYNCED_SETS_AND_BMLS_ACK:
            case DIALOG_REQUESTCODE_GOOGLE_FIT_CONNECT_NO_BMLS_TO_SYNC_ACK:
            case DIALOG_REQUESTCODE_GOOGLE_FIT_CONNECT_NO_SETS_TO_SYNC_ACK:
            case DIALOG_REQUESTCODE_GOOGLE_FIT_CONNECT_FAILED_ACK:
            case DIALOG_REQUESTCODE_GOOGLE_FIT_CONNECT_NOTHING_TO_SYNC_ACK:
            case DIALOG_REQUESTCODE_GOOGLE_FIT_CONNECTED_FAILURE_SYNCING_SETS_ACK:
            case DIALOG_REQUESTCODE_GOOGLE_FIT_CONNECTED_FAILURE_SYNCING_SETS_AND_BMLS_ACK:
                finish();
                break;
            case DIALOG_REQUESTCODE_GOOGLE_FIT_CONNECT_SYNC_NOW_BMLS_AND_SETS_CONFIRM:
                syncBmlsAndSets();
                break;
            case DIALOG_REQUESTCODE_GOOGLE_FIT_CONNECT_SYNC_NOW_SETS_CONFIRM:
                syncSetsOnly();
                break;
            default:
                super.dialogPositiveClicked(requestCode);
                break;
        }
    }

    @Override
    public final void dialogNegativeClicked(final int requestCode) {
        switch (requestCode) {
            case DIALOG_REQUESTCODE_GOOGLE_FIT_CONNECT_SYNC_NOW_BMLS_AND_SETS_CONFIRM: // user hit 'cancel' button
                final RikerApp rikerApp = (RikerApp)getApplication();
                rikerApp.setGoogleFitEnabledAt(null);
                final Date now = new Date();
                rikerApp.setGoogleFitWorkoutsDisabledAt(now);
                rikerApp.setGoogleFitBodyWeightsDisabledAt(now);
                finish();
                break;
            case DIALOG_REQUESTCODE_GOOGLE_FIT_CONNECT_CREATE_BML_CONFIRM:
                finish();
                break;
            case DIALOG_REQUESTCODE_GOOGLE_FIT_CONNECT_SYNC_NOW_SETS_CONFIRM:
                finish();
                break;
        }
    }

    @Override
    public final void dialogNeutralClicked(final int requestCode) {
        switch (requestCode) {
            case DIALOG_REQUESTCODE_GOOGLE_FIT_CONNECT_SYNC_NOW_BMLS_AND_SETS_CONFIRM:
                syncBmlsOnly();
                break;
            case DIALOG_REQUESTCODE_GOOGLE_FIT_CONNECT_SYNC_NOW_SETS_CONFIRM:
                skipSetsAndFinish();
                break;
        }
    }
}
