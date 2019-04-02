/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.audit.data;

/**
 *
 * @author dsmagadx
 */
public class AuditContext {

    public AuditContext(String name, long startMilliseconds) {
        this.name = name;
        this.startMilliseconds = startMilliseconds;
    }
  
    private String name;
    private long startMilliseconds;
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public long getStartMilliseconds() {
        return startMilliseconds;
    }

    public void setStartMilliseconds(long startMilliseconds) {
        this.startMilliseconds = startMilliseconds;
    }
}
