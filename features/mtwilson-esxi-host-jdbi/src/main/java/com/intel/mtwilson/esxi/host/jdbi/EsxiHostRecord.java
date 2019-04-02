/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.esxi.host.jdbi;

import com.intel.dcsg.cpg.io.UUID;

/**
 *
 * @author avaguayo
 */
public class EsxiHostRecord {
    
    private UUID id;
    private UUID clusterId;
    private String hostname;
    
    public EsxiHostRecord() {}

    public UUID getId() {
        return id;
    }

    public UUID getClusterId() {
        return clusterId;
    }

    public String getHostname() {
        return hostname;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setClusterId(UUID clusterId) {
        this.clusterId = clusterId;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }
    
    
    
}
