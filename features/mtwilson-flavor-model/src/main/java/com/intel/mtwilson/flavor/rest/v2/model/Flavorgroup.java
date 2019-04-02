/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.rest.v2.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.core.flavor.model.Flavor;
import com.intel.mtwilson.flavor.model.FlavorMatchPolicyCollection;
import com.intel.mtwilson.jaxrs2.Document;
import java.util.List;

/**
 *
 * @author rksavino
 */
@JacksonXmlRootElement(localName="flavorgroup")
public class Flavorgroup extends Document {
    private String name;
    private FlavorMatchPolicyCollection flavorMatchPolicyCollection;
    private List<UUID> flavorIds;
    private List<Flavor> flavors;
    
    public List<Flavor> getFlavors() {
        return flavors;
    }

    public void setFlavors(List<Flavor> flavors) {
        this.flavors = flavors;
    }

    public List<UUID> getFlavorIds() {
        return flavorIds;
    }

    public void setFlavorIds(List<UUID> flavorIds) {
        this.flavorIds = flavorIds;
    }

    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public FlavorMatchPolicyCollection getFlavorMatchPolicyCollection() {
        return flavorMatchPolicyCollection;
    }
    
    public void setFlavorMatchPolicyCollection(FlavorMatchPolicyCollection flavorMatchPolicyCollection) {
        this.flavorMatchPolicyCollection = flavorMatchPolicyCollection;
    }
}
