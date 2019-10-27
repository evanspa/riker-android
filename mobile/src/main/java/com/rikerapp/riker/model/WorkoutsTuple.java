package com.rikerapp.riker.model;

import java.util.Date;
import java.util.List;

public final class WorkoutsTuple {
    public final List<Workout> workouts;
    public final int numSets;
    public final Date latestSetLoggedAt;

    public WorkoutsTuple(final List<Workout> workouts, final Date latestSetLoggedAt, final int numSets) {
        this.workouts = workouts;
        this.latestSetLoggedAt = latestSetLoggedAt;
        this.numSets = numSets;
    }
}
