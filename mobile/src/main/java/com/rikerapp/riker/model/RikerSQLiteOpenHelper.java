package com.rikerapp.riker.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.rikerapp.riker.BuildConfig;
import com.rikerapp.riker.Utils;
import com.rikerapp.riker.sql.RikerSQLUtil;
import com.rikerapp.riker.sql.columns.CommonColumn;
import com.rikerapp.riker.sql.tables.RikerTable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import timber.log.Timber;

public final class RikerSQLiteOpenHelper extends SQLiteOpenHelper {

    private final Context context;

    private static final String PUSH_UP_BODY_WEIGHT_PERCENTAGE = "0.64";

    public RikerSQLiteOpenHelper(final Context context) {
        super(context,
                context.getDatabasePath(RikerSQLUtil.RIKER_DATABASE_FILE_NAME).getAbsolutePath(),
                null,
                RikerSQLUtil.RIKER_CURRENT_DATABASE_VERSION);
        this.context = context;
        if (BuildConfig.DEBUG) {
            Timber.d("Riker database file: [" + context.getDatabasePath(RikerSQLUtil.RIKER_DATABASE_FILE_NAME).getAbsolutePath() + "]");
        }
    }

    @Override
    public final void onUpgrade(final SQLiteDatabase database, final int oldVersion, final int newVersion) {
        Timber.d("inside onUpgrade, oldVersion: [" + oldVersion + "], newVersion: [" + newVersion + "]");
        switch (newVersion) {
            case 2:
                Timber.d("Proceeding to process RIKER_DATABASE_VERSION_V2 updates");
                v2_updates(database);
                clearChartCache(database);
                Timber.d("RIKER_DATABASE_VERSION_V2 updates processed successfully.");
                // fall-through to apply next set of updates
        }
    }

    private static void clearChartCache(final SQLiteDatabase database) {
        database.delete(RikerTable.CHART_TIME_SERIES_DATA_POINT.tableName, null, null);
        database.delete(RikerTable.CHART_TIME_SERIES.tableName, null, null);
        database.delete(RikerTable.CHART_PIE_SLICE.tableName, null, null);
        database.delete(RikerTable.CHART.tableName, null, null);
    }

    private static void v2_updates(final SQLiteDatabase database) {
        final ContentValues contentValues = RikerDao.contentValues(newMuscleGroup(MuscleGroup.Id.CORE, BodySegment.Id.UPPER_BODY, "core", null));
        database.update(RikerTable.MUSCLE_GROUP.tableName,
                contentValues,
                String.format("%s = ?", CommonColumn.LOCAL_ID.name),
                new String[] { Integer.toString(MuscleGroup.Id.CORE.id) });
        RikerDao.insertMasterMuscleGroup(newMuscleGroup(MuscleGroup.Id.HIP_ABDUCTORS, BodySegment.Id.LOWER_BODY, "hip abductors", null), database);
        RikerDao.insertMasterMuscleGroup(newMuscleGroup(MuscleGroup.Id.HIP_FLEXORS, BodySegment.Id.LOWER_BODY, "hip flexors", null), database);
        RikerDao.insertMasterMuscle(newMuscle(Muscle.Id.OBLIQUES, MuscleGroup.Id.CORE, "obliques", null), database);
        RikerDao.insertMasterMuscle(newMuscle(Muscle.Id.HIP_ABDUCTORS, MuscleGroup.Id.HIP_ABDUCTORS, "hip abductors", null), database);
        RikerDao.insertMasterMuscle(newMuscle(Muscle.Id.HIP_FLEXORS, MuscleGroup.Id.HIP_FLEXORS, "hip flexors", null), database);
        int movementId = 125; // fyi, dumbbell rotational punch is ID 124 (current max movement ID)
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "high twist", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                Arrays.asList(MovementVariant.Id.CABLE.id), // variants
                0, // sort order
                Arrays.asList(Muscle.Id.OBLIQUES.id, // primary muscle ids
                        Muscle.Id.ABS_UPPER.id),
                Arrays.asList(), // secondary muscle ids
                Arrays.asList()), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "low twist", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                Arrays.asList(MovementVariant.Id.CABLE.id), // variants
                0, // sort order
                Arrays.asList(Muscle.Id.OBLIQUES.id, // primary muscle ids
                        Muscle.Id.ABS_LOWER.id),
                Arrays.asList(), // secondary muscle ids
                Arrays.asList()), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "close-grip pulldown", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                Arrays.asList(MovementVariant.Id.MACHINE.id,
                        MovementVariant.Id.CABLE.id), // variants
                0, // sort order
                Arrays.asList(Muscle.Id.BACK_UPPER.id, // primary muscle ids
                        Muscle.Id.BACK_LOWER.id),
                Arrays.asList(Muscle.Id.BICEPS.id), // secondary muscle ids
                Arrays.asList()), // aliases
                database);
        int movementAliasId = 61; // ('60' is the current max ID in alias table - 'rear lateral raises')
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "delt flys", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                Arrays.asList(MovementVariant.Id.MACHINE.id,
                        MovementVariant.Id.CABLE.id), // variants
                0, // sort order
                Arrays.asList(Muscle.Id.DELTS_REAR.id), // primary muscle ids
                Arrays.asList(Muscle.Id.BACK_UPPER.id), // secondary muscle ids
                Arrays.asList(Arrays.asList(movementAliasId++, "rear flys"),
                        Arrays.asList(movementAliasId++, "rear delt flys"))), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "hip abduction", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                Arrays.asList(MovementVariant.Id.CABLE.id), // variants
                0, // sort order
                Arrays.asList(Muscle.Id.HIP_ABDUCTORS.id), // primary muscle ids
                Arrays.asList(), // secondary muscle ids
                Arrays.asList()), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "hip flexor", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                Arrays.asList(MovementVariant.Id.CABLE.id), // variants
                0, // sort order
                Arrays.asList(Muscle.Id.HIP_FLEXORS.id), // primary muscle ids
                Arrays.asList(), // secondary muscle ids
                Arrays.asList()), // aliases
                database);
    }

    @Override
    public final void onCreate(final SQLiteDatabase database) {
        // process DDL
        database.execSQL(RikerSQLUtil.v1_masterUserDDL());
        database.execSQL(RikerSQLUtil.v1_masterUserSettingsDDL());
        database.execSQL(RikerSQLUtil.v1_masterBodySegmentDDL());
        database.execSQL(RikerSQLUtil.v1_masterMuscleGroupDDL());
        database.execSQL(RikerSQLUtil.v1_masterMuscleDDL());
        database.execSQL(RikerSQLUtil.v1_masterMuscleAliasDDL());
        database.execSQL(RikerSQLUtil.v1_masterMovementDDL());
        database.execSQL(RikerSQLUtil.v1_masterMovementVariantDDL());
        database.execSQL(RikerSQLUtil.v1_masterMovementPrimaryMuscleDDL());
        database.execSQL(RikerSQLUtil.v1_masterMovementSecondaryMuscleDDL());
        database.execSQL(RikerSQLUtil.v1_masterMovementAliasDDL());
        database.execSQL(RikerSQLUtil.v1_masterOriginationDeviceDDL());
        database.execSQL(RikerSQLUtil.v1_masterSetDDL());
        database.execSQL(RikerSQLUtil.v1_masterBmlDDL());
        database.execSQL(RikerSQLUtil.v1_chartConfigDDL());
        database.execSQL(RikerSQLUtil.v1_chartDDL());
        database.execSQL(RikerSQLUtil.v1_chartPieSliceDDL());
        database.execSQL(RikerSQLUtil.v1_chartTimeSeriesDDL());
        database.execSQL(RikerSQLUtil.v1_chartTimeSeriesDataPointDDL());
        // reference data loading
        v1_dataLoading_bodySegments(database);
        v1_dataLoading_originationDevices(database);
        v1_dataLoading_muscleGroups(database);
        v1_dataLoading_muscles(database);
        v1_dataLoading_muscleAliases(database);
        v1_dataLoading_movementVariants(database);
        v1_dataLoading_movements(database);
        // version 2 updates
        v2_updates(database);
    }

    /*==============================================================================================
       Helpers
     =============================================================================================*/
    public static void setDefaultCreatedAtUpdatedAtDates(final MasterSupport masterSupport) {
        final Date date = new Date(1474546826849L); // I forget why I chose this date, but matches with what I do in iOS
        masterSupport.createdAt = date;
        masterSupport.updatedAt = date;
    }

    /*==============================================================================================
       Movements
     =============================================================================================*/
    private static List<Object> newMovementWithAliases(final int id,
                                                       final String canonicalName,
                                                       final boolean isBodyLift,
                                                       final BigDecimal percentageOfBodyWeight,
                                                       final Integer variantMask,
                                                       final Integer sortOrder,
                                                       final List<Integer> primaryMuscleIdList,
                                                       final List<Integer> secondaryMuscleIdList,
                                                       final List<List<Object>> aliasList) {
        final Movement movement = new Movement();
        movement.localIdentifier = id;
        movement.canonicalName = canonicalName;
        movement.isBodyLift = isBodyLift;
        movement.percentageOfBodyWeight = percentageOfBodyWeight;
        movement.variantMask = variantMask;
        movement.sortOrder = sortOrder;
        movement.primaryMuscleIdList = primaryMuscleIdList;
        movement.secondaryMuscleIdList = secondaryMuscleIdList;
        movement.globalIdentifier = Utils.globalIdentifier(UriPathPart.MOVEMENTS, id);
        setDefaultCreatedAtUpdatedAtDates(movement);
        final List<MovementAlias> movementAliasList = new ArrayList<>();
        if (aliasList != null) {
            for (final List<Object> aliasPair : aliasList) {
                final MovementAlias movementAlias = new MovementAlias();
                movementAlias.movementId = id;
                movementAlias.localIdentifier = (Integer)aliasPair.get(0);
                movementAlias.alias = (String)aliasPair.get(1);
                movementAlias.globalIdentifier = Utils.globalIdentifier(UriPathPart.MOVEMENT_ALIASES, movementAlias.localIdentifier);
                setDefaultCreatedAtUpdatedAtDates(movementAlias);
                movementAliasList.add(movementAlias);
            }
        }
        return Arrays.asList(movement, movementAliasList);
    }

    // improved (makes it easier to set variant mask)
    private static List<Object> newMovementWithAliasesImp(final int id,
                                                          final String canonicalName,
                                                          final boolean isBodyLift,
                                                          final BigDecimal percentageOfBodyWeight,
                                                          final List<Integer> variants,
                                                          final Integer sortOrder,
                                                          final List<Integer> primaryMuscleIdList,
                                                          final List<Integer> secondaryMuscleIdList,
                                                          final List<List<Object>> aliasList) {
        int variantMask = 0;
        if (variants != null) {
            for (final int variant : variants) {
                variantMask |= variant;
            }
        }
        return newMovementWithAliases(id,
                canonicalName,
                isBodyLift,
                percentageOfBodyWeight,
                variantMask,
                sortOrder,
                primaryMuscleIdList,
                secondaryMuscleIdList,
                aliasList);
    }

    private static void v1_dataLoading_movements(final SQLiteDatabase database) {
        RikerDao.insertMasterMovement(newMovementWithAliases(
                0, // local master identifier
                "bench press", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                31, // variant mask
                0, // sort order
                Arrays.asList(8, 9), // primary muscle ids
                Arrays.asList(2, 13, 20, 22, 23, 24), // secondary muscle ids
                Arrays.asList(Arrays.asList(56, "chest press"))), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliases(
                1, // local master identifier
                "incline bench press", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                31, // variant mask
                0, // sort order
                Arrays.asList(8, 2), // primary muscle ids
                Arrays.asList(9, 13, 20, 22, 23, 24), // secondary muscle ids
                null), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliases(
                2, // local master identifier
                "decline bench press", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                31, // variant mask
                0, // sort order
                Arrays.asList(9), // primary muscle ids
                Arrays.asList(8, 2, 13, 20, 22, 23, 24), // secondary muscle ids
                null), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliases(
                3, // local master identifier
                "flys", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                22, // variant mask
                0, // sort order
                Arrays.asList(8, 9), // primary muscle ids
                Arrays.asList(), // secondary muscle ids
                Arrays.asList(Arrays.asList(1, "chest fly"),
                        Arrays.asList(2, "pectoral fly"),
                        Arrays.asList(3, "pec fly"))), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliases(
                4, // local master identifier
                "incline flys", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                22, // variant mask
                0, // sort order
                Arrays.asList(8), // primary muscle ids
                Arrays.asList(), // secondary muscle ids
                Arrays.asList(Arrays.asList(4, "incline chest fly"),
                        Arrays.asList(5, "incline pectoral fly"),
                        Arrays.asList(6, "incline pec fly"))), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliases(
                5, // local master identifier
                "decline flys", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                22, // variant mask
                0, // sort order
                Arrays.asList(9), // primary muscle ids
                Arrays.asList(), // secondary muscle ids
                Arrays.asList(Arrays.asList(7, "decline chest fly"),
                        Arrays.asList(8, "decline pectoral fly"),
                        Arrays.asList(9, "decline pec fly"))), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliases(
                6, // local master identifier
                "push-up", // canonical getName
                true, // is body lift?
                new BigDecimal("0.64"), // percentage of body weight
                0, // variant mask
                0, // sort order
                Arrays.asList(8, 9), // primary muscle ids
                Arrays.asList(2, 13, 20, 22, 23, 24), // secondary muscle ids
                Arrays.asList(Arrays.asList(10, "press-up"),
                        Arrays.asList(11, "floor dip"))), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliases(
                7, // local master identifier
                "one arm push-up", // canonical getName
                true, // is body lift?
                new BigDecimal(PUSH_UP_BODY_WEIGHT_PERCENTAGE), // percentage of body weight
                0, // variant mask
                0, // sort order
                Arrays.asList(8, 9), // primary muscle ids
                Arrays.asList(2, 13, 20, 22, 23, 24), // secondary muscle ids
                null), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliases(
                8, // local master identifier
                "wide grip dips", // canonical getName
                true, // is body lift?
                new BigDecimal("0.95"), // percentage of body weight
                132, // variant mask
                0, // sort order
                Arrays.asList(8, 9), // primary muscle ids
                Arrays.asList(2, 22, 23, 24), // secondary muscle ids
                Arrays.asList(Arrays.asList(12, "chest dips"))), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliases(
                9, // local master identifier
                "pulldowns", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                20, // variant mask
                0, // sort order
                Arrays.asList(5, 6), // primary muscle ids
                Arrays.asList(18), // secondary muscle ids
                Arrays.asList(Arrays.asList(13, "lat pulldowns"))), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliases(
                10, // local master identifier
                "rows", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                20, // variant mask
                0, // sort order
                Arrays.asList(5, 6), // primary muscle ids
                Arrays.asList(18), // secondary muscle ids
                null), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliases(
                11, // local master identifier
                "t-bar rows", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                1, // variant mask
                0, // sort order
                Arrays.asList(5, 6), // primary muscle ids
                Arrays.asList(18), // secondary muscle ids
                null), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliases(
                12, // local master identifier
                "bent-over rows", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                9, // variant mask
                0, // sort order
                Arrays.asList(5, 6), // primary muscle ids
                Arrays.asList(18), // secondary muscle ids
                Arrays.asList(Arrays.asList(14, "barbell rows"))), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliases(
                13, // local master identifier
                "one arm bent-over rows", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                2, // variant mask
                0, // sort order
                Arrays.asList(5, 6), // primary muscle ids
                Arrays.asList(18), // secondary muscle ids
                null), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliases(
                14, // local master identifier
                "good-mornings", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                1, // variant mask
                0, // sort order
                Arrays.asList(6), // primary muscle ids
                Arrays.asList(15, 21), // secondary muscle ids
                null), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliases(
                15, // local master identifier
                "shoulder press", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                31, // variant mask
                0, // sort order
                Arrays.asList(2, 3), // primary muscle ids
                Arrays.asList(22, 23, 24, 20), // secondary muscle ids
                Arrays.asList(Arrays.asList(15, "overhead press"),
                        Arrays.asList(16, "press behind the neck"))), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliases(
                16, // local master identifier
                "Arnold press", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                2, // variant mask
                0, // sort order
                Arrays.asList(2, 3), // primary muscle ids
                Arrays.asList(22, 23, 24, 20), // secondary muscle ids
                null), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliases(
                17, // local master identifier
                "military press", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                9, // variant mask
                0, // sort order
                Arrays.asList(2, 3), // primary muscle ids
                Arrays.asList(22, 23, 24, 20), // secondary muscle ids
                Arrays.asList(Arrays.asList(17, "front shoulder press"))), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliases(
                18, // local master identifier
                "clean and press", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                1, // variant mask
                0, // sort order
                Arrays.asList(2, 3), // primary muscle ids
                Arrays.asList(22, 23, 24, 20), // secondary muscle ids
                null), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliases(
                19, // local master identifier
                "push press", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                1, // variant mask
                0, // sort order
                Arrays.asList(2, 3), // primary muscle ids
                Arrays.asList(22, 23, 24, 20), // secondary muscle ids
                null), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliases(
                20, // local master identifier
                "lateral raises", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                18, // variant mask
                0, // sort order
                Arrays.asList(3), // primary muscle ids
                Arrays.asList(1), // secondary muscle ids
                null), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliases(
                21, // local master identifier
                "front raises", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                18, // variant mask
                0, // sort order
                Arrays.asList(2), // primary muscle ids
                null, // secondary muscle ids
                null), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliases(
                22, // local master identifier
                "cross cable laterals", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                16, // variant mask
                0, // sort order
                Arrays.asList(1, 3), // primary muscle ids
                null, // secondary muscle ids
                null), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliases(
                23, // local master identifier
                "overhead laterals", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                2, // variant mask
                0, // sort order
                Arrays.asList(2), // primary muscle ids
                Arrays.asList(20), // secondary muscle ids
                null), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliases(
                24, // local master identifier
                "bent-over laterals", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                18, // variant mask
                0, // sort order
                Arrays.asList(1), // primary muscle ids
                Arrays.asList(), // secondary muscle ids
                Arrays.asList(Arrays.asList(18, "reverse fly"),
                        Arrays.asList(19, "rear delt fly"),
                        Arrays.asList(20, "inverted fly"))), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliases(
                25, // local master identifier
                "upright rows", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                9, // variant mask
                0, // sort order
                Arrays.asList(20, 2), // primary muscle ids
                null, // secondary muscle ids
                null), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliases(
                26, // local master identifier
                "shrugs", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                11, // variant mask
                0, // sort order
                Arrays.asList(20), // primary muscle ids
                null, // secondary muscle ids
                null), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliases(
                27, // local master identifier
                "dips", // canonical getName
                true, // is body lift?
                new BigDecimal("0.95"), // percentage of body weight
                132, // variant mask
                0, // sort order
                Arrays.asList(22, 23, 24), // primary muscle ids
                Arrays.asList(2, 8, 9), // secondary muscle ids
                Arrays.asList(Arrays.asList(21, "shoulder width dips"),
                        Arrays.asList(22, "tricep dips"))), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliases(
                28, // local master identifier
                "close-grip bench press", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                9, // variant mask
                0, // sort order
                Arrays.asList(22, 23, 24), // primary muscle ids
                Arrays.asList(8, 9), // secondary muscle ids
                null), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliases(
                29, // local master identifier
                "pushdowns", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                16, // variant mask
                0, // sort order
                Arrays.asList(22), // primary muscle ids
                Arrays.asList(23, 24), // secondary muscle ids
                Arrays.asList(Arrays.asList(28, "press-downs"))), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliases(
                30, // local master identifier
                "tricep extensions", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                34, // variant mask
                0, // sort order
                Arrays.asList(23), // primary muscle ids
                Arrays.asList(22, 24), // secondary muscle ids
                Arrays.asList(Arrays.asList(23, "skull crushers"),
                        Arrays.asList(24, "french press"),
                        Arrays.asList(25, "french extensions"))), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliases(
                31, // local master identifier
                "leg press", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                64, // variant mask
                0, // sort order
                Arrays.asList(14, 15, 21), // primary muscle ids
                Arrays.asList(16), // secondary muscle ids
                null), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliases(
                32, // local master identifier
                "deadlifts", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                1, // variant mask
                0, // sort order
                Arrays.asList(14, 15, 21, 5, 6), // primary muscle ids
                Arrays.asList(19, 11, 12), // secondary muscle ids
                null), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliases(
                33, // local master identifier
                "squats", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                9, // variant mask
                0, // sort order
                Arrays.asList(14), // primary muscle ids
                Arrays.asList(15, 21), // secondary muscle ids
                Arrays.asList(Arrays.asList(48, "back squats"), // aliases
                        Arrays.asList(49, "barbell full squats"))),
                database);
        RikerDao.insertMasterMovement(newMovementWithAliases(
                34, // local master identifier
                "leg extensions", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                4, // variant mask
                0, // sort order
                Arrays.asList(14), // primary muscle ids
                null, // secondary muscle ids
                null), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliases(
                35, // local master identifier
                "wrist curls", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                51, // variant mask
                0, // sort order
                Arrays.asList(19), // primary muscle ids
                null, // secondary muscle ids
                null), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliases(
                36, // local master identifier
                "calf raises", // canonical getName
                false, // is body lift?
                new BigDecimal("1.0"), // percentage of body weight
                140, // variant mask
                0, // sort order
                Arrays.asList(16), // primary muscle ids
                null, // secondary muscle ids
                null), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliases(
                37, // local master identifier
                "curls", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                55, // variant mask
                0, // sort order
                Arrays.asList(18), // primary muscle ids
                null, // secondary muscle ids
                null), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliases(
                38, // local master identifier
                "sit ups", // canonical getName
                true, // is body lift?
                new BigDecimal("0.50"), // percentage of body weight
                0, // variant mask
                0, // sort order
                Arrays.asList(11, 12), // primary muscle ids
                null, // secondary muscle ids
                null), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliases(
                39, // local master identifier
                "pull-ups", // canonical getName
                true, // is body lift?
                new BigDecimal("1.0"), // percentage of body weight
                0, // variant mask
                0, // sort order
                Arrays.asList(5), // primary muscle ids
                Arrays.asList(18), // secondary muscle ids
                Arrays.asList(Arrays.asList(26, "chins"))), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliases(
                40, // local master identifier
                "wide grip pull-ups", // canonical getName
                true, // is body lift?
                new BigDecimal("1.0"), // percentage of body weight
                0, // variant mask
                0, // sort order
                Arrays.asList(5), // primary muscle ids
                null, // secondary muscle ids
                Arrays.asList(Arrays.asList(27, "wide grip chins"))), // aliases
                database);
        /**
         Need to make sure that the order of these insMovementImp statements is the
         same order as they appear in ref_data.clj on the server.
         */
        int movementAliasId = 50; // ('49' is the current max ID in alias table 'barbell full squats')
        int movementId = 41;
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "ab roller", // canonical getName
                true, // is body lift?
                new BigDecimal("0.50"), // percentage of body weight
                Arrays.asList(), // variants
                0, // sort order
                Arrays.asList(Muscle.Id.ABS_UPPER.id,
                        Muscle.Id.ABS_LOWER.id), // primary muscle ids
                Arrays.asList(Muscle.Id.BACK_UPPER.id,
                        Muscle.Id.BACK_LOWER.id,
                        Muscle.Id.TRICEP_LAT.id,
                        Muscle.Id.TRICEP_LONG.id,
                        Muscle.Id.TRICEP_MED.id,
                        Muscle.Id.TRAPS.id), // secondary muscle ids
                Arrays.asList()), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "crunches", // canonical getName
                true, // is body lift?
                new BigDecimal("0.20"), // percentage of body weight
                Arrays.asList(), // variants
                0, // sort order
                Arrays.asList(Muscle.Id.ABS_UPPER.id,
                        Muscle.Id.ABS_LOWER.id), // primary muscle ids
                Arrays.asList(), // secondary muscle ids
                Arrays.asList()), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "bicycle crunches", // canonical getName
                true, // is body lift?
                new BigDecimal("0.20"), // percentage of body weight
                Arrays.asList(), // variants
                0, // sort order
                Arrays.asList(Muscle.Id.ABS_UPPER.id,
                        Muscle.Id.ABS_LOWER.id), // primary muscle ids
                Arrays.asList(), // secondary muscle ids
                Arrays.asList()), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "leg raises", // canonical getName
                true, // is body lift?
                new BigDecimal("0.40"), // percentage of body weight
                Arrays.asList(), // variants
                0, // sort order
                Arrays.asList(Muscle.Id.ABS_UPPER.id,
                        Muscle.Id.ABS_LOWER.id), // primary muscle ids
                Arrays.asList(), // secondary muscle ids
                Arrays.asList()), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "Russian twists", // canonical getName
                true, // is body lift?
                new BigDecimal("0.30"), // percentage of body weight
                Arrays.asList(), // variants
                0, // sort order
                Arrays.asList(Muscle.Id.ABS_UPPER.id,
                        Muscle.Id.ABS_LOWER.id), // primary muscle ids
                Arrays.asList(), // secondary muscle ids
                Arrays.asList()), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "pelvic lifts", // canonical getName
                true, // is body lift?
                new BigDecimal("0.20"), // percentage of body weight
                Arrays.asList(), // variants
                0, // sort order
                Arrays.asList(Muscle.Id.BACK_LOWER.id,
                        Muscle.Id.GLUTES.id,
                        Muscle.Id.ABS_UPPER.id,
                        Muscle.Id.ABS_LOWER.id), // primary muscle ids
                Arrays.asList(), // secondary muscle ids
                Arrays.asList(Arrays.asList(29, "pelvic tilts"))), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "cable crunches", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                Arrays.asList(MovementVariant.Id.CABLE.id), // variants
                0, // sort order
                Arrays.asList(Muscle.Id.ABS_UPPER.id,
                        Muscle.Id.ABS_LOWER.id), // primary muscle ids
                Arrays.asList(), // secondary muscle ids
                Arrays.asList()), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "jackknifes", // canonical getName
                true, // is body lift?
                new BigDecimal("0.40"), // percentage of body weight
                Arrays.asList(), // variants
                0, // sort order
                Arrays.asList(Muscle.Id.ABS_UPPER.id,
                        Muscle.Id.ABS_LOWER.id), // primary muscle ids
                Arrays.asList(), // secondary muscle ids
                Arrays.asList()), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "knee raises", // canonical getName
                true, // is body lift?
                new BigDecimal("0.40"), // percentage of body weight
                Arrays.asList(), // variants
                0, // sort order
                Arrays.asList(Muscle.Id.ABS_UPPER.id,
                        Muscle.Id.ABS_LOWER.id), // primary muscle ids
                Arrays.asList(), // secondary muscle ids
                Arrays.asList(Arrays.asList(30, "hip raises"))), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "V-ups", // canonical getName
                true, // is body lift?
                new BigDecimal("0.75"), // percentage of body weight
                Arrays.asList(), // variants
                0, // sort order
                Arrays.asList(Muscle.Id.ABS_UPPER.id,
                        Muscle.Id.ABS_LOWER.id), // primary muscle ids
                Arrays.asList(), // secondary muscle ids
                Arrays.asList()), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "woodchoppers", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                Arrays.asList(MovementVariant.Id.CABLE.id), // variants
                0, // sort order
                Arrays.asList(Muscle.Id.ABS_UPPER.id,
                        Muscle.Id.ABS_LOWER.id), // primary muscle ids
                Arrays.asList(), // secondary muscle ids
                Arrays.asList(Arrays.asList(31, "standing cable wood chop"))), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "dirty dog", // canonical getName
                true, // is body lift?
                new BigDecimal("0.20"), // percentage of body weight
                Arrays.asList(), // variants
                0, // sort order
                Arrays.asList(Muscle.Id.GLUTES.id), // primary muscle ids
                Arrays.asList(), // secondary muscle ids
                Arrays.asList(Arrays.asList(32, "hip side lifts"),
                        Arrays.asList(33, "fire hydrant exercise"))), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "hip thrust", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                Arrays.asList(MovementVariant.Id.BARBELL.id), // variants
                0, // sort order
                Arrays.asList(Muscle.Id.GLUTES.id), // primary muscle ids
                Arrays.asList(Muscle.Id.QUADS.id), // secondary muscle ids
                Arrays.asList(Arrays.asList(34, "bridge"),
                        Arrays.asList(35, "weighted hip extension"))), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "kettlebell swing", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                Arrays.asList(MovementVariant.Id.KETTLEBELL.id), // variants
                0, // sort order
                Arrays.asList(Muscle.Id.GLUTES.id,
                        Muscle.Id.HAMS.id,
                        Muscle.Id.ABS_UPPER.id,
                        Muscle.Id.ABS_LOWER.id,
                        Muscle.Id.DELTS_FRONT.id,
                        Muscle.Id.DELTS_SIDE.id,
                        Muscle.Id.DELTS_REAR.id), // primary muscle ids
                Arrays.asList(Muscle.Id.QUADS.id,
                        Muscle.Id.CHEST_UPPER.id,
                        Muscle.Id.CHEST_LOWER.id,
                        Muscle.Id.FOREARMS.id), // secondary muscle ids
                Arrays.asList()), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "lunges", // canonical getName
                false, // is body lift?
                new BigDecimal("0.75"), // percentage of body weight
                Arrays.asList(MovementVariant.Id.BODY.id,
                        MovementVariant.Id.DUMBBELL.id,
                        MovementVariant.Id.BARBELL.id), // variants
                0, // sort order
                Arrays.asList(Muscle.Id.GLUTES.id,
                        Muscle.Id.QUADS.id), // primary muscle ids
                Arrays.asList(Muscle.Id.HAMS.id), // secondary muscle ids
                Arrays.asList()), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "box squats", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                Arrays.asList(MovementVariant.Id.SMITH_MACHINE.id,
                        MovementVariant.Id.BARBELL.id), // variants
                0, // sort order
                Arrays.asList(Muscle.Id.QUADS.id), // primary muscle ids
                Arrays.asList(Muscle.Id.HAMS.id, // secondary muscle ids
                        Muscle.Id.GLUTES.id),
                Arrays.asList()), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "front squats", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                Arrays.asList(MovementVariant.Id.SMITH_MACHINE.id,
                        MovementVariant.Id.BARBELL.id), // variants
                0, // sort order
                Arrays.asList(Muscle.Id.QUADS.id), // primary muscle ids
                Arrays.asList(), // secondary muscle ids
                Arrays.asList()), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "hack squats", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                Arrays.asList(MovementVariant.Id.SLED.id,
                        MovementVariant.Id.BARBELL.id), // variants
                0, // sort order
                Arrays.asList(Muscle.Id.QUADS.id), // primary muscle ids
                Arrays.asList(Muscle.Id.HAMS.id, // secondary muscle ids
                        Muscle.Id.GLUTES.id),
                Arrays.asList()), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "split squats", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                Arrays.asList(MovementVariant.Id.DUMBBELL.id), // variants
                0, // sort order
                Arrays.asList(Muscle.Id.QUADS.id), // primary muscle ids
                Arrays.asList(Muscle.Id.HAMS.id, // secondary muscle ids
                        Muscle.Id.GLUTES.id),
                Arrays.asList(Arrays.asList(36, "bulgarian split squats"))), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "overhead squats", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                Arrays.asList(MovementVariant.Id.BARBELL.id), // variants
                0, // sort order
                Arrays.asList(Muscle.Id.QUADS.id), // primary muscle ids
                Arrays.asList(Muscle.Id.BACK_UPPER.id, // secondary muscle ids
                        Muscle.Id.GLUTES.id,
                        Muscle.Id.HAMS.id),
                Arrays.asList()), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "press unders", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                Arrays.asList(MovementVariant.Id.BARBELL.id), // variants
                0, // sort order
                Arrays.asList(Muscle.Id.QUADS.id), // primary muscle ids
                Arrays.asList(Muscle.Id.BACK_UPPER.id), // secondary muscle ids
                Arrays.asList()), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "clean", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                Arrays.asList(MovementVariant.Id.BARBELL.id), // variants
                0, // sort order
                Arrays.asList(Muscle.Id.QUADS.id), // primary muscle ids
                Arrays.asList(Muscle.Id.BACK_LOWER.id,
                        Muscle.Id.DELTS_FRONT.id,
                        Muscle.Id.DELTS_REAR.id,
                        Muscle.Id.DELTS_SIDE.id,
                        Muscle.Id.CALFS.id,
                        Muscle.Id.GLUTES.id,
                        Muscle.Id.HAMS.id,
                        Muscle.Id.TRAPS.id,
                        Muscle.Id.BACK_UPPER.id), // secondary muscle ids
                Arrays.asList()), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "clean and jerk", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                Arrays.asList(MovementVariant.Id.BARBELL.id), // variants
                0, // sort order
                Arrays.asList(Muscle.Id.QUADS.id), // primary muscle ids
                Arrays.asList(Muscle.Id.BACK_LOWER.id,
                        Muscle.Id.TRICEP_MED.id,
                        Muscle.Id.TRICEP_LAT.id,
                        Muscle.Id.TRICEP_LONG.id,
                        Muscle.Id.FOREARMS.id,
                        Muscle.Id.ABS_LOWER.id,
                        Muscle.Id.ABS_UPPER.id,
                        Muscle.Id.DELTS_FRONT.id,
                        Muscle.Id.DELTS_REAR.id,
                        Muscle.Id.DELTS_SIDE.id,
                        Muscle.Id.CALFS.id,
                        Muscle.Id.GLUTES.id,
                        Muscle.Id.HAMS.id,
                        Muscle.Id.TRAPS.id,
                        Muscle.Id.BACK_UPPER.id), // secondary muscle ids
                Arrays.asList()), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "split jerk", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                Arrays.asList(MovementVariant.Id.BARBELL.id), // variants
                0, // sort order
                Arrays.asList(Muscle.Id.QUADS.id), // primary muscle ids
                Arrays.asList(Muscle.Id.BACK_LOWER.id,
                        Muscle.Id.TRICEP_MED.id,
                        Muscle.Id.TRICEP_LAT.id,
                        Muscle.Id.TRICEP_LONG.id,
                        Muscle.Id.FOREARMS.id,
                        Muscle.Id.ABS_LOWER.id,
                        Muscle.Id.ABS_UPPER.id,
                        Muscle.Id.DELTS_FRONT.id,
                        Muscle.Id.DELTS_REAR.id,
                        Muscle.Id.DELTS_SIDE.id,
                        Muscle.Id.CALFS.id,
                        Muscle.Id.GLUTES.id,
                        Muscle.Id.HAMS.id,
                        Muscle.Id.TRAPS.id,
                        Muscle.Id.BACK_UPPER.id), // secondary muscle ids
                Arrays.asList()), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "hang clean", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                Arrays.asList(MovementVariant.Id.BARBELL.id), // variants
                0, // sort order
                Arrays.asList(Muscle.Id.QUADS.id), // primary muscle ids
                Arrays.asList(Muscle.Id.BACK_LOWER.id,
                        Muscle.Id.DELTS_FRONT.id,
                        Muscle.Id.DELTS_REAR.id,
                        Muscle.Id.DELTS_SIDE.id,
                        Muscle.Id.CALFS.id,
                        Muscle.Id.GLUTES.id,
                        Muscle.Id.HAMS.id,
                        Muscle.Id.TRAPS.id,
                        Muscle.Id.BACK_UPPER.id), // secondary muscle ids
                Arrays.asList()), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "jump squat", // canonical getName
                true, // is body lift?
                new BigDecimal("0.50"), // percentage of body weight
                Arrays.asList(), // variants
                0, // sort order
                Arrays.asList(Muscle.Id.QUADS.id), // primary muscle ids
                Arrays.asList(Muscle.Id.CALFS.id, // secondary muscle ids
                        Muscle.Id.HAMS.id,
                        Muscle.Id.GLUTES.id),
                Arrays.asList()), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "leg curl", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                Arrays.asList(MovementVariant.Id.MACHINE.id), // variants
                0, // sort order
                Arrays.asList(Muscle.Id.HAMS.id), // primary muscle ids
                Arrays.asList(), // secondary muscle ids
                Arrays.asList(Arrays.asList(37, "hamstring curl"))), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "hang snatch", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                Arrays.asList(MovementVariant.Id.BARBELL.id), // variants
                0, // sort order
                Arrays.asList(Muscle.Id.HAMS.id), // primary muscle ids
                Arrays.asList(Muscle.Id.BACK_LOWER.id,
                        Muscle.Id.QUADS.id,
                        Muscle.Id.DELTS_FRONT.id,
                        Muscle.Id.DELTS_REAR.id,
                        Muscle.Id.DELTS_SIDE.id,
                        Muscle.Id.CALFS.id,
                        Muscle.Id.GLUTES.id,
                        Muscle.Id.TRAPS.id,
                        Muscle.Id.BACK_UPPER.id), // secondary muscle ids
                Arrays.asList()), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "stiff legged deadlift", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                Arrays.asList(MovementVariant.Id.BARBELL.id), // variants
                0, // sort order
                Arrays.asList(Muscle.Id.GLUTES.id,
                        Muscle.Id.HAMS.id), // primary muscle ids
                Arrays.asList(), // secondary muscle ids
                Arrays.asList()), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "Romanian deadlift", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                Arrays.asList(MovementVariant.Id.BARBELL.id), // variants
                0, // sort order
                Arrays.asList(Muscle.Id.GLUTES.id,
                        Muscle.Id.HAMS.id), // primary muscle ids
                Arrays.asList(), // secondary muscle ids
                Arrays.asList()), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "sumo deadlift", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                Arrays.asList(MovementVariant.Id.BARBELL.id), // variants
                0, // sort order
                Arrays.asList(Muscle.Id.HAMS.id), // primary muscle ids
                Arrays.asList(Muscle.Id.GLUTES.id,
                        Muscle.Id.QUADS.id), // secondary muscle ids
                Arrays.asList()), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "back extension", // canonical getName
                false, // is body lift?
                new BigDecimal("0.50"), // percentage of body weight
                Arrays.asList(MovementVariant.Id.MACHINE.id,
                        MovementVariant.Id.BODY.id), // variants
                0, // sort order
                Arrays.asList(Muscle.Id.BACK_LOWER.id), // primary muscle ids
                Arrays.asList(Muscle.Id.BACK_UPPER.id), // secondary muscle ids
                Arrays.asList(Arrays.asList(38, "hyperextension"))), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "chin-up", // canonical getName
                true, // is body lift?
                new BigDecimal("1.00"), // percentage of body weight
                Arrays.asList(), // variants
                0, // sort order
                Arrays.asList(Muscle.Id.BICEPS.id,
                        Muscle.Id.BACK_UPPER.id), // primary muscle ids
                Arrays.asList(Muscle.Id.BACK_LOWER.id), // secondary muscle ids
                Arrays.asList(Arrays.asList(39, "chin"))), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "muscle-up", // canonical getName
                true, // is body lift?
                new BigDecimal("1.00"), // percentage of body weight
                Arrays.asList(), // variants
                0, // sort order
                Arrays.asList(Muscle.Id.BICEPS.id,
                        Muscle.Id.TRICEP_LONG.id,
                        Muscle.Id.TRICEP_LAT.id,
                        Muscle.Id.TRICEP_MED.id,
                        Muscle.Id.BACK_UPPER.id), // primary muscle ids
                Arrays.asList(Muscle.Id.BACK_LOWER.id), // secondary muscle ids
                Arrays.asList()), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "inverted row", // canonical getName
                true, // is body lift?
                new BigDecimal("0.80"), // percentage of body weight
                Arrays.asList(), // variants
                0, // sort order
                Arrays.asList(Muscle.Id.TRAPS.id,
                        Muscle.Id.BACK_UPPER.id), // primary muscle ids
                Arrays.asList(Muscle.Id.BICEPS.id), // secondary muscle ids
                Arrays.asList(Arrays.asList(40, "supine row"))), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "jump shrug", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                Arrays.asList(MovementVariant.Id.BARBELL.id), // variants
                0, // sort order
                Arrays.asList(Muscle.Id.QUADS.id,
                        Muscle.Id.TRAPS.id,
                        Muscle.Id.BACK_UPPER.id), // primary muscle ids
                Arrays.asList(Muscle.Id.CALFS.id,
                        Muscle.Id.GLUTES.id,
                        Muscle.Id.HAMS.id,
                        Muscle.Id.BACK_LOWER.id), // secondary muscle ids
                Arrays.asList()), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "kipping pull-ups", // canonical getName
                true, // is body lift?
                new BigDecimal("1.00"), // percentage of body weight
                Arrays.asList(), // variants
                0, // sort order
                Arrays.asList(Muscle.Id.BACK_UPPER.id,
                        Muscle.Id.BACK_LOWER.id), // primary muscle ids
                Arrays.asList(Muscle.Id.BICEPS.id), // secondary muscle ids
                Arrays.asList(Arrays.asList(41, "pull-up with kip"))), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "Pendlay rows", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                Arrays.asList(MovementVariant.Id.BARBELL.id), // variants
                0, // sort order
                Arrays.asList(Muscle.Id.BACK_UPPER.id,
                        Muscle.Id.BACK_LOWER.id), // primary muscle ids
                Arrays.asList(), // secondary muscle ids
                Arrays.asList()), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "hammer curls", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                Arrays.asList(MovementVariant.Id.DUMBBELL.id), // variants
                0, // sort order
                Arrays.asList(Muscle.Id.FOREARMS.id), // primary muscle ids
                Arrays.asList(Muscle.Id.BICEPS.id), // secondary muscle ids
                Arrays.asList()), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "reverse barbell curls", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                Arrays.asList(MovementVariant.Id.BARBELL.id), // variants
                0, // sort order
                Arrays.asList(Muscle.Id.FOREARMS.id), // primary muscle ids
                Arrays.asList(Muscle.Id.BICEPS.id), // secondary muscle ids
                Arrays.asList()), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "concentration curls", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                Arrays.asList(MovementVariant.Id.DUMBBELL.id), // variants
                0, // sort order
                Arrays.asList(Muscle.Id.BICEPS.id), // primary muscle ids
                Arrays.asList(), // secondary muscle ids
                Arrays.asList()), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "preacher curls", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                Arrays.asList(MovementVariant.Id.CURL_BAR.id,
                        MovementVariant.Id.BARBELL.id,
                        MovementVariant.Id.CABLE.id,
                        MovementVariant.Id.MACHINE.id,
                        MovementVariant.Id.DUMBBELL.id), // variants
                0, // sort order
                Arrays.asList(Muscle.Id.BICEPS.id), // primary muscle ids
                Arrays.asList(), // secondary muscle ids
                Arrays.asList()), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "around the worlds", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                Arrays.asList(MovementVariant.Id.DUMBBELL.id), // variants
                0, // sort order
                Arrays.asList(Muscle.Id.CHEST_UPPER.id,
                        Muscle.Id.CHEST_LOWER.id), // primary muscle ids
                Arrays.asList(), // secondary muscle ids
                Arrays.asList()), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "cable crossovers", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                Arrays.asList(MovementVariant.Id.CABLE.id), // variants
                0, // sort order
                Arrays.asList(Muscle.Id.CHEST_UPPER.id,
                        Muscle.Id.CHEST_LOWER.id), // primary muscle ids
                Arrays.asList(), // secondary muscle ids
                Arrays.asList()), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "kneeling push-up", // canonical getName
                true, // is body lift?
                new BigDecimal("0.40"), // percentage of body weight
                Arrays.asList(), // variants
                0, // sort order
                Arrays.asList(Muscle.Id.CHEST_UPPER.id,
                        Muscle.Id.CHEST_LOWER.id), // primary muscle ids
                Arrays.asList(Muscle.Id.DELTS_FRONT.id,
                        Muscle.Id.TRAPS.id,
                        Muscle.Id.TRICEP_MED.id,
                        Muscle.Id.TRICEP_LAT.id,
                        Muscle.Id.TRICEP_LONG.id,
                        Muscle.Id.SERRATUS.id), // secondary muscle ids
                Arrays.asList(Arrays.asList(42, "kneeling press-up"),
                        Arrays.asList(43, "kneeling floor dip"))), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "face pull", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                Arrays.asList(MovementVariant.Id.CABLE.id), // variants
                0, // sort order
                Arrays.asList(Muscle.Id.DELTS_REAR.id), // primary muscle ids
                Arrays.asList(Muscle.Id.DELTS_SIDE.id,
                        Muscle.Id.TRAPS.id), // secondary muscle ids
                Arrays.asList(Arrays.asList(44, "rear delt row"))), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "high pull", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                Arrays.asList(MovementVariant.Id.BARBELL.id), // variants
                0, // sort order
                Arrays.asList(Muscle.Id.TRAPS.id,
                        Muscle.Id.DELTS_REAR.id), // primary muscle ids
                Arrays.asList(Muscle.Id.HAMS.id,
                        Muscle.Id.BACK_LOWER.id,
                        Muscle.Id.GLUTES.id), // secondary muscle ids
                Arrays.asList()), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "reverse pushdowns", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                Arrays.asList(MovementVariant.Id.CABLE.id), // variants
                0, // sort order
                Arrays.asList(Muscle.Id.TRICEP_LONG.id,
                        Muscle.Id.TRICEP_LAT.id,
                        Muscle.Id.TRICEP_MED.id), // primary muscle ids
                Arrays.asList(), // secondary muscle ids
                Arrays.asList()), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "rope pushdowns", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                Arrays.asList(MovementVariant.Id.CABLE.id), // variants
                0, // sort order
                Arrays.asList(Muscle.Id.TRICEP_LONG.id,
                        Muscle.Id.TRICEP_LAT.id,
                        Muscle.Id.TRICEP_MED.id), // primary muscle ids
                Arrays.asList(), // secondary muscle ids
                Arrays.asList()), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "overhead extensions", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                Arrays.asList(MovementVariant.Id.DUMBBELL.id), // variants
                0, // sort order
                Arrays.asList(Muscle.Id.TRICEP_LONG.id,
                        Muscle.Id.TRICEP_LAT.id,
                        Muscle.Id.TRICEP_MED.id), // primary muscle ids
                Arrays.asList(), // secondary muscle ids
                Arrays.asList()), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "overhead rope extensions", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                Arrays.asList(MovementVariant.Id.CABLE.id), // variants
                0, // sort order
                Arrays.asList(Muscle.Id.TRICEP_LONG.id,
                        Muscle.Id.TRICEP_LAT.id,
                        Muscle.Id.TRICEP_MED.id), // primary muscle ids
                Arrays.asList(), // secondary muscle ids
                Arrays.asList()), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "bench dips", // canonical getName
                true, // is body lift?
                new BigDecimal("0.75"), // percentage of body weight
                Arrays.asList(), // variants
                0, // sort order
                Arrays.asList(Muscle.Id.TRICEP_LONG.id,
                        Muscle.Id.TRICEP_LAT.id,
                        Muscle.Id.TRICEP_MED.id), // primary muscle ids
                Arrays.asList(), // secondary muscle ids
                Arrays.asList(Arrays.asList(45, "tricep bench dips"))), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier  (FYI, the ID of this, grippers, is: 93)
                "grippers", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                Arrays.asList(), // variants
                0, // sort order
                Arrays.asList(Muscle.Id.FOREARMS.id), // primary muscle ids
                Arrays.asList(), // secondary muscle ids
                Arrays.asList(Arrays.asList(46, "Captains of Crush Grippers"),
                        Arrays.asList(47, "hand grippers"))), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "power cleans", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                Arrays.asList(MovementVariant.Id.BARBELL.id), // variants
                0, // sort order
                Arrays.asList(Muscle.Id.HAMS.id), // primary muscle ids
                Arrays.asList(Muscle.Id.GLUTES.id, // secondary muscle ids
                        Muscle.Id.BACK_UPPER.id,
                        Muscle.Id.BACK_LOWER.id,
                        Muscle.Id.DELTS_REAR.id,
                        Muscle.Id.DELTS_SIDE.id,
                        Muscle.Id.DELTS_FRONT.id,
                        Muscle.Id.CALFS.id,
                        Muscle.Id.QUADS.id),
                Arrays.asList()), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "bent-over two-dumbbell rows", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                Arrays.asList(MovementVariant.Id.DUMBBELL.id), // variants
                0, // sort order
                Arrays.asList(Muscle.Id.BACK_LOWER.id, // primary muscle ids
                        Muscle.Id.BACK_UPPER.id),
                Arrays.asList(Muscle.Id.BICEPS.id), // secondary muscle ids
                Arrays.asList()), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "air squats", // canonical getName
                true, // is body lift?
                new BigDecimal("0.75"), // percentage of body weight
                Arrays.asList(), // variants
                0, // sort order
                Arrays.asList(Muscle.Id.QUADS.id), // primary muscle ids
                Arrays.asList(Muscle.Id.GLUTES.id, // secondary muscle ids
                        Muscle.Id.HAMS.id),
                Arrays.asList(Arrays.asList(movementAliasId++, "body weight squats"))), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "Zercher squats", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                Arrays.asList(MovementVariant.Id.BARBELL.id,  // variants
                        MovementVariant.Id.SMITH_MACHINE.id),
                0, // sort order
                Arrays.asList(Muscle.Id.QUADS.id), // primary muscle ids
                Arrays.asList(Muscle.Id.GLUTES.id, // secondary muscle ids
                        Muscle.Id.HAMS.id),
                Arrays.asList()), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "dead-stop push-up", // canonical getName
                true, // is body lift?
                new BigDecimal(PUSH_UP_BODY_WEIGHT_PERCENTAGE), // percentage of body weight
                Arrays.asList(), // variants
                0, // sort order
                Arrays.asList(Muscle.Id.CHEST_UPPER.id, // primary muscle ids
                        Muscle.Id.CHEST_LOWER.id,
                        Muscle.Id.TRICEP_MED.id,
                        Muscle.Id.TRICEP_LAT.id,
                        Muscle.Id.TRICEP_LONG.id,
                        Muscle.Id.DELTS_FRONT.id,
                        Muscle.Id.SERRATUS.id,
                        Muscle.Id.TRAPS.id),
                Arrays.asList(), // secondary muscle ids
                Arrays.asList()), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "plank push-up", // canonical getName
                true, // is body lift?
                new BigDecimal(PUSH_UP_BODY_WEIGHT_PERCENTAGE), // percentage of body weight
                Arrays.asList(), // variants
                0, // sort order
                Arrays.asList(Muscle.Id.CHEST_UPPER.id, // primary muscle ids
                        Muscle.Id.CHEST_LOWER.id,
                        Muscle.Id.TRICEP_MED.id,
                        Muscle.Id.TRICEP_LAT.id,
                        Muscle.Id.TRICEP_LONG.id,
                        Muscle.Id.DELTS_FRONT.id,
                        Muscle.Id.DELTS_SIDE.id,
                        Muscle.Id.DELTS_REAR.id,
                        Muscle.Id.ABS_LOWER.id,
                        Muscle.Id.ABS_UPPER.id),
                Arrays.asList(), // secondary muscle ids
                Arrays.asList()), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "pullovers", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                Arrays.asList(MovementVariant.Id.DUMBBELL.id,  // variants
                        MovementVariant.Id.CABLE.id),
                0, // sort order
                Arrays.asList(Muscle.Id.CHEST_LOWER.id, // primary muscle ids
                        Muscle.Id.CHEST_UPPER.id,
                        Muscle.Id.SERRATUS.id),
                Arrays.asList(), // secondary muscles
                Arrays.asList()), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "rope pulls", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                Arrays.asList(MovementVariant.Id.CABLE.id),  // variants
                0, // sort order
                Arrays.asList(Muscle.Id.SERRATUS.id), // primary muscle ids
                Arrays.asList(), // secondary muscles
                Arrays.asList()), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "hanging serratus crunches", // canonical getName
                true, // is body lift?
                new BigDecimal("0.50"), // percentage of body weight
                Arrays.asList(),  // variants
                0, // sort order
                Arrays.asList(Muscle.Id.SERRATUS.id), // primary muscle ids
                Arrays.asList(), // secondary muscles
                Arrays.asList()), // aliases
                database);
        /**
         Need to make sure that the order of these insMovementImp statements is the
         same order as they appear in ref_data.clj on the server.
         */
        movementAliasId = 51; // fyi, 'body weight squats' is ID 50
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "landmine press", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                Arrays.asList(MovementVariant.Id.BARBELL.id),  // variants
                0, // sort order
                Arrays.asList(Muscle.Id.CHEST_UPPER.id,  // primary muscle ids
                        Muscle.Id.CHEST_LOWER.id),
                Arrays.asList(Muscle.Id.TRICEP_MED.id, // secondary muscles
                        Muscle.Id.TRICEP_LONG.id,
                        Muscle.Id.TRICEP_LAT.id,
                        Muscle.Id.SERRATUS.id,
                        Muscle.Id.DELTS_FRONT.id),
                Arrays.asList()), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "landmine squats", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                Arrays.asList(MovementVariant.Id.BARBELL.id),  // variants
                0, // sort order
                Arrays.asList(Muscle.Id.QUADS.id),  // primary muscle ids
                Arrays.asList(Muscle.Id.HAMS.id, // secondary muscles
                        Muscle.Id.GLUTES.id),
                Arrays.asList()), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "barbell thruster", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                Arrays.asList(MovementVariant.Id.BARBELL.id),  // variants
                0, // sort order
                Arrays.asList(Muscle.Id.QUADS.id,  // primary muscle ids
                        Muscle.Id.HAMS.id,
                        Muscle.Id.GLUTES.id,
                        Muscle.Id.DELTS_FRONT.id,
                        Muscle.Id.DELTS_SIDE.id,
                        Muscle.Id.BACK_UPPER.id),
                Arrays.asList(Muscle.Id.TRICEP_MED.id, // secondary muscles
                        Muscle.Id.TRICEP_LONG.id,
                        Muscle.Id.TRICEP_LAT.id,
                        Muscle.Id.TRAPS.id),
                Arrays.asList()), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "landmine thruster", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                Arrays.asList(MovementVariant.Id.BARBELL.id),  // variants
                0, // sort order
                Arrays.asList(Muscle.Id.QUADS.id,  // primary muscle ids
                        Muscle.Id.HAMS.id,
                        Muscle.Id.GLUTES.id,
                        Muscle.Id.DELTS_FRONT.id,
                        Muscle.Id.DELTS_SIDE.id,
                        Muscle.Id.BACK_UPPER.id),
                Arrays.asList(Muscle.Id.TRICEP_MED.id, // secondary muscles
                        Muscle.Id.TRICEP_LONG.id,
                        Muscle.Id.TRICEP_LAT.id,
                        Muscle.Id.TRAPS.id),
                Arrays.asList()), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "rotational single-arm press", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                Arrays.asList(MovementVariant.Id.BARBELL.id),  // variants
                0, // sort order
                Arrays.asList(Muscle.Id.GLUTES.id, // primary muscle ids
                        Muscle.Id.DELTS_FRONT.id,
                        Muscle.Id.DELTS_SIDE.id,
                        Muscle.Id.DELTS_REAR.id),
                Arrays.asList(), // secondary muscles
                Arrays.asList(Arrays.asList(movementAliasId++, "landmine rotational single-arm press"))), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "landmine anti-rotations", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                Arrays.asList(MovementVariant.Id.BARBELL.id),  // variants
                0, // sort order
                Arrays.asList(Muscle.Id.ABS_LOWER.id, // primary muscle ids
                        Muscle.Id.ABS_UPPER.id),
                Arrays.asList(), // secondary muscle ids
                Arrays.asList(Arrays.asList(movementAliasId++, "landmine 180s"), // aliases
                        Arrays.asList(movementAliasId++, "landmine twists"),
                        Arrays.asList(movementAliasId++, "landmine rotations"))),
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "single-arm landmine row", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                Arrays.asList(MovementVariant.Id.BARBELL.id),  // variants
                0, // sort order
                Arrays.asList(Muscle.Id.BACK_LOWER.id, // primary muscle ids
                        Muscle.Id.BACK_UPPER.id),
                Arrays.asList(Muscle.Id.BICEPS.id), // secondary muscle ids
                Arrays.asList(Arrays.asList(movementAliasId++, "Meadows row"))), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "typewriter push-up", // canonical getName
                true, // is body lift?
                new BigDecimal("0.64"), // percentage of body weight
                Arrays.asList(), // variants
                0, // sort order
                Arrays.asList(Muscle.Id.CHEST_UPPER.id,
                        Muscle.Id.CHEST_LOWER.id), // primary muscle ids
                Arrays.asList(Muscle.Id.DELTS_FRONT.id,
                        Muscle.Id.TRAPS.id,
                        Muscle.Id.TRICEP_MED.id,
                        Muscle.Id.TRICEP_LAT.id,
                        Muscle.Id.TRICEP_LONG.id,
                        Muscle.Id.SERRATUS.id), // secondary muscle ids
                Arrays.asList()), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "high rows", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                Arrays.asList(MovementVariant.Id.MACHINE.id),  // variants
                0, // sort order
                Arrays.asList(Muscle.Id.BACK_LOWER.id, // primary muscle ids
                        Muscle.Id.BACK_UPPER.id),
                Arrays.asList(Muscle.Id.BICEPS.id), // secondary muscle ids
                Arrays.asList()), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "rear delt machine", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                Arrays.asList(MovementVariant.Id.MACHINE.id),  // variants
                0, // sort order
                Arrays.asList(Muscle.Id.DELTS_REAR.id), // primary muscle ids
                Arrays.asList(), // secondary muscle ids
                Arrays.asList()), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "calf press", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                Arrays.asList(MovementVariant.Id.MACHINE.id,   // variants
                        MovementVariant.Id.SLED.id),
                0, // sort order
                Arrays.asList(Muscle.Id.CALFS.id), // primary muscle ids
                Arrays.asList(), // secondary muscle ids
                Arrays.asList()), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "burpee", // canonical getName
                true, // is body lift?
                new BigDecimal("0.85"), // percentage of body weight
                Arrays.asList(), // variants
                0, // sort order
                Arrays.asList(Muscle.Id.QUADS.id), // primary muscle ids
                Arrays.asList(Muscle.Id.GLUTES.id,  // secondary muscle ids
                        Muscle.Id.HAMS.id,
                        Muscle.Id.CALFS.id),
                Arrays.asList()), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "close-grip push-up", // canonical getName
                true, // is body lift?
                new BigDecimal("0.64"), // percentage of body weight
                Arrays.asList(), // variants
                0, // sort order
                Arrays.asList(Muscle.Id.TRICEP_MED.id, // primary muscle ids
                        Muscle.Id.TRICEP_LAT.id,
                        Muscle.Id.TRICEP_LONG.id),
                Arrays.asList(Muscle.Id.CHEST_UPPER.id,
                        Muscle.Id.CHEST_LOWER.id), // secondary muscle ids
                Arrays.asList()), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "hang power clean", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                Arrays.asList(MovementVariant.Id.BARBELL.id), // variants
                0, // sort order
                Arrays.asList(Muscle.Id.QUADS.id), // primary muscle ids
                Arrays.asList(Muscle.Id.BACK_LOWER.id,
                        Muscle.Id.DELTS_FRONT.id,
                        Muscle.Id.DELTS_REAR.id,
                        Muscle.Id.DELTS_SIDE.id,
                        Muscle.Id.CALFS.id,
                        Muscle.Id.GLUTES.id,
                        Muscle.Id.HAMS.id,
                        Muscle.Id.TRAPS.id,
                        Muscle.Id.BACK_UPPER.id), // secondary muscle ids
                Arrays.asList()), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "JM press", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                Arrays.asList(MovementVariant.Id.BARBELL.id, // variants
                        MovementVariant.Id.CURL_BAR.id,
                        MovementVariant.Id.SMITH_MACHINE.id),
                0, // sort order
                Arrays.asList(Muscle.Id.TRICEP_LONG.id,
                        Muscle.Id.TRICEP_LAT.id,
                        Muscle.Id.TRICEP_MED.id), // primary muscle ids
                Arrays.asList(), // secondary muscle ids
                Arrays.asList()), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "pike press", // canonical getName
                true, // is body lift?
                new BigDecimal("0.64"), // percentage of body weight
                Arrays.asList(), // variants
                0, // sort order
                Arrays.asList(Muscle.Id.DELTS_FRONT.id), // primary muscle ids
                Arrays.asList(Muscle.Id.CHEST_UPPER.id, // secondary muscle ids
                        Muscle.Id.TRICEP_LONG.id,
                        Muscle.Id.TRICEP_LAT.id,
                        Muscle.Id.TRICEP_MED.id),
                Arrays.asList()), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "pike push-up", // canonical getName
                true, // is body lift?
                new BigDecimal("0.64"), // percentage of body weight
                Arrays.asList(), // variants
                0, // sort order
                Arrays.asList(Muscle.Id.CHEST_UPPER.id), // primary muscle ids
                Arrays.asList(Muscle.Id.DELTS_FRONT.id, // secondary muscle ids
                        Muscle.Id.TRICEP_LONG.id,
                        Muscle.Id.TRICEP_LAT.id,
                        Muscle.Id.TRICEP_MED.id),
                Arrays.asList()), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "rear lunges", // canonical getName
                false, // is body lift?
                new BigDecimal("0.75"), // percentage of body weight
                Arrays.asList(MovementVariant.Id.BODY.id,
                        MovementVariant.Id.DUMBBELL.id,
                        MovementVariant.Id.BARBELL.id), // variants
                0, // sort order
                Arrays.asList(Muscle.Id.GLUTES.id,
                        Muscle.Id.QUADS.id), // primary muscle ids
                Arrays.asList(Muscle.Id.HAMS.id), // secondary muscle ids
                Arrays.asList()), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "seated calf raises", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                Arrays.asList(MovementVariant.Id.MACHINE.id), // variants
                0, // sort order
                Arrays.asList(Muscle.Id.CALFS.id), // primary muscle ids
                Arrays.asList(), // secondary muscle ids
                Arrays.asList()), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "side lunges", // canonical getName
                false, // is body lift?
                new BigDecimal("0.75"), // percentage of body weight
                Arrays.asList(MovementVariant.Id.BODY.id,  // variants
                        MovementVariant.Id.DUMBBELL.id),
                0, // sort order
                Arrays.asList(Muscle.Id.GLUTES.id,
                        Muscle.Id.QUADS.id), // primary muscle ids
                Arrays.asList(Muscle.Id.HAMS.id), // secondary muscle ids
                Arrays.asList()), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "step-up", // canonical getName
                false, // is body lift?
                new BigDecimal("0.75"), // percentage of body weight
                Arrays.asList(MovementVariant.Id.BODY.id,
                        MovementVariant.Id.DUMBBELL.id,
                        MovementVariant.Id.BARBELL.id), // variants
                0, // sort order
                Arrays.asList(Muscle.Id.GLUTES.id,
                        Muscle.Id.QUADS.id), // primary muscle ids
                Arrays.asList(Muscle.Id.HAMS.id), // secondary muscle ids
                Arrays.asList()), // aliases
                database);
        RikerDao.insertMasterMovement(newMovementWithAliasesImp(
                movementId++, // local master identifier
                "dumbbell rotational punch", // canonical getName
                false, // is body lift?
                null, // percentage of body weight
                Arrays.asList(MovementVariant.Id.DUMBBELL.id), // variants
                0, // sort order
                Arrays.asList(Muscle.Id.DELTS_FRONT.id, // primary muscle ids
                        Muscle.Id.DELTS_SIDE.id,
                        Muscle.Id.DELTS_REAR.id,
                        Muscle.Id.SERRATUS.id),
                Arrays.asList(), // secondary muscle ids
                Arrays.asList()), // aliases
                database);
        movementAliasId = 57; // 56 == current max alias id ('chest press')
        RikerDao.insertMasterMovementAlias(37, movementAliasId++, "bicep curls", database); // 37 == 'curls' ID
        RikerDao.insertMasterMovementAlias(62, movementAliasId++, "squat clean", database); // 62 == 'clean' ID
        RikerDao.insertMasterMovementAlias(70, movementAliasId++, "RDL", database); // 70 == 'Romanian deadlift' ID
        RikerDao.insertMasterMovementAlias(24, movementAliasId++, "rear lateral raises", database); // 24 == 'bent-over laterals' ID
    }

    /*==============================================================================================
       Movement Variants
     =============================================================================================*/
    private static MovementVariant newMovementVariant(final int id, final String name, final String abbrevName, final String description, final int sortOrder) {
        final MovementVariant movementVariant = new MovementVariant();
        movementVariant.localIdentifier = id;
        movementVariant.name = name;
        movementVariant.abbrevName = abbrevName;
        movementVariant.variantDescription = description;
        movementVariant.sortOrder = sortOrder;
        movementVariant.globalIdentifier = Utils.globalIdentifier(UriPathPart.MOVEMENT_VARIANTS, id);
        setDefaultCreatedAtUpdatedAtDates(movementVariant);
        return movementVariant;
    }

    private static void v1_dataLoading_movementVariants(final SQLiteDatabase database) {
        RikerDao.insertMasterMovementVariant(newMovementVariant(MovementVariant.Id.BARBELL.id,       "barbell", null, null, 0), database);
        RikerDao.insertMasterMovementVariant(newMovementVariant(MovementVariant.Id.DUMBBELL.id,      "dumbbell", null, null, 1), database);
        RikerDao.insertMasterMovementVariant(newMovementVariant(MovementVariant.Id.MACHINE.id,       "machine", null, null, 2), database);
        RikerDao.insertMasterMovementVariant(newMovementVariant(MovementVariant.Id.SMITH_MACHINE.id, "smith machine", "smith m.", null, 3), database);
        RikerDao.insertMasterMovementVariant(newMovementVariant(MovementVariant.Id.CABLE.id,         "cable", null, null, 4), database);
        RikerDao.insertMasterMovementVariant(newMovementVariant(MovementVariant.Id.CURL_BAR.id,      "curl bar", null, null, 5), database);
        RikerDao.insertMasterMovementVariant(newMovementVariant(MovementVariant.Id.SLED.id,          "sled", null, null, 6), database);
        RikerDao.insertMasterMovementVariant(newMovementVariant(MovementVariant.Id.BODY.id,          "body", null, null, 7), database);
        RikerDao.insertMasterMovementVariant(newMovementVariant(MovementVariant.Id.KETTLEBELL.id,    "kettlebell", null, null, 8), database);
    }

    /*==============================================================================================
       Origination Devices
     =============================================================================================*/
    private static OriginationDevice newOriginationDevice(final int id, final String name, final String iconImageName) {
        final OriginationDevice originationDevice = new OriginationDevice();
        originationDevice.localIdentifier = id;
        originationDevice.name = name;
        originationDevice.iconImageName = iconImageName;
        originationDevice.hasLocalImage = true;
        originationDevice.globalIdentifier = Utils.globalIdentifier(UriPathPart.ORIGINATION_DEVICES, id);
        setDefaultCreatedAtUpdatedAtDates(originationDevice);
        return originationDevice;
    }

    private static void v1_dataLoading_originationDevices(final SQLiteDatabase database) {
        RikerDao.insertMasterOriginationDevice(newOriginationDevice(OriginationDevice.Id.WEB.id,          "Web",          "orig_device_web"),          database);
        //RikerDao.insertMasterOriginationDevice(newOriginationDevice(OriginationDevice.Id.PEBBLE.id,       "Pebble",       "orig_device_pebble"),       database);
        RikerDao.insertMasterOriginationDevice(newOriginationDevice(OriginationDevice.Id.IPHONE.id,       "iPhone",       "orig_device_iphone"),       database);
        RikerDao.insertMasterOriginationDevice(newOriginationDevice(OriginationDevice.Id.IPAD.id,         "iPad",         "orig_device_ipad"),         database);
        RikerDao.insertMasterOriginationDevice(newOriginationDevice(OriginationDevice.Id.APPLE_WATCH.id,  "Apple Watch",  "orig_device_apple_watch"),  database);
        RikerDao.insertMasterOriginationDevice(newOriginationDevice(OriginationDevice.Id.ANDROID_WEAR.id, "Android Wear", "orig_device_android_wear"), database);
        RikerDao.insertMasterOriginationDevice(newOriginationDevice(OriginationDevice.Id.ANDROID.id,      "Android",      "orig_device_android"),      database);
    }

    /*==============================================================================================
       Muscle Aliases
     =============================================================================================*/
    private static MuscleAlias newMuscleAlias(final MuscleAlias.Id muscleAliasId,
                                              final Muscle.Id muscleId,
                                              final String alias) {
        final MuscleAlias muscleAlias = new MuscleAlias();
        muscleAlias.localIdentifier = muscleAliasId.id;
        muscleAlias.muscleId = muscleId.id;
        muscleAlias.alias = alias;
        muscleAlias.globalIdentifier = Utils.globalIdentifier(UriPathPart.MUSCLE_ALIASES, muscleAliasId.id);
        setDefaultCreatedAtUpdatedAtDates(muscleAlias);
        return muscleAlias;
    }

    private static void v1_dataLoading_muscleAliases(final SQLiteDatabase database) {
        RikerDao.insertMasterMuscleAlias(newMuscleAlias(MuscleAlias.Id.REAR_DELTS, Muscle.Id.DELTS_REAR, "rear delts"), database);
        RikerDao.insertMasterMuscleAlias(newMuscleAlias(MuscleAlias.Id.POSTERIOR_DELTS, Muscle.Id.DELTS_REAR, "posterior delts"), database);
        RikerDao.insertMasterMuscleAlias(newMuscleAlias(MuscleAlias.Id.FRONT_DELTS, Muscle.Id.DELTS_FRONT, "front delts"), database);
        RikerDao.insertMasterMuscleAlias(newMuscleAlias(MuscleAlias.Id.ANTERIOR_DELTS, Muscle.Id.DELTS_FRONT, "anterior delts"), database);
        RikerDao.insertMasterMuscleAlias(newMuscleAlias(MuscleAlias.Id.SIDE_DELTS, Muscle.Id.DELTS_SIDE, "side delts"), database);
        RikerDao.insertMasterMuscleAlias(newMuscleAlias(MuscleAlias.Id.MIDDLE_DELTS, Muscle.Id.DELTS_SIDE, "middle delts"), database);
        RikerDao.insertMasterMuscleAlias(newMuscleAlias(MuscleAlias.Id.OUTER_DELTS, Muscle.Id.DELTS_SIDE, "outer delts"), database);
        RikerDao.insertMasterMuscleAlias(newMuscleAlias(MuscleAlias.Id.LATERAL_DELTOIDS, Muscle.Id.DELTS_SIDE, "lateral delts"), database);
        RikerDao.insertMasterMuscleAlias(newMuscleAlias(MuscleAlias.Id.UPPER_LATS, Muscle.Id.BACK_UPPER, "upper lats"), database);
        RikerDao.insertMasterMuscleAlias(newMuscleAlias(MuscleAlias.Id.LOWER_LATS, Muscle.Id.BACK_LOWER, "lower lats"), database);
        RikerDao.insertMasterMuscleAlias(newMuscleAlias(MuscleAlias.Id.UPPER_PECS, Muscle.Id.CHEST_UPPER, "upper pecs"), database);
        RikerDao.insertMasterMuscleAlias(newMuscleAlias(MuscleAlias.Id.LOWER_PECS, Muscle.Id.CHEST_LOWER, "lower pecs"), database);
        RikerDao.insertMasterMuscleAlias(newMuscleAlias(MuscleAlias.Id.UPPER_ABDOMINALS, Muscle.Id.ABS_UPPER, "upper abdominals"), database);
        RikerDao.insertMasterMuscleAlias(newMuscleAlias(MuscleAlias.Id.LOWER_ABDOMINALS, Muscle.Id.ABS_LOWER, "lower abdominals"), database);
        RikerDao.insertMasterMuscleAlias(newMuscleAlias(MuscleAlias.Id.QUADS, Muscle.Id.QUADS, "quads"), database);
        RikerDao.insertMasterMuscleAlias(newMuscleAlias(MuscleAlias.Id.HAMS, Muscle.Id.HAMS, "hams"), database);
        RikerDao.insertMasterMuscleAlias(newMuscleAlias(MuscleAlias.Id.TRAPEZIUS, Muscle.Id.TRAPS, "trapezius"), database);
        RikerDao.insertMasterMuscleAlias(newMuscleAlias(MuscleAlias.Id.BUTT, Muscle.Id.GLUTES, "butt"), database);
        RikerDao.insertMasterMuscleAlias(newMuscleAlias(MuscleAlias.Id.BUTTOCKS, Muscle.Id.GLUTES, "buttocks"), database);
        RikerDao.insertMasterMuscleAlias(newMuscleAlias(MuscleAlias.Id.GLUTEUS_MAXIMUS, Muscle.Id.GLUTES, "gluteus maximus"), database);
    }

    /*==============================================================================================
       Muscles
     =============================================================================================*/
    private static Muscle newMuscle(final Muscle.Id muscleId,
                                    final MuscleGroup.Id muscleGroupId,
                                    final String canonicalName,
                                    final String abbrevCanonicalName) {
        final Muscle muscle = new Muscle();
        muscle.localIdentifier = muscleId.id;
        muscle.muscleGroupId = muscleGroupId.id;
        muscle.canonicalName= canonicalName;
        muscle.abbrevCanonicalName= abbrevCanonicalName;
        muscle.globalIdentifier = Utils.globalIdentifier(UriPathPart.MUSCLES, muscleId.id);
        setDefaultCreatedAtUpdatedAtDates(muscle);
        return muscle;
    }

    private static void v1_dataLoading_muscles(final SQLiteDatabase database) {
        RikerDao.insertMasterMuscle(newMuscle(Muscle.Id.DELTS_REAR, MuscleGroup.Id.SHOULDERS, "rear deltoids", "rear delts"), database);
        RikerDao.insertMasterMuscle(newMuscle(Muscle.Id.DELTS_FRONT, MuscleGroup.Id.SHOULDERS, "front deltoids", "front delts"), database);
        RikerDao.insertMasterMuscle(newMuscle(Muscle.Id.DELTS_SIDE, MuscleGroup.Id.SHOULDERS, "side deltoids", "side delts"), database);
        RikerDao.insertMasterMuscle(newMuscle(Muscle.Id.BACK_UPPER, MuscleGroup.Id.BACK, "upper back", null), database);
        RikerDao.insertMasterMuscle(newMuscle(Muscle.Id.BACK_LOWER, MuscleGroup.Id.BACK, "lower back", null), database);
        RikerDao.insertMasterMuscle(newMuscle(Muscle.Id.CHEST_UPPER, MuscleGroup.Id.CHEST, "upper chest", null), database);
        RikerDao.insertMasterMuscle(newMuscle(Muscle.Id.CHEST_LOWER, MuscleGroup.Id.CHEST, "lower chest", null), database);
        RikerDao.insertMasterMuscle(newMuscle(Muscle.Id.ABS_UPPER, MuscleGroup.Id.CORE, "upper abs", null), database);
        RikerDao.insertMasterMuscle(newMuscle(Muscle.Id.ABS_LOWER, MuscleGroup.Id.CORE, "lower abs", null), database);
        RikerDao.insertMasterMuscle(newMuscle(Muscle.Id.SERRATUS, MuscleGroup.Id.CHEST, "serratus", null), database);
        RikerDao.insertMasterMuscle(newMuscle(Muscle.Id.QUADS, MuscleGroup.Id.QUADS, "quadriceps", null), database);
        RikerDao.insertMasterMuscle(newMuscle(Muscle.Id.HAMS, MuscleGroup.Id.HAMS, "hamstrings", null), database);
        RikerDao.insertMasterMuscle(newMuscle(Muscle.Id.CALFS, MuscleGroup.Id.CALF, "calfs", null), database);
        RikerDao.insertMasterMuscle(newMuscle(Muscle.Id.BICEPS, MuscleGroup.Id.BICEPS, "biceps", null), database);
        RikerDao.insertMasterMuscle(newMuscle(Muscle.Id.FOREARMS, MuscleGroup.Id.FOREARMS, "forearms", null), database);
        RikerDao.insertMasterMuscle(newMuscle(Muscle.Id.TRAPS, MuscleGroup.Id.SHOULDERS, "traps", null), database);
        RikerDao.insertMasterMuscle(newMuscle(Muscle.Id.GLUTES, MuscleGroup.Id.GLUTES, "glutes", null), database);
        RikerDao.insertMasterMuscle(newMuscle(Muscle.Id.TRICEP_LAT, MuscleGroup.Id.TRICEPS, "lateral head", "lat. head"), database);
        RikerDao.insertMasterMuscle(newMuscle(Muscle.Id.TRICEP_LONG, MuscleGroup.Id.TRICEPS, "long head", "long head"), database);
        RikerDao.insertMasterMuscle(newMuscle(Muscle.Id.TRICEP_MED, MuscleGroup.Id.TRICEPS, "medial head", "med. head"), database);
    }

    /*==============================================================================================
       Muscle Groups
     =============================================================================================*/
    private static MuscleGroup newMuscleGroup(final MuscleGroup.Id muscleGroupId,
                                              final BodySegment.Id bodySegmentId,
                                              final String name,
                                              final String abbrevName) {
        final MuscleGroup muscleGroup = new MuscleGroup();
        muscleGroup.localIdentifier = muscleGroupId.id;
        muscleGroup.bodySegmentId = bodySegmentId.id;
        muscleGroup.name = name;
        muscleGroup.abbrevName = abbrevName;
        muscleGroup.globalIdentifier = Utils.globalIdentifier(UriPathPart.MUSCLE_GROUPS, muscleGroupId.id);
        setDefaultCreatedAtUpdatedAtDates(muscleGroup);
        return muscleGroup;
    }

    private static void v1_dataLoading_muscleGroups(final SQLiteDatabase database) {
        RikerDao.insertMasterMuscleGroup(newMuscleGroup(MuscleGroup.Id.SHOULDERS, BodySegment.Id.UPPER_BODY, "shoulders", "delts"), database);
        RikerDao.insertMasterMuscleGroup(newMuscleGroup(MuscleGroup.Id.BACK, BodySegment.Id.UPPER_BODY, "back", null), database);
        RikerDao.insertMasterMuscleGroup(newMuscleGroup(MuscleGroup.Id.CHEST, BodySegment.Id.UPPER_BODY, "chest", null), database);
        RikerDao.insertMasterMuscleGroup(newMuscleGroup(MuscleGroup.Id.CORE, BodySegment.Id.UPPER_BODY, "abs", null), database);
        RikerDao.insertMasterMuscleGroup(newMuscleGroup(MuscleGroup.Id.QUADS, BodySegment.Id.LOWER_BODY, "quadriceps", "quads"), database);
        RikerDao.insertMasterMuscleGroup(newMuscleGroup(MuscleGroup.Id.HAMS, BodySegment.Id.LOWER_BODY, "hamstrings", "hams"), database);
        RikerDao.insertMasterMuscleGroup(newMuscleGroup(MuscleGroup.Id.CALF, BodySegment.Id.LOWER_BODY, "calfs", null), database);
        RikerDao.insertMasterMuscleGroup(newMuscleGroup(MuscleGroup.Id.TRICEPS, BodySegment.Id.UPPER_BODY, "triceps", null), database);
        RikerDao.insertMasterMuscleGroup(newMuscleGroup(MuscleGroup.Id.BICEPS, BodySegment.Id.UPPER_BODY, "biceps", null), database);
        RikerDao.insertMasterMuscleGroup(newMuscleGroup(MuscleGroup.Id.FOREARMS, BodySegment.Id.UPPER_BODY, "forearms", null), database);
        RikerDao.insertMasterMuscleGroup(newMuscleGroup(MuscleGroup.Id.GLUTES, BodySegment.Id.LOWER_BODY, "glutes", null), database);
    }

    /*==============================================================================================
       Body Segments
     =============================================================================================*/
    private static BodySegment newBodySegment(final BodySegment.Id bodySegmentId, final String name) {
        final BodySegment bodySegment = new BodySegment();
        bodySegment.localIdentifier = bodySegmentId.id;
        bodySegment.name = name;
        bodySegment.globalIdentifier = Utils.globalIdentifier(UriPathPart.BODY_SEGMENTS, bodySegmentId.id);
        setDefaultCreatedAtUpdatedAtDates(bodySegment);
        return bodySegment;
    }

    private static void v1_dataLoading_bodySegments(final SQLiteDatabase database) {
        RikerDao.insertMasterBodySegment(newBodySegment(BodySegment.Id.UPPER_BODY, "upper body"), database);
        RikerDao.insertMasterBodySegment(newBodySegment(BodySegment.Id.LOWER_BODY, "lower body"), database);
    }
}
