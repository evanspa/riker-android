package com.rikerapp.riker.model;

import com.google.gson.JsonObject;
import com.rikerapp.riker.Utils;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.parceler.Parcel;

@Parcel
public final class MuscleAlias extends MasterSupport {

    public static final String JSON_KEY_PREFIX = "musclealias";

    private static String key(final String key) {
        return String.format("%s/%s", JSON_KEY_PREFIX, key);
    }

    public static MuscleAlias toMuscleAlias(final JsonObject jsonObject, final String globalIdentifier) {
        final MuscleAlias muscleAlias = new MuscleAlias();
        muscleAlias.populateCommon(globalIdentifier, jsonObject, JSON_KEY_PREFIX);
        muscleAlias.alias = Utils.getString(jsonObject, key("alias"));
        muscleAlias.muscleId = Utils.getInteger(jsonObject, key("muscle-id"));
        return muscleAlias;
    }

    public String alias;
    public Integer muscleId;

    @Override
    public final int hashCode() {
        return new HashCodeBuilder(15, 17).append(alias).append(muscleId).toHashCode();
    }

    @Override
    public final boolean equals(final Object object) {
        if (object == this) { return true; }
        if (!object.getClass().equals(getClass())) {
            return false;
        }
        final MuscleAlias muscleAlias = (MuscleAlias)object;
        return super.equals(object) &&
                Utils.equalOrBothNull(alias, muscleAlias.alias) &&
                Utils.equalOrBothNull(muscleId, muscleAlias.muscleId);
    }

    public final void overwriteDomainProperties(final MuscleAlias muscleAlias) {
        alias = muscleAlias.alias;
        muscleId = muscleAlias.muscleId;
    }

    @Override
    public final void overwrite(final ModelSupport modelSupport) {
        super.overwrite(modelSupport);
        overwriteDomainProperties((MuscleAlias)modelSupport);
    }

    public enum Id {

        REAR_DELTS(3),
        POSTERIOR_DELTS(4),
        FRONT_DELTS(5),
        ANTERIOR_DELTS(6),
        SIDE_DELTS(7),
        MIDDLE_DELTS(8),
        OUTER_DELTS(9),
        LATERAL_DELTOIDS(10),
        UPPER_LATS(13),
        LOWER_LATS(14),
        UPPER_PECS(17),
        LOWER_PECS(18),
        UPPER_ABDOMINALS(20),
        LOWER_ABDOMINALS(21),
        QUADS(22),
        HAMS(23),
        TRAPEZIUS(24),
        BUTT(25),
        BUTTOCKS(26),
        GLUTEUS_MAXIMUS(27)
        ;

        public final int id;

        Id(final int id) {
            this.id = id;
        }
    }
}
