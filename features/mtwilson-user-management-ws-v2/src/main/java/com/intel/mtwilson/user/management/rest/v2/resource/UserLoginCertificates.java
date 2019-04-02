/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.user.management.rest.v2.resource;

import com.intel.mtwilson.user.management.rest.v2.model.UserLoginCertificate;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginCertificateCollection;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginCertificateFilterCriteria;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginCertificateLocator;
import com.intel.mtwilson.user.management.rest.v2.repository.UserLoginCertificateRepository;
import com.intel.mtwilson.jaxrs2.NoLinks;
import com.intel.mtwilson.jaxrs2.server.resource.AbstractJsonapiResource;
import com.intel.mtwilson.launcher.ws.ext.V2;

import javax.ws.rs.Path;

/**
 *
 * @author ssbangal
 */
@V2
@Path("/users/{user_id}/login-certificates")
public class UserLoginCertificates extends AbstractJsonapiResource<UserLoginCertificate, UserLoginCertificateCollection, UserLoginCertificateFilterCriteria, NoLinks<UserLoginCertificate>, UserLoginCertificateLocator> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UserLoginCertificates.class);
    private UserLoginCertificateRepository repository;
    
    public UserLoginCertificates() {
        repository = new UserLoginCertificateRepository();
    }
    
    @Override
    protected UserLoginCertificateCollection createEmptyCollection() {
        return new UserLoginCertificateCollection();
    }

    @Override
    protected UserLoginCertificateRepository getRepository() {
        return repository;
    }
        
}
