package com.rikerapp.riker.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import com.rikerapp.riker.Constants;
import com.rikerapp.riker.R;
import com.rikerapp.riker.RikerApp;
import com.rikerapp.riker.Utils;
import com.rikerapp.riker.adapters.SetsRecyclerViewAdapter;
import com.rikerapp.riker.listeners.EndlessRecyclerViewScrollListener;
import com.rikerapp.riker.loader.AsyncLoader;
import com.rikerapp.riker.model.Movement;
import com.rikerapp.riker.model.MovementVariant;
import com.rikerapp.riker.model.RikerDao;
import com.rikerapp.riker.model.Set;
import com.rikerapp.riker.model.User;

import java.util.Date;
import java.util.List;
import java.util.Map;

public final class SetsActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<SetsActivity.FetchData> {

    // local state keys
    private static final String LSTATE_BEFORE_LOGGED_AT = "LSTATE_BEFORE_LOGGED_AT";
    private static final String LSTATE_SCROLL_OFFSET = "LSTATE_SCROLL_OFFSET";
    private static final String LSTATE_SCROLL_POSITION = "LSTATE_SCROLL_POSITION";

    private CoordinatorLayout coordinatorLayout;
    private RecyclerView recyclerView;
    private SetsRecyclerViewAdapter recyclerViewAdapter;
    private LinearLayoutManager linearLayoutManager;
    private boolean loading;
    private Date beforeLoggedAt;
    private boolean asOfFetch;
    private Integer scrollPosition;
    private Integer scrollOffset;

    @Override
    protected final void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        setResult(resultCode);
        switch (resultCode) {
            case Codes.RESULT_CODE_ENTITY_DELETED:
            case Codes.RESULT_CODE_ENTITY_UPDATED:
            case Codes.RESULT_CODE_ENTITY_ADDED:
                reloadSets();
                break;
        }
    }

    @Override
    public final void onPause() {
        super.onPause();
        scrollPosition = linearLayoutManager.findFirstVisibleItemPosition();
        final View v = recyclerView.getChildAt(0);
        scrollOffset = (v == null) ? 0 : (v.getTop() - recyclerView.getPaddingTop());
    }

    @Override
    public final void onResume() {
        super.onResume();
        if (scrollPosition != null) {
            new Handler().postDelayed(() -> linearLayoutManager.scrollToPositionWithOffset(scrollPosition, scrollOffset), 150);
        }
    }

    private final void reloadSets() {
        loading = true;
        this.recyclerViewAdapter.setFetchData(null);
        this.beforeLoggedAt = null;
        getSupportLoaderManager().restartLoader(0, null, this);
        displayDelayedProgressDialog("Re-loading sets...");
    }

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
        super.onSaveInstanceState(outState);
    }

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entities);
        configureAppBar();
        logScreen(getTitle());
        final FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> startActivityForResult(new Intent(this, SelectBodySegmentActivity.class), 0));
        this.coordinatorLayout = findViewById(R.id.coordinatorLayout);
        this.recyclerView = findViewById(R.id.entitiesRecyclerView);
        this.recyclerView.setItemAnimator(new DefaultItemAnimator());
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        if (savedInstanceState != null) {
            this.beforeLoggedAt = (Date)savedInstanceState.getSerializable(LSTATE_BEFORE_LOGGED_AT);
            this.scrollOffset = (Integer)savedInstanceState.getSerializable(LSTATE_SCROLL_OFFSET);
            this.scrollPosition = (Integer)savedInstanceState.getSerializable(LSTATE_SCROLL_POSITION);
        }
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerViewAdapter = new SetsRecyclerViewAdapter(this);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public final void onLoadMore(final int page, final int totalItemsCount, final RecyclerView view) {
                if (totalItemsCount > 2) {
                    getSupportLoaderManager().restartLoader(0, null, SetsActivity.this);
                }
            }
        });
        final ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT | ItemTouchHelper.DOWN | ItemTouchHelper.UP) {

            @Override
            public final boolean onMove(final RecyclerView recyclerView, final RecyclerView.ViewHolder viewHolder, final RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public final int getSwipeDirs(final RecyclerView recyclerView, final RecyclerView.ViewHolder viewHolder) {
                final int position = viewHolder.getAdapterPosition();
                if (position == 0) { // prevent swipe of header
                    return 0;
                }
                if (position == (recyclerViewAdapter.loadedSets.size() + 1)) { // prevent swipe of footer
                    return 0;
                }
                return super.getSwipeDirs(recyclerView, viewHolder);
            }

            @Override
            public final void onSwiped(final RecyclerView.ViewHolder viewHolder, final int swipeDir) {
                final int position = viewHolder.getAdapterPosition();
                if (position > 0) {
                    final RikerApp rikerApp = (RikerApp)getApplication();
                    final int setPosition = position - 1; // -1 because header is first position in adapter
                    final Set set = (Set)recyclerViewAdapter.loadedSets.get(setPosition);
                    rikerApp.dao.deleteSet(set);
                    indicateEntitySavedOrDeleted();
                    setResult(Codes.RESULT_CODE_ENTITY_DELETED);
                    recyclerViewAdapter.loadedSets.remove(setPosition);
                    Snackbar.make(coordinatorLayout, "Set deleted.", Snackbar.LENGTH_SHORT).show();
                    recyclerViewAdapter.notifyItemRemoved(position);
                    recyclerViewAdapter.fetchData.totalNumSets--;
                    recyclerViewAdapter.notifyItemChanged(0); // so header updates itself
                }
            }
        });
        itemTouchHelper.attachToRecyclerView(recyclerView);
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
        displayDelayedProgressDialog("Loading sets...");
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
        return new FetchDataLoader(this, rikerApp.dao, this.beforeLoggedAt, this.asOfFetch);
    }

    @Override
    public final void onLoadFinished(final Loader<FetchData> loader, final FetchData fetchData) {
        loading = false;
        this.asOfFetch = false;
        this.recyclerViewAdapter.setFetchData(fetchData);
        if (fetchData.sets != null && fetchData.sets.size() > 0) {
            this.beforeLoggedAt = ((Set) fetchData.sets.get(fetchData.sets.size() - 1)).loggedAt;
        }
        new Handler().post(() -> Utils.dismissProgressDialog(this)); // https://stackoverflow.com/a/29071756/1034895
    }

    @Override
    public final void onLoaderReset(final Loader<FetchData> loader) {
        this.recyclerViewAdapter.setFetchData(null);
    }

    public final static class FetchData {

        public final User user;
        public final List sets;
        public int totalNumSets;
        public final Map<Integer, Movement> allMovements;
        public final Map<Integer, MovementVariant> allMovementVariants;

        public FetchData(final User user,
                         final List entities,
                         final int totalNumEntities,
                         final Map<Integer, Movement> allMovements,
                         final Map<Integer, MovementVariant> allMovementVariants) {
            this.user = user;
            this.sets = entities;
            this.totalNumSets = totalNumEntities;
            this.allMovements = allMovements;
            this.allMovementVariants = allMovementVariants;
        }
    }

    private final static class FetchDataLoader extends AsyncLoader<FetchData> {

        private final RikerDao rikerDao;
        private final Date beforeLoggedAt;
        private final boolean asOfFetch;

        public FetchDataLoader(final Context context,
                               final RikerDao rikerDao,
                               final Date beforeLoggedAt,
                               final boolean asOfFetch) {
            super(context);
            this.rikerDao = rikerDao;
            this.beforeLoggedAt = beforeLoggedAt;
            this.asOfFetch = asOfFetch;
        }

        @Override
        public final FetchData loadInBackground() {
            final User user = rikerDao.user();
            List sets;
            if (this.beforeLoggedAt != null) {
                if (asOfFetch) {
                    sets = rikerDao.descendingSetsOnOrAfter(this.beforeLoggedAt, user);
                } else {
                    sets = rikerDao.descendingSets(this.beforeLoggedAt, user, Constants.ENTITIES_FETCH_LIMIT);
                }
            } else {
                sets = rikerDao.descendingSets(user, Constants.ENTITIES_FETCH_LIMIT);
            }
            return new FetchData(user,
                    sets,
                    rikerDao.numSets(user),
                    Utils.toMap(rikerDao.movementsWithNullMuscleIds()),
                    Utils.toMap(rikerDao.movementVariants()));
        }
    }
}
