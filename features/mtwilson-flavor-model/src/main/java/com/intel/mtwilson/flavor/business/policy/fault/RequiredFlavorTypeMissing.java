/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.flavor.business.policy.fault;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import com.intel.mtwilson.core.verifier.policy.Fault;

/**
 * Fault if the Defined and Required Flavor Types is missing in TrustReport
 * 
 * @author dtiwari
 */

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RequiredFlavorTypeMissing extends Fault {
    
    public RequiredFlavorTypeMissing() { }
    
    public RequiredFlavorTypeMissing(String flavorPart) {
        super("Required flavor type missing: %s", flavorPart);
    }
}
