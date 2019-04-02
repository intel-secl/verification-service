/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.data;

import com.intel.mtwilson.audit.handler.AuditEventHandler;
import com.intel.mtwilson.core.verifier.policy.TrustReport;
import com.intel.mtwilson.flavor.converter.TrustReportConverter;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;
import org.eclipse.persistence.annotations.Customizer;

/**
 *
 * @author rksavino
 */
@Entity
@Customizer(AuditEventHandler.class)
@Table(name = "mw_report")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "MwReport.findAll", query = "SELECT m FROM MwReport m"),
    @NamedQuery(name = "MwReport.findById", query = "SELECT m FROM MwReport m WHERE m.id = :id"),
    @NamedQuery(name = "MwReport.findByHostId", query = "SELECT m FROM MwReport m WHERE m.hostId = :hostId"),
    @NamedQuery(name = "MwReport.findByTrustReport", query = "SELECT m FROM MwReport m WHERE m.trustReport = :trustReport"),
    @NamedQuery(name = "MwReport.findByCreated", query = "SELECT m FROM MwReport m WHERE m.created = :created"),
    @NamedQuery(name = "MwReport.findByExpiration", query = "SELECT m FROM MwReport m WHERE m.expiration = :expiration"),
    @NamedQuery(name = "MwReport.findBySaml", query = "SELECT m FROM MwReport m WHERE m.saml = :saml")})
public class MwReport implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "id")
    private String id;
    @Basic(optional = false)
    @Column(name = "host_id")
    private String hostId;
    @Basic(optional = false)
    @Lob
    @Column(name = "trust_report", columnDefinition = "json")
    @Convert(converter = TrustReportConverter.class)
    private TrustReport trustReport;
    @Basic(optional = false)
    @Column(name = "created")
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;
    @Basic(optional = false)
    @Column(name = "expiration")
    @Temporal(TemporalType.TIMESTAMP)
    private Date expiration;
    @Basic(optional = false)
    @Column(name = "saml")
    private String saml;

    public MwReport() {
    }

    public MwReport(String id) {
        this.id = id;
    }

    public MwReport(String id, String hostId, TrustReport trustReport, Date created, Date expiration, String saml) {
        this.id = id;
        this.hostId = hostId;
        this.trustReport = trustReport;
        this.created = created;
        this.expiration = expiration;
        this.saml = saml;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHostId() {
        return hostId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }

    public TrustReport getTrustReport() {
        return trustReport;
    }

    public void setTrustReport(TrustReport trustReport) {
        this.trustReport = trustReport;
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

    public String getSaml() {
        return saml;
    }

    public void setSaml(String saml) {
        this.saml = saml;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof MwReport)) {
            return false;
        }
        MwReport other = (MwReport) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.intel.mtwilson.flavor.controller.MwReport[ id=" + id + " ]";
    }
    
}
