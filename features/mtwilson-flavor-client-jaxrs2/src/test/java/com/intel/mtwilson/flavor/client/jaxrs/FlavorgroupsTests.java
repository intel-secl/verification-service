/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.client.jaxrs;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.tls.policy.TlsConnection;
import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import com.intel.dcsg.cpg.tls.policy.impl.InsecureTlsPolicy; 
import com.intel.mtwilson.flavor.client.jaxrs.Flavorgroups;
import com.intel.mtwilson.flavor.rest.v2.model.FlavorgroupLocator;
import java.net.URL;
import java.util.Properties;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author purvades
 */
public class FlavorgroupsTests {
    
    public FlavorgroupsTests() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
//    @Test
//     public void testDeleteFlavorGroup() throws Exception {
//        TlsPolicy tlsPolicy = new InsecureTlsPolicy();
//        TlsConnection tlsConnection = new TlsConnection(new URL("https://192.168.0.1:8443/mtwilson/v2"), tlsPolicy);
//        
//         
//        Properties properties = new Properties();
//        properties.put("mtwilson.api.url", "https://192.168.0.1:8443/mtwilson/v2");
//        
//        // basic authentication
//        properties.put("mtwilson.api.username", "admin");
//        properties.put("mtwilson.api.password", "password");
//        
//        
//        
//        FlavorgroupLocator locator = new FlavorgroupLocator();
//        locator.pathId = UUID.valueOf("44f325df-282c-4e70-a305-e18344dded86");
//      
//        // Create the client and call the delete API
//        Flavorgroups client = new Flavorgroups(properties, tlsConnection);
//        client.delete(locator);
//     }
}
