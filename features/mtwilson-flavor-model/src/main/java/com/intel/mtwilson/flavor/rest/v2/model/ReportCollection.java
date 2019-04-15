/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.flavor.rest.v2.model;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JacksonXmlRootElement(localName="report_collection")
public class ReportCollection extends DocumentCollection<Report>{

    private final ArrayList<Report> reports = new ArrayList<Report>();
    
    @JsonInclude(JsonInclude.Include.ALWAYS)                // jackson 2.0
    @JacksonXmlElementWrapper(localName="report")
    @JacksonXmlProperty(localName="report")    
    public List<Report> getReports() { return reports; }
    
    @Override
    public List<Report> getDocuments() {
        return getReports();
    }
    
}
