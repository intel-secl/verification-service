/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.data;

import com.intel.mtwilson.flavor.converter.StringMapConverter;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;
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
@Table(name = "mw_queue")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "MwQueue.findAll", query = "SELECT m FROM MwQueue m"),
    @NamedQuery(name = "MwQueue.findById", query = "SELECT m FROM MwQueue m WHERE m.id = :id"),
    @NamedQuery(name = "MwQueue.findByQueueAction", query = "SELECT m FROM MwQueue m WHERE m.queueAction = :queueAction"),
    @NamedQuery(name = "MwQueue.findByActionParameters", query = "SELECT m FROM MwQueue m WHERE m.actionParameters = :actionParameters"),
    @NamedQuery(name = "MwQueue.findByQueueStates", query = "SELECT m FROM MwQueue m WHERE m.status IN :queueStates")})
public class MwQueue implements Serializable {
    
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "id")
    private String id;
    @Basic(optional = false)
    @Column(name = "queue_action")
    private String queueAction;
    @Basic(optional = false)
    @Lob
    @Column(name = "action_parameters", columnDefinition = "json")
    @Convert(converter = StringMapConverter.class)
    private Map<String, String> actionParameters;
    @Basic(optional = false)
    @Column(name = "created")
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;
    @Basic(optional = false)
    @Column(name = "updated")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updated;
    @Basic(optional = false)
    @Column(name = "status")
    private String status;
    @Basic(optional = false)
    @Column(name = "message")
    private String message;
    
    public MwQueue() { }
    
    public MwQueue(String id) {
        this.id = id;
    }
    
    public MwQueue(String id, String queueAction, Map<String, String> actionParameters) {
        this.id = id;
        this.queueAction = queueAction;
        this.actionParameters = actionParameters;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getQueueAction() {
        return queueAction;
    }
    
    public void setQueueAction(String queueAction) {
        this.queueAction = queueAction;
    }
    
    public Map<String, String> getActionParameters() {
        return actionParameters;
    }
    
    public void setActionParameters(Map<String, String> actionParameters) {
        this.actionParameters = actionParameters;
    }
    
    public String getActionParameter(String p) {
        return actionParameters.get(p);
    }
    
    public void setActionParameter(String p, String v) {
        this.actionParameters.put(p, v);
    }
    
    public Date getCreated() {
        return created;
    }
    
    public void setCreated(Date created) {
        this.created = created;
    }
    
    public Date getUpdated() {
        return updated;
    }
    
    public void setUpdated(Date updated) {
        this.updated = updated;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
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
        if (!(object instanceof MwQueue)) {
            return false;
        }
        MwQueue other = (MwQueue) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }
    
    @Override
    public String toString() {
        return "com.intel.mtwilson.flavor.controller.MwQueue[ id=" + id + " ]";
    }
}
