/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.shiro.jdbi;

import com.intel.mtwilson.user.management.rest.v2.model.RolePermission;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.intel.dcsg.cpg.io.UUID;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

/**
 * 
 * @author jbuhacoff
 */
public class RolePermissionResultMapper implements ResultSetMapper<RolePermission> {
    
    @Override
    public RolePermission map(int i, ResultSet rs, StatementContext sc) throws SQLException {
        RolePermission rolePermission = new RolePermission();
        rolePermission.setRoleId(UUID.valueOf(rs.getString("role_id")));
        rolePermission.setPermitDomain(rs.getString("permit_domain"));
        rolePermission.setPermitAction(rs.getString("permit_action"));
        rolePermission.setPermitSelection(rs.getString("permit_selection"));
        return rolePermission;
    }
    
}
