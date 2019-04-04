/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.features.queue;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author rksavino
 */
@WebListener
public class QueueExecutionLoader implements ServletContextListener {
    private transient static final Logger log = LoggerFactory.getLogger(QueueExecutionLoader.class);
    private ScheduledExecutorService intervalExecutor = null;
    private ScheduledFuture<?> future = null;
    private QueueExecution queueExecution = null;
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        intervalExecutor = Executors.newScheduledThreadPool(1);
        queueExecution = new QueueExecution();
        log.info("Starting queue execution service [{}]...", new Date().toString());
        future = intervalExecutor.schedule(queueExecution, 5, TimeUnit.SECONDS);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (queueExecution != null) {
            log.info("Cancelling queue execution tasks...");
            queueExecution.cancel();
        }
        
        if( future != null ) {
            log.info("Cancelling queue execution scheduled future...");
            future.cancel(true);
        }
        
        if( intervalExecutor != null ) {
            log.info("Shutting down queue exection loader...");
            intervalExecutor.shutdown();
        }
    }
}
