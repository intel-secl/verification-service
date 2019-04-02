/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.flavor.rest.v2.resource;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.flavor.model.HostStatusInformation;
import com.intel.mtwilson.flavor.rest.v2.model.HostStatus;
import com.intel.mtwilson.flavor.rest.v2.repository.HostStatusRepository;
import com.intel.mtwilson.i18n.HostState;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author hmgowda
 */
public class HostStatusTest {
    
   
     public HostStatusTest() {
    }
    
    @BeforeClass
    public static void setUpClass() throws Exception {
       
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    
    @After
    public void tearDown() {
    }

   private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FlavorsTest.class);
    
    
    @Test
    public void createHostStatusTest() throws Exception {
        HostStatusRepository hostStatusRepository = new HostStatusRepository();
        HostStatus hostStatus = new HostStatus();
        HostStatusInformation hostStatusInfo = new HostStatusInformation();
        hostStatusInfo.setHostState(HostState.CONNECTED);
        
        hostStatus.setId(new UUID());
        hostStatus.setHostId(UUID.valueOf("c894fa6c-3fc1-4615-9f22-f9c13d69c21a"));
        hostStatus.setHostManifest(null);
        hostStatus.setStatus(hostStatusInfo);
        hostStatusRepository.create(hostStatus);
    }
    
    @Test
    public void storeHostStatusTest() throws Exception {
        HostStatusRepository hostStatusRepository = new HostStatusRepository();
        HostStatus hostStatus = new HostStatus();
        HostStatusInformation hostStatusInfo = new HostStatusInformation();
        hostStatusInfo.setHostState(HostState.CONNECTED);
        
        hostStatus.setHostId(UUID.valueOf("c894fa6c-3fc1-4615-9f22-f9c13d69c21a"));
        hostStatus.setHostManifest(null);
        hostStatus.setStatus(hostStatusInfo);
        hostStatusRepository.store(hostStatus);
    }
}
