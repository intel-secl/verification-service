/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.esxi.cluster.jdbi;

import java.sql.ResultSet;
import java.sql.SQLException;
import com.intel.dcsg.cpg.io.UUID;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

/**
 * 
 * @author avaguayo + hxia5
 */
public class EsxiClusterResultMapper implements ResultSetMapper<EsxiClusterRecord> {

    @Override
    public EsxiClusterRecord map(int i, ResultSet rs, StatementContext sc) throws SQLException {
        EsxiClusterRecord esxiClusterRecord = new EsxiClusterRecord();
        esxiClusterRecord.setId(UUID.valueOf(rs.getString("id")));
        esxiClusterRecord.setConnectionString(rs.getString("connection_string"));
        esxiClusterRecord.setClusterName(rs.getString("cluster_name"));
        esxiClusterRecord.setTlsPolicyId(rs.getString("tls_policy_id").trim());
        return esxiClusterRecord;
    }
    
}
