/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.shiro.jdbi;

import com.intel.mtwilson.user.management.rest.v2.model.User;
import com.intel.dcsg.cpg.i18n.LocaleUtil;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.intel.dcsg.cpg.io.UUID;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

/**
 * 
 * @author jbuhacoff
 */
public class UserResultMapper implements ResultSetMapper<User> {

    @Override
    public User map(int i, ResultSet rs, StatementContext sc) throws SQLException {
        User user = new User();
        user.setId(UUID.valueOf(rs.getString("id"))); // works for postgresql  when using uuid field
        user.setUsername(rs.getString("username"));
        user.setComment(rs.getString("comment"));
        if( rs.getString("locale") != null ) {
            user.setLocale(LocaleUtil.forLanguageTag(rs.getString("locale")));
        }
        return user;
    }
    
}
