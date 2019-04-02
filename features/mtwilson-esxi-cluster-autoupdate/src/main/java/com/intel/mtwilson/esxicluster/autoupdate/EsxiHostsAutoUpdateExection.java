/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.esxicluster.autoupdate;

import com.intel.dcsg.cpg.crypto.RandomUtil;
import com.intel.mtwilson.esxi.rest.v2.repository.EsxiClusterRepository;
import java.security.SecureRandom;
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
 * 
 * @author hxia5
 */
public class EsxiHostsAutoUpdateExection implements Runnable {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private volatile boolean running;
    private static ThreadState subjectThreadState;
    private static org.apache.shiro.mgt.SecurityManager shiroSecurityManager = null;

    public EsxiHostsAutoUpdateExection() {
        running = true; 
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
               ini.setSectionProperty("users", username, String.format("%s, %s", guardedPassword.getInsPassword(), role));
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
                    log.debug("EsxiHostsAutoUpdate resumes...");
                    EsxiClusterRepository exsiHostsRepository = new EsxiClusterRepository();
                    exsiHostsRepository.updateHostsFromClusters();
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
}
