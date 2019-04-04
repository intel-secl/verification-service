/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.intel.mtwilson.i18n.HostState;
import com.intel.mtwilson.jaxrs2.Document;
import java.util.Date;

/**
 *
 * @author hmgowda
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class HostStatusInformation extends Document {
    private HostState hostState;
    private Date lastTimeConnected;

    public HostState getHostState() {
        return hostState;
    }

    public void setHostState(HostState hostState) {
        this.hostState = hostState;
    }

    public Date getLastTimeConnected() {
        return lastTimeConnected;
    }

    public void setLastTimeConnected(Date lastTimeConnected) {
        this.lastTimeConnected = lastTimeConnected;
    }
}
