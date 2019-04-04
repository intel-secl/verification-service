/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.tag.dao.jdbi;

import com.intel.mtwilson.core.common.tag.model.TagCertificate;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.jdbi.util.DateArgument;
import java.io.Closeable;
import java.util.Date;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterArgumentFactory;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import com.intel.mtwilson.jdbi.util.UUIDArgument;

/**
 * References:
 * http://www.jdbi.org/five_minute_intro/
 * http://jdbi.org/sql_object_api_argument_binding/
 * http://skife.org/jdbi/java/library/sql/2011/03/16/jdbi-sql-objects.html  (map result set to object)
 * http://www.cowtowncoder.com/blog/archives/2010/04/entry_391.html
 * http://jdbi.org/sql_object_api_batching/   (batching)
 * 
 * @author jbuhacoff
 */
@RegisterArgumentFactory({UUIDArgument.class,DateArgument.class})
@RegisterMapper(TagCertificateResultMapper.class)
public interface TagCertificateDAO extends Closeable{
    @SqlUpdate("create table mw_tag_certificate (id char(36) primary key, certificate blob, subject varchar(255), issuer varchar(255), notBefore timestamp, notAfter timestamp, hardware_uuid char(36))")
    void create();
    
    @SqlUpdate("insert into mw_tag_certificate (id, certificate, subject, issuer, notBefore, notAfter, hardware_uuid) "
            + "values (:id, :certificate, :subject, :issuer, :notBefore, :notAfter, :hardware_uuid)")
    void insert(@Bind("id") UUID id, @Bind("certificate") byte[] certificate, @Bind("subject") String subject, @Bind("issuer") String issuer,
            @Bind("notBefore") Date notBefore, @Bind("notAfter") Date notAfter, @Bind("hardware_uuid") UUID hardwareUuid);

    @SqlQuery("select id,certificate,subject,issuer,notBefore,notAfter,hardware_uuid from mw_tag_certificate where id=:id")
    TagCertificate findById(@Bind("id") UUID id);
    
    @SqlQuery("select * from mw_tag_certificate where LOWER(subject)=LOWER(:subject) order by notbefore desc limit 1")
    TagCertificate findLatestBySubject(@Bind("subject") String subject);

    @SqlUpdate("delete from mw_tag_certificate where id=:id")
    void delete(@Bind("id") UUID id);
    
    @Override
    void close();
}
