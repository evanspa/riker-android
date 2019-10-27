package com.rikerapp.riker.activities;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.rikerapp.riker.AnalyticsEvent;
import com.rikerapp.riker.EventParam;
import com.rikerapp.riker.R;
import com.rikerapp.riker.RikerApp;
import com.rikerapp.riker.SharedPreferenceKey;
import com.rikerapp.riker.Utils;
import com.rikerapp.riker.adapters.MovementSearchResultsAdapter;
import com.rikerapp.riker.dialogfragments.SimpleDialogFragment;
import com.rikerapp.riker.importexport.ExportTaskResult;
import com.rikerapp.riker.model.Movement;
import com.rikerapp.riker.model.MovementSearchResult;
import com.rikerapp.riker.model.MovementVariant;
import com.rikerapp.riker.model.Set;
import com.rikerapp.riker.model.User;
import com.rikerapp.riker.model.WeightUnit;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;

import static com.rikerapp.riker.Constants.RIKER_PREFERENCES;

public abstract class BaseActivity extends AppCompatActivity implements SimpleDialogFragment.Callbacks {

    //public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    // local state keys
    private static final String LSTATE_EXPORT_RESULT = "LSTATE_EXPORT_RESULT";
    private static final String LSTATE_MOVEMENT_SEARCH_TEXT = "LSTATE_MOVEMENT_SEARCH_TEXT";

    // dialog request codes
    public static final int DIALOG_REQUEST_CODE_EXPORT_COMPLETE_INFO = 98;
    public static final int DIALOG_REQUEST_CODE_SHARE_EXPORT_FILE_CONFIRM = 99;
    public static final int DIALOG_REQUESTCODE_CHART_INFO = 100;
    public static final int DIALOG_REQUESTCODE_WEIGHT_LIFTED_INFO_ACK = 1;
    public static final int DIALOG_REQUESTCODE_TOTAL_INFO_ACK = 2;
    public static final int DIALOG_REQUESTCODE_AVG_INFO_ACK = 3;
    public static final int DIALOG_REQUESTCODE_DIST_INFO_ACK = 4;
    public static final int DIALOG_REQUESTCODE_DIST_TIME_INFO_ACK = 5;
    public static final int DIALOG_REQUESTCODE_NO_CHARTS_TO_CONFIGURE_ACK = 6;
    public static final int DIALOG_REQUESTCODE_NETWORK_PROBLEM = 200;
    public static final int DIALOG_REQUESTCODE_SERVER_ERROR = 201;
    public static final int DIALOG_REQUESTCODE_SERVER_BUSY = 202;

    // other request codes
    public static final int REQUEST_CODE_CHART_CONFIG = 1000;
    public static final int REQUEST_CODE_SIGNUP = 1001;
    public static final int REQUEST_CODE_LOGIN = 1002;

    public FloatingActionsMenu floatingActionsMenu;
    public EditText searchMovementsEditText;
    public Button cancelMovementSearchEditTextButton;

    public ExportTaskResult exportResult;
    public String movementSearchText;

    public boolean attemptGoogleFitSyncingOnActivityResume = true;

    @Override
    public void dialogPositiveClicked(final int requestCode) {
        switch (requestCode) {
            case DIALOG_REQUESTCODE_WEIGHT_LIFTED_INFO_ACK:
            case DIALOG_REQUESTCODE_TOTAL_INFO_ACK:
                break;
            case DIALOG_REQUEST_CODE_EXPORT_COMPLETE_INFO:
                final SimpleDialogFragment simpleDialogFragment = SimpleDialogFragment.confirmInstance(DIALOG_REQUEST_CODE_SHARE_EXPORT_FILE_CONFIRM,
                        "Share Export File?",
                        "Would you like to share the export file?\n\nPerhaps email it to yourself or save it to your Google Drive?",
                        "Yes",
                        "No");
                showDialog(simpleDialogFragment, "dialog_fragment_share_export_file_confirm");
                break;
            case DIALOG_REQUEST_CODE_SHARE_EXPORT_FILE_CONFIRM:
                Utils.shareExportFile(this, exportResult.fileName, exportResult.numRecords);
                break;
        }
    }

    @Override
    public void dialogNegativeClicked(final int requestCode) {}

    @Override
    public void dialogNeutralClicked(final int requestCode) {}

    @Override
    public void onBackPressed() {
        if (floatingActionsMenu != null && floatingActionsMenu.isExpanded()) {
            floatingActionsMenu.collapse();
        } else {
            if (cancelMovementSearchEditTextButton != null && cancelMovementSearchEditTextButton.getVisibility() == View.VISIBLE) {
                searchMovementsEditText.setVisibility(View.GONE);
                cancelMovementSearchEditTextButton.setVisibility(View.GONE);
                cancelMovementSearch();
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        if (exportResult != null) {
            outState.putSerializable(LSTATE_EXPORT_RESULT, this.exportResult);
            outState.putString(LSTATE_MOVEMENT_SEARCH_TEXT, this.movementSearchText);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            this.exportResult = (ExportTaskResult) savedInstanceState.getSerializable(LSTATE_EXPORT_RESULT);
            this.movementSearchText = savedInstanceState.getString(LSTATE_MOVEMENT_SEARCH_TEXT);
        }
    }

    private final void attemptSyncWorkouts(final RikerApp rikerApp) {
        Utils.syncWorkoutsToGoogleFit(rikerApp,
                this,
                (numWorkouts, numSets) -> {
                    if (numWorkouts > 0) {
                        runOnUiThread(() -> Toast.makeText(this, String.format("Your workout%s been synced to Google Fit.", numWorkouts > 1 ? "s have" : " has"), Toast.LENGTH_LONG).show());
                    }
                },
                throwable -> {});
    }

    public final void attemptSyncBmls(final RikerApp rikerApp) {
        Executors.newSingleThreadExecutor().execute(() ->
                Utils.syncBmlsToGoogleFit(rikerApp,
                this,
                        numBmls -> {
                            if (numBmls > 0) {
                                runOnUiThread(() -> Toast.makeText(this,
                                        String.format("Your body weight log%s been synced to Google Fit.", numBmls > 1 ? "s have" : " has"),
                                        Toast.LENGTH_LONG).show());
                            }
                        },
                        throwable -> runOnUiThread(() -> Toast.makeText(this,
                                String.format("There was a problem trying to sync to Google Fit."),
                                Toast.LENGTH_LONG).show())));
    }

    private final void attemptSyncWorkoutsAndBmls(final RikerApp rikerApp) {
        Executors.newSingleThreadExecutor().execute(() ->
            Utils.syncBmlsToGoogleFit(rikerApp,
                    this,
                    numBmls -> Utils.syncWorkoutsToGoogleFit(rikerApp,
                            this,
                            (numWorkouts, numSets) -> runOnUiThread(() -> {
                                String toastText = null;
                                if (numWorkouts > 0) {
                                    if (numBmls > 0) {
                                        // since we're syncing both, this should realistically be the only valid code path
                                        toastText = String.format("Your body weight log%s and workout%s been synced to Google Fit.",
                                                numBmls > 1 ? "s" : "",
                                                numWorkouts > 1 ? "s have" : " has");
                                    } else {
                                        toastText = String.format("Your workout%s been synced to Google Fit.",
                                                numWorkouts > 1 ? "s have" : " has");
                                    }
                                } else if (numBmls > 0) {
                                    toastText = String.format("Your body weight log%s been synced to Google Fit.",
                                            numBmls > 1 ? "s have" : " has");
                                }
                                if (toastText != null) {
                                    Toast.makeText(this, toastText, Toast.LENGTH_LONG).show();
                                }
                            }),
                            throwable -> runOnUiThread(() -> Toast.makeText(this,
                                    String.format("There was a problem trying to sync your workouts to Google Fit."),
                                    Toast.LENGTH_LONG).show())),
                    throwable -> runOnUiThread(() -> Toast.makeText(this,
                            String.format("There was a problem trying to sync to Google Fit."),
                            Toast.LENGTH_LONG).show())));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (attemptGoogleFitSyncingOnActivityResume) {
            final RikerApp rikerApp = (RikerApp)getApplication();
            if (rikerApp.googleFitEnabledAt() != null) {
                final Date gfWorkoutsDisabledAt = rikerApp.googleFitWorkoutsDisabledAt();
                final Date gfBodyWeightsDisabledAt = rikerApp.googleFitBodyWeightsDisabledAt();
                if (gfWorkoutsDisabledAt == null) {
                    if (gfBodyWeightsDisabledAt == null) {
                        attemptSyncWorkoutsAndBmls(rikerApp);
                    } else {
                        attemptSyncWorkouts(rikerApp);
                    }
                } else if (gfBodyWeightsDisabledAt == null) {
                    attemptSyncBmls(rikerApp);
                }
            }
        }
    }

    public final void configureFloatingActionsMenu() {
        floatingActionsMenu = findViewById(R.id.floatingActionsMenu);
        if (floatingActionsMenu != null) {
            final com.getbase.floatingactionbutton.FloatingActionButton strengthTrainFab =
                    findViewById(R.id.strengthTrainFab);
            strengthTrainFab.setOnClickListener(view -> {
                startActivityForResult(new Intent(this, SelectBodySegmentActivity.class), 0);
                collapseFabMenuDelayed();
            });
            final com.getbase.floatingactionbutton.FloatingActionButton logBmlFab =
                    findViewById(R.id.logBmlFab);
            logBmlFab.setOnClickListener(view -> {
                startActivityForResult(new Intent(this, WhatToMeasureActivity.class), 0);
                collapseFabMenuDelayed();
            });
            final ViewGroup contentContainer = findViewById(R.id.contentContainer);
            if (contentContainer != null) {
                contentContainer.setOnClickListener(view -> floatingActionsMenu.collapse());
            }
        }
    }

    public final void collapseFabMenuDelayed() {
        new Handler().postDelayed(() -> floatingActionsMenu.collapse(), 150);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_action_movement_search:
                collapseFabMenuDelayed();
                this.searchMovementsEditText.setVisibility(View.VISIBLE);
                this.cancelMovementSearchEditTextButton.setVisibility(View.VISIBLE);
                Utils.showKeyboard(this, this.searchMovementsEditText);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public final void configureAppBar() {
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public final void logScreen(final CharSequence title) {
        FirebaseAnalytics.getInstance(this).setCurrentScreen(this, title.toString().toLowerCase().replace(" ", "_"), null);
    }

    public final void logEvent(final AnalyticsEvent event, final EventParam... eventParams) {
        final Bundle bundle = new Bundle();
        for (final EventParam eventParam : eventParams) {
            bundle.putString(eventParam.paramName.name, eventParam.value);
        }
        FirebaseAnalytics.getInstance(this).logEvent(event.eventName, bundle);
    }

    public final void logEvent(final AnalyticsEvent event) {
        FirebaseAnalytics.getInstance(this).logEvent(event.eventName, null);
    }

    public final void logHelpInfoPopupContentViewed(final String contentName) {
        final Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, contentName);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "help_info_popup");
        FirebaseAnalytics.getInstance(this).logEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle);
    }

    public final void logNewSetEvent(final Set newSet) {
        final Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.SCORE,
                Utils.weightValue(newSet.weight.multiply(new BigDecimal(newSet.numReps)),
                        WeightUnit.weightUnitById(newSet.weightUom),
                        WeightUnit.LBS).toString());
        FirebaseAnalytics.getInstance(this).logEvent(FirebaseAnalytics.Event.POST_SCORE, bundle);
    }

    public final void handleExportCompleteEvent(final ExportTaskResult exportResult, final AnalyticsEvent event, final String entityTypeTitleCase) {
        Utils.dismissProgressDialog(this);
        this.exportResult = exportResult;
        if (exportResult.numRecords > 0) {
            logEvent(event, new EventParam(EventParam.ParamName.NUM_EXPORTED, Integer.toString(exportResult.numRecords)));
            final SimpleDialogFragment simpleDialogFragment = SimpleDialogFragment.successInstance(
                    DIALOG_REQUEST_CODE_EXPORT_COMPLETE_INFO,
                    "Export Complete",
                    String.format("<p>Your <strong>%s Riker %s %s</strong> %s been exported to the following CSV file:</p><p><strong>%s</strong></p>",
                            NumberFormat.getInstance().format(exportResult.numRecords),
                            entityTypeTitleCase.toLowerCase(),
                            Utils.pluralize("record", exportResult.numRecords),
                            exportResult.numRecords == 1 ? "has" : "have",
                            exportResult.fileName));
            showDialog(simpleDialogFragment, "dialog_fragment_export_success");
        } else {
            final SimpleDialogFragment simpleDialogFragment = SimpleDialogFragment.errorInstance(null,
                    String.format("No %ss", entityTypeTitleCase),
                    String.format("You currently have no %ss to export.", entityTypeTitleCase.toLowerCase()));
            showDialog(simpleDialogFragment, "dialog_fragment_none_to_export");
        }
    }

    public final void cancelMovementSearch() {
        final ListView searchResultsListView = findViewById(R.id.searchResultsListView);
        final MovementSearchResultsAdapter movementSearchResultsAdapter = (MovementSearchResultsAdapter)searchResultsListView.getAdapter();
        movementSearchResultsAdapter.clear();
        movementSearchText = null;
        searchMovementsEditText.setText("");
        final LinearLayout numSearchResultsHeaderLinearLayout = findViewById(R.id.numSearchResultsHeaderLinearLayout);
        numSearchResultsHeaderLinearLayout.setVisibility(View.GONE);
        Utils.clearFocusAndDismissKeyboard(this, searchMovementsEditText);
    }

    public final void configureMovementSearch() {
        searchMovementsEditText = findViewById(R.id.searchMovementsEditText);
        cancelMovementSearchEditTextButton = findViewById(R.id.cancelMovementSearchEditTextButton);
        final RikerApp rikerApp = (RikerApp)getApplication();
        final LinearLayout numSearchResultsHeaderLinearLayout = findViewById(R.id.numSearchResultsHeaderLinearLayout);
        final ListView searchResultsListView = findViewById(R.id.searchResultsListView);
        final MovementSearchResultsAdapter movementSearchResultsAdapter =
                new MovementSearchResultsAdapter(this, R.layout.movement_search_result);
        searchResultsListView.setAdapter(movementSearchResultsAdapter);
        if (cancelMovementSearchEditTextButton != null) {
            cancelMovementSearchEditTextButton.setOnClickListener(view -> {
                searchMovementsEditText.setVisibility(View.GONE);
                cancelMovementSearchEditTextButton.setVisibility(View.GONE);
                cancelMovementSearch();
            });
        }
        numSearchResultsHeaderLinearLayout.setVisibility(View.GONE);
        final TextView numSearchResultsTextView = findViewById(R.id.numSearchResultsTextView);
        final Button cancelSearchResultsButton = findViewById(R.id.cancelSearchResultsButton);
        if (cancelMovementSearchEditTextButton != null) {
            cancelSearchResultsButton.setVisibility(View.GONE);
        } else {
            cancelSearchResultsButton.setOnClickListener(view -> {
                cancelMovementSearch();
            });
        }
        searchResultsListView.setOnItemClickListener((parent, view, position, id) -> {
            Utils.clearFocusAndDismissKeyboard(this, searchMovementsEditText);
            movementSearchText = null;
            final MovementSearchResult selectedSearchResult = movementSearchResultsAdapter.movementSearchResultList.get(position);
            final Movement movement = new Movement();
            movement.localIdentifier = selectedSearchResult.movementId;
            movement.variantMask = selectedSearchResult.variantMask;
            movement.canonicalName = selectedSearchResult.canonicalName;
            movement.percentageOfBodyWeight = selectedSearchResult.percentageOfBodyWeight;
            movement.isBodyLift = selectedSearchResult.isBodyLift;
            movement.sortOrder = selectedSearchResult.sortOrder;
            if (selectedSearchResult.variantMask != null && selectedSearchResult.variantMask != 0) {
                final List<MovementVariant> variants = rikerApp.dao.movementVariants(selectedSearchResult.variantMask);
                if (variants.size() > 1) {
                    startActivityForResult(SelectMovementVariantActivity.makeIntent(
                            this,
                            null,
                            null,
                            movement,
                            true), 0);
                } else {
                    startActivityForResult(EnterRepsActivity.makeIntent(this, movement, variants.get(0), rikerApp.setsMap), 0);
                }
            } else {
                startActivityForResult(EnterRepsActivity.makeIntent(this, movement, null, rikerApp.setsMap), 0);
            }
        });
        new Handler().post(() -> {  // https://stackoverflow.com/a/29449717/1034895
            searchMovementsEditText.addTextChangedListener(new TextWatcher() {
                @Override public final void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {}
                @Override public final void onTextChanged(final CharSequence s, final int start, final int before, final int count) {}
                @Override public final void afterTextChanged(final Editable s) {
                    movementSearchText = s.toString();
                    numSearchResultsHeaderLinearLayout.setVisibility(View.VISIBLE);
                    final String searchText = s.toString();
                    if (searchText.trim().length() > 0) {
                        final List<MovementSearchResult> searchResultList = rikerApp.dao.searchMovements(searchText);
                        final int numSearchResults = searchResultList.size();
                        numSearchResultsTextView.setText(String.format("%d result%s", numSearchResults, numSearchResults > 1 || numSearchResults == 0 ? "s" : ""));
                        movementSearchResultsAdapter.movementSearchResultList = searchResultList;
                    } else {
                        numSearchResultsTextView.setText("0 results");
                        movementSearchResultsAdapter.movementSearchResultList = null;
                    }
                    movementSearchResultsAdapter.notifyDataSetChanged();
                }
            });
        });
        if (movementSearchText != null && movementSearchText.trim().length() > 0) {
            // The following line isn't really necessary because EditTexts will retain their value
            // on orientation changes (https://stackoverflow.com/questions/8361501/android-why-does-my-edittext-keep-its-value-even-on-orientation-change)
            searchMovementsEditText.setText(movementSearchText);
            final List<MovementSearchResult> searchResultList = rikerApp.dao.searchMovements(movementSearchText);
            final int numSearchResults = searchResultList.size();
            numSearchResultsHeaderLinearLayout.setVisibility(View.VISIBLE);
            numSearchResultsTextView.setText(String.format("%d result%s", numSearchResults, numSearchResults > 1 || numSearchResults == 0 ? "s" : ""));
            movementSearchResultsAdapter.movementSearchResultList = searchResultList;
            movementSearchResultsAdapter.notifyDataSetChanged();
        }
    }

    public final boolean isPortrait() {
        return getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    public final void setDrawable(@DrawableRes final int drawableRes, final View view) {
        final int sdk = android.os.Build.VERSION.SDK_INT;
        if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackgroundDrawable(ContextCompat.getDrawable(this, drawableRes) );
        } else {
            view.setBackground(ContextCompat.getDrawable(this, drawableRes));
        }
    }

    private final void indicate(final String key) {
        final SharedPreferences sharedPreferences = getSharedPreferences(RIKER_PREFERENCES, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(key, new Date().getTime()).apply();
    }

    public final void indicateEntitySavedOrDeleted() {
        indicate(SharedPreferenceKey.ENTITY_SAVED_OR_DELETED_INDICATOR.name());
    }

    private final String chartsUpdateAtPrefKey() {
        return String.format("%s_chartsUpdatedAt", getClass().getName());
    }

    private final Date sharedPrefDate(final String key) {
        final long chartsUpdatedAtLong = getSharedPreferences(RIKER_PREFERENCES, Context.MODE_PRIVATE).getLong(key, 0);
        if (chartsUpdatedAtLong > 0) {
            return new Date(chartsUpdatedAtLong);
        }
        return null;
    }

    public final Date chartsUpdatedAt() {
        return sharedPrefDate(chartsUpdateAtPrefKey());
    }

    public final void indicateChartsUpdated() {
        indicate(chartsUpdateAtPrefKey());
    }

    public final Date entitySavedOrDeletedAt() {
        return sharedPrefDate(SharedPreferenceKey.ENTITY_SAVED_OR_DELETED_INDICATOR.name());
    }

    public final void showDialog(final SimpleDialogFragment simpleDialogFragment, final String tag) {
        getSupportFragmentManager().beginTransaction().add(simpleDialogFragment, tag).commitAllowingStateLoss();
    }

    public final boolean offlineMode() {
        return getSharedPreferences(RIKER_PREFERENCES, MODE_PRIVATE).getLong(SharedPreferenceKey.OFFLINE_MODE_ENABLED_AT.name(), 0) != 0;
    }

    public final Date offlineModeEnabledAt() {
        final long unixTime = getSharedPreferences(RIKER_PREFERENCES, MODE_PRIVATE).getLong(SharedPreferenceKey.OFFLINE_MODE_ENABLED_AT.name(), 0);
        return unixTime > 0 ? new Date(unixTime) : null;
    }

    public final void setOfflineModeEnabledAt(final Date date) {
        final SharedPreferences sharedPreferences = getSharedPreferences(RIKER_PREFERENCES, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(SharedPreferenceKey.OFFLINE_MODE_ENABLED_AT.name(), date.getTime()).apply();
    }

    public final boolean isUserLoggedIn() {
        final RikerApp rikerApp = (RikerApp)getApplication();
        return isUserLoggedIn(rikerApp.dao.user());
    }

    public final boolean isUserLoggedIn(final User user) {
        return user.globalIdentifier != null;
    }

    public final boolean doesUserHaveValidAuthToken() {
        return getSharedPreferences(RIKER_PREFERENCES, MODE_PRIVATE).getString(SharedPreferenceKey.AUTH_TOKEN.name(), null) != null;
    }

    public final void showServerBusyDialog() {
        final SimpleDialogFragment simpleDialogFragment = SimpleDialogFragment.warningInstance(
                DIALOG_REQUESTCODE_SERVER_BUSY,
                "Server undergoing maintenance",
                "<p>We apologize, but the server is currently busy undergoing maintenance.  Please re-try your request shortly.</p>");
        showDialog(simpleDialogFragment, "dialog_fragment_server_busy");
    }

    public final void showNetworkProblemDialog() {
        final SimpleDialogFragment simpleDialogFragment = SimpleDialogFragment.warningInstance(
                DIALOG_REQUESTCODE_NETWORK_PROBLEM,
                "Oops",
                "<p>Could not contact Riker's server.</p><p>Please check your internet connection and try again.</p>");
        showDialog(simpleDialogFragment, "dialog_fragment_network_problem");
    }

    public final void showServerErrorDialog() {
        final SimpleDialogFragment simpleDialogFragment = SimpleDialogFragment.warningInstance(
                DIALOG_REQUESTCODE_SERVER_ERROR,
                "Oops",
                "<p>Riker's server is currently having some trouble.  Please try again later.</p>");
        showDialog(simpleDialogFragment, "dialog_fragment_server_error");
    }

    public final void showValidationErrors(final List<String> errors) {
        final SimpleDialogFragment simpleDialogFragment =
                SimpleDialogFragment.validationErrorsInstance(null, errors);
        showDialog(simpleDialogFragment, "dialog_fragment_validation_errors");
    }

    public final void rateNow() {
        final String packageName = getPackageName();
        final Uri uri = Uri.parse(String.format("market://details?id=%s", packageName));
        final Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse(String.format("http://play.google.com/store/apps/details?id=%s", packageName))));
        }
    }
}
