/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.telemetry.controller;

import com.intel.mtwilson.telemetry.data.MwTelemetry;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.spi.PersistenceUnitTransactionType;
import static org.eclipse.persistence.config.PersistenceUnitProperties.*;
import com.intel.dcsg.cpg.io.UUID;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author hdxia
 */
public class MwTelemetryJpaControllerTest {
    private static final Logger log = LoggerFactory.getLogger(MwTelemetryJpaControllerTest.class);
    
    private static final String JAVAX_PERSISTENCE_JDBC_DRIVER = "org.postgresql.Driver";
    private static final String JAVAX_PERSISTENCE_JDBC_URL = "jdbc:postgresql://192.168.0.1:5432/mw_as";
    private static final String JAVAX_PERSISTENCE_JDBC_USER = "root";
    private static final String JAVAX_PERSISTENCE_JDBC_PASSWORD = "A8+UZaKONGWV3emoXcvMug";
    private static final String PERSISTENCE_UNIT_NAME = "TelemetryDataPU";
    private static EntityManagerFactory emf;
    private static MwTelemetryJpaController mwTelemetryJpaController; 
    
    public MwTelemetryJpaControllerTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        Properties jpaProperties = new Properties();
        jpaProperties.put(TRANSACTION_TYPE, PersistenceUnitTransactionType.RESOURCE_LOCAL.name());
        jpaProperties.put(JDBC_DRIVER, JAVAX_PERSISTENCE_JDBC_DRIVER);
        jpaProperties.put(JDBC_URL, JAVAX_PERSISTENCE_JDBC_URL);
        jpaProperties.put(JDBC_USER, JAVAX_PERSISTENCE_JDBC_USER);
        jpaProperties.put(JDBC_PASSWORD, JAVAX_PERSISTENCE_JDBC_PASSWORD);
        
        log.debug("Loading database driver {} for persistence unit {}",  jpaProperties.getProperty("javax.persistence.jdbc.driver"), PERSISTENCE_UNIT_NAME);
        emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, jpaProperties);
        mwTelemetryJpaController = new MwTelemetryJpaController(emf);
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

    /**
     * Test of create method, of class MwTelemetryJpaController.
     */
    @Test
    public void testCreate() throws Exception {
        System.out.println("create");
        MwTelemetry mwentry = new MwTelemetry();
        mwentry.setId(new UUID().toString());
        mwentry.setCreateDate(new Date());
        mwentry.setHostNum(5);
        mwTelemetryJpaController.create(mwentry);
        // TODO review the generated test code and remove the default call to fail.
    }

    /**
     * Test of edit method, of class MwTelemetryJpaController.
    
    @Test
    public void testEdit() throws Exception {
        System.out.println("edit");
        MwTelemetry mwTelemetry = new MwTelemetry();
        mwTelemetry.setId(1);;
        MwTelemetryJpaController instance = null;
        instance.edit(mwTelemetry);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    */
    /**
     * Test of destroy method, of class MwTelemetryJpaController.
    
    @Test
    public void testDestroy() throws Exception {
        System.out.println("destroy");
        Integer id = null;
        MwTelemetryJpaController instance = null;
        instance.destroy(id);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    */

    /**
     * Test of findMwTelemetryEntities method, of class MwTelemetryJpaController.
     
    @Test
    public void testFindMwTelemetryEntities_0args() {
        System.out.println("findMwTelemetryEntities");
        MwTelemetryJpaController instance = null;
        List<MwTelemetry> expResult = null;
        List<MwTelemetry> result = instance.findMwTelemetryEntities();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    */
    /**
     * Test of findMwTelemetryEntities method, of class MwTelemetryJpaController.
    
    @Test
    public void testFindMwTelemetryEntities_int_int() {
        System.out.println("findMwTelemetryEntities");
        int maxResults = 0;
        int firstResult = 0;
        MwTelemetryJpaController instance = null;
        List<MwTelemetry> expResult = null;
        List<MwTelemetry> result = instance.findMwTelemetryEntities(maxResults, firstResult);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    */
    /**
     * Test of findMwTelemetry method, of class MwTelemetryJpaController.
    
    @Test
    public void testFindMwTelemetry() {
        System.out.println("findMwTelemetry");
        Integer id = null;
        MwTelemetryJpaController instance = null;
        MwTelemetry expResult = null;
        MwTelemetry result = instance.findMwTelemetry(id);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    */
    /**
     * Test of getMwTelemetryCount method, of class MwTelemetryJpaController.
    
    @Test
    public void testGetMwTelemetryCount() {
        System.out.println("getMwTelemetryCount");
        MwTelemetryJpaController instance = null;
        int expResult = 0;
        int result = instance.getMwTelemetryCount();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    */
}
