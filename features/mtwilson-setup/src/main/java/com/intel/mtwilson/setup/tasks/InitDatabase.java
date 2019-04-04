/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.setup.tasks;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import com.intel.dcsg.cpg.jpa.PersistenceManager;
import com.intel.mtwilson.My;
import com.intel.mtwilson.MyPersistenceManager;
import com.intel.mtwilson.setup.LocalSetupTask;
import com.intel.mtwilson.setup.SetupException;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.sql.DataSource;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author jbuhacoff
 */
public class InitDatabase extends LocalSetupTask {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(InitDatabase.class);

    private String databaseHost;
    private String databasePort;
    private String databaseDriver;
    private String databaseUsername;
    private String databasePassword;
    private String databaseUrl;
    private String databaseVendor;
            
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
        if( testConnection() ) {
            checkAvailableUpdates();
        }
    }

    @Override
    protected void execute() throws Exception {
        initDatabase();
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

    public static class ChangelogEntry {
        public String id;
        public String applied_at;
        public String description;
        public ChangelogEntry() { }
        public ChangelogEntry(String id, String applied_at, String description) {
            this.id = id;
            this.applied_at = applied_at;
            this.description = description;
        }
    }
    
    private void verbose(String format, Object... args) {
        log.debug(String.format(format, args));
    }
    
    private Map<Long,Resource> sql;
    private HashSet<Long> changesToApply;
    
    private void checkAvailableUpdates() throws SetupException, IOException, SQLException {
        log.debug("Loading SQL for {}", databaseVendor);
        sql = getSql(databaseVendor); 
        
        DataSource ds = getDataSource();
        
        log.debug("Connecting to {}", databaseVendor);
        Connection c;
        try {
            c = ds.getConnection();  // username and password should already be set in the datasource
        }
        catch(SQLException e) {
            log.error("Failed to connect to {} with schema: error = {}", databaseVendor, e.getMessage()); 
                validation("Cannot connect to database");
                return;
            // it's possible that the database connection is fine but the SCHEMA doesn't exist... so try connecting w/o a schema
        }
        
        List<ChangelogEntry> changelog = getChangelog(c);
        HashMap<Long,ChangelogEntry> presentChanges = new HashMap<>(); // what is already in the database according to the changelog
        verbose("Existing database changelog has %d entries", changelog.size());
        for(ChangelogEntry entry : changelog) {
            if( entry != null ) { 
                verbose("%s %s %s", entry.id, entry.applied_at, entry.description); 
                presentChanges.put(Long.valueOf(entry.id), entry);
            }
        }

        // Does it have any changes that we don't?  In other words, is the database schema newer than what we know in this installer?
            HashSet<Long> unknownChanges = new HashSet<>(presentChanges.keySet()); // list of what is in database
            unknownChanges.removeAll(sql.keySet()); // remove what we have in this installer
            if( unknownChanges.isEmpty() ) {
                log.info("Database is compatible");
            }
            else {
                // Database has new schema changes we dont' know about
                log.warn("Database schema is newer than this version of Mt Wilson");
                ArrayList<Long> unknownChangesInOrder = new ArrayList<>(unknownChanges);
                Collections.sort(unknownChangesInOrder);
                for(Long unknownChangeId : unknownChangesInOrder) {
                    ChangelogEntry entry = presentChanges.get(unknownChangeId);
                    log.info(String.format("%s %s %s", entry.id, entry.applied_at, entry.description));
                }
                validation("Database is not compatible");
                return;
            }

        changesToApply = new HashSet<>(sql.keySet());
        changesToApply.removeAll(presentChanges.keySet());
        
        if( changesToApply.isEmpty() ) {
            log.info("No database updates available");
        }
        else {
            validation("There are %s database updates to apply", changesToApply.size());
        }
        
    }

    /**
     * Must call checkAvailableUpdates() first in order to initialize the 
     * "sql" and "changesToApply" member variables
     * 
     * @throws SetupException
     * @throws IOException
     * @throws SQLException 
     */
    private void initDatabase() throws SetupException, IOException, SQLException {
        DataSource ds = getDataSource();
        
        log.debug("Connecting to {}", databaseVendor);
        try (Connection c = ds.getConnection()) {
            ArrayList<Long> changesToApplyInOrder = new ArrayList<>(changesToApply);
            Collections.sort(changesToApplyInOrder);
            
            
                log.info("The following changes will be applied:");
                        for(Long changeId : changesToApplyInOrder) {
                            log.info("Change ID: {}", changeId);
                        }
            ResourceDatabasePopulator rdp = new ResourceDatabasePopulator();
            // removing unneeded output as user can't choice what updates to apply
            for(Long id : changesToApplyInOrder) {
                rdp.addScript(sql.get(id));
            }
            
            rdp.setContinueOnError(true);
            rdp.setIgnoreFailedDrops(true);
            rdp.setSeparator(";");
            rdp.populate(c);
        }  catch(SQLException e) {
            log.error("Failed to connect to {} with schema: error = {}", databaseVendor, e.getMessage()); 
                validation("Cannot connect to database");
        }

    }

    /**
     * Locates the SQL files for the specified vendor, and reads them to
     * create a mapping of changelog-date to SQL content. This mapping can
     * then be used to select which files to execute against an existing
     * database.
     * See also iBatis, which we are (very) roughly emulating.
     * @param databaseVendor
     * @return 
     */
    private Map<Long,Resource> getSql(String databaseVendor) throws SetupException {
        System.out.println("Scanning for "+databaseVendor+" SQL files");
        HashMap<Long,Resource> sqlmap = new HashMap<>();
        try {
            Resource[] list = listResources(databaseVendor); // each URL like: jar:file:/C:/Users/jbuhacof/workspace/mountwilson-0.5.4/desktop/setup-console/target/setup-console-0.5.4-SNAPSHOT-with-dependencies.jar!/com/intel/mtwilson/database/mysql/20121226000000_remove_created_by_patch_rc3.sql
            for(Resource resource : list) {
                URL url = resource.getURL();
                Long timestamp = getTimestampFromSqlFilename(basename(url));
                if( timestamp != null ) {
                    sqlmap.put(timestamp, resource);
                }
                else {
                    log.error("SQL filename is not in recognized format: "+url.toExternalForm());
                }
            }
        }
        catch(IOException e) {
            throw new SetupException("Error while scanning for SQL files: "+e.getLocalizedMessage(), e);
        }
        return sqlmap;
    }
    
    private Resource[] listResources(String databaseVendor) throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(getClass().getClassLoader());
        Resource[] resources = resolver.getResources("classpath:com/intel/mtwilson/database/"+databaseVendor+"/*.sql");
        return resources; 
    }
    
    Pattern pTimestampName = Pattern.compile("^([0-9]+).*");
    
    /**
     * Given a URL, returns the final component filename
     * 
     * Example URL: jar:file:/C:/Users/jbuhacof/workspace/mountwilson-0.5.4/desktop/setup-console/target/setup-console-0.5.4-SNAPSHOT-with-dependencies.jar!/com/intel/mtwilson/database/mysql/20121226000000_remove_created_by_patch_rc3.sql
     * Example output: 20121226000000_remove_created_by_patch_rc3.sql
     * @param url
     * @return 
     */
    private String basename(URL url) {
        String[] parts = StringUtils.split(url.toExternalForm(), "/");
        return parts[parts.length-1];
    }
    
    /**
     * @param filename without any path like: 20121226000000_remove_created_by_patch_rc3.sql
     * @return 
     */
    private Long getTimestampFromSqlFilename(String filename) {
        Matcher mTimestampName = pTimestampName.matcher(filename);
        if( mTimestampName.matches() ) {
            String timestamp = mTimestampName.group(1); // the timestamp like: 20121226000000
            return Long.valueOf(timestamp);
        }
        return null;
    }

    /**
     * 
     * @return datasource object for mt wilson database, guaranteed non-null
     * @throws SetupException if the datasource cannot be obtained
     */
    private DataSource getDataSource() throws SetupException {
        try {
            //Properties jpaProperties = MyPersistenceManager.getASDataJpaProperties(My.configuration());
            Properties jpaProperties = MyPersistenceManager.getJpaProperties(My.configuration());
            
            log.debug("JDBC URL with schema: {}", jpaProperties.getProperty("javax.persistence.jdbc.url"));
            if( jpaProperties.getProperty("javax.persistence.jdbc.url") == null ) {
                log.error("Missing database connection settings");
                System.exit(1);
            }
            DataSource ds = PersistenceManager.getPersistenceUnitInfo("ASDataPU", jpaProperties).getNonJtaDataSource();
            if( ds == null ) {
                log.error("Cannot load persistence unit info");
                System.exit(2);
            }
            log.debug("Loaded persistence unit: ASDataPU");
            return ds;
        }
        catch(IOException e) {
            throw new SetupException("Cannot load persistence unit info", e);
        }   
    }

    private List<String> getTableNames(Connection c) throws SQLException {
        
       ArrayList<String> list = new ArrayList<>();
        try (Statement s = c.createStatement()) {
            String sqlStmt = "";
           switch (databaseVendor) {
               case "mysql":
                   sqlStmt = "SHOW TABLES";
                   break;
               case "postgresql":
                   sqlStmt = "SELECT table_name FROM information_schema.tables;";
                   break;
           }
           try (ResultSet rs = s.executeQuery(sqlStmt)) {
               while(rs.next()) {
                   list.add(rs.getString(1));
               }
           }
        }
        return list;

    }
    
    private List<ChangelogEntry> getChangelog(Connection c) throws SQLException {
        ArrayList<ChangelogEntry> list = new ArrayList<>();
        log.debug("Listing tables...");
        // first determine if we have the new changelog table `mw_changelog`, or the old one `changelog`, or none at all
        List<String> tableNames = getTableNames(c);
        boolean hasMwChangelog = false;
        boolean hasChangelog = false;
        if( tableNames.contains("mw_changelog") ) {
            hasMwChangelog = true;
        }
        if( tableNames.contains("changelog") ) {
            hasChangelog = true;
        }
        
        if( !hasChangelog && !hasMwChangelog) {
            return list; /*  empty list indicates database is not initialized and all scripts need to be executed */ 
        }
        
        String changelogTableName = null;
        // if we have both changelog tables, copy all records from old changelog to new changelog and then use that
        if( hasChangelog && hasMwChangelog ) {
            try (PreparedStatement check = c.prepareStatement("SELECT APPLIED_AT FROM mw_changelog WHERE ID=?")) {
                try (PreparedStatement insert = c.prepareStatement("INSERT INTO mw_changelog SET ID=?, APPLIED_AT=?, DESCRIPTION=?")) {
                    try (Statement select = c.createStatement()) {
                        try (ResultSet rs = select.executeQuery("SELECT ID,APPLIED_AT,DESCRIPTION FROM changelog")) {
                            while(rs.next()) {
                                check.setLong(1, rs.getLong("ID"));
                                try (ResultSet rsCheck = check.executeQuery()) {
                                    if( rsCheck.next() ) {
                                        // the id is already in the new mw_changelog table
                                    }
                                    else {
                                        insert.setLong(1, rs.getLong("ID"));
                                        insert.setString(2, rs.getString("APPLIED_AT"));
                                        insert.setString(3, rs.getString("DESCRIPTION"));
                                        insert.executeUpdate();
                                    }
                                }
                            }
                        }
                    }
                }
            }
            changelogTableName = "mw_changelog"; 
        }
        else if( hasMwChangelog ) {
            changelogTableName = "mw_changelog";
        }
        else if( hasChangelog ) {
            changelogTableName = "changelog";
        }
        try (Statement s = c.createStatement()) {
            try (ResultSet rs = s.executeQuery(String.format("SELECT ID,APPLIED_AT,DESCRIPTION FROM %s", changelogTableName))) {
                while(rs.next()) {
                    ChangelogEntry entry = new ChangelogEntry();
                    entry.id = rs.getString("ID");
                    entry.applied_at = rs.getString("APPLIED_AT");
                    entry.description = rs.getString("DESCRIPTION");
                    list.add(entry);
                }
            }
        }
        return list;
    }
    
}
