/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.tag.model;

import com.intel.mtwilson.core.common.tag.model.TagCertificate;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.mtwilson.jaxrs2.DocumentCollection;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ssbangal
 */
@JacksonXmlRootElement(localName="certificate_collection")
public class TagCertificateCollection extends DocumentCollection<TagCertificate>{

    private final ArrayList<TagCertificate> tagCertificates = new ArrayList<TagCertificate>();
    
    @JsonSerialize(include=JsonSerialize.Inclusion.ALWAYS) // jackson 1.9
    @JsonInclude(JsonInclude.Include.ALWAYS)                // jackson 2.0
    @JacksonXmlElementWrapper(localName="certificates")
    @JacksonXmlProperty(localName="certificate")    
    public List<TagCertificate> getTagCertificates() { return tagCertificates; }

    @Override
    public List<TagCertificate> getDocuments() {
        return getTagCertificates();
    }
    
}
