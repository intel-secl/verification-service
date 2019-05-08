/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.setup.tasks;

import com.intel.dcsg.cpg.jpa.PersistenceManager;
import com.intel.mtwilson.My;
import com.intel.mtwilson.MyPersistenceManager;
import com.intel.mtwilson.flavor.model.FlavorMatchPolicyCollection;
import com.intel.mtwilson.flavor.rest.v2.model.Flavorgroup;
import com.intel.mtwilson.flavor.rest.v2.model.FlavorgroupCollection;
import com.intel.mtwilson.flavor.rest.v2.model.FlavorgroupFilterCriteria;
import com.intel.mtwilson.flavor.rest.v2.repository.FlavorgroupRepository;
import com.intel.mtwilson.setup.LocalSetupTask;
import com.intel.mtwilson.setup.SetupException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import javax.sql.DataSource;

/**
 *
 * @author rksavino
 */
public class CreateDefaultFlavorgroups extends LocalSetupTask {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CreateDefaultFlavorgroups.class);
    private String databaseDriver;
    private String databaseUrl;
    private String databaseVendor;


    public CreateDefaultFlavorgroups() { }
    
    @Override
    protected void configure() throws Exception {
        databaseDriver = My.jdbc().driver();
        if( databaseDriver == null ) {
            configuration("Database driver not configured");
        }
        else {
            log.debug("Database driver: {}", databaseDriver);
        }
        databaseUrl = My.jdbc().url();
        if( databaseUrl == null ) {
            configuration("Database URL not configured");
        }
        else {
            log.debug("Database URL: {}", databaseUrl); 
        }
        databaseVendor = My.configuration().getDatabaseProtocol();
        if( databaseVendor == null ) {
            configuration("Database vendor not configured");
        }
        else {
            log.debug("Database vendor: {}", databaseVendor);
        }
    }
    
    @Override
    protected void validate() throws Exception {
        if(!testConnection()) {
            return;
        }
        
        DataSource ds = getDataSource();
        log.debug("Connecting to {}", databaseVendor);
        // username and password should already be set in the datasource
        try (Connection c = ds.getConnection() ){ }
        catch(SQLException e) {
            log.error("Failed to connect to {} with schema: error = {}", databaseVendor, e.getMessage()); 
                validation("Cannot connect to database");
                return;
        }
        
        // look for automatic flavorgroup
        FlavorgroupCollection automaticFlavorgroups = getFlavorgroupCollection(Flavorgroup.AUTOMATIC_FLAVORGROUP);
        
        // validation fault if the automatic flavorgroup does not exist
        if (isFlavorgroupCollectionEmpty(automaticFlavorgroups)) {
            validation("Automatic flavorgroup does not exist");
        } else if (!Flavorgroup.getAutomaticFlavorMatchPolicy().equals(automaticFlavorgroups.getFlavorgroups().get(0).getFlavorMatchPolicyCollection())) {
            validation("Automatic flavorgroup policy does not match");
        }
        
        // look for host_unique flavorgroup
        FlavorgroupCollection uniqueFlavorgroups = getFlavorgroupCollection(Flavorgroup.HOST_UNIQUE_FLAVORGROUP);
        
        // validation fault if the host_unique flavorgroup does not exist
        if (isFlavorgroupCollectionEmpty(uniqueFlavorgroups)) {
            validation("host_unique flavorgroup does not exist");
        }

        // look for platform_software flavorgroup
        FlavorgroupCollection defaultSoftwareFlavorgroups = getFlavorgroupCollection(Flavorgroup.PLATFORM_SOFTWARE_FLAVORGROUP);

        // validation fault if the platform_software flavorgroup does not exist
        if (isFlavorgroupCollectionEmpty(defaultSoftwareFlavorgroups)) {
            validation("Default software flavorgroup does not exist");
        } else if (!Flavorgroup.getIseclSoftwareFlavorMatchPolicy().equals(defaultSoftwareFlavorgroups.getFlavorgroups().get(0).getFlavorMatchPolicyCollection())) {
            validation("Default software flavorgroup policy does not match");
        }

        // look for workload_software flavorgroup
        FlavorgroupCollection defaultWorkloadSoftwareFlavorgroups = getFlavorgroupCollection(Flavorgroup.WORKLOAD_SOFTWARE_FLAVORGROUP);

        // validation fault if the workload_software flavorgroup does not exist
        if (isFlavorgroupCollectionEmpty(defaultWorkloadSoftwareFlavorgroups)) {
            validation("Default workload software flavorgroup does not exist");
        } else if (!Flavorgroup.getIseclSoftwareFlavorMatchPolicy().equals(defaultWorkloadSoftwareFlavorgroups.getFlavorgroups().get(0).getFlavorMatchPolicyCollection())) {
            validation("Default workload software flavorgroup policy does not match");
        }
    }

    private boolean isFlavorgroupCollectionEmpty(FlavorgroupCollection automaticFlavorgroups) {
        return automaticFlavorgroups == null || automaticFlavorgroups.getFlavorgroups() == null
                || automaticFlavorgroups.getFlavorgroups().isEmpty();
    }

    private FlavorgroupCollection getFlavorgroupCollection(String automaticFlavorgroup) {
        FlavorgroupFilterCriteria automaticFlavorgroupFilterCriteria = new FlavorgroupFilterCriteria();
        automaticFlavorgroupFilterCriteria.nameEqualTo = automaticFlavorgroup;
        return new FlavorgroupRepository().search(automaticFlavorgroupFilterCriteria);
    }

    @Override
    protected void execute() throws Exception {
        // create the automatic, host_unique, platform_software and workload_software flavorgroups
        createOrUpdateFlavorgroup(Flavorgroup.AUTOMATIC_FLAVORGROUP, Flavorgroup.getAutomaticFlavorMatchPolicy());
        createOrUpdateFlavorgroup(Flavorgroup.HOST_UNIQUE_FLAVORGROUP, null);
        createOrUpdateFlavorgroup(Flavorgroup.PLATFORM_SOFTWARE_FLAVORGROUP, Flavorgroup.getIseclSoftwareFlavorMatchPolicy());
        createOrUpdateFlavorgroup(Flavorgroup.WORKLOAD_SOFTWARE_FLAVORGROUP, Flavorgroup.getIseclSoftwareFlavorMatchPolicy());
    }
    
    private boolean testConnection() {
        try {
            try (Connection c = My.jdbc().connection(); Statement s = c.createStatement()) {
                s.executeQuery("SELECT 1"); 
            }
            return true;
        }
        catch(Exception e) {
            log.error("Cannot connect to database", e);
            validation("Cannot connect to database");
            return false;
        }
    }
    
    /**
     * 
     * @return datasource object for mt wilson database, guaranteed non-null
     * @throws SetupException if the datasource cannot be obtained
     */
    private DataSource getDataSource() throws SetupException {
        try {
            Properties jpaProperties = MyPersistenceManager.getJpaProperties(My.configuration());
            
            log.debug("JDBC URL with schema: {}", jpaProperties.getProperty("javax.persistence.jdbc.url"));
            if( jpaProperties.getProperty("javax.persistence.jdbc.url") == null ) {
                log.error("Missing database connection settings");
                System.exit(1);
            }
            DataSource ds = PersistenceManager.getPersistenceUnitInfo("FlavorDataPU", jpaProperties).getNonJtaDataSource();
            if( ds == null ) {
                log.error("Cannot load persistence unit info");
                System.exit(2);
            }
            log.debug("Loaded persistence unit: FlavorDataPU");
            return ds;
        }
        catch(IOException e) {
            throw new SetupException("Cannot load persistence unit info", e);
        }   
    }

    private Flavorgroup createOrUpdateFlavorgroup(String flavorgroupName, FlavorMatchPolicyCollection expectedPolicy) {
        // look for flavorgroup
        FlavorgroupCollection flavorgroups = getFlavorgroupCollection(flavorgroupName);
        Flavorgroup flavorgroup;
        // create flavorgroup if it is not already there
        if (isFlavorgroupCollectionEmpty(flavorgroups)) {
            flavorgroup = createFlavorgroup(flavorgroupName, expectedPolicy);
        } else {
            flavorgroup = flavorgroups.getFlavorgroups().get(0);
            if (expectedPolicy != null && !expectedPolicy.equals(flavorgroup.getFlavorMatchPolicyCollection())) {
                log.debug("Update Flavorgroup [{}] for latest policies", flavorgroupName);
                flavorgroup = updateFlavorgroup(expectedPolicy, flavorgroup);
            }
        }
        return flavorgroup;
    }

    private Flavorgroup updateFlavorgroup(FlavorMatchPolicyCollection expectedPolicy, Flavorgroup flavorgroup) {
        flavorgroup.setFlavorMatchPolicyCollection(expectedPolicy);
        return new FlavorgroupRepository().store(flavorgroup);
    }

    private Flavorgroup createFlavorgroup(String flavorgroupName, FlavorMatchPolicyCollection policy) {
        Flavorgroup newFlavorgroup = new Flavorgroup();
        newFlavorgroup.setName(flavorgroupName);
        newFlavorgroup.setFlavorMatchPolicyCollection(policy);
        return new FlavorgroupRepository().create(newFlavorgroup);
    }
}
