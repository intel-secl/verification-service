/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.esxi.cluster.jdbi;

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
 * @author avaguayo
 */
@UseStringTemplate3StatementLocator
@RegisterArgumentFactory({UUIDArgument.class})
@RegisterMapper({EsxiClusterResultMapper.class,})
public interface EsxiClusterDAO extends Closeable {
    @SqlUpdate("insert into mw_esxi_cluster (id, connection_string, cluster_name, tls_policy_id) values (:id, :connectionString, :clusterName, :tlsPolicyId)")
     void insertEsxiCluster(@BindBean EsxiClusterRecord esxiClusterRecord);

    @SqlUpdate("update mw_esxi_cluster set connection_string=:connection_string, cluster_name=:cluster_name, tls_policy_id=:tls_policy_id")
    void updateEsxiCluster(@BindBean EsxiClusterRecord esxiClusterRecord);

    @SqlQuery("select id,connection_string,cluster_name,tls_policy_id from mw_esxi_cluster")
    List<EsxiClusterRecord> findAllEsxiCluster();
    
    @SqlQuery("select id,connection_string,cluster_name,tls_policy_id from mw_esxi_cluster where id=:id")
    EsxiClusterRecord findEsxiClusterById(@Bind("id") UUID id);

    @SqlQuery("select id,connection_string,cluster_name,tls_policy_id from mw_esxi_cluster where id in ( <ids> )")
    List<EsxiClusterRecord> findEsxiClusterByIds(@BindIn("ids") Set<String> ids);

    @SqlQuery("select id,connection_string,cluster_name,tls_policy_id from mw_esxi_cluster where cluster_name=:cluster_name")
    EsxiClusterRecord findEsxiClusterByName(@Bind("cluster_name") String clusterName);
    
    @SqlQuery("select id,connection_string,cluster_name,tls_policy_id from mw_esxi_cluster where cluster_name like concat('%',:cluster_name,'%')")
    List<EsxiClusterRecord> findEsxiClusterByNameContains(@Bind("cluster_name") String cluster_name);

    @SqlUpdate("delete from mw_esxi_cluster where id=:id")
    void deleteEsxiClusterById(@Bind("id") UUID id);

    @SqlUpdate("delete from mw_esxi_cluster where cluster_name=:cluster_name")
    void deleteEsxiClusterByName(@Bind("cluster_name") String cluster_name);
    
}
