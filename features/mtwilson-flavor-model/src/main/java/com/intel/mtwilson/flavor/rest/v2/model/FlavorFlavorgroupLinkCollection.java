/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.rest.v2.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author hmgowda
 */
public class FlavorFlavorgroupLinkCollection {
    private final ArrayList<FlavorFlavorgroupLink> flavorFlavorgroupLinks = new ArrayList<>();
    
    @JsonInclude(JsonInclude.Include.ALWAYS)
    @JacksonXmlElementWrapper(localName="flavor_flavorgroup_links")
    @JacksonXmlProperty(localName="flavor_flavorgroup_link")
    public List<FlavorFlavorgroupLink> getFlavorFlavorgroupLinks() { return flavorFlavorgroupLinks; }
    
    public List<FlavorFlavorgroupLink> getDocuments() {
        return getFlavorFlavorgroupLinks();
    }
}