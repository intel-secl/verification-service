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
 * @author rksavino
 */
@JacksonXmlRootElement(localName="flavor_host_link")
public class FlavorHostLink extends Document {
    private UUID flavorId;
    private UUID hostId;
    
    public UUID getFlavorId() {
        return flavorId;
    }
    
    public void setFlavorId(UUID flavorId) {
        this.flavorId = flavorId;
    }
    
    public UUID getHostId() {
        return hostId;
    }
    
    public void setHostId(UUID hostId) {
        this.hostId = hostId;
    }
}
