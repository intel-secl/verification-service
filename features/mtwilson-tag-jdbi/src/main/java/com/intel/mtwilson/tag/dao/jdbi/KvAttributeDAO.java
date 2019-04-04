/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.tag.dao.jdbi;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.tag.model.KvAttribute;
import java.io.Closeable;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterArgumentFactory;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import com.intel.mtwilson.jdbi.util.UUIDArgument;
import org.skife.jdbi.v2.sqlobject.BindBean;

@RegisterArgumentFactory(UUIDArgument.class)
@RegisterMapper(KvAttributeResultMapper.class)
public interface KvAttributeDAO extends Closeable {
    @SqlUpdate("create table mw_tag_kvattribute (id char(36) primary key, name varchar(255), value varchar(255))")
    void create();
    
    @SqlUpdate("insert into mw_tag_kvattribute (id, name, value) values (:id, :name, :value)")
    void insert(@Bind("id") UUID id, @Bind("name") String name, @Bind("value") String value);

    @SqlUpdate("insert into mw_tag_kvattribute (id, name, value) values (:id, :name, :value)")
    void insert(@BindBean KvAttribute kvattribute);

    @SqlUpdate("update mw_tag_kvattribute set name=:name, value=:value where id=:id")
    void update(@Bind("id") UUID id, @Bind("name") String name, @Bind("value") String value);

    @SqlUpdate("delete from mw_tag_kvattribute where id=:id")
    void delete(@Bind("id") UUID id);

    @SqlQuery("select id, name, value from mw_tag_kvattribute where id=:id")
    KvAttribute findById(@Bind("id") UUID id);
    
    @SqlQuery("select id, name, value from mw_tag_kvattribute where name=:name and value=:value")
    KvAttribute findByNameAndValue(@Bind("name") String name, @Bind("value") String value);

    @SqlQuery("select id, name, value from mw_tag_kvattribute where name=:name")
    KvAttribute findByName(@Bind("name") String name);
    
    @Override
    void close();
}
