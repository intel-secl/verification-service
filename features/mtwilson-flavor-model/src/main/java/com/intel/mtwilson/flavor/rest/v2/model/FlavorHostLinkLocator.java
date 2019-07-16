/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.rest.v2.model;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.repository.Locator;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

/**
 *
 * @author rksavino
 */
public class FlavorHostLinkLocator implements Locator<FlavorHostLink> {
    @PathParam("id")
    public UUID pathId;
    @QueryParam("id")
    public UUID id;
    @QueryParam("flavorId")
    public UUID flavorId;
    @QueryParam("hostId")
    public UUID hostId;
    
    public FlavorHostLinkLocator(UUID id) {
        this.id = id;
    }
    
    public FlavorHostLinkLocator(UUID flavorId, UUID hostId) {
        this.flavorId = flavorId;
        this.hostId = hostId;
    }
    
    @Override
    public void copyTo(FlavorHostLink item) {
        if (id != null)
            item.setId(id);
        if (pathId != null)
            item.setId(pathId);
        if (flavorId != null)
            item.setFlavorId(flavorId);
        if (hostId != null)
            item.setHostId(hostId);
    }
    
}