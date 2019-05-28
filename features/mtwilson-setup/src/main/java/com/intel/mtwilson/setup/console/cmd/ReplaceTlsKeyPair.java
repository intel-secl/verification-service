/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.setup.console.cmd;

import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.dcsg.cpg.console.InteractiveCommand;
import com.intel.dcsg.cpg.crypto.Md5Digest;
import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.dcsg.cpg.crypto.Sha256Digest;
import com.intel.dcsg.cpg.crypto.Sha384Digest;
import com.intel.dcsg.cpg.crypto.key.password.Password;
import com.intel.dcsg.cpg.io.FileResource;
import com.intel.dcsg.cpg.iso8601.Iso8601Date;
import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.mtwilson.Folders;
import com.intel.mtwilson.configuration.ConfigurationFactory;
import com.intel.mtwilson.configuration.ConfigurationProvider;
import com.intel.mtwilson.core.PasswordVaultFactory;
import com.intel.mtwilson.util.crypto.keystore.PasswordKeyStore;
import com.intel.mtwilson.util.crypto.keystore.PrivateKeyStore;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.security.KeyStoreException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.InetAddressValidator;

/**
 *
 * @author rksavino
 */
public class ReplaceTlsKeyPair extends InteractiveCommand {
    // constants
    private static final String TLS_ALIAS = "jetty";
    
    // configuration keys
    private static final String JETTY_TLS_CERT_DN = "jetty.tls.cert.dn";
    private static final String JETTY_TLS_CERT_IP = "jetty.tls.cert.ip";
    private static final String JETTY_TLS_CERT_DNS = "jetty.tls.cert.dns";
    private static final String JAVAX_NET_SSL_KEYSTORE = "javax.net.ssl.keyStore";
    private static final String JAVAX_NET_SSL_KEYSTOREPASSWORD = "javax.net.ssl.keyStorePassword";
    
    private PrivateKey privateKey;
    private List<X509Certificate> x509CertChainList;
    private String dn, ip, dns;
    
    private final String USAGE = "Usage: replace-tls-key-pair <--private-key=private-key-file> <--cert-chain=cert-chain-file>";
    
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
        
        String keystorePath = configuration.get(JAVAX_NET_SSL_KEYSTORE, null);
        if (keystorePath == null) {
            keystorePath = Folders.configuration() + File.separator + "keystore.p12";
        }
        File keystoreFile = new File(keystorePath);
        
        // to avoid putting any passwords in the configuration file, we
        // get the password from the password vault
        Password keystorePassword = null;
        try (PasswordKeyStore passwordVault = PasswordVaultFactory.getPasswordKeyStore(configuration)) {
            if (passwordVault.contains(JAVAX_NET_SSL_KEYSTOREPASSWORD)) {
                keystorePassword = passwordVault.get(JAVAX_NET_SSL_KEYSTOREPASSWORD);
            }
        }
        
        if (!keystoreFile.exists()) {
            throw new FileNotFoundException("\nKeystore file does not exist");
        }
        if (keystorePassword == null || keystorePassword.toCharArray().length == 0) {
            throw new IllegalArgumentException("\nKeystore password is not set");
        }
        
        try (PrivateKeyStore keystore = new PrivateKeyStore(KeyStore.getDefaultType(), new FileResource(keystoreFile), keystorePassword)) {
            // remove existing keypair from keystore
            if (keystore.contains(TLS_ALIAS)) {
                try {
                    keystore.remove(TLS_ALIAS);
                } catch (KeyStoreException e) {
                    throw new KeyStoreException("\nCannot remove existing keypair", e);
                }
            } else {
                log.warn("Keystore does not currently contain the specified key pair [{}]", TLS_ALIAS);
            }
            
            // store it in the keystore
            keystore.set(TLS_ALIAS, privateKey, certChainArray);
        }
        
        /**
         * NOTE: this is NOT the encrypted configuration file, it's a plaintext
         * Java Properties file to store the TLS certificate fingerprints so
         * the administrator can verify a TLS connection to the KMS when using
         * self-signed certificates
         */
        File propertiesFile = new File(Folders.configuration() + File.separator + "https.properties");
        
        /**
         * Log the same information to a plain text file so admin can easily
         * copy it as necessary for use in a TLS policy or to verify the 
         * server's self-signed TLS certificate in the browser.
         * 
         * NOTE: this is NOT the encrypted configuration file, it's a plaintext
         * Java Properties file to store the TLS certificate fingerprints so
         * the administrator can verify a TLS connection to the KMS when using
         * self-signed certificates
         */
        Properties properties = new Properties();
        if (propertiesFile.exists()) {
            properties.load(new StringReader(FileUtils.readFileToString(propertiesFile, Charset.forName("UTF-8"))));
        }
        properties.setProperty("tls.cert.md5", Md5Digest.digestOf(publicKeyCert.getEncoded()).toString());
        properties.setProperty("tls.cert.sha1", Sha1Digest.digestOf(publicKeyCert.getEncoded()).toString());
        properties.setProperty("tls.cert.sha256", Sha256Digest.digestOf(publicKeyCert.getEncoded()).toString());
        properties.setProperty("tls.cert.sha384", Sha384Digest.digestOf(publicKeyCert.getEncoded()).toString());
        StringWriter writer = new StringWriter();
        properties.store(writer, String.format("updated on %s", Iso8601Date.format(new Date())));
        FileUtils.write(propertiesFile, writer.toString(), Charset.forName("UTF-8"));
        log.debug("Wrote https.properties: {}", writer.toString().replaceAll("[\\r\\n]", "|"));
        
        // save the settings in configuration
        configuration.set(JETTY_TLS_CERT_DN, dn);
        if (ip != null && !ip.isEmpty()) {
            configuration.set(JETTY_TLS_CERT_IP, ip);
        }
        if (dns != null && !dns.isEmpty()) {
            configuration.set(JETTY_TLS_CERT_DNS, dns);
        }
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
        
        x509CertChainList = X509Util.decodePemCertificates(FileUtils.readFileToString(certChainFile, Charset.forName("UTF-8")));
        if (x509CertChainList == null || x509CertChainList.isEmpty()) {
            throw new IllegalArgumentException("\nCertificate chain file specified is not in PEM format");
        }
        // Check if last cert is self signed?
        // Check if first cert is compatible with private key?
        
        X509Certificate publicKeyCert = x509CertChainList.get(0);
        if (publicKeyCert == null) {
            throw new IllegalArgumentException("\nCannot establish certificate from file specified");
        }
        Principal publicKeyCertPrincipal = publicKeyCert.getSubjectDN();
        if (publicKeyCertPrincipal == null || publicKeyCertPrincipal.getName() == null) {
            throw new IllegalArgumentException("\nSubject distinguished name must be specified within certificate");
        }
        dn = publicKeyCertPrincipal.getName();
        if (dn.isEmpty()) {
            throw new IllegalArgumentException("\nSubject distinguished name must be specified within certificate");
        }
        
        List<String> ipList = new ArrayList();
        List<String> dnsList = new ArrayList();
        
        Set<String> altNames = X509Util.alternativeNames(publicKeyCert);
        if (altNames == null || altNames.isEmpty()) {
            throw new IllegalArgumentException("\nSubject alternative names must be specified within certificate");
        }
        for (String altName : altNames) {
            if (InetAddressValidator.getInstance().isValid(altName)) {
                ipList.add(altName);
            } else {
                dnsList.add(altName);
            }
        }
        
        ip = null;
        if (!ipList.isEmpty()) {
            ip = StringUtils.join(ipList, ",");
        }
        dns = null;
        if (!dnsList.isEmpty()) {
            dns = StringUtils.join(dnsList, ",");
        }
    }
}