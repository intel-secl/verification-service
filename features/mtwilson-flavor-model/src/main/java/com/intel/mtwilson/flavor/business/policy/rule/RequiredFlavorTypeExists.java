/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.flavor.business.policy.rule;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.intel.mtwilson.core.flavor.common.FlavorPart;
import com.intel.mtwilson.core.verifier.policy.BaseRule;
import com.intel.mtwilson.core.verifier.policy.RuleResult;
import com.intel.mtwilson.core.verifier.policy.TrustReport;
import com.intel.mtwilson.flavor.business.policy.fault.RequiredFlavorTypeMissing;
import com.intel.mtwilson.core.common.model.HostManifest;

/**
 * Rule for check if a required flavor type exists in a provided trust report
 *
 * @author dtiwari
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RequiredFlavorTypeExists extends BaseRule {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RequiredFlavorTypeExists.class);
    
    private FlavorPart requiredFlavorType;
    
    protected RequiredFlavorTypeExists() { } // for desearializing jackson

    public RequiredFlavorTypeExists(FlavorPart requiredFlavorType) {
        this.requiredFlavorType = requiredFlavorType;
    }
    
    public TrustReport apply(TrustReport trustReport) {
        RuleResult report = new RuleResult(this);
        this.setMarkers(requiredFlavorType.name());
        
        if (flavorPartIsMissing(trustReport)) {
            log.debug("Defined and required flavor part [{}] is missing", requiredFlavorType.name());
            report.fault(new RequiredFlavorTypeMissing(requiredFlavorType.name()));
        }
        
        trustReport.addResult(report);
        return trustReport;
    }
    
    public boolean flavorPartIsMissing(TrustReport trustReport){
        if (trustReport.getResultsForMarker(requiredFlavorType.name()) != null
                && !trustReport.getResultsForMarker(requiredFlavorType.name()).isEmpty()) {
            return false;
        }
        return true;
    }
    
    @Override
    public RuleResult apply(HostManifest hostManifest) {
        throw new UnsupportedOperationException("Not supported.");
    }
}
