package com.rikerapp.riker.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.rikerapp.riker.R;
import com.rikerapp.riker.RikerApp;
import com.rikerapp.riker.model.SizeUnit;
import com.rikerapp.riker.model.User;
import com.rikerapp.riker.model.UserSettings;
import com.rikerapp.riker.model.WeightUnit;

import org.parceler.Parcels;

import static com.rikerapp.riker.activities.SettingsActivity.REQUEST_CODE_EDIT_USER_SETTINGS;

public final class ProfileViewDetailsActivity extends BaseActivity {

    private UserSettings userSettings;

    public static Intent makeIntent(final Context context, final UserSettings userSettings) {
        final Intent intent = new Intent(context, ProfileViewDetailsActivity.class);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(CommonBundleKey.UserSettings.name(), Parcels.wrap(userSettings));
        intent.putExtras(bundle);
        return intent;
    }

    @Override
    public final void onSaveInstanceState(final Bundle outState) {
        outState.putParcelable(CommonBundleKey.UserSettings.name(), Parcels.wrap(this.userSettings));
        super.onSaveInstanceState(outState);
    }

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_view_details);
        configureAppBar();
        logScreen(getTitle());
        userSettings = Parcels.unwrap(getIntent().getParcelableExtra(CommonBundleKey.UserSettings.name()));
        if (savedInstanceState != null) {
            userSettings = Parcels.unwrap(savedInstanceState.getParcelable(CommonBundleKey.UserSettings.name()));
        }
        bindProfileToUi(userSettings);
    }

    @Override
    protected final void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_EDIT_USER_SETTINGS:
                switch (resultCode) {
                    case Codes.RESULT_CODE_ENTITY_UPDATED:
                        final Parcelable userSettingsParcel = data.getParcelableExtra(CommonBundleKey.UserSettings.name());
                        this.userSettings = Parcels.unwrap(userSettingsParcel);
                        bindProfileToUi(userSettings);
                        setUpdatedUserSettingsResult(userSettingsParcel);
                        break;
                }
        }
    }

    private void setUpdatedUserSettingsResult(final Parcelable userSettingsParcel) {
        final Intent resultIntent = new Intent();
        final Bundle bundle = new Bundle();
        bundle.putParcelable(CommonBundleKey.UserSettings.name(), userSettingsParcel);
        resultIntent.putExtras(bundle);
        setResult(Codes.RESULT_CODE_ENTITY_UPDATED, resultIntent);
    }

    @Override
    public final boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user_settings_view_details, menu);
        return true;
    }

    private final void bindProfileToUi(final UserSettings userSettings) {
        final TextView weightAdjustTextView = (TextView)findViewById(R.id.weightAdjustTextView);
        weightAdjustTextView.setText(userSettings.weightIncDecAmount.toString());
        final TextView defaultWeightUnitsTextView = (TextView)findViewById(R.id.defaultWeightUnitsTextView);
        final WeightUnit weightUnit = WeightUnit.weightUnitById(userSettings.weightUom);
        defaultWeightUnitsTextView.setText(weightUnit.name);
        final TextView defaultSizeUnitsTextView = (TextView)findViewById(R.id.defaultSizeUnitsTextView);
        final SizeUnit sizeUnit = SizeUnit.sizeUnitById(userSettings.sizeUom);
        defaultSizeUnitsTextView.setText(sizeUnit.name);
    }

    @Override
    public final boolean onOptionsItemSelected(final MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        final int id = item.getItemId();
        if (id == R.id.menu_action_view_entity_edit) {
            startActivityForResult(ProfileEditDetailsActivity.makeIntent(this, userSettings), REQUEST_CODE_EDIT_USER_SETTINGS);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
