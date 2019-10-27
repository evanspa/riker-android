package com.rikerapp.riker.model;

import com.rikerapp.riker.Utils;

public enum KnownRikerMediaType {
    USER                (Utils.rikerMediaType("user", false)),
    USER_SETTINGS       (Utils.rikerMediaType("usersettings", false)),
    MOVEMENT            (Utils.rikerMediaType("movement", false)),
    MOVEMENT_VARIANT    (Utils.rikerMediaType("movementvariant", false)),
    MOVEMENT_ALIAS      (Utils.rikerMediaType("movementalias", false)),
    BODY_SEGMENT        (Utils.rikerMediaType("bodysegment", false)),
    MUSCLE_GROUP        (Utils.rikerMediaType("musclegroup", false)),
    MUSCLE              (Utils.rikerMediaType("muscle", false)),
    MUSCLE_ALIAS        (Utils.rikerMediaType("musclealias", false)),
    ORIGINATION_DEVICES (Utils.rikerMediaType("originationdevices", false)),
    SET                 (Utils.rikerMediaType("set", false)),
    BML                 (Utils.rikerMediaType("bodyjournallog", false))
    ;
    public final String mediaType;

    KnownRikerMediaType(final String mediaType) {
        this.mediaType = mediaType;
    }

    public static KnownRikerMediaType lookup(final String mediaType) {
        return lookup(mediaType, KnownRikerMediaType.values());
    }

    public static KnownRikerMediaType lookup(final String mediaType, final KnownRikerMediaType knownRikerMediaTypes[]) {
        for (final KnownRikerMediaType knownRikerMediaType : knownRikerMediaTypes) {
            if (knownRikerMediaType.mediaType.equals(mediaType)) {
                return knownRikerMediaType;
            }
        }
        return null;
    }
}
