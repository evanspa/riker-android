package com.rikerapp.riker.model;

import com.google.gson.JsonObject;
import com.rikerapp.riker.Utils;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.parceler.Parcel;

import java.math.BigDecimal;
import java.util.Date;

@Parcel
public final class BodyMeasurementLog extends MainSupport {

    public static final String JSON_KEY_PREFIX = "bodyjournallog";

    private static String key(final String key) {
        return String.format("%s/%s", JSON_KEY_PREFIX, key);
    }

    public static BodyMeasurementLog toBml(final JsonObject jsonObject, final String globalIdentifier) {
        final BodyMeasurementLog bml = new BodyMeasurementLog();
        bml.populateCommon(globalIdentifier, jsonObject, JSON_KEY_PREFIX);
        bml.originationDeviceId = Utils.getInteger(jsonObject, key("origination-device-id"));
        bml.loggedAt = Utils.getDate(jsonObject, key("logged-at"));
        bml.importedAt = Utils.getDate(jsonObject, key("imported-at"));
        bml.waistSize = Utils.getBigDecimal(jsonObject, key("waist-size"));
        bml.chestSize = Utils.getBigDecimal(jsonObject, key("chest-size"));
        bml.armSize = Utils.getBigDecimal(jsonObject, key("arm-size"));
        bml.forearmSize = Utils.getBigDecimal(jsonObject, key("forearm-size"));
        bml.neckSize = Utils.getBigDecimal(jsonObject, key("neck-size"));
        bml.thighSize = Utils.getBigDecimal(jsonObject, key("thigh-size"));
        bml.bodyWeight = Utils.getBigDecimal(jsonObject, key("body-weight"));
        bml.calfSize = Utils.getBigDecimal(jsonObject, key("calf-size"));
        bml.sizeUom = Utils.getInteger(jsonObject, key("size-uom"));
        bml.bodyWeightUom = Utils.getInteger(jsonObject, key("body-weight-uom"));
        return bml;
    }

    public BigDecimal bodyWeight;
    public Integer bodyWeightUom;
    public BigDecimal armSize;
    public BigDecimal calfSize;
    public BigDecimal neckSize;
    public BigDecimal forearmSize;
    public BigDecimal chestSize;
    public BigDecimal waistSize;
    public BigDecimal thighSize;
    public Integer sizeUom;
    public Integer originationDeviceId;
    public Date importedAt;

    @Override
    public final int hashCode() {
        return new HashCodeBuilder(27, 29).
                append(bodyWeight).
                append(bodyWeightUom).
                append(armSize).
                append(calfSize).
                append(neckSize).
                append(forearmSize).
                append(chestSize).
                append(waistSize).
                append(thighSize).
                append(sizeUom).
                append(loggedAt).
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
        final BodyMeasurementLog bodyMeasurementLog = (BodyMeasurementLog)object;
        return super.equals(object) &&
                Utils.equalOrBothNull(bodyWeight, bodyMeasurementLog.bodyWeight) &&
                Utils.equalOrBothNull(bodyWeightUom, bodyMeasurementLog.bodyWeightUom) &&
                Utils.equalOrBothNull(armSize, bodyMeasurementLog.armSize) &&
                Utils.equalOrBothNull(calfSize, bodyMeasurementLog.calfSize) &&
                Utils.equalOrBothNull(neckSize, bodyMeasurementLog.neckSize) &&
                Utils.equalOrBothNull(forearmSize, bodyMeasurementLog.forearmSize) &&
                Utils.equalOrBothNull(chestSize, bodyMeasurementLog.chestSize) &&
                Utils.equalOrBothNull(waistSize, bodyMeasurementLog.waistSize) &&
                Utils.equalOrBothNull(thighSize, bodyMeasurementLog.thighSize) &&
                Utils.equalOrBothNull(sizeUom, bodyMeasurementLog.sizeUom) &&
                Utils.equalOrBothNull(loggedAt, bodyMeasurementLog.loggedAt) &&
                Utils.equalOrBothNull(originationDeviceId, bodyMeasurementLog.originationDeviceId) &&
                Utils.equalOrBothNull(importedAt, bodyMeasurementLog.importedAt);
    }

    public final void overwriteDomainProperties(final BodyMeasurementLog bodyMeasurementLog) {
        super.overwriteDomainProperties(bodyMeasurementLog);
        bodyWeight = bodyMeasurementLog.bodyWeight;
        bodyWeightUom = bodyMeasurementLog.bodyWeightUom;
        armSize = bodyMeasurementLog.armSize;
        calfSize = bodyMeasurementLog.calfSize;
        neckSize = bodyMeasurementLog.neckSize;
        forearmSize = bodyMeasurementLog.forearmSize;
        chestSize = bodyMeasurementLog.chestSize;
        waistSize = bodyMeasurementLog.waistSize;
        thighSize = bodyMeasurementLog.thighSize;
        sizeUom = bodyMeasurementLog.sizeUom;
        loggedAt = bodyMeasurementLog.loggedAt;
        originationDeviceId = bodyMeasurementLog.originationDeviceId;
        importedAt = bodyMeasurementLog.importedAt;
    }

    @Override
    public final void overwrite(final ModelSupport modelSupport) {
        super.overwrite(modelSupport);
        overwriteDomainProperties((BodyMeasurementLog) modelSupport);
    }
}
