/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.user.management.client.jaxrs;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.My;
import com.intel.mtwilson.user.management.rest.v2.model.RolePermission;
import com.intel.mtwilson.user.management.rest.v2.model.RolePermissionCollection;
import com.intel.mtwilson.user.management.rest.v2.model.RolePermissionFilterCriteria;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author ssbangal
 */
public class RolePermissionTest {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RolePermissionTest.class);

    private static RolePermissions client = null;
    
    @BeforeClass
    public static void init() throws Exception {
        client = new RolePermissions(My.configuration().getClientProperties());
    }
    
    @Test
    public void testRolePermission() {
        
        UUID roleId = new UUID();
        
        RolePermission createRolePermission = new RolePermission();
        createRolePermission.setRoleId(roleId);
        createRolePermission.setPermitDomain("rew_resource");
        createRolePermission.setPermitAction("import");
        client.create(createRolePermission);
                
        RolePermissionFilterCriteria criteria = new RolePermissionFilterCriteria();
        criteria.roleId = roleId;
        criteria.filter = false;
        RolePermissionCollection rolePerms = client.search(criteria);
        for(RolePermission rolePerm : rolePerms.getRolePermissions()) {
            log.debug("Searched role permission role_id is {}, domain is {}, action is {} and selection is {}", 
                    rolePerm.getRoleId(), rolePerm.getPermitDomain(), rolePerm.getPermitAction(), rolePerm.getPermitSelection());
        }
        
        
    }
     
}
