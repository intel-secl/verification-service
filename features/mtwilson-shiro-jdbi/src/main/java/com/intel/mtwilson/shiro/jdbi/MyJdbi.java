/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.shiro.jdbi;

import com.intel.dcsg.cpg.jpa.PersistenceManager;
import com.intel.mtwilson.My;
import com.intel.mtwilson.MyPersistenceManager;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import javax.sql.DataSource;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.tweak.ConnectionFactory;

/**
 * References: Validation queries:
 * http://stackoverflow.com/questions/3668506/efficient-sql-test-query-or-validation-query-that-will-work-across-all-or-most
 *
 * @author jbuhacoff
 */
public class MyJdbi {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MyJdbi.class);

    private static class DataSourceHolder {
        private static final DataSource ds = createDataSource();

        private static DataSource createDataSource() {
            //#5820: Call to static method 'com.intel.mtwilson.MyPersistenceManager.getASDataJpaProperties' via instance reference.
            Properties jpaProperties = MyPersistenceManager.getFlavorDataJpaProperties(My.configuration());
            return PersistenceManager.createDataSource(jpaProperties);
        }
    }

    /**
     * Must close connection when done! for example: try(LoginDAO loginDAO =
     * MyJdbi.authz()) { // do things... java7 will automatically call close()
     * on loginDAO when done or on exception }
     *
     * @return
     * @throws SQLException
     */
    public static LoginDAO authz() throws SQLException, IOException {
        return getDBI().open(LoginDAO.class);
    }

    private static DBI getDBI() throws IOException {
        // issue #4978: creating new DBI instance for each request using connection pool
        log.debug("MyJdbi (mtwilson-shiro-jdbi) created new DBI instance");
        return new DBI(new DataSourceConnectionFactory(DataSourceHolder.ds));
    }
    
    public static class DataSourceConnectionFactory implements ConnectionFactory {
        private DataSource ds;

        public DataSourceConnectionFactory(DataSource ds) {
            this.ds = ds;
        }

        @Override
        public Connection openConnection() throws SQLException {
            log.debug("MyJdbi (mtwilson-shiro-jdbi) opening database connection via configured datasource...");
            return ds.getConnection();
        }
        
    }

    public static class ExistingConnectionFactory implements ConnectionFactory {

        @Override
        public Connection openConnection() throws SQLException {
            try {
                Connection connection = My.jdbc().connection();
                log.debug("MyJdbi (mtwilson-shiro-jdbi) connection: {}", connection);
                return connection;
            } catch (IOException | ClassNotFoundException | SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
