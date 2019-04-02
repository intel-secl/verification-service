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
@Table(name = "mw_link_flavor_flavorgroup")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "MwLinkFlavorFlavorgroup.findAll", query = "SELECT m FROM MwLinkFlavorFlavorgroup m"),
    @NamedQuery(name = "MwLinkFlavorFlavorgroup.findById", query = "SELECT m FROM MwLinkFlavorFlavorgroup m WHERE m.id = :id"),
    @NamedQuery(name = "MwLinkFlavorFlavorgroup.findByFlavorId", query = "SELECT m FROM MwLinkFlavorFlavorgroup m WHERE m.flavorId = :flavorId"),
    @NamedQuery(name = "MwLinkFlavorFlavorgroup.findByFlavorgroupId", query = "SELECT m FROM MwLinkFlavorFlavorgroup m WHERE m.flavorgroupId = :flavorgroupId"),
    @NamedQuery(name = "MwLinkFlavorFlavorgroup.findByBothIds", query = "SELECT m FROM MwLinkFlavorFlavorgroup m WHERE m.flavorId = :flavorId AND m.flavorgroupId = :flavorgroupId")})
public class MwLinkFlavorFlavorgroup implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "id")
    private String id;
    @Basic(optional = false)
    @Column(name = "flavor_id")
    private String flavorId;
    @Basic(optional = false)
    @Column(name = "flavorgroup_id")
    private String flavorgroupId;

    public MwLinkFlavorFlavorgroup() {
    }

    public MwLinkFlavorFlavorgroup(String id) {
        this.id = id;
    }

    public MwLinkFlavorFlavorgroup(String id, String flavorId, String flavorgroupId) {
        this.id = id;
        this.flavorId = flavorId;
        this.flavorgroupId = flavorgroupId;
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

    public String getFlavorgroupId() {
        return flavorgroupId;
    }

    public void setFlavorgroupId(String flavorgroupId) {
        this.flavorgroupId = flavorgroupId;
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
        if (!(object instanceof MwLinkFlavorFlavorgroup)) {
            return false;
        }
        MwLinkFlavorFlavorgroup other = (MwLinkFlavorFlavorgroup) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.intel.mtwilson.flavor.controller.MwLinkFlavorFlavorgroup[ id=" + id + " ]";
    }
    
}
