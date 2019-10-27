package com.rikerapp.riker;

import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.rikerapp.riker.model.BodySegment;
import com.rikerapp.riker.model.ChartColorsContainer;
import com.rikerapp.riker.model.ChartConfig;
import com.rikerapp.riker.model.ChartRawData;
import com.rikerapp.riker.model.HttpResponseTuple;
import com.rikerapp.riker.model.MasterSupport;
import com.rikerapp.riker.model.Movement;
import com.rikerapp.riker.model.MovementVariant;
import com.rikerapp.riker.model.Muscle;
import com.rikerapp.riker.model.MuscleGroup;
import com.rikerapp.riker.model.NormalizedLineChartDataEntry;
import com.rikerapp.riker.model.NormalizedTimeSeriesTupleCollection;
import com.rikerapp.riker.model.PieSliceDataTuple;
import com.rikerapp.riker.model.RikerDao;
import com.rikerapp.riker.model.RikerErrorType;
import com.rikerapp.riker.model.Set;
import com.rikerapp.riker.model.User;
import com.rikerapp.riker.model.UserSettings;
import com.rikerapp.riker.model.YAxisLabelText;
import com.rikerapp.riker.sql.tables.RikerTable;

import java.io.BufferedReader;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Function {

    /*==============================================================================================
    ************************************************************************************************
    ================================================================================================
       Function Interfaces
    ================================================================================================
    ************************************************************************************************
    ==============================================================================================*/
    public interface RsObjConverter      { Object invoke(Cursor cursor); }
    public interface TextTransformer     { String invoke(String text); }
    public interface TextArgsTransformer { String[] invoke(String textArgs[]); }
    public interface EntitiesFilter      { List invoke(List entities); }
    public interface EntityDbOp          { void invoke(MasterSupport entity, SQLiteDatabase database); }
    public interface VoidFunction extends Serializable { void invoke(); }
    public interface ToString            { String invoke(final Object object); }
    public interface FileOnTouch         { void invoke(final String fileName); }
    public interface IntToString         { String invoke(final int value); }
    public interface SetOnTouch          { void invoke(final Set set); }
    public interface EntitiesImportHandler { void invoke(final Uri fileUri); }
    public interface ChartRawDataMaker { ChartRawData invoke(final UserSettings userSettings,
                                                             final List<BodySegment> bodySegments,
                                                             final Map<Integer, BodySegment> bodySegmentsDict,
                                                             final List<MuscleGroup> muscleGroups,
                                                             final Map<Integer, MuscleGroup> muscleGroupsDict,
                                                             final List<Muscle> muscles,
                                                             final Map<Integer, Muscle> musclesDict,
                                                             final Map<Integer, Movement> movementsDict,
                                                             final List<MovementVariant> movementVariants,
                                                             final Map<Integer, MovementVariant> movementVariantsDict,
                                                             final List entities,
                                                             final boolean calcPercentages,
                                                             final boolean calcAverages); }
    public interface NormalizedTimeSeriesCollectionMaker { NormalizedTimeSeriesTupleCollection invoke(final ChartRawData chartStrengthRawData,
                                                                                                      final ChartConfig.AggregateBy aggregateBy); }
    public interface MaxValueMaker { BigDecimal invoke(final NormalizedTimeSeriesTupleCollection normalizedTimeSeriesTupleCollection); }
    public interface YValueMaker { BigDecimal invoke(final NormalizedLineChartDataEntry normalizedLineChartDataEntry); }
    public interface YAxisValueFormatterMaker { ValueFormatter invoke(final BigDecimal maxValue); }
    public interface YAxisValueLabelMaker { YAxisLabelText.LabelPair invoke(final UserSettings userSettings, final BigDecimal maxValue); }
    public interface LineWidthFn { float invoke(final Resources resources); }
    public interface ChartColorsFn { Map<Integer, Integer> invoke(final ChartColorsContainer chartColorsContainer); }
    public interface AscendingEntitiesFn { List invoke(final RikerDao dao, final User user, final Date since); }
    public interface BoundedAscendingEntitiesFn { List invoke(final RikerDao dao, final User user, final Date since, final Date end); }
    public interface YAxisMaximumFn { BigDecimal invoke(final BigDecimal maxyValue); }
    public interface PieSliceMaker { HashMap<Integer, PieSliceDataTuple> invoke(final ChartRawData chartRawData); }

    public interface HttpUnprocessableEntityHandler { void invoke(final List<String> errors); }
    public interface ErrorTuplesFn { List<RikerErrorType.ErrorTuple> invoke(); }
}
