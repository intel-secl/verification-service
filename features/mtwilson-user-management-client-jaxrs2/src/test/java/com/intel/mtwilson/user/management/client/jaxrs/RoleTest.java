/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.user.management.client.jaxrs;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.My;
import com.intel.mtwilson.user.management.rest.v2.model.Role;
import com.intel.mtwilson.user.management.rest.v2.model.RoleCollection;
import com.intel.mtwilson.user.management.rest.v2.model.RoleFilterCriteria;
import java.util.Locale;
import java.util.Properties;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author ssbangal
 */
public class RoleTest {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RoleTest.class);

    private static Roles client = null;
  
    @Test
    public void testRole() throws Exception {
        Properties properties = new Properties();
        properties.put("mtwilson.api.url","https://192.168.0.1:8443/mtwilson/v2");
        properties.put("mtwilson.api.username", "admin");
        properties.put("mtwilson.api.password", "password");
        client = new Roles(properties);
        UUID roleId = new UUID();
        
        Role createRole = new Role();
        createRole.setId(roleId);
        createRole.setRoleName("Admin999");
        createRole.setDescription("Admin role");
        client.create(createRole);
        
       // Role retrievRole = client.retrieveRole(createRole.getId().toString());
        //log.debug("Retrieved role name is {}, and description is {}", retrievRole.getRoleName(), retrievRole.getDescription());
        
       // createRole.setDescription("Updated Admin role description.");
       // client.store(createRole);
        
      //  RoleFilterCriteria criteria = new RoleFilterCriteria();
       // criteria.filter = false;
       // RoleCollection users = client.search(criteria);
      //  for(Role user : users.getRoles()) {
     //       log.debug("Searched role name is {}, and description is {}", user.getRoleName(), user.getDescription());
      //  }
      //  
     //   client.deleteRole(roleId.toString());
    }
 
 //   @Test
 //   public void testRoleDeleteSearchCriteria() throws Exception {

     //   RoleFilterCriteria criteria = new RoleFilterCriteria();
   //     criteria.nameContains = "Developer";
   //     client.delete(criteria);
        
  //  }
    
}
