package com.rikerapp.riker.model;

import com.google.gson.JsonObject;
import com.rikerapp.riker.Utils;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.parceler.Parcel;

import java.math.BigDecimal;
import java.util.Date;

@Parcel
public final class Set extends MainSupport {

    public static final String JSON_KEY_PREFIX = "set";

    private static String key(final String key) {
        return String.format("%s/%s", JSON_KEY_PREFIX, key);
    }

    public static Set toSet(final JsonObject jsonObject, final String globalIdentifier) {
        final Set set = new Set();
        set.populateCommon(globalIdentifier, jsonObject, JSON_KEY_PREFIX);
        set.weight = Utils.getBigDecimal(jsonObject, key("weight"));
        set.toFailure = Utils.getBoolean(jsonObject, key("to-failure"));
        set.movementId = Utils.getInteger(jsonObject, key("movement-id"));
        set.movementVariantId = Utils.getInteger(jsonObject, key("movement-variant-id"));
        set.originationDeviceId = Utils.getInteger(jsonObject, key("origination-device-id"));
        set.negatives = Utils.getBoolean(jsonObject, key("negatives"));
        set.loggedAt = Utils.getDate(jsonObject, key("logged-at"));
        set.importedAt = Utils.getDate(jsonObject, key("imported-at"));
        set.numReps = Utils.getInteger(jsonObject, key("num-reps"));
        set.weightUom = Utils.getInteger(jsonObject, key("weight-uom"));
        set.ignoreTime = Utils.getBoolean(jsonObject, key("ignore-time"));
        return set;
    }

    public Integer numReps;
    public BigDecimal weight;
    public Integer weightUom;
    public boolean negatives;
    public boolean toFailure;
    public boolean ignoreTime;
    public Integer movementId;
    public Integer movementVariantId;
    public Integer originationDeviceId;
    public Date importedAt;
    // the following are not "core" properties, but are instead here for convenience
    public boolean realTime;

    public static Set minimalCopy(final Set set) {
        final Set setCopy = new Set();
        setCopy.numReps = set.numReps;
        setCopy.weight = set.weight;
        setCopy.weightUom = set.weightUom;
        setCopy.loggedAt = set.loggedAt;
        setCopy.movementId = set.movementId;
        setCopy.movementVariantId = set.movementVariantId;
        return setCopy;
    }

    @Override
    public final int hashCode() {
        return new HashCodeBuilder(25, 27).
                append(numReps).
                append(weight).
                append(weightUom).
                append(negatives).
                append(toFailure).
                append(loggedAt).
                append(ignoreTime).
                append(movementId).
                append(movementVariantId).
                append(originationDeviceId).
                append(importedAt).
                toHashCode();
    }

    @Override
    public final boolean equals(final Object object) {
        if (object == this) { return true; }
        if (!object.getClass().equals(getClass())) {
            return false;
        }
        final Set set = (Set)object;
        return super.equals(object) &&
                Utils.equalOrBothNull(numReps, set.numReps) &&
                Utils.equalOrBothNull(weight, set.weight) &&
                Utils.equalOrBothNull(weightUom, set.weightUom) &&
                Utils.equalOrBothNull(negatives, set.negatives) &&
                Utils.equalOrBothNull(toFailure, set.toFailure) &&
                Utils.equalOrBothNull(loggedAt, set.loggedAt) &&
                Utils.equalOrBothNull(ignoreTime, set.ignoreTime) &&
                Utils.equalOrBothNull(movementId, set.movementId) &&
                Utils.equalOrBothNull(movementVariantId, set.movementVariantId) &&
                Utils.equalOrBothNull(originationDeviceId, set.originationDeviceId) &&
                Utils.equalOrBothNull(importedAt, set.importedAt);
    }

    public final void overwriteDomainProperties(final Set set) {
        super.overwriteDomainProperties(set);
        numReps = set.numReps;
        weight = set.weight;
        weightUom = set.weightUom;
        negatives = set.negatives;
        toFailure = set.toFailure;
        loggedAt = set.loggedAt;
        ignoreTime = set.ignoreTime;
        movementId = set.movementId;
        movementVariantId = set.movementVariantId;
        originationDeviceId = set.originationDeviceId;
        importedAt = set.importedAt;
    }

    @Override
    public final void overwrite(final ModelSupport modelSupport) {
        super.overwrite(modelSupport);
        overwriteDomainProperties((Set) modelSupport);
    }
}
