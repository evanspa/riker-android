package com.rikerapp.riker.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.rikerapp.riker.R;
import com.rikerapp.riker.RikerApp;
import com.rikerapp.riker.Utils;
import com.rikerapp.riker.adapters.MuscleGroupsAndMovementsAdapter;
import com.rikerapp.riker.model.RikerDao;
import com.rikerapp.riker.loader.AsyncLoader;
import com.rikerapp.riker.model.Movement;

import java.util.List;

public final class MuscleGroupsAndMovementsActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<MuscleGroupsAndMovementsActivity.FetchData> {

    public static final String INTENT_DATA_SELECTED_MOVEMENT_ID = "INTENT_DATA_SELECTED_MOVEMENT_ID";
    public static final String INTENT_DATA_FINISH_ON_MOVEMENT_TAP = "INTENT_DATA_FINISH_ON_MOVEMENT_TAP";
    public static final String INTENT_DATA_SHOW_MOVEMENT_DETAILS_ON_MOVEMENT_TAP = "INTENT_DATA_SHOW_MOVEMENT_DETAILS_ON_MOVEMENT_TAP";

    private RecyclerView recyclerView;
    private MuscleGroupsAndMovementsAdapter recyclerViewAdapter;
    private Integer selectedMovementId;

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_muscle_groups_and_movements);
        configureAppBar();
        logScreen(getTitle());
        this.recyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        linearLayoutManager.scrollToPosition(0);
        recyclerView.setLayoutManager(linearLayoutManager);
        final Intent intent = getIntent();
        this.selectedMovementId = (Integer)intent.getSerializableExtra(INTENT_DATA_SELECTED_MOVEMENT_ID);
        recyclerViewAdapter = new MuscleGroupsAndMovementsAdapter(this,
                selectedMovementId,
                intent.getBooleanExtra(INTENT_DATA_FINISH_ON_MOVEMENT_TAP, false),
                intent.getBooleanExtra(INTENT_DATA_SHOW_MOVEMENT_DETAILS_ON_MOVEMENT_TAP, false));
        recyclerView.setAdapter(recyclerViewAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Utils.displayProgressDialog(this, "Loading movements...");
        getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    public final Loader<FetchData> onCreateLoader(final int id, final Bundle args) {
        final RikerApp rikerApp = (RikerApp)getApplication();
        return new FetchDataLoader(this, rikerApp.dao);
    }

    @Override
    public final void onLoadFinished(final Loader<FetchData> loader, final FetchData fetchData) {
        this.recyclerViewAdapter.setFetchData(fetchData);
        new Handler().post(() -> Utils.dismissProgressDialog(this)); // https://stackoverflow.com/a/29071756/1034895
        if (this.selectedMovementId != null) {
            int selectedMovementPosition = 0;
            final int numItems = fetchData.muscleGroupsAndMovements.size();
            for (int i = 0; i < numItems; i++) {
                final Object mgOrMovement = fetchData.muscleGroupsAndMovements.get(i);
                if (mgOrMovement instanceof Movement) {
                    if (this.selectedMovementId.equals(((Movement)mgOrMovement).localIdentifier)) {
                        selectedMovementPosition = i;
                        break;
                    }
                }
            }
            this.recyclerView.scrollToPosition(selectedMovementPosition);
        }
    }

    @Override
    public final void onLoaderReset(final Loader<FetchData> loader) {
        this.recyclerViewAdapter.setFetchData(null);
    }

    public final static class FetchData {
        public final List muscleGroupsAndMovements;

        public FetchData(final List muscleGroupsAndMovements) {
            this.muscleGroupsAndMovements = muscleGroupsAndMovements;
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
            return new FetchData(rikerDao.muscleGroupsAndMovements());
        }
    }
}
