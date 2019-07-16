/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.rest.v2.model;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.flavor.model.HostStatusInformation;
import com.intel.mtwilson.jaxrs2.Document;
import com.intel.mtwilson.core.common.model.HostManifest;
import java.util.Date;

/**
 *
 * @author hmgowda
 */
public class HostStatus extends Document{
    private UUID hostId;
    private HostStatusInformation status;
    private Date created;
    private HostManifest hostManifest;

    public UUID getHostId() {
        return hostId;
    }

    public void setHostId(UUID hostId) {
        this.hostId = hostId;
    }

    public HostStatusInformation getStatus() {
        return status;
    }

    public void setStatus(HostStatusInformation status) {
        this.status = status;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public HostManifest getHostManifest() {
        return hostManifest;
    }

    public void setHostManifest(HostManifest hostManifest) {
        this.hostManifest = hostManifest;
    }
    
    
}
