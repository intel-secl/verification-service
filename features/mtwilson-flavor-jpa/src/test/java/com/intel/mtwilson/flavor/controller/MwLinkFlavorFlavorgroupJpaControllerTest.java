/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.controller;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.core.flavor.common.FlavorPart;
import com.intel.mtwilson.flavor.data.MwFlavor;
import com.intel.mtwilson.flavor.data.MwFlavorgroup;
import com.intel.mtwilson.flavor.data.MwLinkFlavorFlavorgroup;
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
public class MwLinkFlavorFlavorgroupJpaControllerTest {
    private static final Logger log = LoggerFactory.getLogger(MwLinkFlavorFlavorgroupJpaControllerTest.class);
    
    private static final String JAVAX_PERSISTENCE_JDBC_DRIVER = "org.postgresql.Driver";
    private static final String JAVAX_PERSISTENCE_JDBC_URL = "jdbc:postgresql://192.168.0.1:5432/mw_as";
    private static final String JAVAX_PERSISTENCE_JDBC_USER = "root";
    private static final String JAVAX_PERSISTENCE_JDBC_PASSWORD = "password";
    
    private static final String PERSISTENCE_UNIT_NAME = "FlavorDataPU";
    private static EntityManagerFactory emf;
    private static MwFlavorJpaController mwFlavorJpaController;
    private static MwFlavorgroupJpaController mwFlavorgroupJpaController;
    private static MwLinkFlavorFlavorgroupJpaController mwLinkFlavorFlavorgroupJpaController;
    
    public MwLinkFlavorFlavorgroupJpaControllerTest() {
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
        mwFlavorJpaController = new MwFlavorJpaController(emf);
        mwFlavorgroupJpaController = new MwFlavorgroupJpaController(emf);
        mwLinkFlavorFlavorgroupJpaController = new MwLinkFlavorFlavorgroupJpaController(emf);
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
        MwFlavorgroup mwFlavorgroupAutomatic = mwFlavorgroupJpaController.findMwFlavorgroupByName("mtwilson_automatic");
        MwFlavorgroup mwFlavorgroupUnique = mwFlavorgroupJpaController.findMwFlavorgroupByName("mtwilson_unique");
        List<MwFlavor> mwFlavorEntries = mwFlavorJpaController.findMwFlavorEntities();
        for (MwFlavor mwFlavor : mwFlavorEntries) {
            String flavorgroupId = null;
            switch (FlavorPart.valueOf(mwFlavor.getContent().getMeta().getDescription().getFlavorPart())) {
                case HOST_UNIQUE:
                    System.out.println(String.format("HOST_UNIQUE flavor [%s] detected", mwFlavor.getId()));
                    flavorgroupId = mwFlavorgroupUnique.getId();
                    break;
                case PLATFORM:
                case OS:
                    System.out.println(String.format("AUTOMATIC flavor [%s] detected", mwFlavor.getId()));
                    flavorgroupId = mwFlavorgroupAutomatic.getId();
                    break;
                default:
                    throw new IllegalArgumentException(String.format("Invalid partial flavor type: %s",
                            mwFlavor.getContent().getMeta().getDescription().getFlavorPart()));
            }
            
            MwLinkFlavorFlavorgroup mwLinkFlavorFlavorgroup
                    = new MwLinkFlavorFlavorgroup(new UUID().toString(), mwFlavor.getId(), flavorgroupId);
            mwLinkFlavorFlavorgroupJpaController.create(mwLinkFlavorFlavorgroup);
            System.out.println(String.format("Link between flavor [%s] and flavorgroup [%s] created",
                    mwFlavor.getId(), mwFlavorgroupAutomatic.getName()));
        }
    }
    
    @Test
    public void readTestData() throws Exception {
        List<MwLinkFlavorFlavorgroup> mwLinkFlavorFlavorgroupEntries = mwLinkFlavorFlavorgroupJpaController.findMwLinkFlavorFlavorgroupEntities();
        for (MwLinkFlavorFlavorgroup mwLinkFlavorFlavorgroup : mwLinkFlavorFlavorgroupEntries) {
            System.out.println(String.format("Found link [%s] between flavor [%s] and flavorgroup [%s]",
                    mwLinkFlavorFlavorgroup.getId(), mwLinkFlavorFlavorgroup.getFlavorId(), mwLinkFlavorFlavorgroup.getFlavorgroupId()));
        }
    }
    
    @Test
    public void deleteAllData() throws Exception {
        List<MwLinkFlavorFlavorgroup> mwLinkFlavorFlavorgroupEntries = mwLinkFlavorFlavorgroupJpaController.findMwLinkFlavorFlavorgroupEntities();
        for (MwLinkFlavorFlavorgroup mwLinkFlavorFlavorgroup : mwLinkFlavorFlavorgroupEntries) {
            mwLinkFlavorFlavorgroupJpaController.destroy(mwLinkFlavorFlavorgroup.getId());
            System.out.println(String.format("Link [%s] between flavor and flavorgroup deleted",
                    mwLinkFlavorFlavorgroup.getId()));
        }
    }
    
    @Test
    public void findFlavorFlavorgroupLinkbyFlavorId() throws Exception {
        MwFlavorJpaController mwFlavorJpaController = new MwFlavorJpaController(emf);
        List<MwFlavor> mwFlavorList = mwFlavorJpaController.findMwFlavorByNameLike("host-");
        for (MwFlavor mwFlavor : mwFlavorList) {
            List<MwLinkFlavorFlavorgroup> mwLinkFlavorFlavorgroupList =
                    mwLinkFlavorFlavorgroupJpaController.findMwLinkFlavorFlavorgroupByFlavorId(mwFlavor.getId());
            if (mwLinkFlavorFlavorgroupList == null || mwLinkFlavorFlavorgroupList.isEmpty()) {
                System.out.println(String.format("Could not find any flavor flavorgroup links for flavor: %s", mwFlavor.getId()));
                continue;
            }
            for (MwLinkFlavorFlavorgroup mwLinkFlavorFlavorgroup : mwLinkFlavorFlavorgroupList) {
                System.out.println(String.format("Found flavor [%s] flavorgroup [%s] link: %s",
                        mwLinkFlavorFlavorgroup.getFlavorId(), mwLinkFlavorFlavorgroup.getFlavorgroupId(), mwLinkFlavorFlavorgroup.getId()));
            }
        }
    }
    
    @Test
    public void findFlavorFlavorgroupLinkbyFlavorgroupId() throws Exception {
        MwFlavorgroupJpaController mwFlavorgroupJpaController = new MwFlavorgroupJpaController(emf);
        List<MwFlavorgroup> mwFlavorgroupList = mwFlavorgroupJpaController.findMwFlavorgroupEntities();
        for (MwFlavorgroup mwFlavorgroup : mwFlavorgroupList) {
            List<MwLinkFlavorFlavorgroup> mwLinkFlavorFlavorgroupList =
                    mwLinkFlavorFlavorgroupJpaController.findMwLinkFlavorFlavorgroupByFlavorgroupId(mwFlavorgroup.getId());
            if (mwLinkFlavorFlavorgroupList == null || mwLinkFlavorFlavorgroupList.isEmpty()) {
                System.out.println(String.format(
                        "Could not find any flavor flavorgroup links for flavorgroup: %s", mwFlavorgroup.getName()));
                continue;
            }
            for (MwLinkFlavorFlavorgroup mwLinkFlavorFlavorgroup : mwLinkFlavorFlavorgroupList) {
                System.out.println(String.format("Found flavor [%s] flavorgroup [%s] link: %s",
                        mwLinkFlavorFlavorgroup.getFlavorId(), mwLinkFlavorFlavorgroup.getFlavorgroupId(),
                        mwLinkFlavorFlavorgroup.getId()));
            }
        }
    }
    
    @Test
    public void findFlavorFlavorgroupLinkbyBothIds() throws Exception {
        MwFlavorJpaController mwFlavorJpaController = new MwFlavorJpaController(emf);
        MwFlavorgroupJpaController mwFlavorgroupJpaController = new MwFlavorgroupJpaController(emf);
        List<MwFlavorgroup> mwFlavorgroupList = mwFlavorgroupJpaController.findMwFlavorgroupEntities();
        for (MwFlavorgroup mwFlavorgroup : mwFlavorgroupList) {
            List<MwFlavor> mwFlavorList = mwFlavorJpaController.findMwFlavorEntities();
            if (mwFlavorList == null || mwFlavorList.isEmpty()) {
                System.out.println("Could not find any flavors");
                continue;
            }
            for (MwFlavor mwFlavor : mwFlavorList) {
                MwLinkFlavorFlavorgroup mwLinkFlavorFlavorgroup
                        = mwLinkFlavorFlavorgroupJpaController.findMwLinkFlavorFlavorgroupByBothIds(
                                mwFlavor.getId(), mwFlavorgroup.getId());
                if (mwLinkFlavorFlavorgroup == null) {
                    System.out.println(String.format(
                            "Could not find a flavor flavorgroup link for flavorgroup: %s", mwFlavorgroup.getName()));
                    continue;
                }
                System.out.println(String.format("Found flavor [%s] flavorgroup [%s] link: %s",
                        mwLinkFlavorFlavorgroup.getFlavorId(), mwLinkFlavorFlavorgroup.getFlavorgroupId(),
                        mwLinkFlavorFlavorgroup.getId()));
            }
        }
    }
}
