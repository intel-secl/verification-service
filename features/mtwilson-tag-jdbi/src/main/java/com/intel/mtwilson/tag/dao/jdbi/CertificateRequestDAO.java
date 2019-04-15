/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.tag.dao.jdbi;

import com.intel.mtwilson.tag.model.CertificateRequest;
import com.intel.dcsg.cpg.io.UUID;
import java.io.Closeable;
import java.util.List;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterArgumentFactory;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import com.intel.mtwilson.jdbi.util.UUIDArgument;
import org.skife.jdbi.v2.sqlobject.BindBean;

@RegisterArgumentFactory(UUIDArgument.class)
@RegisterMapper(CertificateRequestResultMapper.class)
public interface CertificateRequestDAO extends Closeable{
    @SqlUpdate("create table mw_tag_certificate_request (id char(36) primary key, subject varchar(255), status varchar(255), content blob not null, contentType varchar(255) not null)")
    void create();

    @SqlUpdate("insert into mw_tag_certificate_request (id, subject, status) "
            + "values (:id, :subject, 'New')")
    void insert(@Bind("id") UUID id, @Bind("subject") String subject);

    @SqlUpdate("insert into mw_tag_certificate_request (id, subject, status, content, contentType) "
            + "values (:id, :subject, :status, :content, :contentType)")
    void insert(@BindBean CertificateRequest request);
    
    @SqlUpdate("update mw_tag_certificate_request set status=:status where id=:id")
    void updateStatus(@Bind("id") UUID id, @Bind("status") String status);

    @SqlUpdate("delete from mw_tag_certificate_request where id=:id")
    void deleteById(@Bind("id") UUID id);
    
    @SqlQuery("select id, subject, status, content, contentType from mw_tag_certificate_request where id=:id")
    CertificateRequest findById(@Bind("id") UUID id);
    
    @SqlQuery("select id, subject, status from mw_tag_certificate_request where subject=:subject")
    List<CertificateRequest> findBySubject(@Bind("subject") String subject);
    
    @Override
    void close();
}
