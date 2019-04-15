/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.plugin;

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
 * @author jbuhacoff
 */
@WebListener
public class AutoRefreshTrustLoader implements ServletContextListener {

    private transient static Logger log = LoggerFactory.getLogger(AutoRefreshTrustLoader.class);
    private static final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
    private static final long DEFAULT_AUTO_REFRESH_TRUST_INTERVAL = 120;
    private Thread mainThread;
    private AutoRefreshTrust art = null;
    private ScheduledFuture<?> future = null;
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        My.initDataEncryptionKey();
        long sleepInterval = My.configuration().getConfiguration().getLong("mtwilson.auto.refresh.trust.interval.seconds", DEFAULT_AUTO_REFRESH_TRUST_INTERVAL);
        log.debug("============= {} =============", sleepInterval);
        if (sleepInterval == 0) {
            log.debug("mtwilson.auto.refresh.trust.interval.seconds=0, skipping AutoRefreshTrust");
        } else {
            art = new AutoRefreshTrust(sleepInterval, TimeUnit.SECONDS);
            future = executor.scheduleWithFixedDelay(art, sleepInterval, sleepInterval, TimeUnit.SECONDS);
            log.debug("================ Done ==============");
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (art != null) {
            log.trace("AutoRefreshTrust: About to end the thread");
            art.cancel();
        }
        if( future != null ) {
            future.cancel(true);            
    }
        if( executor != null ) {
            executor.shutdown();
        }
    }
}
