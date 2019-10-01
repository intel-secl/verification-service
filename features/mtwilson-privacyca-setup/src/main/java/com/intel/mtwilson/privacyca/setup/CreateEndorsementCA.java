/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.privacyca.setup;

import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.dcsg.cpg.crypto.RandomUtil;
import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.dcsg.cpg.tls.policy.TlsConnection;
import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import com.intel.dcsg.cpg.tls.policy.TlsPolicyBuilder;
import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.mtwilson.My;
import com.intel.mtwilson.configuration.ConfigurationFactory;
import com.intel.mtwilson.configuration.ConfigurationProvider;
import com.intel.mtwilson.core.common.model.CertificateType;
import com.intel.mtwilson.jaxrs2.client.CMSClient;
import com.intel.mtwilson.setup.LocalSetupTask;
import com.intel.mtwilson.core.common.utils.AASTokenFetcher;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.util.List;
import java.util.Properties;

import com.intel.mtwilson.core.common.utils.AASConstants;
import gov.niarl.his.privacyca.TpmUtils;

import com.intel.mtwilson.core.common.utils.CertificateUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class CreateEndorsementCA extends LocalSetupTask {
    private static Logger log = LoggerFactory.getLogger(CreateEndorsementCA.class);
    private File endorsementPemFile;
    private File endorsementExternalPemFile;
    private String endorsementPassword;
    private String endorsementIssuer;
    private File endorsementP12;
    private File truststorep12;
    private int endorsementCertificateValidityDays;

    @Override
    protected void configure() throws Exception {
        endorsementPemFile = My.configuration().getPrivacyCaEndorsementCacertsFile();
        endorsementExternalPemFile = My.configuration().getPrivacyCaEndorsementExternalCacertsFile();
        endorsementIssuer = My.configuration().getPrivacyCaEndorsementIssuer();
        endorsementP12 = My.configuration().getPrivacyCaEndorsementP12();
        endorsementPassword = My.configuration().getPrivacyCaEndorsementPassword();
        endorsementCertificateValidityDays = My.configuration().getPrivacyCaEndorsementValidityDays();
        truststorep12 = My.configuration().getTruststoreFile();

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
        KeyPair keyPair = RsaUtil.generateRsaKeyPair(3072);
        ConfigurationProvider configurationProvider = ConfigurationFactory.getConfigurationProvider();
        Configuration configuration = configurationProvider.load();
        RSAPrivateKey privKey = (RSAPrivateKey) keyPair.getPrivate();
        Properties properties = new Properties();

        TlsPolicy tlsPolicy = TlsPolicyBuilder.factory().strictWithKeystore(truststorep12,
            "changeit").build();

        String token = new AASTokenFetcher().getAASToken(configuration.get(AASConstants.MC_FIRST_USERNAME),configuration.get(AASConstants.MC_FIRST_PASSWORD),
            new TlsConnection(new URL(configuration.get(AASConstants.AAS_API_URL)), tlsPolicy));
        properties.setProperty(AASConstants.BEARER_TOKEN, token);

        CMSClient cmsClient = new CMSClient(properties, new TlsConnection(new URL(configuration.get(AASConstants.CMS_BASE_URL)), tlsPolicy));

        X509Certificate cacert = cmsClient.getCertificate(CertificateUtils.getCSR(keyPair, "CN="+endorsementIssuer).toString(), CertificateType.SIGNING.getValue());
        FileOutputStream newp12 = new FileOutputStream(endorsementP12.getAbsolutePath());

        try {
            KeyStore keystore = KeyStore.getInstance("PKCS12");
            keystore.load(null, endorsementPassword.toCharArray());
            Certificate[] chain = {cacert};
            keystore.setKeyEntry("1", privKey, endorsementPassword.toCharArray(), chain);
            keystore.store(newp12, endorsementPassword.toCharArray());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            newp12.close();
        }

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
