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
@JacksonXmlRootElement(localName="flavorgroup_host_link")
public class FlavorgroupHostLink extends Document{
    private UUID flavorgroupId;
    private UUID hostId;

    public UUID getHostId() {
        return hostId;
    }

    public void setHostId(UUID hostId) {
        this.hostId = hostId;
    }

    public UUID getFlavorgroupId() {
        return flavorgroupId;
    }

    public void setFlavorgroupId(UUID flavorgroupId) {
        this.flavorgroupId = flavorgroupId;
    }
    
    
}
