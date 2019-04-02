/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.audit.helper;


import com.intel.mtwilson.audit.data.AuditContext;
//import com.sun.jersey.spi.container.ContainerRequest;
//import com.sun.jersey.spi.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerRequestContext;
import java.util.UUID;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dsmagadx
 */
public class AuditJerseyRequestFilter implements ContainerRequestFilter{
 private static Logger log = LoggerFactory.getLogger(AuditJerseyRequestFilter.class);
     private @Context SecurityContext sc; 
    @Override
    public void filter(ContainerRequestContext request) {
        String user;
        if( sc != null && sc.getUserPrincipal() != null ) {
            user = sc.getUserPrincipal().getName();
        }
        else {
            user = "Unknown";
        }
        
        AuditContext auditContext = new AuditContext(user,System.currentTimeMillis()) ;
        
        log.debug("AuditJerseyRequestFilter request for {} {}  Start {}", new String[] 
        { request.getMethod(), request.getUriInfo().getPath(), String.valueOf(auditContext.getStartMilliseconds()) });
        
        MtWilsonThreadLocal.set(auditContext);
        
//        return request;
    }
    
}
