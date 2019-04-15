/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.audit.api;

import com.intel.mtwilson.audit.api.worker.AuditAsyncWorker;
import com.intel.mtwilson.audit.helper.AuditHandlerException;
import com.intel.mtwilson.audit.data.AuditContext;
import com.intel.mtwilson.audit.data.AuditLog;
import com.intel.mtwilson.audit.data.AuditLogEntry;
import com.intel.mtwilson.audit.helper.MtWilsonThreadLocal;

import java.util.Date;
import javax.naming.NamingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dsmagadx
 */
public class AuditLogger {
    private static Logger log = LoggerFactory.getLogger(AuditLogger.class);

    public void addLog(AuditLog log) throws AuditHandlerException{
        
        try {
            AuditWorker worker = getAuditWorker();
            worker.addLog(getAuditLogEntry(log));
        } catch (Exception e) {
            throw new AuditHandlerException(e);
        }
    }

    private AuditLogEntry getAuditLogEntry(AuditLog log) {
        AuditLogEntry auditLogEntry = new AuditLogEntry();
        auditLogEntry.setId(log.getId());
        auditLogEntry.setAction(log.getAction());
        auditLogEntry.setCreated(new Date(System.currentTimeMillis()));
        auditLogEntry.setData(log.getData());
        auditLogEntry.setEntityId(log.getEntityId());
        auditLogEntry.setEntityType(log.getEntityType());
        return auditLogEntry;
    }

    private AuditWorker getAuditWorker() throws NamingException {
        
        return new AuditAsyncWorker();
    }
    
    public String getAuditUserName() {
        String userName;
        try {
            AuditContext auditContext =  MtWilsonThreadLocal.get();
            if(auditContext != null){
                userName = auditContext.getName();
            }else{
                userName = "Unknown";
            } 
        } catch (Exception ex) {
            log.error("Error during retrieval of user name from the audit context. " + ex.getMessage());
            userName = "Unknown";
        }
        return userName;
    }
    
}
