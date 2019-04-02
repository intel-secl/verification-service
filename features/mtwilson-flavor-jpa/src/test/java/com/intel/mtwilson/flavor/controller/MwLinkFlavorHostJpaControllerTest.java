/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.controller;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.flavor.data.MwFlavor;
import com.intel.mtwilson.flavor.data.MwHost;
import com.intel.mtwilson.flavor.data.MwLinkFlavorHost;
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
public class MwLinkFlavorHostJpaControllerTest {
    private static final Logger log = LoggerFactory.getLogger(MwLinkFlavorHostJpaControllerTest.class);
    
    private static final String JAVAX_PERSISTENCE_JDBC_DRIVER = "org.postgresql.Driver";
    private static final String JAVAX_PERSISTENCE_JDBC_URL = "jdbc:postgresql://192.168.0.1:5432/mw_as";
    private static final String JAVAX_PERSISTENCE_JDBC_USER = "root";
    private static final String JAVAX_PERSISTENCE_JDBC_PASSWORD = "password";
    
    private static final String PERSISTENCE_UNIT_NAME = "FlavorDataPU";
    private static EntityManagerFactory emf;
    private static MwLinkFlavorHostJpaController mwLinkFlavorHostJpaController;
    
    public MwLinkFlavorHostJpaControllerTest() {
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
        mwLinkFlavorHostJpaController = new MwLinkFlavorHostJpaController(emf);
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
        MwFlavorJpaController mwFlavorJpaController = new MwFlavorJpaController(emf);
        MwHostJpaController mwHostJpaController = new MwHostJpaController(emf);
        List<MwHost> mwHostList = mwHostJpaController.findMwHostEntities();
        
        for (MwHost mwHost : mwHostList) {
            List<MwFlavor> mwFlavorList = mwFlavorJpaController.findMwFlavorByKeyValue("label", mwHost.getName());
            if (mwFlavorList == null || mwFlavorList.isEmpty()) {
                System.out.println(String.format("Could not find any flavors for host: %s", mwHost.getName()));
                continue;
            }
            for (MwFlavor mwFlavor : mwFlavorList) {
                UUID flavorHostLinkId = new UUID();
                System.out.println(String.format(
                        "Found flavor [%s|%s] for host [%s], creating link association: %s",
                        mwFlavor.getContent().getMeta().getDescription().getFlavorPart(),
                        mwFlavor.getId(), mwHost.getName(), flavorHostLinkId.toString()));
                mwLinkFlavorHostJpaController.create(new MwLinkFlavorHost(
                        flavorHostLinkId.toString(), mwFlavor.getId(), mwHost.getId()));
            }
        }
    }
    
    @Test
    public void readTestData() throws Exception {
        List<MwLinkFlavorHost> mwLinkFlavorHostList = mwLinkFlavorHostJpaController.findMwLinkFlavorHostEntities();
        for (MwLinkFlavorHost mwLinkFlavorHost : mwLinkFlavorHostList) {
            System.out.println(String.format("Found flavor host link [%s] with flavor ID [%s] and host ID [%s]",
                    mwLinkFlavorHost.getId(), mwLinkFlavorHost.getFlavorId(), mwLinkFlavorHost.getHostId()));
        }
    }
    
    @Test
    public void deleteAllData() throws Exception {
        List<MwLinkFlavorHost> mwLinkFlavorHostList = mwLinkFlavorHostJpaController.findMwLinkFlavorHostEntities();
        for (MwLinkFlavorHost mwLinkFlavorHost : mwLinkFlavorHostList) {
            mwLinkFlavorHostJpaController.destroy(mwLinkFlavorHost.getId());
            System.out.println(String.format("Flavor host link [%s] deleted", mwLinkFlavorHost.getId()));
        }
    }
    
    @Test
    public void findFlavorHostLinkbyFlavorId() throws Exception {
        MwFlavorJpaController mwFlavorJpaController = new MwFlavorJpaController(emf);
        List<MwFlavor> mwFlavorList = mwFlavorJpaController.findMwFlavorByNameLike("host-");
        for (MwFlavor mwFlavor : mwFlavorList) {
            List<MwLinkFlavorHost> mwLinkFlavorHostList =
                    mwLinkFlavorHostJpaController.findMwLinkFlavorHostByFlavorId(mwFlavor.getId());
            if (mwLinkFlavorHostList == null || mwLinkFlavorHostList.isEmpty()) {
                System.out.println(String.format("Could not find any flavor host links for flavor: %s", mwFlavor.getId()));
                continue;
            }
            for (MwLinkFlavorHost mwLinkFlavorHost : mwLinkFlavorHostList) {
                System.out.println(String.format("Found flavor [%s] host [%s] link: %s",
                        mwLinkFlavorHost.getFlavorId(), mwLinkFlavorHost.getHostId(), mwLinkFlavorHost.getId()));
            }
        }
    }
    
    @Test
    public void findFlavorHostLinkbyHostId() throws Exception {
        MwHostJpaController mwHostJpaController = new MwHostJpaController(emf);
        List<MwHost> mwHostList = mwHostJpaController.findMwHostEntities();
        for (MwHost mwHost : mwHostList) {
            List<MwLinkFlavorHost> mwLinkFlavorHostList =
                    mwLinkFlavorHostJpaController.findMwLinkFlavorHostByHostId(mwHost.getId());
            if (mwLinkFlavorHostList == null || mwLinkFlavorHostList.isEmpty()) {
                System.out.println(String.format("Could not find any flavor host links for host: %s", mwHost.getName()));
                continue;
            }
            for (MwLinkFlavorHost mwLinkFlavorHost : mwLinkFlavorHostList) {
                System.out.println(String.format("Found flavor [%s] host [%s] link: %s",
                        mwLinkFlavorHost.getFlavorId(), mwLinkFlavorHost.getHostId(), mwLinkFlavorHost.getId()));
            }
        }
    }
    
    @Test
    public void findFlavorHostLinkbyBothIds() throws Exception {
        MwFlavorJpaController mwFlavorJpaController = new MwFlavorJpaController(emf);
        MwHostJpaController mwHostJpaController = new MwHostJpaController(emf);
        List<MwHost> mwHostList = mwHostJpaController.findMwHostEntities();
        for (MwHost mwHost : mwHostList) {
            MwFlavor mwFlavor = mwFlavorJpaController.findMwFlavorByName(mwHost.getName());
            if (mwFlavor == null) {
                System.out.println(String.format("Could not find any flavors with label: %s", mwHost.getName()));
                continue;
            }
            MwLinkFlavorHost mwLinkFlavorHost
                    = mwLinkFlavorHostJpaController.findMwLinkFlavorHostByBothIds(mwFlavor.getId(), mwHost.getId());
            if (mwLinkFlavorHost == null) {
                System.out.println(String.format("Could not find a flavor host link for host: %s", mwHost.getName()));
                continue;
            }
            System.out.println(String.format("Found flavor [%s] host [%s] link: %s",
                    mwLinkFlavorHost.getFlavorId(), mwLinkFlavorHost.getHostId(), mwLinkFlavorHost.getId()));
        }
    }
}
