/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.plugin;

import com.intel.mtwilson.My;
import com.intel.mtwilson.flavor.rest.v2.repository.HostRepository;
import com.intel.mtwilson.flavor.rest.v2.resource.HostResource;
import static com.intel.mtwilson.i18n.HostState.QUEUE;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Required configuration:
 * 1. enabled - true if the trust status of all hosts should be refreshed automatically
 * 2. max time in cache (seconds) - if a host trust record is older than this number it will be automatically refreshed
 * 
 * This bean should be executed periodically (once every minute, or every 5 minutes) in order to automatically
 * refresh the trust status
 * 
 * @author jbuhacoff
 */
public class AutoRefreshTrust implements Runnable {
    private Logger log = LoggerFactory.getLogger(getClass());
    private boolean enabled = true;
    private long maxCacheDuration = 5; // hour
    private TimeUnit maxCacheDurationUnits = TimeUnit.MINUTES;
    private int refreshTimeBeforeSamlExpiry = 300; // seconds
    private long interval = 120; // seconds
    private TimeUnit intervalUnits = TimeUnit.SECONDS;
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public void setMaxCacheDuration(long maxCacheDuration) { this.maxCacheDuration = maxCacheDuration; }
    public void setMaxCacheDurationUnits(TimeUnit maxCacheDurationUnits) { this.maxCacheDurationUnits = maxCacheDurationUnits; }
    public void setInterval(long interval) { this.interval = interval; }
    public void setIntervalUnits(TimeUnit intervalUnits) { this.intervalUnits = intervalUnits; }
    private volatile boolean running;

    public AutoRefreshTrust(long interval, TimeUnit intervalUnits) {
        this.interval = interval;
        this.intervalUnits = intervalUnits;
    }
    
    
    public void cancel() {
        running = false;
    }
    
    @Override
    public void run() {
        running = true;
        while (running) {
            // make a list of hosts whose last trust status check is more than max cache duration ago
            List<String> hostsToRefresh = findHostIdsWithExpiredCache();
            if (hostsToRefresh != null && hostsToRefresh.size() > 0) {
                List<String> hostListForFlavorVerifyQueue = new HostRepository().filterHostsAlreadyInQueue(hostsToRefresh, true);

                // update host status of all the hosts to be added to flavor-verify queue
                new HostResource().updateHostStatusList(hostListForFlavorVerifyQueue, QUEUE, null);
                //add all the hosts with expired saml to queue
                new HostResource().addHostsToFlavorVerifyQueue(hostListForFlavorVerifyQueue, true);
                log.info("AutoRefreshTrust completed for {} hosts.", hostsToRefresh.size());
            } else {
                log.info("No hosts for bulk refresh");
            }
            if (!running) {
                break;
            }
            try {
                log.trace("Auto refresh thread would sleep for {} seconds.", TimeUnit.SECONDS.convert(interval, intervalUnits));
                Thread.sleep(TimeUnit.MILLISECONDS.convert(interval, intervalUnits));
            } catch (InterruptedException ex) {
                log.info("AutoRefreshTrust: Error during waiting for the next process: {}", ex.getMessage());
            }
        }
    }
    
    public List<String> findHostIdsWithExpiredCache() {
        try {
            return My.jpa().mwReport().findHostsWithExpiredCache(refreshTimeBeforeSamlExpiry);
        } catch (Exception ex) {
            log.error("AutoRefreshTrust:findHostnamesWithExpiredCache - Error during retrieval of hosts with expired cache.", ex);
            return null;
        }
    }

    public static class ExpiredHostStatus {
        String hostname;
        Date lastChecked; // alwasys more than maxCacheDuration ago...
    }
    
}
