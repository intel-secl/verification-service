/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.tag.client.jaxrs;

import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.dcsg.cpg.crypto.RandomUtil;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.My;
import com.intel.mtwilson.core.common.tag.model.TagCertificate;
import com.intel.mtwilson.tag.model.TagCertificateCollection;
import com.intel.mtwilson.tag.model.TagCertificateCreateCriteria;
import com.intel.mtwilson.tag.model.TagCertificateFilterCriteria;
import com.intel.mtwilson.tag.model.X509AttributeCertificate;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.cert.AttributeCertificateHolder;
import org.bouncycastle.cert.AttributeCertificateIssuer;
import org.bouncycastle.cert.X509AttributeCertificateHolder;
import org.bouncycastle.cert.X509v2AttributeCertificateBuilder;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.bc.BcRSAContentSignerBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author ssbangal
 */
public class TagCertificateTest {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TagCertificateTest.class);

    private static TagCertificates client = null;
    
    @BeforeClass
    public static void init() throws Exception {
        client = new TagCertificates(My.configuration().getClientProperties());
    }
    
    @Test
    public void tagCertificateTest() throws NoSuchAlgorithmException, CertificateEncodingException, IOException, OperatorCreationException { 
        
        KeyPair keyPair = RsaUtil.generateRsaKeyPair(RsaUtil.MINIMUM_RSA_KEY_SIZE);
        AlgorithmIdentifier sigAlgId = new DefaultSignatureAlgorithmIdentifierFinder().find("SHA256withRSA");
        AlgorithmIdentifier digAlgId = new DefaultDigestAlgorithmIdentifierFinder().find(sigAlgId);
        ContentSigner authority = new BcRSAContentSignerBuilder(sigAlgId, digAlgId).build(PrivateKeyFactory.createKey(keyPair.getPrivate().getEncoded()));
        AttributeCertificateHolder holder = new AttributeCertificateHolder(new X500Name(new RDN[]{})); 
        AttributeCertificateIssuer issuer = new AttributeCertificateIssuer(new X500Name(new RDN[]{}));
        BigInteger serialNumber = new BigInteger(64, RandomUtil.getSecureRandom());
        Date notBefore = new Date();
        Date notAfter = new Date(notBefore.getTime() + TimeUnit.MILLISECONDS.convert(365, TimeUnit.DAYS));
        X509v2AttributeCertificateBuilder builder = new X509v2AttributeCertificateBuilder(holder, issuer, serialNumber, notBefore, notAfter);
        X509AttributeCertificateHolder cert = builder.build(authority);
        log.debug("cert: {}", Base64.encodeBase64String(cert.getEncoded())); // MIICGDCCAQACAQEwH6EdpBswGTEXMBUGAWkEEJKnGiKMF0UioYv9PtPQCzmgXzBdpFswWTEQMA4GA1UEAwwHQXR0ciBDQTEMMAoGA1UECwwDQ1BHMQ0wCwYDVQQLDAREQ1NHMQ4wDAYDVQQKDAVJbnRlbDELMAkGA1UECAwCQ0ExCzAJBgNVBAYTAlVTMA0GCSqGSIb3DQEBBQUAAgEBMCIYDzIwMTMwODA4MjIyMTEzWhgPMjAxMzA5MDgyMjIxMTNaMEMwEwYLKwYBBAG9hDcBAQExBAwCVVMwEwYLKwYBBAG9hDgCAgIxBAwCQ0EwFwYLKwYBBAG9hDkDAwMxCAwGRm9sc29tMA0GCSqGSIb3DQEBBQUAA4IBAQCcN8KjjmR2H3LT5aL1SCFS4joy/7vAd3/xdJtkqrb3UAQHMdUUJQHf3frJsMJs22m0So0xs/f1sB15frC1LsQGF5+RYVXsClv0glStWbPYiqEfdM7dc/RDMRtrXKEH3sBlxMT7YS/g5E6qwmKZX9shQ3BYmeZi5A3DTzgHCbA3Cm4/MQbgWGjoamfWZ9EDk4Bww2y0ueRi60PfoLg43rcijr8Wf+JEzCRw040vIaH3DtFdmzvvGRdqE3YlEkrUL3gEIZNY3Po1NL4cb238vT5CHZTt9NyD7xSv0XkwOY4RbSUdYBsxfH3mEcdQ6LtJdfF1BUXfMThKN3TctFcY/dLF
                
        TagCertificate obj = new TagCertificate();
        obj.setCertificate(cert.getEncoded());
        TagCertificateCreateCriteria createCriteria = new TagCertificateCreateCriteria();
        createCriteria.setHardwareUuid(UUID.valueOf("0eae2c80-9d4a-4abf-9785-de90f0dd0f51"));
        obj = client.createCertificate(createCriteria);
        
        //TagCertificate retrieveCertificate = client.retrieveCertificate(UUID.valueOf("76cb8a0b-79b1-437a-9f0f-f8f6ad9b9df3"));
        //log.debug(retrieveCertificate.getIssuer());
        
        TagCertificateFilterCriteria criteria = new TagCertificateFilterCriteria();
        criteria.subjectEqualTo = "064866ea-620d-11e0-b1a9-001e671043c4";
        TagCertificateCollection objCollection = client.searchCertificates(criteria);
        for (TagCertificate cObj : objCollection.getTagCertificates()) {
            X509AttributeCertificate attrCert = X509AttributeCertificate.valueOf(cObj.getCertificate());
            log.debug(attrCert.getIssuer() + "::" + attrCert.getSubject());
        }
        
        TagCertificate editObj = new TagCertificate();
        editObj.setId(UUID.valueOf("695e8d32-0dd8-46bb-90d6-d2520ff5e2f0"));
//        editObj.setRevoked(false);
    }  
}
