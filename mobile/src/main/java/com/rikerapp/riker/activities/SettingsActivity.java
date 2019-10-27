package com.rikerapp.riker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.SwitchCompat;
import android.widget.Button;
import android.widget.TextView;

import com.rikerapp.riker.AnalyticsEvent;
import com.rikerapp.riker.BuildConfig;
import com.rikerapp.riker.Constants;
import com.rikerapp.riker.R;
import com.rikerapp.riker.RikerApp;
import com.rikerapp.riker.Utils;
import com.rikerapp.riker.dialogfragments.SimpleDialogFragment;
import com.rikerapp.riker.eventbus.AppEvent;
import com.rikerapp.riker.importexport.ExportBmlsTask;
import com.rikerapp.riker.importexport.ExportSetsTask;
import com.rikerapp.riker.model.User;
import com.rikerapp.riker.model.UserSettings;
import com.rikerapp.riker.tasks.DeleteAllDataTask;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.parceler.Parcels;

import java.text.SimpleDateFormat;
import java.util.Date;

public final class SettingsActivity extends BaseActivity implements SimpleDialogFragment.Callbacks {

    public static final int REQUEST_CODE_EDIT_USER_SETTINGS = 1;

    // dialog request codes
    private static final int DIALOG_REQUESTCODE_DELETE_DATA_CONFIRM = 1;
    private static final int DIALOG_REQUESTCODE_ALL_DATA_DELETED_ACK = 2;

    private SwitchCompat googleFitSwitch;
    private SwitchCompat googleFitWorkoutsSwitch;
    private TextView setsSyncedToGoogleFitTextView;
    private Button syncSetsToGoogleFitButton;
    private SwitchCompat googleFitBodyWeightLogsSwitch;
    private TextView bodyWeightBmlsSyncedToGoogleFitTextView;

    @Subscribe
    public final void onMessageEvent(final AppEvent.BmlsExportCompleteEvent bmlsExportCompleteEvent) {
        handleExportCompleteEvent(bmlsExportCompleteEvent.exportTaskResult, AnalyticsEvent.BMLS_EXPORTED, "Body Measurement Log");
    }

    @Subscribe
    public final void onMessageEvent(final AppEvent.SetsExportCompleteEvent setsExportCompleteEvent) {
        handleExportCompleteEvent(setsExportCompleteEvent.exportTaskResult, AnalyticsEvent.SETS_EXPORTED, "Set");
    }

    @Subscribe
    public final void onMessageEvent(final AppEvent.DeleteAllDataCompleteEvent deleteAllDataCompleteEvent) {
        indicateEntitySavedOrDeleted();
        logEvent(AnalyticsEvent.ALL_DATA_DELETED);
        Utils.dismissProgressDialog(this);
        final SimpleDialogFragment simpleDialogFragment = SimpleDialogFragment.successInstance(
                DIALOG_REQUESTCODE_ALL_DATA_DELETED_ACK,
                "Data Deleted",
                String.format("<p>Your Riker sets, body measurement logs and settings have been deleted successfully.</p>"));
        showDialog(simpleDialogFragment, "dialog_fragment_delete_all_data_success");
    }

    @Override
    public final void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    protected final void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_EDIT_USER_SETTINGS:
                switch (resultCode) {
                    case Codes.RESULT_CODE_ENTITY_UPDATED:
                        final Parcelable userSettingsParcel = data.getParcelableExtra(CommonBundleKey.UserSettings.name());
                        final UserSettings userSettings = Parcels.unwrap(userSettingsParcel);
                        // delayed so the showing/removing of the 'sync needed' text is noticeable to the user
                        new Handler().postDelayed(() -> setProfileAndSettingsCaptionText(userSettings), 500);
                        break;
                }
        }
    }

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        setContentView(R.layout.activity_settings);
        configureAppBar();
        logScreen(getTitle());
        final Button profileAndSettingsButton = findViewById(R.id.profileAndSettingsButton);
        profileAndSettingsButton.setOnClickListener(view -> {
            final RikerApp rikerApp = (RikerApp)getApplication();
            final User user = rikerApp.dao.user();
            final UserSettings userSettings = rikerApp.dao.userSettings(user);
            startActivityForResult(ProfileViewDetailsActivity.makeIntent(this, userSettings), 0);
        });
        final RikerApp rikerApp = (RikerApp)getApplication();
        final User user = rikerApp.dao.user();
        final UserSettings userSettings = rikerApp.dao.userSettings(user);
        setProfileAndSettingsCaptionText(userSettings);
        final Button generalInfoButton = findViewById(R.id.generalInfoButton);
        generalInfoButton.setOnClickListener(view -> startActivity(new Intent(this, GeneralInfoActivity.class)));
        googleFitSwitch = findViewById(R.id.googleFitSwitch);
        this.googleFitWorkoutsSwitch = findViewById(R.id.googleFitWorkoutsSwitch);
        this.setsSyncedToGoogleFitTextView = findViewById(R.id.setsSyncedToGoogleFitTextView);
        this.syncSetsToGoogleFitButton = findViewById(R.id.syncSetsToGoogleFitButton);
        this.googleFitBodyWeightLogsSwitch = findViewById(R.id.googleFitBodyWeightLogsSwitch);
        this.bodyWeightBmlsSyncedToGoogleFitTextView = findViewById(R.id.bodyWeightBmlsSyncedToGoogleFitTextView);
        googleFitSwitch.setOnClickListener(view -> {
            if (googleFitSwitch.isChecked()) {
                syncSetsToGoogleFitButton.setEnabled(true);
                googleFitWorkoutsSwitch.setEnabled(true);
                googleFitWorkoutsSwitch.setChecked(true);
                googleFitBodyWeightLogsSwitch.setEnabled(true);
                googleFitBodyWeightLogsSwitch.setChecked(true);
                logEvent(AnalyticsEvent.ENABLE_GOOGLE_FIT);
                final Intent intent = new Intent(this, EnableGoogleFitActivity.class);
                intent.putExtra(EnableGoogleFitActivity.INTENTDATA_SHOULD_SYNC_BODY_WEIGHTS, true);
                intent.putExtra(EnableGoogleFitActivity.INTENTDATA_SHOULD_SYNC_WORKOUTS, true);
                startActivity(intent);
            } else {
                rikerApp.setGoogleFitEnabledAt(null);
                googleFitWorkoutsSwitch.setChecked(false);
                googleFitWorkoutsSwitch.setEnabled(false);
                syncSetsToGoogleFitButton.setEnabled(false);
                rikerApp.setGoogleFitWorkoutsDisabledAt(new Date());
                googleFitBodyWeightLogsSwitch.setChecked(false);
                googleFitBodyWeightLogsSwitch.setEnabled(false);
                rikerApp.setGoogleFitBodyWeightsDisabledAt(new Date());
            }
        });
        googleFitBodyWeightLogsSwitch.setOnClickListener(view -> {
            if (googleFitBodyWeightLogsSwitch.isChecked()) {
                final Intent intent = new Intent(this, EnableGoogleFitActivity.class);
                intent.putExtra(EnableGoogleFitActivity.INTENTDATA_SHOULD_SYNC_BODY_WEIGHTS, true);
                startActivity(intent);
            } else {
                rikerApp.setGoogleFitBodyWeightsDisabledAt(new Date());
                if (!googleFitWorkoutsSwitch.isChecked()) { // is workoutsTupleForDescendingSets switch ALSO off?
                    rikerApp.setGoogleFitEnabledAt(null);
                    googleFitSwitch.setChecked(false);
                    googleFitBodyWeightLogsSwitch.setEnabled(false);
                    googleFitWorkoutsSwitch.setEnabled(false);
                    syncSetsToGoogleFitButton.setEnabled(false);
                }
            }
        });
        googleFitWorkoutsSwitch.setOnClickListener(view -> {
            if (googleFitWorkoutsSwitch.isChecked()) {
                final Intent intent = new Intent(this, EnableGoogleFitActivity.class);
                intent.putExtra(EnableGoogleFitActivity.INTENTDATA_SHOULD_SYNC_WORKOUTS, true);
                startActivity(intent);
            } else {
                rikerApp.setGoogleFitWorkoutsDisabledAt(new Date());
                if (!googleFitBodyWeightLogsSwitch.isChecked()) { // is body weight switch ALSO off?
                    rikerApp.setGoogleFitEnabledAt(null);
                    googleFitSwitch.setChecked(false);
                    googleFitWorkoutsSwitch.setEnabled(false);
                    googleFitBodyWeightLogsSwitch.setEnabled(false);
                    syncSetsToGoogleFitButton.setEnabled(false);
                }
            }
        });
        final Button syncSetsToGoogleFitButton = findViewById(R.id.syncSetsToGoogleFitButton);
        syncSetsToGoogleFitButton.setOnClickListener(view -> {
            final Intent intent = new Intent(this, EnableGoogleFitActivity.class);
            intent.putExtra(EnableGoogleFitActivity.INTENTDATA_SHOULD_SYNC_WORKOUTS, true);
            startActivity(intent);
        });
        final Button exportSetsButton = findViewById(R.id.exportSetsButton);
        exportSetsButton.setOnClickListener(view -> {
            Utils.displayProgressDialog(this, "Exporting sets...");
            new ExportSetsTask((RikerApp)getApplication()).execute();
        });
        final Button exportBmlsButton = findViewById(R.id.exportBmlsButton);
        exportBmlsButton.setOnClickListener(view -> {
            Utils.displayProgressDialog(this, "Exporting body logs...");
            new ExportBmlsTask((RikerApp)getApplication()).execute();
        });
        final Button deleteAllDataButton = findViewById(R.id.deleteAllDataButton);
        deleteAllDataButton.setOnClickListener(view -> {
            final SimpleDialogFragment simpleDialogFragment = SimpleDialogFragment.confirmInstance(DIALOG_REQUESTCODE_DELETE_DATA_CONFIRM,
                    "Are you absolutely sure?",
                    "This will permanently delete your Riker data from this device and cannot be undone.",
                    "Delete My Data",
                    "Cancel");
            showDialog(simpleDialogFragment, "dialog_fragment_delete_all_data_confirm");
        });
        final Button reviewButton = findViewById(R.id.reviewButton);
        reviewButton.setOnClickListener(view -> rateNow());
        final Button shareButton = findViewById(R.id.shareButton);
        shareButton.setOnClickListener(view -> Utils.shareRiker(this, getApplication()));
        final Button viewSplashScreenButton = findViewById(R.id.viewSplashScreenButton);
        viewSplashScreenButton.setOnClickListener(view -> {
            final Intent intent = new Intent(this, SplashActivity.class);
            intent.putExtra(SplashActivity.INTENTDATA_SPLASH_AGAIN, true);
            startActivity(intent);
        });
        //final Button legalButton = findViewById(R.id.legalButton);
        //legalButton.setOnClickListener(view -> startActivity(new Intent(this, LegalActivity.class)));
        final TextView rikerVersionTextView = findViewById(R.id.rikerVersionTextView);
        rikerVersionTextView.setText(String.format("version: %s code: %s", BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE));
    }

    @Override
    public final void onResume() {
        super.onResume();
        updateGoogleFitUiElements();
    }

    private final void updateGoogleFitUiElements() {
        final RikerApp rikerApp = (RikerApp)getApplication();
        final Date googleFitEnabledAt = rikerApp.googleFitEnabledAt();
        final boolean googleFitEnabled = googleFitEnabledAt != null;
        googleFitSwitch.setChecked(googleFitEnabled);
        googleFitWorkoutsSwitch.setEnabled(googleFitEnabled);
        syncSetsToGoogleFitButton.setEnabled(googleFitEnabled);
        googleFitBodyWeightLogsSwitch.setEnabled(googleFitEnabled);
        if (googleFitEnabled) {
            googleFitWorkoutsSwitch.setChecked(rikerApp.googleFitWorkoutsDisabledAt() == null);
            googleFitBodyWeightLogsSwitch.setChecked(rikerApp.googleFitBodyWeightsDisabledAt() == null);
        } else {
            googleFitWorkoutsSwitch.setChecked(false);
            googleFitBodyWeightLogsSwitch.setChecked(false);
        }
        final Date googleFitLastBodyWeightEndDate = rikerApp.googleFitLastBodyWeightEndDate();
        if (googleFitLastBodyWeightEndDate != null) {
            final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Constants.DATE_FORMAT);
            bodyWeightBmlsSyncedToGoogleFitTextView.setText(
                    Utils.fromHtml(String.format("All body weights logged up to<br><strong>%s</strong> are synced to Google Fit.", simpleDateFormat.format(googleFitLastBodyWeightEndDate))));
        } else {
            bodyWeightBmlsSyncedToGoogleFitTextView.setText("No body weight logs synced to Google Fit yet.");
        }
        final Date googleFitLastWorkoutEndDate = rikerApp.googleFitLastWorkoutEndDate();
        if (googleFitLastWorkoutEndDate != null) {
            final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Constants.DATE_FORMAT);
            setsSyncedToGoogleFitTextView.setText(
                    Utils.fromHtml(String.format("All sets logged up to<br><strong>%s</strong> are processed.", simpleDateFormat.format(googleFitLastWorkoutEndDate))));
        } else {
            setsSyncedToGoogleFitTextView.setText("No sets synced to Google Fit yet.");
        }
    }

    private final void setProfileAndSettingsCaptionText(final UserSettings userSettings) {
        final TextView profileAndSettingsCaptionTextView = findViewById(R.id.profileAndSettingsCaptionTextView);
        final StringBuilder captionText = new StringBuilder("From here you can view and edit various defaults (e.g., weight units) and other information about yourself.");
        if (userSettings.globalIdentifier != null && !userSettings.synced) {
             captionText.append(String.format("&nbsp;&nbsp;<strong><font color=\"%s\">Sync needed</font></strong>.",
                    Utils.toSimpleHtmlColor(ContextCompat.getColor(this, R.color.bootstrapBlue))));
        }
        profileAndSettingsCaptionTextView.setText(Utils.fromHtml(captionText.toString()));
    }

    @Override
    public final void dialogPositiveClicked(final int requestCode) {
        switch (requestCode) {
            case DIALOG_REQUESTCODE_DELETE_DATA_CONFIRM:
                Utils.displayProgressDialog(this, "Deleting your Riker data...");
                new DeleteAllDataTask((RikerApp)getApplication()).execute();
                break;
            case DIALOG_REQUESTCODE_ALL_DATA_DELETED_ACK:
                final RikerApp rikerApp = (RikerApp)getApplication();
                rikerApp.setGoogleFitLastBodyWeightEndDate(null);
                rikerApp.setGoogleFitLastWorkoutEndDate(null);
                updateGoogleFitUiElements();
                break;
            default:
                super.dialogPositiveClicked(requestCode);
                break;
        }
    }

    @Override
    public final void dialogNegativeClicked(final int requestCode) {}

    @Override
    public final void dialogNeutralClicked(final int requestCode) {}
}
