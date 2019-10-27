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
import com.rikerapp.riker.model.Movement;
import com.rikerapp.riker.model.MovementVariant;
import com.rikerapp.riker.model.OriginationDevice;
import com.rikerapp.riker.model.Set;
import com.rikerapp.riker.model.WeightUnit;

import org.parceler.Parcels;

import java.text.SimpleDateFormat;
import java.util.Map;

public final class SetViewDetailsActivity extends BaseActivity implements SimpleDialogFragment.Callbacks {

    private static final int REQUEST_CODE_EDIT_SET = 1;

    // dialog request codes
    private static final int DIALOG_REQUESTCODE_DELETE_SET_CONFIRM = 1;

    private Map<Integer, Movement> allMovements;
    private Map<Integer, MovementVariant> allMovementVariants;
    private Set set;

    public static Intent makeIntent(final Context context,
                                    final Set set,
                                    final Map<Integer, Movement> allMovements,
                                    final Map<Integer, MovementVariant> allMovementVariants) {
        final Intent intent = new Intent(context, SetViewDetailsActivity.class);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(CommonBundleKey.Set.name(), Parcels.wrap(set));
        bundle.putParcelable(CommonBundleKey.Movements.name(), Parcels.wrap(allMovements));
        bundle.putParcelable(CommonBundleKey.MovementVariants.name(), Parcels.wrap(allMovementVariants));
        intent.putExtras(bundle);
        return intent;
    }

    @Override
    public final void onSaveInstanceState(final Bundle outState) {
        outState.putParcelable(CommonBundleKey.Set.name(), Parcels.wrap(this.set));
        super.onSaveInstanceState(outState);
    }

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_view_details);
        configureAppBar();
        logScreen(getTitle());
        set = Parcels.unwrap(getIntent().getParcelableExtra(CommonBundleKey.Set.name()));
        allMovements = Parcels.unwrap(getIntent().getParcelableExtra(CommonBundleKey.Movements.name()));
        allMovementVariants = Parcels.unwrap(getIntent().getParcelableExtra(CommonBundleKey.MovementVariants.name()));
        if (savedInstanceState != null) {
            set = Parcels.unwrap(savedInstanceState.getParcelable(CommonBundleKey.Set.name()));
        }
        bindSetToUi(set);
    }

    private final void bindSetToUi(final Set set) {
        final TextView loggedAtDateTimeTextView = (TextView)findViewById(R.id.loggedAtTextView);
        final String loggedAtFormat = set.ignoreTime ? Constants.DATE_FORMAT : Constants.DATE_TIME_FORMAT;
        loggedAtDateTimeTextView.setText(new SimpleDateFormat(loggedAtFormat).format(set.loggedAt));
        final TextView ignoreTimeTextView = (TextView)findViewById(R.id.ignoreTimeTextView);
        ignoreTimeTextView.setText(Utils.toYesNo(set.ignoreTime));
        final TextView movementTextView = (TextView)findViewById(R.id.movementTextView);
        movementTextView.setText(allMovements.get(set.movementId).canonicalName);
        final TextView movementVariantTextView = (TextView)findViewById(R.id.movementVariantTextView);
        if (set.movementVariantId != null) {
            movementVariantTextView.setText(allMovementVariants.get(set.movementVariantId).name);
        } else {
            movementVariantTextView.setText("---");
        }
        Utils.bindWeightSizeTextView(this, R.id.weightTextView, set.weight);
        final TextView weightUomTextView = (TextView)findViewById(R.id.weightUomTextView);
        weightUomTextView.setText(WeightUnit.weightUnitById(set.weightUom).name);
        final TextView repsTextView = (TextView)findViewById(R.id.repsTextView);
        repsTextView.setText(set.numReps.toString());
        final TextView toFailureTextView = (TextView)findViewById(R.id.toFailureTextView);
        toFailureTextView.setText(Utils.toYesNo(set.toFailure));
        final TextView negativesTextView = (TextView)findViewById(R.id.negativesTextView);
        negativesTextView.setText(Utils.toYesNo(set.negatives));
        final ImageView originationDeviceImageView = (ImageView)findViewById(R.id.originationDeviceImageView);
        final OriginationDevice.Id originationDeviceId = OriginationDevice.Id.originationDeviceIdById(set.originationDeviceId);
        originationDeviceImageView.setImageResource(originationDeviceId.drawableResource);
        final TextView originationDeviceTextView = (TextView)findViewById(R.id.originationDeviceTextView);
        originationDeviceTextView.setText(originationDeviceId.deviceName);
        final View importedAtContainer = findViewById(R.id.importedAtContainer);
        if (set.importedAt != null) {
            final TextView importedAtTextView = (TextView)findViewById(R.id.importedAtDateTimeTextView);
            importedAtTextView.setText(new SimpleDateFormat(Constants.DATE_TIME_FORMAT).format(set.importedAt));
        } else {
            importedAtContainer.setVisibility(View.GONE);
        }
    }

    @Override
    protected final void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_EDIT_SET:
                switch (resultCode) {
                    case Codes.RESULT_CODE_ENTITY_UPDATED:
                        final Parcelable setParcel = data.getParcelableExtra(CommonBundleKey.Set.name());
                        this.set = Parcels.unwrap(setParcel);
                        bindSetToUi(set);
                        setUpdatedSetResult(setParcel);
                        break;
                }
        }
    }

    private void setUpdatedSetResult(final Parcelable setParcel) {
        final Intent resultIntent = new Intent();
        final Bundle bundle = new Bundle();
        bundle.putParcelable(CommonBundleKey.Set.name(), setParcel);
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
            startActivityForResult(SetEditDetailsActivity.makeIntent(this, set, allMovements), REQUEST_CODE_EDIT_SET);
            return true;
        } else if (id == R.id.menu_action_view_entity_delete) {
            final SimpleDialogFragment simpleDialogFragment = SimpleDialogFragment.confirmInstance(DIALOG_REQUESTCODE_DELETE_SET_CONFIRM,
                    "Are you sure?",
                    "Are you sure you want to delete this set?",
                    "Yes",
                    "No");
            showDialog(simpleDialogFragment, "dialog_fragment_delete_set_confirm");
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public final void dialogPositiveClicked(final int requestCode) {
        switch (requestCode) {
            case DIALOG_REQUESTCODE_DELETE_SET_CONFIRM:
                final RikerApp rikerApp = (RikerApp)getApplication();
                rikerApp.dao.deleteSet(set);
                indicateEntitySavedOrDeleted();
                final Integer setId = set.localIdentifier;
                final Intent resultIntent = new Intent();
                resultIntent.putExtra(CommonBundleKey.SetId.name(), setId);
                setResult(Codes.RESULT_CODE_ENTITY_DELETED, resultIntent);
                finish();
                Toast.makeText(this, "Set deleted.", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public final void dialogNegativeClicked(final int requestCode) {}

    @Override
    public final void dialogNeutralClicked(final int requestCode) {}
}
