package com.rikerapp.riker.model;

import com.google.gson.JsonObject;
import com.rikerapp.riker.Utils;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.parceler.Parcel;

@Parcel
public final class BodySegment extends MasterSupport {

    public static final String JSON_KEY_PREFIX = "bodysegment";

    private static String key(final String key) {
        return String.format("%s/%s", JSON_KEY_PREFIX, key);
    }

    public static BodySegment toBodySegment(final JsonObject jsonObject, final String globalIdentifier) {
        final BodySegment bodySegment = new BodySegment();
        bodySegment.populateCommon(globalIdentifier, jsonObject, JSON_KEY_PREFIX);
        bodySegment.name = Utils.getString(jsonObject, key("name"));
        return bodySegment;
    }

    public String name;

    @Override
    public final int hashCode() {
        return new HashCodeBuilder(9, 11).append(name).toHashCode();
    }

    @Override
    public final boolean equals(final Object object) {
        if (object == this) { return true; }
        if (!object.getClass().equals(getClass())) {
            return false;
        }
        final BodySegment bodySegment = (BodySegment)object;
        return super.equals(object) && Utils.equalOrBothNull(name, bodySegment.name);
    }

    public final void overwriteDomainProperties(final BodySegment bodySegment) {
        name = bodySegment.name;
    }

    @Override
    public final void overwrite(final ModelSupport modelSupport) {
        super.overwrite(modelSupport);
        overwriteDomainProperties((BodySegment)modelSupport);
    }

    public enum Id {

        UPPER_BODY(0),
        LOWER_BODY(1),
        ;

        public final int id;

        Id(final int id) {
            this.id = id;
        }

        public static Id bodySegmentIdById(final int id) {
            if (id == UPPER_BODY.id) {
                return UPPER_BODY;
            } else if (id == LOWER_BODY.id) {
                return LOWER_BODY;
            }
            return null;
        }
    }
}
