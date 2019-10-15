/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.flavor.business.policy.rule;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.intel.mtwilson.core.flavor.model.SignedFlavor;
import com.intel.mtwilson.core.verifier.policy.BaseRule;
import com.intel.mtwilson.core.verifier.policy.HostTrustPolicyManager;
import com.intel.mtwilson.core.verifier.policy.Policy;
import com.intel.mtwilson.core.verifier.policy.Rule;
import com.intel.mtwilson.core.verifier.policy.RuleResult;
import com.intel.mtwilson.core.verifier.policy.TrustReport;
import com.intel.mtwilson.core.verifier.policy.vendor.VendorTrustPolicyReader;
import com.intel.mtwilson.flavor.business.policy.fault.RuleAllOfFlavorsMissing;
import com.intel.mtwilson.core.common.model.HostManifest;
import com.intel.mtwilson.flavor.rest.v2.model.SignedFlavorCollection;

/**
 *
 * @author dtiwari
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RuleAllOfFlavors extends BaseRule {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RuleAllOfFlavors.class);
    SignedFlavorCollection allOfFlavors;
    private String privacyCaCert;
    private String tagCaCert;
    private String flavorSigningCertPath;
    private String caCertPath;
    private Boolean skipFlavorSignatureVerification;
    
    protected RuleAllOfFlavors() {
    } // for desearializing jackson

    public RuleAllOfFlavors(SignedFlavorCollection allOfFlavors, String privacyCaCert, String tagCaCert, String flavorSigningCertPath, String caCertPath, Boolean skipFlavorSignatureVerification) {
        this.allOfFlavors = allOfFlavors;
        this.privacyCaCert = privacyCaCert;
        this.tagCaCert = tagCaCert;
        this.flavorSigningCertPath = flavorSigningCertPath;
        this.caCertPath = caCertPath;
        this.skipFlavorSignatureVerification = skipFlavorSignatureVerification;
    }

    // HostManifest is required if vendor details are missing in flavor e.g. [SOFTWARE, ASSET_TAG]
    private VendorTrustPolicyReader getVendorTrustPolicyReader(SignedFlavor signedFlavor, HostManifest hostManifest) {
        HostTrustPolicyManager policymanager = new HostTrustPolicyManager(signedFlavor, hostManifest, privacyCaCert, tagCaCert, flavorSigningCertPath, caCertPath, skipFlavorSignatureVerification);
        VendorTrustPolicyReader trustPolicy = policymanager.getVendorTrustPolicyReader();
        return trustPolicy;
    }

    public TrustReport addFaults(TrustReport trustReport) {
        if (allOfFlavorsEmpty()) {
            return trustReport;
        }
        for (SignedFlavor flavor : allOfFlavors.getSignedFlavors()) {
            Policy policy = getVendorTrustPolicyReader(flavor, trustReport.getHostManifest()).loadTrustRules();
            for (Rule policyrule : policy.getRules()) {
                RuleResult result = policyrule.apply(trustReport.getHostManifest());
                if (!trustReport.checkResultExists(result)) {
                    String flavorPart = flavor.getFlavor().getMeta().getDescription().getFlavorPart();
                    RuleResult rule = new RuleResult(this);
                    rule.fault(new RuleAllOfFlavorsMissing(flavorPart));
                    trustReport.addResult(rule);
                }
            }
        }
        return trustReport;
    }

    public boolean allOfFlavorsEmpty() {
        if (allOfFlavors == null || allOfFlavors.getSignedFlavors() == null || allOfFlavors.getSignedFlavors().isEmpty()) {
            return true;
        }
        return false;
    }

    public boolean checkAllOfFlavorsExist(TrustReport trustReport) {
        if (allOfFlavors == null) {
            return false;
        }
        for (SignedFlavor flavor : allOfFlavors.getSignedFlavors()) {
            Policy policy = getVendorTrustPolicyReader(flavor, trustReport.getHostManifest()).loadTrustRules();
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
