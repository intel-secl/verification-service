/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.esxi.rest.v2.model;

import com.intel.dcsg.cpg.io.UUID;
import javax.ws.rs.PathParam;
import com.intel.mtwilson.repository.Locator;

/**
 *
 * @author avaguayo
 */
public class EsxiClusterLocator implements Locator<EsxiCluster>{
    
    @PathParam("id")
    public UUID id;
    
    @Override
    public void copyTo(EsxiCluster cluster) {
        if( id != null ) {
            cluster.setId(id);
        }
    }
}
