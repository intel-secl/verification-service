/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.saml;

import com.intel.dcsg.cpg.configuration.CommonsConfiguration;
import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.mtwilson.My;
import com.intel.mtwilson.supplemental.saml.IssuerConfiguration;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.security.KeyStore;
import java.security.KeyStore.ProtectionParameter;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

/**
 *
 * @author rksavino
 */
public class IssuerConfigurationFactory {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(IssuerConfigurationFactory.class);

    public IssuerConfiguration loadIssuerConfiguration() {
        try {
            log.debug("loadIssuerConfiguration");
            org.apache.commons.configuration.Configuration apacheConfiguration = My.configuration().getConfiguration();
            Configuration configuration = new CommonsConfiguration(apacheConfiguration);
            String issuerName = configuration.get(SamlConfiguration.SAML_ISSUER, "AttestationService");
            if (issuerName == null) {
                issuerName = configuration.get("mtwilson.api.url");
                if (issuerName == null) {
                    InetAddress localhost = InetAddress.getLocalHost();
                    issuerName = "https://" + localhost.getHostAddress() + ":8443/mtwilson";   // was; 8181/AttestationService       
                    configuration.set(SamlConfiguration.SAML_ISSUER, issuerName);
                } else {
                    configuration.set(SamlConfiguration.SAML_ISSUER, issuerName);
                }
            }
            log.debug("loadIssuerConfiguration creating IssuerConfiguration");
            
            SamlConfiguration saml = new SamlConfiguration(configuration);
            log.debug("SAML keystore file: {}", saml.getSamlKeystoreFile());
            File keystoreFile = saml.getSamlKeystoreFile(); // replaces My.configuration().getSamlKeystoreFile();
            
            KeyStore keyStore;
            try (FileInputStream keystoreInputStream = new FileInputStream(keystoreFile)) {
                keyStore = getKeyStore(keystoreInputStream, saml.getSamlKeystorePassword()); /*configuration.getString("saml.keystore.password"*//*,System.getenv("SAMLPASSWORD")*/
            }
            
            // set saml key password if exists in configuration, or set it to null otherwise
            char[] samlKeyPassword = null;
            if (saml.getSamlKeyPassword() != null && !saml.getSamlKeyPassword().isEmpty()
                    && saml.getSamlKeyPassword().toCharArray() != null && saml.getSamlKeyPassword().toCharArray().length > 0) {
                samlKeyPassword = saml.getSamlKeyPassword().toCharArray();
            }
            ProtectionParameter samlKeyProtectionParam = null;
            if (samlKeyPassword != null && samlKeyPassword.length > 0) {
                samlKeyProtectionParam = new KeyStore.PasswordProtection(samlKeyPassword);
            }
            
            KeyStore.PrivateKeyEntry entry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(saml.getSamlKeyAlias(), samlKeyProtectionParam);
            PrivateKey privateKey = entry.getPrivateKey();
            Certificate certificate = entry.getCertificate();
            String issuerServiceName = "Intel Security Libraries";
            String jsr105provider = saml.getJsr105Provider(); // conf.getString(JSR105_PROVIDER, "org.jcp.xml.dsig.internal.dom.XMLDSigRI");
            Integer validitySeconds = saml.getSamlValiditySeconds();
            return new IssuerConfiguration(privateKey, certificate, configuration, issuerName, issuerServiceName, validitySeconds, jsr105provider);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot load SAML issuer configuration", e);
        }
    }
    
    /**
     * Get a KeyStore object given the keystore filename and password.
     */
    private KeyStore getKeyStore(InputStream in, String password)
            throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        KeyStore result = KeyStore.getInstance(KeyStore.getDefaultType());
        result.load(in, password.toCharArray());
        return result;
    }
}
