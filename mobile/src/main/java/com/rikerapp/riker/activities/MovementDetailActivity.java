package com.rikerapp.riker.activities;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rikerapp.riker.R;
import com.rikerapp.riker.RikerApp;
import com.rikerapp.riker.Utils;
import com.rikerapp.riker.model.RikerDao;
import com.rikerapp.riker.loader.AsyncLoader;
import com.rikerapp.riker.model.Movement;
import com.rikerapp.riker.model.MovementAlias;
import com.rikerapp.riker.model.MovementVariant;
import com.rikerapp.riker.model.Muscle;
import com.rikerapp.riker.model.MuscleAlias;
import com.rikerapp.riker.model.MuscleGroup;

import org.apache.commons.lang3.StringUtils;
import org.parceler.Parcels;

import java.util.List;
import java.util.Map;

public final class MovementDetailActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<MovementDetailActivity.FetchData> {

    private static final String INTENTDATA_ALLOW_SET_START = "INTENTDATA_ALLOW_SET_START";

    private static final int INBETWEEN_ITEM_MARGIN_DP = 3;

    private boolean allowSetStart;
    private TextView movementLabel;
    private Button movementStartSetButton;
    private TextView bodyLiftTextView;
    private TextView movementAliasesTextView;
    private TextView primaryMusclesIntroTextView;
    private ViewGroup primaryMusclesContainer;
    private TextView secondaryMusclesIntroTextView;
    private ViewGroup secondaryMusclesContainer;
    private TextView movementVariantsIntroTextView;
    private ViewGroup movementVariantsContainer;

    public static Intent makeIntent(final Context context,
                                    final Movement movement,
                                    final boolean allowSetStart) {
        final Intent intent = new Intent(context, MovementDetailActivity.class);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(CommonBundleKey.Movement.name(), Parcels.wrap(movement));
        bundle.putBoolean(INTENTDATA_ALLOW_SET_START, allowSetStart);
        intent.putExtras(bundle);
        return intent;
    }

    private final Movement selectedMovement() {
        return Parcels.unwrap(getIntent().getParcelableExtra(CommonBundleKey.Movement.name()));
    }

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movement_detail);
        configureAppBar();
        logScreen(getTitle());
        final Intent intent = getIntent();
        this.allowSetStart = intent.getBooleanExtra(INTENTDATA_ALLOW_SET_START, false);
        this.movementLabel = findViewById(R.id.movementLabel);
        this.movementStartSetButton = findViewById(R.id.movementStartSetButton);
        this.bodyLiftTextView = findViewById(R.id.bodyLiftTextView);
        this.movementAliasesTextView = findViewById(R.id.movementAliasesTextView);
        this.primaryMusclesIntroTextView = findViewById(R.id.primaryMusclesIntroTextView);
        this.primaryMusclesContainer = findViewById(R.id.primaryMusclesContainer);
        this.secondaryMusclesIntroTextView = findViewById(R.id.secondaryMusclesIntroTextView);
        this.secondaryMusclesContainer = findViewById(R.id.secondaryMusclesContainer);
        this.movementVariantsIntroTextView = findViewById(R.id.movementVariantsIntroTextView);
        this.movementVariantsContainer = findViewById(R.id.movementVariantsContainer);
        if (!allowSetStart) {
            this.movementStartSetButton.setVisibility(View.GONE);
        } else {
            this.movementStartSetButton.setOnClickListener(view -> {
                final Movement selectedMovement = selectedMovement();
                if (selectedMovement.variantMask != null && selectedMovement.variantMask != 0) {
                    startActivity(SelectMovementVariantActivity.makeIntent(
                            this,
                            null,
                            null,
                            selectedMovement,
                            false));
                } else {
                    final RikerApp rikerApp = (RikerApp)getApplication();
                    startActivity(EnterRepsActivity.makeIntent(this,
                            selectedMovement,
                            null,
                            rikerApp.setsMap));
                }
            });
        }
    }

    private static void bindMuscleToView(final Muscle muscle, final View muscleDetailView, final FetchData fetchData, final RikerDao rikerDao) {
        final TextView muscleLabel = muscleDetailView.findViewById(R.id.muscleLabel);
        final TextView muscleGroupValueTextView = muscleDetailView.findViewById(R.id.muscleGroupValueTextView);
        final ViewGroup aliasContainer = muscleDetailView.findViewById(R.id.aliasContainer);
        final TextView aliasTextView = muscleDetailView.findViewById(R.id.aliasTextView);
        final TextView aliasValueTextView = muscleDetailView.findViewById(R.id.aliasValueTextView);
        muscleLabel.setText(muscle.canonicalName);
        final MuscleGroup muscleGroup = fetchData.allMuscleGroupsMap.get(muscle.muscleGroupId);
        muscleGroupValueTextView.setText(muscleGroup.name);
        final List<MuscleAlias> muscleAliases = rikerDao.muscleAliases(muscle.localIdentifier);
        final int numMuscleAliases = muscleAliases.size();
        if (numMuscleAliases > 0) {
            if (numMuscleAliases > 1) {
                aliasTextView.setText("aliases: ");
            }
            final StringBuilder aliases = new StringBuilder();
            for (int i = 0; i < numMuscleAliases; i++) {
                aliases.append(String.format("<strong>%s</strong>", muscleAliases.get(i).alias));
                if (i + 2 < numMuscleAliases) {
                    aliases.append(", ");
                } else if (i + 1 < numMuscleAliases) {
                    aliases.append(" and ");
                }
            }
            aliasValueTextView.setText(Utils.fromHtml(aliases.toString()));
        } else {
            aliasContainer.setVisibility(View.GONE);
        }
    }

    private final void bindMuscleListToView(final List<Muscle> muscleList,
                                            final LayoutInflater layoutInflater,
                                            final FetchData fetchData,
                                            final RikerApp rikerApp,
                                            final ViewGroup musclesContainer) {
        final int numMuscles = muscleList.size();
        musclesContainer.removeAllViews();
        for (int i = 0; i < numMuscles; i++) {
            final Muscle muscle = muscleList.get(i);
            final View muscleDetailView = layoutInflater.inflate(R.layout.muscle_detail, null);
            bindMuscleToView(muscle, muscleDetailView, fetchData, rikerApp.dao);
            if (i > 0) {
                final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, // width
                        LinearLayout.LayoutParams.WRAP_CONTENT); // height
                layoutParams.setMargins(
                        0, // left
                        Utils.dpToPx(this, INBETWEEN_ITEM_MARGIN_DP), // top
                        0, // right
                        0); // bottom
                muscleDetailView.setLayoutParams(layoutParams);
            }
            musclesContainer.addView(muscleDetailView);
        }
    }

    private final void bindToUi(final FetchData fetchData) {
        final RikerApp rikerApp = (RikerApp)getApplication();
        final Movement movement = selectedMovement();
        this.movementLabel.setText(movement.canonicalName);
        final int numPrimaryMuscles = fetchData.primaryMusclesList.size();
        final String capitalizedMovementName = StringUtils.capitalize(movement.canonicalName);
        if (movement.isBodyLift) {
            this.bodyLiftTextView.setText(Utils.fromHtml(String.format("<strong>%s</strong> is a body-lift movement that Riker estimates to use <strong>%s</strong> of your body weight.",
                    capitalizedMovementName,
                    Utils.percentageText(movement.percentageOfBodyWeight))));
        } else {
            this.bodyLiftTextView.setVisibility(View.GONE);
        }
        final int numMovementAliases = fetchData.movementAliasList.size();
        if (numMovementAliases > 0) {
            final StringBuilder movementAliasesText = new StringBuilder(String.format("%s is also known by the following name%s: ",
                    capitalizedMovementName,
                    numMovementAliases > 1 ? "s" : ""));
            for (int i = 0; i < numMovementAliases; i++) {
                movementAliasesText.append(String.format("<strong>%s</strong>", fetchData.movementAliasList.get(i).alias));
                if (i + 2 < numMovementAliases) {
                    movementAliasesText.append(", ");
                } else if (i + 1 < numMovementAliases) {
                    movementAliasesText.append(" and ");
                }
            }
            this.movementAliasesTextView.setText(Utils.fromHtml(movementAliasesText.toString()));
        } else {
            this.movementAliasesTextView.setVisibility(View.GONE);
        }
        this.primaryMusclesIntroTextView.setText(Utils.fromHtml(String.format("The following %s the <strong>primary muscle%s</strong> hit by the %s movement:",
                numPrimaryMuscles > 1 ? "are" : "is",
                numPrimaryMuscles > 1 ? "s" : "",
                movement.canonicalName)));
        final LayoutInflater layoutInflater = getLayoutInflater();
        bindMuscleListToView(fetchData.primaryMusclesList, layoutInflater, fetchData, rikerApp, this.primaryMusclesContainer);
        final int numSecondaryMuscles = fetchData.secondaryMusclesList.size();
        if (numSecondaryMuscles > 0) {
            this.secondaryMusclesIntroTextView.setText(Utils.fromHtml(String.format("The following %s the <strong>secondary muscle%s</strong> hit by the %s movement:",
                    numSecondaryMuscles > 1 ? "are" : "is",
                    numSecondaryMuscles > 1 ? "s" : "",
                    movement.canonicalName)));
            bindMuscleListToView(fetchData.secondaryMusclesList, layoutInflater, fetchData, rikerApp, this.secondaryMusclesContainer);
        } else {
            this.secondaryMusclesIntroTextView.setVisibility(View.GONE);
            this.secondaryMusclesContainer.setVisibility(View.GONE);
        }
        final int numVariants = fetchData.movementVariantList != null ? fetchData.movementVariantList.size() : 0;
        if (numVariants > 0) {
            this.movementVariantsIntroTextView.setText(Utils.fromHtml(String.format("The following %s the <strong>variant%s</strong> available for the %s movement:",
                    numVariants > 1 ? "are" : "is",
                    numVariants > 1 ? "s" : "",
                    movement.canonicalName)));
            this.movementVariantsContainer.removeAllViews();
            for (int i = 0; i < numVariants; i++) {
                final MovementVariant movementVariant = fetchData.movementVariantList.get(i);
                final View view = layoutInflater.inflate(R.layout.movement_variant, null);
                final TextView movementVariantLabel = (TextView)view.findViewById(R.id.movementVariantLabel);
                movementVariantLabel.setText(movementVariant.name);
                final Button startSetButton = (Button)view.findViewById(R.id.startSetButton);
                if (!this.allowSetStart) {
                    startSetButton.setVisibility(View.GONE);
                } else {
                    startSetButton.setOnClickListener(v -> startActivity(EnterRepsActivity.makeIntent(this, movement, movementVariant, rikerApp.setsMap)));
                }
                if (i > 0) {
                    final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, // width
                            LinearLayout.LayoutParams.WRAP_CONTENT); // height
                    layoutParams.setMargins(
                            0, // left
                            Utils.dpToPx(this, INBETWEEN_ITEM_MARGIN_DP), // top
                            0, // right
                            0); // bottom
                    view.setLayoutParams(layoutParams);
                }
                this.movementVariantsContainer.addView(view);
            }
        } else {
            this.movementVariantsIntroTextView.setVisibility(View.GONE);
            this.movementVariantsContainer.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Utils.displayProgressDialog(this, "Loading movement details...");
        getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    public final Loader<FetchData> onCreateLoader(final int id, final Bundle args) {
        final RikerApp rikerApp = (RikerApp)getApplication();
        return new FetchDataLoader(this, rikerApp.dao, selectedMovement());
    }

    @Override
    public final void onLoadFinished(final Loader<FetchData> loader, final FetchData fetchData) {
        new Handler().post(() -> Utils.dismissProgressDialog(this)); // https://stackoverflow.com/a/29071756/1034895
        bindToUi(fetchData);
    }

    @Override
    public final void onLoaderReset(final Loader<FetchData> loader) {
        // nothing to do really...
    }

    final static class FetchData {
        public final List<MovementAlias> movementAliasList;
        public final Map<Integer, MuscleGroup> allMuscleGroupsMap;
        public final List<MovementVariant> movementVariantList;
        public final List<Muscle> primaryMusclesList;
        public final List<Muscle> secondaryMusclesList;

        public FetchData(final List<MovementAlias> movementAliasList,
                final Map<Integer, MuscleGroup> allMuscleGroupsMap,
                final List<MovementVariant> movementVariantList,
                final List<Muscle> primaryMusclesList,
                final List<Muscle> secondaryMusclesList) {
            this.movementAliasList = movementAliasList;
            this.allMuscleGroupsMap = allMuscleGroupsMap;
            this.movementVariantList = movementVariantList;
            this.primaryMusclesList = primaryMusclesList;
            this.secondaryMusclesList = secondaryMusclesList;
        }
    }

    private final static class FetchDataLoader extends AsyncLoader<FetchData> {

        private final RikerDao rikerDao;
        private final Movement movement;

        public FetchDataLoader(final Context context,
                               final RikerDao rikerDao,
                               final Movement movement) {
            super(context);
            this.rikerDao = rikerDao;
            this.movement = movement;
        }

        @Override
        public final FetchData loadInBackground() {
            List<MovementVariant> movementVariantList = null;
            if (movement.variantMask != null && movement.variantMask != 0) {
                movementVariantList = rikerDao.movementVariants(movement.variantMask);
            }
            final Integer movementId = movement.localIdentifier;
            return new FetchData(
                    rikerDao.movementAliases(movementId),
                    Utils.toMap(rikerDao.muscleGroups()),
                    movementVariantList,
                    rikerDao.primaryMuscles(movementId),
                    rikerDao.secondaryMuscles(movementId));
        }
    }
}
