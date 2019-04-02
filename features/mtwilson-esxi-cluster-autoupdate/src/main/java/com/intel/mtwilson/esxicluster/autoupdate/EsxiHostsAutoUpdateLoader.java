/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.esxicluster.autoupdate;

import com.intel.mtwilson.My;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hxia5
 */
@WebListener
public class EsxiHostsAutoUpdateLoader implements ServletContextListener {

    private transient static Logger log = LoggerFactory.getLogger(EsxiHostsAutoUpdateLoader.class);
    private static final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
    private static final long DEFAULT_ESXIHOSTS_AUTOUPDATE_INTERVAL = 120; //seconds since the unit is set to SECONDS below
    private EsxiHostsAutoUpdateExection autoRefreshExecution = null;
    private ScheduledFuture<?> future = null;
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        log.info("EsxiHostsAutoUpdateExecution: About to start the thread");
        long sleepInterval = My.configuration().getConfiguration().getLong("mtwilson.esxihosts.autoupdate.interval", DEFAULT_ESXIHOSTS_AUTOUPDATE_INTERVAL);
        if (sleepInterval == 0) {
            log.debug("mtwilson.esxihosts.autoupdate.interval=0, skipping autorefresh");
        } else {
            autoRefreshExecution = new EsxiHostsAutoUpdateExection();
            future = executor.scheduleAtFixedRate(autoRefreshExecution, 5, sleepInterval, TimeUnit.SECONDS);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (autoRefreshExecution != null) {
            log.info("EsxiHostsAutoUpdateExecution: About to end the thread");
            autoRefreshExecution.cancel();            
        }
        if( future != null ) {
            future.cancel(true);            
    }
        if( executor != null ) {
            executor.shutdown();
        }
    }
}
