/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.esxi.host.jdbi;

import com.intel.dcsg.cpg.io.UUID;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

/**
 *
 * @author avaguayo
 */
public class EsxiHostResultMapper implements ResultSetMapper<EsxiHostRecord> {
    
@Override
    public EsxiHostRecord map(int i, ResultSet rs, StatementContext sc) throws SQLException {
        EsxiHostRecord esxihostRecord = new EsxiHostRecord();
        esxihostRecord.setId(UUID.valueOf(rs.getString("id")));
        esxihostRecord.setClusterId(UUID.valueOf(rs.getString("cluster_id")));
        esxihostRecord.setHostname(rs.getString("hostname"));
        return esxihostRecord;
    }
    
}
