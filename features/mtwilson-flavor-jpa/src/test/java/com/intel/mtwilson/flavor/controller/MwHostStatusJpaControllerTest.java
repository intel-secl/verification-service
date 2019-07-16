/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.controller;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.intel.dcsg.cpg.extensions.Extensions;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.validation.Fault;
import com.intel.mtwilson.flavor.data.MwHost;
import com.intel.mtwilson.flavor.data.MwHostStatus;
import com.intel.mtwilson.flavor.model.HostStatusInformation;
import com.intel.mtwilson.i18n.HostState;
import com.intel.mtwilson.jackson.bouncycastle.BouncyCastleModule;
import com.intel.mtwilson.jackson.validation.ValidationModule;
import com.intel.mtwilson.jaxrs2.provider.JacksonObjectMapperProvider;
import com.intel.mtwilson.core.common.model.HostManifest;
import static java.lang.Thread.sleep;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.spi.PersistenceUnitTransactionType;
import org.apache.shiro.codec.Base64;
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
public class MwHostStatusJpaControllerTest {
    private static final Logger log = LoggerFactory.getLogger(MwHostStatusJpaControllerTest.class);
    
    private static final String JAVAX_PERSISTENCE_JDBC_DRIVER = "org.postgresql.Driver";
    private static final String JAVAX_PERSISTENCE_JDBC_URL = "jdbc:postgresql://192.168.0.1:5432/mw_as";
    private static final String JAVAX_PERSISTENCE_JDBC_USER = "root";
    private static final String JAVAX_PERSISTENCE_JDBC_PASSWORD = "Kt0pWmXqZiOyF1SXKfsPOg";
    
    private static final String PERSISTENCE_UNIT_NAME = "FlavorDataPU";
    private static EntityManagerFactory emf;
    private static MwHostStatusJpaController mwHostStatusJpaController;
    private final ObjectMapper mapper = JacksonObjectMapperProvider.createDefaultMapper();
    
    public MwHostStatusJpaControllerTest() {
    }
    
    private static void registerJacksonModules() {
        Extensions.register(Module.class, BouncyCastleModule.class);
        Extensions.register(Module.class, ValidationModule.class);
    }
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        registerJacksonModules();
        Properties jpaProperties = new Properties();
        jpaProperties.put(TRANSACTION_TYPE, PersistenceUnitTransactionType.RESOURCE_LOCAL.name());
        jpaProperties.put(JDBC_DRIVER, JAVAX_PERSISTENCE_JDBC_DRIVER);
        jpaProperties.put(JDBC_URL, JAVAX_PERSISTENCE_JDBC_URL);
        jpaProperties.put(JDBC_USER, JAVAX_PERSISTENCE_JDBC_USER);
        jpaProperties.put(JDBC_PASSWORD, JAVAX_PERSISTENCE_JDBC_PASSWORD);
        
        log.debug("Loading database driver {} for persistence unit {}",  jpaProperties.getProperty("javax.persistence.jdbc.driver"), PERSISTENCE_UNIT_NAME);
        emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, jpaProperties);
        mwHostStatusJpaController = new MwHostStatusJpaController(emf);
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
        MwHostJpaController mwHostJpaController = new MwHostJpaController(emf);
        List<MwHost> mwHostList = mwHostJpaController.findMwHostEntities();
        HostStatusInformation hostStatus = new HostStatusInformation();
        for (MwHost mwHost : mwHostList) {
            for (int i = 1; i <= 5; i++) {
                String hostManifestAsJson = Resources.toString(Resources.getResource("rhel-host-manifest-test.json"), Charsets.UTF_8);
                HostManifest hostManifest = mapper.readValue(hostManifestAsJson, HostManifest.class);
                hostManifest.getHostInfo().setHostName(mwHost.getName());
                hostManifest.getHostInfo().setHardwareUuid(mwHost.getHardwareUuid());
                hostStatus.setHostState(HostState.QUEUE);
                hostStatus.setLastTimeConnected(Calendar.getInstance().getTime());
//                hostStatus.setFaults(getFaults());
                MwHostStatus mwHostStatus = new MwHostStatus();
                mwHostStatus.setId(new UUID().toString());
                mwHostStatus.setHostId(mwHost.getId());
                mwHostStatus.setStatus(hostStatus);
                mwHostStatus.setCreated(Calendar.getInstance().getTime());
                mwHostStatus.setHostManifest(hostManifest);
                mwHostStatusJpaController.create(mwHostStatus);
                System.out.println(String.format("Host status [%s] for host [%s] created with status: %s",
                        mwHostStatus.getId(), mwHostStatus.getHostId(), mwHostStatus.getStatus()));
                sleep(2000);
            }
        }
    }
    
    @Test
    public void readTestData() throws Exception {
        List<MwHostStatus> mwHostStatusList = mwHostStatusJpaController.findMwHostStatusEntities();
        for (MwHostStatus mwHostStatus : mwHostStatusList) {
            String hostStatusId = mwHostStatus.getId();
            System.out.println(String.format("Found host status: %s", hostStatusId));
            String hostId = mwHostStatus.getHostId();
            System.out.println(String.format("   Host status [%s] has host ID: %s", hostStatusId, hostId));
            HostStatusInformation hostStatus = mwHostStatus.getStatus();
            ObjectMapper mapper = JacksonObjectMapperProvider.createDefaultMapper();
            //System.out.println(String.format("   Host status [%s] has status: \n %s", hostStatusId, mapper.writeValueAsString(hostStatus)));
            Date createdTimeStamp = mwHostStatus.getCreated();
            System.out.println(String.format("   Host status [%s] has created date: %s", hostStatusId, createdTimeStamp));
            HostManifest hostManifest = mwHostStatus.getHostManifest(); 
            mapper.setPropertyNamingStrategy(new PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy());
            System.out.println(String.format("   Host status [%s] has host manifest:\n %s", hostStatusId,
                    mapper.writeValueAsString(hostManifest)));
        }
    }
    
    @Test
    public void deleteAllData() throws Exception {
        List<MwHostStatus> mwHostStatusList = mwHostStatusJpaController.findMwHostStatusEntities();
        for (MwHostStatus mwHostStatus : mwHostStatusList) {
            mwHostStatusJpaController.destroy(mwHostStatus.getId());
            System.out.println(String.format("Host Status [%s] deleted", mwHostStatus.getId()));
        }
    }
    
    @Test
    public void retrieveHostStatusForAllHosts() throws Exception {
        MwHostJpaController mwHostJpaController = new MwHostJpaController(emf);
        List<MwHost> mwHostList = mwHostJpaController.findMwHostEntities();
        for (MwHost mwHost : mwHostList) {
            MwHostStatus mwHostStatus = mwHostStatusJpaController.findMwHostStatusByHostId(mwHost.getId());
            if (mwHostStatus == null) {
                System.out.println(String.format("No entry found for host [%s]", mwHost.getId()));
                return;
            }
            System.out.println(String.format(
                    "Host [%s] created on [%s] has host status:",
                    mwHost.getId(), mwHostStatus.getCreated()));
        }
    }
    
    @Test
    public void getHostsByKeyValue() throws Exception {
        List<String> mwHostEntries = mwHostStatusJpaController.findMwHostListByKeyValue("os_name", "RHEL");
        if (mwHostEntries == null || mwHostEntries.isEmpty())
            throw new Exception("No matching hosts found");
        for (String hostId : mwHostEntries) {
            System.out.println(String.format("Host [%s] retrieved", hostId));
        }        
    }
    
    public List<Fault> getFaults() {
        ArrayList<Fault> faults = new ArrayList<>();
        faults.add(new Fault("Test Description"));
        return faults;
    }
    
    @Test
    public void retrieveHostStatusByAikCertificate() throws Exception {
        String hostManifestAsJson = Resources.toString(Resources.getResource("rhel-host-manifest-test.json"), Charsets.UTF_8);
        HostManifest hostManifest = mapper.readValue(hostManifestAsJson, HostManifest.class);
        String aikCertificate = Base64.encodeToString(hostManifest.getAikCertificate().getEncoded());
        System.out.println(String.format("AIK certificate:\n%s", aikCertificate));
        
        MwHostStatus mwHostStatus = mwHostStatusJpaController.findMwHostStatusByAikCertificate(aikCertificate);
        if (mwHostStatus == null) {
            System.out.println(String.format("No entry found for aik certificate:\n%s", aikCertificate));
            return;
        }
        System.out.println(String.format("Host [%s] created on [%s] has ID: %s",
                mwHostStatus.getHostId(), mwHostStatus.getCreated(), mwHostStatus.getId()));
    }
}
