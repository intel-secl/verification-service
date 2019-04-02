/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.data;

import com.intel.mtwilson.util.ASDataCipher;
import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author rksavino
 */
@Entity
@Table(name = "mw_host")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "MwHost.findAll", query = "SELECT m FROM MwHost m"),
    @NamedQuery(name = "MwHost.findById", query = "SELECT m FROM MwHost m WHERE m.id = :id"),
    @NamedQuery(name = "MwHost.findByName", query = "SELECT m FROM MwHost m WHERE m.name = :name"),
    @NamedQuery(name = "MwHost.findByNameLike", query = "SELECT m FROM MwHost m WHERE m.name LIKE :name"), // it's the caller's responsibility to add "%" before and/or after the name value
    @NamedQuery(name = "MwHost.findByDescription", query = "SELECT m FROM MwHost m WHERE m.description = :description"),
    @NamedQuery(name = "MwHost.findByConnectionString", query = "SELECT m FROM MwHost m WHERE m.connectionString = :connectionString"),
    @NamedQuery(name = "MwHost.findByHardwareUuid", query = "SELECT m FROM MwHost m WHERE m.hardwareUuid = :hardwareUuid"),
    @NamedQuery(name = "MwHost.findByTlsPolicyId", query = "SELECT m FROM MwHost m WHERE m.tlsPolicyId = :tlsPolicyId")})
public class MwHost implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "id")
    private String id;
    @Basic(optional = false)
    @Column(name = "name")
    private String name;
    @Column(name = "description")
    private String description;
    @Basic(optional = false)
    @Column(name = "connection_string")
    private String connectionString;
    @Column(name = "hardware_uuid")
    private String hardwareUuid;
    @Column(name = "tls_policy_id")
    private String tlsPolicyId;
    
    @Transient
    private transient String connectionStringInPlainText;
    
    public MwHost() {
    }

    public MwHost(String id) {
        this.id = id;
    }

    public MwHost(String id, String name, String connectionString) {
        this.id = id;
        this.name = name;
        this.connectionString = connectionString;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getConnectionString() {
        return connectionString;
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString; 
    }

    public String getHardwareUuid() {
        return hardwareUuid;
    }

    public void setHardwareUuid(String hardwareUuid) {
        this.hardwareUuid = hardwareUuid;
    }

    public String getTlsPolicyId() {
        return tlsPolicyId;
    }

    public void setTlsPolicyId(String tlsPolicyId) {
        this.tlsPolicyId = tlsPolicyId;
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
        if (!(object instanceof MwHost)) {
            return false;
        }
        MwHost other = (MwHost) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.intel.mtwilson.flavor.controller.MwHost[ id=" + id + " ]";
    }
}
