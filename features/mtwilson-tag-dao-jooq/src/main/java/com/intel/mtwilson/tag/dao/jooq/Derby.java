/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.tag.dao.jooq;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import javax.sql.DataSource;
import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * References:
 * Validation queries: http://stackoverflow.com/questions/3668506/efficient-sql-test-query-or-validation-query-that-will-work-across-all-or-most
 * 
 * @author jbuhacoff
 */
public class Derby {
    private static Logger log = LoggerFactory.getLogger(Derby.class);
    public static String driver = "org.apache.derby.jdbc.EmbeddedDriver";

    /**
     * NOTE: this url is repeated in the pom.xml where jooq needs it to connect
     * to the database to grab the schema and automatically generate sources
     */
    public static String protocol = "jdbc:derby:directory:mytestdb"; // places it in directory "target/derby" under current directory (good for junit testing)
    public static Connection c = null;
    public static DataSource ds = null;
    private static boolean isLoaded = false;
    private static boolean isSchemaCreated = false;
    
    public static void startDatabase() throws SQLException {
        // assume derby database is in a "derby" folder in current directory unless the user has set the system property to override this.
        if( System.getProperty("derby.system.home") == null ) {
            log.debug("System property derby.system.home is not set; using default ./derby");
            System.setProperty("derby.system.home", "derby");
        }
        
        if( !isLoaded ) {
            try {
            Class.forName(driver).newInstance();
            isLoaded = true;
            }
            catch(ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                throw new SQLException("Cannot load Derby driver", e);
            }
        }
    }
    
    public static void stopDatabase() {
        try {
            // shut down all databaes and the derby engine  ; throws SQLException "Derby system shutdown."
            DriverManager.getConnection(protocol+";shutdown=true"); // same as the protocol above but with create=true replaced with shutdown=true
        } 
        catch(Exception e) {
            log.info("{}", e.getMessage()); // expect:   Database 'directory:target/derby/mytestdb' shutdown.
            // we don't print the full stack trace because we know Derby throws an exception on shutdown (part of its API documentation)
            // if you print e.toString() it would be like: java.sql.SQLNonTransientConnectionException: Database 'directory:target/derby/mytestdb' shutdown.
            // and the rest of the stack trace is not useful since we know the cause is we issued a shutdown command via the connection.
        }
    }
    
    public static DataSource getDataSource() throws SQLException {
        if( !isLoaded ) { startDatabase(); }
        if( ds == null ) {
        BasicDataSource dataSource = new BasicDataSource();

        dataSource.setDriverClassName(driver); // or com.mysql.jdbc.Driver  for mysql
        dataSource.setUrl(protocol+";create=true"); // creates it in the "target/derby" folder which is for temporary files, good for junit tests
        dataSource.setMaxActive(10);
        dataSource.setMaxIdle(5);
        dataSource.setInitialSize(5);
        dataSource.setValidationQuery("VALUES 1");  // derby-specific query,  for mysql /postgresl / microsoft sql / sqlite / and h2  use "select 1"
        ds = dataSource;
        }
        return ds;
    }
    public static Connection getConnection() throws SQLException {
        if( c == null ) {
            c = getDataSource().getConnection(); // also a username/password option is available
        }
        return c;
    }
    
    public static void testDatabaseConnection() throws SQLException {
        try (Connection c = DriverManager.getConnection(protocol, new Properties())) {
            try (Statement s = c.createStatement()) {
                try (ResultSet rs = s.executeQuery("VALUES 1")) {
                    if( rs.next() ) {
                        log.info("Database connection is ok");
                    }
                }
            }
        }
    }

    public static boolean tableExists(String tableName) throws SQLException {
        Set<String> availableTables = listTablesAndViews(getConnection());
        return availableTables.contains(tableName) || availableTables.contains(tableName.toUpperCase()); // derby tables names tend to be all caps
    }
    
    public static Set<String> listTablesAndViews(Connection targetDBConn) throws SQLException
  {
    HashSet<String> set = new HashSet<String>();
    DatabaseMetaData dbmeta = targetDBConn.getMetaData();
    readDBTable(set, dbmeta, "TABLE", null);
    readDBTable(set, dbmeta, "VIEW", null);
    return set;
  }

  private static void readDBTable(Set<String> set, DatabaseMetaData dbmeta, String searchCriteria, String schema)
      throws SQLException
  {
        try (ResultSet rs = dbmeta.getTables(null, schema, null, new String[]{ searchCriteria })) {
            while (rs.next()) {
                log.trace("readDBTable Table: {}" , rs.getString("TABLE_NAME"));
              set.add(rs.getString("TABLE_NAME"));
            }
        }
  }    
  
  
}
