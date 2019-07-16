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
public class FlavorFlavorgroupLinkLocator implements Locator<FlavorFlavorgroupLink> {
    @PathParam("id")
    public UUID pathId;
    @QueryParam("id")
    public UUID id;
    @QueryParam("flavorId")
    public UUID flavorId;
    @QueryParam("flavorgroupId")
    public UUID flavorgroupId;
    
    @Override
    public void copyTo(FlavorFlavorgroupLink item) {
        if (id != null)
            item.setId(id);
        if (pathId != null)
            item.setId(pathId);
        if (flavorId != null)
            item.setFlavorId(flavorId);
        if (flavorgroupId != null)
            item.setFlavorgroupId(flavorgroupId);
    }
}
