/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.data;

import com.intel.mtwilson.core.flavor.model.Flavor;
import com.intel.mtwilson.flavor.converter.FlavorConverter;
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

/**
 *
 * @author rksavino
 */
@Entity
@Table(name = "mw_flavor")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "MwFlavor.findAll", query = "SELECT m FROM MwFlavor m"),
    @NamedQuery(name = "MwFlavor.findById", query = "SELECT m FROM MwFlavor m WHERE m.id = :id"),
    @NamedQuery(name = "MwFlavor.findByContent", query = "SELECT m FROM MwFlavor m WHERE m.content = :content")})
public class MwFlavor implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "id")
    private String id;

    @Basic(optional = false)
    @Column(name = "label")
    private String label;

    @Basic(optional = false)
    @Lob
    @Column(name = "content", columnDefinition = "json")
    @Convert(converter = FlavorConverter.class)
    private Flavor content;

    @Basic(optional = false)
    @Column(name = "signature")
    private String signature;

    @Basic(optional = false)
    @Column(name = "created")
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;
    
    public MwFlavor() {
    }

    public MwFlavor(String id) {
        this.id = id;
    }

    public MwFlavor(String id, Flavor content) {
        this.id = id;
        this.content = content;
        if (this.content != null && this.content.getMeta() != null) {
            this.content.getMeta().setId(id);
        }
        if (this.content != null && this.content.getMeta() != null
                && this.content.getMeta().getDescription() != null) {
            this.label = this.content.getMeta().getDescription().getLabel();
        }
    }

    public MwFlavor(String id, Flavor content, String signature) {
        this.id = id;
        this.content = content;
        this.signature = signature;
        if (this.content != null && this.content.getMeta() != null) {
            this.content.getMeta().setId(id);
        }
        if (this.content != null && this.content.getMeta() != null
                && this.content.getMeta().getDescription() != null) {
            this.label = this.content.getMeta().getDescription().getLabel();
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
        if (this.content != null && this.content.getMeta() != null) {
            this.content.getMeta().setId(id);
        }
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Flavor getContent() {
        return content;
    }

    public void setContent(Flavor content) {
        this.content = content;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getSignature() {
        return signature;
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
        if (!(object instanceof MwFlavor)) {
            return false;
        }
        MwFlavor other = (MwFlavor) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.intel.mtwilson.flavor.controller.MwFlavor[ id=" + id + " ]";
    }
    
}
