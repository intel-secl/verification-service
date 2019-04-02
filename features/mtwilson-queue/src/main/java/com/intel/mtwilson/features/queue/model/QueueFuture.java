/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.features.queue.model;

import com.intel.dcsg.cpg.io.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 *
 * @author rksavino
 */
public class QueueFuture {
    private UUID queueEntryId;
    private Callable callable;
    private Future future;
    
    public QueueFuture() { }
    
    public QueueFuture(UUID queueEntryId, Callable callable, Future future) {
        this.queueEntryId = queueEntryId;
        this.callable = callable;
        this.future = future;
    }
    
    public UUID getQueueEntryId() {
        return queueEntryId;
    }
    
    public void setQueueEntryId(UUID queueEntryId) {
        this.queueEntryId = queueEntryId;
    }
    
    public Callable getCallable() {
        return callable;
    }
    
    public void setCallable(Callable callable) {
        this.callable = callable;
    }
    
    public Future getFuture() {
        return future;
    }
    
    public void setFuture(Future future) {
        this.future = future;
    }
}
