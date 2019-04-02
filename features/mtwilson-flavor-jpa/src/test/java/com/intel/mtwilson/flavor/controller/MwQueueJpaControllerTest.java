/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.controller;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.flavor.data.MwQueue;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.spi.PersistenceUnitTransactionType;
import static org.eclipse.persistence.config.PersistenceUnitProperties.*;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author rksavino
 */
public class MwQueueJpaControllerTest {
    private static final Logger log = LoggerFactory.getLogger(MwQueueJpaControllerTest.class);
    
    private static final String JAVAX_PERSISTENCE_JDBC_DRIVER = "org.postgresql.Driver";
    private static final String JAVAX_PERSISTENCE_JDBC_URL = "jdbc:postgresql://192.168.0.1:5432/mw_as";
    private static final String JAVAX_PERSISTENCE_JDBC_USER = "root";
    private static final String JAVAX_PERSISTENCE_JDBC_PASSWORD = "password";
    
    private static final String PERSISTENCE_UNIT_NAME = "FlavorDataPU";
    private static EntityManagerFactory emf;
    private static MwQueueJpaController mwQueueJpaController;
    
    public MwQueueJpaControllerTest() {
    }
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        Properties jpaProperties = new Properties();
        jpaProperties.put(TRANSACTION_TYPE, PersistenceUnitTransactionType.RESOURCE_LOCAL.name());
        jpaProperties.put(JDBC_DRIVER, JAVAX_PERSISTENCE_JDBC_DRIVER);
        jpaProperties.put(JDBC_URL, JAVAX_PERSISTENCE_JDBC_URL);
        jpaProperties.put(JDBC_USER, JAVAX_PERSISTENCE_JDBC_USER);
        jpaProperties.put(JDBC_PASSWORD, JAVAX_PERSISTENCE_JDBC_PASSWORD);
        
        log.debug("Loading database driver {} for persistence unit {}",  jpaProperties.getProperty("javax.persistence.jdbc.driver"), PERSISTENCE_UNIT_NAME);
        emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, jpaProperties);
        mwQueueJpaController = new MwQueueJpaController(emf);
    }
    
    @AfterClass
    public static void tearDownClass() {
        emf.close();
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }
    
    @Test
    public void createTestData() throws Exception {
        for (int i = 0; i < 10; i++) {
            Map<String, String> actionParameters = new HashMap();
            actionParameters.put("host_id", new UUID().toString());
            actionParameters.put("force_update", "false");
            
            MwQueue queueEntry = new MwQueue();
            queueEntry.setId(new UUID().toString());
            queueEntry.setQueueAction("flavor-verify");
            queueEntry.setActionParameters(actionParameters);
            mwQueueJpaController.create(queueEntry);
            System.out.println(String.format("Queue Entry [%s] with queue action [%s] created for host with name: %s", queueEntry.getId(), queueEntry.getQueueAction(), queueEntry.getActionParameters()));
        }
    }
    
    @Test
    public void readTestData() throws Exception {
        List<MwQueue> queueEntries = mwQueueJpaController.findMwQueueEntities();
        for (MwQueue queueEntry : queueEntries) {
            String id = queueEntry.getId();
            System.out.println(String.format("Found Queue Entry: %s", id));
            String queueAction = queueEntry.getQueueAction();
            System.out.println(String.format("   Queue Entry [%s] has queue action: %s", id, queueAction));
            String hostName = queueEntry.getActionParameter("host_name");
            System.out.println(String.format("   Queue Entry [%s] has hostname: %s", id, hostName));
            String forceUpdate = queueEntry.getActionParameter("force_update");
            System.out.println(String.format("   Queue Entry [%s] has force update: %s", id, forceUpdate));
            Map<String, String> actionParameters = queueEntry.getActionParameters();
            System.out.println(String.format("   Queue Entry [%s] has action parameters: %s", id, actionParameters.toString()));
        }
    }
    
    @Test
    public void deleteAllData() throws Exception {
        List<MwQueue> queueEntries = mwQueueJpaController.findMwQueueEntities();
        for (MwQueue queueEntry : queueEntries) {
            mwQueueJpaController.destroy(queueEntry.getId());
            System.out.println(String.format("Queue Entry [%s] with queue action [%s] deleted for host with name: %s", queueEntry.getId(), queueEntry.getQueueAction(), queueEntry.getActionParameters()));
        }
    }
    
    @Test
    public void findFlavorVerifyQueueEntriesByHostId() throws Exception {
        List<MwQueue> wholeQueueList = mwQueueJpaController.findMwQueueEntities(1, 1);
        String hostId = wholeQueueList.get(0).getActionParameter("host_id");
        List<MwQueue> hostQueueList = mwQueueJpaController.findMwQueueByActionParameter("flavor-verify", "host_id", hostId);
        for (MwQueue queueEntry : hostQueueList) {
            System.out.println(String.format(
                    "Queue Entry [%s] with queue action [%s] already in queue with force update: %s",
                    queueEntry.getId(), queueEntry.getQueueAction(), queueEntry.getActionParameter("force_update")));
        }
    }
}
