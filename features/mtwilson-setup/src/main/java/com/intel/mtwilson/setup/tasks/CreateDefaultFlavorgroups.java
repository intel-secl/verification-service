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
import com.intel.mtwilson.flavor.rest.v2.repository.FlavorRepository;
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
    private final String automaticFlavorgroupName = "mtwilson_automatic";
    private final String uniqueFlavorgroupName = "mtwilson_unique";
    
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
        FlavorgroupFilterCriteria automaticFlavorgroupFilterCriteria = new FlavorgroupFilterCriteria();
        automaticFlavorgroupFilterCriteria.nameEqualTo = automaticFlavorgroupName;
        FlavorgroupCollection automaticFlavorgroups
                = new FlavorgroupRepository().search(automaticFlavorgroupFilterCriteria);
        
        // validation fault if the automatic flavorgroup does not exist
        if (automaticFlavorgroups == null || automaticFlavorgroups.getFlavorgroups() == null
                || automaticFlavorgroups.getFlavorgroups().isEmpty()) {
            validation("Automatic flavorgroup does not exist");
        }
        
        // look for unique flavorgroup
        FlavorgroupFilterCriteria uniqueFlavorgroupFilterCriteria = new FlavorgroupFilterCriteria();
        uniqueFlavorgroupFilterCriteria.nameEqualTo = uniqueFlavorgroupName;
        FlavorgroupCollection uniqueFlavorgroups
                = new FlavorgroupRepository().search(uniqueFlavorgroupFilterCriteria);
        
        // validation fault if the automatic flavorgroup does not exist
        if (uniqueFlavorgroups == null || uniqueFlavorgroups.getFlavorgroups() == null
                || uniqueFlavorgroups.getFlavorgroups().isEmpty()) {
            validation("Unique flavorgroup does not exist");
        }
    }

    @Override
    protected void execute() throws Exception {
        // create the automatic and unique flavorgroups
        createFlavorgroup(automaticFlavorgroupName, new FlavorRepository().createAutomaticFlavorMatchPolicy());
        createFlavorgroup(uniqueFlavorgroupName, null);
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
    
    private Flavorgroup createFlavorgroup(String flavorgroupName, FlavorMatchPolicyCollection policy) {
        // look for flavorgroup
        FlavorgroupFilterCriteria flavorgroupFilterCriteria = new FlavorgroupFilterCriteria();
        flavorgroupFilterCriteria.nameEqualTo = flavorgroupName;
        FlavorgroupCollection flavorgroups
                = new FlavorgroupRepository().search(flavorgroupFilterCriteria);
        
        // create the automatic flavorgroup if it is not already there
        if (flavorgroups == null || flavorgroups.getFlavorgroups() == null
                || flavorgroups.getFlavorgroups().isEmpty()) {
            Flavorgroup newFlavorgroup = new Flavorgroup();
            newFlavorgroup.setName(flavorgroupName);
            newFlavorgroup.setFlavorMatchPolicyCollection(policy);
            return new FlavorgroupRepository().create(newFlavorgroup);
        }
        log.debug("Flavorgroup [{}] already exists", flavorgroupName);
        return flavorgroups.getFlavorgroups().get(0);
    }
}
