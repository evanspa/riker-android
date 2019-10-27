package com.rikerapp.riker.model;

public enum UriPathPart {

    ORIGINATION_DEVICES("originationdevices"),
    BODY_SEGMENTS("bodysegments"),
    MUSCLE_GROUPS("musclegroups"),
    MUSCLES("muscles"),
    MUSCLE_ALIASES("musclealiases"),
    MOVEMENT_VARIANTS("movementvariants"),
    MOVEMENTS("movementsWithNullMuscleIds"),
    MOVEMENT_ALIASES("movementaliases"),
    ;

    public final String pathPart;

    UriPathPart(final String pathPart) {
        this.pathPart = pathPart;
    }
}
