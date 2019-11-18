/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.features.queue;

import com.intel.dcsg.cpg.crypto.RandomUtil;
import com.intel.dcsg.cpg.extensions.Extensions;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.My;
import com.intel.mtwilson.features.queue.model.Queue;
import com.intel.mtwilson.features.queue.model.QueueCollection;
import com.intel.mtwilson.features.queue.model.QueueFilterCriteria;
import com.intel.mtwilson.features.queue.model.QueueFuture;
import com.intel.mtwilson.features.queue.model.QueueLocator;
import com.intel.mtwilson.features.queue.model.QueueState;
import static com.intel.mtwilson.features.queue.model.QueueState.ERROR;
import static com.intel.mtwilson.features.queue.model.QueueState.NEW;
import static com.intel.mtwilson.features.queue.model.QueueState.PENDING;
import static com.intel.mtwilson.features.queue.model.QueueState.RETURNED;
import com.intel.mtwilson.features.queue.repository.QueueRepository;
import com.intel.mtwilson.text.transform.PascalCaseNamingStrategy;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
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

/**
 * 
 * @author rksavino
 */
public class QueueExecution implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(QueueExecution.class);
    private static ThreadState subjectThreadState;
    private volatile boolean running;
    
    // queue interval
    private static final long DEFAULT_QUEUE_EXECUTION_INTERVAL = 3;
    long sleepInterval = My.configuration().getConfiguration().getLong("mtwilson.queue.execution.interval", DEFAULT_QUEUE_EXECUTION_INTERVAL);
    
    // queue threading
    private final int DEFAULT_QUEUE_MAX_THREADS = 128;
    int maxThreads = My.configuration().getConfiguration().getInt("mtwilson.queue.max.threads", DEFAULT_QUEUE_MAX_THREADS);
    
    // thread executer service
    private ExecutorService threadExecutor = null;
    
    public QueueExecution() {
        if (sleepInterval <= 0) {
            log.warn("Queue execution interval set to zero, turning off queue execution");
            return;
        }
        if (maxThreads <= 0) {
            log.warn("Queue execution threads set to zero, turning off queue execution");
            return;
        }
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
    
    private static void setSecurityManager(org.apache.shiro.mgt.SecurityManager securityManager) {
        SecurityUtils.setSecurityManager(securityManager);
    }
    
    private static org.apache.shiro.mgt.SecurityManager getSecurityManager() {
        return SecurityUtils.getSecurityManager();
    }
    
    @Override
    public void run() {
        threadExecutor = Executors.newFixedThreadPool(maxThreads);
        QueueRepository queueRepositoryObject = new QueueRepository();
        // try to login as superuser
        try {
            // define shiro superuser credentials, create subject and bind it to thread
            SecureRandom random = RandomUtil.getSecureRandom();
            String username = System.getProperty("user.name", "anonymous");
            String password = Integer.toHexString(random.nextInt());
            
            // define superuser role and permissions
            String role = "superuser";
            String permissions = "*";
            
            // build shiro INI configuration with authentication details
            Ini ini = new Ini();
            ini.setSectionProperty("users", username, String.format("%s, %s", password, role));
            ini.setSectionProperty("roles", role, permissions);
            
            // build security manager from shiro INI configuration
            Factory<org.apache.shiro.mgt.SecurityManager> factory = new IniSecurityManagerFactory(ini);
            setSecurityManager(factory.getInstance());
            
            // build subject from security manager
            setSubject(new Subject.Builder(getSecurityManager()).buildSubject());
            
            UsernamePasswordToken loginToken = new UsernamePasswordToken(username, password);
            getSubject().login(loginToken);
            log.debug("Logged in as user [{}] with superuser role", username);
        } catch (Exception e) {
            log.warn("Error while trying to login as super user: {}", e.getMessage(), e);
        }
        
        try {
            //check if there are any PENDING items in the queue when the queue starts for the first time
            QueueFilterCriteria criteriaForPendingItems = new QueueFilterCriteria();
            criteriaForPendingItems.queueStates = new ArrayList();
            criteriaForPendingItems.queueStates.add(PENDING.name());
            QueueCollection queueCollectionWithPendingEntries = new QueueRepository().search(criteriaForPendingItems);
            
            if (queueCollectionWithPendingEntries != null && queueCollectionWithPendingEntries.getQueueEntries() != null
                        && !queueCollectionWithPendingEntries.getQueueEntries().isEmpty()) {
                for (Queue queueEntry : queueCollectionWithPendingEntries.getQueueEntries()) {
                    UUID queueEntryId = queueEntry.getId();
                    updateQueueEntryStatus(queueEntryId, RETURNED, "Entry stuck at startup.");
                    log.debug("Moving unfinished queue entries to RETURNED state.");
                }
            }
        } catch (Exception e) {
            log.warn("Error while processing unfinished queue entries: {}", e.getMessage(), e);
        }
        
        Set<QueueFuture> queueSet = ConcurrentHashMap.newKeySet();
        try {
            while(running){
                // retrieve all queue entries
                QueueFilterCriteria criteria = new QueueFilterCriteria();
                criteria.queueStates = new ArrayList();
                criteria.queueStates.add(NEW.name());
                criteria.queueStates.add(RETURNED.name());
                criteria.queueStates.add(ERROR.name());
                QueueCollection queueCollection = new QueueRepository().search(criteria);
                if (queueCollection == null || queueCollection.getQueueEntries() == null
                        || queueCollection.getQueueEntries().isEmpty()) {
                    log.debug("No queue entries found");
                } else {
                    // submit queue entries to thread pool
                    for (Queue queueEntry : queueCollection.getQueueEntries()) {
                        if (!running) {
                            break;
                        }

                        // set queue entry status to PENDING
                        updateQueueEntryStatus(queueEntry.getId(), PENDING, null);

                        // validation queue entry has an action and ID
                        if (queueEntry.getQueueAction() == null || queueEntry.getId() == null) {
                            updateQueueEntryStatus(queueEntry.getId(), ERROR, "Invalid queue entry format. Either queue action or ID is not valid content.");
                            continue;
                        }
                        log.debug("Found queue entry [{}] with queue action: {}", queueEntry.getId(), queueEntry.getQueueAction());

                        // find a matching queue operation with the queue entry queue action
                        QueueOperation queueOperation = findQueueOperation(queueEntry);
                        if (queueOperation == null) {
                            updateQueueEntryStatus(queueEntry.getId(), ERROR, "Could not find matching queue operation on classpath.");
                            continue;
                        }

                        // add the queue operation to the thread executor
                        queueOperation.setQueueState(PENDING);
                        queueSet.add(new QueueFuture(queueEntry.getId(), queueOperation, threadExecutor.submit(queueOperation)));
                    }
                }
                // check status of each thread and remove the once completed from set and from queue table
                // The purpose of this below loop is to ensure that all the threads have completed processing.
                // Also, if cancel is called, it will cancel all queue executions and return the entries to the queue
                for (QueueFuture qf : queueSet) {
                    UUID queueEntryId = qf.getQueueEntryId();
                    Future<?> runningCommand = qf.getFuture();
                    if (!running) {
                        runningCommand.cancel(true);
                        continue;
                    }
//                        runningCommand.get(timeout, TimeUnit.SECONDS);
                    if(runningCommand.isDone()){
                        QueueState queueState = ((QueueOperation)qf.getCallable()).getQueueState();
                        if(queueState == null)
                            queueState = QueueState.ERROR;
                        switch(queueState){
                            case TIMEOUT:
                                log.debug("Thread timed out");
                                break;
                            case CONNECTION_FAILURE:
                                log.debug("Failed to connect to host. Removing queue entry from repository");
                                Queue queueEntry = new Queue();
                                QueueLocator queueLocator = new QueueLocator();
                                queueEntry.setId(queueEntryId);
                                queueLocator.copyTo(queueEntry);
                                queueRepositoryObject.delete(queueLocator);
                            case ERROR:
                                runningCommand.cancel(true);
                                log.error("Exception while retrieving queue operation.");
                                queueSet.remove(qf);
                                updateQueueEntryStatus(queueEntryId, ERROR, "Exception thrown during queue operation.");
                                continue;
                            case COMPLETED:
                        }
                        // delete queue entry after successful exectuion
                        QueueLocator locator = new QueueLocator();
                        locator.id = queueEntryId;
                        new QueueRepository().delete(locator);
                        
                        log.debug("Successfully executed queue entry: {}", queueEntryId);
                        queueSet.remove(qf);
                    }
                }
                try {
                    TimeUnit.SECONDS.sleep(sleepInterval);
                }
                catch (InterruptedException ie) {
                    for (QueueFuture qf : queueSet) {
                        UUID queueEntryId = qf.getQueueEntryId();
                        updateQueueEntryStatus(queueEntryId, RETURNED, "Thread cancelled.");
                        log.debug("Moving unfinished queue entries in RETURNED state.", ie);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error while calling queue operation: {}", e.getMessage(), e);
        }  finally {
            for (QueueFuture qf : queueSet) {
                UUID queueEntryId = qf.getQueueEntryId();
                updateQueueEntryStatus(queueEntryId, RETURNED, "Thread cancelled.");
                log.debug("Moving unfinished queue entries in RETURNED state.");
            }
            try {
                threadExecutor.shutdown();
                while (!threadExecutor.isTerminated()) { }
                log.debug("All queue threads completed, executor shutdown");
            } catch (Exception e) {
                log.error("Cannot shutdown queue thread executor service: {}", e.getMessage(), e);
            }
            try {
                doClearSubject();
            } catch (Exception e) {
                log.warn("Cannot clear shiro subject: {}", e.getMessage());
            }
            try {
                LifecycleUtils.destroy(getSecurityManager());
            } catch (Exception e) {
                log.warn("Cannot destroy shiro security manager: {}", e.getMessage());
            }
            try {
                SecurityUtils.setSecurityManager(null);
            } catch (Exception e) {
                log.warn("Cannot set shiro security manager to null: {}", e.getMessage());
            }
        }
    }
    
    private QueueOperation findQueueOperation(Queue queueEntry) {
        // find available queue operations
        List<QueueOperation> queueOperations = Extensions.findAll(QueueOperation.class);
        if (queueOperations == null || queueOperations.isEmpty()) {
            log.error("No valid implementations exist for queue operation");
            return null;
        }
        
        for (QueueOperation queueOperation : queueOperations) {
            if (queueOperation.getClass() == null || queueOperation.getClass().getName() == null) {
                log.warn("Invalid queue operation format");
                continue;
            }
            
            log.trace("Trying to cast queue action [{}] to queue operation implementation: {}", queueEntry.getQueueAction(), queueOperation.getClass().getName());
            String queueActionPascalCase = new PascalCaseNamingStrategy().toPascalCase(queueEntry.getQueueAction());
            if (!queueActionPascalCase.equals(queueOperation.getClass().getSimpleName())) {
                log.trace("Queue entry action [{}] does not match queue operation: {}", queueActionPascalCase, queueOperation.getClass().getSimpleName());
                continue;
            }
            
            log.trace("Queue entry action [{}] matches queue operation: {}", queueEntry.getQueueAction(), queueOperation.getClass().getSimpleName());
            if (queueEntry.getActionParameters() != null && !queueEntry.getActionParameters().isEmpty()) {
                log.trace("Adding queue entry parameters...");
                queueOperation.setParameters(queueEntry.getActionParameters());
            }
            return queueOperation;
        }
        return null;
    }
    
    private void updateQueueEntryStatus(UUID queueEntryId, QueueState status, String message) {
        Queue queueEntry = new Queue();
        queueEntry.setId(queueEntryId);
        queueEntry.setStatus(status);
        if (message != null && !message.isEmpty())
            queueEntry.setMessage(message);
        new QueueRepository().store(queueEntry);
    }
}
