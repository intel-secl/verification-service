/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.rest.v2.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.intel.mtwilson.jaxrs2.DocumentCollection;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author hmgowda
 */
public class HostStatusCollection extends DocumentCollection<HostStatus> {
    private final ArrayList<HostStatus> hostStatus = new ArrayList<>();
    
    @JsonInclude(JsonInclude.Include.ALWAYS)
    @JacksonXmlElementWrapper(localName="hoststatus")
    @JacksonXmlProperty(localName="hoststatus")    
    public List<HostStatus> getHostStatus() { return hostStatus; }
    
    @Override
    public List<HostStatus> getDocuments() {
        return getHostStatus();
    }
}