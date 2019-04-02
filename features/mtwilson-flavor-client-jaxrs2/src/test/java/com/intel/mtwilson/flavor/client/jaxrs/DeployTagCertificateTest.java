/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.client.jaxrs;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.flavor.client.jaxrs.DeployTagCertificate;
import java.io.File;
import java.util.Properties;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author purvades
 */
public class DeployTagCertificateTest {
    Properties properties = new Properties();
    
    public DeployTagCertificateTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        properties.put("mtwilson.api.url", "https://192.168.0.1:8443/mtwilson/v2");
        
        // basic authentication
        properties.put("mtwilson.api.username", "admin");
        properties.put("mtwilson.api.password", "password");
        
        properties.put("mtwilson.api.tls.policy.insecure", "INSECURE");
    }
    
    @After
    public void tearDown() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
     @Test
     public void testDeployTagCertificate() throws Exception {
        DeployTagCertificate client = new DeployTagCertificate(properties);
        client.deployTagCertificate(UUID.valueOf("caa3aae4-3a7b-407b-8eb1-bbf82818ab2e"));
     }
}
