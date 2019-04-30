/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.tag.rest.v2.rpc;

import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.x509.X509Builder;
import com.intel.mtwilson.tag.common.X509AttrBuilder;
import com.intel.mtwilson.core.common.tag.model.X509AttributeCertificate;
import com.intel.mtwilson.tag.model.x509.UTF8NameValueSequence;
import com.intel.mtwilson.tag.selection.xml.AttributeType;
import com.intel.mtwilson.tag.selection.xml.DerAttributeType;
import com.intel.mtwilson.tag.selection.xml.SelectionType;
import java.io.IOException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import org.apache.commons.codec.binary.Base64;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class CacheTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CacheTest.class);

    protected X509AttributeCertificate createCertificate() throws NoSuchAlgorithmException {
        // create an issuer
        KeyPair cakey = RsaUtil.generateRsaKeyPair(2048); // throws NoSuchAlgorithmException
        X509Certificate cacert = X509Builder.factory().selfSigned("CN=Attr CA,OU=CPG,OU=DCSG,O=Intel,ST=CA,C=US", cakey).build();
        byte[] attributeCertificateBytes = X509AttrBuilder.factory()
                .subjectUuid(new UUID())
                .randomSerial()
                .issuerName(cacert)
                .issuerPrivateKey(cakey.getPrivate())
                .attribute("Country", "US")
                .attribute("State", "CA", "TX")
                .build();
        X509AttributeCertificate attributeCertificate = X509AttributeCertificate.valueOf(attributeCertificateBytes);
        return attributeCertificate;
    }
    
    @Test
    public void testCreateCertificate() throws NoSuchAlgorithmException {
        X509AttributeCertificate cert = createCertificate();
        log.debug("cert base64: {}", Base64.encodeBase64String(cert.getEncoded()));
    }
    
    protected SelectionType createSelectionSame() throws IOException {
        // country = US
        DerAttributeType countryValue = new DerAttributeType();
        countryValue.setValue(new UTF8NameValueSequence("Country", "US").toASN1Primitive().getEncoded());
        AttributeType country = new AttributeType();
        country.setOid("2.5.4.789.2");
        country.setDer(countryValue);
        // state = CA, TX
        DerAttributeType stateValue = new DerAttributeType();
        stateValue.setValue(new UTF8NameValueSequence("State", "CA", "TX").toASN1Primitive().getEncoded());
        AttributeType state = new AttributeType();
        state.setOid("2.5.4.789.2");
        state.setDer(stateValue);
        // selection
        SelectionType selectionType = new SelectionType();
        selectionType.getAttribute().add(country);
        selectionType.getAttribute().add(state);
        return selectionType;
    }
}
