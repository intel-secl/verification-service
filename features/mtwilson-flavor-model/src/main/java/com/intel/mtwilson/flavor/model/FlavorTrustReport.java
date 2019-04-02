/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.model;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.core.flavor.common.FlavorPart;
import com.intel.mtwilson.core.verifier.policy.TrustReport;

/**
 *
 * @author rksavino
 */
public class FlavorTrustReport {
    private FlavorPart flavorPart;
    private UUID flavorId;
    private TrustReport trustReport;
    
    public FlavorTrustReport() { }
    
    public FlavorTrustReport(FlavorPart flavorPart, UUID flavorId, TrustReport trustReport) {
        this.flavorPart = flavorPart;
        this.flavorId = flavorId;
        this.trustReport = trustReport;
    }
    
    public FlavorPart getFlavorPart() {
        return flavorPart;
    }
    
    public void setFlavorPart(FlavorPart flavorPart) {
        this.flavorPart = flavorPart;
    }
    
    public UUID getFlavorId() {
        return flavorId;
    }
    
    public void setFlavorId(UUID flavorId) {
        this.flavorId = flavorId;
    }
    
    public TrustReport getTrustReport() {
        return trustReport;
    }
    
    public void setTrustReport(TrustReport trustReport) {
        this.trustReport = trustReport;
    }
}
