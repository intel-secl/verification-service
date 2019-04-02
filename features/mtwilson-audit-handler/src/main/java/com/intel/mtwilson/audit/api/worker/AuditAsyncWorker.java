/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.audit.api.worker;

import com.intel.mtwilson.MyConfiguration;
import com.intel.mtwilson.audit.api.AuditWorker;
import com.intel.mtwilson.audit.controller.AuditLogEntryJpaController;
import com.intel.mtwilson.audit.data.AuditLogEntry;
import com.intel.mtwilson.audit.helper.AuditHandlerException;
import com.intel.mtwilson.audit.helper.AuditPersistenceManager;
//import javax.ejb.Asynchronous;
//import javax.ejb.LocalBean;
//import javax.ejb.Stateless;
import javax.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dsmagadx
 */
//@Stateless
//@LocalBean
public class AuditAsyncWorker implements AuditWorker {

    private Logger logger = LoggerFactory.getLogger(getClass().getName());

    private static AuditPersistenceManager persistenceManager = new AuditPersistenceManager();
    
    // Audit log table size
    private static final int DEFAULT_AUDIT_LOG_MAX_ROW_COUNT = 1000000;
    private final int maxRowCount = new MyConfiguration().getConfiguration().getInt("mtwilson.audit.log.max.row.count", DEFAULT_AUDIT_LOG_MAX_ROW_COUNT);
    
    // Audit log number of rotations
    private static final int DEFAULT_AUDIT_LOG_NUM_ROTATIONS = 10;
    private final int numRotations = new MyConfiguration().getConfiguration().getInt("mtwilson.audit.log.num.rotations", DEFAULT_AUDIT_LOG_NUM_ROTATIONS);
    
    @Override
//    @Asynchronous
    public void addLog(AuditLogEntry log) throws AuditHandlerException {
        AuditLogEntryJpaController controller = new AuditLogEntryJpaController(getEntityManagerFactory());
        controller.rotate(maxRowCount,numRotations);
		controller.create(log);
    }

    private EntityManagerFactory getEntityManagerFactory() {
        return persistenceManager.getEntityManagerFactory("AuditDataPU");
    }
}
