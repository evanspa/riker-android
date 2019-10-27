package com.rikerapp.riker.model;

import com.google.gson.JsonObject;
import com.rikerapp.riker.Utils;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.parceler.Parcel;

@Parcel
public class ModelSupport implements Identifiable, Cloneable {

    public Integer localIdentifier;
    public String globalIdentifier;

    public void populateCommon(final String globalIdentifier, final JsonObject jsonObject, final String keyPrefix) {
        this.globalIdentifier = globalIdentifier;
        this.localIdentifier = Utils.getInteger(jsonObject, String.format("%s/id", keyPrefix));
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(1, 3).
                append(localIdentifier).
                append(globalIdentifier).
                toHashCode();
    }

    @Override
    public boolean equals(final Object object) {
        if (object == this) { return true; }
        if (!object.getClass().equals(getClass())) {
            return false;
        }
        final ModelSupport modelSupport = (ModelSupport)object;
        return super.equals(object) &&
                Utils.equalOrBothNull(globalIdentifier, modelSupport.globalIdentifier);
    }

    @Override
    public final boolean doesHaveEqualIdentifiers(final Identifiable identifiable) {
        final ModelSupport modelSupport = (ModelSupport)identifiable;
        if (globalIdentifier != null && modelSupport.globalIdentifier != null) {
            return globalIdentifier.equals(modelSupport.globalIdentifier);
        } else if (localIdentifier != null && modelSupport.localIdentifier != null) {
            return localIdentifier.equals(modelSupport.localIdentifier);
        }
        return false;
    }

    public void overwrite(final ModelSupport modelSupport) {
        globalIdentifier = modelSupport.globalIdentifier;
    }
}
