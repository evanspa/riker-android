package com.rikerapp.riker.model;

import com.google.gson.JsonObject;

public final class HttpResponseTuple {

    public final int code;
    public final JsonObject responseBody;
    public final String locationHeaderVal;
    public final String authTokenHeaderVal;
    public final String errorMaskHeaderVal;

    public HttpResponseTuple(
            final int code,
            final JsonObject responseBody,
            final String locationHeaderVal,
            final String authTokenHeaderVal,
            final String errorMaskHeaderVal) {
        this.code = code;
        this.responseBody = responseBody;
        this.locationHeaderVal = locationHeaderVal;
        this.authTokenHeaderVal = authTokenHeaderVal;
        this.errorMaskHeaderVal = errorMaskHeaderVal;
    }
}
