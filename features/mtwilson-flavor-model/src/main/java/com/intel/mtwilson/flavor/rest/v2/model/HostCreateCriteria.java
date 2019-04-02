/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.rest.v2.model;

import com.intel.dcsg.cpg.validation.Validator;
import com.intel.mtwilson.validators.ConnectionStringValidator;

import java.util.List;

/**
 *
 * @author hmgowda
 */
public class HostCreateCriteria {
    private String hostName;
    private String tlsPolicyId;
    private String connectionString;
    private String flavorgroupName;
    private String description;
    
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getTlsPolicyId() {
        return tlsPolicyId;
    }

    public void setTlsPolicyId(String tlsPolicyId) {
        this.tlsPolicyId = tlsPolicyId;
    }

    @Validator(ConnectionStringValidator.class)
    public String getConnectionString() {
        return connectionString;
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    public String getFlavorgroupName() {
        return flavorgroupName;
    }

    public void setFlavorgroupName(String flavorgroupName) {
        this.flavorgroupName = flavorgroupName;
    }

}
