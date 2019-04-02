/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.model;

import com.intel.mtwilson.core.verifier.policy.RuleResult;
import java.util.List;


public class FlavorsTrustStatus {

    Boolean trust;
    List<RuleResult> rules;
    
    public FlavorsTrustStatus() {
    }

    public FlavorsTrustStatus(Boolean trust, List<RuleResult> rules) {
        this.trust = trust;
        this.rules = rules;
    }

    public Boolean getTrust() {
        return trust;
    }

    public void setTrust(Boolean trust) {
        this.trust = trust;
    }

    public List<RuleResult> getRules() {
        return rules;
    }

    public void setRules(List<RuleResult> rules) {
        this.rules = rules;
    }  
}
