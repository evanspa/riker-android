package com.rikerapp.riker.model;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.rikerapp.riker.Utils;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.parceler.Parcel;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Parcel
public final class Movement extends MasterSupport implements Serializable {

    public static final String JSON_KEY_PREFIX = "movement";

    private static String key(final String key) {
        return String.format("%s/%s", JSON_KEY_PREFIX, key);
    }

    public static Movement toMovement(final JsonObject jsonObject, final String globalIdentifier) {
        final Movement movement = new Movement();
        movement.populateCommon(globalIdentifier, jsonObject, JSON_KEY_PREFIX);
        movement.canonicalName = Utils.getString(jsonObject, key("canonical-name"));
        movement.isBodyLift = Utils.getBoolean(jsonObject, key("is-body-lift"));
        movement.sortOrder = Utils.getInteger(jsonObject, key("sort-order"));
        movement.variantMask = Utils.getInteger(jsonObject, key("variant-mask"));
        movement.aliases = new Gson().fromJson(jsonObject.getAsJsonArray(key("aliases")), String[].class);
        movement.primaryMuscleIdList = new ArrayList<>(Arrays.asList(new Gson().fromJson(jsonObject.getAsJsonArray(key("primary-muscle-ids")), Integer[].class)));
        movement.secondaryMuscleIdList = new ArrayList<>(Arrays.asList(new Gson().fromJson(jsonObject.getAsJsonArray(key("secondary-muscle-ids")), Integer[].class)));
        return movement;
    }

    public String canonicalName;
    public boolean isBodyLift;
    public BigDecimal percentageOfBodyWeight;
    public Integer variantMask;
    public Integer sortOrder;
    public List<Integer> primaryMuscleIdList = new ArrayList<>();
    public List<Integer> secondaryMuscleIdList = new ArrayList<>();
    public String aliases[];

    @Override
    public final int hashCode() {
        return new HashCodeBuilder(17, 19).
                append(canonicalName).
                append(isBodyLift).
                append(percentageOfBodyWeight).
                append(variantMask).
                append(sortOrder).
                toHashCode();
    }

    @Override
    public final boolean equals(final Object object) {
        if (object == this) { return true; }
        if (!object.getClass().equals(getClass())) {
            return false;
        }
        final Movement movement = (Movement)object;
        return super.equals(object) &&
                Utils.equalOrBothNull(canonicalName, movement.canonicalName) &&
                isBodyLift == movement.isBodyLift &&
                Utils.equalOrBothNull(percentageOfBodyWeight, movement.percentageOfBodyWeight) &&
                Utils.equalOrBothNull(variantMask, movement.variantMask) &&
                Utils.equalOrBothNull(sortOrder, movement.sortOrder);
    }

    public final void overwriteDomainProperties(final Movement movement) {
        canonicalName = movement.canonicalName;
        isBodyLift = movement.isBodyLift;
        percentageOfBodyWeight = movement.percentageOfBodyWeight;
        variantMask = movement.variantMask;
        sortOrder = movement.sortOrder;
        primaryMuscleIdList = movement.primaryMuscleIdList;
        secondaryMuscleIdList = movement.secondaryMuscleIdList;
    }

    @Override
    public final void overwrite(final ModelSupport modelSupport) {
        super.overwrite(modelSupport);
        overwriteDomainProperties((Movement)modelSupport);
    }
}
