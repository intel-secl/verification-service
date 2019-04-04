/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.shiro.jdbi;

import com.intel.mtwilson.user.management.rest.v2.model.UserLoginHmacRole;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.intel.dcsg.cpg.io.UUID;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

/**
 * 
 * @author jbuhacoff
 */
public class UserLoginHmacRoleResultMapper implements ResultSetMapper<UserLoginHmacRole> {

    @Override
    public UserLoginHmacRole map(int i, ResultSet rs, StatementContext sc) throws SQLException {
        UserLoginHmacRole role = new UserLoginHmacRole();
        role.setLoginHmacId(UUID.valueOf(rs.getString("login_hmac_id")));
        role.setRoleId(UUID.valueOf(rs.getString("role_id")));
        return role;
    }
    
}
