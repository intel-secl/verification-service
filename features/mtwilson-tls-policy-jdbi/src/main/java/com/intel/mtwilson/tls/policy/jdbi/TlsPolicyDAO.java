/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.tls.policy.jdbi;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.jdbi.util.UUIDArgument;
import java.io.Closeable;
import java.util.List;
import java.util.Set;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterArgumentFactory;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.sqlobject.stringtemplate.UseStringTemplate3StatementLocator;
import org.skife.jdbi.v2.unstable.BindIn;

/**
 *
 * @author jbuhacoff
 */
@UseStringTemplate3StatementLocator
@RegisterArgumentFactory({UUIDArgument.class})
@RegisterMapper({TlsPolicyResultMapper.class,})
public interface TlsPolicyDAO extends Closeable {
    @SqlUpdate("insert into mw_tls_policy (id, name, private, content_type, content, comment) values (:id, :name, :private, :contentType, :content, :comment)")
     void insertTlsPolicy(@BindBean TlsPolicyRecord tlsPolicyRecord);

    @SqlUpdate("update mw_tls_policy set name=:name, private=:private, content_type=:contentType, content=:content, comment=:comment where id=:id")
    void updateTlsPolicy(@BindBean TlsPolicyRecord tlsPolicyRecord);

    @SqlQuery("select id, name, private, content_type, content, comment from mw_tls_policy")
    List<TlsPolicyRecord> findAllTlsPolicy();
    
    @SqlQuery("select id, name, private, content_type, content, comment from mw_tls_policy where id=:id")
    TlsPolicyRecord findTlsPolicyById(@Bind("id") UUID id);

    @SqlQuery("select id, name, private, content_type, content, comment from mw_tls_policy where id in ( <ids> )")
    List<TlsPolicyRecord> findTlsPolicyByIds(@BindIn("ids") Set<String> ids);

    @SqlQuery("select id, name, private, content_type, content, comment from mw_tls_policy where private=true and name=:host_id")
    TlsPolicyRecord findPrivateTlsPolicyByHostId(@Bind("host_id") String host_id);
    
    @SqlQuery("select id, name, private, content_type, content, comment from mw_tls_policy where name=:name")
    TlsPolicyRecord findTlsPolicyByNameEqualTo(@Bind("name") String name);

    @SqlQuery("select id, name, private, content_type, content, comment from mw_tls_policy where name like concat('%',:name,'%')")
    List<TlsPolicyRecord> findTlsPolicyByNameContains(@Bind("name") String name);

    @SqlQuery("select id, name, private, content_type, content, comment from mw_tls_policy where private=:private")
    List<TlsPolicyRecord> findTlsPolicyByPrivateEqualTo(@Bind("private") boolean privateScope);

    @SqlQuery("select id, name, private, content_type, content, comment from mw_tls_policy where comment=:comment")
    TlsPolicyRecord findTlsPolicyByCommentEqualTo(@Bind("comment") String comment);

    @SqlQuery("select id, name, private, content_type, content, comment from mw_tls_policy where comment like concat('%',:comment,'%')")
    List<TlsPolicyRecord> findTlsPolicyByCommentContains(@Bind("comment") String comment);
    
    @SqlUpdate("delete from mw_tls_policy where id=:id")
    void deleteTlsPolicyById(@Bind("id") UUID id);

    @SqlUpdate("delete from mw_tls_policy where private=true and name=:host_id")
    void deletePrivateTlsPolicyByHostId(@Bind("host_id") String host_id);
    
}
