package com.rikerapp.riker.model;

import android.support.annotation.DrawableRes;
import android.support.annotation.LayoutRes;

import com.google.gson.JsonObject;
import com.rikerapp.riker.R;
import com.rikerapp.riker.Utils;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.parceler.Parcel;

@Parcel
public final class OriginationDevice extends MasterSupport {

    public static final String JSON_KEY_PREFIX = "originationdevice";

    private static String key(final String key) {
        return String.format("%s/%s", JSON_KEY_PREFIX, key);
    }

    public static OriginationDevice toOriginationDevice(final JsonObject jsonObject, final String globalIdentifier) {
        final OriginationDevice originationDevice = new OriginationDevice();
        originationDevice.populateCommon(globalIdentifier, jsonObject, JSON_KEY_PREFIX);
        originationDevice.name = Utils.getString(jsonObject, key("name"));
        originationDevice.iconImageName = Utils.getString(jsonObject, key("icon-image-name"));
        return originationDevice;
    }

    public String name;
    public String iconImageName;
    public boolean hasLocalImage;

    @Override
    public final int hashCode() {
        return new HashCodeBuilder(23, 25).
                append(name).
                append(iconImageName).
                append(hasLocalImage).
                toHashCode();
    }

    @Override
    public final boolean equals(final Object object) {
        if (object == this) { return true; }
        if (!object.getClass().equals(getClass())) {
            return false;
        }
        final OriginationDevice originationDevice = (OriginationDevice)object;
        return super.equals(object) &&
                Utils.equalOrBothNull(name, originationDevice.name) &&
                Utils.equalOrBothNull(iconImageName, originationDevice.iconImageName) &&
                Utils.equalOrBothNull(hasLocalImage, originationDevice.hasLocalImage);
    }

    public final void overwriteDomainProperties(final OriginationDevice originationDevice) {
        name = originationDevice.name;
        iconImageName = originationDevice.iconImageName;
        hasLocalImage = originationDevice.hasLocalImage;
    }

    @Override
    public final void overwrite(final ModelSupport modelSupport) {
        super.overwrite(modelSupport);
        overwriteDomainProperties((OriginationDevice) modelSupport);
    }

    public enum Id {

        WEB          (1, "web", R.drawable.orig_device_web),
        //PEBBLE       (2, 0),
        IPHONE       (3, "iPhone", R.drawable.orig_device_iphone),
        IPAD         (4, "iPad", R.drawable.orig_device_ipad),
        APPLE_WATCH  (5, "Apple Watch", R.drawable.orig_device_apple_watch),
        ANDROID_WEAR (6, "Android Wear", R.drawable.orig_device_android_wear),
        ANDROID      (7, "Android", R.drawable.orig_device_android)
        ;

        public final int id;
        public final String deviceName;
        @DrawableRes public final int drawableResource;


        Id(final int id, final String deviceName, @DrawableRes final int drawableResource) {
            this.id = id;
            this.deviceName = deviceName;
            this.drawableResource = drawableResource;
        }

        public static Id originationDeviceIdById(final int id) {
            final Id[] originationDeviceIds = Id.values();
            for (int i = 0; i < originationDeviceIds.length; i++) {
                if (originationDeviceIds[i].id == id) {
                    return originationDeviceIds[i];
                }
            }
            return null;
        }
    }
}
