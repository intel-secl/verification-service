/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson;

import com.intel.dcsg.cpg.jpa.PersistenceManager;
import java.util.Properties;
import javax.persistence.EntityManagerFactory;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Example:
 * 
        Properties jdbc = new Properties();
        jdbc.setProperty("mtwilson.db.host", "192.168.0.1");
        jdbc.setProperty("mtwilson.db.schema", "mw_as");
        jdbc.setProperty("mtwilson.db.user", "root");
        jdbc.setProperty("mtwilson.db.password", "password");
        jdbc.setProperty("mtwilson.db.port", "3306");
        jdbc.setProperty("mtwilson.as.dek", "hPKk/2uvMFRAkpJNJgoBwA=="); // optional;  if you don't set this ,the value you see here is the default
        CustomPersistenceManager pm = new CustomPersistenceManager(jdbc);

 *
 * @author jbuhacoff
 */
public class MyPersistenceManager extends PersistenceManager {
    private transient static Logger log = LoggerFactory.getLogger(MyPersistenceManager.class);
    private static ClassLoader jpaClassLoader = null;
    
    private Properties jdbcProperties;
    public MyPersistenceManager(Properties jdbcProperties) {
        this.jdbcProperties = jdbcProperties;
    }
    @Override
    public void configure() {
        log.debug("MyPersistenceManager: Database Host: {}", jdbcProperties.getProperty("mtwilson.db.host"));
        MyConfiguration c = new MyConfiguration(jdbcProperties);
        addPersistenceUnit("FlavorDataPU", getFlavorDataJpaProperties(c));
        addPersistenceUnit("TelemetryDataPU", getFlavorDataJpaProperties(c));
        addPersistenceUnit("AuditDataPU", getAuditDataJpaProperties(c));
    }
    public EntityManagerFactory getFlavorData() {
        return getEntityManagerFactory("FlavorDataPU");
    }
    public EntityManagerFactory getTelemetryData() {
        return getEntityManagerFactory("TelemetryDataPU");
    }

    // mtwilson-launcher calls setJpaClassLoader with the application classloader as parameter so that ASPersistenceManager can pass it o PersistenceManager as the classlaoder to use for loading persistence.xml files (because they are under /opt/mtwilson/java not in the .war)

    public static Properties getJpaProperties(MyConfiguration config) {
        Properties prop = new Properties();
        prop.put("javax.persistence.jdbc.driver", config.getDatabaseDriver());
        prop.put("javax.persistence.jdbc.scheme", config.getDatabaseProtocol()); 
        String url = String.format("jdbc:%s://%s:%s/%s?autoReconnect=true",
                config.getDatabaseProtocol(), config.getDatabaseHost(), 
                config.getDatabasePort(), config.getDatabaseSchema());
        prop.put("javax.persistence.jdbc.url", url);
        prop.put("javax.persistence.jdbc.user", config.getDatabaseUsername());
        prop.put("javax.persistence.jdbc.password", config.getDatabasePassword());
        prop.put("eclipselink.jdbc.batch-writing", "JDBC");
        log.debug("javax.persistence.jdbc.url={}", url);
        //System.err.println("getJpaProps Default url == " + prop.getProperty("javax.persistence.jdbc.url"));
        return prop;
    }
    
    // copies some properties described in http://commons.apache.org/proper/commons-dbcp/configuration.html
    // using same defaults as shown on that page
    public static void copyDbcpProperties(Configuration myConfig, Properties prop) {
        prop.setProperty("dbcp.max.active", myConfig.getInteger("dbcp.max.active", 200).toString());
        prop.setProperty("dbcp.max.idle", myConfig.getInteger("dbcp.max.idle", 32).toString());
        prop.setProperty("dbcp.min.idle", myConfig.getInteger("dbcp.min.idle", 16).toString()); // can be used instead of initial size
        prop.setProperty("dbcp.validation.query", myConfig.getString("dbcp.validation.query","")); // for example SELECT 1 ; we provide empty string default because Properties would throw NullPointerException for a null value
        prop.setProperty("dbcp.validation.on.borrow",String.valueOf( myConfig.getBoolean("dbcp.validation.on.borrow", true))); 
        prop.setProperty("dbcp.validation.on.return", String.valueOf(myConfig.getBoolean("dbcp.validation.on.return", false))); 
    }
    
    public static Properties getFlavorDataJpaProperties(MyConfiguration config) {
        Properties prop = new Properties();
        Configuration myConfig = config.getConfiguration();
        prop.put("javax.persistence.jdbc.driver",
                myConfig.getString("mountwilson.as.db.driver", config.getDatabaseDriver()));
        
        String jdbcDriver = null;
        if (prop.containsKey("javax.persistence.jdbc.driver")) {
            jdbcDriver = prop.get("javax.persistence.jdbc.driver").toString();
        }
        
        if ((jdbcDriver != null) && (jdbcDriver.equals("com.mysql.jdbc.Driver"))) {
            prop.put("javax.persistence.jdbc.scheme", "mysql"); // NOTE: this is NOT a standard javax.persistence property, we are setting it for our own use
        } else if ((jdbcDriver != null) && (jdbcDriver.equals("org.postgresql.Driver"))) {
            prop.put("javax.persistence.jdbc.scheme", "postgresql"); // NOTE: this is NOT a standard javax.persistence property, we are setting it for our own use
        } else {
            prop.put("javax.persistence.jdbc.scheme", "unknown-scheme");
        }
        
        prop.put("javax.persistence.jdbc.host", myConfig.getString("mountwilson.as.db.host", config.getDatabaseHost()));
        prop.put("javax.persistence.jdbc.port", myConfig.getString("mountwilson.as.db.port", config.getDatabasePort()));
        prop.put("javax.persistence.jdbc.schema", myConfig.getString("mountwilson.as.db.schema", config.getDatabaseSchema()));
        prop.put("javax.persistence.jdbc.url", myConfig.getString("mountwilson.as.db.url",
                myConfig.getString("mtwilson.db.url", String.format("jdbc:%s://%s:%s/%s?autoReconnect=true",
                        prop.get("javax.persistence.jdbc.scheme"),
                        prop.get("javax.persistence.jdbc.host"),
                        prop.get("javax.persistence.jdbc.port"),
                        prop.get("javax.persistence.jdbc.schema")))));
        prop.put("javax.persistence.jdbc.user", myConfig.getString("mountwilson.as.db.user",
                myConfig.getString("mtwilson.db.user", "root")));
        prop.put("javax.persistence.jdbc.password", myConfig.getString("mountwilson.as.db.password",
                myConfig.getString("mtwilson.db.password", "password")));
        prop.put("eclipselink.jdbc.batch-writing", "JDBC");
        log.debug("FlavorData javax.persistence.jdbc.url={}", prop.getProperty("javax.persistence.jdbc.url"));
        copyDbcpProperties(myConfig, prop);
        return prop;
    }
    
    public static Properties getEnvDataJpaProperties(MyConfiguration config) {
        Properties prop = getFlavorDataJpaProperties(config);
        String mtwilsonDbDriverEnvVar = System.getenv("MTWILSON_DB_DRIVER");
        if ( mtwilsonDbDriverEnvVar != null && !mtwilsonDbDriverEnvVar.isEmpty()) {
            prop.put("javax.persistence.jdbc.driver", mtwilsonDbDriverEnvVar);
        }

        String jdbcDriver = null;
        if (prop.containsKey("javax.persistence.jdbc.driver"))
            jdbcDriver = prop.get("javax.persistence.jdbc.driver").toString();        

        if( (jdbcDriver != null) && (jdbcDriver.equals("com.mysql.jdbc.Driver")) ) {
            prop.put("javax.persistence.jdbc.scheme", "mysql"); // NOTE: this is NOT a standard javax.persistence property, we are setting it for our own use
        }
        else if((jdbcDriver != null) && (jdbcDriver.equals("org.postgresql.Driver")) ) {
            prop.put("javax.persistence.jdbc.scheme", "postgresql"); // NOTE: this is NOT a standard javax.persistence property, we are setting it for our own use
        }
        else {
            prop.put("javax.persistence.jdbc.scheme", "unknown-scheme");
        }
        String mtwilsonDbHostEnvVar = System.getenv("MTWILSON_DB_HOST");
        if ( mtwilsonDbHostEnvVar != null && !mtwilsonDbHostEnvVar.isEmpty()) {
            prop.put("javax.persistence.jdbc.host", mtwilsonDbHostEnvVar);
        }
        String mtwilsonDbPortEnvVar = System.getenv("MTWILSON_DB_PORT");
        if (mtwilsonDbPortEnvVar != null && !mtwilsonDbPortEnvVar.isEmpty()) {
            prop.put("javax.persistence.jdbc.port", mtwilsonDbPortEnvVar);
        }
        String mtwilsonDbSchemaEnvVar = System.getenv("MTWILSON_DB_SCHEMA");
        if (mtwilsonDbSchemaEnvVar != null && !mtwilsonDbSchemaEnvVar.isEmpty()) {
            prop.put("javax.persistence.jdbc.schema", mtwilsonDbSchemaEnvVar);
        }
        
        prop.put("javax.persistence.jdbc.url",
                String.format("jdbc:%s://%s:%s/%s?autoReconnect=true",
                prop.get("javax.persistence.jdbc.scheme"),
                prop.get("javax.persistence.jdbc.host"),
                prop.get("javax.persistence.jdbc.port"),
                prop.get("javax.persistence.jdbc.schema")));
        
        if (System.getenv("MTWILSON_DB_USER") != null && !System.getenv("MTWILSON_DB_USER").isEmpty()) {
            prop.put("javax.persistence.jdbc.user",
                    System.getenv("MTWILSON_DB_USER"));
        }
        
        if (System.getenv("MTWILSON_DB_PASSWORD") != null && !System.getenv("MTWILSON_DB_PASSWORD").isEmpty()) {
            prop.put("javax.persistence.jdbc.password",
                    System.getenv("MTWILSON_DB_PASSWORD"));
        }
        
        prop.put("eclipselink.jdbc.batch-writing", "JDBC");
        
        return prop;
    }
    public static Properties getAuditDataJpaProperties(MyConfiguration config) {
        Properties prop = new Properties();
        Configuration myConfig = config.getConfiguration();
        prop.put("javax.persistence.jdbc.driver", 
                myConfig.getString("mountwilson.audit.db.driver", 
                config.getDatabaseDriver()));

        String jdbcDriver = null;
        if (prop.containsKey("javax.persistence.jdbc.driver"))
            jdbcDriver = prop.get("javax.persistence.jdbc.driver").toString();        
        
        if( (jdbcDriver != null) && (jdbcDriver.equals("com.mysql.jdbc.Driver")) ) {
            prop.put("javax.persistence.jdbc.scheme", "mysql"); // NOTE: this is NOT a standard javax.persistence property, we are setting it for our own use
        }
        else if( (jdbcDriver != null) && (jdbcDriver.equals("org.postgresql.Driver")) ) {
            prop.put("javax.persistence.jdbc.scheme", "postgresql"); // NOTE: this is NOT a standard javax.persistence property, we are setting it for our own use
        }
        else {
            prop.put("javax.persistence.jdbc.scheme", "unknown-scheme");
        }        
        prop.put("javax.persistence.jdbc.url" , 
                myConfig.getString("mountwilson.audit.db.url",
                myConfig.getString("mtwilson.db.url",
                String.format("jdbc:%s://%s:%s/%s?autoReconnect=true",
                    prop.getProperty("javax.persistence.jdbc.scheme"),
                    myConfig.getString("mountwilson.audit.db.host", config.getDatabaseHost()),
                    myConfig.getString("mountwilson.audit.db.port", config.getDatabasePort()),
                    myConfig.getString("mountwilson.audit.db.schema", config.getDatabaseSchema())))));
        prop.put("javax.persistence.jdbc.user",
                myConfig.getString("mountwilson.audit.db.user",
                myConfig.getString("mtwilson.db.user",
                "root")));
        prop.put("javax.persistence.jdbc.password", 
                myConfig.getString("mountwilson.audit.db.password", 
                myConfig.getString("mtwilson.db.password", 
                "password")));
        log.debug("AuditData javax.persistence.jdbc.url={}", prop.getProperty("javax.persistence.jdbc.url"));
        copyDbcpProperties(myConfig, prop);
        return prop;
    }
}
