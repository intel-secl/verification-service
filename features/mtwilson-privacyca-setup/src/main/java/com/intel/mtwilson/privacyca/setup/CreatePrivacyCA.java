/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.privacyca.setup;

import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.dcsg.cpg.crypto.RandomUtil;
import com.intel.dcsg.cpg.tls.policy.TlsConnection;
import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import com.intel.dcsg.cpg.tls.policy.TlsPolicyBuilder;
import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.mtwilson.My;
import com.intel.mtwilson.configuration.ConfigurationFactory;
import com.intel.mtwilson.configuration.ConfigurationProvider;
import com.intel.mtwilson.core.common.model.CertificateType;
import com.intel.mtwilson.core.common.utils.AASTokenFetcher;
import com.intel.mtwilson.jaxrs2.client.CMSClient;
import com.intel.mtwilson.setup.LocalSetupTask;
import com.intel.mtwilson.core.common.utils.CertificateUtils;
import com.intel.mtwilson.core.common.utils.AASConstants;
import gov.niarl.his.privacyca.TpmUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class CreatePrivacyCA extends LocalSetupTask {
    private File identityPemFile;
    private String identityPassword;
    private String identityIssuer;
    private File identityP12;
    private File truststorep12;
    private int identityCertificateValidityDays;
    private static Logger log = LoggerFactory.getLogger(CreatePrivacyCA.class);


    @Override
    protected void configure() throws Exception {
        truststorep12 = My.configuration().getTruststoreFile();
        identityPemFile = My.configuration().getPrivacyCaIdentityCacertsFile();
        identityIssuer = My.configuration().getPrivacyCaIdentityIssuer();
        identityP12 = My.configuration().getPrivacyCaIdentityP12();
        identityPassword = My.configuration().getPrivacyCaIdentityPassword();
        identityCertificateValidityDays = My.configuration().getPrivacyCaIdentityValidityDays();
        
        if( identityPassword == null || identityPassword.isEmpty() ) {
            identityPassword = RandomUtil.randomBase64String(16); 
            getConfiguration().set("mtwilson.privacyca.aik.p12.password", identityPassword);
        }
    }

    @Override
    protected void validate() throws Exception {
        if( !identityPemFile.exists() ) {
            validation("Privacy CA certs file does not exist");
        }
        if( !identityP12.exists() ) {
            validation("Privacy CA P12 file does not exist");
        }
    }

    @Override
    protected void execute() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(3072);
        KeyPair keyPair = keyGen.generateKeyPair();
        RSAPrivateKey privKey = (RSAPrivateKey) keyPair.getPrivate();
// keyPair, "CN=" + endorsementIssuer
        ConfigurationProvider configurationProvider = ConfigurationFactory.getConfigurationProvider();
        Configuration configuration = configurationProvider.load();
        Properties properties = new Properties();

        TlsPolicy tlsPolicy = TlsPolicyBuilder.factory().strictWithKeystore(truststorep12,
            "changeit").build();
        String token = new AASTokenFetcher().getAASToken(configuration.get(AASConstants.MC_FIRST_USERNAME),configuration.get(AASConstants.MC_FIRST_PASSWORD),
            new TlsConnection(new URL(configuration.get(AASConstants.AAS_API_URL)), tlsPolicy));
        properties.setProperty(AASConstants.BEARER_TOKEN, token);

        CMSClient cmsClient = new CMSClient(properties, new TlsConnection(new URL(configuration.get(AASConstants.CMS_BASE_URL)), tlsPolicy));

        X509Certificate[] privacyCaCertChain = cmsClient.getCertificate(CertificateUtils.getCSR(keyPair, "CN="+identityIssuer).toString(), CertificateType.SIGNING_CA.getValue());

        FileOutputStream newp12 = new FileOutputStream(identityP12.getAbsolutePath());

        try {
            KeyStore keystore = KeyStore.getInstance("PKCS12");
            keystore.load(null, identityPassword.toCharArray());
            Certificate[] chain = {privacyCaCertChain[0]};
            keystore.setKeyEntry("privacy-ca", privKey, identityPassword.toCharArray(), chain);
            keystore.store(newp12, identityPassword.toCharArray());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            newp12.close();
        }

        X509Certificate pcaCert = TpmUtils.certFromP12(identityP12.getAbsolutePath(), identityPassword);
        String self = X509Util.encodePemCertificate(pcaCert);
        String existingPrivacyAuthorities = "";
        if( identityPemFile.exists() ) {
            existingPrivacyAuthorities = FileUtils.readFileToString(identityPemFile);
        }
        FileUtils.writeStringToFile(identityPemFile, String.format("%s\n%s", existingPrivacyAuthorities,self)); 
    }
    
}
