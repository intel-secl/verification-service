/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.flavor.model;

import com.intel.mtwilson.core.flavor.common.FlavorPart;
import com.intel.mtwilson.jaxrs2.Document;

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

    public void setMatchPolicy(MatchPolicy matchPolicy) {
        this.matchPolicy = matchPolicy;
    }   

}
