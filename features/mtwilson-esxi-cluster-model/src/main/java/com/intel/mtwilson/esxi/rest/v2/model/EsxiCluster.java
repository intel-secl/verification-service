/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.esxi.rest.v2.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.dcsg.cpg.validation.Validator;
import com.intel.mtwilson.jaxrs2.Document;
import com.intel.mtwilson.core.common.validators.ConnectionStringValidator;
import java.util.HashMap;

/**
 *
 * @author avaguayo
 */
@JacksonXmlRootElement(localName="esxi_cluster")
public class EsxiCluster extends Document {
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EsxiCluster.class);
    
    private String connectionString;
    private String clusterName;
    private String tlsPolicyId;
    private final HashMap<String,Object> hosts = new HashMap();

    public HashMap<String, Object> getHosts() {
        return hosts;
    }

    @Validator(ConnectionStringValidator.class)
    public String getConnectionString() {
        return connectionString;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }
    
    public String getTlsPolicyId() {
        return tlsPolicyId;
    }

    public void setTlsPolicyId(String tlsPolicyId) {
        this.tlsPolicyId = tlsPolicyId;
    }
}
