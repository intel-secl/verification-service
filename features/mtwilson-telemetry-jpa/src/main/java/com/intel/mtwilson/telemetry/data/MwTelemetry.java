/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.telemetry.data;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author hdxia
 */
@Entity
@Table(name = "mw_telemetry")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "MwTelemetry.findAll", query = "SELECT m FROM MwTelemetry m"),
    @NamedQuery(name = "MwTelemetry.findById", query = "SELECT m FROM MwTelemetry m WHERE m.id = :id"),
    @NamedQuery(name = "MwTelemetry.findByCreateDate", query = "SELECT m FROM MwTelemetry m WHERE m.createDate = :createDate"),
    @NamedQuery(name = "MwTelemetry.findByHostNum", query = "SELECT m FROM MwTelemetry m WHERE m.hostNum = :hostNum")})
public class MwTelemetry implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "id")
    private String id;
    @Column(name = "create_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createDate;
    @Basic(optional = false)
    @Column(name = "host_num")
    private Integer hostNum;

    public MwTelemetry() {
    }

    public MwTelemetry(String id) {
        this.id = id;
    }

    public MwTelemetry(String id, Integer hostNum) {
        this.id = id;
        this.hostNum = hostNum;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Integer getHostNum() {
        return hostNum;
    }

    public void setHostNum(Integer hostNum) {
        this.hostNum = hostNum;
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
        if (!(object instanceof MwTelemetry)) {
            return false;
        }
        MwTelemetry other = (MwTelemetry) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.intel.mtwilson.features.telemetry.jpa.MwTelemetry[ id=" + id + " ]";
    }
    
}
