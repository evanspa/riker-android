package com.rikerapp.riker.model;

import com.google.android.gms.fitness.data.Field;
import com.google.gson.JsonObject;
import com.rikerapp.riker.Utils;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.parceler.Parcel;

@Parcel
public final class MovementVariant extends MasterSupport {

    public static final String JSON_KEY_PREFIX = "movementvariant";

    private static String key(final String key) {
        return String.format("%s/%s", JSON_KEY_PREFIX, key);
    }

    public static MovementVariant toMovementVariant(final JsonObject jsonObject, final String globalIdentifier) {
        final MovementVariant movementVariant = new MovementVariant();
        movementVariant.populateCommon(globalIdentifier, jsonObject, JSON_KEY_PREFIX);
        movementVariant.name = Utils.getString(jsonObject, key("name"));
        movementVariant.abbrevName = Utils.getString(jsonObject, key("abbrev-name"));
        movementVariant.variantDescription = Utils.getString(jsonObject, key("description"));
        movementVariant.sortOrder = Utils.getInteger(jsonObject, key("sort-order"));
        return movementVariant;
    }

    public String name;
    public String abbrevName;
    public String variantDescription;
    public Integer sortOrder;

    @Override
    public final int hashCode() {
        return new HashCodeBuilder(21, 23).
                append(name).
                append(abbrevName).
                append(variantDescription).
                append(sortOrder).
                toHashCode();
    }

    @Override
    public final boolean equals(final Object object) {
        if (object == this) { return true; }
        if (!object.getClass().equals(getClass())) {
            return false;
        }
        final MovementVariant movementVariant = (MovementVariant)object;
        return super.equals(object) &&
                Utils.equalOrBothNull(name, movementVariant.name) &&
                Utils.equalOrBothNull(abbrevName, movementVariant.abbrevName) &&
                Utils.equalOrBothNull(variantDescription, movementVariant.variantDescription) &&
                Utils.equalOrBothNull(sortOrder, movementVariant.sortOrder);
    }

    public final void overwriteDomainProperties(final MovementVariant movementVariant) {
        name = movementVariant.name;
        abbrevName = movementVariant.abbrevName;
        variantDescription = movementVariant.variantDescription;
        sortOrder = movementVariant.sortOrder;
    }

    @Override
    public final void overwrite(final ModelSupport modelSupport) {
        super.overwrite(modelSupport);
        overwriteDomainProperties((MovementVariant) modelSupport);
    }

    public enum Id {

        BARBELL(1, Field.RESISTANCE_TYPE_BARBELL),
        DUMBBELL(2, Field.RESISTANCE_TYPE_DUMBBELL),
        MACHINE(4, Field.RESISTANCE_TYPE_MACHINE),
        SMITH_MACHINE(8, Field.RESISTANCE_TYPE_MACHINE),
        CABLE(16, Field.RESISTANCE_TYPE_CABLE),
        CURL_BAR(32, Field.RESISTANCE_TYPE_BARBELL),
        SLED(64, Field.RESISTANCE_TYPE_MACHINE),
        BODY(128, Field.RESISTANCE_TYPE_BODY),
        KETTLEBELL(256, Field.RESISTANCE_TYPE_KETTLEBELL)
        ;

        public final int id;
        public final int googleFitResistanceType;

        Id(final int id, final int googleFitResistanceType) {
            this.id = id;
            this.googleFitResistanceType = googleFitResistanceType;
        }

        public static Id movementVariantIdByRawId(final int id) {
            final Id[] ids = Id.values();
            for (int i = 0; i < ids.length; i++) {
                if (ids[i].id == id) {
                    return ids[i];
                }
            }
            return null;
        }
    }
}
