package com.rikerapp.riker.model;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.rikerapp.riker.Utils;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.List;

@Parcel
public final class Muscle extends MasterSupport {

    public static final String JSON_KEY_PREFIX = "muscle";

    private static String key(final String key) {
        return String.format("%s/%s", JSON_KEY_PREFIX, key);
    }

    public static Muscle toMuscle(final JsonObject jsonObject, final String globalIdentifier) {
        final Muscle muscle = new Muscle();
        muscle.populateCommon(globalIdentifier, jsonObject, JSON_KEY_PREFIX);
        muscle.canonicalName = Utils.getString(jsonObject, key("canonical-name"));
        muscle.abbrevCanonicalName = Utils.getString(jsonObject, key("abbrev-canonical-name"));
        muscle.muscleGroupId = Utils.getInteger(jsonObject, key("muscle-group-id"));
        muscle.aliasIds = new Gson().fromJson(jsonObject.getAsJsonArray(key("alias-ids")), Integer[].class);
        return muscle;
    }

    public String canonicalName;
    public String abbrevCanonicalName;
    public Integer muscleGroupId;
    public Integer aliasIds[];

    @Override
    public final int hashCode() {
        return new HashCodeBuilder(13, 15).
                append(canonicalName).
                append(abbrevCanonicalName).
                append(muscleGroupId).
                toHashCode();
    }

    @Override
    public final boolean equals(final Object object) {
        if (object == this) { return true; }
        if (!object.getClass().equals(getClass())) {
            return false;
        }
        final Muscle muscle = (Muscle)object;
        return super.equals(object) &&
                Utils.equalOrBothNull(canonicalName, muscle.canonicalName) &&
                Utils.equalOrBothNull(abbrevCanonicalName, muscle.abbrevCanonicalName) &&
                Utils.equalOrBothNull(muscleGroupId, muscle.muscleGroupId);
    }

    public final void overwriteDomainProperties(final Muscle muscle) {
        canonicalName = muscle.canonicalName;
        abbrevCanonicalName = muscle.abbrevCanonicalName;
        muscleGroupId = muscle.muscleGroupId;
    }

    @Override
    public final void overwrite(final ModelSupport modelSupport) {
        super.overwrite(modelSupport);
        overwriteDomainProperties((Muscle)modelSupport);
    }

    public enum Id {

        DELTS_REAR  (1),
        DELTS_FRONT (2),
        DELTS_SIDE  (3),
        BACK_UPPER  (5),
        BACK_LOWER  (6),
        CHEST_UPPER (8),
        CHEST_LOWER (9),
        ABS_UPPER   (11),
        ABS_LOWER   (12),
        SERRATUS    (13),
        QUADS       (14),
        HAMS        (15),
        CALFS       (16),
        BICEPS      (18),
        FOREARMS    (19),
        TRAPS       (20),
        GLUTES      (21),
        TRICEP_LAT  (22),
        TRICEP_LONG (23),
        TRICEP_MED  (24),
        OBLIQUES    (25),
        HIP_ABDUCTORS (26),
        HIP_FLEXORS (27)
        ;

        public final int id;

        Id(final int id) {
            this.id = id;
        }
    }
}
