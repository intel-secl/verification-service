/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.tag.model.x509;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.bouncycastle.asn1.*;

/**
 * OID: 2.5.4.789.2
 * @author jbuhacoff
 */
public class UTF8NameValueSequence extends ASN1Object {
    public final static String OID = "2.5.4.789.2";
    private DERUTF8String name;
    private ASN1Sequence values;
    
    public UTF8NameValueSequence(String name, String... values) {
        this.name = new DERUTF8String(name);
        ASN1EncodableVector v = new ASN1EncodableVector();
        for(String value : values) {
            v.add(new DERUTF8String(value));
        }
        this.values = new DERSequence(v);
    }
    
    public UTF8NameValueSequence(DERUTF8String name, DERSequence values) {
        this.name = name;
        this.values = values;
    }
    
    public UTF8NameValueSequence(ASN1Sequence sequence) {
        this.name = DERUTF8String.getInstance(sequence.getObjectAt(0));
        this.values = (ASN1Sequence) DERSequence.getInstance(sequence.getObjectAt(1)).toASN1Primitive();
    }

    @JsonIgnore
    public DERUTF8String getNameDER() {
        return name;
    }

    @JsonIgnore
    public ASN1Sequence getValuesDER() {
        return values;
    }
    
    public String getName() {
        return name.getString();
    }
    
    public List<String> getValues() {
        ArrayList<String> list = new ArrayList<>();
        Enumeration e = values.getObjects();
        while(e.hasMoreElements()) {
            list.add(DERUTF8String.getInstance(e.nextElement()).getString());
        }
        return list;
    }
    
    
    @Override
    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(name);
        v.add(values);
        return new DERSequence(v);
    }
    
}
