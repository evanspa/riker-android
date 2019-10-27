package com.rikerapp.riker.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.rikerapp.riker.Constants;
import com.rikerapp.riker.R;
import com.rikerapp.riker.RikerApp;
import com.rikerapp.riker.Utils;
import com.rikerapp.riker.dialogfragments.SimpleDialogFragment;
import com.rikerapp.riker.model.BodyMeasurementLog;
import com.rikerapp.riker.model.OriginationDevice;
import com.rikerapp.riker.model.SizeUnit;
import com.rikerapp.riker.model.WeightUnit;

import org.parceler.Parcels;

import java.text.SimpleDateFormat;

public final class BmlViewDetailsActivity extends BaseActivity implements SimpleDialogFragment.Callbacks {

    private static final int REQUEST_CODE_EDIT_BML = 1;

    // dialog request codes
    private static final int DIALOG_REQUESTCODE_DELETE_BML_CONFIRM = 1;

    private BodyMeasurementLog bml;

    public static Intent makeIntent(final Context context, final BodyMeasurementLog bml) {
        final Intent intent = new Intent(context, BmlViewDetailsActivity.class);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(CommonBundleKey.Bml.name(), Parcels.wrap(bml));
        intent.putExtras(bundle);
        return intent;
    }

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bml_view_details);
        configureAppBar();
        logScreen(getTitle());
        bml = Parcels.unwrap(getIntent().getParcelableExtra(CommonBundleKey.Bml.name()));
        if (savedInstanceState != null) {
            bml = Parcels.unwrap(savedInstanceState.getParcelable(CommonBundleKey.Bml.name()));
        }
        bindSetToUi(bml);
    }

    @Override
    public final void onSaveInstanceState(final Bundle outState) {
        outState.putParcelable(CommonBundleKey.Bml.name(), Parcels.wrap(this.bml));
        super.onSaveInstanceState(outState);
    }

    private final void bindSetToUi(final BodyMeasurementLog bml) {
        final TextView loggedAtDateTimeTextView = (TextView)findViewById(R.id.loggedAtTextView);
        final String loggedAtFormat = Constants.DATE_FORMAT;
        loggedAtDateTimeTextView.setText(new SimpleDateFormat(loggedAtFormat).format(bml.loggedAt));
        final TextView weightUomTextView = (TextView)findViewById(R.id.weightUomTextView);
        weightUomTextView.setText(WeightUnit.weightUnitById(bml.bodyWeightUom).name);
        Utils.bindWeightSizeTextView(this, R.id.bodyWeightTextView, bml.bodyWeight);
        final TextView sizeUomTextView = (TextView)findViewById(R.id.sizeUomTextView);
        sizeUomTextView.setText(SizeUnit.sizeUnitById(bml.sizeUom).name);
        Utils.bindWeightSizeTextView(this, R.id.armSizeTextView, bml.armSize);
        Utils.bindWeightSizeTextView(this, R.id.chestSizeTextView, bml.chestSize);
        Utils.bindWeightSizeTextView(this, R.id.calfSizeTextView, bml.calfSize);
        Utils.bindWeightSizeTextView(this, R.id.thighSizeTextView, bml.thighSize);
        Utils.bindWeightSizeTextView(this, R.id.forearmSizeTextView, bml.forearmSize);
        Utils.bindWeightSizeTextView(this, R.id.waistSizeTextView, bml.waistSize);
        Utils.bindWeightSizeTextView(this, R.id.neckSizeTextView, bml.neckSize);
        final ImageView originationDeviceImageView = (ImageView)findViewById(R.id.originationDeviceImageView);
        final OriginationDevice.Id originationDeviceId = OriginationDevice.Id.originationDeviceIdById(bml.originationDeviceId);
        originationDeviceImageView.setImageResource(originationDeviceId.drawableResource);
        final TextView originationDeviceTextView = (TextView)findViewById(R.id.originationDeviceTextView);
        originationDeviceTextView.setText(originationDeviceId.deviceName);
        final View importedAtContainer = findViewById(R.id.importedAtContainer);
        if (bml.importedAt != null) {
            final TextView importedAtTextView = (TextView)findViewById(R.id.importedAtDateTimeTextView);
            importedAtTextView.setText(new SimpleDateFormat(Constants.DATE_TIME_FORMAT).format(bml.importedAt));
        } else {
            importedAtContainer.setVisibility(View.GONE);
        }
    }

    @Override
    protected final void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_EDIT_BML:
                switch (resultCode) {
                    case Codes.RESULT_CODE_ENTITY_UPDATED:
                        final Parcelable bmlParcel = data.getParcelableExtra(CommonBundleKey.Bml.name());
                        this.bml = Parcels.unwrap(bmlParcel);
                        bindSetToUi(bml);
                        setUpdatedBmlResult(bmlParcel);
                        break;
                }
        }
    }

    private void setUpdatedBmlResult(final Parcelable bmlParcel) {
        final Intent resultIntent = new Intent();
        final Bundle bundle = new Bundle();
        bundle.putParcelable(CommonBundleKey.Bml.name(), bmlParcel);
        resultIntent.putExtras(bundle);
        setResult(Codes.RESULT_CODE_ENTITY_UPDATED, resultIntent);
    }

    @Override
    public final boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_entity_view_details, menu);
        return true;
    }

    @Override
    public final boolean onOptionsItemSelected(final MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        final int id = item.getItemId();
        if (id == R.id.menu_action_view_entity_edit) {
            startActivityForResult(BmlEditDetailsActivity.makeIntent(this, bml), REQUEST_CODE_EDIT_BML);
            return true;
        } else if (id == R.id.menu_action_view_entity_delete) {
            final SimpleDialogFragment simpleDialogFragment = SimpleDialogFragment.confirmInstance(DIALOG_REQUESTCODE_DELETE_BML_CONFIRM,
                    "Are you sure?",
                    "Are you sure you want to delete this body measurement log?",
                    "Yes",
                    "No");
            showDialog(simpleDialogFragment, "dialog_fragment_delete_set_confirm");
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public final void dialogPositiveClicked(final int requestCode) {
        switch (requestCode) {
            case DIALOG_REQUESTCODE_DELETE_BML_CONFIRM:
                final RikerApp rikerApp = (RikerApp)getApplication();
                rikerApp.dao.deleteBml(bml);
                indicateEntitySavedOrDeleted();
                final Integer bmlId = bml.localIdentifier;
                final Intent resultIntent = new Intent();
                resultIntent.putExtra(CommonBundleKey.BmlId.name(), bmlId);
                setResult(Codes.RESULT_CODE_ENTITY_DELETED, resultIntent);
                finish();
                Toast.makeText(this, "Body log deleted.", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public final void dialogNegativeClicked(final int requestCode) {}

    @Override
    public final void dialogNeutralClicked(final int requestCode) {}
}
