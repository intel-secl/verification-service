/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.controller;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.intel.dcsg.cpg.extensions.Extensions;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.core.verifier.policy.TrustReport;
import com.intel.mtwilson.flavor.data.MwHost;
import com.intel.mtwilson.flavor.data.MwReport;
import com.intel.mtwilson.jackson.bouncycastle.BouncyCastleModule;
import com.intel.mtwilson.jackson.validation.ValidationModule;
import com.intel.mtwilson.jaxrs2.provider.JacksonObjectMapperProvider;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
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
public class MwReportJpaControllerTest {

    private static final Logger log = LoggerFactory.getLogger(MwReportJpaControllerTest.class);

    private static final String JAVAX_PERSISTENCE_JDBC_DRIVER = "org.postgresql.Driver";
    private static final String JAVAX_PERSISTENCE_JDBC_URL = "jdbc:postgresql://192.168.0.1:5432/mw_as";
    private static final String JAVAX_PERSISTENCE_JDBC_USER = "root";
    private static final String JAVAX_PERSISTENCE_JDBC_PASSWORD = "password";

    private static final String PERSISTENCE_UNIT_NAME = "FlavorDataPU";
    private static EntityManagerFactory emf;
    private static MwReportJpaController mwReportJpaController;

    public MwReportJpaControllerTest() {
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

        log.debug("Loading database driver {} for persistence unit {}", jpaProperties.getProperty("javax.persistence.jdbc.driver"), PERSISTENCE_UNIT_NAME);
        emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, jpaProperties);
        mwReportJpaController = new MwReportJpaController(emf);
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

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        Date created, expiration;

        String hostId = new UUID().toString();

        for (int i = 1; i <= 10; i++) {

            MwReport obj = new MwReport();
            obj.setId(new UUID().toString());
            if ((i % 3) == 0) {
                hostId = new UUID().toString();
            }
            obj.setHostId(hostId);
            
            ObjectMapper mapper = JacksonObjectMapperProvider.createDefaultMapper();
            mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            String trustReportInString = Resources.toString(Resources.getResource("trustreport.json"), Charsets.UTF_8);
            TrustReport combinedTrustReport = mapper.readValue(trustReportInString, TrustReport.class);
            obj.setTrustReport(combinedTrustReport);
                    
            obj.setSaml("This is SAML report");

            created = new Date(); // Get the current date and time
            cal.setTime(created);
            created = dateFormat.parse(dateFormat.format(cal.getTime()));

            cal.add(Calendar.HOUR, i);
            expiration = dateFormat.parse(dateFormat.format(cal.getTime()));

            obj.setCreated(created);
            obj.setExpiration(expiration);
            mwReportJpaController.create(obj);

            //Thread.sleep(5000);
        }
    }

    @Test
    public void readData() throws Exception {

        List<String> hostIds = Arrays.asList("b90b7e3e-b702-4a89-90ad-8c61eb7fade4", "c88aa6ea-c816-40c3-bf71-54a077d980a7");
        String quotedHostIds = String.join(",", hostIds).replaceAll("([^,]+)", "\'$1\'");
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        Date toDate, fromDate;

        toDate = new Date(); // Get the current date and time
        cal.setTime(toDate);
        cal.add(Calendar.HOUR, -(16));
        toDate = dateFormat.parse(dateFormat.format(cal.getTime()));
        // To get the fromDate, we substract the number of days fromm the current date.
        cal.add(Calendar.DATE, -(1));
        fromDate = dateFormat.parse(dateFormat.format(cal.getTime()));

        List<MwReport> reports = mwReportJpaController.findMwReport(null, null, null, null, null, fromDate, toDate, false, 50);
        if (reports == null) {
            System.out.println("No reports found for the hosts specified");
            return;
        }

        for (MwReport report : reports) {
            System.out.println(report.getHostId() + "-" + report.getCreated() + "-" + report.getExpiration());
        }

        reports = mwReportJpaController.findMwReport(null, null, null, null, null, fromDate, toDate, false, 50);
        if (reports == null) {
            System.out.println("No reports found for the hosts specified");
            return;
        }

        for (MwReport report : reports) {
            System.out.println(report.getHostId() + "-" + report.getCreated() + "-" + report.getExpiration());
        }

    }
    
    @Test
    public void fetchJsonData() throws Exception {
        List<MwReport> reports = mwReportJpaController.findMwReportEntities();
        for (MwReport report : reports){
            System.out.println(report.getTrustReport().getPolicyName());
        }
    }
    
    @Test
    public void retrieveReportForAllHosts() throws Exception {
        MwHostJpaController mwHostJpaController = new MwHostJpaController(emf);
        List<MwHost> mwHostList = mwHostJpaController.findMwHostEntities();
        for (MwHost mwHost : mwHostList) {
            MwReport mwReport = mwReportJpaController.findMwReportByHostId(mwHost.getId());
            if (mwReport == null) {
                System.out.println(String.format("No entry found for host [%s]", mwHost.getId()));
                return;
            }
            System.out.println(String.format(
                    "Host [%s] created on [%s] has latest trust value: %s",
                    mwHost.getId(), mwReport.getCreated(), mwReport.getTrustReport().isTrusted()));
        }
    }
}
