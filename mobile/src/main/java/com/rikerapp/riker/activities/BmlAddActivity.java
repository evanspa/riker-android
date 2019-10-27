package com.rikerapp.riker.activities;

import com.rikerapp.riker.AnalyticsEvent;
import com.rikerapp.riker.RikerApp;
import com.rikerapp.riker.dialogfragments.SimpleDialogFragment;
import com.rikerapp.riker.model.BodyMeasurementLog;
import com.rikerapp.riker.model.OriginationDevice;
import com.rikerapp.riker.model.User;
import com.rikerapp.riker.model.UserSettings;

import java.util.Date;

public final class BmlAddActivity extends BmlEditDetailsActivity implements SimpleDialogFragment.Callbacks {

    // dialog request codes
    private static final int DIALOG_REQUESTCODE_BML_SAVED_SUCCESS = 1;

    @Override
    public final BodyMeasurementLog bml() {
        final RikerApp rikerApp = (RikerApp) getApplication();
        final User user = rikerApp.dao.user();
        final UserSettings userSettings = rikerApp.dao.userSettings(user);
        final BodyMeasurementLog bml = new BodyMeasurementLog();
        bml.loggedAt = new Date();
        bml.sizeUom = userSettings.sizeUom;
        bml.bodyWeightUom = userSettings.weightUom;
        bml.originationDeviceId = OriginationDevice.Id.ANDROID.id;
        return bml;
    }

    @Override
    public final void cancel() {
        finish();
    }

    @Override
    public final void saveAndDone(final User user, final BodyMeasurementLog bml) {
        final RikerApp rikerApp = (RikerApp)getApplication();
        rikerApp.dao.saveNewBml(user, bml);
        indicateEntitySavedOrDeleted();
        if (offlineMode()) {

        } else if (isUserLoggedIn() && !doesUserHaveValidAuthToken()) {

        } else if (isUserLoggedIn() && user.isBadAccount()) {

        } else {
            logEvent(AnalyticsEvent.BML_SAVED_LOCAL_WHILE_ANONYMOUS);
            final SimpleDialogFragment simpleDialogFragment = SimpleDialogFragment.successInstance(
                    DIALOG_REQUESTCODE_BML_SAVED_SUCCESS,
                    "Body Log Saved",
                    "Your body measurement log has been saved.");
            showDialog(simpleDialogFragment, "dialog_fragment_bml_saved_success");
        }
    }

    @Override
    public final void dialogPositiveClicked(final int requestCode) {
        switch (requestCode) {
            case DIALOG_REQUESTCODE_BML_SAVED_SUCCESS:
                finish();
                break;
        }
    }

    @Override
    public final void dialogNegativeClicked(final int requestCode) {}

    @Override
    public final void dialogNeutralClicked(final int requestCode) {}
}
