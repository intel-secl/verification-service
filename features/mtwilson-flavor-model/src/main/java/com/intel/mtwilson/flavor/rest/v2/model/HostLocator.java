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

public class HostLocator implements Locator<Host> {
    @PathParam("id")
    public UUID pathId;
    @QueryParam("id")
    public UUID id;
    @QueryParam("nameEqualTo")
    public String name;
    @QueryParam("hardwareUuid")
    public UUID hardwareUuid;
    @QueryParam("aikCertificate")
    public String aikCertificate;
    
    @Override
    public void copyTo(Host item) {
        if (id != null)
            item.setId(id);
        if (pathId != null)
            item.setId(pathId);
        if (name != null)
            item.setHostName(name);
        if (hardwareUuid != null)
            item.setHardwareUuid(hardwareUuid);
    }
}
