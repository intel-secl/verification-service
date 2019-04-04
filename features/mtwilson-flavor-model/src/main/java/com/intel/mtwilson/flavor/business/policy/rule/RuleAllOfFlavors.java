/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.flavor.business.policy.rule;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.intel.mtwilson.core.flavor.model.Flavor;
import com.intel.mtwilson.core.verifier.policy.BaseRule;
import com.intel.mtwilson.core.verifier.policy.HostTrustPolicyManager;
import com.intel.mtwilson.core.verifier.policy.Policy;
import com.intel.mtwilson.core.verifier.policy.Rule;
import com.intel.mtwilson.core.verifier.policy.RuleResult;
import com.intel.mtwilson.core.verifier.policy.TrustReport;
import com.intel.mtwilson.core.verifier.policy.vendor.VendorTrustPolicyReader;
import com.intel.mtwilson.flavor.business.policy.fault.RuleAllOfFlavorsMissing;
import com.intel.mtwilson.flavor.rest.v2.model.FlavorCollection;
import com.intel.mtwilson.core.common.model.HostManifest;

/**
 *
 * @author dtiwari
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RuleAllOfFlavors extends BaseRule {

    FlavorCollection allOfFlavors;
    private String privacyCaCert;
    private String tagCaCert;
    
    protected RuleAllOfFlavors() {
    } // for desearializing jackson

    public RuleAllOfFlavors(FlavorCollection allOfFlavors, String privacyCaCert, String tagCaCert) {
        this.allOfFlavors = allOfFlavors;
        this.privacyCaCert = privacyCaCert;
        this.tagCaCert = tagCaCert;
    }

    private VendorTrustPolicyReader getVendorTrustPolicyReader(Flavor flavor) {
        HostTrustPolicyManager policymanager = new HostTrustPolicyManager(flavor, null, privacyCaCert, tagCaCert);
        VendorTrustPolicyReader trustPolicy = policymanager.getVendorTrustPolicyReader();
        return trustPolicy;
    }

    public TrustReport addFaults(TrustReport trustReport) {
        if (allOfFlavorsEmpty()) {
            return trustReport;
        }
        for (Flavor flavor : allOfFlavors.getFlavors()) {
            Policy policy = getVendorTrustPolicyReader(flavor).loadTrustRules();
            for (Rule policyrule : policy.getRules()) {
                RuleResult result = policyrule.apply(trustReport.getHostManifest());
                if (!trustReport.checkResultExists(result)) {
                    String flavorPart = flavor.getMeta().getDescription().getFlavorPart();
                    RuleResult rule = new RuleResult(this);
                    rule.fault(new RuleAllOfFlavorsMissing(flavorPart));
                    trustReport.addResult(rule);
                }
            }
        }
        return trustReport;
    }

    public boolean allOfFlavorsEmpty() {
        if (allOfFlavors == null || allOfFlavors.getFlavors() == null || allOfFlavors.getFlavors().isEmpty()) {
            return true;
        }
        return false;
    }
    
    public boolean checkAllOfFlavorsExist(TrustReport trustReport) {
        if (allOfFlavors == null) {
            return false;
        }
        for (Flavor flavor : allOfFlavors.getFlavors()) {
            Policy policy = getVendorTrustPolicyReader(flavor).loadTrustRules();
            for (Rule policyrule : policy.getRules()) {
                RuleResult result = policyrule.apply(trustReport.getHostManifest());
                if (!trustReport.checkResultExists(result)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public RuleResult apply(HostManifest hostManifest) {
        RuleResult report = new RuleResult(this);
        return report;
    }
}
