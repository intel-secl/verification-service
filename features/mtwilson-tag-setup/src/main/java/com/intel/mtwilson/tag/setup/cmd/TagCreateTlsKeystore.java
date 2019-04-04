/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.tag.setup.cmd;

import com.intel.mtwilson.tag.setup.TagCommand;
import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.dcsg.cpg.validation.Fault;
import com.intel.dcsg.cpg.x509.X509Builder;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Properties;
import org.apache.commons.configuration.MapConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This command exports a file from the database to the filesystem
 * 
 * Usage: create-tls-keystore "CN=mykey,O=myorg,C=US" keystore.jks
 * 
 * Use double-quotes; on Windows especially do not use single quotes around the argument because it will be a part of it
 * 
 * If a distinguished name is not provided, a default name will be used
 * 
 * @deprecated now using mtwilson ssl keystore - ASSET TAG SERVICE NO LONGER HAS A SEPARATE SSL KEYSTORE
 * @author jbuhacoff
 */
public class TagCreateTlsKeystore extends TagCommand {
    private static Logger log = LoggerFactory.getLogger(TagCreateTlsKeystore.class);
    
    @Override
    public void execute(String[] args) throws Exception {
        // file name, and either outfile or stdout
        String ipAlternativeName, dnsAlternativeName;
        String dn;
        if( args.length > 0 ) { 
            dn = args[0];
        } 
        else {
            dn = "CN=asset-tag-service,OU=mtwilson";
        }
        /***** UNUSED
        if( args.length > 1 ) {
            filename = args[1];
        }
        else {
            filename = "keystore.jks";
        }*/
        
        ipAlternativeName = getOptions().getString("ip");
        dnsAlternativeName = getOptions().getString("dns");
        
        // create a new key pair
        KeyPair cakey = RsaUtil.generateRsaKeyPair(2048);
        X509Builder builder = X509Builder.factory();
        builder.selfSigned(dn, cakey);
        if( dnsAlternativeName != null ) {
            builder.dnsAlternativeName(dnsAlternativeName);
        }
        if( ipAlternativeName != null ) {
            builder.ipAlternativeName(ipAlternativeName);            
        }
        X509Certificate cacert = builder.build();
        if( cacert == null ) {
            List<Fault> faults = builder.getFaults();
            for(Fault fault : faults) {
                log.error(String.format("%s: %s", fault.getClass().getName(), fault.toString()));
            }
            return;
            
        }
        
    }
    
    /**
     * Example certificate generated by this command line:
     * create-tls-keystore --ip=127.0.0.1 --dns=localhost "CN=Asset CA,OU=Datacenter,C=US" target/keystore.jks
     * 
Owner: CN=Asset CA, OU=Datacenter, C=US
Issuer: CN=Asset CA, OU=Datacenter, C=US
Serial number: 19ba7e7bc07ec54b
Valid from: Fri Nov 15 09:46:55 PST 2013 until: Sat Nov 15 09:46:55 PST 2014
Certificate fingerprints:
         MD5:  1D:AF:C1:7B:CE:02:9C:A5:EF:0A:FA:91:51:4A:BD:E1
         SHA1: 49:4B:AF:29:DF:D2:CC:EA:87:0E:0B:92:32:7F:93:F2:CE:84:DB:A1
         SHA256: D9:3C:42:EC:44:5F:D8:07:52:E5:E8:53:C0:15:0B:FF:5B:4C:CC:75:31:5E:B4:2E:9B:7F:07:BF:58:1E:A5:62
         Signature algorithm name: SHA256withRSA
         Version: 3

Extensions:

#1: ObjectId: 2.5.29.17 Criticality=false
SubjectAlternativeName [
  DNSName: localhost
  IPAddress: 127.0.0.1
]

     * 
     */
    
    public static void main(String args[]) throws Exception {
        TagCreateTlsKeystore cmd = new TagCreateTlsKeystore();
        Properties options = new Properties();
        options.setProperty("ip", "127.0.0.1"); // corresponds to  --ip=127.0.0.1   on command line
        options.setProperty("dns", "localhost"); // corresponds to  --dns=localhost   on command line
        cmd.setOptions(new MapConfiguration(options));
        cmd.execute(new String[] { "CN=Asset CA,OU=Datacenter,C=US", "target/keystore.jks" });
    }    
    
}
