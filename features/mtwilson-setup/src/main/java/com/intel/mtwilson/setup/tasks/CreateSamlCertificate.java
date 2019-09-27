/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.setup.tasks;

import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.crypto.RandomUtil;
import com.intel.dcsg.cpg.crypto.RsaCredentialX509;
import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.dcsg.cpg.io.FileResource;
import com.intel.dcsg.cpg.io.pem.Pem;
import com.intel.dcsg.cpg.tls.policy.TlsConnection;
import com.intel.dcsg.cpg.tls.policy.impl.InsecureTlsPolicy;
import com.intel.dcsg.cpg.validation.Fault;
import com.intel.dcsg.cpg.x509.X509Builder;
import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.mtwilson.My;
import com.intel.mtwilson.configuration.ConfigurationFactory;
import com.intel.mtwilson.configuration.ConfigurationProvider;
import com.intel.mtwilson.core.common.model.CertificateType;
import com.intel.mtwilson.core.common.utils.AASTokenFetcher;
import com.intel.mtwilson.jaxrs2.client.CMSClient;
import com.intel.mtwilson.setup.LocalSetupTask;
import com.intel.mtwilson.setup.SetupException;
import com.intel.mtwilson.setup.utils.CertificateUtils;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.*;
import java.security.cert.*;
import java.security.cert.Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
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
    public static final String SAML_KEY_ALIAS = "saml.key.alias";
    public static final String SAML_KEY_PASSWORD = "saml.key.password";
    private static final String CMS_BASE_URL = "cms.base.url";
    private static final String AAS_API_URL = "aas.api.url";
    private static final String MC_FIRST_USERNAME = "mc.first.username";
    private static final String MC_FIRST_PASSWORD = "mc.first.password";
    private static final String SAML_CERTIFICATE_FILE = My.configuration().getDirectoryPath() + File.separator +"saml.crt";
    private File samlKeystoreFile;
    private KeyStore keystore;
    private String samlKeystorePassword;
    FileInputStream keystoreFIS;


    public String getSamlKeystoreFile() {
        return getConfiguration().get(SAML_KEYSTORE_FILE, My.configuration().getDirectoryPath() + File.separator + "mtwilson-saml.p12");
    }

    public void setSamlKeystoreFile(String samlKeystoreFile) {
        getConfiguration().set(SAML_KEYSTORE_FILE, samlKeystoreFile);
    }

    public String getSamlKeystorePassword() {
        return getConfiguration().get(SAML_KEYSTORE_PASSWORD, null); // no default here, will return null if not configured: only the configure() method will generate a new random password if necessary
    }

    public void setSamlKeystorePassword(String samlKeystorePassword) {
        getConfiguration().set(SAML_KEYSTORE_PASSWORD, samlKeystorePassword);
    }

    public String getSamlKeyAlias() {
        return getConfiguration().get(SAML_KEY_ALIAS, "mtwilson-saml");
    }

    public void setSamlKeyAlias(String samlKeyAlias) {
        getConfiguration().set(SAML_KEY_ALIAS, samlKeyAlias);
    }

    public String getSamlKeyPassword() {
        return getConfiguration().get(SAML_KEY_PASSWORD, null); // no default here, will return null if not configured: only the configure() method will generate a new random password if necessary
    }

    public void setSamlKeyPassword(String samlKeyPassword) {
        getConfiguration().set(SAML_KEY_PASSWORD, samlKeyPassword);
    }

    public String getSamlCertificateDistinguishedName() {
        return getConfiguration().get(SAML_CERTIFICATE_DN, "CN=mtwilson-saml");
    }


    public void setSamlCertificateDistinguishedName(String samlDistinguishedName) {
        getConfiguration().set(SAML_CERTIFICATE_DN, samlDistinguishedName);
    }

    @Override
    protected void configure() throws Exception {
        String samlKeystorePassword = getSamlKeystorePassword();

        if (getConfiguration().get(SAML_KEYSTORE_PASSWORD) == null || getConfiguration().get(SAML_KEYSTORE_PASSWORD).isEmpty()) {
            getConfiguration().set(SAML_KEYSTORE_PASSWORD, RandomUtil.randomBase64String(16));
        }
        if (getConfiguration().get(SAML_KEYSTORE_FILE) == null || getConfiguration().get(SAML_KEYSTORE_FILE).isEmpty()) {
            getConfiguration().set(SAML_KEYSTORE_FILE, My.configuration().getDirectoryPath() + File.separator + "SAML.p12");
            log.info(getConfiguration().get(SAML_KEYSTORE_FILE));
        }
        if (getConfiguration().get(SAML_CERTIFICATE_DN) == null || getConfiguration().get(SAML_CERTIFICATE_DN).isEmpty()) {
            getConfiguration().set(SAML_CERTIFICATE_DN, "CN=mtwilson-saml");
        }
        if (getConfiguration().get(SAML_KEY_ALIAS) == null || getConfiguration().get(SAML_KEY_ALIAS).isEmpty()) {
            getConfiguration().set(SAML_KEY_ALIAS, "saml-ley");
        }
    }

    @Override
    protected void validate() throws Exception {
        if (!new File(getConfiguration().get(SAML_KEYSTORE_FILE)).exists()) {
            validation("Saml keystore file is missing");
        }
        if (!getConfigurationFaults().isEmpty()) {
            return;
        }

    }

    @Override
    protected void execute() throws Exception {
        KeyPair keyPair = RsaUtil.generateRsaKeyPair(3072);
        ConfigurationProvider configurationProvider = ConfigurationFactory.getConfigurationProvider();
        Configuration configuration = configurationProvider.load();
        RSAPrivateKey privKey = (RSAPrivateKey) keyPair.getPrivate();
// keyPair, "CN=" + endorsementIssuer
        Properties properties = new Properties();

        String token = new AASTokenFetcher().getAASToken(configuration.get(MC_FIRST_USERNAME), configuration.get(MC_FIRST_PASSWORD),
            new TlsConnection(new URL(configuration.get(AAS_API_URL)), new InsecureTlsPolicy()));
        properties.setProperty("bearer.token", token);

        CMSClient cmsClient = new CMSClient(properties, new TlsConnection(new URL(configuration.get("cms.base.url")), new InsecureTlsPolicy()));

        X509Certificate cacert = cmsClient.getCertificate(CertificateUtils.getCSR(keyPair, getConfiguration().get(SAML_CERTIFICATE_DN)).toString(), "Signing");
        log.info(getConfiguration().get(SAML_CERTIFICATE_DN));
        log.info(cacert.toString());
        log.info(getConfiguration().get(SAML_KEYSTORE_FILE));
        FileOutputStream newp12 = new FileOutputStream("/opt/mtwilson/configuration/" +getConfiguration().get(SAML_KEYSTORE_FILE));

        try {
            KeyStore keystore = KeyStore.getInstance("PKCS12");
            keystore.load(null, getConfiguration().get(SAML_KEYSTORE_PASSWORD).toCharArray());
            Certificate[] chain = {cacert};
            keystore.setKeyEntry("1", privKey, getConfiguration().get(SAML_KEYSTORE_PASSWORD).toCharArray(), chain);
            keystore.store(newp12, getConfiguration().get(SAML_KEYSTORE_PASSWORD).toCharArray());

            FileOutputStream out = new FileOutputStream(SAML_CERTIFICATE_FILE);
            IOUtils.write(X509Util.encodePemCertificate(cacert), out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            newp12.close();
        }

    }

}