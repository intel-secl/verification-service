/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.setup.tasks;

import com.intel.dcsg.cpg.crypto.Aes128;
import com.intel.mtwilson.My;
import com.intel.mtwilson.setup.LocalSetupTask;
import javax.crypto.SecretKey;
import org.apache.commons.codec.binary.Base64;

/**
 *
 * @author rksavino
 * 
 */
public class CreateDataEncryptionKey extends LocalSetupTask {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CreateDataEncryptionKey.class);
    
    public static final String DATA_ENCRYPTION_KEY = "mtwilson.as.dek";
    
    public String getDataEncryptionKeyBase64() {
        return getConfiguration().get(DATA_ENCRYPTION_KEY, My.configuration().getDataEncryptionKeyBase64());
    }
    
    public void setDataEncryptionKeyBase64(String dekBase64) {
        getConfiguration().set(DATA_ENCRYPTION_KEY, dekBase64);
    }
    
    @Override
    public void configure() throws Exception { }
    
    @Override
    public void validate() throws Exception {
        String dataEncryptionKeyBase64 = getDataEncryptionKeyBase64();
        if (dataEncryptionKeyBase64 == null || dataEncryptionKeyBase64.isEmpty()) {
            validation("Data encryption key is not configured");
        }
        if (!Base64.isBase64(dataEncryptionKeyBase64)) {
            validation("Data encryption key is not formatted correctly");
        }
    }
    
    @Override
    public void execute() throws Exception {
        System.out.println(String.format("Generating data encryption key %s...", DATA_ENCRYPTION_KEY));
        SecretKey dek = Aes128.generateKey();
        String dekBase64 = Base64.encodeBase64String(dek.getEncoded());
        setDataEncryptionKeyBase64(dekBase64);
    }
}
