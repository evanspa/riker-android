package com.rikerapp.riker.model;

public enum SizeUnit {

    INCHES("in", 0),
    CM("cm", 1)
    ;

    public final String name;
    public final int id;

    SizeUnit(final String name, final int id) {
        this.name = name;
        this.id = id;
    }

    public static SizeUnit sizeUnitById(final int id) {
        final SizeUnit[] sizeUnits = SizeUnit.values();
        for (int i = 0; i < sizeUnits.length; i++) {
            if (sizeUnits[i].id == id) {
                return sizeUnits[i];
            }
        }
        return null;
    }
}
