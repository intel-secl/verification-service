/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.plugins.telemetry;

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
public class TelemetryLoader implements ServletContextListener {

    private transient static Logger log = LoggerFactory.getLogger(TelemetryLoader.class);
    private static final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
    private static final long DEFAULT_TELEMETRY_AUTOUPDATE_INTERVAL = 10; //seconds. 1day= 24*60*60 = 86,4000s
    private TelemetryExecution autoRefreshExecution = null;
    private ScheduledFuture<?> future = null;
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        log.info("TelemetryServiceLoader: About to start the thread");
        long sleepInterval = My.configuration().getConfiguration().getLong("mtwilson.telemetry.interval", DEFAULT_TELEMETRY_AUTOUPDATE_INTERVAL);
        if (sleepInterval == 0) {
            log.debug("mtwilson.telemetry.interval=0, skipping autorefresh");
        } else {
            autoRefreshExecution = new TelemetryExecution();
            future = executor.scheduleAtFixedRate(autoRefreshExecution, sleepInterval, sleepInterval, TimeUnit.SECONDS);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (autoRefreshExecution != null) {
            log.info("AuditExecution: About to end the thread");
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
