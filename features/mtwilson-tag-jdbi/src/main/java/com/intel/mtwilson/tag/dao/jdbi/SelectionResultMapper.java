/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.tag.dao.jdbi;

import java.sql.ResultSet;
import java.sql.SQLException;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.tag.model.Selection;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

/**
 * 
 * @author jbuhacoff
 */
public class SelectionResultMapper implements ResultSetMapper<Selection> {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SelectionResultMapper.class);
    
    @Override
    public Selection map(int i, ResultSet rs, StatementContext sc) throws SQLException {
        String driver = (String)sc.getAttribute("driver");
        log.debug("driver is {}", driver);
        Selection selection = new Selection();
        selection.setId(UUID.valueOf(rs.getString("id")));
        selection.setName(rs.getString("name"));
        selection.setDescription(rs.getString("description"));
        return selection;
    }
    
}
