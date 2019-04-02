/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.rest.v2.resource;

import com.intel.mtwilson.tls.policy.model.HostTlsPolicyCollection;
import com.intel.mtwilson.tls.policy.model.HostTlsPolicyFilterCriteria;
import com.intel.mtwilson.tls.policy.model.HostTlsPolicyLocator;
import com.intel.mtwilson.launcher.ws.ext.V2;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.validation.ValidationUtil;
import com.intel.mtwilson.flavor.rest.v2.model.HostCollection;
import com.intel.mtwilson.flavor.rest.v2.model.HostFilterCriteria;
import com.intel.mtwilson.flavor.rest.v2.repository.HostRepository;
import com.intel.mtwilson.flavor.rest.v2.repository.HostTlsPolicyRepository;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import com.intel.mtwilson.jaxrs2.mediatype.DataMediaType;
import com.intel.mtwilson.tls.policy.model.HostTlsPolicy;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.BeanParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.shiro.authz.annotation.RequiresPermissions;



/**
 * Example registration of new certificate policy with one certificate:
 * 
 * <pre>
 * {"name":"new policy",
 * "private":false,
 * "comment":"for example",
 * "descriptor":{"policy_type":"certificate",
 * "meta":{"encoding":"base64"},
 * "data":["MIIBwzCCASygAwIBAgIJANE6wc0/mOjZMA0GCSqGSIb3DQEBCwUAMBExDz
 * ANBgNVBAMTBnRlc3RjYTAeFw0xNDA2MjQyMDQ1MjdaFw0xNDA3MjQyMDQ1MjdaMBExD
 * zANBgNVBAMTBnRlc3RjYTCBnzANBgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEAt9EmIilK
 * 3qSRGMRxEtcGj42dsJUf5h2OZIG25Er7dDxJbdw6KrOQhVUUx+2DUOQLMsr3sJt9D5e
 * yWC4+vhoiNRMUjamR52/hjIBosr2XTfWKdKG8NsuDzwljHkB/6uv3P+AfQQ/eStXc42
 * cv8J6vZXeQF6QMf63roW8i6SNYHwMCAwEAAaMjMCEwDgYDVR0PAQH/BAQDAgIEMA8GA
 * 1UdEwEB/wQFMAMBAf8wDQYJKoZIhvcNAQELBQADgYEAXov/vFVOMAznD+BT8tBfAT1R
 * /nWFmrFB7os4Ry1mYjbr0lrW2vtUzA2XFx6nUzafYdyL1L4PnI7LGYqRqicT6WzGb1g
 * rNTJUJhrI7FkGg6TXQ4QSf6EmcEwsTlGHk9rxp9YySJt/xrhboP33abdXMHUWOXnJEH
 * u4la8tnuzwSvM="]}}
 * </pre>
 * 
 * @author ssbangal
 */
@V2
@Path("/tls-policies")
public class HostTlsPolicyResource {

    private HostTlsPolicyRepository repository;
    
     private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HostTlsPolicyResource.class);
    
    public HostTlsPolicyResource() {
        repository = new HostTlsPolicyRepository();
    }
    
    protected HostTlsPolicyRepository getRepository() { return repository; }

    
    protected HostTlsPolicyCollection createEmptyCollection() {
        return new HostTlsPolicyCollection();
    }
    
    
    
    
    @GET
    @Produces(DataMediaType.APPLICATION_VND_API_JSON)
    @RequiresPermissions("host_tls_policies:search")
    public HostTlsPolicyCollection searchCollection(@BeanParam HostTlsPolicyFilterCriteria criteria) {
        log.debug("searchJsonapiCollection");
        ValidationUtil.validate(criteria); 
        return repository.search(criteria);
    }

    /**
     * Add an item to the collection. Input Content-Type is
     * application/vnd.api+json Output Content-Type is application/vnd.api+json
     *
     * The input must represent a collection of items to add, even if the
     * collection only contains a single item.
     *
     *
     * @param hosts
     * @return
     */
    
    @POST
    @Consumes({DataMediaType.APPLICATION_VND_API_JSON})
    @Produces({DataMediaType.APPLICATION_VND_API_JSON})
    @RequiresPermissions("host_tls_policies:create")
    public HostTlsPolicyCollection createCollection(HostTlsPolicyCollection collection) {
        log.debug("createCollection");
        ValidationUtil.validate(collection);
        // this behavior of autmoatically generating uuids if client didn't provide could be implemented in one place and reused in all create() methods...  the utility could accept a DocumentCollection and set the ids... 
        for (HostTlsPolicy item : collection.getDocuments()) {
            if (item.getId() == null) {
                item.setId(new UUID());
            }
            repository.create(item); 
        }
        return collection;
    }

    /**
     * Retrieve an item from the collection. Input Content-Type is not
     * applicable. Output Content-Type is application/vnd.api+json
     *
     * The output item is always wrapped in a collection.
     *
     * @param id
     * @return
     */
    
    @Path("/{id}")
    @GET
    @Produces({DataMediaType.APPLICATION_VND_API_JSON})
    @RequiresPermissions("host_tls_policies:retrieve")
    public HostTlsPolicyCollection retrieveJsonapiCollection(@BeanParam HostTlsPolicyLocator locator) { // misnomer, what we really mean is "retrieve one but wrapped ina  collection for jsonapi"
        log.debug("retrieveCollection");
        HostTlsPolicy item = getRepository().retrieve(locator); // subclass is responsible for validating id
        if (item == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND); 
        }
        HostTlsPolicyCollection collection = createEmptyCollection();
        collection.getDocuments().add(item);
        return collection;
    }

    /**
     * Replace an item in the collection. Input Content-Type is
     * application/vnd.api+json Output Content-Type is application/vnd.api+json
     *
     * The input item must be wrapped in a collection. The output item is always
     * wrapped in a collection.
     *
     * @param id
     * @param hostCollection
     * @return
     */
    
    @Path("/{id}")
    @PUT
    @Consumes(DataMediaType.APPLICATION_VND_API_JSON)
    @Produces(DataMediaType.APPLICATION_VND_API_JSON)
    @RequiresPermissions("host_tls_policies:store")
    public HostTlsPolicyCollection storeJsonapiCollection(@BeanParam HostTlsPolicyLocator locator, HostTlsPolicyCollection collection) {// misnomer, what we really mean is "store one but wrapped ina  collection for jsonapi"
        log.debug("storeCollection");
        ValidationUtil.validate(collection);
        List<HostTlsPolicy> list = collection.getDocuments();
        if (list == null || list.isEmpty()) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST); 
        }
        HostTlsPolicy item = list.get(0);
        locator.copyTo(item);
        if (item == null) {
            repository.create(item);
        } else {
            repository.store(item);
        }
        return collection;
    }

    
    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, DataMediaType.APPLICATION_YAML, DataMediaType.TEXT_YAML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, DataMediaType.APPLICATION_YAML, DataMediaType.TEXT_YAML})
    @RequiresPermissions("host_tls_policies:create")
    public HostTlsPolicy createOne(@BeanParam HostTlsPolicyLocator locator, HostTlsPolicy item, @Context HttpServletRequest httpServletRequest, @Context HttpServletResponse httpServletResponse) {
        
        locator.copyTo(item);
        ValidationUtil.validate(item); 
        if (item.getId() == null) {
            item.setId(new UUID());
        }
        getRepository().create(item);
        httpServletResponse.setStatus(Response.Status.CREATED.getStatusCode());
        return item;
    }
    
    @Path("/{id}")
    @DELETE
    @RequiresPermissions("host_tls_policies:delete")
    public void deleteOne(@BeanParam HostTlsPolicyLocator locator, @Context HttpServletRequest httpServletRequest, @Context HttpServletResponse httpServletResponse) {
        HostTlsPolicy item = getRepository().retrieve(locator);
        if (item == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        HostFilterCriteria hostFilterCriteria = new HostFilterCriteria();
        hostFilterCriteria.tlsPolicyId = item.getId().toString();
        HostCollection hostCollection = new HostRepository().search(hostFilterCriteria);
        
        if (hostCollection != null && hostCollection.getHosts() != null && !hostCollection.getHosts().isEmpty()){
            throw new WebApplicationException("Cannot delete a TLS policy that is currently associated with a host", Response.Status.BAD_REQUEST); 
        }
        getRepository().delete(locator);
        httpServletResponse.setStatus(Response.Status.NO_CONTENT.getStatusCode());
    }
    
      
    @Path("/{id}")
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, DataMediaType.APPLICATION_YAML, DataMediaType.TEXT_YAML})
    @RequiresPermissions("host_tls_policies:retrieve")
    public HostTlsPolicy retrieveOne(@BeanParam HostTlsPolicyLocator locator, @Context HttpServletRequest httpServletRequest, @Context HttpServletResponse httpServletResponse) {
        
        HostTlsPolicy existing = getRepository().retrieve(locator); // subclass is responsible for validating the id in whatever manner it needs to;  most will return null if !UUID.isValid(id)  but we don't do it here because a resource might want to allow using something other than uuid as the url key, for example uuid OR hostname for hosts
        if (existing == null) {
            httpServletResponse.setStatus(Response.Status.NOT_FOUND.getStatusCode());
            return null;
//            throw new WebApplicationException(Response.Status.NOT_FOUND); 
        }
        return existing;
    }
    
    
    @Path("/{id}")
    @PUT
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, DataMediaType.APPLICATION_YAML, DataMediaType.TEXT_YAML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, DataMediaType.APPLICATION_YAML, DataMediaType.TEXT_YAML})
    @RequiresPermissions("host_tls_policies:store")
    public HostTlsPolicy storeOne(@BeanParam HostTlsPolicyLocator locator, HostTlsPolicy item) {
        
        ValidationUtil.validate(item);
        locator.copyTo(item);
        HostTlsPolicy existing = getRepository().retrieve(locator); 
        if (existing == null) {
            getRepository().create(item);
        } else {
            getRepository().store(item);
        }

        return item;
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, DataMediaType.APPLICATION_YAML, DataMediaType.TEXT_YAML})
    @RequiresPermissions("host_tls_policies:search")
    public HostTlsPolicyCollection searchJsonapiCollection(@BeanParam HostTlsPolicyFilterCriteria criteria) {
        log.debug("searchJsonapiCollection");
        ValidationUtil.validate(criteria); // throw new MWException(e, ErrorCode.AS_INPUT_VALIDATION_ERROR, input, method.getName());
        return getRepository().search(criteria);
    }
      
}
