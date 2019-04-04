/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.esxi.cluster.jdbi;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.util.ASDataCipher;

/**
 * Represents a single row in the mw_esxi_cluster table.
 *
 * @author avaguayo + hxia5
 */
public class EsxiClusterRecord {

    private UUID id;
    private String connectionString;  //encrypted format.
    private String clusterName;
    private String tlsPolicyId;

    public EsxiClusterRecord() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getConnectionString() {
        return connectionString;
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getTlsPolicyId() {
        return tlsPolicyId;
    }

    public void setTlsPolicyId(String tlsPolicyId) {
        this.tlsPolicyId = tlsPolicyId;
    }    
        
    public String getConnectionStringInPlainText() {
        String connectionStringPlainText;
        try {
            connectionStringPlainText = ASDataCipher.cipher.decryptString(connectionString);
            return connectionStringPlainText;
        } catch (Exception e) {
            // this will happen if the data is being decrypted with the wrong key (which will happen if someone reinstalled mt wilson and kept the data but didn't save the data encryption key)
            // it may also happen if the data wasn't encrypted in the first place
            throw new IllegalArgumentException("Cannot decrypt connection credentials; check the key or delete and re-register the cluster");
        }
    }

    public void setConnectionStringInPlainText(String connectionStringPlainText) {
        connectionString = ASDataCipher.cipher.encryptString(connectionStringPlainText);
    }
}
