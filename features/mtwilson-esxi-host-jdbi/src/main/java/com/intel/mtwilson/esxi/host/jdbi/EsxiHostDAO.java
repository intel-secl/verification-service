/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.esxi.host.jdbi;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.jdbi.util.UUIDArgument;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterArgumentFactory;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.sqlobject.stringtemplate.UseStringTemplate3StatementLocator;
import java.io.Closeable;
import java.util.List;
import java.util.Set;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.unstable.BindIn;

/**
 *
 * @author avaguayo
 */

@UseStringTemplate3StatementLocator
@RegisterArgumentFactory({UUIDArgument.class})
@RegisterMapper({EsxiHostResultMapper.class,})
public interface EsxiHostDAO extends Closeable {
    
    @SqlUpdate("insert into mw_link_esxi_cluster_host  (id, cluster_id, hostname) values (:id, :clusterId, :hostname)")
     void insertEsxiHost(@BindBean EsxiHostRecord esxiHostRecord);
     
    @SqlUpdate("update mw_link_esxi_cluster_host  set hostname=:hostname")
    void updateEsxiHostname(@BindBean EsxiHostRecord esxiHostRecord);
    
    @SqlQuery("select id, cluster_id, hostname from mw_link_esxi_cluster_host ")
    List<EsxiHostRecord> findAllEsxiHosts();
    
    @SqlQuery("select id, cluster_id, hostname from mw_link_esxi_cluster_host  where cluster_id=:cluster_id")
    List<EsxiHostRecord> findEsxiHostsByClusterId(@Bind("cluster_id") UUID id);
    
    @SqlUpdate("delete from mw_link_esxi_cluster_host  where id=:id")
    void deleteEsxiHostById(@Bind("id") UUID id);
    
    @SqlUpdate("delete from mw_link_esxi_cluster_host  where id in ( <ids> )")
    void deleteEsxiHostByIds(@BindIn("ids") Set<String> ids);
    
    @SqlUpdate("delete from mw_link_esxi_cluster_host  where hostname=:hostname")
    void deleteEsxiHostByHostname(@Bind("hostname") String hostname);
    
}
