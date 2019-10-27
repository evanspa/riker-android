package com.rikerapp.riker.model;

import org.parceler.Parcel;

import java.util.List;
import java.util.Map;

@Parcel
public final class ChartRawDataContainer {

    public User user;
    public UserSettings userSettings;
    public List<BodySegment> bodySegmentList;
    public Map<Integer, BodySegment> bodySegmentMap;
    public List<MuscleGroup> muscleGroupList;
    public Map<Integer, MuscleGroup> muscleGroupMap;
    public List<Muscle> muscleList;
    public Map<Integer, Muscle> muscleMap;
    public List<Movement> movementList;
    public Map<Integer, Movement> movementMap;
    public List<MovementVariant> movementVariantList;
    public Map<Integer, MovementVariant> movementVariantMap;
    public List<MainSupport> entities;
    public ChartRawData chartRawData;

    @Override
    public final String toString() {
        return String.format("chartRawData: %s", chartRawData);
    }
}
