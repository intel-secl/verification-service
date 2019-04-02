/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson;

import com.intel.mtwilson.flavor.controller.*;
import com.intel.mtwilson.telemetry.controller.*;
import java.io.IOException;

/**
 * Convenience class to instantiate JPA controllers for the purpose of writing
 * JUnit tests... Using this class allows you to eliminate a lot of boilerplate
 * from your tests.
 *
 * Instead of writing this:
 *
 * TblHostsJpaController hosts = new
 * TblHostsJpaController(ASPersistenceManager.createEntityManagerFactory("ASDataPU",
 * ASConfig.getJpaProperties())); (and that only works when it executes on a
 * server with /etc/intel/cloudsecurity/attestation-service.properties)
 *
 * You write this:
 *
 * TblHostsJpaController hosts = My.jpa().mwHosts();
 *
 * The naming convention is that given a table name like mw_api_client_x509, the
 * method name is chosen by removing underscores, and capitalizing the first
 * letter of every word after the first one. So the method to get the
 * corresponding JPA controller in this example would be mwApiClientX509().
 *
 *
 * @author jbuhacoff
 */
public class MyJpa {
    private final MyPersistenceManager pm;
    
    public MyJpa(MyPersistenceManager pm) {
        this.pm = pm;
    }
    
    public MwFlavorJpaController mwFlavor() throws IOException {
        return new MwFlavorJpaController(pm.getFlavorData());
    }
    
    public MwFlavorgroupJpaController mwFlavorgroup() throws IOException {
        return new MwFlavorgroupJpaController(pm.getFlavorData());
    }
    
    public MwLinkFlavorFlavorgroupJpaController mwLinkFlavorFlavorgroup() throws IOException {
        return new MwLinkFlavorFlavorgroupJpaController(pm.getFlavorData());
    }
    
    public MwHostJpaController mwHost() throws IOException {
        return new MwHostJpaController(pm.getFlavorData());
    }
    
    public MwHostStatusJpaController mwHostStatus() throws IOException {
        return new MwHostStatusJpaController(pm.getFlavorData());
    }
    
    public MwLinkFlavorHostJpaController mwLinkFlavorHost() throws IOException {
        return new MwLinkFlavorHostJpaController(pm.getFlavorData());
    }
    
    public MwLinkFlavorgroupHostJpaController mwLinkFlavorgroupHost() throws IOException {
        return new MwLinkFlavorgroupHostJpaController(pm.getFlavorData());
    }
    
    public MwReportJpaController mwReport() throws IOException {
        return new MwReportJpaController(pm.getFlavorData());
    }
    
    public MwQueueJpaController mwQueue() throws IOException {
        return new MwQueueJpaController(pm.getFlavorData());
    }
    
    public MwHostCredentialJpaController mwHostCredential() throws IOException {
        return new MwHostCredentialJpaController(pm.getFlavorData());
    }
    
    public MwTelemetryJpaController mwTelemetry() throws IOException {
        return new MwTelemetryJpaController(pm.getTelemetryData());
    }
}