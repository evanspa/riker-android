package com.rikerapp.riker.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.Loader;

import com.rikerapp.riker.RikerApp;
import com.rikerapp.riker.model.RikerDao;
import com.rikerapp.riker.loader.AsyncLoader;
import com.rikerapp.riker.model.BodySegment;
import com.rikerapp.riker.model.MasterSupport;
import com.rikerapp.riker.model.MuscleGroup;

import org.parceler.Parcels;

import java.util.List;

public final class SelectMuscleGroupActivity extends ButtonSelectActivity {

    public static Intent makeIntent(final Context context, final BodySegment bodySegment) {
        final Intent intent = new Intent(context, SelectMuscleGroupActivity.class);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(CommonBundleKey.BodySegment.name(), Parcels.wrap(bodySegment));
        intent.putExtras(bundle);
        return intent;
    }

    @Override
    public final ButtonText buttonText() {
        return masterSupport -> ((MuscleGroup)masterSupport).name;
    }

    @Override
    public final OnClick onClick() {
        return masterSupport -> startActivityForResult(SelectMovementActivity.makeIntent(
                this,
                Parcels.unwrap(getIntent().getParcelableExtra(CommonBundleKey.BodySegment.name())),
                (MuscleGroup)masterSupport), 0);
    }

    @Override
    public final Loader<List<? extends MasterSupport>> loader(final RikerApp rikerApp) {
        return new MuscleGroupLoader(this, rikerApp.dao, Parcels.unwrap(getIntent().getParcelableExtra(CommonBundleKey.BodySegment.name())));
    }

    public final static class MuscleGroupLoader extends AsyncLoader<List<? extends MasterSupport>> {

        private final RikerDao rikerDao;
        private final BodySegment bodySegment;

        public MuscleGroupLoader(final Context context, final RikerDao rikerDao, final BodySegment bodySegment) {
            super(context);
            this.rikerDao = rikerDao;
            this.bodySegment = bodySegment;
        }

        @Override
        public final List<? extends MasterSupport> loadInBackground() {
            return rikerDao.muscleGroups(this.bodySegment.localIdentifier);
        }
    }
}
