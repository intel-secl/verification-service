/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.setup.console.cmd;

import com.intel.dcsg.cpg.console.InteractiveCommand;
import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.mtwilson.Folders;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author rksavino
 */
public class ReplaceRootKeyPair extends InteractiveCommand {
    private PrivateKey privateKey;
    private List<X509Certificate> x509CertChainList;
    
    private final String USAGE = "Usage: replace-root-key-pair <--private-key=private-key-file> <--cert-chain=cert-chain-file>";
    
    @Override
    public void execute(String[] args) throws Exception {
        validateInput();
        X509Certificate publicKeyCert = x509CertChainList.get(0);
        
        // update root cakey file
        String rootKeyString = RsaUtil.encodePemPrivateKey(privateKey);
        String publicKeyCertString = X509Util.encodePemCertificate(publicKeyCert);
        File rootKeyFile = new File(Folders.configuration() + File.separator + "cakey.pem");
        try(FileOutputStream out = new FileOutputStream(rootKeyFile)) {
            IOUtils.write(rootKeyString.concat(publicKeyCertString).getBytes("UTF-8"), out);
        }
        
        // update root cacerts file
        File rootCacertsFile = new File(Folders.configuration() + File.separator + "cacerts.pem");
        try(FileOutputStream out = new FileOutputStream(rootCacertsFile)) {
            IOUtils.write(publicKeyCertString.getBytes("UTF-8"), out);
        }
        
        // update root cert chain file
        String certChainString = "";
        for (X509Certificate x509Cert : x509CertChainList) {
            certChainString = certChainString.concat(X509Util.encodePemCertificate(x509Cert));
        }
        File rootCertChainFile = new File(Folders.configuration() + File.separator + "MtWilsonRootCA.crt.pem");
        try(FileOutputStream out = new FileOutputStream(rootCertChainFile)) {
            IOUtils.write(certChainString.getBytes("UTF-8"), out);
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
    }
}