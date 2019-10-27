package com.rikerapp.riker.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.rikerapp.riker.R;
import com.rikerapp.riker.RikerApp;
import com.rikerapp.riker.Utils;
import com.rikerapp.riker.adapters.WorkoutsRecyclerViewAdapter;
import com.rikerapp.riker.listeners.EndlessRecyclerViewScrollListener;
import com.rikerapp.riker.loader.AsyncLoader;
import com.rikerapp.riker.model.Movement;
import com.rikerapp.riker.model.Muscle;
import com.rikerapp.riker.model.MuscleGroup;
import com.rikerapp.riker.model.RikerDao;
import com.rikerapp.riker.model.User;
import com.rikerapp.riker.model.UserSettings;
import com.rikerapp.riker.model.Workout;
import com.rikerapp.riker.model.WorkoutsTuple;

import org.parceler.Parcels;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class WorkoutsActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<WorkoutsActivity.FetchData> {

    private static final int SETS_FETCH_LIMIT_FOR_WORKOUTS = 200;

    // local state keys
    private static final String LSTATE_BEFORE_LOGGED_AT = "LSTATE_BEFORE_LOGGED_AT";
    private static final String LSTATE_SCROLL_OFFSET = "LSTATE_SCROLL_OFFSET";
    private static final String LSTATE_SCROLL_POSITION = "LSTATE_SCROLL_POSITION";
    private static final String LSTATE_USER = "LSTATE_USER";
    private static final String LSTATE_USER_SETTINGS = "LSTATE_USER_SETTINGS";
    private static final String LSTATE_ALL_MUSCLE_GROUPS = "LSTATE_ALL_MUSCLE_GROUPS";
    private static final String LSTATE_ALL_MUSCLES = "LSTATE_ALL_MUSCLES";
    private static final String LSTATE_ALL_MOVEMENTS = "LSTATE_ALL_MOVEMENTS";

    private boolean loading;
    private RecyclerView recyclerView;
    private WorkoutsRecyclerViewAdapter recyclerViewAdapter;
    private LinearLayoutManager linearLayoutManager;
    private Date beforeLoggedAt;
    private boolean asOfFetch;
    private Integer scrollPosition;
    private Integer scrollOffset;
    private User user;
    private UserSettings userSettings;
    private Map<Integer, MuscleGroup> allMuscleGroups;
    private Map<Integer, Muscle> allMuscles;
    private Map<Integer, Movement> allMovements;

    @Override
    public final void onSaveInstanceState(final Bundle outState) {
        if (this.beforeLoggedAt != null) {
            outState.putSerializable(LSTATE_BEFORE_LOGGED_AT, this.beforeLoggedAt);
        }
        if (this.scrollOffset != null) {
            outState.putSerializable(LSTATE_SCROLL_OFFSET, scrollOffset);
        }
        if (this.scrollPosition != null) {
            outState.putSerializable(LSTATE_SCROLL_POSITION, scrollPosition);
        }
        if (this.user != null) {
            outState.putParcelable(LSTATE_USER, Parcels.wrap(this.user));
        }
        if (this.userSettings != null) {
            outState.putParcelable(LSTATE_USER_SETTINGS, Parcels.wrap(this.userSettings));
        }
        if (this.allMuscleGroups != null) {
            outState.putParcelable(LSTATE_ALL_MUSCLE_GROUPS, Parcels.wrap(this.allMuscleGroups));
        }
        if (this.allMuscles != null) {
            outState.putParcelable(LSTATE_ALL_MUSCLES, Parcels.wrap(this.allMuscles));
        }
        if (this.allMovements != null) {
            outState.putParcelable(LSTATE_ALL_MOVEMENTS, Parcels.wrap(this.allMovements));
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public final void onResume() {
        super.onResume();
        if (scrollPosition != null) {
            new Handler().postDelayed(() -> linearLayoutManager.scrollToPositionWithOffset(scrollPosition, scrollOffset), 150);
        }
    }

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entities);
        configureAppBar();
        logScreen(getTitle());
        final FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> startActivityForResult(new Intent(this, SelectBodySegmentActivity.class), 0));
        this.recyclerView = findViewById(R.id.entitiesRecyclerView);
        this.recyclerView.setItemAnimator(new DefaultItemAnimator());
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        if (savedInstanceState != null) {
            this.beforeLoggedAt = (Date)savedInstanceState.getSerializable(LSTATE_BEFORE_LOGGED_AT);
            this.scrollOffset = (Integer)savedInstanceState.getSerializable(LSTATE_SCROLL_OFFSET);
            this.scrollPosition = (Integer)savedInstanceState.getSerializable(LSTATE_SCROLL_POSITION);
            this.user = Parcels.unwrap(savedInstanceState.getParcelable(LSTATE_USER));
            this.userSettings = Parcels.unwrap(savedInstanceState.getParcelable(LSTATE_USER_SETTINGS));
            this.allMuscleGroups = Parcels.unwrap(savedInstanceState.getParcelable(LSTATE_ALL_MUSCLE_GROUPS));
            this.allMuscles = Parcels.unwrap(savedInstanceState.getParcelable(LSTATE_ALL_MUSCLES));
            this.allMovements = Parcels.unwrap(savedInstanceState.getParcelable(LSTATE_ALL_MOVEMENTS));
        }
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerViewAdapter = new WorkoutsRecyclerViewAdapter(this);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public final void onLoadMore(final int page, final int totalItemsCount, final RecyclerView view) {
                if (totalItemsCount > 2) {
                    getSupportLoaderManager().restartLoader(0, null, WorkoutsActivity.this);
                }
            }
        });
        loading = true;
        if (this.beforeLoggedAt != null) {
            // because, since we're in onCreate, then that means we're here because of a configuration
            // change or something...and so if 'bundle' is not null, that means that this isn't our
            // first time through onCreate (again, config change), and so, we need to fetch the sets
            // as a range (i.e., re-fetch all the sets we have aleady fetched up to this point)
            this.asOfFetch = true;
            getSupportLoaderManager().restartLoader(0, null, this);
        } else {
            getSupportLoaderManager().initLoader(0, null, this);
        }
        displayDelayedProgressDialog("Loading workouts...");
    }

    private final void displayDelayedProgressDialog(final String message) {
        new Handler().postDelayed(() -> {
            if (loading) {
                Utils.displayProgressDialog(this, message);
            }
        }, 150);
    }

    @Override
    public final Loader<FetchData> onCreateLoader(final int id, final Bundle args) {
        final RikerApp rikerApp = (RikerApp)getApplication();
        return new FetchDataLoader(this, user, userSettings, allMovements, allMuscleGroups, allMuscles, rikerApp.dao, beforeLoggedAt, asOfFetch);
    }

    @Override
    public final void onLoadFinished(final Loader<FetchData> loader, final FetchData fetchData) {
        loading = false;
        this.asOfFetch = false;
        this.user = fetchData.user;
        this.userSettings = fetchData.userSettings;
        this.allMovements = fetchData.allMovements;
        this.allMuscleGroups = fetchData.allMuscleGroups;
        this.allMuscles = fetchData.allMuscles;
        final List<Workout> workouts = fetchData.workoutsTuple.workouts;
        if (workouts != null && workouts.size() > 1 && fetchData.workoutsTuple.numSets == SETS_FETCH_LIMIT_FOR_WORKOUTS) {
            // because our sets are paginated, we don't know if the last workout element
            // in workouts list really represents a "full" workout (because, the sets
            // that came back may have been cutoff mid-workout); and therefore, we simply
            // blow away the last workout in the list
            workouts.remove(workouts.size() - 1);
        }
        this.recyclerViewAdapter.setFetchData(workouts);
        if (workouts != null && workouts.size() > 0) {
            this.beforeLoggedAt = workouts.get(workouts.size() - 1).startDate;
        }
        new Handler().post(() -> Utils.dismissProgressDialog(this)); // https://stackoverflow.com/a/29071756/1034895
    }

    @Override
    public final void onLoaderReset(final Loader<FetchData> loader) {
        this.recyclerViewAdapter.setFetchData(null);
    }

    public final static class FetchData {

        public final User user;
        public final UserSettings userSettings;
        public final Map<Integer, MuscleGroup> allMuscleGroups;
        public final Map<Integer, Muscle> allMuscles;
        public final Map<Integer, Movement> allMovements;
        public final WorkoutsTuple workoutsTuple;

        public FetchData(final User user,
                         final UserSettings userSettings,
                         final Map<Integer, Movement> allMovements,
                         final Map<Integer, MuscleGroup> allMuscleGroups,
                         final Map<Integer, Muscle> allMuscles,
                         final WorkoutsTuple workoutsTuple) {
            this.user = user;
            this.userSettings = userSettings;
            this.allMovements = allMovements;
            this.allMuscleGroups = allMuscleGroups;
            this.allMuscles = allMuscles;
            this.workoutsTuple = workoutsTuple;
        }
    }

    private final static class FetchDataLoader extends AsyncLoader<FetchData> {

        private final RikerDao rikerDao;
        private final Date beforeLoggedAt;
        private final boolean asOfFetch;

        private User user;
        private UserSettings userSettings;
        private Map<Integer, MuscleGroup> allMuscleGroups;
        private Map<Integer, Muscle> allMuscles;
        private Map<Integer, Movement> allMovements;

        public FetchDataLoader(final Context context,
                               final User user,
                               final UserSettings userSettings,
                               final Map<Integer, Movement> allMovements,
                               final Map<Integer, MuscleGroup> allMuscleGroups,
                               final Map<Integer, Muscle> allMuscles,
                               final RikerDao rikerDao,
                               final Date beforeLoggedAt,
                               final boolean asOfFetch) {
            super(context);
            this.user = user;
            this.userSettings = userSettings;
            this.allMovements = allMovements;
            this.allMuscleGroups = allMuscleGroups;
            this.allMuscles = allMuscles;
            this.rikerDao = rikerDao;
            this.beforeLoggedAt = beforeLoggedAt;
            this.asOfFetch = asOfFetch;
        }

        @Override
        public final FetchData loadInBackground() {
            if (user == null) {
                user = rikerDao.user();
            }
            if (userSettings == null) {
                userSettings = rikerDao.userSettings(user);
            }
            if (allMuscleGroups == null) {
                final List<MuscleGroup> muscleGroupList = rikerDao.muscleGroups();
                allMuscleGroups = Utils.toMap(muscleGroupList);
            }
            if (this.allMuscles == null) {
                final List<Muscle> muscleList = rikerDao.muscles();
                this.allMuscles = Utils.toMap(muscleList);
            }
            if (this.allMovements == null) {
                final List<Movement> movementList = rikerDao.movements();
                this.allMovements = Utils.toMap(movementList);
            }
            List descendingSets;
            if (beforeLoggedAt != null) {
                if (asOfFetch) {
                    descendingSets = rikerDao.descendingSetsOnOrAfter(beforeLoggedAt, user);
                } else {
                    descendingSets = rikerDao.descendingSets(beforeLoggedAt, user, SETS_FETCH_LIMIT_FOR_WORKOUTS);
                }
            } else {
                descendingSets = rikerDao.descendingSets(user, SETS_FETCH_LIMIT_FOR_WORKOUTS);
            }
            final WorkoutsTuple workoutsTuple = rikerDao.workoutsTupleForDescendingSets(descendingSets,
                    user,
                    userSettings,
                    allMovements,
                    allMuscleGroups,
                    allMuscles);
            return new FetchData(user, userSettings, allMovements, allMuscleGroups, allMuscles, workoutsTuple);
        }
    }
}
