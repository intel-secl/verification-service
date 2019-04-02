/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.model;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.core.flavor.model.Flavor;
import com.intel.mtwilson.core.verifier.policy.TrustReport;
import com.intel.mtwilson.flavor.rest.v2.model.FlavorCollection;

/**
 *
 * @author rksavino
 */
public class HostTrustCache {
    private UUID hostId;
    private FlavorCollection trustedFlavors = new FlavorCollection();
    private TrustReport trustReport;
    
    public HostTrustCache() { }
    
    public UUID getHostId() {
        return hostId;
    }

    public void setHostId(UUID hostId) {
        this.hostId = hostId;
    }
    
    public FlavorCollection getTrustedFlavors() {
        return trustedFlavors;
    }

    public void setTrustedFlavors(FlavorCollection trustedFlavors) {
        this.trustedFlavors = trustedFlavors;
    }
    
    public void addTrustedFlavor(Flavor flavor) {
        trustedFlavors.getFlavors().add(flavor);
    }
    
    public void removeTrustedFlavor(Flavor flavor) {
        trustedFlavors.getFlavors().remove(flavor);
    }
    
    public TrustReport getTrustReport() {
        return trustReport;
    }

    public void setTrustReport(TrustReport trustReport) {
        this.trustReport = trustReport;
    }
}
