package com.rikerapp.riker.activities;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.Loader;

import com.rikerapp.riker.RikerApp;
import com.rikerapp.riker.model.RikerDao;
import com.rikerapp.riker.loader.AsyncLoader;
import com.rikerapp.riker.model.BodySegment;
import com.rikerapp.riker.model.MasterSupport;

import java.util.List;

public final class SelectBodySegmentActivity extends ButtonSelectActivity {

    @Override
    public final ButtonText buttonText() {
        return masterSupport -> ((BodySegment)masterSupport).name;
    }

    @Override
    public final OnClick onClick() {
        return masterSupport -> startActivityForResult(SelectMuscleGroupActivity.makeIntent(this, (BodySegment)masterSupport), 0);
    }

    @Override
    public final Loader<List<? extends MasterSupport>> loader(final RikerApp rikerApp) {
        return new BodySegmentLoader(this, rikerApp.dao);
    }

    public final static class BodySegmentLoader extends AsyncLoader<List<? extends MasterSupport>> {

        private final RikerDao rikerDao;

        public BodySegmentLoader(final Context context, final RikerDao rikerDao) {
            super(context);
            this.rikerDao = rikerDao;
        }

        @Override
        public final List<? extends MasterSupport> loadInBackground() {
            return rikerDao.bodySegments();
        }
    }
}
