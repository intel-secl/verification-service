/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.rest.v2.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.validation.Validator;
import com.intel.mtwilson.jaxrs2.Document;
import com.intel.mtwilson.validators.ConnectionStringValidator;

import java.util.List;

/**
 *
 * @author hmgowda
 */
@JacksonXmlRootElement(localName = "host")
public class Host extends Document {
    private String hostName;
    private String description;
    private String connectionString;
    private UUID hardwareUuid;
    private String tlsPolicyId;
    private String flavorgroupName;
    private List<String> flavorgroupNames;

    public String getHostName() {
        return hostName;
    }
    
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    @Validator(ConnectionStringValidator.class)
    public String getConnectionString() {
        return connectionString;
    }
    
    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }
    
    public UUID getHardwareUuid() {
        return hardwareUuid;
    }
    
    public void setHardwareUuid(UUID hardwareUuid) {
        this.hardwareUuid = hardwareUuid;
    }
    
    public String getTlsPolicyId() {
        return tlsPolicyId;
    }
    
    public void setTlsPolicyId(String tlsPolicyId) {
        this.tlsPolicyId = tlsPolicyId;
    }

    public String getFlavorgroupName() {
        return flavorgroupName;
    }

    public void setFlavorgroupName(String flavorgroupName) {
        this.flavorgroupName = flavorgroupName;
    }

    public List<String> getFlavorgroupNames() {
        return flavorgroupNames;
    }
    
    public void setFlavorgroupNames(List<String> flavorgroupNames) {
        this.flavorgroupNames = flavorgroupNames;
    }
    
}