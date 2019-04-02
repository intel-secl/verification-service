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
public class FlavorgroupLocator implements Locator<Flavorgroup> {
    @PathParam("id")
    public UUID pathId;
    @QueryParam("id")
    public UUID id;
    @QueryParam("nameEqualTo")
    public String name;
    @QueryParam("includeFlavorContent")
    public boolean includeFlavorContent;
    
    @Override
    public void copyTo(Flavorgroup item) {
        if (id != null)
            item.setId(id);
        if (pathId != null)
            item.setId(pathId);
        if (name != null)
            item.setName(name);
    }
}