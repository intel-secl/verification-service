/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.setup.console.cmd;

import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.dcsg.cpg.console.InteractiveCommand;
import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.dcsg.cpg.crypto.key.password.Password;
import com.intel.dcsg.cpg.io.FileResource;
import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.mtwilson.Folders;
import com.intel.mtwilson.configuration.ConfigurationFactory;
import com.intel.mtwilson.configuration.ConfigurationProvider;
import com.intel.mtwilson.flavor.rest.v2.model.Host;
import com.intel.mtwilson.flavor.rest.v2.model.HostCollection;
import com.intel.mtwilson.flavor.rest.v2.model.HostFilterCriteria;
import com.intel.mtwilson.flavor.rest.v2.repository.HostRepository;
import com.intel.mtwilson.flavor.rest.v2.resource.HostResource;
import com.intel.mtwilson.util.crypto.keystore.PrivateKeyStore;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.nio.file.Paths;
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
public class ReplaceSamlKeyPair extends InteractiveCommand {
    // configuration keys
    private static final String SAML_KEYSTORE_FILE = "saml.keystore.file";
    private static final String SAML_KEYSTORE_PASSWORD = "saml.keystore.password";
    private static final String SAML_KEY_ALIAS = "saml.key.alias";
    private static final String SAML_CERTIFICATE_DN = "saml.certificate.dn";
    private static final String SAML_ISSUER = "saml.issuer";
    
    private PrivateKey privateKey;
    private List<X509Certificate> x509CertChainList;
    private String dn, issuer;
    
    private final String USAGE = "Usage: replace-saml-key-pair <--private-key=private-key-file> <--cert-chain=cert-chain-file>";
    
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
        
        String keystorePath = configuration.get(SAML_KEYSTORE_FILE, null);
        if (keystorePath == null) {
            keystorePath = Folders.configuration() + File.separator + "SAML.jks";
        } else if (!Paths.get(keystorePath).isAbsolute()) {
            keystorePath = Folders.configuration() + File.separator + keystorePath;
        }
        File keystoreFile = new File(keystorePath);
        if (!keystoreFile.exists()) {
            throw new FileNotFoundException("\nKeystore file does not exist");
        }
        GuardedPassword guardedPassword = new GuardedPassword();
        guardedPassword.setPassword(configuration.get(SAML_KEYSTORE_PASSWORD, null));
        if (!guardedPassword.isPasswordValid()) {
            throw new IllegalArgumentException("\nKeystore password is not set");
        }
        Password keystorePassword = new Password(guardedPassword.getInsPassword());
        guardedPassword.dispose();

        String samlKeyAlias = configuration.get(SAML_KEY_ALIAS, null);
        if (samlKeyAlias == null) {
            samlKeyAlias = "samlkey1";
        }
        try (PrivateKeyStore keystore = new PrivateKeyStore("JKS", new FileResource(keystoreFile), keystorePassword)) {
            // remove existing keypair from keystore
            if (keystore.contains(samlKeyAlias)) {
                try {
                    keystore.remove(samlKeyAlias);
                } catch (KeyStoreException e) {
                    throw new KeyStoreException("\nCannot remove existing keypair", e);
                }
            } else {
                log.warn("Keystore does not currently contain the specified key pair [{}]", samlKeyAlias);
            }
            
            // store it in the keystore
            keystore.set(samlKeyAlias, privateKey, certChainArray);
        }
        
        // write public key cert to saml.crt
        File certificateChainDerFile = new File(Folders.configuration() + File.separator + "saml.crt");
        try (FileOutputStream out = new FileOutputStream(certificateChainDerFile)) {
            IOUtils.write(publicKeyCert.getEncoded(), out);
        }
        
        // write pem formatted public key cert to saml.crt.pem
        String publicKeyCertString = X509Util.encodePemCertificate(publicKeyCert);
        File certificateChainPemFile = new File(Folders.configuration() + File.separator + "saml.crt.pem");
        try (FileOutputStream out = new FileOutputStream(certificateChainPemFile)) {
            IOUtils.write(publicKeyCertString.getBytes("UTF-8"), out);
        }
        
        // save the settings in configuration
        configuration.set(SAML_CERTIFICATE_DN, dn);
        configuration.set(SAML_ISSUER, issuer);
        provider.save(configuration);
        
        // get list of current hosts
        HostFilterCriteria criteria = new HostFilterCriteria();
        criteria.filter = false;
        HostCollection hostCollection = new HostRepository().search(criteria);
        
        // return if no hosts exist
        if (hostCollection == null || hostCollection.getHosts() == null
                || hostCollection.getHosts().isEmpty()) {
            return;
        }
        
        // add all hosts to the flavor-verify queue
        HostResource hosts = new HostResource();
        for (Host host : hostCollection.getHosts()) {
            hosts.addHostToFlavorVerifyQueue(host.getId(), true);
        }
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
        
        // get subject distinguished name (DN)
        Principal publicKeyCertSubject = publicKeyCert.getSubjectDN();
        if (publicKeyCertSubject == null || publicKeyCertSubject.getName() == null) {
            throw new IllegalArgumentException("\nSubject distinguished name must be specified within certificate");
        }
        dn = publicKeyCertSubject.getName();
        if (dn.isEmpty()) {
            throw new IllegalArgumentException("\nSubject distinguished name must be specified within certificate");
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
