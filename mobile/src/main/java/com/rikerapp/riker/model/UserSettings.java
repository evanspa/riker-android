package com.rikerapp.riker.model;

import com.google.gson.JsonObject;
import com.rikerapp.riker.Utils;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.parceler.Parcel;

@Parcel
public class UserSettings extends MainSupport implements Cloneable {

    public static final String JSON_KEY_PREFIX = "usersettings";

    private static String key(final String key) {
        return String.format("%s/%s", JSON_KEY_PREFIX, key);
    }

    public static UserSettings toUserSettings(final JsonObject jsonObject, final String globalIdentifier) {
        final UserSettings userSettings = new UserSettings();
        userSettings.populateCommon(globalIdentifier, jsonObject, JSON_KEY_PREFIX);
        userSettings.weightUom = Utils.getInteger(jsonObject, key("weight-uom"));
        userSettings.sizeUom = Utils.getInteger(jsonObject, key("size-uom"));
        userSettings.weightIncDecAmount = Utils.getInteger(jsonObject, key("weight-inc-dec-amount"));
        return userSettings;
    }

    public Integer weightUom;
    public Integer sizeUom;
    public Integer weightIncDecAmount;

    @Override
    public int hashCode() {
        return new HashCodeBuilder(7, 9).
                append(sizeUom).
                append(weightUom).
                append(weightIncDecAmount).
                toHashCode();
    }

    @Override
    public boolean equals(final Object object) {
        if (object == this) { return true; }
        if (!object.getClass().equals(getClass())) {
            return false;
        }
        final UserSettings userSettings = (UserSettings)object;
        return super.equals(object) &&
                Utils.equalOrBothNull(weightUom, userSettings.weightUom) &&
                Utils.equalOrBothNull(sizeUom, userSettings.sizeUom) &&
                Utils.equalOrBothNull(weightIncDecAmount, userSettings.weightIncDecAmount);
    }

    public void overwriteDomainProperties(final UserSettings userSettings) {
        super.overwriteDomainProperties(userSettings);
        weightUom = userSettings.weightUom;
        sizeUom = userSettings.sizeUom;
        weightIncDecAmount = userSettings.weightIncDecAmount;
    }

    public void overwrite(final UserSettings userSettings) {
        super.overwrite(userSettings);
        overwriteDomainProperties(userSettings);
    }
}
