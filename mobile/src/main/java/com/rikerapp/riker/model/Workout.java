package com.rikerapp.riker.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public final class Workout {

    public boolean vigorous;
    public BigDecimal caloriesBurned;
    public Date startDate;
    public Date endDate;
    public long workoutDurationInSeconds;
    public List sets;
    public List<MuscleGroupTuple> impactedMuscleGroupTuples;

    @Override
    public final String toString() {
        return String.format("vigorous: %s, calories burned: %s, start date: %s, end date: %s, workout duration (in min): %s",
                Boolean.toString(vigorous),
                caloriesBurned,
                startDate,
                endDate,
                Float.toString((workoutDurationInSeconds / 60.0f)));
    }
}
