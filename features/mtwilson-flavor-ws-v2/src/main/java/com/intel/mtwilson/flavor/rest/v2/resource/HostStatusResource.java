/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.rest.v2.resource;

import com.intel.dcsg.cpg.validation.ValidationUtil;
import com.intel.mtwilson.flavor.rest.v2.model.HostStatusCollection;
import com.intel.mtwilson.flavor.rest.v2.model.HostStatusFilterCriteria;
import com.intel.mtwilson.flavor.rest.v2.model.HostStatusLocator;
import com.intel.mtwilson.flavor.rest.v2.repository.HostStatusRepository;
import com.intel.mtwilson.i18n.HostState;
import static com.intel.mtwilson.i18n.HostState.CONNECTION_FAILURE;
import static com.intel.mtwilson.i18n.HostState.CONNECTION_TIMEOUT;
import static com.intel.mtwilson.i18n.HostState.UNAUTHORIZED;
import static com.intel.mtwilson.i18n.HostState.UNKNOWN;
import com.intel.mtwilson.jaxrs2.mediatype.DataMediaType;
import com.intel.mtwilson.launcher.ws.ext.V2;
import java.io.IOException;
import java.net.ConnectException;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.Path;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.shiro.authz.annotation.RequiresPermissions;

/**
 *
 * @author hmgowda
 */
@V2
@Path("/host-status")
public class HostStatusResource {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HostStatusResource.class);
    private HostStatusRepository repository;
    
    public HostStatusResource() {
        repository = new HostStatusRepository();
    }
    
    protected HostStatusRepository getRepository() {
        return repository;
    }
    
    @GET
    @Path("/{id}")
    @RequiresPermissions("host_status:retrieve")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, DataMediaType.APPLICATION_YAML, DataMediaType.TEXT_YAML})
    public com.intel.mtwilson.flavor.rest.v2.model.HostStatus retrieve(@BeanParam HostStatusLocator locator) {
        ValidationUtil.validate(locator);
        return getRepository().retrieve(locator);
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, DataMediaType.APPLICATION_YAML, DataMediaType.TEXT_YAML})
    @RequiresPermissions("host_status:search")
    public HostStatusCollection search(@BeanParam HostStatusFilterCriteria filterCriteria) {
        ValidationUtil.validate(filterCriteria);
        return getRepository().search(filterCriteria);
    }
    
    public HostState determineHostState(Exception e) {
        if (e instanceof ProcessingException) {
            log.warn("Failed to generate host manifest due to processing exception. {} - {}", e.getCause().toString(), e.getCause().getMessage());
            if (e.getCause() != null && e.getCause() instanceof ConnectException) {
                log.warn("Failed connection to host, host has CONNECTION_FAILURE state with error message: {}", e.getCause().getMessage());
                return CONNECTION_FAILURE;
            } else if (e.getCause() != null && e.getCause() instanceof javax.net.ssl.SSLHandshakeException) {
                log.warn("Failed connection to host, host has CONNECTION_FAILURE state with error message: {}", e.getCause().getMessage());
                return CONNECTION_FAILURE;
            } else if (e.getCause() != null && e.getCause() instanceof java.net.NoRouteToHostException) {
                log.warn("Failed connection to host, host has CONNECTION_FAILURE state with error message: {}", e.getCause().getMessage());
                return CONNECTION_FAILURE;
            } else if (e.getCause() != null && e.getCause() instanceof java.net.SocketTimeoutException){
                log.warn("Failed connection to host, host has CONNECTION_TIMEOUT state with error message: {}", e.getCause().getMessage());
                return CONNECTION_TIMEOUT;
            }
            else {
                log.warn("Failed connection to host, host has UNKNOWN state with error message: {}", e.getMessage());
                return UNKNOWN;
            }
        } else if (e instanceof NotAuthorizedException) {
            log.warn("Failed to generate host manifest, host has UNAUTHORIZED state with error message: {}", e.getMessage());
            return UNAUTHORIZED;
        } else if (e instanceof InternalServerErrorException) {
            String errorResponse = ((InternalServerErrorException) e).getResponse().readEntity(String.class);
            HostState hostState = HostState.getHostState(errorResponse);
            log.warn("Failed to generate host manifest, host has {} state with error message: {}", hostState.name(), errorResponse);
            return hostState;
        } else if (e instanceof IOException) {
            log.warn("Failed connection to host because of IOException. Error message: {} - {}", e.getCause().toString(), e.getCause().getMessage());
            if (e.getCause() != null && e.getCause() instanceof com.intel.mtwilson.core.host.connector.vmware.VMwareConnectionException)
                return CONNECTION_FAILURE;
            else
                return UNKNOWN;
         } else {
            log.warn("Failed to generate host manifest, host has UNKNOWN state with error message: {}", e.getMessage(), e);
            return UNKNOWN;
        }
    }
}
