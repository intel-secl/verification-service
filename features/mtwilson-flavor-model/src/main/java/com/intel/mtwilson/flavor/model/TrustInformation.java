/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.intel.mtwilson.core.flavor.common.FlavorPart;

import java.util.Map;

public class TrustInformation {

    @JsonProperty("OVERALL")
    boolean overall;

    Map<FlavorPart, FlavorsTrustStatus> flavorsTrust;
    
    public TrustInformation() {
    }

    public TrustInformation(boolean overall, Map<FlavorPart, FlavorsTrustStatus> flavorsTrust) {
        this.overall = overall;
        this.flavorsTrust = flavorsTrust;
    }

    public boolean isOverall() {
        return overall;
    }

    public void setOverall(boolean overall) {
        this.overall = overall;
    }
    
    public Map<FlavorPart, FlavorsTrustStatus> getFlavorsTrust() {
        return flavorsTrust;
    }

    public void setFlavorsTrust(Map<FlavorPart, FlavorsTrustStatus> flavorsTrust) {
        this.flavorsTrust = flavorsTrust;
    }
}
