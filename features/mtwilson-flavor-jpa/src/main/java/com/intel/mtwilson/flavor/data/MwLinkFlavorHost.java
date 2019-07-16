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
@Table(name = "mw_link_flavor_host")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "MwLinkFlavorHost.findAll", query = "SELECT m FROM MwLinkFlavorHost m"),
    @NamedQuery(name = "MwLinkFlavorHost.findById", query = "SELECT m FROM MwLinkFlavorHost m WHERE m.id = :id"),
    @NamedQuery(name = "MwLinkFlavorHost.findByFlavorId", query = "SELECT m FROM MwLinkFlavorHost m WHERE m.flavorId = :flavorId"),
    @NamedQuery(name = "MwLinkFlavorHost.findByHostId", query = "SELECT m FROM MwLinkFlavorHost m WHERE m.hostId = :hostId"),
    @NamedQuery(name = "MwLinkFlavorHost.findByBothIds", query = "SELECT m FROM MwLinkFlavorHost m WHERE m.flavorId = :flavorId AND m.hostId = :hostId"),
    @NamedQuery(name = "MwLinkFlavorHost.findByHostIdAndFlavorgroupId", query = "SELECT m FROM MwLinkFlavorHost m INNER JOIN MwLinkFlavorFlavorgroup n ON m.flavorId = n.flavorId where n.flavorgroupId = :flavorgroupId and m.hostId = :hostId")})
public class MwLinkFlavorHost implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "id")
    private String id;
    @Basic(optional = false)
    @Column(name = "flavor_id")
    private String flavorId;
    @Basic(optional = false)
    @Column(name = "host_id")
    private String hostId;

    public MwLinkFlavorHost() {
    }

    public MwLinkFlavorHost(String id) {
        this.id = id;
    }

    public MwLinkFlavorHost(String id, String flavorId, String hostId) {
        this.id = id;
        this.flavorId = flavorId;
        this.hostId = hostId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFlavorId() {
        return flavorId;
    }

    public void setFlavorId(String flavorId) {
        this.flavorId = flavorId;
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
        if (!(object instanceof MwLinkFlavorHost)) {
            return false;
        }
        MwLinkFlavorHost other = (MwLinkFlavorHost) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.intel.mtwilson.flavor.controller.MwLinkFlavorHost[ id=" + id + " ]";
    }
    
}
