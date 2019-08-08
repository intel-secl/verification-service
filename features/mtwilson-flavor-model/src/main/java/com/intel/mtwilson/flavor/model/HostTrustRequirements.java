/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.model;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.core.flavor.common.FlavorPart;
import com.intel.mtwilson.core.flavor.model.Flavor;
import com.intel.mtwilson.core.flavor.model.SignedFlavor;
import com.intel.mtwilson.flavor.rest.v2.model.FlavorCollection;
import com.intel.mtwilson.flavor.rest.v2.model.SignedFlavorCollection;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author rksavino
 */
public class HostTrustRequirements {
    private UUID flavorgroupId;
    private FlavorMatchPolicyCollection flavorMatchPolicy;
    private SignedFlavorCollection allOfFlavors;
    private List<FlavorPart> allOfFlavorsTypes = new ArrayList<>();
    private List<FlavorPart> definedAndRequiredFlavorTypes = new ArrayList();
    
    public HostTrustRequirements() { }
    
    public HostTrustRequirements(SignedFlavorCollection allOfFlavors, List<FlavorPart> definedAndRequiredFlavorTypes) {
        this.allOfFlavors = allOfFlavors;
        this.definedAndRequiredFlavorTypes = definedAndRequiredFlavorTypes;
    }
    
    public UUID getFlavorgroupId() {
        return flavorgroupId;
    }
    
    public void setFlavorgroupId(UUID flavorgroupId) {
        this.flavorgroupId = flavorgroupId;
    }
    
    public FlavorMatchPolicyCollection getFlavorMatchPolicy() {
        return flavorMatchPolicy;
    }
    
    public void setFlavorMatchPolicy(FlavorMatchPolicyCollection flavorMatchPolicy) {
        this.flavorMatchPolicy = flavorMatchPolicy;
    }
    
    public SignedFlavorCollection getAllOfFlavors() {
        return allOfFlavors;
    }
    
    public void setAllOfFlavors(SignedFlavorCollection allOfFlavors) {
        this.allOfFlavors = allOfFlavors;
    }
    
    public void addAllOfFlavor(SignedFlavor flavor) {
        allOfFlavors.getSignedFlavors().add(flavor);
    }
    
    public void removeAllOfFlavor(SignedFlavor flavor) {
        allOfFlavors.getSignedFlavors().remove(flavor);
    }
    
    public List<FlavorPart> getDefinedAndRequiredFlavorTypes() {
        return definedAndRequiredFlavorTypes;
    }

    public void setDefinedAndRequiredFlavorTypes(List<FlavorPart> definedAndRequiredFlavorTypes) {
        this.definedAndRequiredFlavorTypes = definedAndRequiredFlavorTypes;
    }
    
    public void addDefinedAndRequiredFlavorType(FlavorPart flavorPart) {
        definedAndRequiredFlavorTypes.add(flavorPart);
    }
    
    public void removeDefinedAndRequiredFlavorType(FlavorPart flavorPart) {
        definedAndRequiredFlavorTypes.remove(flavorPart);
    }

    public List<FlavorPart> getAllOfFlavorsTypes() {
        return allOfFlavorsTypes;
    }

    public void setAllOfFlavorsTypes(List<FlavorPart> allOfFlavorsTypes) {
        this.allOfFlavorsTypes = allOfFlavorsTypes;
    }
}
