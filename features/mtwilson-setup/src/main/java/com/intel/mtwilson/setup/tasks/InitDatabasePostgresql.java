/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.setup.tasks;

import com.intel.mtwilson.My;
import com.intel.mtwilson.MyPersistenceManager;
import com.intel.dcsg.cpg.jpa.PersistenceManager;
import com.intel.mtwilson.setup.LocalSetupTask;
import com.intel.mtwilson.setup.SetupException;
import java.io.IOException;
import java.net.URL;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
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
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bug #509 create java program to handle database updates and ensure that 
 * old updates (already executed) are not executed again
 * 
 * Added -n (dry run) option on request from the SAM team: provide a way to determine if the
 * installer is compatible with the database; in other words, can the installer use the existing
 * database or does it know how to upgrade it as necessary. This is only true if the changelog
 * in the database is a subset of what is in the installer - in this case return a dry run success. 
 * If the database has any entries that are not in the installer, return a dry run failure.
 * 
 * Examples:
 * 
 * java -jar setup-console-1.2-SNAPSHOT-with-dependencies.jar InitDatabase postgresql --check
 * 
 * java -jar setup-console-1.2-SNAPSHOT-with-dependencies.jar InitDatabase mysql --check
 * 
 * Test cases for the --check option:
 * 1. install mt wilson, then run the tool.  Should report no changes are necessary 
 * 2. delete some entries from the mw_changelog table, run the tool. Should report that there are database upgrades to apply.
 * 3. create new bogus entries in the mw_changelog table, run the tool. Should report that it's not compatible with the database
 * 
 * References:
 * http://stackoverflow.com/questions/1429172/how-do-i-list-the-files-inside-a-jar-file
 * http://stackoverflow.com/questions/1044194/running-a-sql-script-using-mysql-with-jdbc
 * 
 * @author jbuhacoff
 */
public class InitDatabasePostgresql extends LocalSetupTask {
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    private final String databaseVendor = "postgresql";
    private Configuration options = null;
    private boolean runOnce = false;
    
    public void setOptions(Configuration options) {
        this.options = options;
    }
    
    @Override
    public void configure() throws Exception { }
    
    @Override
    public void validate() throws Exception {
        if (!runOnce) {
            validation("Forcing setup task to run at least once");
        }
    }
    
    @Override
    public void execute() throws Exception {
        try {
            initDatabase();
        } catch(Exception e) {
            throw new SetupException(String.format("Cannot setup database: %s", e.toString()), e);
        }
        runOnce = true;
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
        if (options != null && options.getBoolean("verbose", false)) {
            System.out.println(String.format(format, args));
        }
    }
    
    private void initDatabase() throws SetupException, IOException, SQLException {
        log.debug("Loading SQL for {}", databaseVendor);
        Map<Long,Resource> sql = getSql(databaseVendor);
        DataSource ds = getDataSource();
        
        log.debug("Connecting to {}", databaseVendor);
        Connection c = null;
        try {
            c = ds.getConnection();  // username and password should already be set in the datasource
        } catch(SQLException e) {
            log.error("Failed to connect to {} with schema: error = {}", databaseVendor, e.getMessage());
                log.error("Cannot connect to database");
                System.exit(2);
        }
        
        List<ChangelogEntry> changelog = getChangelog(c);
        HashMap<Long,ChangelogEntry> presentChanges = new HashMap(); // what is already in the database according to the changelog
        verbose("Existing database changelog has %d entries", changelog.size());
        for(ChangelogEntry entry : changelog) {
            presentChanges.put(Long.valueOf(entry.id), entry);
            verbose("%s %s %s", entry.id, entry.applied_at, entry.description);
        }
        
        // Does it have any changes that we don't?  In other words, is the database schema newer than what we know in this installer?
        if (options != null && options.getBoolean("check", false)) {
            HashSet<Long> unknownChanges = new HashSet(presentChanges.keySet()); // list of what is in database
            unknownChanges.removeAll(sql.keySet()); // remove what we have in this installer
            if( unknownChanges.isEmpty() ) {
                System.out.println("Database is compatible");
//                System.exit(0); // not yet -- after this block we'll print out if there are any changes to apply
            } else { // if( !unknownChanges.isEmpty() ) {
                // Database has new schema changes we dont' know about
                System.out.println("WARNING: Database schema is newer than this version of Mt Wilson");
                ArrayList<Long> unknownChangesInOrder = new ArrayList(unknownChanges);
                Collections.sort(unknownChangesInOrder);
                for(Long unknownChangeId : unknownChangesInOrder) {
                    ChangelogEntry entry = presentChanges.get(unknownChangeId);
                    System.out.println(String.format("%s %s %s", entry.id, entry.applied_at, entry.description));
                }
                System.exit(8); // database not compatible
            }
        }
        
        HashSet<Long> changesToApply = new HashSet(sql.keySet());
        changesToApply.removeAll(presentChanges.keySet());
        
        if( changesToApply.isEmpty() ) {
            System.out.println("No database updates available");
            System.exit(0); // database is compatible;   whether we are doing a dry run with --check or not, we exit here with success because there is nothing else to do
        }
        
        // At this point we know we have some updates to the databaseschema
        ArrayList<Long> changesToApplyInOrder = new ArrayList(changesToApply);
        Collections.sort(changesToApplyInOrder);
        
        if (options != null && options.getBoolean("check", false)) {
            System.out.println("The following changes will be applied:");
            for (Long changeId : changesToApplyInOrder) {
                System.out.println(changeId);
            }
            System.exit(0); // database is compatible
        }
        
        ResourceDatabasePopulator rdp = new ResourceDatabasePopulator();
        for(Long id : changesToApplyInOrder) {
            rdp.addScript(sql.get(id)); // new ClassPathResource("/com/intel/mtwilson/database/mysql/bootstrap.sql")); // must specify full path to resource
        }
        
        rdp.setContinueOnError(true);
        rdp.setIgnoreFailedDrops(true);
        rdp.setSeparator(";");
        rdp.populate(c);
        
        c.close();
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
        System.out.println(String.format("Scanning for %s SQL files", databaseVendor));
        HashMap<Long,Resource> sqlmap = new HashMap();
        try {
            Resource[] list = listResources(databaseVendor); // each URL like: jar:file:/C:/Users/jbuhacof/workspace/mountwilson-0.5.4/desktop/setup-console/target/setup-console-0.5.4-SNAPSHOT-with-dependencies.jar!/com/intel/mtwilson/database/mysql/20121226000000_remove_created_by_patch_rc3.sql
            for(Resource resource : list) {
                URL url = resource.getURL();
                Long timestamp = getTimestampFromSqlFilename(basename(url));
                if( timestamp != null ) {
                    sqlmap.put(timestamp, resource);
                } else {
                    System.err.println(String.format("SQL filename is not in recognized format: %s", url.toExternalForm()));
                }
            }
        } catch(IOException e) {
            throw new SetupException(String.format("Error while scanning for SQL files: %s", e.getLocalizedMessage()), e);
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
        } catch(IOException e) {
            throw new SetupException("Cannot load persistence unit info", e);
        }   
    }
    
    private List<String> getTableNames(Connection c) throws SQLException {
        ArrayList<String> list = new ArrayList();
        try (Statement s = c.createStatement()) {
            String sql = "";
            if (databaseVendor.equals("mysql")) {
                sql = "SHOW TABLES";
            } else if (databaseVendor.equals("postgresql")) {
                sql = "SELECT table_name FROM information_schema.tables;";
            }
            try (ResultSet rs  = s.executeQuery(sql)) {
                while (rs.next()) {
                    list.add(rs.getString(1));
                }
            }
        }
        return list;
    }
    
    private List<ChangelogEntry> getChangelog(Connection c) throws SQLException {
        ArrayList<ChangelogEntry> list = new ArrayList();
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
        if( hasChangelog && hasMwChangelog) {
            try (PreparedStatement check = c.prepareStatement("SELECT APPLIED_AT FROM mw_changelog WHERE ID=?")) {
                try (PreparedStatement insert = c.prepareStatement("INSERT INTO mw_changelog SET ID=?, APPLIED_AT=?, DESCRIPTION=?")) {
                    try (Statement select = c.createStatement()) {
                        try (ResultSet rs = select.executeQuery("SELECT ID,APPLIED_AT,DESCRIPTION FROM changelog")) {
                            while (rs.next()) {
                                check.setLong(1, rs.getLong("ID"));
                                try (ResultSet rsCheck = check.executeQuery()) {
                                    if (rsCheck.next()) {
                                        // the id is already in the new mw_changelog table
                                    } else {
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
        } else if( hasMwChangelog ) {
            changelogTableName = "mw_changelog";
        } else if( hasChangelog ) {
            changelogTableName = "changelog";
        }
        try (Statement s = c.createStatement()) {
            try (ResultSet rs = s.executeQuery(String.format("SELECT ID,APPLIED_AT,DESCRIPTION FROM %s", changelogTableName))) {
                while (rs.next()) {
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
