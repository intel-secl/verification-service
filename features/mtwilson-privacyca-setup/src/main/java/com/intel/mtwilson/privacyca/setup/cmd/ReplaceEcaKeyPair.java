/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.privacyca.setup.cmd;

import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.dcsg.cpg.console.InteractiveCommand;
import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.dcsg.cpg.crypto.key.password.Password;
import com.intel.dcsg.cpg.io.FileResource;
import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.mtwilson.Folders;
import com.intel.mtwilson.configuration.ConfigurationFactory;
import com.intel.mtwilson.configuration.ConfigurationProvider;
import com.intel.mtwilson.util.crypto.keystore.PrivateKeyStore;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.security.KeyStoreException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import com.intel.mtwilson.crypto.password.GuardedPassword;

/**
 *
 * @author rksavino
 */
public class ReplaceEcaKeyPair extends InteractiveCommand {
    // constants
    private static final String PRIVACYCA_EK_ALIAS = "1";
    
    // configuration keys
    private static final String PRIVACYCA_EK_CACERTS_FILE = "mtwilson.privacyca.ek.cacerts.file";
    private static final String PRIVACYCA_EK_EXTERNAL_CACERTS_FILE = "mtwilson.privacyca.ek.external.cacerts.file";
    private static final String PRIVACYCA_EK_ISSUER = "mtwilson.privacyca.ek.issuer";
    private static final String PRIVACYCA_EK_P12_FILE = "mtwilson.privacyca.ek.p12.file";
    private static final String PRIVACYCA_EK_P12_PASSWORD = "mtwilson.privacyca.ek.p12.password";
    
    private PrivateKey privateKey;
    private List<X509Certificate> x509CertChainList;
    private String issuer;
    
    private final String USAGE = "Usage: replace-eca-key-pair <--private-key=private-key-file> <--cert-chain=cert-chain-file>";
    
    @Override
    public void execute(String[] args) throws Exception {
        ConfigurationProvider provider = ConfigurationFactory.getConfigurationProvider();
        Configuration configuration = provider.load();
        validateInput();
        
        List<Certificate> certChainList = new ArrayList();
        for (X509Certificate x509Cert : x509CertChainList) {
            certChainList.add((Certificate)x509Cert);
        }
        Certificate[] certChainArray = certChainList.toArray(new Certificate[0]);
        X509Certificate publicKeyCert = x509CertChainList.get(0);
        
        String keystorePath = configuration.get(PRIVACYCA_EK_P12_FILE, null);
        if( keystorePath == null ) {
            keystorePath = Folders.configuration() + File.separator + "EndorsementCA.p12";
        }
        File keystoreFile = new File(keystorePath);
        if (!keystoreFile.exists() ) {
            throw new FileNotFoundException("\nKeystore file does not exist");
        }
        GuardedPassword guardedPassword = new GuardedPassword();
        guardedPassword.setPassword(configuration.get(PRIVACYCA_EK_P12_PASSWORD, null));
        if (!guardedPassword.isPasswordValid()) {
            throw new IllegalArgumentException("\nKeystore password is not set");
        }
        Password keystorePassword = new Password(guardedPassword.getInsPassword());
        guardedPassword.dispose();
        
        try (PrivateKeyStore keystore = new PrivateKeyStore("PKCS12", new FileResource(keystoreFile), keystorePassword)) {
            // remove existing keypair from keystore
            if (keystore.contains(PRIVACYCA_EK_ALIAS)) {
                try {
                    keystore.remove(PRIVACYCA_EK_ALIAS);
                } catch (KeyStoreException e) {
                    throw new KeyStoreException("\nCannot remove existing keypair", e);
                }
            } else {
                log.warn("Keystore does not currently contain the specified key pair [{}]", PRIVACYCA_EK_ALIAS);
            }
            
            // store it in the keystore
            keystore.set(PRIVACYCA_EK_ALIAS, privateKey, certChainArray);
        }
        
        String ekCacerts = "";
        
        // read in additional external maufacturer ECs
        String ekExternalCacertsPath = configuration.get(PRIVACYCA_EK_EXTERNAL_CACERTS_FILE, null);
        if( ekExternalCacertsPath == null ) {
            ekExternalCacertsPath = Folders.configuration() + File.separator + "EndorsementCA-external.pem";
        }
        File ekExternalCacertsFile = new File(ekExternalCacertsPath);
        String ekExternalCacertsFileContent = FileUtils.readFileToString(ekExternalCacertsFile, Charset.forName("UTF-8"));
        List<X509Certificate> ekExternalCacerts = X509Util.decodePemCertificates(ekExternalCacertsFileContent);
        if (ekExternalCacerts != null && !ekExternalCacerts.isEmpty()) {
            for (X509Certificate ekExternalCacert : ekExternalCacerts) {
                String ekExternalCacertString = X509Util.encodePemCertificate(ekExternalCacert);
                ekCacerts = ekCacerts.concat(String.format("%s\n", ekExternalCacertString));
            }
        } else {
            log.warn("External manufacturer endorsement certificates file not present or readable");
        }
        String publicKeyCertString = X509Util.encodePemCertificate(publicKeyCert);
        ekCacerts = ekCacerts.concat(String.format("%s\n", publicKeyCertString));
        
        // update EK cacerts file on disk
        String ekCacertsPath = configuration.get(PRIVACYCA_EK_CACERTS_FILE, null);
        if( ekCacertsPath == null ) {
            ekCacertsPath = Folders.configuration() + File.separator + "EndorsementCA.pem";
        }
        File ekCacertsFile = new File(ekCacertsPath);
        try(FileOutputStream out = new FileOutputStream(ekCacertsFile)) {
            IOUtils.write(ekCacerts.getBytes("UTF-8"), out);
        }
        
        // save the settings in configuration
        configuration.set(PRIVACYCA_EK_ISSUER, issuer);
        provider.save(configuration);
    }
    
    public void validateInput() throws Exception {
        if (options == null || !options.containsKey("private-key") || !options.containsKey("cert-chain")) {
            throw new IllegalArgumentException(String.format("\n%s", USAGE));
        }
        File privateKeyFile = new File(options.getString("private-key"));
        if (!privateKeyFile.exists()) {
            throw new FileNotFoundException("\nPrivate key file specified does not exist or user does not have required read permission");
        }
        File certChainFile = new File(options.getString("cert-chain"));
        if (!certChainFile.exists()) {
            throw new FileNotFoundException("\nCertificate chain file specified does not exist or user does not have required read permission");
        }
        
        String privateKeyFileContent = FileUtils.readFileToString(privateKeyFile, Charset.forName("UTF-8"));
        if (privateKeyFileContent.contains("ENCRYPTED PRIVATE KEY")) {
            throw new IllegalArgumentException("\nPassphrase protected private key not supported");
        }
        privateKey = RsaUtil.decodePemPrivateKey(privateKeyFileContent);
        if (privateKey == null) {
            throw new IllegalArgumentException("\nPrivate key file specified is not in PEM format");
        }
        
        String certChainFileContent = FileUtils.readFileToString(certChainFile, Charset.forName("UTF-8"));
        x509CertChainList = X509Util.decodePemCertificates(certChainFileContent);
        if (x509CertChainList == null || x509CertChainList.isEmpty()) {
            throw new IllegalArgumentException("\nCertificate chain file specified is not in PEM format");
        }
        // Check if last cert is self signed?
        // Check if first cert is compatible with private key?
        
        X509Certificate publicKeyCert = x509CertChainList.get(0);
        if (publicKeyCert == null) {
            throw new IllegalArgumentException("\nCannot establish certificate from file specified");
        }
        
        // get issuer distinguished name (DN)
        Principal publicKeyCertIssuer = publicKeyCert.getIssuerDN();
        if (publicKeyCertIssuer == null || publicKeyCertIssuer.getName() == null) {
            throw new IllegalArgumentException("\nIssuer distinguished name must be specified within certificate");
        }
        issuer = publicKeyCertIssuer.getName();
        if (issuer.isEmpty()) {
            throw new IllegalArgumentException("\nIssuer distinguished name must be specified within certificate");
        }
    }
}
