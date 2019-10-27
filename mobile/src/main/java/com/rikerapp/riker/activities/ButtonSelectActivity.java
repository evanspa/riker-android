package com.rikerapp.riker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.rikerapp.riker.R;
import com.rikerapp.riker.RikerApp;
import com.rikerapp.riker.adapters.ButtonSelectRecyclerViewAdapter;
import com.rikerapp.riker.model.MasterSupport;

import org.parceler.Parcels;

import java.util.List;

public abstract class ButtonSelectActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<List<? extends MasterSupport>>  {

    public static final String INTENTDATA_FROM_SEARCH_RESULT = "INTENTDATA_FROM_SEARCH_RESULT";

    private ButtonSelectRecyclerViewAdapter buttonSelectRecyclerViewAdapter;

    @Override
    protected final void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        setResult(resultCode);
    }

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_button_select);
        configureAppBar();
        logScreen(getTitle());
        final RecyclerView recyclerView = findViewById(R.id.selectButtonsRecyclerView);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        linearLayoutManager.scrollToPosition(0);
        recyclerView.setLayoutManager(linearLayoutManager);
        final Intent intent = getIntent();
        this.buttonSelectRecyclerViewAdapter = new ButtonSelectRecyclerViewAdapter(this,
                buttonText(),
                onClick(),
                footerResource(),
                intent.getBooleanExtra(INTENTDATA_FROM_SEARCH_RESULT, false),
                Parcels.unwrap(intent.getParcelableExtra(CommonBundleKey.BodySegment.name())),
                Parcels.unwrap(intent.getParcelableExtra(CommonBundleKey.MuscleGroup.name())),
                Parcels.unwrap(intent.getParcelableExtra(CommonBundleKey.Movement.name())),
                Parcels.unwrap(intent.getParcelableExtra(CommonBundleKey.MovementVariant.name())));
        recyclerView.setAdapter(this.buttonSelectRecyclerViewAdapter);
        configureMovementSearch();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSupportLoaderManager().initLoader(0, null, this);
    }

    public interface ButtonText { String invoke(MasterSupport masterSupport); }
    public abstract ButtonText buttonText();

    public interface OnClick { void invoke(MasterSupport masterSupport); }
    public abstract OnClick onClick();

    public abstract Loader<List<? extends MasterSupport>> loader(final RikerApp rikerApp);

    public @LayoutRes int footerResource() { return R.layout.bottom_margin_default_footer; }

    @Override
    public final Loader<List<? extends MasterSupport>> onCreateLoader(final int id, final Bundle args) {
        return loader((RikerApp)getApplication());
    }

    @Override
    public final void onLoadFinished(final Loader<List<? extends MasterSupport>> loader, final List<? extends MasterSupport> dataList) {
        this.buttonSelectRecyclerViewAdapter.setDataList(dataList);
    }

    @Override
    public final void onLoaderReset(final Loader<List<? extends MasterSupport>> loader) {
        this.buttonSelectRecyclerViewAdapter.setDataList(null);
    }
}

