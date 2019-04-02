/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.features.queue.model;

import com.intel.mtwilson.jaxrs2.Document;
import java.util.Date;
import java.util.Map;

/**
 *
 * @author hmgowda
 */
public class Queue extends Document {
    private String queueAction;
    private Map<String, String> actionParameters;
    private Date created;
    private Date updated;
    private QueueState status;
    private String message;
    
    public String getQueueAction() {
        return queueAction;
    }
    
    public void setQueueAction(String queueAction) {
        this.queueAction = queueAction;
    }
    
    public Map<String, String> getActionParameters() {
        return actionParameters;
    }
    
    public void setActionParameters(Map<String, String> actionParameters) {
        this.actionParameters = actionParameters;
    }
    
    public Date getCreated() {
        return created;
    }
    
    public void setCreated(Date created) {
        this.created = created;
    }
    
    public Date getUpdated() {
        return updated;
    }
    
    public void setUpdated(Date updated) {
        this.updated = updated;
    }
    
    public QueueState getStatus() {
        return status;
    }
    
    public void setStatus(QueueState status) {
        this.status = status;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}
