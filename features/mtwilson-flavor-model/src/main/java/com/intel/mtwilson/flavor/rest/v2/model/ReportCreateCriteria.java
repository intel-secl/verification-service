/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.rest.v2.model;

import com.intel.dcsg.cpg.io.UUID;

/**
 *
 * @author rksavino
 */

public class ReportCreateCriteria {
    private UUID hostId;
    private String hostName;
    private UUID hardwareUuid;
    private String aikCertificate;

    public UUID getHostId() {
        return hostId;
    }

    public void setHostId(UUID hostId) {
        this.hostId = hostId;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public UUID getHardwareUuid() {
        return hardwareUuid;
    }

    public void setHardwareUuid(UUID hardwareUuid) {
        this.hardwareUuid = hardwareUuid;
    }

    public String getAikCertificate() {
        return aikCertificate;
    }

    public void setAikCertificate(String aikCertificate) {
        this.aikCertificate = aikCertificate;
    }
}
