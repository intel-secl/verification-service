/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.user.management.rest.v2.resource;

import com.intel.dcsg.cpg.validation.ValidationUtil;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginPassword;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginPasswordCollection;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginPasswordFilterCriteria;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginPasswordLocator;
import com.intel.mtwilson.user.management.rest.v2.repository.UserLoginPasswordRepository;
import com.intel.mtwilson.jaxrs2.NoLinks;
import com.intel.mtwilson.jaxrs2.server.resource.AbstractJsonapiResource;
import com.intel.mtwilson.launcher.ws.ext.V2;
import com.intel.mtwilson.user.management.rest.v2.model.RoleCollection;
import com.intel.mtwilson.user.management.rest.v2.model.RoleFilterCriteria;
import com.intel.mtwilson.user.management.rest.v2.repository.RoleRepository;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.BeanParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.apache.shiro.authz.annotation.RequiresPermissions;

/**
 *
 * @author ssbangal
 */
@V2
@Path("/users/{user_id}/login-passwords")
public class UserLoginPasswords extends AbstractJsonapiResource<UserLoginPassword, UserLoginPasswordCollection, UserLoginPasswordFilterCriteria, NoLinks<UserLoginPassword>, UserLoginPasswordLocator> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UserLoginPasswords.class);
    private UserLoginPasswordRepository repository;
    
    public UserLoginPasswords() {
        repository = new UserLoginPasswordRepository();
    }
    
    @Override
    protected UserLoginPasswordCollection createEmptyCollection() {
        return new UserLoginPasswordCollection();
    }

    @Override
    protected UserLoginPasswordRepository getRepository() {
        return repository;
    }
    
    @Override
    @Path("/{id}")
    @PUT
    @RequiresPermissions({"user_login_passwords:store", "user_login_password_roles:create"})
    public UserLoginPassword storeOne(@BeanParam UserLoginPasswordLocator locator, UserLoginPassword item) {
        ValidationUtil.validate(item);
        locator.copyTo(item);
        UserLoginPassword existing = getRepository().retrieve(locator); // subclass is responsible for validating id
        if (existing == null) {
            throw new WebApplicationException("User login password does not exist", Response.Status.BAD_REQUEST);
        }
        List<String> roles = item.getRoles();
        for (String role : roles) {

            RoleFilterCriteria filterCriteria = new RoleFilterCriteria();
            filterCriteria.nameEqualTo = role;
            RoleCollection roleCollection = new RoleRepository().search(filterCriteria);

            if (roleCollection == null || roleCollection.getRoles().isEmpty()) {
                throw new WebApplicationException("One or more specified roles does not exist.", Response.Status.BAD_REQUEST);
            }
        }
        getRepository().store(item);
        return item;
    }
}
