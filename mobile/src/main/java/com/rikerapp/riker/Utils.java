package com.rikerapp.riker;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.DimenRes;
import android.support.annotation.IdRes;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.util.Patterns;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessActivities;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.request.SessionInsertRequest;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.rikerapp.riker.activities.BaseActivity;
import com.rikerapp.riker.activities.WebViewActivity;
import com.rikerapp.riker.dialogfragments.ProgressDialogFragment;
import com.rikerapp.riker.importexport.ImportError;
import com.rikerapp.riker.model.BodyMeasurementLog;
import com.rikerapp.riker.model.BodySegment;
import com.rikerapp.riker.model.HttpResponseTuple;
import com.rikerapp.riker.model.ModelSupport;
import com.rikerapp.riker.model.Movement;
import com.rikerapp.riker.model.MovementVariant;
import com.rikerapp.riker.model.Muscle;
import com.rikerapp.riker.model.MuscleGroup;
import com.rikerapp.riker.model.MuscleGroupTuple;
import com.rikerapp.riker.model.OriginationDevice;
import com.rikerapp.riker.model.RikerDao;
import com.rikerapp.riker.model.RikerErrorType;
import com.rikerapp.riker.model.Set;
import com.rikerapp.riker.model.SizeUnit;
import com.rikerapp.riker.model.UriPathPart;
import com.rikerapp.riker.model.User;
import com.rikerapp.riker.model.UserSettings;
import com.rikerapp.riker.model.WeightUnit;
import com.rikerapp.riker.model.Workout;
import com.rikerapp.riker.model.WorkoutsTuple;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.Seconds;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

import static com.rikerapp.riker.Constants.DIVIDE_SCALE;
import static com.rikerapp.riker.importexport.PrepareSetsImportTask.NUM_FIELDS;

public final class Utils {

    private Utils() {}

    public static String setKey(final Movement selectedMovement, final MovementVariant selectedMovementVariant) {
        final StringBuilder stringBuilder = new StringBuilder(selectedMovement.localIdentifier.toString());
        if (selectedMovementVariant != null) {
            stringBuilder.append(selectedMovementVariant.localIdentifier.toString());
        }
        return stringBuilder.toString();
    }

    public static final class GoogleFitDataSetTuple {
        public final DataSet dataSet;
        public final int numDataPoints;
        public final Date lastDate;

        public GoogleFitDataSetTuple(final DataSet dataSet,
                final int numDataPoints,
                final Date lastDate) {
            this.dataSet = dataSet;
            this.numDataPoints = numDataPoints;
            this.lastDate = lastDate;
        }
    }

    public static final DataSet addSetGfDataPointToGfDataSet(final DataSet dataSet, final Set set, final Map<Integer, Movement> allMovements) {
        final DataPoint dataPoint = dataSet.createDataPoint()
                .setTimestamp(set.loggedAt.getTime(), TimeUnit.MILLISECONDS);
        final Movement movement = allMovements.get(set.movementId);
        String exerciseName = movement.canonicalName.toLowerCase();
        exerciseName = exerciseName.replace("-", "");
        exerciseName = exerciseName.replace(" ", "_");
        dataPoint.getValue(Field.FIELD_EXERCISE).setString(exerciseName);
        if (set.movementVariantId != null) {
            dataPoint.getValue(Field.FIELD_RESISTANCE_TYPE).setInt(MovementVariant.Id.movementVariantIdByRawId(set.movementVariantId).googleFitResistanceType);
        } else {
            dataPoint.getValue(Field.FIELD_RESISTANCE_TYPE).setInt(Field.RESISTANCE_TYPE_BODY);
        }
        dataPoint.getValue(Field.FIELD_REPETITIONS).setInt(set.numReps);
        dataPoint.getValue(Field.FIELD_RESISTANCE).setFloat(
                Utils.weightValue(set.weight,
                        WeightUnit.weightUnitById(set.weightUom),
                        WeightUnit.KG).floatValue());
        dataSet.add(dataPoint);
        return dataSet;
    }

    public static final DataSet addCalorieExpenditureGfDataPointToGfDataSet(final DataSet dataSet, final Workout workout) {
        final DataPoint dataPoint = dataSet.createDataPoint()
                .setTimeInterval(workout.startDate.getTime(), workout.endDate.getTime(), TimeUnit.MILLISECONDS);
        dataPoint.getValue(Field.FIELD_CALORIES).setFloat(workout.caloriesBurned.floatValue());
        dataSet.add(dataPoint);
        return dataSet;
    }

    public static final DataSet newWorkoutExercisesGfDataSet(final RikerApp rikerApp) {
        final DataSource workoutDataSource = new DataSource.Builder()
                .setDataType(DataType.TYPE_WORKOUT_EXERCISE)
                .setAppPackageName(rikerApp)
                .setType(DataSource.TYPE_RAW).build();
        return DataSet.create(workoutDataSource);
    }

    public static final DataSet newCaloriesExpendedGfDataSet(final RikerApp rikerApp) {
        final DataSource workoutDataSource = new DataSource.Builder()
                .setDataType(DataType.TYPE_CALORIES_EXPENDED)
                .setAppPackageName(rikerApp)
                .setType(DataSource.TYPE_RAW).build();
        return DataSet.create(workoutDataSource);
    }

    public static final BigDecimal caloriesBurned(final BigDecimal bodyWeightLbs, final long durationSeconds, final boolean vigorous) {
        final BigDecimal vigorousMultiplier = vigorous ? new BigDecimal("0.024") : new BigDecimal("0.012");
        // why 30?  Because the Harvard study gave samples at 30lb intervals
        // http://www.health.harvard.edu/diet-and-weight-loss/calories-burned-in-30-minutes-of-leisure-and-routine-activities
        return bodyWeightLbs.divide(new BigDecimal(30), DIVIDE_SCALE, RoundingMode.HALF_UP).multiply(vigorousMultiplier).multiply(new BigDecimal(durationSeconds));
    }

    private static final void computeImpactedMuscleGroups(final List<Integer> muscleIds,
                                                          final BigDecimal totalWeight,
                                                          final Map<Integer, BigDecimal> impactedMuscleGroups,
                                                          final Map<Integer, Muscle> allMuscles) {
        final List<Integer> muscleGroupIds = new ArrayList<>();
        for (final Integer muscleId : muscleIds) {
            final Muscle muscle = allMuscles.get(muscleId);
            muscleGroupIds.add(muscle.muscleGroupId);
        }
        final int numMuscleGroupIds = muscleGroupIds.size();
        if (numMuscleGroupIds > 0) {
            final BigDecimal perMuscleGroupAmount = totalWeight.divide(new BigDecimal(numMuscleGroupIds), DIVIDE_SCALE, RoundingMode.HALF_UP);
            for (final Integer muscleGroupId : muscleGroupIds) {
                BigDecimal totalMuscleGroupWeight = impactedMuscleGroups.get(muscleGroupId);
                if (totalMuscleGroupWeight == null) {
                    totalMuscleGroupWeight = BigDecimal.ZERO;
                }
                totalMuscleGroupWeight = totalMuscleGroupWeight.add(perMuscleGroupAmount);
                impactedMuscleGroups.put(muscleGroupId, totalMuscleGroupWeight);
            }
        }
    }

    public static final Workout workoutForDescendingSets(final List descendingSets,
                                                         final BodyMeasurementLog nearestBml,
                                                         final UserSettings userSettings,
                                                         final Map<Integer, Movement> allMovements,
                                                         final Map<Integer, MuscleGroup> allMuscleGroups,
                                                         final Map<Integer, Muscle> allMuscles) {
        final BigDecimal bodyWeightLbs = nearestBml != null ? Utils.weightValue(nearestBml.bodyWeight,
                WeightUnit.weightUnitById(nearestBml.bodyWeightUom),
                WeightUnit.LBS) : null;
        final Workout workout = new Workout();
        final int numSets = descendingSets.size();
        workout.sets = new ArrayList(numSets);
        for (final Object set : descendingSets) {
            workout.sets.add(Set.minimalCopy((Set)set));
        }
        final Set lastSet = (Set)descendingSets.get(0);
        Set firstSet = null;
        if (numSets > 1) {
            firstSet = (Set)descendingSets.get(numSets - 1);
        }
        Date startDate;
        if (firstSet != null) {
            startDate = new DateTime(firstSet.loggedAt).minusSeconds(30).toDate();
        } else {
            startDate = new DateTime(lastSet.loggedAt).minusSeconds(30).toDate();
        }
        final Date endDate = lastSet != null ? lastSet.loggedAt : firstSet.loggedAt;
        workout.workoutDurationInSeconds = (endDate.getTime() - startDate.getTime()) / 1000l;
        final boolean computeImpactedMuscleGroups = allMovements != null;
        BigDecimal primaryMuscleGroupPercentage = null;
        BigDecimal totalWorkoutWeightLifted = null;
        Map<Integer, BigDecimal> impactedMuscleGroups = null;
        if (computeImpactedMuscleGroups) {
            primaryMuscleGroupPercentage = ChartUtils.PRIMARY_MUSCLE_PERCENTAGE;
            totalWorkoutWeightLifted = BigDecimal.ZERO;
            impactedMuscleGroups = new HashMap<>();
        }
        int numSetsToFailure = 0;
        for (int i = 0; i < numSets; i++) {
            final Set set = (Set)descendingSets.get(i);
            if (set.toFailure) {
                numSetsToFailure++;
            }
            if (computeImpactedMuscleGroups) {
                final BigDecimal weight = Utils.weightValue(set.weight, WeightUnit.weightUnitById(set.weightUom), WeightUnit.weightUnitById(userSettings.weightUom));
                final int numReps = set.numReps.intValue();
                final BigDecimal totalWeight = weight.multiply(new BigDecimal(numReps));
                totalWorkoutWeightLifted = totalWorkoutWeightLifted.add(totalWeight);
                final Movement movement = allMovements.get(set.movementId);
                BigDecimal primaryMusclesTotalWeight;
                if (movement.secondaryMuscleIdList.size() > 0) {
                    primaryMusclesTotalWeight = totalWeight.multiply(primaryMuscleGroupPercentage);
                } else {
                    primaryMusclesTotalWeight = totalWeight;
                }
                final BigDecimal secondaryMusclesTotalWeight = totalWeight.subtract(primaryMusclesTotalWeight);
                computeImpactedMuscleGroups(movement.primaryMuscleIdList, primaryMusclesTotalWeight, impactedMuscleGroups, allMuscles);
                computeImpactedMuscleGroups(movement.secondaryMuscleIdList, secondaryMusclesTotalWeight, impactedMuscleGroups, allMuscles);
            }
        }
        List<MuscleGroupTuple> muscleGroupTuples = null;
        if (computeImpactedMuscleGroups) {
            muscleGroupTuples = new ArrayList<>(impactedMuscleGroups.size());
            final java.util.Set<Integer> muscleGroupIds = impactedMuscleGroups.keySet();
            for (final Integer muscleGroupId : muscleGroupIds) {
                final MuscleGroupTuple muscleGroupTuple = new MuscleGroupTuple();
                final MuscleGroup muscleGroup = allMuscleGroups.get(muscleGroupId);
                final BigDecimal muscleGroupWeight = impactedMuscleGroups.get(muscleGroupId);
                muscleGroupTuple.percentageOfTotalWorkout = muscleGroupWeight.divide(totalWorkoutWeightLifted, DIVIDE_SCALE, RoundingMode.HALF_UP);
                muscleGroupTuple.muscleGroupId = muscleGroupId;
                muscleGroupTuple.muscleGroupName = muscleGroup.name;
                muscleGroupTuples.add(muscleGroupTuple);
            }
            Collections.sort(muscleGroupTuples, (mgt1, mgt2) -> mgt2.percentageOfTotalWorkout.compareTo(mgt1.percentageOfTotalWorkout));
        }
        final BigDecimal toFailurePercentage = new BigDecimal(numSetsToFailure / (1.0f * numSets));
        workout.vigorous = toFailurePercentage.compareTo(new BigDecimal("0.75")) >= 0;
        if (bodyWeightLbs != null) {
            workout.caloriesBurned = caloriesBurned(bodyWeightLbs, workout.workoutDurationInSeconds, workout.vigorous);
        }
        workout.startDate = startDate;
        workout.endDate = endDate;
        workout.impactedMuscleGroupTuples = muscleGroupTuples;
        return workout;
    }

    public static final DataSet addBodyWeightGfDataPointToGfDataSet(final DataSet dataSet, final BodyMeasurementLog bml) {
        final long loggedAtTime = bml.loggedAt.getTime();
        final DataPoint bmlDataPoint = dataSet.createDataPoint()
                .setTimeInterval(loggedAtTime, loggedAtTime, TimeUnit.MILLISECONDS)
                .setTimestamp(loggedAtTime, TimeUnit.MILLISECONDS)
                .setFloatValues(Utils.weightValue(
                        bml.bodyWeight,
                        WeightUnit.weightUnitById(bml.bodyWeightUom),
                        WeightUnit.KG).floatValue());
        dataSet.add(bmlDataPoint);
        return dataSet;
    }

    public static final DataSet newBodyWeightGfDataSet(final RikerApp rikerApp) {
        final DataSource bodyWeightDataSource = new DataSource.Builder()
                .setDataType(DataType.TYPE_WEIGHT)
                .setAppPackageName(rikerApp)
                .setType(DataSource.TYPE_RAW).build();
        return DataSet.create(bodyWeightDataSource);
    }

    public static final DataSet singleBodyWeightGfDataSet(final RikerApp rikerApp, final BodyMeasurementLog bml) {
        final DataSet dataSet = newBodyWeightGfDataSet(rikerApp);
        return addBodyWeightGfDataPointToGfDataSet(dataSet, bml);
    }

    public static GoogleFitDataSetTuple allSyncableBodyWeightsGfDataSetTuple(final RikerApp rikerApp) {
        final Date googleFitLastBodyWeightEndDate = rikerApp.googleFitLastBodyWeightEndDate();
        List<BodyMeasurementLog> bmlsToSync;
        if (googleFitLastBodyWeightEndDate != null) {
            bmlsToSync = rikerApp.dao.ascendingBmlsWithNonNilBodyWeight(rikerApp.dao.user(), googleFitLastBodyWeightEndDate);
        } else {
            bmlsToSync = rikerApp.dao.ascendingBmlsWithNonNilBodyWeight(rikerApp.dao.user());
        }
        DataSet bmlsDataSet = null;
        final int numBmlsToSync = bmlsToSync.size();
        if (numBmlsToSync > 0) {
            bmlsDataSet = newBodyWeightGfDataSet(rikerApp);
            for (int i = 0; i < numBmlsToSync; i++) {
                addBodyWeightGfDataPointToGfDataSet(bmlsDataSet, bmlsToSync.get(i));
            }
        }
        return new GoogleFitDataSetTuple(bmlsDataSet,
                numBmlsToSync,
                numBmlsToSync > 0 ? bmlsToSync.get(numBmlsToSync - 1).loggedAt : null);
    }

    public static final List<SessionInsertRequest> sessionInsertRequests(final RikerApp rikerApp, final List workouts, final Map<Integer, Movement> allMovements) {
        final List<SessionInsertRequest> sessionInsertRequests = new ArrayList();
        final int numWorkouts = workouts.size();
        for (int i = 0; i < numWorkouts; i++) {
            final Workout workout = (Workout) workouts.get(i);
            final Session session = new Session.Builder()
                    .setStartTime(workout.startDate.getTime(), TimeUnit.MILLISECONDS)
                    .setEndTime(workout.endDate.getTime(), TimeUnit.MILLISECONDS)
                    .setActivity(FitnessActivities.STRENGTH_TRAINING)
                    .build();
            final DataSet exercisesDataSet = Utils.newWorkoutExercisesGfDataSet(rikerApp);
            final int numSets = workout.sets.size();
            for (int j = 0; j < numSets; j++) {
                final Set set = (Set)workout.sets.get(j);
                Utils.addSetGfDataPointToGfDataSet(exercisesDataSet, set, allMovements);
            }
            final SessionInsertRequest insertRequest = new SessionInsertRequest.Builder()
                    .setSession(session)
                    .addDataSet(exercisesDataSet).build();
            sessionInsertRequests.add(insertRequest);
        }
        return sessionInsertRequests;
    }

    public interface GoogleFitSyncError {
        void invoke(final Throwable throwable);
    }

    public interface GoogleFitSyncWorkoutsDone {
        void invoke(final int numWorkouts, final int numSets);
    }

    public interface GoogleFitSyncBmlsDone {
        void invoke(final int numBmls);
    }

    public static void syncBmlsToGoogleFit(final RikerApp rikerApp, final Activity activity, final GoogleFitSyncBmlsDone googleFitSyncBmlsDone, final GoogleFitSyncError googleFitSyncError) {
        try {
            final FitnessOptions fitnessOptions = FitnessOptions.builder()
                    .addDataType(DataType.TYPE_WEIGHT, FitnessOptions.ACCESS_WRITE)
                    .build();
            final GoogleSignInAccount googleSignInAccount = GoogleSignIn.getLastSignedInAccount(activity);
            if (GoogleSignIn.hasPermissions(googleSignInAccount, fitnessOptions)) {
                final Utils.GoogleFitDataSetTuple googleFitBodyWeightDataSetTuple = Utils.allSyncableBodyWeightsGfDataSetTuple(rikerApp);
                if (googleFitBodyWeightDataSetTuple.dataSet != null) {
                    final Task<Void> insertDataTask = Fitness.getHistoryClient(activity, googleSignInAccount).insertData(googleFitBodyWeightDataSetTuple.dataSet);
                    if (insertDataTask != null) {
                        insertDataTask.continueWith(task -> {
                            rikerApp.setGoogleFitLastBodyWeightEndDate(googleFitBodyWeightDataSetTuple.lastDate);
                            if (googleFitSyncBmlsDone != null) {
                                googleFitSyncBmlsDone.invoke(googleFitBodyWeightDataSetTuple.numDataPoints);
                            }
                            return null;
                        });
                    }
                } else {
                    if (googleFitSyncBmlsDone != null) {
                        googleFitSyncBmlsDone.invoke(0);
                    }
                }
            } else {
                googleFitSyncBmlsDone.invoke(0);
            }
        } catch (final Throwable throwable) {
            Crashlytics.logException(throwable);
            if (googleFitSyncError != null) {
                googleFitSyncError.invoke(throwable);
            }
        }
    }

    public static void syncWorkoutsToGoogleFit(final RikerApp rikerApp, final Activity activity, final GoogleFitSyncWorkoutsDone onDone, final GoogleFitSyncError googleFitSyncError) {
        try {
            final DataSet caloriesExpendedDataSet = Utils.newCaloriesExpendedGfDataSet(rikerApp);
            final User user = rikerApp.dao.user();
            final Date mostRecentSetDate = rikerApp.dao.mostRecentSetDate(user);
            if (mostRecentSetDate != null) {
                final DateTime now = DateTime.now();
                final DateTime mostRecentSetDateTime = new DateTime(mostRecentSetDate);
                final Seconds seconds = Seconds.secondsBetween(mostRecentSetDateTime, now);
                if (seconds.getSeconds() > 3600) { // an hour has passed since the most recent set was logged
                    final Date lastWorkoutEndDate = rikerApp.googleFitLastWorkoutEndDate();
                    final UserSettings userSettings = rikerApp.dao.userSettings(user);
                    List descendingSets;
                    if (lastWorkoutEndDate != null) {
                        descendingSets = rikerApp.dao.descendingSetsAfter(lastWorkoutEndDate, user);
                    } else {
                        descendingSets = rikerApp.dao.descendingSets(user);
                    }
                    final WorkoutsTuple workoutsTuple = rikerApp.dao.workoutsTupleForDescendingSets(descendingSets,
                            user,
                            userSettings,
                            null,
                            null,
                            null);
                    final List workouts = workoutsTuple.workouts;
                    if (workouts != null) {
                        final int numWorkouts = workouts.size();
                        for (int i = 0; i < numWorkouts; i++) {
                            final Workout workout = (Workout)workouts.get(i);
                            Utils.addCalorieExpenditureGfDataPointToGfDataSet(caloriesExpendedDataSet, workout);
                        }
                        final FitnessOptions fitnessOptions = FitnessOptions.builder()
                                .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_WRITE)
                                .build();
                        final GoogleSignInAccount googleSignInAccount = GoogleSignIn.getLastSignedInAccount(activity);
                        if (GoogleSignIn.hasPermissions(googleSignInAccount, fitnessOptions)) {
                            final Task<Void> insertDataTask = Fitness.getHistoryClient(activity, googleSignInAccount).insertData(caloriesExpendedDataSet);
                            insertDataTask.continueWith(dataTask -> {
                                final Map<Integer, Movement> allMovements = Utils.toMap(rikerApp.dao.movementsWithNullMuscleIds());
                                final List<SessionInsertRequest> sessionInsertRequests = sessionInsertRequests(rikerApp, workouts, allMovements);
                                final int numInsertRequests = sessionInsertRequests.size();
                                for (int i = 0; i < numInsertRequests; i++) {
                                    final int iCopy = i;
                                    final SessionInsertRequest sessionInsertRequest = sessionInsertRequests.get(i);
                                    final Task<Void> insertSessionTask = Fitness.getSessionsClient(activity, GoogleSignIn.getLastSignedInAccount(activity)).insertSession(sessionInsertRequest);
                                    insertSessionTask.continueWith(sessionTask -> {
                                        if (iCopy + 1 == numInsertRequests) {
                                            rikerApp.setGoogleFitLastWorkoutEndDate(workoutsTuple.latestSetLoggedAt);
                                            if (onDone != null) {
                                                onDone.invoke(numWorkouts, workoutsTuple.numSets);
                                            }
                                        }
                                        return null;
                                    });
                                }
                                return null;
                            });
                        } else {
                            if (onDone != null) { onDone.invoke(0, 0); }
                        }
                    } else {
                        if (onDone != null) { onDone.invoke(0, 0); }
                    }
                } else {
                    if (onDone != null) { onDone.invoke(0, 0); }
                }
            } else {
                if (onDone != null) { onDone.invoke(0, 0); }
            }
        } catch (final Throwable throwable) {
            Crashlytics.logException(throwable);
            if (googleFitSyncError != null) {
                googleFitSyncError.invoke(throwable);
            }
        }
    }

    public static void putBigDecimalIfNotEmpty(final Bundle bundle, final EditText editText, final String key) {
        final String editTextValue = editText.getText().toString();
        if (editTextValue.trim().length() > 0) {
            bundle.putSerializable(key, new BigDecimal(editTextValue.trim()));
        }
    }

    public static Date adjustTime(final Date date, final int hourOfDay, final int minute, final int second) {
        return new DateTime(date).withHourOfDay(hourOfDay).withMinuteOfHour(minute).withSecondOfMinute(second).toDate();
    }

    public static Date adjustDate(final Date date, final int year, final int monthOfYear, final int dayOfMonth) {
        return new DateTime(date).withYear(year).withMonthOfYear(monthOfYear + 1).withDayOfMonth(dayOfMonth).toDate();
    }

    public static Date dateWithoutTime(final Date date) {
        return new LocalDate(date).toDate();
    }

    public static Date addSeconds(final Date date, final int numSeconds) {
        return new DateTime(date).plusSeconds(numSeconds).toDate();
    }

    public static Date addDays(final Date date, final int numDays) {
        return new DateTime(date).plusDays(numDays).toDate();
    }

    public static void clearFocusAndDismissKeyboard(final Activity activity, final EditText editText) {
        editText.clearFocus();
        final InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    public static void showKeyboard(final Activity activity, final EditText editText) {
        final InputMethodManager im = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        im.showSoftInput(editText, 0);
    }

    public static final void startWebViewActivity(final Activity activity, final String title, final String rikerUri) {
        final Intent intent = new Intent(activity, WebViewActivity.class);
        intent.putExtra(WebViewActivity.INTENTDATA_TITLE, title);
        intent.putExtra(WebViewActivity.INTENTDATA_RIKER_URI, rikerUri);
        activity.startActivity(intent);
    }

    public static final void shareRiker(final BaseActivity activity, final Application application) {
        FirebaseAnalytics.getInstance(activity).logEvent(FirebaseAnalytics.Event.SHARE, null);
        final Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        final String shareSubject = "Track your strength with Riker";
        final String shareBody = String.format("Track your strength and relevant body measurements with Riker.\n\nAvailable on the App Store at: %s\n\nand Google Play at: %s",
                Constants.RIKER_IOS_APP_STORE_URL,
                Utils.playStoreUrlForPackage(application.getPackageName()));
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, shareSubject);
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        activity.startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }

    public static final String playStoreUrlForPackage(final String packageName) {
        return String.format("https://play.google.com/store/apps/details?id=%s", packageName);
    }

    public static final String toSimpleHtmlColor(final String hexString) {
        return String.format("#%s", hexString.substring(2, hexString.length()));
    }

    public static final String toSimpleHtmlColor(final int colorResource) {
        return toSimpleHtmlColor(Integer.toHexString(colorResource));
    }

    public static void shareExportFile(final Activity activity, final String fileName, final Integer numRecords) {
        // https://stackoverflow.com/a/38858040/1034895
        final Intent intent = new Intent(Intent.ACTION_SEND);
        final String authority = String.format("%s.fileprovider", activity.getApplicationContext().getPackageName());
        final File file = new File(activity.getFilesDir(), fileName);
        Timber.d("inside Utils.shareExportFile, authority: [%s], file: [%s]", authority, file.getAbsolutePath());
        final Uri uri = FileProvider.getUriForFile(activity, authority, file);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.putExtra(Intent.EXTRA_SUBJECT, String.format("%s", fileName));
        final StringBuilder extraTextStringBuilder = new StringBuilder();
        extraTextStringBuilder.append(String.format("Sharing Riker export file: %s.", fileName));
        if (numRecords != null) {
            extraTextStringBuilder.append(String.format("  This Riker export file contains %s %s.",
                    NumberFormat.getInstance().format(numRecords),
                    Utils.pluralize("record", numRecords)));
        }
        intent.putExtra(Intent.EXTRA_TEXT, extraTextStringBuilder.toString());
        intent.setType("text/csv");
        activity.startActivity(Intent.createChooser(intent, "Share Export File Using: "));
    }

    public static BigDecimal parseBigDecimal(final CSVRecord csvRecord, final int recordIndex, final String errorMsg, final List<ImportError> errors, final int rowNum) {
        try {
            final String valueStr = csvRecord.get(recordIndex);
            if (valueStr.trim().length() > 0) {
                return new BigDecimal(valueStr);
            }
            return null;
        } catch (NumberFormatException nfe) {
            errors.add(new ImportError(errorMsg, rowNum, false));
            return null;
        }
    }

    public static Date parseDate(final CSVRecord csvRecord, final int recordIndex, final String errorMsg, final List<ImportError> errors, final int rowNum) {
        try {
            return new Date(Long.parseLong(csvRecord.get(recordIndex).replace("'", "")));
        } catch (NumberFormatException nfe) {
            errors.add(new ImportError(errorMsg, rowNum, false));
            return null;
        }
    }

    public static Integer parseInteger(final CSVRecord csvRecord, final int recordIndex, final String errorMsg, final List<ImportError> errors, final int rowNum) {
        try {
            final String valueStr = csvRecord.get(recordIndex);
            if (valueStr.trim().length() > 0) {
                return new Integer(valueStr);
            }
            return null;
        } catch (NumberFormatException nfe) {
            errors.add(new ImportError(errorMsg, rowNum, false));
            return null;
        }
    }

    public static Boolean parseBoolean(final CSVRecord csvRecord, final int recordIndex, final String errorMsg, final List<ImportError> errors, final int rowNum) {
        try {
            final String valueStr = csvRecord.get(recordIndex);
            if (valueStr.trim().length() > 0) {
                return Boolean.parseBoolean(valueStr);
            }
            return null;
        } catch (NumberFormatException nfe) {
            errors.add(new ImportError(errorMsg, rowNum, false));
            return null;
        }
    }

    public static void exportSets(final Context context,
                                  final String fileName,
                                  final List<Set> setsList,
                                  final Map<Integer, Movement> allMovements,
                                  final Map<Integer, MovementVariant> allMovementVariants) {
        CSVPrinter csvPrinter = null;
        try {
            final FileOutputStream fileOutputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            final Writer writer = new OutputStreamWriter(fileOutputStream);
            csvPrinter = CSVFormat.DEFAULT.withHeader(
                    "Logged",
                    "Logged Unix Time",
                    "Movement",
                    "Movement ID",
                    "Variant",
                    "Variant ID",
                    "Weight",
                    "Weight UOM",
                    "Weight UOM ID",
                    "To Failure?",
                    "Negatives?",
                    "Ignore Time Component?",
                    "Reps",
                    "Origination Device",
                    "Origination Device ID")
                    .print(writer);
            final SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.DATE_FORMAT);
            for (final Set set : setsList) {
                csvPrinter.print(String.format("'%s'", dateFormat.format(set.loggedAt)));
                csvPrinter.print(Long.toString(set.loggedAt.getTime()));
                final Movement movement = allMovements.get(set.movementId);
                csvPrinter.print(movement.canonicalName);
                csvPrinter.print(set.movementId);
                if (set.movementVariantId != null) {
                    final MovementVariant movementVariant = allMovementVariants.get(set.movementVariantId);
                    csvPrinter.print(movementVariant.name);
                    csvPrinter.print(set.movementVariantId);
                } else {
                    csvPrinter.print(""); // movement variant getName
                    csvPrinter.print(""); // movement variant id
                }
                csvPrinter.print(set.weight);
                final WeightUnit weightUnit = WeightUnit.weightUnitById(set.weightUom);
                csvPrinter.print(weightUnit.name);
                csvPrinter.print(set.weightUom);
                csvPrinter.print(Boolean.toString(set.toFailure));
                csvPrinter.print(Boolean.toString(set.negatives));
                csvPrinter.print(Boolean.toString(set.ignoreTime));
                csvPrinter.print(set.numReps);
                final OriginationDevice.Id originationDeviceId = OriginationDevice.Id.originationDeviceIdById(set.originationDeviceId);
                csvPrinter.print(originationDeviceId.deviceName);
                csvPrinter.print(set.originationDeviceId);
                csvPrinter.println(); // indicate end-of-line
            }
        } catch (final Throwable throwable) {
            // do nothing for now...this might have a near-0% chance of happening, so for now, just
            // don't do anything
            Crashlytics.logException(throwable);
            Timber.e(throwable);
        } finally {
            try {
                if (csvPrinter != null) {
                    csvPrinter.close();
                }
            } catch (final Throwable throwable) {
                Crashlytics.logException(throwable);
                Timber.e(throwable);
            }
        }
    }

    public static void exportBmls(final Context context,
                                  final String fileName,
                                  final List<BodyMeasurementLog> bmlsList) {
        CSVPrinter csvPrinter = null;
        try {
            final FileOutputStream fileOutputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            final Writer writer = new OutputStreamWriter(fileOutputStream);
            csvPrinter = CSVFormat.DEFAULT.withHeader(
                    "Logged",
                    "Logged Unix Time",
                    "Body Weight",
                    "Body Weight UOM",
                    "Body Weight UOM ID",
                    "Calf Size",
                    "Chest Size",
                    "Arm Size",
                    "Neck Size",
                    "Waist Size",
                    "Thigh Size",
                    "Forearm Size",
                    "Size UOM",
                    "Size UOM ID",
                    "Origination Device",
                    "Origination Device ID")
                    .print(writer);
            final SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.DATE_FORMAT);
            for (final BodyMeasurementLog bml : bmlsList) {
                csvPrinter.print(String.format("'%s'", dateFormat.format(bml.loggedAt)));
                csvPrinter.print(Long.toString(bml.loggedAt.getTime()));
                csvPrinter.print(emptyIfNull(bml.bodyWeight));
                final WeightUnit weightUnit = WeightUnit.weightUnitById(bml.bodyWeightUom);
                csvPrinter.print(weightUnit.name);
                csvPrinter.print(weightUnit.id);
                csvPrinter.print(emptyIfNull(bml.calfSize));
                csvPrinter.print(emptyIfNull(bml.chestSize));
                csvPrinter.print(emptyIfNull(bml.armSize));
                csvPrinter.print(emptyIfNull(bml.neckSize));
                csvPrinter.print(emptyIfNull(bml.waistSize));
                csvPrinter.print(emptyIfNull(bml.thighSize));
                csvPrinter.print(emptyIfNull(bml.forearmSize));
                final SizeUnit sizeUnit = SizeUnit.sizeUnitById(bml.sizeUom);
                csvPrinter.print(sizeUnit.name);
                csvPrinter.print(sizeUnit.id);
                final OriginationDevice.Id originationDeviceId = OriginationDevice.Id.originationDeviceIdById(bml.originationDeviceId);
                csvPrinter.print(originationDeviceId.deviceName);
                csvPrinter.print(bml.originationDeviceId);
                csvPrinter.println(); // indicate end-of-line
            }
        } catch (final Throwable throwable) {
            // do nothing for now...this might have a near-0% chance of happening, so for now, just
            // don't do anything
            Crashlytics.logException(throwable);
            Timber.e(throwable);
        } finally {
            try {
                if (csvPrinter != null) {
                    csvPrinter.close();
                }
            } catch (final Throwable throwable) {
                Crashlytics.logException(throwable);
                Timber.e(throwable);
            }
        }
    }

    public static final String emptyIfNull(final Object value) {
        return value != null ? value.toString() : "";
    }

    public static final List<String> toStringList(final List objects, final Function.ToString toString) {
        final List<String> stringList = new ArrayList<>();
        for (final Object obj : objects) {
            stringList.add(toString.invoke(obj));
        }
        return stringList;
    }

    public static final String toYesNo(final boolean value) {
        return value ? "yes" : "no";
    }

    public static final void dismissProgressDialog(final AppCompatActivity activity) {
        final ProgressDialogFragment progressFragment =
                (ProgressDialogFragment)activity.getSupportFragmentManager().findFragmentByTag(Constants.FRAGMENT_TAG_PROGRESS_DIALOG);
        if (progressFragment != null) {
            progressFragment.dismiss();
        }
    }

    public static final void displayProgressDialog(final AppCompatActivity activity, final String message) {
        final FragmentManager fragmentManager = activity.getSupportFragmentManager();
        final ProgressDialogFragment progressFragment =
                (ProgressDialogFragment)fragmentManager.findFragmentByTag(Constants.FRAGMENT_TAG_PROGRESS_DIALOG);
        if (progressFragment == null || progressFragment.isDetached()) {
            final ProgressDialogFragment progressDialogFragment = ProgressDialogFragment.newInstance(message);
            try {
                progressDialogFragment.show(fragmentManager, Constants.FRAGMENT_TAG_PROGRESS_DIALOG);
            } catch (final Throwable t) {
                // This (calling 'show' in try/catch block) isn't necessary on Nexus 6P physical device,
                // but it was necessary when testing in emulator.  Go figure.
                Timber.e(t);
            }
        }
    }

    public static <T extends ModelSupport> Map<Integer, T> toMap(final List<T> entities) {
        final Map<Integer, T> entitiesMap = new HashMap<>();
        for (final T entity : entities) {
            entitiesMap.put(entity.localIdentifier, entity);
        }
        return entitiesMap;
    }

    public static String pluralize(final String word, final int value) {
        return String.format("%s%s", word, value == 0 || value > 1 ? "s" : "");
    }

    public static BigDecimal bigDecimalOrNullFromEditText(final EditText editText) {
        try {
            return new BigDecimal(editText.getText().toString().trim());
        } catch (NumberFormatException nfe) {
            return null;
        }
    }

    public static String validateNonNullStringEditText(final List<String> errorMessagesList,
                                                       final EditText editText,
                                                       final String msgIfEmpty) {
        final String valueStr = editText.getText().toString().trim();
        if (valueStr.length() == 0) {
            if (msgIfEmpty != null) {
                errorMessagesList.add(msgIfEmpty);
            }
            return null;
        } else {
            return valueStr;
        }
    }

    public static String validateEmailEditText(final List<String> errorMessagesList,
                                               final EditText editText,
                                               final String msgIfEmpty,
                                               final String msgIfNotEmail) {
        final String valueStr = editText.getText().toString().trim();
        if (valueStr.length() == 0) {
            errorMessagesList.add(msgIfEmpty);
        } else {
            if (Patterns.EMAIL_ADDRESS.matcher(valueStr).matches()) {
                return valueStr;
            } else {
                if (msgIfNotEmail != null) {
                    errorMessagesList.add(msgIfNotEmail);
                }
            }
        }
        return null;
    }

    public static BigDecimal validatePositiveNumberEditText(final boolean nullAllowed,
                                                            final List<String> errorMessagesList,
                                                            final EditText editText,
                                                            final String msgIfEmpty,
                                                            final String msgIfNotNumeric,
                                                            final String msgIfNotPositive) {
        final String valueStr = editText.getText().toString().trim();
        if (valueStr.length() == 0) {
            if (!nullAllowed) {
                errorMessagesList.add(msgIfEmpty);
            }
        } else {
            try {
                final BigDecimal value = new BigDecimal(valueStr);
                if (value.compareTo(BigDecimal.ZERO) <= 0) {
                    errorMessagesList.add(msgIfNotPositive);
                } else {
                    return value;
                }
            } catch (NumberFormatException nfe) {
                errorMessagesList.add(msgIfNotNumeric);
            }
        }
        return null;
    }

    public static void convert(final EditText editText, final SizeUnit currentSizeUnit, final SizeUnit targetSizeUnit) {
        final BigDecimal newValue = Utils.sizeValue(Utils.bigDecimalOrNullFromEditText(editText), currentSizeUnit, targetSizeUnit);
        editText.setText(Utils.formatWeightSizeValue(newValue));
    }

    public static void convert(final EditText editText, final WeightUnit currentWeightUnit, final WeightUnit targetWeightUnit) {
        final BigDecimal newValue = Utils.weightValue(Utils.bigDecimalOrNullFromEditText(editText), currentWeightUnit, targetWeightUnit);
        editText.setText(Utils.formatWeightSizeValue(newValue));
    }

    public static BigDecimal weightValue(final BigDecimal weightValue, final WeightUnit currentUom, final WeightUnit targetUom) {
        if (weightValue != null) {
            if (currentUom == targetUom) {
                return weightValue;
            } else {
                if (currentUom == WeightUnit.LBS) {
                    return weightValue.multiply(new BigDecimal("0.453592"));
                } else {
                    return weightValue.multiply(new BigDecimal("2.20462"));
                }
            }
        }
        return null;
    }

    public static BigDecimal sizeValue(final BigDecimal sizeValue, final SizeUnit currentUom, final SizeUnit targetUom) {
        if (sizeValue != null) {
            if (currentUom == targetUom) {
                return sizeValue;
            } else {
                if (currentUom == SizeUnit.INCHES) {
                    return sizeValue.multiply(new BigDecimal("2.54"));
                } else {
                    return sizeValue.multiply(new BigDecimal("0.393701"));
                }
            }
        }
        return null;
    }

    public static String formatWeightSizeValue(final BigDecimal weightSizeValue) {
        if (weightSizeValue != null) {
            final String weightValueStr = String.format("%.1f", weightSizeValue);
            if (weightValueStr.endsWith(".0")) {
                return String.format("%.0f", weightSizeValue);
            }
            return weightValueStr;
        }
        return "";
    }

    public static final BigDecimal editTextValueOrZero(final EditText editText) {
        try {
            final String valStr = editText.getText().toString();
            return new BigDecimal(valStr);
        } catch (NumberFormatException nfe) {
            return BigDecimal.ZERO;
        }
    }

    public static void decrement(final Activity activity, final EditText editText, final BigDecimal subtrahend) {
        try {
            final String valStr = editText.getText().toString();
            BigDecimal val = new BigDecimal(valStr);
            val = val.subtract(subtrahend);
            if (val.compareTo(BigDecimal.ZERO) <= 0) {
                editText.setText("0");
            } else {
                editText.setText(val.toString());
            }
            clearFocusAndDismissKeyboard(activity, editText);
        } catch (NumberFormatException nfe) {
            editText.setText("0");
        }
    }

    public static void increment(final Activity activity, final EditText editText, final BigDecimal augend) {
        try {
            final String valStr = editText.getText().toString();
            BigDecimal val = new BigDecimal(valStr);
            val = val.add(augend);
            editText.setText(val.toString());
            clearFocusAndDismissKeyboard(activity, editText);
        } catch (NumberFormatException nfe) {
            editText.setText(augend.toString());
        }
    }

    public static void bindWeightSizeTextView(final Activity activity, @IdRes int textViewId, final BigDecimal value) {
        final TextView textView = (TextView)activity.findViewById(textViewId);
        textView.setText(formatWeightSizeValue(value));
    }

    public static EditText bindWeightSizeEditText(final Activity activity, @IdRes int editTextId, final Number value) {
        final EditText editView = (EditText) activity.findViewById(editTextId);
        editView.setText(value != null ? value.toString() : "");
        return editView;
    }

    public interface AddBreadcrumb { void invoke(String breadcrumb); }

    public static void populateBreadcrumbs(final BodySegment selectedBodySegment,
                                           final MuscleGroup selectedMuscleGroup,
                                           final Movement selectedMovement,
                                           final MovementVariant selectedMovementVariant,
                                           final boolean suppressBodySegmentAndMuscleGroup,
                                           final AddBreadcrumb addBreadcrumb,
                                           final View separator) {
        boolean breadcrumbAdded = false;
        if (!suppressBodySegmentAndMuscleGroup && selectedBodySegment != null) {
            addBreadcrumb.invoke(selectedBodySegment.name);
            breadcrumbAdded = true;
        }
        if (!suppressBodySegmentAndMuscleGroup && selectedMuscleGroup != null) {
            addBreadcrumb.invoke(selectedMuscleGroup.name);
            breadcrumbAdded = true;
        }
        if (selectedMovement != null) {
            addBreadcrumb.invoke(selectedMovement.canonicalName);
            breadcrumbAdded = true;
        }
        if (selectedMovementVariant != null) {
            addBreadcrumb.invoke(selectedMovementVariant.name);
            breadcrumbAdded = true;
        }
        if (!breadcrumbAdded) {
            separator.setVisibility(View.GONE);
        }
    }

    public static AddBreadcrumb makeBreadcrumbAdder(final Activity activity, final ViewGroup breadcrumbsViewGroup) {
        return name -> {
            final Button breadcrumbButton = (Button) LayoutInflater.from(activity).inflate(R.layout.breadcrumb_button, null);
            final FlexboxLayout.LayoutParams layoutParams = new FlexboxLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(
                    Utils.dpToPx(activity, 5), // left
                    Utils.dpToPx(activity, 5), // top
                    0, // right
                    0); // bottom
            breadcrumbButton.setLayoutParams(layoutParams);
            breadcrumbButton.setText(name);
            breadcrumbsViewGroup.addView(breadcrumbButton);
        };
    }

    public static boolean equalOrBothNull(final Object lhs, final Object rhs) {
        if (lhs == null) {
            return rhs == null;
        } else if (rhs == null) {
            return false;
        } else {
            return lhs.equals(rhs);
        }
    }

    public static void applyRikerFont(final AssetManager assetManager, final TextView textView) {
        final Typeface typeface = Typeface.createFromAsset(assetManager, "fonts/jr-hand.ttf");
        textView.setTypeface(typeface);
    }

    public static String globalIdentifier(final UriPathPart uriPathPart, final int localMasterIdentifier) {
        return String.format("%s/d/%s/%s", BuildConfig.globalIdentifierPrefix, uriPathPart.pathPart, localMasterIdentifier);
    }

    public static int dpToPx(final Activity activity, final int dp) {
        final DisplayMetrics displayMetrics = activity.getResources().getDisplayMetrics();
        return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, displayMetrics);
    }

    public static final Spanned fromHtml(final String source) {
        return fromHtml(source, null);
    }

    @SuppressWarnings("deprecation")
    public static final Spanned fromHtml(final String source, final Html.TagHandler tagHandler) { // http://stackoverflow.com/a/39841101/1034895
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY, null, tagHandler);
        } else {
            return Html.fromHtml(source, null, tagHandler);
        }
    }

    public static String percentageText(final BigDecimal value) {
        final NumberFormat percentageFormat = NumberFormat.getPercentInstance();
        percentageFormat.setMinimumFractionDigits(0);
        return percentageFormat.format(value);
    }

    public static float floatValue(final Resources resources, final @DimenRes int dimenRes) {
        final TypedValue typedValue = new TypedValue();
        resources.getValue(dimenRes, typedValue, true);
        return typedValue.getFloat();
    }

    public static void safeRemove(final ViewGroup container, @IdRes final int viewId) {
        safeSetVisibility(container, viewId, View.GONE);
    }

    public static void safeMakeVisible(final ViewGroup container, @IdRes final int viewId) {
        safeSetVisibility(container, viewId, View.VISIBLE);
    }

    public static void safeMakeInvisible(final ViewGroup container, @IdRes final int viewId) {
        safeSetVisibility(container, viewId, View.INVISIBLE);
    }

    private static void safeSetVisibility(final ViewGroup container, @IdRes final int viewId, final int visibility) {
        if (container != null) {
            final View view = container.findViewById(viewId);
            if (view != null) {
                view.setVisibility(visibility);
            }
        }
    }

    public static boolean equalOrBothNull(final Integer value1, final Integer value2) {
        if (value1 == null && value2 == null) {
            return true;
        }
        if (value1 != null) {
            if (value2 != null) {
                return value1.equals(value2);
            }
        }
        return false;
    }

    public static final class ParseSetsCsvResult {
        public final List<Set> sets;
        public final boolean anyReferenceErrors;
        public final List<ImportError> errors;

        public ParseSetsCsvResult(final List<Set> sets, final boolean anyReferenceErrors, final List<ImportError> errors) {
            this.sets = sets;
            this.anyReferenceErrors = anyReferenceErrors;
            this.errors = errors;
        }
    }

    public static ParseSetsCsvResult parseSetsCsv(final RikerDao rikerDao, final BufferedReader bufferedReader) {
        CSVParser csvParser = null;
        final List<ImportError> errors = new ArrayList<>();
        boolean anyReferenceErrors = false;
        final Map<Integer, Movement> allMovements = Utils.toMap(rikerDao.movementsWithNullMuscleIds());
        final Map<Integer, MovementVariant> allMovementVariants = Utils.toMap(rikerDao.movementVariants());
        final Map<Integer, OriginationDevice> allOriginationDevices = Utils.toMap(rikerDao.originationDevices());
        final List<Set> sets = new ArrayList<>();
        Integer recordNum = null;
        try {
            csvParser = CSVFormat.DEFAULT.parse(bufferedReader);
            final List<CSVRecord> recordList = csvParser.getRecords();
            final int numRecords = recordList.size();
            for (int i = 1; i < numRecords; i++) { // i starts at 1 (not 0) so we skip headings line
                recordNum = i;
                final CSVRecord csvRecord = recordList.get(i);
                if (csvRecord.size() != NUM_FIELDS) {
                    errors.add(new ImportError(String.format("Wrong number of records found (%d).  Should be: %d", csvRecord.size(), NUM_FIELDS), i, false));
                } else {
                    int movementId = 0;
                    try {
                        movementId = Integer.parseInt(csvRecord.get(3));
                        if (allMovements.get(movementId) == null) {
                            anyReferenceErrors = true;
                            errors.add(new ImportError(String.format("References a movement not present on your device."), i, true));
                        }
                    } catch (NumberFormatException nfe) {
                        errors.add(new ImportError("Invalid movement ID value.", i, false));
                    }
                    final String variantIdStr = csvRecord.get(5);
                    Integer variantId = null;
                    if (variantIdStr.trim().length() > 0 && !variantIdStr.trim().equals("null")) {
                        try {
                            variantId = Integer.parseInt(variantIdStr);
                            if (allMovementVariants.get(variantId) == null) {
                                anyReferenceErrors = true;
                                errors.add(new ImportError(String.format("References a movement variant not present on your device."), i, true));
                            }
                        } catch (NumberFormatException nfe) {
                            errors.add(new ImportError("Invalid movement variant ID value.", i, false));
                        }
                    }
                    int originationDeviceId = 0;
                    try {
                        originationDeviceId = Integer.parseInt(csvRecord.get(14));
                        if (allOriginationDevices.get(originationDeviceId) == null) {
                            anyReferenceErrors = true;
                            errors.add(new ImportError(String.format("References Riker system data not present on your device."), i, true));
                        }
                    } catch (NumberFormatException nfe) {
                        errors.add(new ImportError("Invalid origination device ID value.", i, false));
                    }
                    if (errors.size() == 0) {
                        final Date loggedAt = Utils.parseDate(csvRecord, 1, "Invalid logged at date.", errors, i);
                        final BigDecimal weight = Utils.parseBigDecimal(csvRecord, 6, "Invalid weight value.", errors, i);
                        final int weightUomId = Utils.parseInteger(csvRecord, 8, "Invalid weight units value.", errors, i);
                        final Boolean toFailure = Utils.parseBoolean(csvRecord, 9, "Invalid to-failure value.", errors, i);
                        final Boolean negatives = Utils.parseBoolean(csvRecord, 10, "Invalid negatives value.", errors, i);
                        final Boolean ignoreTime = Utils.parseBoolean(csvRecord, 11, "Invalid ignore-time value.", errors, i);
                        final Integer numReps = Utils.parseInteger(csvRecord, 12, "Invalid reps value.", errors, i);
                        if (errors.size() == 0) {
                            final Set set = new Set();
                            set.importedAt = new Date();
                            set.loggedAt = loggedAt;
                            set.weight = weight;
                            set.weightUom = weightUomId;
                            set.toFailure = toFailure;
                            set.negatives = negatives;
                            set.ignoreTime = ignoreTime;
                            set.numReps = numReps;
                            set.movementId = movementId;
                            set.movementVariantId = variantId;
                            set.originationDeviceId = originationDeviceId;
                            sets.add(set);
                        }
                    }
                }
            }
        } catch (Throwable any) {
            Timber.e(any);
            errors.add(new ImportError(ExceptionUtils.getRootCauseMessage(any), recordNum, false));
        } finally {
            try {
                if (csvParser != null) {
                    csvParser.close();
                }
            } catch (IOException e) { /* do nothing */ }
        }
        return new ParseSetsCsvResult(sets, anyReferenceErrors, errors);
    }

    public static void toggleVisibility(final View view) {
        if (view.getVisibility() == View.GONE) {
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.GONE);
        }
    }

    public static String absoluteUrl(final String uri) {
        return String.format("%s%s", BuildConfig.rikerUriPrefix, uri);
    }

    public static String rikerMediaType(final String entityType, final boolean withCharset) {
        return String.format("application/vnd.riker.%s-v0.0.1+json%s", entityType, withCharset ? ";charset=UTF-8" : "");
    }

    public static String rikerAcceptHeader(final String entityType) {
        return String.format("application/vnd.riker.%s-v0.0.1+json", entityType);
    }

    public static JsonObject toJsonObject(final String json) {
        return new JsonParser().parse(json).getAsJsonObject();
    }

    public static String getString(final JsonObject jsonObject, final String property) {
        if (jsonObject.has(property)) {
            final JsonElement jsonElement = jsonObject.get(property);
            if (jsonElement instanceof JsonPrimitive) {
                return jsonElement.getAsString();
            }
        }
        return null;
    }

    public static boolean getBoolean(final JsonObject jsonObject, final String property) {
        if (jsonObject.has(property)) {
            final JsonElement jsonElement = jsonObject.get(property);
            if (jsonElement instanceof JsonPrimitive) {
                return jsonElement.getAsBoolean();
            }
        }
        return false;
    }

    public static Integer getInteger(final JsonObject jsonObject, final String property) {
        if (jsonObject.has(property)) {
            final JsonElement jsonElement = jsonObject.get(property);
            if (jsonElement instanceof JsonPrimitive) {
                return jsonElement.getAsInt();
            }
        }
        return null;
    }

    public static Long getLong(final JsonObject jsonObject, final String property) {
        if (jsonObject.has(property)) {
            final JsonElement jsonElement = jsonObject.get(property);
            if (jsonElement instanceof JsonPrimitive) {
                return jsonElement.getAsLong();
            }
        }
        return null;
    }

    public static Date getDate(final JsonObject jsonObject, final String property) {
        final Long longValue = getLong(jsonObject, property);
        if (longValue != null) {
            return new Date(longValue);
        }
        return null;
    }

    public static BigDecimal getBigDecimal(final JsonObject jsonObject, final String property) {
        if (jsonObject.has(property)) {
            final JsonElement jsonElement = jsonObject.get(property);
            if (jsonElement instanceof JsonPrimitive) {
                return jsonElement.getAsBigDecimal();
            }
        }
        return null;
    }

    /*public static HttpResponseTuple executeHttpPost(final JsonObject jsonObject, final String uri, final String entityType) {
        return executeHttpPost(jsonObject, uri, entityType, null);
    }

    // So that we can trust my local SSL cert (that otherwise is not trustable)
    // https://stackoverflow.com/a/25992879/1034895
    private static OkHttpClient getUnsafeOkHttpClient() {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        @Override
                        public final void checkClientTrusted(final java.security.cert.X509Certificate[] chain, final String authType) {
                        }

                        @Override
                        public final void checkServerTrusted(final java.security.cert.X509Certificate[] chain, final String authType) {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };
            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            final OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager)trustAllCerts[0]);
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public final boolean verify(final String hostname, final SSLSession session) {
                    return true;
                }
            });
            return builder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static HttpResponseTuple executeHttpPost(final JsonObject jsonRequestBody, final String uri, final String entityType, final Boolean establishSession) {
        final OkHttpClient client = BuildConfig.DEBUG ? getUnsafeOkHttpClient() : new OkHttpClient();
        final RequestBody requestBody = RequestBody.create(MediaType.parse(Utils.rikerMediaType(entityType, true)), jsonRequestBody.toString());
        final Request.Builder requestBuilder = new Request.Builder();
        if (establishSession != null) {
            requestBuilder.addHeader(BuildConfig.rikerEstablishSessionHeader, Boolean.toString(establishSession));
        }
        final String absoluteUrlString = Utils.absoluteUrl(uri);
        final Request request = requestBuilder
                .url(absoluteUrlString)
                .addHeader("accept", Utils.rikerAcceptHeader(entityType))
                .addHeader("accept-charset", "UTF-8")
                .post(requestBody)
                .build();
        try {
            final Response response = client.newCall(request).execute();
            if (response != null) {
                JsonObject jsonResponsePayload = null;
                final ResponseBody responseBody = response.body();
                if (responseBody != null) {
                    final String responseString = responseBody.string();
                    if (responseString != null && responseString.trim().length() > 0) {
                        Timber.d("response string for url: [%s]: %s", absoluteUrlString, responseString);
                        try {
                            jsonResponsePayload = Utils.toJsonObject(responseString);
                        } catch (final Throwable throwable) { // this would happen for 'server busy' because 503 is handled by nginx and ngxin will return a text/html response, and so json parse will fail
                            Timber.e(throwable);
                        }
                    }
                }
                return new HttpResponseTuple(response.code(),
                        jsonResponsePayload,
                        response.header("location"),
                        response.header(BuildConfig.rikerAuthTokenHeader),
                        response.header(BuildConfig.rikerErrorMaskHeader));
            }
            return null;
        } catch (final IOException exception) {
            Timber.e(exception);
            Crashlytics.logException(exception);
            return null;
        }
    }*/

    public static List<String> errorsForMask(final String mask, final List<RikerErrorType.ErrorTuple> errorTuples) {
        final List<String> errors = new ArrayList<>();
        if (mask != null) {
            try {
                final int maskInt = Integer.parseInt(mask);
                return RikerErrorType.computeErrors(maskInt, errorTuples);
            } catch (final NumberFormatException e) {
                Timber.e(e);
                Crashlytics.logException(e);
            }
        }
        return errors;
    }

    public static void handleHttpResponse(
            final HttpResponseTuple responseTuple,
            final Function.VoidFunction okHandler,
            final Function.VoidFunction nullHandler,
            final Function.HttpUnprocessableEntityHandler unprocessableEntityHandler,
            final Function.ErrorTuplesFn validationErrorTuplesFn,
            final Function.VoidFunction serverErrorHandler,
            final Function.VoidFunction notAuthenticatedHandler,
            final Function.VoidFunction unavailableHandler) {
        if (responseTuple != null) {
            switch (responseTuple.code) {
                case 200:
                case 201:
                case 204:
                    if (okHandler != null) okHandler.invoke();
                    break;
                case 422:
                    if (unprocessableEntityHandler != null) unprocessableEntityHandler.invoke(errorsForMask(responseTuple.errorMaskHeaderVal, validationErrorTuplesFn.invoke()));
                    break;
                case 401:
                    if (notAuthenticatedHandler != null) notAuthenticatedHandler.invoke();
                    break;
                case 500:
                case 502:
                    if (serverErrorHandler != null) serverErrorHandler.invoke();
                    break;
                case 503:
                    if (unavailableHandler != null) unavailableHandler.invoke();
                    break;
            }
        } else {
            if (nullHandler != null) nullHandler.invoke();
        }
    }
}
