/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.esxi.rest.v2.model;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.jaxrs2.DefaultFilterCriteria;
import com.intel.mtwilson.repository.FilterCriteria;
import javax.ws.rs.QueryParam;

/**
 *
 * @author avaguayo
 */
public class EsxiClusterFilterCriteria extends DefaultFilterCriteria implements FilterCriteria<EsxiCluster> {
    
    @QueryParam("id")
    public UUID id;
    @QueryParam("connection_string")
    public String connectionString;
    @QueryParam("cluster_name")
    public String clustername;
    
}
