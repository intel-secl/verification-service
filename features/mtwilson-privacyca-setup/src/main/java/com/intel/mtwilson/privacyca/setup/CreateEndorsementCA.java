/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.privacyca.setup;

import com.intel.dcsg.cpg.crypto.RandomUtil;
import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.mtwilson.Folders;
import com.intel.mtwilson.My;
import com.intel.mtwilson.setup.LocalSetupTask;
import gov.niarl.his.privacyca.TpmUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.security.cert.X509Certificate;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author jbuhacoff
 */
public class CreateEndorsementCA extends LocalSetupTask {
    private File endorsementPemFile;
    private File endorsementExternalPemFile;
    private String endorsementPassword;
    private String endorsementIssuer;
    private File endorsementP12;
    private int endorsementCertificateValidityDays;
    
    @Override
    protected void configure() throws Exception {
        endorsementPemFile = My.configuration().getPrivacyCaEndorsementCacertsFile();
        endorsementExternalPemFile = My.configuration().getPrivacyCaEndorsementExternalCacertsFile();
        endorsementIssuer = My.configuration().getPrivacyCaEndorsementIssuer();
        endorsementP12 = My.configuration().getPrivacyCaEndorsementP12();
        endorsementPassword = My.configuration().getPrivacyCaEndorsementPassword();
        endorsementCertificateValidityDays = My.configuration().getPrivacyCaEndorsementValidityDays();
        
        if( endorsementPassword == null || endorsementPassword.isEmpty() ) {
            endorsementPassword = RandomUtil.randomBase64String(16); 
            getConfiguration().set("mtwilson.privacyca.ek.p12.password", endorsementPassword);
        }
    }

    @Override
    protected void validate() throws Exception {
        if( !endorsementPemFile.exists() ) {
            validation("Endorsement CA certs file does not exist");
        }
        if( !endorsementP12.exists() ) {
            validation("Endorsement P12 file does not exist");
        }
        if( !endorsementP12.exists() ) {
            validation("Privacy CA p12 file does not exist");
        }
    }

    @Override
    protected void execute() throws Exception {
        TpmUtils.createCaP12(2048, endorsementIssuer, endorsementPassword, endorsementP12.getAbsolutePath(), endorsementCertificateValidityDays);
        X509Certificate pcaCert = TpmUtils.certFromP12(endorsementP12.getAbsolutePath(), endorsementPassword);
        String self = X509Util.encodePemCertificate(pcaCert);
        
        // read in additional external maufacturer ECs
        String ekCacerts = "";
        String ekExternalCacertsFileContent = FileUtils.readFileToString(endorsementExternalPemFile, Charset.forName("UTF-8"));
        List<X509Certificate> ekExternalCacerts = X509Util.decodePemCertificates(ekExternalCacertsFileContent);
        if (ekExternalCacerts != null && !ekExternalCacerts.isEmpty()) {
            for (X509Certificate ekExternalCacert : ekExternalCacerts) {
                String ekExternalCacertString = X509Util.encodePemCertificate(ekExternalCacert);
                ekCacerts = ekCacerts.concat(String.format("%s\n", ekExternalCacertString));
            }
        }
        ekCacerts = ekCacerts.concat(String.format("%s\n", self));
        
        // update EK cacerts file on disk
        try(FileOutputStream out = new FileOutputStream(endorsementPemFile)) {
            IOUtils.write(ekCacerts.getBytes("UTF-8"), out);
        }
    }
}
