/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.rest.v2.model;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.core.flavor.model.Flavor;
import com.intel.mtwilson.repository.Locator;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

/**
 *
 * @author srege
 */
public class FlavorLocator implements Locator<Flavor> {
    @PathParam("id")
    public UUID pathId;
    @QueryParam("id")
    public UUID id;
    @QueryParam("label")
    public String label;
    
    @Override
    public void copyTo(Flavor item) {
        if (id != null)
            item.getMeta().setId(id.toString());
        if (pathId != null)
            item.getMeta().setId(pathId.toString());
        if (label != null)
            item.getMeta().getDescription().setLabel(label);
    }
    
}