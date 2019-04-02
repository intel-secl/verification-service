/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.controller;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.flavor.data.MwFlavorgroup;
import com.intel.mtwilson.flavor.model.FlavorMatchPolicy;
import com.intel.mtwilson.flavor.model.FlavorMatchPolicyCollection;
import com.intel.mtwilson.flavor.model.MatchPolicy;

import static com.intel.mtwilson.core.flavor.common.FlavorPart.*;
import static com.intel.mtwilson.flavor.model.MatchPolicy.MatchType.ANY_OF;
import static com.intel.mtwilson.flavor.model.MatchPolicy.Required.REQUIRED;
import static com.intel.mtwilson.flavor.model.MatchPolicy.Required.REQUIRED_IF_DEFINED;
import static com.intel.mtwilson.core.flavor.common.FlavorPart.*;
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
public class MwFlavorgroupJpaControllerTest {
    private static final Logger log = LoggerFactory.getLogger(MwFlavorgroupJpaControllerTest.class);
    
    private static final String JAVAX_PERSISTENCE_JDBC_DRIVER = "org.postgresql.Driver";
    private static final String JAVAX_PERSISTENCE_JDBC_URL = "jdbc:postgresql://192.168.0.1:5432/mw_as";
    private static final String JAVAX_PERSISTENCE_JDBC_USER = "root";
    private static final String JAVAX_PERSISTENCE_JDBC_PASSWORD = "password";
    
    private static final String PERSISTENCE_UNIT_NAME = "FlavorDataPU";
    private static EntityManagerFactory emf;
    private static MwFlavorgroupJpaController mwFlavorgroupJpaController;
    
    public MwFlavorgroupJpaControllerTest() {
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
        mwFlavorgroupJpaController = new MwFlavorgroupJpaController(emf);
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
        FlavorMatchPolicyCollection flavorMatchPolicyAutomatic = new FlavorMatchPolicyCollection();
        flavorMatchPolicyAutomatic.addFlavorMatchPolicy(new FlavorMatchPolicy(PLATFORM, new MatchPolicy(ANY_OF, REQUIRED)));
        flavorMatchPolicyAutomatic.addFlavorMatchPolicy(new FlavorMatchPolicy(OS, new MatchPolicy(ANY_OF, REQUIRED)));
//        flavorMatchPolicyAutomatic.addFlavorMatchPolicy(new FlavorMatchPolicy(ASSET_TAG, new MatchPolicy(ALL_OF, REQUIRED_IF_DEFINED)));
        flavorMatchPolicyAutomatic.addFlavorMatchPolicy(new FlavorMatchPolicy(HOST_UNIQUE, new MatchPolicy(ANY_OF, REQUIRED_IF_DEFINED)));
        
        MwFlavorgroup mwFlavorgroupAutomatic = new MwFlavorgroup();
        mwFlavorgroupAutomatic.setId(new UUID().toString());
        mwFlavorgroupAutomatic.setName(Flavorgroup.AUTOMATIC_FLAVORGROUP);
        mwFlavorgroupAutomatic.setFlavorTypeMatchPolicy(flavorMatchPolicyAutomatic);
        mwFlavorgroupJpaController.create(mwFlavorgroupAutomatic);
        System.out.println(String.format("Automatic flavorgroup [%s] created with name: %s", mwFlavorgroupAutomatic.getId(), mwFlavorgroupAutomatic.getName()));
        
        MwFlavorgroup mwFlavorgroupUnique = new MwFlavorgroup();
        mwFlavorgroupUnique.setId(new UUID().toString());
        mwFlavorgroupUnique.setName(Flavorgroup.UNIQUE_FLAVORGROUP);
        mwFlavorgroupJpaController.create(mwFlavorgroupUnique);
        System.out.println(String.format("Unique flavorgroup [%s] created with name: %s", mwFlavorgroupUnique.getId(), mwFlavorgroupUnique.getName()));

        for (int i = 1; i <= 10; i++) {
            FlavorMatchPolicyCollection flavorMatchPolicyHostBased = new FlavorMatchPolicyCollection();
            flavorMatchPolicyAutomatic.addFlavorMatchPolicy(new FlavorMatchPolicy(PLATFORM, new MatchPolicy(ANY_OF, REQUIRED)));
            flavorMatchPolicyAutomatic.addFlavorMatchPolicy(new FlavorMatchPolicy(OS, new MatchPolicy(ANY_OF, REQUIRED)));
            flavorMatchPolicyAutomatic.addFlavorMatchPolicy(new FlavorMatchPolicy(HOST_UNIQUE, new MatchPolicy(ANY_OF, REQUIRED_IF_DEFINED)));

            MwFlavorgroup mwFlavorgroupHostBased = new MwFlavorgroup();
            mwFlavorgroupHostBased.setId(new UUID().toString());
            mwFlavorgroupHostBased.setName(String.format("host-%d", i));
            mwFlavorgroupHostBased.setFlavorTypeMatchPolicy(flavorMatchPolicyHostBased);
            mwFlavorgroupJpaController.create(mwFlavorgroupHostBased);
            System.out.println(String.format("Host based flavorgroup [%s] created with name: %s", mwFlavorgroupHostBased.getId(), mwFlavorgroupHostBased.getName()));
        }
    }
    
    @Test
    public void readTestData() throws Exception {
        List<MwFlavorgroup> mwFlavorgroupList = mwFlavorgroupJpaController.findMwFlavorgroupEntities();
        for (MwFlavorgroup mwFlavorgroup : mwFlavorgroupList) {
            String flavorgroupId = mwFlavorgroup.getId();
            System.out.println(String.format("Found flavorgroup [%s] with name: %s", flavorgroupId, mwFlavorgroup.getName()));
            FlavorMatchPolicyCollection flavorMatchPolicyCollection = mwFlavorgroup.getFlavorTypeMatchPolicy();
            if (flavorMatchPolicyCollection == null) {
                System.out.println(String.format("   Flavorgroup [%s] has no flavor match policy", mwFlavorgroup.getName()));
                continue;
            }
            for (FlavorMatchPolicy flavorMatchPolicy : flavorMatchPolicyCollection.getFlavorMatchPolicies()) {
                System.out.println(String.format("   Flavorgroup [%s] has flavor part [%s] with match type [%s] and required [%s]",
                        flavorgroupId, flavorMatchPolicy.getFlavorPart().name(),
                        flavorMatchPolicy.getMatchPolicy().getMatchType().name(),
                        flavorMatchPolicy.getMatchPolicy().getRequired().name()));
            }
        }
    }
    
    @Test
    public void deleteAllData() throws Exception {
        List<MwFlavorgroup> mwFlavorgroupList = mwFlavorgroupJpaController.findMwFlavorgroupEntities();
        for (MwFlavorgroup mwFlavorgroup : mwFlavorgroupList) {
            mwFlavorgroupJpaController.destroy(mwFlavorgroup.getId());
            System.out.println(String.format("Flavorgroup [%s] deleted", mwFlavorgroup.getId()));
        }
    }
    
    @Test
    public void findFlavorgroupsByName() throws Exception {
        String name = Flavorgroup.AUTOMATIC_FLAVORGROUP;
        MwFlavorgroup mwFlavorgroup = mwFlavorgroupJpaController.findMwFlavorgroupByName(name);
        
        if (mwFlavorgroup == null) {
            System.out.println(String.format("Could not find flavorgroup with name: %s", name));
            return;
        }
        System.out.println(String.format(
                "Found flavorgroup [%s] with name: %s", mwFlavorgroup.getId(), mwFlavorgroup.getName()));
    }
    
    @Test
    public void findFlavorgroupsByNameLike() throws Exception {
        String nameLike = "wilson";
        List<MwFlavorgroup> mwFlavorgroupList = mwFlavorgroupJpaController.findMwFlavorgroupByNameLike(nameLike);
        
        if (mwFlavorgroupList == null) {
            System.out.println(String.format(
                    "Could not find any flavorgroups with name pattern: %s", nameLike));
            return;
        }
        
        for (MwFlavorgroup mwFlavorgroup : mwFlavorgroupList) {
            System.out.println(String.format(
                    "Found flavorgroup [%s] with name: %s", mwFlavorgroup.getId(), mwFlavorgroup.getName()));
        }
    }
}
