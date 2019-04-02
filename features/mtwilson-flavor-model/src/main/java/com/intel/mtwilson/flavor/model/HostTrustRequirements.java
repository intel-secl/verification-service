/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.model;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.core.flavor.common.FlavorPart;
import com.intel.mtwilson.core.flavor.model.Flavor;
import com.intel.mtwilson.flavor.rest.v2.model.FlavorCollection;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author rksavino
 */
public class HostTrustRequirements {
    private UUID flavorgroupId;
    private FlavorMatchPolicyCollection flavorMatchPolicy;
    private FlavorCollection allOfFlavors;
    private List<FlavorPart> definedAndRequiredFlavorTypes = new ArrayList();
    
    public HostTrustRequirements() { }
    
    public HostTrustRequirements(FlavorCollection allOfFlavors, List<FlavorPart> definedAndRequiredFlavorTypes) {
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
    
    public FlavorCollection getAllOfFlavors() {
        return allOfFlavors;
    }
    
    public void setAllOfFlavors(FlavorCollection allOfFlavors) {
        this.allOfFlavors = allOfFlavors;
    }
    
    public void addAllOfFlavor(Flavor flavor) {
        allOfFlavors.getFlavors().add(flavor);
    }
    
    public void removeAllOfFlavor(Flavor flavor) {
        allOfFlavors.getFlavors().remove(flavor);
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
}
