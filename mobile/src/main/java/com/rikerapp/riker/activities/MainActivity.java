package com.rikerapp.riker.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.rikerapp.riker.SharedPreferenceKey;

import timber.log.Timber;

import static com.rikerapp.riker.Constants.RIKER_PREFERENCES;

public final class MainActivity extends AppCompatActivity {

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final SharedPreferences sharedPreferences = getSharedPreferences(RIKER_PREFERENCES, Context.MODE_PRIVATE);
        final long experiencedSplashScreenAtTime = sharedPreferences.getLong(SharedPreferenceKey.EXPERIENCED_SPLASH_SCREEN_AT.name(), 0);
        Class activityClass;
        if (experiencedSplashScreenAtTime == 0) {
            activityClass = SplashActivity.class;
        } else {
            activityClass = HomeActivity.class;
        }
        startActivity(new Intent(this, activityClass));
        finish();
    }
}
