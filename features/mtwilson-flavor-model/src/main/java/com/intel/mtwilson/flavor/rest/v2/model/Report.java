/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.rest.v2.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.core.verifier.policy.TrustReport;
import com.intel.mtwilson.flavor.model.TrustInformation;
import com.intel.mtwilson.jaxrs2.Document;
import java.util.Date;
import com.intel.mtwilson.core.common.model.HostInfo;
import com.intel.mtwilson.jaxrs2.JsonOnly;

/**
 *
 * @author ssbangal
 */
@JacksonXmlRootElement(localName="report")
public class Report extends Document{
    private UUID hostId;
    private TrustInformation trustInformation;
    @JsonIgnore
    private TrustReport trustReport;
    @JsonIgnore
    private String saml;
    @JsonOnly
    private HostInfo hostInfo;
    private Date created;
    private Date expiration;

    public UUID getHostId() {
        return hostId;
    }

    public void setHostId(UUID hostId) {
        this.hostId = hostId;
    }
    
    public TrustInformation getTrustInformation() {
        return trustInformation;
    }

    public void setTrustInformation(TrustInformation trustInformation) {
        this.trustInformation = trustInformation;
    }
    
    public TrustReport getTrustReport() {
        return trustReport;
    }

    public void setTrustReport(TrustReport trustReport) {
        this.trustReport = trustReport;
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

    public HostInfo getHostInfo() {
        return this.trustReport.getHostManifest().getHostInfo();
    }

}
