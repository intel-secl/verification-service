/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.plugins.telemetry;

import com.intel.dcsg.cpg.crypto.RandomUtil;
import com.intel.mtwilson.My;
import com.intel.mtwilson.flavor.rest.v2.model.HostCollection;
import com.intel.mtwilson.flavor.rest.v2.model.HostFilterCriteria;
import com.intel.mtwilson.flavor.rest.v2.repository.HostRepository;
import com.intel.mtwilson.telemetry.rest.v2.repository.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.logging.Level;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.config.Ini;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.SubjectThreadState;
import org.apache.shiro.util.Factory;
import org.apache.shiro.util.LifecycleUtils;
import org.apache.shiro.util.ThreadState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.intel.mtwilson.crypto.password.GuardedPassword;

/**
 * Required configuration:
 * 1. enabled - true if the trust status of all hosts should be refreshed automatically
 * 2. max time in cache (seconds) - if a host trust record is older than this number it will be automatically refreshed
 * 
 * This bean should be executed periodically (once every minute, or every 5 minutes) in order to automatically
 * 
 * @author hxia5
 */
public class TelemetryExecution implements Runnable {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private volatile boolean running;
    private volatile boolean isRefreshing = true;
    private final String telemetryLogLocation = "/logs/mtwilson-telemetry-host.log";
    private String auditLogFileName = null;
    private static ThreadState subjectThreadState;
    private static org.apache.shiro.mgt.SecurityManager shiroSecurityManager = null;

    public TelemetryExecution() {
        running = true;
        this.auditLogFileName = My.configuration().getMtWilsonHome() + telemetryLogLocation;
    }
    
    public void cancel() {
        running = false;        
    }
    
    private void setSubject(Subject subject) {
        clearSubject();
        subjectThreadState = createThreadState(subject);
        subjectThreadState.bind();
    }
    
    private Subject getSubject() {
        return SecurityUtils.getSubject();
    }
    
    private ThreadState createThreadState(Subject subject) {
        return new SubjectThreadState(subject);
    }
    
    private void clearSubject() {
        doClearSubject();
    }
    
    private static void doClearSubject() {
        if (subjectThreadState != null) {
            subjectThreadState.clear();
            subjectThreadState = null;
        }
    }
    
    @Override
    public void run() {
        if (running) {  
            
            // try to login as superuser
            Subject threadSubject;
            try {
               // define shiro superuser credentials, create subject and bind it to thread
               SecureRandom random = RandomUtil.getSecureRandom();
               String username = System.getProperty("user.name", "anonymous");
               GuardedPassword guardedPassword = new GuardedPassword();
               guardedPassword.setPassword(Integer.toHexString(random.nextInt()));

               // define superuser role and permissions
               String role = "esxi-superuser";
               String permissions = "*";

               // build shiro INI configuration with authentication details
               Ini ini = new Ini();
               ini.setSectionProperty("users", username, String.format("%s, %s",guardedPassword.getInsPassword(), role));
               ini.setSectionProperty("roles", role, permissions);

               // build security manager from shiro INI configuration
               Factory<org.apache.shiro.mgt.SecurityManager> factory = new IniSecurityManagerFactory(ini);
               shiroSecurityManager = factory.getInstance();
               //setSecurityManager(sm);

               // build subject from security manager
               threadSubject = new Subject.Builder(shiroSecurityManager).buildSubject();
               setSubject(threadSubject);

               UsernamePasswordToken loginToken = new UsernamePasswordToken(username, guardedPassword.getInsPassword());
               guardedPassword.dispose();

               threadSubject.login(loginToken);
               log.debug("Logged in as user [{}] with superuser role", username);           
            
                try {
                    // retrieve number of hosts from db
                    HostRepository repository = new HostRepository();
                    HostFilterCriteria criteria = new HostFilterCriteria();
                    criteria.filter = false;
                    HostCollection hCollection = repository.search(criteria);
                    // update telmetry db
                    updateDb(hCollection.getHosts().size());
                    /* write to log file
                    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                    Date date = new Date();
                    String newLog = dateFormat.format(date) + " Number_Of_Hosts: " + hCollection.getHosts().size();
                    updateLog(newLog); 
                    */
                } catch (Exception e) {
                    log.warn("Error while calling ESXi auto update operation: {}", e.getMessage(), e);
                }  finally {
                    try {
                        threadSubject.logout();
                        doClearSubject();
                    } catch (Exception e) {
                        log.warn("Cannot clear shiro subject: {}", e.getMessage());
                    }
                    try {
                        LifecycleUtils.destroy(shiroSecurityManager);
                    } catch (Exception e) {
                        log.warn("Cannot destroy shiro security manager: {}", e.getMessage());
                    }
                }
            } catch (Exception e) {
               log.warn("Error while trying to login as super user: {}", e.getMessage(), e);
            } 
        }
    }

    // update the audit log
    private void updateLog(String logtxt) { 
      try {
          File auditFile = new File(auditLogFileName);
          if (!auditFile.exists())
              auditFile.createNewFile();
          try (FileWriter fw = new FileWriter(auditFile.getAbsoluteFile(), true);
          BufferedWriter bw = new BufferedWriter(fw);) {          
            bw.write(logtxt);
            bw.newLine();
          }
      } catch (IOException ex) {
          log.error("Error while trying to update the audit log: {}", ex);
      }
    }
    
    private void updateDb(int hostNum) {
        TelemetryRepository telemetryRepos = new TelemetryRepository();
        telemetryRepos.create(hostNum);
    }
}
