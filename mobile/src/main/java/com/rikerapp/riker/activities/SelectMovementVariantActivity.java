package com.rikerapp.riker.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.Loader;

import com.rikerapp.riker.RikerApp;
import com.rikerapp.riker.model.BodySegment;
import com.rikerapp.riker.model.Movement;
import com.rikerapp.riker.model.MuscleGroup;
import com.rikerapp.riker.model.RikerDao;
import com.rikerapp.riker.loader.AsyncLoader;
import com.rikerapp.riker.model.MasterSupport;
import com.rikerapp.riker.model.MovementVariant;

import org.parceler.Parcels;

import java.util.List;

public final class SelectMovementVariantActivity extends ButtonSelectActivity {

    public static Intent makeIntent(final Context context,
                                    final BodySegment bodySegment,
                                    final MuscleGroup muscleGroup,
                                    final Movement movement,
                                    final boolean fromSearchResult) {
        final Intent intent = new Intent(context, SelectMovementVariantActivity.class);
        final Bundle bundle = new Bundle();
        if (bodySegment != null) { // if user came here from a movement search, bodySegment would be null
            bundle.putParcelable(CommonBundleKey.BodySegment.name(), Parcels.wrap(bodySegment));
        }
        if (muscleGroup != null) { // if user came here from a movement search, muscleGroup would be null
            bundle.putParcelable(CommonBundleKey.MuscleGroup.name(), Parcels.wrap(muscleGroup));
        }
        bundle.putParcelable(CommonBundleKey.Movement.name(), Parcels.wrap(movement));
        bundle.putBoolean(INTENTDATA_FROM_SEARCH_RESULT, fromSearchResult);
        intent.putExtras(bundle);
        return intent;
    }

    private final Movement selectedMovement() {
        return Parcels.unwrap(getIntent().getParcelableExtra(CommonBundleKey.Movement.name()));
    }

    @Override
    public final ButtonText buttonText() {
        return masterSupport -> ((MovementVariant)masterSupport).name;
    }

    @Override
    public final OnClick onClick() {
        final RikerApp rikerApp = (RikerApp)getApplication();
        return masterSupport -> startActivityForResult(EnterRepsActivity.makeIntent(
                this,
                selectedMovement(),
                (MovementVariant)masterSupport,
                rikerApp.setsMap), 0);
    }

    @Override
    public final Loader<List<? extends MasterSupport>> loader(final RikerApp rikerApp) {
        final Movement movement = selectedMovement();
        return new MovementVariantLoader(this, rikerApp.dao, movement.variantMask);
    }

    public final static class MovementVariantLoader extends AsyncLoader<List<? extends MasterSupport>> {

        private final RikerDao rikerDao;
        private final int movementVariantMask;

        public MovementVariantLoader(final Context context, final RikerDao rikerDao, final int movementVariantMask) {
            super(context);
            this.rikerDao = rikerDao;
            this.movementVariantMask = movementVariantMask;
        }

        @Override
        public final List<? extends MasterSupport> loadInBackground() {
            return rikerDao.movementVariants(this.movementVariantMask);
        }
    }
}
