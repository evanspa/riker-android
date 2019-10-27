package com.rikerapp.riker;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.multidex.MultiDexApplication;
import android.support.v4.util.LruCache;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.FirebaseApp;
import com.rikerapp.riker.model.ChartRawData;
import com.rikerapp.riker.model.RikerDao;
import com.rikerapp.riker.model.Set;
import com.rikerapp.riker.model.User;

import net.danlew.android.joda.JodaTimeAndroid;

import java.util.Date;
import java.util.List;
import java.util.Map;

import io.fabric.sdk.android.Fabric;
import timber.log.Timber;

import static com.rikerapp.riker.Constants.RIKER_PREFERENCES;

public final class RikerApp extends MultiDexApplication {

    private static final int SETS_CACHE_SIZE = 50 * 1024 * 1024; // 50MiB
    //private static final int BMLS_CACHE_SIZE = 50 * 1024 * 1024; // 50MiB
    private static final int RAW_CHART_DATA_CACHE_SIZE = 25 * 1024 * 1024; // 25MiB

    public RikerDao dao;
    public Map<String, List<Set>> setsMap; // in-memory only and is safe to be nulled-out on app kill (gets set by EnterRepsActivity)
    public LruCache<String, List> setCache;
    //public LruCache<String, List> bmlCache;
    public LruCache<String, ChartRawData> chartRawDataCache;

    @Override
    public final void onCreate() {
        super.onCreate();
        JodaTimeAndroid.init(this);
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        // initialize Firebase
        FirebaseApp.initializeApp(this);
        Fabric.with(this, new Crashlytics());

        // initialize DAO and setup the user
        dao = new RikerDao(this);
        final User user = dao.user();
        if (user == null) {
            dao.establishLocalUser();
        }

        // initialize caches
        setCache = new LruCache<>(SETS_CACHE_SIZE);
        //bmlCache = new LruCache<>(BMLS_CACHE_SIZE);
        chartRawDataCache = new LruCache<>(RAW_CHART_DATA_CACHE_SIZE);

        // store first launch date
        final SharedPreferences sharedPreferences = getSharedPreferences(RIKER_PREFERENCES, Context.MODE_PRIVATE);
        if (sharedPreferences.getLong(SharedPreferenceKey.FIRST_LAUNCH_AT.name(), 0) == 0) {
            final SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putLong(SharedPreferenceKey.FIRST_LAUNCH_AT.name(), new Date().getTime()).apply();
        }

        /*final User userYo = dao.user();
        if (dao.ascendingSets(userYo).size() == 0) {
            try {
                final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(getAssets().open("riker-sets-2018-06-07.csv")));
                final Utils.ParseSetsCsvResult parseSetsCsvResult = Utils.parseSetsCsv(dao, bufferedReader);
                dao.saveAllNewImportedSets(userYo, parseSetsCsvResult.sets);
            } catch (Throwable t) {
                Timber.e(t);
            }
        }*/
    }

    private final Date sharedPrefDate(final String sharedPrefsFileName, final SharedPreferenceKey sharedPreferenceKey) {
        final SharedPreferences sharedPreferences = getSharedPreferences(sharedPrefsFileName, Context.MODE_PRIVATE);
        final long val = sharedPreferences.getLong(sharedPreferenceKey.name(), 0);
        if (val > 0) {
            return new Date(val);
        }
        return null;
    }

    private final void sharedPrefStoreDate(final String sharedPrefsFileName, final SharedPreferenceKey sharedPreferenceKey, final Date date) {
        final SharedPreferences sharedPreferences = getSharedPreferences(sharedPrefsFileName, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        if (date != null) {
            editor.putLong(sharedPreferenceKey.name(), date.getTime());
        } else {
            editor.remove(sharedPreferenceKey.name());
        }
        editor.commit();
    }

    public final Date suppressedWeightTfDefaultedToBodyWeightPopupAt() {
        return sharedPrefDate(RIKER_PREFERENCES, SharedPreferenceKey.SUPPRESSED_WEIGHTTF_DEFAULTED_TO_BODYWEIGHT_POPUP_AT);
    }

    public final void setSuppressedWeightTfDefaultedToBodyWeightPopupAt(final Date date) {
        sharedPrefStoreDate(RIKER_PREFERENCES, SharedPreferenceKey.SUPPRESSED_WEIGHTTF_DEFAULTED_TO_BODYWEIGHT_POPUP_AT, date);
    }

    public final Date googleFitEnabledAt() {
        return sharedPrefDate(RIKER_PREFERENCES/*_NOT_BACKED_UP*/, SharedPreferenceKey.GOOGLE_FIT_ENABLED_AT);
    }

    public final void setGoogleFitEnabledAt(final Date date) {
        sharedPrefStoreDate(RIKER_PREFERENCES/*_NOT_BACKED_UP*/, SharedPreferenceKey.GOOGLE_FIT_ENABLED_AT, date);
    }

    public final Date googleFitWorkoutsDisabledAt() {
        return sharedPrefDate(RIKER_PREFERENCES/*_NOT_BACKED_UP*/, SharedPreferenceKey.GOOGLE_FIT_WORKOUTS_DISABLED_AT);
    }

    public final void setGoogleFitWorkoutsDisabledAt(final Date date) {
        sharedPrefStoreDate(RIKER_PREFERENCES/*_NOT_BACKED_UP*/, SharedPreferenceKey.GOOGLE_FIT_WORKOUTS_DISABLED_AT, date);
    }

    public final Date googleFitBodyWeightsDisabledAt() {
        return sharedPrefDate(RIKER_PREFERENCES/*_NOT_BACKED_UP*/, SharedPreferenceKey.GOOGLE_FIT_BODYWEIGHTS_DISABLED_AT);
    }

    public final void setGoogleFitBodyWeightsDisabledAt(final Date date) {
        sharedPrefStoreDate(RIKER_PREFERENCES/*_NOT_BACKED_UP*/, SharedPreferenceKey.GOOGLE_FIT_BODYWEIGHTS_DISABLED_AT, date);
    }

    public final void setGoogleFitLastBodyWeightEndDate(final Date date) {
        sharedPrefStoreDate(RIKER_PREFERENCES, SharedPreferenceKey.GOOGLE_FIT_LAST_BODY_WEIGHT_END_DATE, date);
    }

    public final Date googleFitLastBodyWeightEndDate() {
        return sharedPrefDate(RIKER_PREFERENCES, SharedPreferenceKey.GOOGLE_FIT_LAST_BODY_WEIGHT_END_DATE);
    }

    public final void setGoogleFitLastWorkoutEndDate(final Date date) {
        sharedPrefStoreDate(RIKER_PREFERENCES, SharedPreferenceKey.GOOGLE_FIT_LAST_WORKOUT_END_DATE, date);
    }

    public final Date googleFitLastWorkoutEndDate() {
        return sharedPrefDate(RIKER_PREFERENCES, SharedPreferenceKey.GOOGLE_FIT_LAST_WORKOUT_END_DATE);
    }
}
