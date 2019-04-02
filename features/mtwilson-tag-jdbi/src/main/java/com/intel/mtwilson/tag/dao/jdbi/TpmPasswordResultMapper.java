/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.tag.dao.jdbi;



import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.tag.model.TpmPassword;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

/**
 *
 * @author stdalex
 */
public class TpmPasswordResultMapper implements ResultSetMapper<TpmPassword>  {

    @Override
    public TpmPassword map(int i, ResultSet rs, StatementContext sc) throws SQLException {
        TpmPassword tpm = new TpmPassword();
        tpm.setId(UUID.valueOf(rs.getString("id")));
        tpm.setPassword(rs.getString("password"));
        return tpm;
    }
    
}
