/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.rest.v2.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.jaxrs2.Document;

/**
 *
 * @author hmgowda
 */
@JacksonXmlRootElement(localName="flavor_flavorgroup_link")
public class FlavorFlavorgroupLink extends Document{
    private UUID flavorgroupId;
    private UUID flavorId;
    
    public UUID getFlavorgroupId() {
        return flavorgroupId;
    }
    
    public void setFlavorgroupId(UUID flavorgroupId) {
        this.flavorgroupId = flavorgroupId;
    }
    
    public UUID getFlavorId() {
        return flavorId;
    }
    
    public void setFlavorId(UUID flavorId) {
        this.flavorId = flavorId;
    }
}
