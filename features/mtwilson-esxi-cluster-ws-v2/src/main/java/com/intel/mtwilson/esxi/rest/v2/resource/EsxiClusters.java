/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.esxi.rest.v2.resource;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.validation.ValidationUtil;
import com.intel.mtwilson.esxi.rest.v2.model.EsxiCluster;
import com.intel.mtwilson.esxi.rest.v2.model.EsxiClusterCollection;
import com.intel.mtwilson.esxi.rest.v2.model.EsxiClusterFilterCriteria;
import com.intel.mtwilson.esxi.rest.v2.model.EsxiClusterLocator;
import com.intel.mtwilson.esxi.rest.v2.repository.EsxiClusterRepository;
import com.intel.mtwilson.jaxrs2.NoLinks;
import com.intel.mtwilson.jaxrs2.server.resource.AbstractJsonapiResource;
import com.intel.mtwilson.launcher.ws.ext.V2;
import javax.ws.rs.Path;
import javax.ws.rs.POST;
import javax.ws.rs.Consumes;
import com.intel.mtwilson.jaxrs2.mediatype.DataMediaType;
import com.intel.mtwilson.tls.policy.filter.HostTlsPolicyFilter;
import javax.ws.rs.Produces;


/**
 *
 * @author avaguayo
 */
@V2
@Path("/esxi-cluster")
public class EsxiClusters extends AbstractJsonapiResource<EsxiCluster, EsxiClusterCollection, EsxiClusterFilterCriteria, NoLinks<EsxiCluster>, EsxiClusterLocator>{
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EsxiClusters.class);
    private EsxiClusterRepository repository;
    
    
    public EsxiClusters() {
        repository = new EsxiClusterRepository();
    }
    
    @Override
    protected EsxiClusterCollection createEmptyCollection() {
        return new EsxiClusterCollection();
    }

    @Override
    protected EsxiClusterRepository getRepository() {
        return repository;
    }
    
    // We override this method because we need to skip the validation part as the Connection String has backslashes such as:
    // connection string format: vmware:https://192.168.0.1:443/sdk;u=admin;p=password
    
    @POST
    @Consumes({DataMediaType.APPLICATION_VND_API_JSON})
    @Produces({DataMediaType.APPLICATION_VND_API_JSON})
    @Override
    public EsxiClusterCollection createJsonapiCollection(EsxiClusterCollection collection) {
        log.debug("createCollection - modified");
        ValidationUtil.validate(collection);
        // this behavior of autmoatically generating uuids if client didn't provide could be implemented in one place and reused in all create() methods...  the utility could accept a DocumentCollection and set the ids... 
        for (EsxiCluster item : collection.getDocuments()) {
            if (item.getClusterName() == null) {
                log.debug("Missing cluster name");
                item.setClusterName("MISSING cluster name");
                continue;
            }
            if (item.getConnectionString() == null) {
                log.debug("MISSING connection string for cluster {}", item.getClusterName());
                item.setConnectionString("MISSING connection string");
                continue;
            }
            if (item.getId() == null) {
                item.setId(new UUID());
            }            
            // set the tls policy id to default if it is not specified
            if (item.getTlsPolicyId() == null || item.getTlsPolicyId().isEmpty()) {            
                 item.setTlsPolicyId(HostTlsPolicyFilter.getDefaultTlsPolicyType());
            }            
            log.debug("Creating repository for cluster {}", item.getClusterName());
            getRepository().create(item);
        }
        return collection;
    }
    
    
    
}
