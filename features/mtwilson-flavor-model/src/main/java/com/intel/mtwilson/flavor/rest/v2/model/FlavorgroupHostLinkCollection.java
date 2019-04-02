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
 * @author rksavino
 */
public class FlavorgroupHostLinkCollection extends DocumentCollection<FlavorgroupHostLink> {
    private final ArrayList<FlavorgroupHostLink> flavorgroupHostLinks = new ArrayList<>();
    
    @JsonInclude(JsonInclude.Include.ALWAYS)
    @JacksonXmlElementWrapper(localName="flavorgroup_host_links")
    @JacksonXmlProperty(localName="flavorgroup_host_link")
    public List<FlavorgroupHostLink> getFlavorgroupHostLinks() { return flavorgroupHostLinks; }
    
    @Override
    public List<FlavorgroupHostLink> getDocuments() {
        return getFlavorgroupHostLinks();
    }
}
