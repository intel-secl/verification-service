/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.shiro.jdbi;

import com.intel.mtwilson.user.management.rest.v2.model.UserLoginHmac;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.intel.dcsg.cpg.io.UUID;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

/**
  id uuid DEFAULT NULL,
  user_id uuid DEFAULT NULL,
  hmac_key bytea NOT NULL,
  protection character varying(128) NOT NULL,
  expires timestamp DEFAULT NULL,
  enabled boolean NOT NULL DEFAULT '0',
 * 
 * @author jbuhacoff
 */
public class UserLoginHmacResultMapper implements ResultSetMapper<UserLoginHmac> {

    @Override
    public UserLoginHmac map(int i, ResultSet rs, StatementContext sc) throws SQLException {
        UserLoginHmac userLoginHmac = new UserLoginHmac();
        userLoginHmac.setId(UUID.valueOf(rs.getString("id"))); // works for postgresql  when using uuid field
        userLoginHmac.setUserId(UUID.valueOf(rs.getString("user_id"))); // works for postgresql  when using uuid field
        userLoginHmac.setHmacKey(rs.getBytes("hmac_key"));
        userLoginHmac.setProtection(rs.getString("protection"));
        userLoginHmac.setExpires(rs.getTimestamp("expires"));
        userLoginHmac.setEnabled(rs.getBoolean("enabled"));
        return userLoginHmac;
    }
    
}
