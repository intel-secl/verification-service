/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.tag.dao.jdbi;

import com.intel.mtwilson.tag.model.Configuration;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.intel.dcsg.cpg.io.UUID;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

/**
 * 
 * @author jbuhacoff
 */
public class ConfigurationResultMapper implements ResultSetMapper<Configuration> {

    @Override
    public Configuration map(int i, ResultSet rs, StatementContext sc) throws SQLException {
        Configuration configuration = new Configuration();
        configuration.setId(UUID.valueOf(rs.getString("id")));
        configuration.setName(rs.getString("name"));
        if( rs.getString("content") != null ) {
            try {
                configuration.setXmlContent(rs.getString("content"));
            }
            catch(Exception e) {
                throw new SQLException("Cannot parse configuration content", e);
            }
        }
        return configuration;
    }
    
}
