/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.telemetry.rest.v2.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.jaxrs2.Document;
import java.util.Date;

/**
 *
 * @author hdxia
 */   

@JacksonXmlRootElement(localName="report")
public class TeleReport extends Document{
    private TelemetryCollection records;
    @JsonIgnore
    private String saml;
    private Date created;
    private Date expiration;

    public TelemetryCollection getRecords() {
        return records;
    }

    public void setRecords(TelemetryCollection records) {
        this.records = records;
    }

    public String getSaml() {
        return saml;
    }

    public void setSaml(String saml) {
        this.saml = saml;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getExpiration() {
        return expiration;
    }

    public void setExpiration(Date expiration) {
        this.expiration = expiration;
    }
}