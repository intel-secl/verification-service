/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.setup.tasks;

import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.dcsg.cpg.crypto.RandomUtil;
import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.dcsg.cpg.io.pem.Pem;
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
import com.intel.mtwilson.core.common.utils.CertificateUtils;

import java.io.*;
import java.net.URL;
import java.security.*;
import java.security.cert.*;
import java.security.cert.Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.util.Properties;

import com.intel.mtwilson.core.common.utils.AASConstants;
import org.apache.commons.io.IOUtils;

/**
 * Depends on CreateCertificateAuthorityKey to create the cakey first
 *
 * @author jbuhacoff
 */
public class CreateSamlCertificate extends LocalSetupTask {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CreateSamlCertificate.class);
    public static final String SAML_CERTIFICATE_DN = "saml.certificate.dn";
    public static final String SAML_KEYSTORE_FILE = "saml.keystore.file";
    public static final String SAML_KEYSTORE_PASSWORD = "saml.keystore.password";
    private static final String SAML_TRUSTSTORE_PASSWORD = "changeit";
    public static final String SAML_KEY_ALIAS = "saml.key.alias";
    private static final String SAML_CERTIFICATE_CERT_PEM = "saml.crt.pem";
    private static final String SAML_KEYSTORE_NAME="SAML.p12";
    private static final String DEFAULT_SAML_DN = "CN=mtwilson-saml";
    private static final String DEFAULT_SAML_KEYSTORE_ALIAS = "saml-key";
    private static final String SAML_KEYSTORE_FORMAT = "PKCS12";
    private static final String BEARER_TOKEN_ENV = "BEARER_TOKEN";
    private File truststorep12;
    private Configuration configuration;

    public String getSamlKeystorePassword() {
        return configuration.get(SAML_KEYSTORE_PASSWORD, null); // no default here, will return null if not configured: only the configure() method will generate a new random password if necessary
    }

    @Override
    protected void configure() throws Exception {
        ConfigurationProvider configurationProvider = ConfigurationFactory.getConfigurationProvider();
        configuration = configurationProvider.load();

        truststorep12 = My.configuration().getTruststoreFile();
        if (configuration.get(SAML_KEYSTORE_PASSWORD) == null || configuration.get(SAML_KEYSTORE_PASSWORD).isEmpty()) {
            configuration.set(SAML_KEYSTORE_PASSWORD, RandomUtil.randomBase64String(16));
        }
        if (configuration.get(SAML_KEYSTORE_FILE) == null || configuration.get(SAML_KEYSTORE_FILE).isEmpty()) {
            configuration.set(SAML_KEYSTORE_FILE, My.configuration().getDirectoryPath() + File.separator + SAML_KEYSTORE_NAME);
        }
        if (configuration.get(SAML_CERTIFICATE_DN) == null || configuration.get(SAML_CERTIFICATE_DN).isEmpty()) {
            configuration.set(SAML_CERTIFICATE_DN, DEFAULT_SAML_DN);
        }
        if (configuration.get(SAML_KEY_ALIAS) == null || configuration.get(SAML_KEY_ALIAS).isEmpty()) {
            configuration.set(SAML_KEY_ALIAS, DEFAULT_SAML_KEYSTORE_ALIAS);
        }
        configurationProvider.save(configuration);
    }

    @Override
    protected void validate() throws Exception {
        if (!new File(configuration.get(SAML_KEYSTORE_FILE)).exists()) {
            validation("Saml keystore file is missing");
        }
        if (!getConfigurationFaults().isEmpty()) {
            return;
        }

    }

    @Override
    protected void execute() throws Exception {
        KeyPair keyPair = RsaUtil.generateRsaKeyPair(3072);
        RSAPrivateKey privKey = (RSAPrivateKey) keyPair.getPrivate();
        Properties properties = new Properties();

        TlsPolicy tlsPolicy = TlsPolicyBuilder.factory().strictWithKeystore(truststorep12, SAML_TRUSTSTORE_PASSWORD).build();

        String token = System.getenv(BEARER_TOKEN_ENV);
        if (token == null || token.isEmpty() ){
            configuration("BEARER_TOKEN not set in the environment");
            return;
        }
        properties.setProperty(AASConstants.BEARER_TOKEN, token);

        CMSClient cmsClient = new CMSClient(properties, new TlsConnection(new URL(configuration.get(AASConstants.CMS_BASE_URL)), tlsPolicy));

        X509Certificate[] samlCertChain = cmsClient.getCertificate(CertificateUtils.getCSR(keyPair, configuration.get(SAML_CERTIFICATE_DN)).toString(), CertificateType.SIGNING.getValue());
        FileOutputStream newp12 = new FileOutputStream(configuration.get(SAML_KEYSTORE_FILE));

        try {
            KeyStore keystore = KeyStore.getInstance(SAML_KEYSTORE_FORMAT);
            keystore.load(null, configuration.get(SAML_KEYSTORE_PASSWORD).toCharArray());
            Certificate[] chain = {samlCertChain[0]};
            keystore.setKeyEntry(configuration.get(SAML_KEY_ALIAS), privKey, configuration.get(SAML_KEYSTORE_PASSWORD).toCharArray(), chain);
            keystore.store(newp12, configuration.get(SAML_KEYSTORE_PASSWORD).toCharArray());

        } catch (Exception e) {
            throw e;
        } finally {
            newp12.close();
        }

        Pem certEncoded;
        String certificateChainPem = "";
        for (int certIndex = 0; certIndex < samlCertChain.length; certIndex ++) {
            certEncoded = new Pem("CERTIFICATE", samlCertChain[certIndex].getEncoded());
            certificateChainPem = certificateChainPem + String.format("%s\n", certEncoded.toString());
        }
        File certificateChainPemFile = new File(My.configuration().getDirectoryPath() + File.separator + SAML_CERTIFICATE_CERT_PEM);
        try (FileOutputStream certificateChainPemFileOut = new FileOutputStream(certificateChainPemFile)) {
            IOUtils.write(certificateChainPem, certificateChainPemFileOut);
        } catch (IOException e) {
            validation(e, "Cannot write to saml.crt.pem");
        }
    }

}
