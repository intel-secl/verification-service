/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.user.management.rest.v2.repository;

import com.intel.mtwilson.user.management.rest.v2.model.UserLoginHmacRole;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginHmacRoleCollection;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginHmacRoleFilterCriteria;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginHmacRoleLocator;
import com.intel.mtwilson.jaxrs2.server.resource.DocumentRepository;
import org.apache.shiro.authz.annotation.RequiresPermissions;

/**
 *
 * @author ssbangal
 */
public class UserLoginHmacRoleRepository implements DocumentRepository<UserLoginHmacRole, UserLoginHmacRoleCollection, UserLoginHmacRoleFilterCriteria, UserLoginHmacRoleLocator> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UserLoginHmacRoleRepository.class);
    
    @Override
    @RequiresPermissions("user_login_certificate_roles:search")        
    public UserLoginHmacRoleCollection search(UserLoginHmacRoleFilterCriteria criteria) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Unlike other tables, for this table the primary key is a combination of the loginHmacId and roleId. So, we cannot
     * use the locator object. Hence, store, retrieve and delete will not be supported. But users can get the list using search and
     * also delete by specifying the search criteria.
     * @param locator
     * @return 
     */
    @Override
    @RequiresPermissions("user_login_certificate_roles:retrieve")        
    public UserLoginHmacRole retrieve(UserLoginHmacRoleLocator locator) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.        
    }

    /**
     * Unlike other tables, for this table the primary key is a combination of the loginHmacId and roleId. So, we cannot
     * use the locator object. Hence, store, retrieve and delete will not be supported. But users can get the list using search and
     * also delete by specifying the search criteria.
     * @param item 
     */
    @Override
    @RequiresPermissions("user_login_certificate_roles:store")        
    public void store(UserLoginHmacRole item) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.        
    }

    @Override
    @RequiresPermissions("user_login_certificate_roles:create")        
    public void create(UserLoginHmacRole item) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    @RequiresPermissions("user_login_certificate_roles:delete")        
    public void delete(UserLoginHmacRoleLocator locator) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.        
    }
    
    @Override
    @RequiresPermissions("user_login_certificate_roles:delete,search")        
    public void delete(UserLoginHmacRoleFilterCriteria criteria) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
