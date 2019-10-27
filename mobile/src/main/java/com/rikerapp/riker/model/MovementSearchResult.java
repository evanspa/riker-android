package com.rikerapp.riker.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public final class MovementSearchResult {

    public Integer movementId;
    public String canonicalName;
    public boolean isBodyLift;
    public BigDecimal percentageOfBodyWeight;
    public Integer variantMask;
    public Integer sortOrder;
    public List<String> aliases = new ArrayList<>();
}
