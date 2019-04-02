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
//import com.intel.mtwilson.audit.helper.AuditConfig;
import com.intel.mtwilson.audit.helper.MtWilsonThreadLocal;

import java.util.Date;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dsmagadx
 */
public class AuditLogger {
    private static Logger log = LoggerFactory.getLogger(AuditLogger.class);
//    private static boolean isAsyncEnabled = true; // AuditConfig.isAsyncEnabled();
    
    
//    private static String AUDIT_LOGGER_JNDI= "";
//    static{
//        try {
//        	
//            logger.info("Before the JNDILookup");
//            AUDIT_LOGGER_JNDI = String.format("java:global/%s/AuditAsyncWorker", (String) new InitialContext().lookup("java:app/AppName"));
//            logger.info("JNDI Name for look up : {}", AUDIT_LOGGER_JNDI);
//            logger.info("Async Mode -" + isAsyncEnabled);
//            
//        } catch (NamingException ex) {
//            logger.error("Error while setting JNDI name for AuditLogger.", ex);
//        }
//    }



    
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

    /*private void setSecurityCredentials(AuditLogEntry auditLogEntry) {
        AuditContext auditContext =  MtWilsonThreadLocal.get();
        
        log.debug("Object from thread local " + auditContext);
        if(auditContext != null){
            //Need to handle the old auth scheme
            auditLogEntry.setFingerPrint(auditContext.getName());
            auditLogEntry.setTransactionId(auditContext.getTransactionUuid());
        }else{
            log.warn("No Audit context. Setting user as unknown.");
            auditLogEntry.setFingerPrint("Unknown");
            auditLogEntry.setTransactionId("Unknown");
        }
    }
*/
    private AuditWorker getAuditWorker() throws NamingException {
        
        return new AuditAsyncWorker();
//        if(isAsyncEnabled){
//        	
//            return (AuditWorker) new InitialContext().lookup(AUDIT_LOGGER_JNDI);
//        }else{
//            return new AuditAsyncWorker();
//        }
        
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
