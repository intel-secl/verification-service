/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package test.jdbi;

import com.intel.mtwilson.esxi.cluster.jdbi.*;
import com.intel.dcsg.cpg.io.UUID;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author avaguayo
 */
public class RepositoryTest {
    private static Logger log = LoggerFactory.getLogger(RepositoryTest.class);

    @Test
    public void testCreateEsxiCluster() throws Exception {
        try(EsxiClusterDAO dao = EsxiClusterJdbiFactory.esxiClusterDAO()) {
        
        //create a new record
            EsxiClusterRecord esxiClusterRecord = new EsxiClusterRecord();
            esxiClusterRecord.setId(new UUID());
            esxiClusterRecord.setConnectionString("192.168.0.1:ADMIN:password");
            esxiClusterRecord.setClusterName("MyCluster");
            
        dao.insertEsxiCluster(esxiClusterRecord);

        log.debug("Created esxi cluster {} with id {}", esxiClusterRecord.getClusterName(), esxiClusterRecord.getId());
        
        }
    }
    
}
