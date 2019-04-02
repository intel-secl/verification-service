/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.flavor.model;

import com.intel.mtwilson.core.flavor.common.FlavorPart;
import com.intel.mtwilson.jaxrs2.Document;

import java.util.Objects;

/**
 *
 * @author dtiwari
 */

public class FlavorMatchPolicy extends Document {

    private FlavorPart flavorPart;
    private MatchPolicy matchPolicy;
    
    public FlavorMatchPolicy() { }
    
    public FlavorMatchPolicy(FlavorPart flavorPart, MatchPolicy matchPolicy) {
        this.flavorPart = flavorPart;
        this.matchPolicy = matchPolicy;
    }
    
    public FlavorPart getFlavorPart() {
        return flavorPart;
    }

    public void setFlavorPart(FlavorPart flavorPart) {
        this.flavorPart = flavorPart;
    }

    public MatchPolicy getMatchPolicy() {
        return matchPolicy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FlavorMatchPolicy that = (FlavorMatchPolicy) o;
        return flavorPart == that.flavorPart &&
                Objects.equals(matchPolicy, that.matchPolicy);
    }

    @Override
    public String toString() {
        return "FlavorMatchPolicy{" +
                "flavorPart=" + flavorPart +
                ", matchPolicy=" + matchPolicy +
                '}';
    }

    @Override
    public int hashCode() {

        return Objects.hash(flavorPart, matchPolicy);
    }

    public void setMatchPolicy(MatchPolicy matchPolicy) {
        this.matchPolicy = matchPolicy;
    }   

}
