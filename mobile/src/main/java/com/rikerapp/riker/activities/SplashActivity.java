package com.rikerapp.riker.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import com.rikerapp.riker.R;
import com.rikerapp.riker.SharedPreferenceKey;
import com.rikerapp.riker.adapters.SplashPagerAdapter;

import java.util.Date;

import static com.rikerapp.riker.Constants.RIKER_PREFERENCES;

public final class SplashActivity extends BaseActivity {

    public static final String INTENTDATA_SPLASH_AGAIN = "INTENTDATA_SPLASH_AGAIN";

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        final boolean splashAgain = getIntent().getBooleanExtra(INTENTDATA_SPLASH_AGAIN, false);
        if (splashAgain) {
            logScreen("splash_again");
        } else {
            logScreen("splash");
        }
        final ViewPager splashViewPager = findViewById(R.id.splashViewPager);
        final SplashPagerAdapter splashPagerAdapter = new SplashPagerAdapter(getSupportFragmentManager());
        splashViewPager.setAdapter(splashPagerAdapter);
        final TabLayout tabLayout = findViewById(R.id.splashTabDots);
        tabLayout.setupWithViewPager(splashViewPager);
        final Button goButton = findViewById(R.id.splashGoButton);
        goButton.setOnClickListener(view -> {
                    if (!splashAgain) {
                        final SharedPreferences sharedPreferences = getSharedPreferences(RIKER_PREFERENCES, Context.MODE_PRIVATE);
                        final SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putLong(SharedPreferenceKey.EXPERIENCED_SPLASH_SCREEN_AT.name(), new Date().getTime());
                        editor.commit();
                        startActivity(new Intent(this, HomeActivity.class));
                    }
                    finish();
                }
        );
    }
}
