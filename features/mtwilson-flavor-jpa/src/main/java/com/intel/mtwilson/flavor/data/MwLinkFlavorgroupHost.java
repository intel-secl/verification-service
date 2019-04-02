/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.data;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author rksavino
 */
@Entity
@Table(name = "mw_link_flavorgroup_host")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "MwLinkFlavorgroupHost.findAll", query = "SELECT m FROM MwLinkFlavorgroupHost m"),
    @NamedQuery(name = "MwLinkFlavorgroupHost.findById", query = "SELECT m FROM MwLinkFlavorgroupHost m WHERE m.id = :id"),
    @NamedQuery(name = "MwLinkFlavorgroupHost.findByFlavorgroupId", query = "SELECT m FROM MwLinkFlavorgroupHost m WHERE m.flavorgroupId = :flavorgroupId"),
    @NamedQuery(name = "MwLinkFlavorgroupHost.findByHostId", query = "SELECT m FROM MwLinkFlavorgroupHost m WHERE m.hostId = :hostId"),
    @NamedQuery(name = "MwLinkFlavorgroupHost.findByBothIds", query = "SELECT m FROM MwLinkFlavorgroupHost m WHERE m.flavorgroupId = :flavorgroupId AND m.hostId = :hostId")})
public class MwLinkFlavorgroupHost implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "id")
    private String id;
    @Basic(optional = false)
    @Column(name = "flavorgroup_id")
    private String flavorgroupId;
    @Basic(optional = false)
    @Column(name = "host_id")
    private String hostId;

    public MwLinkFlavorgroupHost() {
    }

    public MwLinkFlavorgroupHost(String id) {
        this.id = id;
    }

    public MwLinkFlavorgroupHost(String id, String flavorgroupId, String hostId) {
        this.id = id;
        this.flavorgroupId = flavorgroupId;
        this.hostId = hostId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFlavorgroupId() {
        return flavorgroupId;
    }

    public void setFlavorgroupId(String flavorgroupId) {
        this.flavorgroupId = flavorgroupId;
    }

    public String getHostId() {
        return hostId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
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
        if (!(object instanceof MwLinkFlavorgroupHost)) {
            return false;
        }
        MwLinkFlavorgroupHost other = (MwLinkFlavorgroupHost) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.intel.mtwilson.flavor.controller.MwLinkFlavorgroupHost[ id=" + id + " ]";
    }
    
}
