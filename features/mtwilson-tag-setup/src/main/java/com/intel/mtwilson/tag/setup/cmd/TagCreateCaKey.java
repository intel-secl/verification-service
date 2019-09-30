/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.tag.setup.cmd;

import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.dcsg.cpg.tls.policy.TlsConnection;
import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import com.intel.dcsg.cpg.tls.policy.TlsPolicyBuilder;
import com.intel.dcsg.cpg.tls.policy.impl.InsecureTlsPolicy;
import com.intel.mtwilson.configuration.ConfigurationFactory;
import com.intel.mtwilson.configuration.ConfigurationProvider;
import com.intel.mtwilson.core.common.model.CertificateType;
import com.intel.mtwilson.jaxrs2.client.CMSClient;
import com.intel.mtwilson.tag.setup.TagCommand;
import com.intel.mtwilson.tag.model.File;
import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.dcsg.cpg.io.ByteArray;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.core.common.utils.AASTokenFetcher;
import com.intel.dcsg.cpg.x509.X509Builder;
import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.mtwilson.My;
import com.intel.mtwilson.tag.dao.TagJdbi;
import java.io.FileOutputStream;
import java.net.URL;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Properties;
import com.intel.mtwilson.setup.utils.CertificateUtils;
import java.util.concurrent.TimeUnit;
import org.apache.commons.configuration.MapConfiguration;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This command exports a file from the database to the filesystem
 * 
 * Usage: create-ca-key "CN=mykey,O=myorg,C=US"
 * 
 * Use double-quotes; on Windows especially do not use single quotes around the argument because it will be a part of it
 * 
 * If a distinguished name is not provided, a default name will be used
 * 
 * @author jbuhacoff
 */
public class TagCreateCaKey extends TagCommand {
    private static Logger log = LoggerFactory.getLogger(TagCreateCaKey.class);
    public static final String PRIVATEKEY_FILE = "cakey";
    public static final String CACERTS_FILE = "cacerts";
    private static final String CMS_BASE_URL = "cms.base.url";
    private static final String AAS_API_URL = "aas.api.url";
    private static final String MC_FIRST_USERNAME = "mc.first.username";
    private static final String MC_FIRST_PASSWORD = "mc.first.password";

    @Override
    public void execute(String[] args) throws Exception {
        String dn;
        if( args.length > 0 ) { 
            dn = args[0];
        } 
        else {
            dn = "CN=asset-tag-service";
        }
        
        // create a new key pair
        KeyPair cakey = RsaUtil.generateRsaKeyPair(3072);
        ConfigurationProvider configurationProvider = ConfigurationFactory.getConfigurationProvider();
        Configuration configuration = configurationProvider.load();
        TlsPolicy tlsPolicy = TlsPolicyBuilder.factory().strictWithKeystore(My.configuration().getTruststoreFile(),
            "changeit").build();

        Properties properties = new Properties();

        String token = new AASTokenFetcher().getAASToken(configuration.get(MC_FIRST_USERNAME),configuration.get(MC_FIRST_PASSWORD),
            new TlsConnection(new URL(configuration.get(AAS_API_URL)), tlsPolicy));
        properties.setProperty("bearer.token", token);

        CMSClient cmsClient = new CMSClient(properties, new TlsConnection(new URL(configuration.get(CMS_BASE_URL)), tlsPolicy));

        X509Certificate tagcert = cmsClient.getCertificate(CertificateUtils.getCSR(cakey, dn).toString(), CertificateType.SIGNING.getValue());
        String privateKeyPem = RsaUtil.encodePemPrivateKey(cakey.getPrivate());
        String tagCertPem = X509Util.encodePemCertificate(tagcert);
        
        String combinedPrivateKeyAndCertPem = privateKeyPem + tagCertPem;
        
        byte[] combinedPrivateKeyAndCertPemBytes = combinedPrivateKeyAndCertPem.getBytes("UTF-8");
        byte[] cacertPemContent = tagCertPem.getBytes("UTF-8");
        
        // for now... there can only be ONE CA private key in the database  (but we support storing multiple certs)
        File cakeyFile = TagJdbi.fileDao().findByName(PRIVATEKEY_FILE);
        if( cakeyFile == null ) {
            // create new private key file
            TagJdbi.fileDao().insert(new UUID(), PRIVATEKEY_FILE, "text/plain", combinedPrivateKeyAndCertPemBytes);
        }
        else {
            // replace existing private key... 
            TagJdbi.fileDao().update(cakeyFile.getId(), PRIVATEKEY_FILE, "text/plain", combinedPrivateKeyAndCertPemBytes);
        }
        
        // add the ca cert to the list of approved certs
        File cacertsFile = TagJdbi.fileDao().findByName(CACERTS_FILE);
        if( cacertsFile == null ) {
            // create new cacerts file
            TagJdbi.fileDao().insert(new UUID(), CACERTS_FILE, "text/plain", cacertPemContent);
        }
        else {
            // append new tagcert to existing file in database
            byte[] content = ByteArray.concat(cacertsFile.getContent(), cacertPemContent);
            TagJdbi.fileDao().update(cacertsFile.getId(), CACERTS_FILE, "text/plain", content);
            // and write to disk also for easy sharing with mtwilson: tag-cacerts.pem
            try(FileOutputStream out = new FileOutputStream(My.configuration().getAssetTagCaCertificateFile())) {
                IOUtils.write(content, out);
            }
        }
        
    }
    
    
    public static void main(String args[]) throws Exception {
        TagCreateCaKey cmd = new TagCreateCaKey();
        cmd.setOptions(new MapConfiguration(new Properties()));
        cmd.execute(new String[] { "CN=asset-tag-service" });
        
    }    
    
}
