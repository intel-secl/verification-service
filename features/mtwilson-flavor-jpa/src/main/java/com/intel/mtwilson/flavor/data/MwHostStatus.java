/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.data;

import com.intel.mtwilson.flavor.converter.HostManifestConverter;
import com.intel.mtwilson.flavor.converter.HostStatusConverter;
import com.intel.mtwilson.flavor.model.HostStatusInformation;
import com.intel.mtwilson.core.common.model.HostManifest;
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
import com.intel.mtwilson.audit.handler.AuditEventHandler;

/**
 *
 * @author rksavino
 */
@Entity
@Customizer(AuditEventHandler.class)
@Table(name = "mw_host_status")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "MwHostStatus.findAll", query = "SELECT m FROM MwHostStatus m"),
    @NamedQuery(name = "MwHostStatus.findById", query = "SELECT m FROM MwHostStatus m WHERE m.id = :id"),
    @NamedQuery(name = "MwHostStatus.findByHostId", query = "SELECT m FROM MwHostStatus m WHERE m.hostId = :hostId"),
    @NamedQuery(name = "MwHostStatus.findByStatus", query = "SELECT m FROM MwHostStatus m WHERE m.status = :status"),
    @NamedQuery(name = "MwHostStatus.findByCreated", query = "SELECT m FROM MwHostStatus m WHERE m.created = :created"),
    @NamedQuery(name = "MwHostStatus.findByHostReport", query = "SELECT m FROM MwHostStatus m WHERE m.hostManifest = :hostManifest")})
public class MwHostStatus implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "id")
    private String id;
    @Basic(optional = false)
    @Column(name = "host_id")
    private String hostId;
    @Basic(optional = true)
    @Lob
    @Column(name = "status", columnDefinition = "json")
    @Convert(converter = HostStatusConverter.class)
    private HostStatusInformation status;
    @Basic(optional = false)
    @Column(name = "created")
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;
    @Basic(optional = true)
    @Lob
    @Column(name = "host_report", columnDefinition = "json")
    @Convert(converter = HostManifestConverter.class)
    private HostManifest hostManifest;

    public MwHostStatus() {
    }

    public MwHostStatus(String id) {
        this.id = id;
    }

    public MwHostStatus(String id, String hostId, HostStatusInformation status) {
        this.id = id;
        this.hostId = hostId;
        this.status = status;
    }
    
    public MwHostStatus(String id, String hostId, HostStatusInformation status, HostManifest hostManifest) {
        this.id = id;
        this.hostId = hostId;
        this.status = status;
        this.hostManifest = hostManifest;
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
    
    public HostStatusInformation getStatus() {
        return status;
    }

    public void setStatus(HostStatusInformation status) {
        this.status = status;
    }
    
    public Date getCreated() {
        return created;
    }
    
    public void setCreated(Date created) {
        this.created = created;
    }
    
    public HostManifest getHostManifest() {
        return hostManifest;
    }

    public void setHostManifest(HostManifest hostReport) {
        this.hostManifest = hostReport;
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
        if (!(object instanceof MwHostStatus)) {
            return false;
        }
        MwHostStatus other = (MwHostStatus) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.intel.mtwilson.flavor.controller.MwHostStatus[ id=" + id + " ]";
    }
}
