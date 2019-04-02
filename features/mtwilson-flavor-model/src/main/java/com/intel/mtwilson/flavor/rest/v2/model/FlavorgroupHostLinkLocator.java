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
 * @author hmgowda
 */
public class FlavorgroupHostLinkLocator implements Locator<FlavorgroupHostLink> {
    @PathParam("id")
    public UUID pathId;
    @QueryParam("id")
    public UUID id;
    @QueryParam("flavorgroupId")
    public UUID flavorgroupId;
    @QueryParam("hostId")
    public UUID hostId;
    
    public FlavorgroupHostLinkLocator() { }
    
    public FlavorgroupHostLinkLocator(UUID id) {
        this.id = id;
    }
    
    public FlavorgroupHostLinkLocator(UUID flavorgroupId, UUID hostId) {
        this.flavorgroupId = flavorgroupId;
        this.hostId = hostId;
    }
    
    @Override
    public void copyTo(FlavorgroupHostLink item) {
        if (id != null)
            item.setId(id);
        if (pathId != null)
            item.setId(pathId);
        if (flavorgroupId != null)
            item.setFlavorgroupId(flavorgroupId);
        if (hostId != null)
            item.setHostId(hostId);
    }
}
