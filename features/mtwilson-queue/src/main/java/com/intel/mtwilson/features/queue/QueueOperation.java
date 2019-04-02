/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.features.queue;

import com.intel.mtwilson.features.queue.model.QueueState;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * 
 * @author rksavino
 */
public abstract class QueueOperation implements Callable {
    private Map<String, String> parameters;
    private QueueState queueState;
    
    public QueueOperation() { }

    public QueueState getQueueState() {
        return queueState;
    }

    public void setQueueState(QueueState queueState) {
        this.queueState = queueState;
    }
    
    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }
    
    public String getParameter(String p) {
        if (parameters == null) { return null; }
        return parameters.get(p);
    }

    public void setParameter(String p, String v) {
        if (parameters == null) { parameters = new HashMap(); }
        this.parameters.put(p, v);
    }
}
