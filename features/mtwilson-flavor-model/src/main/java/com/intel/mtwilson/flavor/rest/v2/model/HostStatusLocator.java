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
public class HostStatusLocator implements Locator<HostStatus> {

    @PathParam("id")
    public UUID pathId;
    @QueryParam("id")
    public UUID id;
    @QueryParam("hostId")
    public UUID hostId;
    @QueryParam("aikCertificate")
    public String aikCertificate;

    @Override
    public void copyTo(HostStatus item) {
        if (id != null)
            item.setId(id);
        if (hostId != null)
            item.setHostId(hostId);
    }
}
