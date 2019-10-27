package com.rikerapp.riker.model;

import com.google.gson.JsonObject;
import com.rikerapp.riker.Utils;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.parceler.Parcel;

@Parcel
public final class MovementAlias extends MasterSupport {

    public static final String JSON_KEY_PREFIX = "musclealias";

    private static String key(final String key) {
        return String.format("%s/%s", JSON_KEY_PREFIX, key);
    }

    public static MovementAlias toMovementAlias(final JsonObject jsonObject, final String globalIdentifier) {
        final MovementAlias movementAlias = new MovementAlias();
        movementAlias.populateCommon(globalIdentifier, jsonObject, JSON_KEY_PREFIX);
        movementAlias.alias = Utils.getString(jsonObject, key("alias"));
        movementAlias.movementId = Utils.getInteger(jsonObject, key("movement-id"));
        return movementAlias;
    }

    public String alias;
    public Integer movementId;

    @Override
    public final int hashCode() {
        return new HashCodeBuilder(19, 21).append(alias).append(movementId).toHashCode();
    }

    @Override
    public final boolean equals(final Object object) {
        if (object == this) { return true; }
        if (!object.getClass().equals(getClass())) {
            return false;
        }
        final MovementAlias movementAlias = (MovementAlias)object;
        return super.equals(object) &&
                Utils.equalOrBothNull(alias, movementAlias.alias) &&
                Utils.equalOrBothNull(movementId, movementAlias.movementId);
    }

    public final void overwriteDomainProperties(final MovementAlias movementAlias) {
        alias = movementAlias.alias;
        movementId = movementAlias.movementId;
    }

    @Override
    public final void overwrite(final ModelSupport modelSupport) {
        super.overwrite(modelSupport);
        overwriteDomainProperties((MovementAlias)modelSupport);
    }
}
