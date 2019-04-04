/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.user.management.client.jaxrs;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.user.management.rest.v2.model.Role;
import java.util.Properties;
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
    }
}
