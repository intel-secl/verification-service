/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.flavor.business.policy.fault;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import com.intel.mtwilson.core.verifier.policy.Fault;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RuleAllOfFlavorsMissing extends Fault {

    public RuleAllOfFlavorsMissing () {}
    public RuleAllOfFlavorsMissing(String flavorPart) {
        super("All of Flavor Types Missing :  %s", flavorPart);
    }
}
