/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.data;

import com.intel.mtwilson.util.ASDataCipher;
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
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dtiwari
 */
@Entity
@Table(name = "mw_host_credential")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "MwHostCredential.findAll", query = "SELECT m FROM MwHostCredential m"),
    @NamedQuery(name = "MwHostCredential.findById", query = "SELECT m FROM MwHostCredential m WHERE m.id = :id ORDER BY m.createdTs DESC"),
    @NamedQuery(name = "MwHostCredential.findByHostId", query = "SELECT m FROM MwHostCredential m WHERE m.hostId = :hostId ORDER BY m.createdTs DESC"),
    @NamedQuery(name = "MwHostCredential.findByHostName", query = "SELECT m FROM MwHostCredential m WHERE m.hostName = :hostName ORDER BY m.createdTs DESC"),
    @NamedQuery(name = "MwHostCredential.findByHardwareUuid", query = "SELECT m FROM MwHostCredential m WHERE m.hardwareUuid = :hardwareUuid ORDER BY m.createdTs DESC")})
public class MwHostCredential implements Serializable {
    @Transient
    private transient Logger log = LoggerFactory.getLogger(getClass());

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "id")
    private String id;
    @Column(name = "host_id")
    private String hostId;
    @Column(name = "hardware_uuid")
    private String hardwareUuid;
    @Column(name = "host_name")
    private String hostName;
    @Basic(optional = false)
    @Column(name = "credential")
    private String credential;
    @Column(name = "created_ts")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdTs;

    @Transient
    private transient String credentialInPlain; // the decrypted version
    
    public MwHostCredential() {
    }

    public MwHostCredential(String id) {
        this.id = id;
    }

    public MwHostCredential(String id, String name, String login) {
        this.id = id;
        this.hostId = name;
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

    public String getHardwareUuid() {
        return hardwareUuid;
    }

    public void setHardwareUuid(String hardwareUuid) {
        this.hardwareUuid = hardwareUuid;
    }

    public String getCredential() {
        if (credentialInPlain == null && credential != null) {
            try {
                credentialInPlain = ASDataCipher.cipher.decryptString(credential);
                //do not log senstive info
                //log.debug("MwHostCredential ASDataCipher plainText = {}", passwordInPlain);
                log.debug("MwHostCredential ASDataCipher cipherText = {}", credential);
            } catch (Exception e) {
                log.error("Cannot decrypt host pre-registration credentials", e);
                throw new IllegalArgumentException("Cannot decrypt host pre-registration credentials.");
            }
        }
        return credentialInPlain;
    }

    public void setCredential(String credential) {
        this.credentialInPlain = credential;
        if (credentialInPlain == null) {
            this.credential = null;
        } else {
            this.credential = ASDataCipher.cipher.encryptString(credentialInPlain);
        }
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }
    
    public Date getCreatedTs() {
        return createdTs;
    }

    public void setCreatedTs(Date createdTs) {
        this.createdTs = createdTs;
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
        if (!(object instanceof MwHostCredential)) {
            return false;
        }
        MwHostCredential other = (MwHostCredential) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.intel.mtwilson.flavor.data.MwHostCredential[ id=" + id + " ]";
    }
    
}
