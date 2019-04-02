/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.tag.setup.cmd;

import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.dcsg.cpg.console.InteractiveCommand;
import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.mtwilson.My;
import com.intel.mtwilson.configuration.ConfigurationFactory;
import com.intel.mtwilson.configuration.ConfigurationProvider;
import com.intel.mtwilson.tag.dao.TagJdbi;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author rksavino
 */
public class ReplaceTagKeyPair extends InteractiveCommand {
    // constants
    public static final String TAG_PRIVATE_KEY_FILE = "cakey";
    public static final String TAG_CACERTS_FILE = "cacerts";
    
    // configuration keys
    private static final String TAG_ISSUER_DN = "tag.issuer.dn";
    
    private PrivateKey privateKey;
    private List<X509Certificate> x509CertChainList;
    private String issuer;
    
    private final String USAGE = "Usage: replace-tag-key-pair <--private-key=private-key-file> <--cert-chain=cert-chain-file>";
    
    @Override
    public void execute(String[] args) throws Exception {
        ConfigurationProvider provider = ConfigurationFactory.getConfigurationProvider();
        Configuration configuration = provider.load();
        validateInput();
        
        X509Certificate publicKeyCert = x509CertChainList.get(0);
        
        // update the database with the tag private key with cert appended
        String caKeyCertContent = X509Util.encodePemCertificate(publicKeyCert);
        String cakeyContent = RsaUtil.encodePemPrivateKey(privateKey).concat(caKeyCertContent);
        com.intel.mtwilson.tag.model.File file = TagJdbi.fileDao().findByName(TAG_PRIVATE_KEY_FILE);
        if (file == null) {
            log.warn("Tag key pair file does not currently exist in the database [{}]", TAG_PRIVATE_KEY_FILE);
            TagJdbi.fileDao().insert(new UUID(), TAG_PRIVATE_KEY_FILE, getOptions()
                    .getString("type", "text/plain"), cakeyContent.getBytes("UTF-8"));
        } else {
            TagJdbi.fileDao().update(file.getId(), TAG_PRIVATE_KEY_FILE, getOptions()
                    .getString("type",  file.getContentType()), cakeyContent.getBytes("UTF-8"));
        }
        
        // update the database with the tag ca cert
        com.intel.mtwilson.tag.model.File cacertsFile = TagJdbi.fileDao().findByName(TAG_CACERTS_FILE);
        if (cacertsFile == null) {
            log.warn("Tag CA certificates file does not currently exist in the database [{}]", TAG_CACERTS_FILE);
            TagJdbi.fileDao().insert(new UUID(), TAG_CACERTS_FILE, "text/plain", caKeyCertContent.getBytes("UTF-8"));
        } else {
            TagJdbi.fileDao().update(cacertsFile.getId(), TAG_CACERTS_FILE, "text/plain", caKeyCertContent.getBytes("UTF-8"));
        }
        // write to disk also for easy sharing with mtwilson: tag-cacerts.pem
        try(FileOutputStream out = new FileOutputStream(My.configuration().getAssetTagCaCertificateFile())) {
            IOUtils.write(caKeyCertContent.getBytes("UTF-8"), out);
        }
        
        // save the settings in configuration
        configuration.set(TAG_ISSUER_DN, issuer);
        provider.save(configuration);
        
        // TODO: deploy tags to all hosts
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