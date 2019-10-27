package com.rikerapp.riker.model;

import com.google.gson.JsonObject;
import com.rikerapp.riker.Utils;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.parceler.Parcel;

import java.io.Serializable;

@Parcel
public final class MuscleGroup extends MasterSupport implements Serializable {

    public static final String JSON_KEY_PREFIX = "musclegroup";

    private static String key(final String key) {
        return String.format("%s/%s", JSON_KEY_PREFIX, key);
    }

    public static MuscleGroup toMuscleGroup(final JsonObject jsonObject, final String globalIdentifier) {
        final MuscleGroup muscleGroup = new MuscleGroup();
        muscleGroup.populateCommon(globalIdentifier, jsonObject, JSON_KEY_PREFIX);
        muscleGroup.name = Utils.getString(jsonObject, key("name"));
        muscleGroup.abbrevName = Utils.getString(jsonObject, key("abbrev-name"));
        muscleGroup.bodySegmentId = Utils.getInteger(jsonObject, key("body-segment-id"));
        return muscleGroup;
    }

    public String name;
    public String abbrevName;
    public Integer bodySegmentId;

    @Override
    public final int hashCode() {
        return new HashCodeBuilder(11, 13).
                append(name).
                append(abbrevName).
                append(bodySegmentId).
                toHashCode();
    }

    @Override
    public final boolean equals(final Object object) {
        if (object == this) { return true; }
        if (!object.getClass().equals(getClass())) {
            return false;
        }
        final MuscleGroup muscleGroup = (MuscleGroup)object;
        return super.equals(object) &&
                Utils.equalOrBothNull(name, muscleGroup.name) &&
                Utils.equalOrBothNull(abbrevName, muscleGroup.abbrevName) &&
                Utils.equalOrBothNull(bodySegmentId, muscleGroup.bodySegmentId);
    }

    public final void overwriteDomainProperties(final MuscleGroup muscleGroup) {
        name = muscleGroup.name;
        abbrevName = muscleGroup.abbrevName;
        bodySegmentId = muscleGroup.bodySegmentId;
    }

    @Override
    public final void overwrite(final ModelSupport modelSupport) {
        super.overwrite(modelSupport);
        overwriteDomainProperties((MuscleGroup)modelSupport);
    }

    public enum Id {

        SHOULDERS(0),
        BACK(1),
        CHEST(2),
        CORE(3),
        QUADS(5),
        HAMS(6),
        CALF(7),
        TRICEPS(8),
        BICEPS(9),
        FOREARMS(10),
        GLUTES(11),
        HIP_ABDUCTORS(12),
        HIP_FLEXORS(13)
        ;

        public final int id;

        Id(final int id) {
            this.id = id;
        }

        public static Id muscleGroupIdById(final int id) {
            final Id[] muscleGroupIds = MuscleGroup.Id.values();
            for (int i = 0; i < muscleGroupIds.length; i++) {
                if (muscleGroupIds[i].id == id) {
                    return muscleGroupIds[i];
                }
            }
            return null;
        }
    }
}
