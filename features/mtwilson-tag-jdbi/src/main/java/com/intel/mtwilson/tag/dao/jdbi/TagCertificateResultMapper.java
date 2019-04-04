/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.tag.dao.jdbi;

import com.intel.mtwilson.core.common.tag.model.TagCertificate;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.intel.dcsg.cpg.io.UUID;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

/**
 * 
 * @author jbuhacoff
 */
public class TagCertificateResultMapper implements ResultSetMapper<TagCertificate> {

    @Override
    public TagCertificate map(int i, ResultSet rs, StatementContext sc) throws SQLException {
        byte[] content = rs.getBytes("certificate");
        TagCertificate certificate = new TagCertificate();
        java.util.UUID id_uuid = java.util.UUID.fromString(rs.getString("id"));
        certificate.setId(UUID.valueOf(id_uuid.toString()));
        certificate.setCertificate(content);
        certificate.setSubject(rs.getString("subject"));
        certificate.setIssuer(rs.getString("issuer"));
        certificate.setNotBefore(rs.getTimestamp("notBefore"));
        certificate.setNotAfter(rs.getTimestamp("notAfter"));
        java.util.UUID hid_uuid = java.util.UUID.fromString(rs.getString("hardware_uuid"));
        certificate.setHardwareUuid(UUID.valueOf(hid_uuid.toString()));
        return certificate;
    }
    
}
