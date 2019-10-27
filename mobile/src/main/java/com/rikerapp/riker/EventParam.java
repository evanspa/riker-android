package com.rikerapp.riker;

public class EventParam {

    public final ParamName paramName;
    public final String value;

    public EventParam(final ParamName paramName, final String value) {
        this.paramName = paramName;
        this.value = value;
    }

    public enum ParamName {

        NUM_EXPORTED("num_exported")
        ;

        public final String name;

        ParamName(final String name) {
            this.name = name;
        }
    }
}
