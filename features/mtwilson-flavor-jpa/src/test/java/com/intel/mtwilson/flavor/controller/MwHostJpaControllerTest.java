/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.controller;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.flavor.data.MwHost;
import java.util.List;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author rksavino
 */
public class MwHostJpaControllerTest {
    private static final Logger log = LoggerFactory.getLogger(MwHostJpaControllerTest.class);
    
    private static final String JAVAX_PERSISTENCE_JDBC_DRIVER = "org.postgresql.Driver";
    private static final String JAVAX_PERSISTENCE_JDBC_URL = "jdbc:postgresql://192.168.0.1:5432/mw_as";
    private static final String JAVAX_PERSISTENCE_JDBC_USER = "root";
    private static final String JAVAX_PERSISTENCE_JDBC_PASSWORD = "password";
    
    private static final String PERSISTENCE_UNIT_NAME = "FlavorDataPU";
    private static EntityManagerFactory emf;
    private static MwHostJpaController mwHostJpaController;
    
    public MwHostJpaControllerTest() {
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
        mwHostJpaController = new MwHostJpaController(emf);
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
        for (int i = 1; i <= 10; i++) {
            MwHost mwHost = new MwHost();
            mwHost.setId(new UUID().toString());
            mwHost.setName(String.format("host-%d", i));
            mwHost.setTlsPolicyId(new UUID().toString());
            mwHost.setHardwareUuid(new UUID().toString());
            mwHost.setDescription(String.format("description-%d", i));
            mwHost.setConnectionString("https://192.168.0.1:1443/;uslogin;uspassword");
            mwHostJpaController.create(mwHost);
            System.out.println(String.format("Host [%s] with name [%s] created", mwHost.getId(), mwHost.getName()));
        }
    }
    
    @Test
    public void readTestData() throws Exception {
        List<MwHost> mwHostList = mwHostJpaController.findMwHostEntities();
        for (MwHost mwHost : mwHostList) {
            String hostId = mwHost.getId();
            System.out.println(String.format("Found host: %s", hostId));
            String hostName = mwHost.getName();
            System.out.println(String.format("   Host [%s] has host name: %s", hostId, hostName));
            String tlsPolicyId = mwHost.getTlsPolicyId();
            System.out.println(String.format("   Host [%s] has TLS policy ID: %s", hostName, tlsPolicyId));
            String hardwareUuid = mwHost.getHardwareUuid();
            System.out.println(String.format("   Host [%s] has hardware UUID: %s", hostName, hardwareUuid));
            String description = mwHost.getDescription();
            System.out.println(String.format("   Host [%s] has description: %s", hostName, description));
            String connectionString = mwHost.getConnectionString();
            System.out.println(String.format("   Host [%s] has connection string: %s", hostName, connectionString));
        }
    }
    
    @Test
    public void deleteAllData() throws Exception {
        List<MwHost> mwHostList = mwHostJpaController.findMwHostEntities();
        for (MwHost mwHost : mwHostList) {
            mwHostJpaController.destroy(mwHost.getId());
            System.out.println(String.format("Host [%s] with name [%s] deleted", mwHost.getId(), mwHost.getName()));
        }
    }
    
    @Test
    public void findHostsByName() throws Exception {
        String name = "host-1";
        MwHost mwHost = mwHostJpaController.findMwHostByName(name);
        
        if (mwHost == null) {
            System.out.println(String.format("Could not find host with name: %s", name));
            return;
        }
        System.out.println(String.format(
                "Found host [%s] with name: %s", mwHost.getId(), mwHost.getName()));
    }
    
    @Test
    public void findHostsByNameLike() throws Exception {
        String nameLike = "1";
        List<MwHost> mwHostList = mwHostJpaController.findMwHostByNameLike(nameLike);
        
        if (mwHostList == null) {
            System.out.println(String.format(
                    "Could not find any hosts with name pattern: %s", nameLike));
            return;
        }
        
        for (MwHost mwHost : mwHostList) {
            System.out.println(String.format(
                    "Found host [%s] with name: %s", mwHost.getId(), mwHost.getName()));
        }
    }
}
