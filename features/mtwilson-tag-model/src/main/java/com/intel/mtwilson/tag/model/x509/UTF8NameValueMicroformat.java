/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.tag.model.x509;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.DERUTF8String;

/**
 * OID: 2.5.4.789.1
 * @author jbuhacoff
 */
public class UTF8NameValueMicroformat extends ASN1Object {
    public final static String OID = "2.5.4.789.1";
    private DERUTF8String microformat; // name=value
    private String name;
    private String value;
    
    public UTF8NameValueMicroformat(String name, String value) {
        this.microformat = new DERUTF8String(String.format("%s=%s", name, value));
        this.name = name;
        this.value = value;
    }
    
    public UTF8NameValueMicroformat(DERUTF8String microformat) {
        this.microformat = microformat;
        String[] parts = microformat.getString().split("=");
        this.name = parts[0];
        this.value = parts[1];
    }
    
    @JsonIgnore
    public DERUTF8String getMicroformat() {
        return microformat;
    }

    public String getName() {
        return name;
    }
    
    public String getValue() {
        return value;
    }
    
    
    @Override
    public ASN1Primitive toASN1Primitive() {
        return microformat;
    }
    
}
