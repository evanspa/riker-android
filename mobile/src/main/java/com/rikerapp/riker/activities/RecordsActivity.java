package com.rikerapp.riker.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.widget.Button;
import android.widget.TextView;

import com.rikerapp.riker.AnalyticsEvent;
import com.rikerapp.riker.Constants;
import com.rikerapp.riker.R;
import com.rikerapp.riker.RikerApp;
import com.rikerapp.riker.Utils;
import com.rikerapp.riker.dialogfragments.SimpleDialogFragment;
import com.rikerapp.riker.eventbus.AppEvent;
import com.rikerapp.riker.importexport.BmlImportPrepResult;
import com.rikerapp.riker.importexport.BmlImportResult;
import com.rikerapp.riker.importexport.ExportBmlsTask;
import com.rikerapp.riker.importexport.ExportSetsTask;
import com.rikerapp.riker.importexport.ImportBmlsTask;
import com.rikerapp.riker.importexport.ImportError;
import com.rikerapp.riker.importexport.ImportPrepResult;
import com.rikerapp.riker.importexport.ImportResult;
import com.rikerapp.riker.importexport.ImportSetsTask;
import com.rikerapp.riker.importexport.PrepareBmlsImportTask;
import com.rikerapp.riker.importexport.PrepareSetsImportTask;
import com.rikerapp.riker.importexport.SetImportPrepResult;
import com.rikerapp.riker.importexport.SetImportResult;
import com.rikerapp.riker.loader.AsyncLoader;
import com.rikerapp.riker.model.MainSupport;
import com.rikerapp.riker.model.RikerDao;
import com.rikerapp.riker.model.User;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.parceler.Parcels;

import java.text.NumberFormat;
import java.util.List;

import timber.log.Timber;

public class RecordsActivity extends BaseActivity implements
        LoaderManager.LoaderCallbacks<RecordsActivity.FetchData>,
        SimpleDialogFragment.Callbacks {

    private static final int SET_FILE_CHOSEN_REQUEST_CODE = 18;
    private static final int BML_FILE_CHOSEN_REQUEST_CODE = 19;

    // dialog request codes
    private static final int DIALOG_REQUESTCODE_IMPORT_EXPORT_SET_INFO = 1;
    private static final int DIALOG_REQUESTCODE_IMPORT_EXPORT_BML_INFO = 2;
    private static final int DIALOG_REQUESTCODE_CONFIRM_SET_IMPORT = 3;
    private static final int DIALOG_REQUESTCODE_CONFIRM_BML_IMPORT = 4;
    private static final int DIALOG_REQUESTCODE_IMPORT_SUCCESS_ACK = 5;

    // local state keys
    private static final String LSTATE_RESOLUTION_ATTEMPTS = "LSTATE_RESOLUTION_ATTEMPTS";
    private static final String LSTATE_ACTIVE_ENTITY_IMPORT_REQUEST_CODE = "LSTATE_ACTIVE_ENTITY_IMPORT_REQUEST_CODE";
    private static final String LSTATE_ENTITIES_TO_IMPORT = "LSTATE_ENTITIES_TO_IMPORT";

    private TextView setCountButtonTextView;
    private TextView bmlCountButtonTextView;

    private int resolutionAttempts;
    private int activeEntityImportRequestCode;
    private List<? extends MainSupport> entitiesToImport;

    @Override
    public final void onSaveInstanceState(final Bundle outState) {
        outState.putInt(LSTATE_RESOLUTION_ATTEMPTS, resolutionAttempts);
        outState.putInt(LSTATE_ACTIVE_ENTITY_IMPORT_REQUEST_CODE, activeEntityImportRequestCode);
        outState.putParcelable(LSTATE_ENTITIES_TO_IMPORT, Parcels.wrap(entitiesToImport));
        super.onSaveInstanceState(outState);
    }

    @Subscribe
    public final void onMessageEvent(final AppEvent.BmlsExportCompleteEvent bmlsExportCompleteEvent) {
        handleExportCompleteEvent(bmlsExportCompleteEvent.exportTaskResult, AnalyticsEvent.BMLS_EXPORTED, "Body Measurement Log");
    }

    @Subscribe
    public final void onMessageEvent(final AppEvent.SetsExportCompleteEvent setsExportCompleteEvent) {
        handleExportCompleteEvent(setsExportCompleteEvent.exportTaskResult, AnalyticsEvent.SETS_EXPORTED, "Set");
    }

    @Subscribe
    public final void onMessageEvent(final SetImportPrepResult importPrepResult) {
        Utils.dismissProgressDialog(RecordsActivity.this);
        if (importPrepResult.throwable == null) {
            handleImportPrepEvent(importPrepResult, "set", DIALOG_REQUESTCODE_CONFIRM_SET_IMPORT);
        } else {
            final SimpleDialogFragment simpleDialogFragment = SimpleDialogFragment.errorInstance(null,
                    "Oops",
                    "There was a problem attempting to read the file.\n\nPlease try again later.");
            showDialog(simpleDialogFragment, "dialog_fragment_read_file_error");
        }
    }

    @Subscribe
    public final void onMessageEvent(final BmlImportPrepResult importPrepResult) {
        Utils.dismissProgressDialog(RecordsActivity.this);
        if (importPrepResult.throwable == null) {
            handleImportPrepEvent(importPrepResult, "body measurement log", DIALOG_REQUESTCODE_CONFIRM_BML_IMPORT);
        } else {
            final SimpleDialogFragment simpleDialogFragment = SimpleDialogFragment.errorInstance(null,
                    "Oops",
                    "There was a problem attempting to read the file.\n\nPlease try again later.");
            showDialog(simpleDialogFragment, "dialog_fragment_read_file_error");
        }
    }

    @Subscribe
    public final void onMessageEvent(final SetImportResult importResult) {
        handleImportResultEvent(importResult, "set", importResult.numEntitiesImported);
    }

    @Subscribe
    public final void onMessageEvent(final BmlImportResult importResult) {
        handleImportResultEvent(importResult, "body measurement log", importResult.numEntitiesImported);
    }

    @Override
    public final void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    protected final void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        Timber.d("inside onActivityResult [%d], result: [%d]", requestCode, resultCode);
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SET_FILE_CHOSEN_REQUEST_CODE:
                if (resultCode == RecordsActivity.RESULT_OK) {
                    Utils.displayProgressDialog(this, "Processing file...");
                    new PrepareSetsImportTask((RikerApp)getApplication(), data.getData()).execute();
                }
                break;
            case BML_FILE_CHOSEN_REQUEST_CODE:
                if (resultCode == RecordsActivity.RESULT_OK) {
                    Utils.displayProgressDialog(this, "Processing file...");
                    new PrepareBmlsImportTask((RikerApp)getApplication(), data.getData()).execute();
                }
                break;
            default:
                switch (resultCode) {
                    case Codes.RESULT_CODE_ENTITY_DELETED:
                    case Codes.RESULT_CODE_ENTITY_UPDATED:
                    case Codes.RESULT_CODE_ENTITY_ADDED:
                        reloadCounts();
                        break;
                }
        }
    }

    private final void handleImportResultEvent(final ImportResult importResult, final String entityType, final int numImported) {
        Utils.dismissProgressDialog(this);
        entitiesToImport = null;
        if (importResult.error != null) {
            final StringBuilder errMessageStringBuilder = new StringBuilder();
            errMessageStringBuilder.append(String.format("<p>There was a problem saving at least one of your %ss from the import file.</p><p>The error is:</p><ul><li>&nbsp;&nbsp;%s</li></ul>",
                    entityType,
                    ExceptionUtils.getRootCauseMessage(importResult.error)));
            errMessageStringBuilder.append(String.format("<p><strong>The import has been aborted.</strong>&nbsp;&nbsp;None of the %ss from your import file have been saved.</p>", entityType));
            final SimpleDialogFragment simpleDialogFragment = SimpleDialogFragment.errorInstance(null,
                    "Oops",
                    errMessageStringBuilder.toString());
            showDialog(simpleDialogFragment, "dialog_fragment_import_error");
        } else {
            indicateEntitySavedOrDeleted();
            final SimpleDialogFragment simpleDialogFragment = SimpleDialogFragment.successInstance(DIALOG_REQUESTCODE_IMPORT_SUCCESS_ACK,
                    "Import Successful",
                    String.format("Your <strong>%s %s%s</strong> %s been imported successfully.",
                            NumberFormat.getInstance().format(numImported),
                            entityType,
                            numImported > 1 ? "s" : "",
                            numImported > 1 ? "have" : "has"));
            showDialog(simpleDialogFragment, "dialog_fragment_import_success");
        }
    }

    private final void handleImportPrepEvent(final ImportPrepResult importPrepResult, final String entityType, final int dialogRequestCodeConfirmImport) {
        final int numErrors = importPrepResult.errors.size();
        if (numErrors > 0) {
            final boolean maxErrorsExceeded = numErrors > Constants.MAX_IMPORT_ERRORS_RECORDED;
            final StringBuilder errMsgStringBuilder = new StringBuilder();
            errMsgStringBuilder.append(String.format("<p>There %s with the file you selected, <b>%s</b>.</p><p>%s</p>",
                    numErrors > 1 ? "are problems" : "is a problem",
                    importPrepResult.fileName,
                    maxErrorsExceeded ? String.format("There are %d errors.  Some of them are:", numErrors) : numErrors > 1 ? "The errors are:" : "The error is:"));
            errMsgStringBuilder.append("<ul>");
            int numErrorsToDisplay = numErrors;
            if (maxErrorsExceeded) {
                numErrorsToDisplay = Constants.MAX_IMPORT_ERRORS_RECORDED;
            }
            for (int i = 0; i < numErrorsToDisplay; i++) {
                final ImportError importError = importPrepResult.errors.get(i);
                if (importError.recordNumber != null) {
                    errMsgStringBuilder.append(String.format("<li>&nbsp;&nbsp;<b>On line %d: </b>%s</li>", importError.recordNumber, importError.message));
                } else {
                    errMsgStringBuilder.append(String.format("<li>&nbsp;&nbsp;%s</li>", importError.message));
                }
            }
            errMsgStringBuilder.append("</ul>");
            if (importPrepResult.anyReferenceErrors) {
                errMsgStringBuilder.append("<p>It looks like the import file references movements or other Riker internals that do not exist on your device.  Please upgrade Riker to the latest version from the Play Store, and try this again.</p>");
            }
            final SimpleDialogFragment simpleDialogFragment = SimpleDialogFragment.errorInstance(null,
                    "Oops",
                    errMsgStringBuilder.toString());
            showDialog(simpleDialogFragment, "dialog_fragment_import_error");
        } else {
            entitiesToImport = importPrepResult.entitiesToImport;
            final int numEntitiesToImport = importPrepResult.entitiesToImport.size();
            final String sOrBlank = numEntitiesToImport > 1 ? "s" : "";
            final SimpleDialogFragment simpleDialogFragment = SimpleDialogFragment.confirmInstance(dialogRequestCodeConfirmImport,
                    "Confirm Import",
                    String.format("<p>The file you selected, <b>%s</b>, contains <b>%s %s record%s</b>.</p><p>Are you sure you want to import %s record%s?</p>",
                            importPrepResult.fileName,
                            NumberFormat.getInstance().format(numEntitiesToImport),
                            entityType,
                            sOrBlank,
                            numEntitiesToImport > 1 ? "these" : "the",
                            sOrBlank),
                    "Do Import",
                    "Cancel");
            showDialog(simpleDialogFragment, "dialog_fragment_import_confirm");
        }
    }

    private final void importEntitiesFromFile(final int requestCode) {
        this.activeEntityImportRequestCode = requestCode;
        final Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv");
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        setContentView(R.layout.activity_records);
        configureAppBar();
        logScreen(getTitle());
        configureFloatingActionsMenu();
        if (savedInstanceState != null) {
            this.resolutionAttempts = savedInstanceState.getInt(LSTATE_RESOLUTION_ATTEMPTS);
            this.activeEntityImportRequestCode = savedInstanceState.getInt(LSTATE_ACTIVE_ENTITY_IMPORT_REQUEST_CODE);
            this.entitiesToImport = Parcels.unwrap(savedInstanceState.getParcelable(LSTATE_ENTITIES_TO_IMPORT));
        }
        this.setCountButtonTextView = findViewById(R.id.setCountButtonTextView);
        final Button setsButton = findViewById(R.id.setsButton);
        setsButton.setOnClickListener(view -> {
            startActivityForResult(new Intent(this, SetsActivity.class), 0);
            collapseFabMenuDelayed();
        });
        this.setCountButtonTextView.setText("Calculating set count...");
        final Button setsImportButton = findViewById(R.id.setsImportButton);
        setsImportButton.setOnClickListener(view -> {
            importEntitiesFromFile(SET_FILE_CHOSEN_REQUEST_CODE);
            collapseFabMenuDelayed();
        });
        final Button setsExportButton = findViewById(R.id.setsExportButton);
        setsExportButton.setOnClickListener(view -> {
            Utils.displayProgressDialog(this, "Exporting sets...");
            new ExportSetsTask((RikerApp)getApplication()).execute();
            collapseFabMenuDelayed();
        });
        final Button setFilesButton = findViewById(R.id.setFilesButton);
        setFilesButton.setOnClickListener(view -> {
            startActivity(new Intent(this, SetExportFilesActivity.class));
            collapseFabMenuDelayed();
        });
        final Button setsImportExportInfoButton = findViewById(R.id.setsImportExportInfoButton);
        setsImportExportInfoButton.setOnClickListener(view -> {
            final SimpleDialogFragment simpleDialogFragment = SimpleDialogFragment.infoInstance(
                    DIALOG_REQUESTCODE_IMPORT_EXPORT_SET_INFO,
                    getResources().getString(R.string.activity_records_import_export_info_title),
                    getResources().getString(R.string.activity_set_records_import_export_info_msg));
            showDialog(simpleDialogFragment, "dialog_fragment_export_set_info");
        });
        this.bmlCountButtonTextView = (TextView)findViewById(R.id.bmlCountButtonTextView);
        final Button bmlsButton = (Button)findViewById(R.id.bmlsButton);
        bmlsButton.setOnClickListener(view -> {
            startActivityForResult(new Intent(this, BmlsActivity.class), 0);
            collapseFabMenuDelayed();
        });
        this.bmlCountButtonTextView.setText("Calculating body measurement log count...");
        final Button bmlsImportButton = (Button)findViewById(R.id.bmlsImportButton);
        bmlsImportButton.setOnClickListener(view -> {
            importEntitiesFromFile(BML_FILE_CHOSEN_REQUEST_CODE);
            collapseFabMenuDelayed();
        });
        final Button bmlsExportButton = (Button)findViewById(R.id.bmlsExportButton);
        bmlsExportButton.setOnClickListener(view -> {
            Utils.displayProgressDialog(this, "Exporting body measurement logs...");
            new ExportBmlsTask((RikerApp)getApplication()).execute();
            collapseFabMenuDelayed();
        });
        final Button bmlFilesButton = (Button)findViewById(R.id.bmlFilesButton);
        bmlFilesButton.setOnClickListener(view -> {
            startActivity(new Intent(this, BmlExportFilesActivity.class));
            collapseFabMenuDelayed();
        });
        final Button bmlsImportExportInfoButton = (Button)findViewById(R.id.bmlsImportExportInfoButton);
        bmlsImportExportInfoButton.setOnClickListener(view -> {
            final SimpleDialogFragment simpleDialogFragment = SimpleDialogFragment.infoInstance(
                    DIALOG_REQUESTCODE_IMPORT_EXPORT_BML_INFO,
                    getResources().getString(R.string.activity_records_import_export_info_title),
                    getResources().getString(R.string.activity_bml_records_import_export_info_msg));
            showDialog(simpleDialogFragment, "dialog_fragment_export_bml_info");
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSupportLoaderManager().initLoader(0, null, this);
    }

    private final void reloadCounts() {
        this.setCountButtonTextView.setText("Re-calculating set count...");
        this.bmlCountButtonTextView.setText("Re-calculating body measurement log count...");
        getSupportLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public final Loader<FetchData> onCreateLoader(final int id, final Bundle args) {
        final RikerApp rikerApp = (RikerApp)getApplication();
        return new FetchDataLoader(this, rikerApp.dao);
    }

    @Override
    public final void onLoadFinished(final Loader<FetchData> loader, final FetchData fetchData) {
        setCountButtonTextView.setText(String.format("You currently have %s set %s.",
                NumberFormat.getInstance().format(fetchData.setCount),
                Utils.pluralize("record", fetchData.setCount)));
        bmlCountButtonTextView.setText(String.format("You currently have %s body measurement %s.",
                NumberFormat.getInstance().format(fetchData.bmlCount),
                Utils.pluralize("log", fetchData.bmlCount)));
    }

    @Override
    public final void onLoaderReset(final Loader<FetchData> loader) { }

    final static class FetchData {
        public final int setCount;
        public final int bmlCount;

        public FetchData(final int setCount, final int bmlCount) {
            this.setCount = setCount;
            this.bmlCount = bmlCount;
        }
    }

    private final static class FetchDataLoader extends AsyncLoader<FetchData> {

        private final RikerDao rikerDao;

        public FetchDataLoader(final Context context, final RikerDao rikerDao) {
            super(context);
            this.rikerDao = rikerDao;
        }

        @Override
        public final FetchData loadInBackground() {
            final User user = rikerDao.user();
            return new FetchData(rikerDao.numSets(user), rikerDao.numBmls(user));
        }
    }

    @Override
    public final void dialogPositiveClicked(final int requestCode) {
        final RikerApp rikerApp = (RikerApp)getApplication();
        switch (requestCode) {
            case DIALOG_REQUESTCODE_IMPORT_EXPORT_SET_INFO:
                logHelpInfoPopupContentViewed("import_export_set_info");
                break;
            case DIALOG_REQUESTCODE_IMPORT_EXPORT_BML_INFO:
                logHelpInfoPopupContentViewed("import_export_bml_info");
                break;
            case DIALOG_REQUESTCODE_CONFIRM_SET_IMPORT:
                Utils.displayProgressDialog(this, "Importing sets...");
                new ImportSetsTask(entitiesToImport, rikerApp.dao).execute();
                break;
            case DIALOG_REQUESTCODE_CONFIRM_BML_IMPORT:
                Utils.displayProgressDialog(this, "Importing body measurement logs...");
                new ImportBmlsTask(entitiesToImport, rikerApp.dao).execute();
                break;
            case DIALOG_REQUESTCODE_IMPORT_SUCCESS_ACK:
                reloadCounts();
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
