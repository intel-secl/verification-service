/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.tag.dao.jdbi;

import com.intel.mtwilson.tag.model.Certificate;
import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.dcsg.cpg.crypto.Sha256Digest;
import com.intel.dcsg.cpg.crypto.Sha384Digest;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.intel.dcsg.cpg.io.UUID;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

/**
 * 
 * @author jbuhacoff
 */
public class CertificateResultMapper implements ResultSetMapper<Certificate> {

    @Override
    public Certificate map(int i, ResultSet rs, StatementContext sc) throws SQLException {
        byte[] content = rs.getBytes("certificate");
        Certificate certificate = new Certificate();
        certificate.setId(UUID.valueOf(rs.getString("id")));
        certificate.setCertificate(content);
        certificate.setSha1(Sha1Digest.digestOf(content));
        certificate.setSha256(Sha256Digest.digestOf(content));
        certificate.setSha384(Sha384Digest.digestOf(content));
        certificate.setSubject(rs.getString("subject"));
        certificate.setIssuer(rs.getString("issuer"));
        certificate.setNotBefore(rs.getTimestamp("notBefore"));
        certificate.setNotAfter(rs.getTimestamp("notAfter"));
        return certificate;
    }
    
}
