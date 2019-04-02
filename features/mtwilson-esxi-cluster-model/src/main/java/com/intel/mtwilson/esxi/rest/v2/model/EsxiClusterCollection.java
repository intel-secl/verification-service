/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.esxi.rest.v2.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.mtwilson.jaxrs2.DocumentCollection;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author avaguayo
 */
@JacksonXmlRootElement(localName="esxi_cluster_collection")
public class EsxiClusterCollection extends DocumentCollection<EsxiCluster>{
    
    private final ArrayList<EsxiCluster> esxiclusters = new ArrayList<EsxiCluster>();
    
    @JsonSerialize(include=JsonSerialize.Inclusion.ALWAYS) // jackson 1.9
    @JsonInclude(JsonInclude.Include.ALWAYS)                // jackson 2.0
    @JacksonXmlElementWrapper(localName="esxi_clusters")
    @JacksonXmlProperty(localName="esxi_cluster")  
    
    public List<EsxiCluster> getEsxiClusters() {
        return esxiclusters;
    }
    
    @Override
    public List<EsxiCluster> getDocuments() {
        return getEsxiClusters();
    }
    
    
}
