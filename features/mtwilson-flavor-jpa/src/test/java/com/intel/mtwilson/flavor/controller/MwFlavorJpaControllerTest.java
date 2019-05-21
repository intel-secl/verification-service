/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import java.util.Arrays; 
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.core.flavor.PlatformFlavor;
import com.intel.mtwilson.core.flavor.PlatformFlavorFactory;
import com.intel.mtwilson.core.flavor.common.FlavorPart;
import com.intel.mtwilson.core.flavor.model.Flavor;
import com.intel.mtwilson.flavor.data.MwFlavor;
import com.intel.mtwilson.flavor.data.MwFlavorgroup;
import com.intel.mtwilson.flavor.rest.v2.model.Flavorgroup;
import com.intel.mtwilson.jaxrs2.provider.JacksonObjectMapperProvider;
import com.intel.mtwilson.core.common.model.HostManifest;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.spi.PersistenceUnitTransactionType;
import org.apache.commons.lang3.StringUtils;

import static com.intel.mtwilson.core.flavor.common.FlavorPart.*;
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
public class MwFlavorJpaControllerTest {
    private static final Logger log = LoggerFactory.getLogger(MwFlavorJpaControllerTest.class);
    
    private static final String JAVAX_PERSISTENCE_JDBC_DRIVER = "org.postgresql.Driver";
    private static final String JAVAX_PERSISTENCE_JDBC_URL = "jdbc:postgresql://192.168.0.1:5432/mw_as";
    private static final String JAVAX_PERSISTENCE_JDBC_USER = "root";
    private static final String JAVAX_PERSISTENCE_JDBC_PASSWORD = "password";
    
    private static final String PERSISTENCE_UNIT_NAME = "FlavorDataPU";
    private static EntityManagerFactory emf;
    private static MwFlavorJpaController mwFlavorJpaController;
    private static MwFlavorgroupJpaController mwFlavorgroupJpaController;
    private final ObjectMapper mapper = JacksonObjectMapperProvider.createDefaultMapper();
        
    
    public MwFlavorJpaControllerTest() {
        mapper.setPropertyNamingStrategy(new PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy());
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
        String hostManifestAsJson = Resources.toString(Resources.getResource("rhel-host-manifest-test.json"), Charsets.UTF_8);
        HostManifest hostManifest = mapper.readValue(hostManifestAsJson, HostManifest.class);
        System.out.println(String.format("Successfully deserialized file to host manifest with host name: %s", hostManifest.getHostInfo().getHostName()));
        PlatformFlavorFactory factory = new PlatformFlavorFactory();
        PlatformFlavor platformFlavor = factory.getPlatformFlavor(hostManifest, null);
        Flavor flavorPlatform = mapper.readValue(platformFlavor.getFlavorPart(PLATFORM.getValue()).get(0), Flavor.class);
        Flavor flavorOs = mapper.readValue(platformFlavor.getFlavorPart(OS.getValue()).get(0), Flavor.class);
        Flavor flavorHostUnique = mapper.readValue(platformFlavor.getFlavorPart(HOST_UNIQUE.getValue()).get(0), Flavor.class);

        for (int i = 1; i <= 5; i++) {
            // PLATFORM
            MwFlavor mwFlavorPlatform = new MwFlavor();
            String uuidPlatform = new UUID().toString();
            flavorPlatform.getMeta().setId(uuidPlatform);
            mwFlavorPlatform.setId(flavorPlatform.getMeta().getId());
            String[] biosVersionArray = flavorPlatform.getMeta().getDescription().getBiosVersion().split("\\.");
            biosVersionArray[0] = Integer.toString(Integer.parseInt(biosVersionArray[0]) + i);
            String biosVersion = "";
            for (String v : biosVersionArray) { biosVersion = String.format("%s%s.", biosVersion, v); }
            biosVersion = StringUtils.strip(biosVersion, "\\.");
            flavorPlatform.getMeta().getDescription().setBiosVersion(biosVersion);
            mwFlavorPlatform.setContent(flavorPlatform);
            mwFlavorJpaController.create(mwFlavorPlatform);
            System.out.println(String.format("PLATFORM flavor [%s] created", mwFlavorPlatform.getId()));
            
            // OS
            MwFlavor mwFlavorOs = new MwFlavor();
            String uuidOs = new UUID().toString();
            flavorOs.getMeta().setId(uuidOs);
            mwFlavorOs.setId(flavorOs.getMeta().getId());
            mwFlavorOs.setContent(flavorOs);
            mwFlavorJpaController.create(mwFlavorOs);
            System.out.println(String.format("OS flavor [%s] created", mwFlavorOs.getId()));
            
//            // TAG
//            MwFlavor mwFlavorTag = new MwFlavor();
//            String uuidTag = new UUID().toString();
//            flavorTag.getMeta().setId(uuidTag);
//            mwFlavorTag.setId(flavorTag.getMeta().getId());
//            mwFlavorTag.setContent(flavorTag);
//            mwFlavorJpaController.create(mwFlavorTag);
//            System.out.println(String.format("ASSET_TAG flavor [%s] created", mwFlavorTag.getId()));
            
            // HOST_UNIQUE
            MwFlavor mwFlavorHostUnique = new MwFlavor();
            String uuidHostUnique = new UUID().toString();
            flavorHostUnique.getMeta().setId(uuidHostUnique);
            mwFlavorHostUnique.setId(flavorHostUnique.getMeta().getId());
            flavorHostUnique.getMeta().getDescription().setLabel(String.format("host-%d", i));
            mwFlavorHostUnique.setContent(flavorHostUnique);
            mwFlavorJpaController.create(mwFlavorHostUnique);
            System.out.println(String.format("HOST_UNIQUE flavor [%s] created", mwFlavorHostUnique.getId()));
        }
    }
    
    @Test
    public void readTestData() throws Exception {
        List<MwFlavor> mwFlavorEntries = mwFlavorJpaController.findMwFlavorEntities();
        for (MwFlavor mwFlavor : mwFlavorEntries) {
            String flavorId = mwFlavor.getId();
            System.out.println(String.format("Found flavor: %s", flavorId));
            String flavorPart = mwFlavor.getContent().getMeta().getDescription().getFlavorPart();
            System.out.println(String.format("   Flavor [%s] has flavor part: %s", flavorId, flavorPart));
            String flavorLabel = mwFlavor.getContent().getMeta().getDescription().getLabel();
            System.out.println(String.format("   Flavor [%s] has label: %s", flavorId, flavorLabel));
            String flavorBiosName = mwFlavor.getContent().getMeta().getDescription().getBiosName();
            System.out.println(String.format("   Flavor [%s] has PLATFORM name: %s", flavorId, flavorBiosName));
            String flavorBiosVersion = mwFlavor.getContent().getMeta().getDescription().getBiosVersion();
            System.out.println(String.format("   Flavor [%s] has PLATFORM version: %s", flavorId, flavorBiosVersion));
            String flavorTpmVersion = mwFlavor.getContent().getMeta().getDescription().getTpmVersion();
            System.out.println(String.format("   Flavor [%s] has TPM version: %s", flavorId, flavorTpmVersion));
            Flavor flavor = mwFlavor.getContent();
            System.out.println(String.format("   Flavor [%s] has content:\n %s", flavorId,
                    mapper.writeValueAsString(flavor)));
        }
    }
    
    @Test
    public void deleteAllData() throws Exception {
        List<MwFlavor> mwFlavorEntries = mwFlavorJpaController.findMwFlavorEntities();
        for (MwFlavor mwFlavor : mwFlavorEntries) {
            mwFlavorJpaController.destroy(mwFlavor.getId());
            System.out.println(String.format("Flavor [%s] deleted", mwFlavor.getId()));
        }
    }
    
    @Test
    public void getFlavorByKeyValue() throws Exception {
        List<MwFlavor> mwFlavorEntries = mwFlavorJpaController.findMwFlavorByKeyValue("label", "host-4");
        for (MwFlavor mwFlavor : mwFlavorEntries) {
            System.out.println(String.format("Flavor [%s] retrieved", mwFlavor.getId()));
        }        
    }
    
    @Test
    public void findFlavorsByName() throws Exception {
        String name = "host-1";
        MwFlavor mwFlavor = mwFlavorJpaController.findMwFlavorByName(name);

        if (mwFlavor == null) {
            System.out.println(String.format("Could not find any flavors with name: %s", name));
            return;
        }

        Flavor flavor = mwFlavor.getContent();
        System.out.println(String.format(
                "Found flavor [%s]:\n%s", flavor.getMeta().getId(), mapper.writeValueAsString(flavor)));
        System.out.println(String.format(
                "Flavor [%s] has name: %s", flavor.getMeta().getId(), flavor.getMeta().getDescription().getLabel()));
    }
    
    @Test
    public void findFlavorsByNameLike() throws Exception {
        String nameLike = "1";
        List<MwFlavor> mwFlavorEntries = mwFlavorJpaController.findMwFlavorByNameLike(nameLike);
        
        if (mwFlavorEntries == null) {
            System.out.println(String.format(
                    "Could not find any flavors with name pattern: %s", nameLike));
            return;
        }
        
        for (MwFlavor mwFlavor : mwFlavorEntries) {
            Flavor flavor = mwFlavor.getContent();
            System.out.println(String.format(
                    "Found flavor [%s]:\n%s", flavor.getMeta().getId(), mapper.writeValueAsString(flavor)));
            System.out.println(String.format(
                    "Flavor [%s] has name: %s", flavor.getMeta().getId(), flavor.getMeta().getDescription().getLabel()));
        }
    }
    
    @Test
    public void findFlavors() throws Exception {
        List<String> flavorTypes = Arrays.asList(PLATFORM.getValue(), OS.getValue(), HOST_UNIQUE.getValue());
        String hostManifestAsJson = Resources.toString(Resources.getResource("rhel-host-manifest-test.json"), Charsets.UTF_8);
        HostManifest hostManifest = mapper.readValue(hostManifestAsJson, HostManifest.class);
        hostManifest.getHostInfo().setBiosVersion("3.1.6");
        System.out.println(String.format(
                "Successfully deserialized file to host manifest with host name: %s",
                hostManifest.getHostInfo().getHostName()));
        
        MwFlavorgroup mwFlavorgroupAutomatic = mwFlavorgroupJpaController.findMwFlavorgroupByName(Flavorgroup.AUTOMATIC_FLAVORGROUP);
        List<MwFlavor> mwFlavorEntries = mwFlavorJpaController.findMwFlavorEntities(UUID.valueOf(mwFlavorgroupAutomatic.getId()), hostManifest, null);
//        List<MwFlavor> mwFlavorEntries = mwFlavorJpaController.findMwFlavorEntities(UUID.valueOf(mwFlavorgroupAutomatic.getId()), flavorTypes, null);
//        List<MwFlavor> mwFlavorEntries = mwFlavorJpaController.findMwFlavorEntities(UUID.valueOf(mwFlavorgroupAutomatic.getId()), null, null);
//        List<MwFlavor> mwFlavorEntries = mwFlavorJpaController.findMwFlavorEntities(null, flavorTypes, null);
//        List<MwFlavor> mwFlavorEntries = mwFlavorJpaController.findMwFlavorEntities(null, null, null);
//        List<MwFlavor> mwFlavorEntries = mwFlavorJpaController.findMwFlavorEntities(null, null, hostManifest);
//        List<MwFlavor> mwFlavorEntries = mwFlavorJpaController.findMwFlavorEntities(UUID.valueOf(mwFlavorgroupAutomatic.getId()), new ArrayList<String>(), hostManifest);
        
        if (mwFlavorEntries == null) {
            System.out.println(String.format("Could not find any flavors"));
            return;
        }
        
        for (MwFlavor mwFlavor : mwFlavorEntries) {
            Flavor flavor = mwFlavor.getContent();
            System.out.println(String.format("Found flavor:\n%s", mapper.writeValueAsString(flavor)));
        }
    }
    
    @Test
    public void hostHasUniqueFlavor() throws Exception {
//        String hostName = "host-1";
        String hardwareUuid = "00083153-d529-e511-906e-0012795d96dd";
        boolean hostHasUniqueFlavor = mwFlavorJpaController.hostHasUniqueFlavor(hardwareUuid, HOST_UNIQUE.getValue());
        if (!hostHasUniqueFlavor) {
            System.out.println(String.format(
                    "Could not find any unique flavors for host [%s]", hardwareUuid));
            return;
        }
        System.out.println(String.format("Host [%s] has unique flavor", hardwareUuid));
        
        boolean hostHasTagFlavor = mwFlavorJpaController.hostHasUniqueFlavor(hardwareUuid, ASSET_TAG.getValue());
        if (!hostHasTagFlavor) {
            System.out.println(String.format(
                    "Could not find any tag flavors for host [%s]", hardwareUuid));
            return;
        }
        System.out.println(String.format("Host [%s] has tag flavor", hardwareUuid));
    }
    
    @Test
    public void flavorgroupContainsFlavorType() throws Exception {
//        List<String> flavorTypes = Arrays.asList("PLATFORM", "OS", "TEST");
//        List<String> flavorTypes = new ArrayList<>();
        List<String> flavorTypes = null;
        MwFlavorgroup mwFlavorgroupAutomatic = mwFlavorgroupJpaController.findMwFlavorgroupByName(Flavorgroup.AUTOMATIC_FLAVORGROUP);
        
        List<String> flavorTypesInFlavorGroup = new ArrayList<>();
        for (String flavorType : flavorTypes) {
            boolean flavorgroupContainsFlavorType =
                    mwFlavorJpaController.flavorgroupContainsFlavorType(
                            UUID.valueOf(mwFlavorgroupAutomatic.getId()), flavorType);
            if (flavorgroupContainsFlavorType) {
                System.out.println(String.format(
                        "Flavorgroup [%s] contains flavor type %s",
                        mwFlavorgroupAutomatic.getName(), flavorType));
                flavorTypesInFlavorGroup.add(flavorType);
            }
        }
        
        if (flavorTypesInFlavorGroup.isEmpty()) {
            System.out.println(String.format(
                    "Flavorgroup [%s] does NOT contain flavor types %s",
                    mwFlavorgroupAutomatic.getName(), flavorTypes.toString()));
            return;
        }
        System.out.println(String.format(
                "Flavorgroup [%s] contains flavor types: %s",
                mwFlavorgroupAutomatic.getName(), flavorTypesInFlavorGroup.toString()));
    }
}
