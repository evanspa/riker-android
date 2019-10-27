package com.rikerapp.riker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.widget.Button;

import com.rikerapp.riker.R;

public final class WhatToMeasureActivity extends BaseActivity {

    private final void startBmlLoggingActivity(@IdRes final int buttonRes, final Class bmlActivityClass) {
        final Button button = findViewById(buttonRes);
        button.setOnClickListener(view -> startActivityForResult(new Intent(this, bmlActivityClass), 0));
    }

    @Override
    protected final void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        setResult(resultCode);
    }

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_what_to_measure);
        configureAppBar();
        logScreen(getTitle());
        startBmlLoggingActivity(R.id.bodyWeightButton, EnterBodyWeightActivity.class);
        startBmlLoggingActivity(R.id.armsButton, EnterArmSizeActivity.class);
        startBmlLoggingActivity(R.id.chestButton, EnterChestSizeActivity.class);
        startBmlLoggingActivity(R.id.calvesButton, EnterCalfSizeActivity.class);
        startBmlLoggingActivity(R.id.waistButton, EnterWaistSizeActivity.class);
        startBmlLoggingActivity(R.id.neckButton, EnterNeckSizeActivity.class);
        startBmlLoggingActivity(R.id.forearmsButton, EnterForearmSizeActivity.class);
        startBmlLoggingActivity(R.id.thighsButton, EnterThighSizeActivity.class);
        startBmlLoggingActivity(R.id.severalButton, BmlAddActivity.class);
    }
}
