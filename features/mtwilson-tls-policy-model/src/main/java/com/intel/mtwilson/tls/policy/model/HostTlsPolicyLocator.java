/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.tls.policy.model;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.repository.Locator;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

/**
 *
 * @author ssbangal, jbuhacoff
 */
public class HostTlsPolicyLocator implements Locator<HostTlsPolicy> {
    @PathParam("id")
    public UUID pathId;
    @QueryParam("id")
    public UUID id;
    @QueryParam("nameEqualTo")
    public String name;
    
    @Override
    public void copyTo(HostTlsPolicy item) {
        if (id != null)
            item.setId(id);
        if (pathId != null)
            item.setId(pathId);
        if (name != null)
            item.setName(name);
    }
}
