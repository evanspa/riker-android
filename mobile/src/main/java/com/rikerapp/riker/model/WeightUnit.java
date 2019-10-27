package com.rikerapp.riker.model;

public enum WeightUnit {

    LBS("lbs", 0),
    KG("kg", 1)
    ;

    public final String name;
    public final int id;

    WeightUnit(final String name, final int id) {
        this.name = name;
        this.id = id;
    }

    public static WeightUnit weightUnitById(final int id) {
        final WeightUnit[] weightUnits = WeightUnit.values();
        for (int i = 0; i < weightUnits.length; i++) {
            if (weightUnits[i].id == id) {
                return weightUnits[i];
            }
        }
        return null;
    }
}
