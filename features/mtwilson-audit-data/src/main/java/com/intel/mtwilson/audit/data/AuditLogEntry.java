/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.audit.data;

import com.intel.mtwilson.audit.converter.AuditDataConverter;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author dsmagadx
 */
@Entity
@Table(name = "mw_audit_log_entry")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "AuditLogEntry.findAll", query = "SELECT a FROM AuditLogEntry a"),
    @NamedQuery(name = "AuditLogEntry.findById", query = "SELECT a FROM AuditLogEntry a WHERE a.id = :id"),
    @NamedQuery(name = "AuditLogEntry.findByEntityId", query = "SELECT a FROM AuditLogEntry a WHERE a.entityId = :entityId"),
    @NamedQuery(name = "AuditLogEntry.findByEntityType", query = "SELECT a FROM AuditLogEntry a WHERE a.entityType = :entityType"),
    @NamedQuery(name = "AuditLogEntry.findByCreateDt", query = "SELECT a FROM AuditLogEntry a WHERE a.created = :created"),
    @NamedQuery(name = "AuditLogEntry.findByAction", query = "SELECT a FROM AuditLogEntry a WHERE a.action = :action")})
public class AuditLogEntry implements Serializable {
    @Column(name = "entity_id")
    private String entityId;
    @Basic(optional = false)
    @Column(name = "created")
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "id")
    private String id;
    @Basic(optional = false)
    @Column(name = "entity_type")
    private String entityType;
    @Basic(optional = false)
    @Column(name = "action")
    private String action;
    @Basic(optional = false)
    @Lob
    @Column(name = "data", columnDefinition = "json")
    @Convert(converter = AuditDataConverter.class)
    private AuditTableData data;

    public AuditLogEntry() {
    }

    public AuditLogEntry(String id) {
        this.id = id;
    }

    public AuditLogEntry(String id, String entityId, String entityType, Date created, String action) {
        this.id = id;
        this.entityId = entityId;
        this.entityType = entityType;
        this.created = created;
        this.action = action;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }
    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public AuditTableData getData() {
        return data;
    }

    public void setData(AuditTableData data) {
        this.data = data;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof AuditLogEntry)) {
            return false;
        }
        AuditLogEntry other = (AuditLogEntry) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.intel.mtwilson.audit.data.AuditLogEntry[ id=" + id + " ]";
    }
}
