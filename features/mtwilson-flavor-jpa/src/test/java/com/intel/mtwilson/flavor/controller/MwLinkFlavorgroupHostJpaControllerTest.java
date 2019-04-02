/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.controller;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.flavor.data.MwFlavorgroup;
import com.intel.mtwilson.flavor.data.MwHost;
import com.intel.mtwilson.flavor.data.MwLinkFlavorgroupHost;
import java.util.List;
import java.util.Properties;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.spi.PersistenceUnitTransactionType;
import static org.eclipse.persistence.config.PersistenceUnitProperties.*;

import com.intel.mtwilson.flavor.rest.v2.model.Flavorgroup;
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
public class MwLinkFlavorgroupHostJpaControllerTest {
    private static final Logger log = LoggerFactory.getLogger(MwLinkFlavorgroupHostJpaControllerTest.class);
    
    private static final String JAVAX_PERSISTENCE_JDBC_DRIVER = "org.postgresql.Driver";
    private static final String JAVAX_PERSISTENCE_JDBC_URL = "jdbc:postgresql://192.168.0.1:5432/mw_as";
    private static final String JAVAX_PERSISTENCE_JDBC_USER = "root";
    private static final String JAVAX_PERSISTENCE_JDBC_PASSWORD = "password";
    
    private static final String PERSISTENCE_UNIT_NAME = "FlavorDataPU";
    private static EntityManagerFactory emf;
    private static MwFlavorgroupJpaController mwFlavorgroupJpaController;
    private static MwHostJpaController mwHostJpaController;
    private static MwLinkFlavorgroupHostJpaController mwLinkFlavorgroupHostJpaController;
    
    public MwLinkFlavorgroupHostJpaControllerTest() { }
    
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
        mwFlavorgroupJpaController = new MwFlavorgroupJpaController(emf);
        mwHostJpaController = new MwHostJpaController(emf);
        mwLinkFlavorgroupHostJpaController = new MwLinkFlavorgroupHostJpaController(emf);
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
        MwFlavorgroup mwFlavorgroupAutomatic = mwFlavorgroupJpaController.findMwFlavorgroupByName(Flavorgroup.AUTOMATIC_FLAVORGROUP);
        List<MwHost> mwHostEntries = mwHostJpaController.findMwHostEntities();
        for (MwHost mwHost : mwHostEntries) {
            MwLinkFlavorgroupHost mwLinkFlavorgroupHost
                    = new MwLinkFlavorgroupHost(new UUID().toString(), mwFlavorgroupAutomatic.getId(), mwHost.getId());
            mwLinkFlavorgroupHostJpaController.create(mwLinkFlavorgroupHost);
            System.out.println(String.format("Link between flavorgroup [%s] and host [%s] created",
                    mwFlavorgroupAutomatic.getName(), mwHost.getId()));
        }
    }
    
    @Test
    public void readTestData() throws Exception {
        List<MwLinkFlavorgroupHost> mwLinkFlavorgroupHostEntries = mwLinkFlavorgroupHostJpaController.findMwLinkFlavorgroupHostEntities();
        for (MwLinkFlavorgroupHost mwLinkFlavorgroupHost : mwLinkFlavorgroupHostEntries) {
            System.out.println(String.format("Found link [%s] between flavorgroup [%s] and host [%s]",
                    mwLinkFlavorgroupHost.getId(), mwLinkFlavorgroupHost.getFlavorgroupId(), mwLinkFlavorgroupHost.getHostId()));
        }
    }
    
    @Test
    public void deleteAllData() throws Exception {
        List<MwLinkFlavorgroupHost> mwLinkFlavorgroupHostEntries = mwLinkFlavorgroupHostJpaController.findMwLinkFlavorgroupHostEntities();
        for (MwLinkFlavorgroupHost mwLinkFlavorgroupHost : mwLinkFlavorgroupHostEntries) {
            mwLinkFlavorgroupHostJpaController.destroy(mwLinkFlavorgroupHost.getId());
            System.out.println(String.format("Link [%s] between flavorgroup and host deleted",
                    mwLinkFlavorgroupHost.getId()));
        }
    }
    
    @Test
    public void findFlavorgroupHostLinkbyFlavorgroupId() throws Exception {
        List<MwFlavorgroup> mwFlavorgroupList = mwFlavorgroupJpaController.findMwFlavorgroupEntities();
        for (MwFlavorgroup mwFlavorgroup : mwFlavorgroupList) {
            List<MwLinkFlavorgroupHost> mwLinkFlavorgroupHostList =
                    mwLinkFlavorgroupHostJpaController.findMwLinkFlavorgroupHostByFlavorgroupId(mwFlavorgroup.getId());
            if (mwLinkFlavorgroupHostList == null || mwLinkFlavorgroupHostList.isEmpty()) {
                System.out.println(String.format(
                        "Could not find any flavorgroup host links for flavorgroup: %s", mwFlavorgroup.getName()));
                continue;
            }
            for (MwLinkFlavorgroupHost mwLinkFlavorgroupHost : mwLinkFlavorgroupHostList) {
                System.out.println(String.format("Found flavorgroup [%s] host [%s] link: %s",
                        mwLinkFlavorgroupHost.getFlavorgroupId(), mwLinkFlavorgroupHost.getHostId(),
                        mwLinkFlavorgroupHost.getId()));
            }
        }
    }
    
    @Test
    public void findFlavorgroupHostLinkbyHostId() throws Exception {
        List<MwHost> mwHostList = mwHostJpaController.findMwHostEntities();
        for (MwHost mwHost : mwHostList) {
            List<MwLinkFlavorgroupHost> mwLinkFlavorgroupHostList =
                    mwLinkFlavorgroupHostJpaController.findMwLinkFlavorgroupHostByHostId(mwHost.getId());
            if (mwLinkFlavorgroupHostList == null || mwLinkFlavorgroupHostList.isEmpty()) {
                System.out.println(String.format(
                        "Could not find any flavorgroup host links for host: %s", mwHost.getId()));
                continue;
            }
            for (MwLinkFlavorgroupHost mwLinkFlavorgroupHost : mwLinkFlavorgroupHostList) {
                System.out.println(String.format("Found flavorgroup [%s] host [%s] link: %s",
                        mwLinkFlavorgroupHost.getFlavorgroupId(), mwLinkFlavorgroupHost.getHostId(),
                        mwLinkFlavorgroupHost.getId()));
            }
        }
    }
    
    @Test
    public void findFlavorgroupHostLinkbyBothIds() throws Exception {
        List<MwFlavorgroup> mwFlavorgroupList = mwFlavorgroupJpaController.findMwFlavorgroupEntities();
        for (MwFlavorgroup mwFlavorgroup : mwFlavorgroupList) {
            List<MwHost> mwHostList = mwHostJpaController.findMwHostEntities();
            if (mwHostList == null || mwHostList.isEmpty()) {
                System.out.println("Could not find any hosts");
                continue;
            }
            for (MwHost mwHost : mwHostList) {
                MwLinkFlavorgroupHost mwLinkFlavorgroupHost
                        = mwLinkFlavorgroupHostJpaController.findMwLinkFlavorgroupHostByBothIds(
                                mwFlavorgroup.getId(), mwHost.getId());
                if (mwLinkFlavorgroupHost == null) {
                    System.out.println(String.format(
                            "Could not find a flavorgroup host link for host: %s", mwHost.getName()));
                    continue;
                }
                System.out.println(String.format("Found flavorgroup [%s] host [%s] link: %s",
                        mwLinkFlavorgroupHost.getFlavorgroupId(), mwLinkFlavorgroupHost.getHostId(),
                        mwLinkFlavorgroupHost.getId()));
            }
        }
    }
}
