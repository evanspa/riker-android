package com.rikerapp.riker.activities;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.content.Loader;
import android.view.View;

import com.rikerapp.riker.R;
import com.rikerapp.riker.RikerApp;
import com.rikerapp.riker.model.BodySegment;
import com.rikerapp.riker.model.MuscleGroup;
import com.rikerapp.riker.model.RikerDao;
import com.rikerapp.riker.loader.AsyncLoader;
import com.rikerapp.riker.model.MasterSupport;
import com.rikerapp.riker.model.Movement;

import org.parceler.Parcels;

import java.util.List;

public final class SelectMovementActivity extends ButtonSelectActivity {

    public static Intent makeIntent(final Context context,
                                    final BodySegment bodySegment,
                                    final MuscleGroup muscleGroup) {
        final Intent intent = new Intent(context, SelectMovementActivity.class);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(CommonBundleKey.BodySegment.name(), Parcels.wrap(bodySegment));
        bundle.putParcelable(CommonBundleKey.MuscleGroup.name(), Parcels.wrap(muscleGroup));
        intent.putExtras(bundle);
        return intent;
    }

    @Override
    public final ButtonText buttonText() {
        return masterSupport -> ((Movement)masterSupport).canonicalName;
    }

    @Override
    public final OnClick onClick() {
        return masterSupport -> {
            final Movement movement = (Movement) masterSupport;
            if (movement.variantMask != null && movement.variantMask != 0) {
                startActivityForResult(SelectMovementVariantActivity.makeIntent(
                        this,
                        Parcels.unwrap(getIntent().getParcelableExtra(CommonBundleKey.BodySegment.name())),
                        Parcels.unwrap(getIntent().getParcelableExtra(CommonBundleKey.MuscleGroup.name())),
                        movement,
                        false), 0);
            } else {
                final RikerApp rikerApp = (RikerApp)getApplication();
                startActivityForResult(EnterRepsActivity.makeIntent(
                        this,
                        movement,
                        null,
                        rikerApp.setsMap), 0);
            }
        };
    }

    @Override
    @LayoutRes
    public final int footerResource() { return R.layout.movement_select_footer; }

    @Override
    public final Loader<List<? extends MasterSupport>> loader(final RikerApp rikerApp) {
        final MuscleGroup selectedMuscleGroup = Parcels.unwrap(getIntent().getParcelableExtra(CommonBundleKey.MuscleGroup.name()));
        return new MovementLoader(this, rikerApp.dao, selectedMuscleGroup.localIdentifier);
    }

    public final void supportEmailOnClick(final View view) {
        final Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.putExtra(Intent.EXTRA_SUBJECT, "Missing or incorrect movementsWithNullMuscleIds");
        intent.setData(Uri.parse(String.format("mailto: %s", getString(R.string.riker_support_email))));
        startActivity(Intent.createChooser(intent, "Provide feedback"));
    }

    public final static class MovementLoader extends AsyncLoader<List<? extends MasterSupport>> {

        private final RikerDao rikerDao;
        private final int muscleGroupId;

        public MovementLoader(final Context context, final RikerDao rikerDao, final int muscleGroupId) {
            super(context);
            this.rikerDao = rikerDao;
            this.muscleGroupId = muscleGroupId;
        }

        @Override
        public final List<? extends MasterSupport> loadInBackground() {
            return rikerDao.movementsWithNullMuscleIds(this.muscleGroupId);
        }
    }
}
