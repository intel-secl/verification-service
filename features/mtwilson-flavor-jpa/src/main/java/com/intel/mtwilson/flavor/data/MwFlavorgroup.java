/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.data;

import com.intel.mtwilson.flavor.converter.FlavorMatchPolicyCollectionConverter;
import com.intel.mtwilson.flavor.model.FlavorMatchPolicyCollection;
import java.io.Serializable;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author rksavino
 */
@Entity
@Table(name = "mw_flavorgroup")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "MwFlavorgroup.findAll", query = "SELECT m FROM MwFlavorgroup m"),
    @NamedQuery(name = "MwFlavorgroup.findById", query = "SELECT m FROM MwFlavorgroup m WHERE m.id = :id"),
    @NamedQuery(name = "MwFlavorgroup.findByName", query = "SELECT m FROM MwFlavorgroup m WHERE m.name = :name"),
    @NamedQuery(name = "MwFlavorgroup.findByNameLike", query = "SELECT m FROM MwFlavorgroup m WHERE m.name LIKE :name"), // it's the caller's responsibility to add "%" before and/or after the name value
    @NamedQuery(name = "MwFlavorgroup.findByFlavorTypeMatchPolicy", query = "SELECT m FROM MwFlavorgroup m WHERE m.flavorTypeMatchPolicy = :flavorTypeMatchPolicy")})
public class MwFlavorgroup implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "id")
    private String id;
    @Basic(optional = false)
    @Column(name = "name")
    private String name;
    @Basic(optional = false)
    @Lob
    @Column(name = "flavor_type_match_policy", columnDefinition = "json")
    @Convert(converter = FlavorMatchPolicyCollectionConverter.class)
    private FlavorMatchPolicyCollection flavorTypeMatchPolicy;
    
    public MwFlavorgroup() {
    }

    public MwFlavorgroup(String id) {
        this.id = id;
    }

    public MwFlavorgroup(String id, String name, FlavorMatchPolicyCollection flavorTypeMatchPolicy) {
        this.id = id;
        this.name = name;
        this.flavorTypeMatchPolicy = flavorTypeMatchPolicy;
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

    public FlavorMatchPolicyCollection getFlavorTypeMatchPolicy() {
        return flavorTypeMatchPolicy;
    }

    public void setFlavorTypeMatchPolicy(FlavorMatchPolicyCollection flavorTypeMatchPolicy) {
        this.flavorTypeMatchPolicy = flavorTypeMatchPolicy;
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
        if (!(object instanceof MwFlavorgroup)) {
            return false;
        }
        MwFlavorgroup other = (MwFlavorgroup) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.intel.mtwilson.flavor.controller.MwFlavorgroup[ id=" + id + " ]";
    }
    
}
